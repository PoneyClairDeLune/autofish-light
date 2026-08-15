package troy.autofish.monitor;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundSoundEntityPacket;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.sounds.SoundEvent;
import troy.autofish.Autofish;
import troy.autofish.modded.Common;

public class FishMonitorMPSound implements FishMonitorMP {
	public static final double HOOKSOUND_DISTANCESQ_THRESHOLD = 25D;

	@Override
	public void handleHookRemoved() {}

	@Override
	public void hookTick(Autofish autofish, Minecraft minecraft, Projectile hook) {}

	@Override
	public void handlePacket(Autofish autofish, Packet<?> packet, Minecraft minecraft) {
		if (minecraft.player == null) return;
		Projectile bobber = Common.getPlayerBobber(minecraft.player);
		if (bobber == null) return;
		if (!(
			packet instanceof ClientboundSoundPacket || packet instanceof ClientboundSoundEntityPacket
		)) return;
		//TODO investigate PlaySoundFromEntityS2CPacket; i dont think its ever used for fishing but whatever
		if (!(packet instanceof ClientboundSoundPacket)) return;
		double x, y, z;
		ClientboundSoundPacket soundPacket = (ClientboundSoundPacket) packet;
		SoundEvent soundEvent = soundPacket.getSound().value();
		x = soundPacket.getX();
		y = soundPacket.getY();
		z = soundPacket.getZ();
		if (Common.isSplashSound(soundEvent)) {
			if (bobber.distanceToSqr(x, y, z) < HOOKSOUND_DISTANCESQ_THRESHOLD) {
				autofish.reelRod();
			}
		}
	}
}
