package troy.autofish.monitor;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.network.packet.Packet;
import troy.autofish.Autofish;

public interface FishMonitorMP {

    void hookTick(Autofish autofish, MinecraftClient minecraft, Entity hook);

    void handleHookRemoved();

    void handlePacket(Autofish autofish, Packet<?> packet, MinecraftClient minecraft);

}
