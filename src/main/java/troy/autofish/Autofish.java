package troy.autofish;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cc.ltgc.luneApi.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundSystemChatPacket;
import net.minecraft.network.chat.Component;
import net.minecraft.util.StringUtil;
import net.minecraft.util.Util;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.Level;
import troy.autofish.modded.Common;
import troy.autofish.monitor.FishMonitorMP;
import troy.autofish.monitor.FishMonitorMPMotion;
import troy.autofish.monitor.FishMonitorMPSound;
import troy.autofish.scheduler.ActionType;

import org.apache.commons.lang3.StringUtils;

public class Autofish {
	private FabricModAutofish modAutofish;
	private FishMonitorMP fishMonitorMP;

	private boolean hookExists = false;
	private long hookRemovedAt = 0L;
	public long timeMillis = 0L;

	public Autofish(FabricModAutofish modAutofish) {
		this.modAutofish = modAutofish;
		setDetection();
		Common.initialize(modAutofish);
		LogSession.info("Autofish is now activated!");

		// Initiate the repeating action for persistent mode casting.
		// Invocation of the new implementation here.
		modAutofish.getScheduler().scheduleRepeatingAction(200, this::persistenceModeTick);
	}

	public void tick(Minecraft client) {
		if (client.level == null || client.player == null) return;
		if (
			modAutofish.getConfig().modEnabled()
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
				fishMonitorMP.hookTick(this, client, bobber);
			} else {
				removeHook();
			}
		}
	}

	/**
	* Callback from mixin when sound and motion packets are received
	* For multiplayer detection only
	*/
	public void handlePacket(Packet<?> packet) {
		if (modAutofish.getConfig().modEnabled()) {
			fishMonitorMP.handlePacket(this, packet, EnvUtils.client());
		}
	}

	/**
	* Callback from mixin when chat packets are received
	* For multiplayer detection only
	*/
	public void handleChat(ClientboundSystemChatPacket packet) {
		if (
			!modAutofish.getConfig().modEnabled() ||
			EnvUtils.client().isLocalServer() ||
			!isRodHeld(EnvUtils.client().player)
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

	public void reelRod() {
		boolean emitReelingLogs = !modAutofish.getConfig().noisyDetection();
		if (!modAutofish.getScheduler().isRecastQueued()) { // Prevents double reels.
			LocalPlayer player = EnvUtils.client().player;
			if (player != null) {
				checkAndNotifyOpenWater(Common.getPlayerBobber(player), player);
			}
			if (emitReelingLogs) LogSession.info("Reeling scheduled.");
			// Queue actions.
			queueRodSwitch();
			queueRecast();
			modAutofish.getScheduler().scheduleAction(ActionType.REEL_IN, modAutofish.getConfig().getReelInDelay(), () -> useRodItem(player));
		} else {
			if (emitReelingLogs) LogSession.info("Reeling prevented.");
		}
	}

	public void queueRecast() {
		modAutofish.getScheduler().scheduleAction(
			ActionType.RECAST,
			getRandomDelay() + modAutofish.getConfig().getReelInDelay(),
			() -> {
				if (hookExists) return;
				if (!isRodHeld(EnvUtils.client().player)) return;
				ItemStack heldOnHand = PlayerUtils.getHeldStack(EnvUtils.client().player, this::isOffhandPredicate);
				if (Common.shouldNotReel(heldOnHand)) return;
				useRodItem(EnvUtils.client().player);
			}
		);
	}

	private void queueRodSwitch() {
		modAutofish.getScheduler().scheduleAction(
			ActionType.ROD_SWITCH,
			(long) (getRandomDelay() * 0.83) + modAutofish.getConfig().getReelInDelay(),
			() -> {
				if (!modAutofish.getConfig().multiRod()) return;
				switchToFirstRod(EnvUtils.client().player);
			}
		);
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
				if (modAutofish.getConfig().rodBreakAvoided()) {
					if (slotStack.getDamageValue() + Common.damageSafeMargin() < slotStack.getMaxDamage()) {
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

	public void setDetection() {
		if (modAutofish.getConfig().soundUsed()) {
			fishMonitorMP = new FishMonitorMPSound();
		} else {
			fishMonitorMP = new FishMonitorMPMotion();
		}
	}

	private long getRandomDelay(){
		return (
			Math.random() >= 0.5 ?
			(long) (modAutofish.getConfig().getRecastDelay() * (1 - (Math.random() * modAutofish.getConfig().getRandomDelay() * 0.01))) :
			(long) (modAutofish.getConfig().getRecastDelay() * (1 + (Math.random() * modAutofish.getConfig().getRandomDelay() * 0.01)))
		);
	}

	// Rewritten sections go below.
	// Properties for state keeping.
	private boolean bobberOwnerNotNotified = true;
	private BlockState lastBlock = null;
	private boolean lastHeldRod = false;
	private boolean lastNotifyAttemptOpenWater = false;
	private long persistenceTick = 0; // 5 ≈ 1s
	private long persistenceTickLegacyLast = -65536;
	// Detection.
	/** Common method to determine if a method should return early. */
	private boolean earlyReturn(LocalPlayer player) {
		if (player == null) return true;
		if (EnvUtils.client().level == null) return true;
		if (EnvUtils.client().gameMode == null) return true;
		return false;
	}
	/** Detect if the bobber is in water. */
	private boolean isBobberInWater(LocalPlayer player) {
		if (earlyReturn(player)) {
			lastBlock = null;
			return false;
		}
		Projectile bobber = Common.getPlayerBobber(player);
		if (bobber == null) return false;
		BlockState currentBlock = EnvUtils.client().level.getBlockState(bobber.blockPosition());
		boolean waterVerdict = Common.isLiquidFishableTo(player, currentBlock, false);
		if (currentBlock != lastBlock) {
			LogSession.info("Block " + RegistryUtils.getIdKey(currentBlock) + (waterVerdict ? " is" : " isn't") + " fishable liquid.");
		}
		lastBlock = currentBlock;
		return waterVerdict;
	}
	/** Detect if the bobber entity is in open water. Useful to determine if treasure items are going to be obtained. */
	private boolean isInOpenWater(Projectile bobber, Player player, boolean useNewerMethod) {
		/*
		Source: https://minecraft.wiki/w/Fishing#Items_obtainable
		The open water check is passed when all horizontal layers within the bounding box match only one of the two criteria below.
		1. All of them are air blocks or lily pads.
		2. All of them are water source blocks, or waterlogged blocks without collision. Flowing water is prohibited.
		As an example, if a layer has both air blocks and water source blocks, the check fails. And even with the second criteria, if the fishing rod is in the bubble column directly, the check fails.
		The detection bounding box is defined as 5×4×5 (w×h×d), centered around the bobber entity. 	As such, the box is defined as [a-2, a+2] on both horizontal directions, however this left ambiguity on the vertical axis. Is it (a-2, a+2] or [a-2, a+2) that formed the four-block height?
		*/
		// TODO: Determine what the vertical four block actually means. Could it be that the actual detection range is [-1, 2] (the bounding box used by the new method), since when the fish bites the bobber gets pulled into the source block? At least how Minecraft Wiki worded it suggests that this is the case.
		if (bobber == null) return false;
		// Old detection method inherited with some refactoring.
		Level bobberWorld = bobber.level();
		int blockX = bobber.getBlockX();
		int blockY = bobber.getBlockY();
		int blockZ = bobber.getBlockZ();
		boolean verdict = false;
		// Bounding box check
		for (int deltaY = (useNewerMethod ? -1 : -2); deltaY <= 2; deltaY ++) {
			boolean liquidVerdict = BlockPos.betweenClosedStream(
				blockX - 2, blockY + deltaY, blockZ - 2,
				blockX + 2, blockY + deltaY, blockZ + 2
			).allMatch(blockPos -> {
				BlockState blockState = bobberWorld.getBlockState(blockPos);
				if (useNewerMethod) return Common.isLiquidloggedValid(player, blockState, false);
				return Common.isLiquidFishableTo(player, blockState, false);
			});
			boolean blockVerdict = BlockPos.betweenClosedStream(
				blockX - 2, blockY + deltaY, blockZ - 2,
				blockX + 2, blockY + deltaY, blockZ + 2
			).allMatch(blockPos -> Common.isBlockNegligible(bobberWorld.getBlockState(blockPos)));
			verdict = liquidVerdict || blockVerdict;
			if (!verdict) break;
		}
		// Final check
		if (useNewerMethod && verdict) {
			// The older implementation does not directly check for bubble columns.
			if (
				bobberWorld.getBlockState(
					bobber.blockPosition()
				).getBlock() == Blocks.BUBBLE_COLUMN
			) verdict = false;
		}
		return verdict;
	}
	/** Detect if a fishing rod is held. */
	private boolean isRodHeld(LocalPlayer player) {
		if (earlyReturn(player)) {
			lastHeldRod = false;
			return false;
		}
		ItemStack heldItemStack = PlayerUtils.getHeldStack(player, this::isOffhandPredicate);
		boolean rodHeld = Common.isFishingRod(heldItemStack);
		if (lastHeldRod != rodHeld) {
			String message = (rodHeld ? "H" : "Not h") + "olding fishing rod: " + RegistryUtils.getIdKey(heldItemStack) + ".";
			if (modAutofish.getConfig().noisyDetection()) {
				LogSession.info(message);
			} else {
				LogSession.debug(message);
			}
		}
		lastHeldRod = rodHeld;
		return rodHeld;
	}
	/** Determine if the item on the offhand should be used. */
	private boolean isOffhand(Player player) {
		return PlayerUtils.matchOffhandItem(player, this::isOffhandPredicate);
	}
	/** The predicate used to determine if the offhand should be used. */
	private boolean isOffhandPredicate(ItemStack itemStack) {
		return Common.isFishingRod(itemStack);
	}
	// Interaction.
	/** Notify the player of the open water check status. */
	private void notifyOpenWater(Projectile bobber, Player player, boolean checkResult, boolean useNewerMethod, boolean isNoisy) {
		Player bobberOwner = Common.getPlayerOwner(bobber);
		if (bobberOwnerNotNotified || isNoisy || (checkResult ^ lastNotifyAttemptOpenWater)) {
			LogSession.info("Bobber " + (checkResult ? "was" : "wasn't") + " in open water.");
			if (bobberOwner != null && (!useNewerMethod || player == bobberOwner)) {
				bobberOwner.sendOverlayMessage(
					Component.translatable(checkResult ? "info.autofish.open_water_detection.success" : "info.autofish.open_water_detection.fail")
				);
				bobberOwnerNotNotified = false;
			} else {
				bobberOwnerNotNotified = true;
			}
		}
		lastNotifyAttemptOpenWater = checkResult;
	}
	/** Nullify item usage by attempting to switch away from fishing rods, and returns <code>true</code> when successful. Useful for avoiding rod usage when avoidable with this method, while still allowing hard cancellation via explicit usage. */
	private boolean nullifyRodUsage(LocalPlayer player) {
		// TODO: Implement natural slot shifting - Detect closest unmatched slot on either direction of scrolling, then decide which direction to scroll to accordingly. Should be useful to help evade overly stringent server-side anti-cheat.
		return false;
	}
	/** Use the fishing rod item. */
	private void useRodItem(LocalPlayer player) {
		if (earlyReturn(player)) return;
		boolean targetHand = isOffhand(EnvUtils.client().player);
		//LogSession.debug("Selected " + (targetHand ? "main " : "off") + "hand.");
		PlayerUtils.useItem(EnvUtils.client().player, targetHand);
		return;
	}
	// Combined actions.
	private void checkAndNotifyOpenWater(Projectile bobber, Player player) {
		if (!modAutofish.getConfig().openWaterDetected()) return;
		boolean useNewerMethod = modAutofish.getConfig().openWaterNewAlgo();
		boolean isNoisy = modAutofish.getConfig().noisyDetection();
		notifyOpenWater(bobber, player, isInOpenWater(bobber, player, useNewerMethod), useNewerMethod, isNoisy);
	}
	private void persistenceModeTick() {
		persistenceTick ++;
		if (persistenceTick < 0) {
			persistenceTick = 0;
		}
		Minecraft client = EnvUtils.client();
		if (client.isPaused()) return;
		LocalPlayer player = client.player;
		if (earlyReturn(player)) return;
		if (modAutofish.getConfig().legacyPersistence()) {
			if (persistenceTick - persistenceTickLegacyLast < 50) return;
			persistenceTickLegacyLast = persistenceTick;
			if (!isRodHeld(player)) return;
			if (!modAutofish.getConfig().persistentMode()) return;
			if (Common.shouldNotReel(player, false)) return;
			if (hookExists) {
				if (isBobberInWater(player)) return;
				else useRodItem(EnvUtils.client().player);
			}
			if (modAutofish.getScheduler().isRecastQueued()) return;
			useRodItem(player);
		} else {
			// TODO: New persistence mode implementation go here.
		}
	}
}
