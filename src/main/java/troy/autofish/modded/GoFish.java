package troy.autofish.modded;

//import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;

//import net.minecraft.client.player.LocalPlayer;
//import net.minecraft.world.entity.projectile.Projectile;
//import troy.autofish.LogSession;

public class GoFish {
	//private static Class<?> moddedAccessor = null;
	//private static final String moddedAccessorClassId = "de.dafuqs.spectrum.api.entity.PlayerEntityAccessor";
	//private static final HashSet<String> moddedBobbers = new HashSet<String>(Arrays.asList());
	private static final HashSet<String> moddedRods = new HashSet<>(Arrays.asList("gofish:blaze_rod", "gofish:celestial_rod", "gofish:diamond_reinforced_rod", "gofish:ender_rod", "gofish:frosted_rod", "gofish:matrix_rod", "gofish:skeletal_rod", "gofish:slime_rod", "gofish:soul_rod"));
	/*public static Projectile getModdedBobber(LocalPlayer player) {
		if (!Common.hasMod("spectrum")) return null;
		// This should immediately cause a crash if it throws.
		try {
			//Object playerAccessor = player;
			if (moddedAccessor == null) {
				moddedAccessor = Class.forName(moddedAccessorClassId);
				LogSession.info("Created a modded accessor for \"spectrum\".");
			}
			Method bobberRetriever = moddedAccessor.getMethod("getSpectrumBobber");
			if (bobberRetriever != null) {
				return (Projectile) bobberRetriever.invoke(player);
			}
		} catch (Exception err) {
			LogSession.error("Spectrum bobber retrieval error: " + err.getMessage());
		}
		return null;
	}*/
	public static boolean isFishableLiquid(String blockId) {
		// No idea how to retrieve tags yet, so hardcoding for now.
		switch (blockId) {
			//case "spectrum:dragonrot":
			//case "spectrum:liquid_crystal":
			//case "spectrum:midnight_solution":
			//case "spectrum:sludge":
			case "minecraft:lava": {
				return true;
			}
		}
		return false;
	}
	/*public static boolean isLiquidFishableIn(String itemId, String blockId) {
		// WIP
		return false;
	}
	public static boolean isModdedBobber(Projectile entity) {
		if (!Common.hasMod("spectrum")) return false;
		return moddedBobbers.contains(RegistryUtils.getIdKey(entity));
	}*/
	public static boolean isModdedRod(String itemId) {
		if (!Common.hasMod("spectrum")) return false;
		return moddedRods.contains(itemId);
	}
}
