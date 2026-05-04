package troy.autofish.util;

import java.util.HashMap;
import java.util.ArrayList;
import java.util.Map;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import troy.autofish.Autofish;

public class Common {
	public static final FabricLoader fabricInstance = FabricLoader.getInstance();
	private static final Map<String, Boolean> modExistCache = new HashMap<>();
	private static final ArrayList<Entity> testCases = new ArrayList<>();

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
		if (testCases == null || testCases.size() <= 0) {
			testCases.addLast(player.fishHook);
			if (hasMod("spectrum")) testCases.addLast(Spectrum.getModdedBobber(player));
		}
		for (Entity testCase : testCases) {
			if (testCase != null) {
				return testCase;
			}
		}
		return null;
	}
}
