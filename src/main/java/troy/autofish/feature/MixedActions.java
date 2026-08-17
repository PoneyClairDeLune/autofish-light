package troy.autofish.feature;

import cc.ltgc.luneApi.PlayerUtils;
import cc.ltgc.luneApi.RegistryUtils;
import net.minecraft.client.player.LocalPlayer;
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
						Detections::isOffhandPredicate
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
