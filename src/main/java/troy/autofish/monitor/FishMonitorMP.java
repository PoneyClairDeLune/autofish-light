package troy.autofish.monitor;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.network.packet.Packet;
import troy.autofish.Autofish;

public interface FishMonitorMP {
	void handleHookRemoved();
	void hookTick(Autofish autofish, MinecraftClient minecraft, ProjectileEntity hook);
	void handlePacket(Autofish autofish, Packet<?> packet, MinecraftClient minecraft);
}
