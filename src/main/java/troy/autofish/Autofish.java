package troy.autofish;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundSystemChatPacket;
import net.minecraft.network.chat.Component;
import net.minecraft.util.StringUtil;
import net.minecraft.util.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import troy.autofish.modded.Common;
import troy.autofish.monitor.FishMonitorMP;
import troy.autofish.monitor.FishMonitorMPMotion;
import troy.autofish.monitor.FishMonitorMPSound;
import troy.autofish.scheduler.ActionType;
import troy.autofish.utils.*;

import org.apache.commons.lang3.StringUtils;

public class Autofish {
	private FabricModAutofish modAutofish;
	private FishMonitorMP fishMonitorMP;

	private boolean hookExists = false;
	private boolean playerAlreadyAlerted = false;
	private boolean playerOpenCheckAlreadyPassed = false;
	private long hookRemovedAt = 0L;
	public long timeMillis = 0L;

	public Autofish(FabricModAutofish modAutofish) {
		this.modAutofish = modAutofish;
		setDetection();
		Common.initialize(modAutofish);
		LogSession.info("Autofish is now activated!");

		// Initiate the repeating action for persistent mode casting.
		// Honestly, this needs a better implementation...
		modAutofish.getScheduler().scheduleRepeatingAction(10000, () -> {
			if (!isRodHeld(EnvUtils.client.player)) return;
			if (!modAutofish.getConfig().isPersistentMode()) return;
			if (Common.shouldNotReel(EnvUtils.client.player, false)) return;
			if (hookExists) {
				if (isBobberInWater()) return;
				else useRodItem(EnvUtils.client.player);
			}
			if (modAutofish.getScheduler().isRecastQueued()) return;
			useRodItem(EnvUtils.client.player);
		});
	}

	public void tick(Minecraft client) {
		if (client.level == null || client.player == null) return;
		if (
			modAutofish.getConfig().isAutofishEnabled()
		) {
			timeMillis = Util.getMillis(); // Update current working time for this tick.
			Projectile bobber = Common.getPlayerBobber(client.player);
			if (isRodHeld(client.player)) {
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
		EnvUtils.client.execute(() -> {
			if (!modAutofish.getConfig().isAutofishEnabled() || shouldUseMPDetection()) return;
			// Null checks for sanity.
			if (
				EnvUtils.client.player == null ||
				Common.getPlayerBobber(EnvUtils.client.player) == null
			) return;
			// The hook can be caught with the correct player.
			if (
				ticksCatchable > 0 &&
				owner.getUUID().compareTo(EnvUtils.client.player.getUUID()) == 0
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
				fishMonitorMP.handlePacket(this, packet, EnvUtils.client);
			}
		}
	}

	/**
	* Callback from mixin when chat packets are received
	* For multiplayer detection only
	*/
	public void handleChat(ClientboundSystemChatPacket packet) {
		if (
			!modAutofish.getConfig().isAutofishEnabled() ||
			EnvUtils.client.isLocalServer() ||
			!isRodHeld(EnvUtils.client.player)
		) return;
		// Check if the hook either exists or was just removed.
		// This prevents false casts if a rod is held but isn't used for fishing.
		if (hookExists || (timeMillis - hookRemovedAt < 2000)) {
			//make sure there is actually something there in the regex field
			if (
				StringUtils.deleteWhitespace(
					modAutofish.getConfig().getClearLagRegex()
				).isEmpty()
			) return;
			// Check if it matches.
			Matcher matcher = Pattern.compile(
				modAutofish.getConfig().getClearLagRegex(),
				Pattern.CASE_INSENSITIVE
			).matcher(StringUtil.stripColor(
				packet.content().getString()
			));
			if (matcher.find()) {
				queueRecast();
			}
		}
	}

	public void catchFish() {
		if (!modAutofish.getScheduler().isRecastQueued()) { // Prevents double reels.
			if (EnvUtils.client.player != null) {
				detectOpenWater(Common.getPlayerBobber(EnvUtils.client.player));
			}
			LogSession.info("Reeling scheduled.");
			// Queue actions.
			queueRodSwitch();
			queueRecast();
			modAutofish.getScheduler().scheduleAction(ActionType.REEL_IN, modAutofish.getConfig().getReelInDelay(), () -> useRodItem(EnvUtils.client.player));
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
				if (!isRodHeld(EnvUtils.client.player)) return;
				ItemStack heldOnHand = PlayerUtils.getHeldStack(EnvUtils.client.player, this::isOffhandPredicate);
				if (Common.shouldNotReel(heldOnHand)) return;
				useRodItem(EnvUtils.client.player);
			}
		);
	}

	private void queueRodSwitch() {
		modAutofish.getScheduler().scheduleAction(
			ActionType.ROD_SWITCH,
			(long) (getRandomDelay() * 0.83) + modAutofish.getConfig().getReelInDelay(),
			() -> {
				if (!modAutofish.getConfig().isMultiRod()) return;
				switchToFirstRod(EnvUtils.client.player);
			}
		);
	}

	private void detectOpenWater(Projectile bobber) {
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
				BlockPos.betweenClosedStream(x - 2, y + yi, z - 2, x + 2, y + yi, z + 2).allMatch((blockPos ->
					// Every block is water.
					Common.isFishableLiquid(bobber.level().getBlockState(blockPos).getBlock())
				)) ||
				BlockPos.betweenClosedStream(x - 2, y + yi, z - 2, x + 2, y + yi, z + 2).allMatch((blockPos ->
					// Or every block is air or lily pad.
					bobber.level().getBlockState(blockPos).getBlock() == Blocks.AIR ||
					Common.isFishableFlora(bobber.level().getBlockState(blockPos).getBlock())
				))
			)) {
				// Didn't pass the open water check.
				if (!playerAlreadyAlerted) {
					Player bobberOwner = Common.getPlayerOwner(bobber);
					if (bobberOwner != null) {
						bobberOwner.sendOverlayMessage(
							Component.translatable("info.autofish.open_water_detection.fail")
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
			Player bobberOwner = Common.getPlayerOwner(bobber);
			if (bobberOwner != null) {
				bobberOwner.sendOverlayMessage(
					Component.translatable("info.autofish.open_water_detection.success")
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

	public void switchToFirstRod(LocalPlayer player) {
		if (player == null) return;
		Inventory inventory = player.getInventory();
		NonNullList<ItemStack> mainInventory = inventory.getNonEquipmentItems();
		int inventorySize = mainInventory.size();
		for (int i = 0; i < inventorySize; i ++) {
			if (i >= 9) break; // Hotbar only.
			ItemStack slotStack = mainInventory.get(i);
			if (Common.isFishingRod(slotStack)) {
				if (modAutofish.getConfig().isNoBreak()) {
					if (slotStack.getDamageValue() + Common.damageSafeMargin < slotStack.getMaxDamage()) {
						inventory.setSelectedSlot(i);
						return;
					}
				} else {
					inventory.setSelectedSlot(i);
					return;
				}
			}
		}
	}

	private Block lastBlock = null;
	public boolean isBobberInWater() {
		if (EnvUtils.client.player == null || EnvUtils.client.level == null) {
			lastBlock = null;
			return false;
		}
		Projectile bobber = Common.getPlayerBobber(EnvUtils.client.player);
		if (bobber == null) return false;
		Block currentBlock = EnvUtils.client.level.getBlockState(bobber.blockPosition()).getBlock();
		String currentBlockId = RegistryUtils.getIdKey(currentBlock);
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

	public void setDetection() {
		if (modAutofish.getConfig().isUseSoundDetection()) {
			fishMonitorMP = new FishMonitorMPSound();
		} else {
			fishMonitorMP = new FishMonitorMPMotion();
		}
	}

	private boolean shouldUseMPDetection(){
		if (modAutofish.getConfig().isForceMPDetection()) return true;
		return !EnvUtils.client.isLocalServer();
	}

	private long getRandomDelay(){
		return (
			Math.random() >= 0.5 ?
			(long) (modAutofish.getConfig().getRecastDelay() * (1 - (Math.random() * modAutofish.getConfig().getRandomDelay() * 0.01))) :
			(long) (modAutofish.getConfig().getRecastDelay() * (1 + (Math.random() * modAutofish.getConfig().getRandomDelay() * 0.01)))
		);
	}

	// Rewritten sections go below.
	// Properties for logging.
	private boolean lastHeldRod = false;
	// Detection.
	public boolean isRodHeld(Player player) {
		ItemStack heldItemStack = PlayerUtils.getHeldStack(player, this::isOffhandPredicate);
		boolean rodHeld = Common.isFishingRod(heldItemStack);
		if (lastHeldRod != rodHeld) {
			LogSession.debug((rodHeld ? "H" : "Not h") + "olding fishing rod: " + RegistryUtils.getIdKey(heldItemStack) + ".");
		}
		lastHeldRod = rodHeld;
		return rodHeld;
	}
	private boolean isOffhand(Player player) {
		return PlayerUtils.matchOffhandItem(player, this::isOffhandPredicate);
	}
	private boolean isOffhandPredicate(ItemStack itemStack) {
		return Common.isFishingRod(itemStack);
	}
	// Interaction.
	public void useRodItem(Player player) {
		if (EnvUtils.client.level == null) return;
		if (EnvUtils.client.gameMode == null) return;
		if (player == null) return;
		boolean targetHand = isOffhand(EnvUtils.client.player);
		//LogSession.debug("Selected " + (targetHand ? "main " : "off") + "hand.");
		PlayerUtils.useItem(EnvUtils.client.player, targetHand);
		return;
	}
}
