package troy.autofish.modded;

import java.util.HashSet;
import java.util.Set;

import net.minecraft.tags.FluidTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.material.Fluid;

/** <i>Go Fish</i> by Draylar and Elis Kvitka. */
public class GoFish extends NamespacedContent {
	/** <i>Go Fish</i> by Draylar and Elis Kvitka. */
	public GoFish() {
		super("gofish", "gofish", "Go Fish");
	}

	public boolean populateIds() {
		// Explicitly hardcoded rods
		if (rodIds.isEmpty()) {
			rodIds.add("gofish:blaze_rod");
			rodIds.add("gofish:celestial_rod");
			rodIds.add("gofish:diamond_reinforced_rod");
			rodIds.add("gofish:ender_rod");
			rodIds.add("gofish:frosted_rod");
			rodIds.add("gofish:matrix_rod");
			rodIds.add("gofish:skeletal_rod");
			rodIds.add("gofish:slime_rod");
			rodIds.add("gofish:soul_rod");
		}
		return true;
	}
	public boolean populateFluidTags() {
		//if (!hasMod()) return false;
		if (fluidTags.size() > 0) return true;
		if (populateIds()) {
			for (String rodId: rodIds) {
				Set<TagKey<Fluid>> rodFluidTags = new HashSet<>();
				rodFluidTags.add(FluidTags.WATER);
				switch (rodId) {
					case "gofish:blaze_rod":
					case "gofish:diamond_reinforced_rod":
					case "gofish:skeletal_rod":
					case "gofish:soul_rod": {
						rodFluidTags.add(FluidTags.LAVA);
						break;
					}
				}
				fluidTags.put(rodId, rodFluidTags);
			}
		}
		return true;
	}
}
