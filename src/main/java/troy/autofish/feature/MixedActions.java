package troy.autofish.feature;

import cc.ltgc.luneApi.PlayerUtils;
import cc.ltgc.luneApi.RegistryUtils;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.block.state.BlockState;
import troy.autofish.LogSession;
import troy.autofish.modded.Common;

public class MixedActions {
	/** Is the bobber owner not notified prior. */
	private boolean bobberOwnerNotNotified = true;
	/** The last block checked against. */
	private BlockState lastBlock = null;
	/** Was the bobber in open water in the last notification attempt. */
	private boolean lastNotifyAttemptOpenWater = false;
	/** Was the held item a fishing rod. */
	private boolean wasRodHeld = false;

	/** Cancel fishing rod usage by attempting to switch away from fishing rods, and returns <code>true</code> when successful. Useful to prevent avoidable durability drop with this method, while still allowing hard cancellation via explicit usage. */
	public boolean cancelRodUsage(LocalPlayer player) {
		// TODO: Implement natural slot shifting - Detect closest unmatched slot on either direction of scrolling, then decide which direction to scroll to accordingly. Should be useful to help evade overly stringent server-side anti-cheat.
		if (player == null) return true; // No need to prompt further actions.
		final byte rodHandMatchResult = PlayerUtils.matchItemOnHands(player, Detections::isPredicateFishingRod);
		if ((rodHandMatchResult & 2) > 0) return false; // You can't switch the active slots from the offhand anyway.
		if (rodHandMatchResult == 0) return true; // Already with no rods held, no need to do anything.
		// This is a search implementation I'm satisfied with.
		final Inventory inventoryPlayer = player.getInventory();
		int chosenSlot = PlayerUtils.getClosestMatchInHotbar(inventoryPlayer, Detections::isPredicateFishingRod, true);
		LogSession.info("Chosen slot: " + String.valueOf(chosenSlot));
		if (chosenSlot < 0) return false;
		return false;
	}
	public boolean isBobberInWaterThenNotify(LocalPlayer player, boolean useNewerMethod, boolean useUnsafeFluid) {
		if (Detections.earlyReturn(player)) {
			lastBlock = null;
			return false;
		};
		Projectile bobber = Common.getPlayerBobber(player);
		if (bobber == null) return false;
		BlockState containedBlock = player.level().getBlockState(bobber.blockPosition());
		boolean waterVerdict = Detections.isBobberInWater(
			player, bobber,
			containedBlock,
			useNewerMethod,
			useUnsafeFluid
		);
		if (containedBlock != lastBlock) {
			LogSession.info("Block " + RegistryUtils.getIdKey(containedBlock) + (waterVerdict ? " is" : " isn't") + " fishable liquid.");
		}
		lastBlock = containedBlock;
		return waterVerdict;
	}
	public void isInOpenWaterThenNotify(Projectile bobber, Player player, boolean detectOpenWater, boolean useNewerMethod, boolean useUnsafeFluid, boolean isNoisy) {
		if (!detectOpenWater) return;
		boolean checkResult = Detections.isInOpenWater(bobber, player, useNewerMethod, useUnsafeFluid);
		Player bobberOwner = Common.getPlayerOwner(bobber);
		if (bobberOwnerNotNotified || isNoisy || (checkResult ^ lastNotifyAttemptOpenWater)) {
			LogSession.info("Bobber " + (checkResult ? "was" : "wasn't") + " in open water.");
			bobberOwnerNotNotified = !Interactions.notifyOpenWater(player, bobberOwner, checkResult, useNewerMethod);
		} else {
			bobberOwnerNotNotified = true;
		}
		lastNotifyAttemptOpenWater = checkResult;
	}
	public boolean isRodHeldThenNotify(Player player, boolean noisyDetection) {
		if (Detections.earlyReturn(player)) {
			wasRodHeld = false;
			return false;
		}
		boolean isRodHeldNow = Detections.isRodHeld(player);
		if (wasRodHeld != isRodHeldNow) {
			String message = (isRodHeldNow ? "H" : "Not h") + "olding fishing rod: " + RegistryUtils.getIdKey(
				PlayerUtils.getHeldStack(
					player,
					PlayerUtils.matchOffhandItem(
						player,
						Detections::isPredicateFishingRod
					)
				)
			) + ".";
			if (noisyDetection) {
				LogSession.info(message);
			} else {
				LogSession.debug(message);
			}
		}
		wasRodHeld = isRodHeldNow;
		return isRodHeldNow;
	}
}
