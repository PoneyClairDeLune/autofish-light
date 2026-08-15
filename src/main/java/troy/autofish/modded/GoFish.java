package troy.autofish.modded;

//import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;

//import net.minecraft.client.player.LocalPlayer;
//import net.minecraft.world.entity.projectile.Projectile;
//import troy.autofish.LogSession;

public class GoFish {
	private static final HashSet<String> modRods = new HashSet<>(Arrays.asList("gofish:blaze_rod", "gofish:celestial_rod", "gofish:diamond_reinforced_rod", "gofish:ender_rod", "gofish:frosted_rod", "gofish:matrix_rod", "gofish:skeletal_rod", "gofish:slime_rod", "gofish:soul_rod"));
	public static boolean isFishableLiquid(String blockId) {
		// No idea how to retrieve tags yet, so hardcoding for now.
		switch (blockId) {
			case "minecraft:lava": {
				return true;
			}
		}
		return false;
	}
	public static boolean isModdedRod(String itemId) {
		if (!Common.hasMod("gofish")) return false;
		return modRods.contains(itemId);
	}
}
