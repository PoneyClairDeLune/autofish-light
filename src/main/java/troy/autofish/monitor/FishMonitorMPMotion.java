package troy.autofish.monitor;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import troy.autofish.Autofish;
import troy.autofish.modded.Common;


public class FishMonitorMPMotion implements FishMonitorMP {

    // The threshold of detecting a bobber moving downwards, to detect as a fish.
    public static final double PACKET_MOTION_Y_THRESHOLD = -0.1;

    // Start catching fish after a 1 second threshold of hitting water.
    public static final int START_CATCHING_AFTER_THRESHOLD = 1000;

    // True if the bobber is in the water.
    private boolean hasHitWater = false;

    // Time at which bobber begins to rise in the water.
    // 0 if the bobber has not rose in the water yet.
    private long bobberRiseTimestamp = 0;

    @Override
    public void handleHookRemoved() {
        hasHitWater = false;
        bobberRiseTimestamp = 0;
    }

	@Override
	public void hookTick(Autofish autofish, Minecraft minecraft, Projectile hook) {
		if (LevelContainsBlockWithFishableLiquid(hook.level(), hook.getBoundingBox())) {
			hasHitWater = true;
		}
	}

	@Override
	public void handlePacket(Autofish autofish, Packet<?> packet, Minecraft minecraft) {
		if (packet instanceof ClientboundSetEntityMotionPacket velocityPacket) {
			if (minecraft.player == null) return;
			Projectile bobber = Common.getPlayerBobber(minecraft.player);
			if (bobber == null) return;
			if (bobber.getId() != velocityPacket.id()) return;
			// Wait until the bobber has rose in the water.
			// Prevent remarking the bobber rise timestamp until it is reset by catching.
			Vec3 bobberMovement = velocityPacket.movement();
			if (
				hasHitWater && bobberRiseTimestamp == 0 &&
				bobberMovement.y() > 0
			) {
				// Mark the time in which the bobber began to rise.
				bobberRiseTimestamp = autofish.timeMillis;
			}
			// Calculate the time in which the bobber has been in the water
			long timeInWater = autofish.timeMillis - bobberRiseTimestamp;
			// If the bobber has been in the water long enough, start detecting the bobber movement.
			if (
				hasHitWater && bobberRiseTimestamp != 0 &&
				timeInWater > START_CATCHING_AFTER_THRESHOLD
			) {
				// minecraft.player.sendMessage(Text.of("Y: "+ bobberMovement.Y()),true);
				if (bobberMovement.x() == 0.0 && bobberMovement.z() == 0.0 && bobberMovement.y() < PACKET_MOTION_Y_THRESHOLD) {
					// Catch the fish
					autofish.catchFish();
					// Reset the class attributes to default.
					this.handleHookRemoved();
				}
			}
		}
	}

    public static boolean LevelContainsBlockWithFishableLiquid(Level Level, AABB box) {
        int i = Mth.floor(box.minX);
        int j = Mth.ceil(box.maxX);
        int k = Mth.floor(box.minY);
        int l = Mth.ceil(box.maxY);
        int m = Mth.floor(box.minZ);
        int n = Mth.ceil(box.maxZ);
        return BlockPos.betweenClosedStream(i, k, m, j - 1, l - 1, n - 1).anyMatch((blockPos) -> Common.isBlockNegligible(Level.getBlockState(blockPos)));
    }
}
