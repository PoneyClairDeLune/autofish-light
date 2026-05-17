package troy.autofish;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.StringHelper;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import troy.autofish.modded.Common;
import troy.autofish.monitor.FishMonitorMP;
import troy.autofish.monitor.FishMonitorMPMotion;
import troy.autofish.monitor.FishMonitorMPSound;
import troy.autofish.scheduler.ActionType;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Autofish {
	private MinecraftClient client;
	private FabricModAutofish modAutofish;
	private FishMonitorMP fishMonitorMP;

	private boolean hookExists = false;
	private boolean playerAlreadyAlerted = false;
	private boolean playerOpenCheckAlreadyPassed = false;
	private long hookRemovedAt = 0L;
	public long timeMillis = 0L;

	public Autofish(FabricModAutofish modAutofish) {
		this.modAutofish = modAutofish;
		this.client = MinecraftClient.getInstance();
		setDetection();
		Common.initialize(modAutofish);
		LogSession.info("Autofish is now activated!");

		// Initiate the repeating action for persistent mode casting.
		// Honestly, this needs a better implementation...
		modAutofish.getScheduler().scheduleRepeatingAction(10000, () -> {
			if (!isHoldingFishingRod()) return;
			if (!modAutofish.getConfig().isPersistentMode()) return;
			if (Common.shouldNotReel(getHeldItem())) return;
			if (hookExists) {
				if (isBobberInWater()) return;
				else useRod();
			}
			if (modAutofish.getScheduler().isRecastQueued()) return;
			useRod();
		});
	}

	public void tick(MinecraftClient client) {
		if (client.world == null || client.player == null) return;
		if (
			modAutofish.getConfig().isAutofishEnabled()
		) {
			timeMillis = Util.getMeasuringTimeMs(); // Update current working time for this tick.
			ProjectileEntity bobber = Common.getPlayerBobber(client.player);
			if (isHoldingFishingRod()) {
				if (bobber == null) {
					removeHook();
					return;
				}
				hookExists = true;
				// Multiplayer catch listener
				if (shouldUseMPDetection()) {
					// Multiplayer-only, send tick event to monitor.
					fishMonitorMP.hookTick(this, client, bobber);
				}
			} else {
				removeHook();
			}
		}
	}

	/**
	* Callback from mixin for the catchingFish method of the EntityFishHook.
	* For singleplayer detection only.
	*/
	public void tickFishingLogic(Entity owner, int ticksCatchable) {
		// This callback will come from the Server thread. Use client.execute() to run this action in the Render thread.
		client.execute(() -> {
			if (!modAutofish.getConfig().isAutofishEnabled() || shouldUseMPDetection()) return;
			// Null checks for sanity.
			if (
				client.player == null ||
				Common.getPlayerBobber(client.player) == null
			) return;
			// The hook can be caught with the correct player.
			if (
				ticksCatchable > 0 &&
				owner.getUuid().compareTo(client.player.getUuid()) == 0
			) {
				catchFish();
			}
		});
	}

	/**
	* Callback from mixin when sound and motion packets are received
	* For multiplayer detection only
	*/
	public void handlePacket(Packet<?> packet) {
		if (modAutofish.getConfig().isAutofishEnabled()) {
			if (shouldUseMPDetection()) {
				fishMonitorMP.handlePacket(this, packet, client);
			}
		}
	}

	/**
	* Callback from mixin when chat packets are received
	* For multiplayer detection only
	*/
	public void handleChat(GameMessageS2CPacket packet) {
		if (
			!modAutofish.getConfig().isAutofishEnabled() ||
			client.isInSingleplayer() ||
			!isHoldingFishingRod()
		) return;
		// Check if the hook either exists or was just removed.
		// This prevents false casts if a rod is held but isn't used for fishing.
		if (hookExists || (timeMillis - hookRemovedAt < 2000)) {
			//make sure there is actually something there in the regex field
			if (
				org.apache.commons.lang3.StringUtils.deleteWhitespace(
					modAutofish.getConfig().getClearLagRegex()
				).isEmpty()
			) return;
			// Check if it matches.
			Matcher matcher = Pattern.compile(
				modAutofish.getConfig().getClearLagRegex(),
				Pattern.CASE_INSENSITIVE
			).matcher(StringHelper.stripTextFormat(
				packet.content().getString()
			));
			if (matcher.find()) {
				queueRecast();
			}
		}
	}

	public void catchFish() {
		if (!modAutofish.getScheduler().isRecastQueued()) { // Prevents double reels.
			if (client.player != null) {
				detectOpenWater(Common.getPlayerBobber(client.player));
			}
			LogSession.info("Reeling scheduled.");
			// Queue actions.
			queueRodSwitch();
			queueRecast();
			modAutofish.getScheduler().scheduleAction(ActionType.REEL_IN, modAutofish.getConfig().getReelInDelay(), this::useRod);
		} else {
			LogSession.info("Reeling prevented.");
		}
	}

	public void queueRecast() {
		modAutofish.getScheduler().scheduleAction(
			ActionType.RECAST,
			getRandomDelay() + modAutofish.getConfig().getReelInDelay(),
			() -> {
				if (hookExists) return;
				if (!isHoldingFishingRod()) return;
				ItemStack heldOnHand = getHeldItem();
				if (Common.shouldNotReel(heldOnHand)) return;
				useRod();
			}
		);
	}

	private void queueRodSwitch() {
		modAutofish.getScheduler().scheduleAction(
			ActionType.ROD_SWITCH,
			(long) (getRandomDelay() * 0.83) + modAutofish.getConfig().getReelInDelay(),
			() -> {
				if (!modAutofish.getConfig().isMultiRod()) return;
				switchToFirstRod(client.player);
			}
		);
	}

	private void detectOpenWater(ProjectileEntity bobber) {
		/*
		* To catch items in the treasure category, the bobber must be in open water,
		* defined as the 5×4×5 vicinity around the bobber resting on the water surface
		* (2 blocks away horizontally, 2 blocks above the water surface, and 2 blocks deep).
		* Each horizontal layer in this area must consist only of air and lily pads or water source blocks,
		* waterlogged blocks without collision (such as signs, kelp, or coral fans), and bubble columns.
		* (from Minecraft wiki)
		*/
		if (!modAutofish.getConfig().isOpenWaterDetectEnabled()) return;
		if (bobber == null) return;
		int x = bobber.getBlockX();
		int y = bobber.getBlockY();
		int z = bobber.getBlockZ();
		boolean flag = true;
		// Refactor note: It seems like not all blocks listed were matched. Perhaps time to look at later?
		for (int yi = -2; yi <= 2; yi ++) {
			if (!(
				BlockPos.stream(x - 2, y + yi, z - 2, x + 2, y + yi, z + 2).allMatch((blockPos ->
					// Every block is water.
					Common.isFishableLiquid(bobber.getEntityWorld().getBlockState(blockPos).getBlock())
				)) ||
				BlockPos.stream(x - 2, y + yi, z - 2, x + 2, y + yi, z + 2).allMatch((blockPos ->
					// Or every block is air or lily pad.
					bobber.getEntityWorld().getBlockState(blockPos).getBlock() == Blocks.AIR ||
					Common.isFishableFlora(bobber.getEntityWorld().getBlockState(blockPos).getBlock())
				))
			)) {
				// Didn't pass the open water check.
				if (!playerAlreadyAlerted) {
					PlayerEntity clientPlayer = Common.getPlayerOwner(bobber);
					if (clientPlayer != null) {
						clientPlayer.sendMessage(
							Text.translatable("info.autofish.open_water_detection.fail"),
							true
						);
						playerAlreadyAlerted = true;
						playerOpenCheckAlreadyPassed = false;
					}
					LogSession.warn("Bobber wasn't in open water.");
				}
				flag = false;
			}
		}
		if (flag && !playerOpenCheckAlreadyPassed) {
			PlayerEntity clientPlayer = Common.getPlayerOwner(bobber);
			if (clientPlayer != null) {
				clientPlayer.sendMessage(
					Text.translatable("info.autofish.open_water_detection.success"),
					true
				);
				playerOpenCheckAlreadyPassed = true;
				playerAlreadyAlerted = false;
			}
			LogSession.info("Bobber was in open water.");
		}
	}

	/**
	* When the hook disappears, call this method.
	*/
	private void removeHook() {
		if (hookExists) {
			hookExists = false;
			hookRemovedAt = timeMillis;
			fishMonitorMP.handleHookRemoved();
		}
	}

	public void switchToFirstRod(ClientPlayerEntity player) {
		if (player == null) return;
		PlayerInventory inventory = player.getInventory();
		int inventorySize = inventory.main.size();
		for (int i = 0; i < inventorySize; i ++) {
			if (i >= 9) break; // Hotbar only.
			ItemStack slot = inventory.main.get(i);
			if (Common.isFishingRod(slot.getItem())) {
				if (modAutofish.getConfig().isNoBreak()) {
					if (slot.getDamage() + Common.damageSafeMargin < slot.getMaxDamage()) {
						inventory.selectedSlot = i;
						return;
					}
				} else {
					inventory.selectedSlot = i;
					return;
				}
			}
		}
	}

	private Block lastBlock = null;
	public boolean isBobberInWater() {
		if (client.player == null || client.world == null) {
			lastBlock = null;
			return false;
		}
		ProjectileEntity bobber = Common.getPlayerBobber(client.player);
		if (bobber == null) return false;
		Block currentBlock = client.world.getBlockState(bobber.getBlockPos()).getBlock();
		String currentBlockId = Common.getRegistryKey(currentBlock);
		boolean waterVerdict = false;
		switch (currentBlockId) {
			case "minecraft:water": {
				waterVerdict = true;
				break;
			}
			default: {
				waterVerdict = Common.isFishableLiquid(currentBlock);
			}
		}
		if (currentBlock != lastBlock) {
			LogSession.info("Block " + currentBlockId + (waterVerdict ? " is" : " isn't") + " fishable liquid.");
		}
		lastBlock = currentBlock;
		return waterVerdict;
	}

	public void useRod() {
		if (client.player != null && client.world != null) {
			Hand hand = getCorrectHand();
			ActionResult actionResult = null;
			if (client.interactionManager != null) {
				actionResult = client.interactionManager.interactItem(client.player, hand);
			}
			if (actionResult != null && actionResult.isAccepted()) {
				if (actionResult.shouldSwingHand()) {
					client.player.swingHand(hand);
				}
				client.gameRenderer.firstPersonRenderer.resetEquipProgress(hand);
			}
			LogSession.debug("Current rod has been used.");
		}
	}

	private boolean lastHeldFishingRod = false;
	public boolean isHoldingFishingRod() {
		Item heldItem = getHeldItem().getItem();
		boolean heldRod = Common.isFishingRod(heldItem);
		if (lastHeldFishingRod != heldRod) {
			LogSession.debug((heldRod ? "H" : "Not h") + "olding fishing rod: " + Common.getRegistryKey(heldItem) + ".");
		}
		lastHeldFishingRod = heldRod;
		return heldRod;
	}

	private Hand getCorrectHand() {
		if (!modAutofish.getConfig().isMultiRod()) {
			if (
				client.player != null &&
				Common.isFishingRod(client.player.getOffHandStack().getItem())
			) return Hand.OFF_HAND;
		}
		return Hand.MAIN_HAND;
	}

	private ItemStack getHeldItem() {
		if (client.player == null) return ItemStack.EMPTY;
		if (!modAutofish.getConfig().isMultiRod()) {
			if (
				Common.isFishingRod(client.player.getOffHandStack().getItem())
			) return client.player.getOffHandStack();
		}
		return client.player.getMainHandStack();
	}

	public void setDetection() {
		if (modAutofish.getConfig().isUseSoundDetection()) {
			fishMonitorMP = new FishMonitorMPSound();
		} else {
			fishMonitorMP = new FishMonitorMPMotion();
		}
	}

	private boolean shouldUseMPDetection(){
		if (modAutofish.getConfig().isForceMPDetection()) return true;
		return !client.isInSingleplayer();
	}

	private long getRandomDelay(){
		return (
			Math.random() >= 0.5 ?
			(long) (modAutofish.getConfig().getRecastDelay() * (1 - (Math.random() * modAutofish.getConfig().getRandomDelay() * 0.01))) :
			(long) (modAutofish.getConfig().getRecastDelay() * (1 + (Math.random() * modAutofish.getConfig().getRandomDelay() * 0.01)))
		);
	}
}
