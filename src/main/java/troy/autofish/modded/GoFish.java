package troy.autofish.modded;

import java.util.concurrent.ConcurrentHashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import cc.ltgc.luneApi.*;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;

/** <i>Go Fish</i> by Draylar. */
public class GoFish {
	public static final String modId = "gofish";
	public static final String modNamespace = "gofish";
	public static final Map<String, Set<TagKey<Fluid>>> modFluidTags = new ConcurrentHashMap<>();
	public static final Set<String> modRods = new HashSet<>();

	public static void populateIds() {
		// Explicitly hardcoded rods
		if (modRods.size() <= 0) {
			modRods.add("gofish:blaze_rod");
			modRods.add("gofish:celestial_rod");
			modRods.add("gofish:diamond_reinforced_rod");
			modRods.add("gofish:ender_rod");
			modRods.add("gofish:frosted_rod");
			modRods.add("gofish:matrix_rod");
			modRods.add("gofish:skeletal_rod");
			modRods.add("gofish:slime_rod");
			modRods.add("gofish:soul_rod");
		}
	}
	public static void populateFluidTags() {
		if (!Common.hasMod(modId)) return;
		if (modFluidTags.size() > 0) return;
	}
	static {
		populateIds();
		populateFluidTags();
	}

	@Deprecated
	public static boolean isFishableLiquid(String blockId) {
		// No idea how to retrieve tags yet, so hardcoding for now.
		switch (blockId) {
			case "minecraft:lava": {
				return true;
			}
		}
		return false;
	}
	public static boolean isLiquidFishableTo(String itemId, FluidState fluidState) {
		if (!Common.hasMod(modId)) return false;
		populateFluidTags();
		Set<TagKey<Fluid>> validFluidTags = modFluidTags.get(itemId);
		if (validFluidTags == null) return false;
		for (TagKey<Fluid> fluidTag: validFluidTags) {
			if (RegistryUtils.isIn(fluidTag, fluidState)) return true;
		}
		return false;
	}
	public static boolean isRod(String itemId) {
		populateIds();
		return modRods.contains(itemId);
	}
}
