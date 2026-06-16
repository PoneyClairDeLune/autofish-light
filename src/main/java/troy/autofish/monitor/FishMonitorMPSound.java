package troy.autofish.monitor;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.PlaySoundFromEntityS2CPacket;
import net.minecraft.network.packet.s2c.play.PlaySoundS2CPacket;
import net.minecraft.sound.SoundEvent;
import troy.autofish.Autofish;
import troy.autofish.modded.Common;

public class FishMonitorMPSound implements FishMonitorMP {
	public static final double HOOKSOUND_DISTANCESQ_THRESHOLD = 25D;

	@Override
	public void handleHookRemoved() {}

	@Override
	public void hookTick(Autofish autofish, MinecraftClient minecraft, ProjectileEntity hook) {}

	@Override
	public void handlePacket(Autofish autofish, Packet<?> packet, MinecraftClient minecraft) {
		if (minecraft.player == null) return;
		ProjectileEntity bobber = Common.getPlayerBobber(minecraft.player);
		if (bobber == null) return;
		if (!(
			packet instanceof PlaySoundS2CPacket || packet instanceof PlaySoundFromEntityS2CPacket
		)) return;
		//TODO investigate PlaySoundFromEntityS2CPacket; i dont think its ever used for fishing but whatever
		if (!(packet instanceof PlaySoundS2CPacket)) return;
		double x, y, z;
		PlaySoundS2CPacket soundPacket = (PlaySoundS2CPacket) packet;
		SoundEvent soundEvent = soundPacket.getSound().value();
		x = soundPacket.getX();
		y = soundPacket.getY();
		z = soundPacket.getZ();
		if (Common.isSplashSound(soundEvent)) {
			if (bobber.squaredDistanceTo(x, y, z) < HOOKSOUND_DISTANCESQ_THRESHOLD) {
				autofish.catchFish();
			}
		}
	}
}
