package troy.autofish.monitor;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.network.protocol.Packet;
import troy.autofish.Autofish;

public interface FishMonitorMP {
	void handleHookRemoved();
	void hookTick(Autofish autofish, Minecraft minecraft, Projectile hook);
	void handlePacket(Autofish autofish, Packet<?> packet, Minecraft minecraft);
}
