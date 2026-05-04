package troy.autofish.util;

import java.util.HashMap;
import java.util.Map;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import troy.autofish.Autofish;

public class Common {
	public static final FabricLoader fabricInstance = FabricLoader.getInstance();
	private static final Map<String, Boolean> modExistCache = new HashMap<>();

	// Just a simple cached detector of mods.
	public static Boolean hasMod(String modId) {
		if (modExistCache.containsKey(modId)) {
			return modExistCache.get(modId);
		} else {
			boolean modExistence = fabricInstance.isModLoaded(modId);
			modExistCache.put(modId, modExistence);
			Autofish.logSession.debug("Mod \"" + modId + "\" " + (modExistence ? "exists" : "does not exist") + ".");
			return modExistence;
		}
	}
	public static Entity getPlayerBobber(ClientPlayerEntity player) {
		if (player == null) return null;
		// Vanilla Minecraft.
		Entity bobber = player.fishHook;
		if (bobber != null) return bobber;
		// The modded section.
		if (hasMod("spectrum")) {
			bobber = Spectrum.getModdedBobber(player);
			if (bobber != null) return bobber;
		}
		return null;
	}
}
