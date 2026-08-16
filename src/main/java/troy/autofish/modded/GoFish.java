package troy.autofish.modded;

import java.util.HashSet;
import java.util.Set;

import net.minecraft.tags.FluidTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.material.Fluid;

/** <i>Go Fish</i> by <i>Draylar</i> and <i>Elis Kvitka</i>. */
public class GoFish extends NamespacedContent {
	/** <i>Go Fish</i> by <i>Draylar</i> and <i>Elis Kvitka</i>. */
	public GoFish() {
		super("gofish", "gofish", "Go Fish");
	}

	protected boolean populateIds() {
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
	protected boolean populateFluidTags() {
		//if (!hasMod()) return false;
		if (fluidTags.size() > 0) return true;
		if (!populateIds()) return false;
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
				default: {
					Set<TagKey<Fluid>> rodUnsafeFluidTags = new HashSet<>();
					rodUnsafeFluidTags.add(FluidTags.LAVA);
					fluidTagsUnsafe.put(rodId, rodUnsafeFluidTags);
				}
			}
			fluidTags.put(rodId, rodFluidTags);
		}
		return true;
	}
}
