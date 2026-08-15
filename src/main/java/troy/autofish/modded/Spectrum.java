package troy.autofish.modded;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

import cc.ltgc.luneApi.*;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.material.Fluid;
import troy.autofish.LogSession;

/** <i>Spectrum</i> by DaFuqs. */
public class Spectrum extends NamespacedContent {
	/** <i>Spectrum</i> by DaFuqs. */
	public Spectrum() {
		super("spectrum", "spectrum", "Spectrum");
	}
	public Class<?> accessorBobber = null;

	protected boolean populateIds() {
		// Explicitly hardcoded bobbers
		if (bobberIds.isEmpty()) {
			bobberIds.add("spectrum:lagoon_fishing_bobber");
			bobberIds.add("spectrum:molten_fishing_bobber");
			bobberIds.add("spectrum:bedrock_fishing_bobber");
		}
		// Explicitly hardcoded rods
		if (rodIds.isEmpty()) {
			rodIds.add("spectrum:lagoon_rod");
			rodIds.add("spectrum:molten_rod");
			rodIds.add("spectrum:bedrock_fishing_rod");
		}
		return true;
	}
	protected boolean populateFluidTags() {
		if (!hasMod()) return false;
		if (fluidTags.size() > 0) return true;
		try {
			Set<TagKey<Fluid>> lagoonFluidTags = new HashSet<>();
			lagoonFluidTags.add(ReflectorUtils.getField("de.dafuqs.spectrum.registries.SpectrumFluidTags", "LAGOON_ROD_FISHABLE_IN", true, null));
			Set<TagKey<Fluid>> moltenFluidTags = new HashSet<>();
			moltenFluidTags.add(ReflectorUtils.getField("de.dafuqs.spectrum.registries.SpectrumFluidTags", "MOLTEN_ROD_FISHABLE_IN", true, null));
			Set<TagKey<Fluid>> bedrockFluidTags = new HashSet<>();
			bedrockFluidTags.add(ReflectorUtils.getField("de.dafuqs.spectrum.registries.SpectrumFluidTags", "BEDROCK_ROD_FISHABLE_IN", true, null));
			// Postpone commit to make any error visible.
			fluidTags.put("spectrum:lagoon_rod", lagoonFluidTags);
			fluidTags.put("spectrum:molten_rod", moltenFluidTags);
			fluidTags.put("spectrum:bedrock_fishing_rod", bedrockFluidTags);
			return true;
		} catch (Exception err) {
			LogSession.error("Spectrum fluid tag retrieval error: " + err.getMessage());
		}
		return false;
	}
	protected boolean populateItemTags() {
		if (!hasMod()) return false;
		if (rodTags.size() > 0) return true;
		try {
			rodTags.add(ReflectorUtils.getField("de.dafuqs.spectrum.registries.SpectrumItemTags", "FISHING_RODS", true, null));
			return true;
		} catch (Exception err) {
			LogSession.error("Spectrum block tag retrieval error: " + err.getMessage());
		}
		return false;
	}

	public Projectile getBobber(LocalPlayer player) {
		if (!hasMod()) return null;
		try {
			// TODO: Move to ReflectorUtils.getMethod once properly tested on 1.21.1.
			if (accessorBobber == null) {
				accessorBobber = Class.forName("de.dafuqs.spectrum.api.entity.PlayerEntityAccessor");
				LogSession.info("Created a modded accessor for \"spectrum\".");
			}
			Method bobberRetriever = accessorBobber.getMethod("getSpectrumBobber");
			if (bobberRetriever != null) {
				return (Projectile) bobberRetriever.invoke(player);
			}
		} catch (Exception err) {
			LogSession.error("Spectrum bobber retrieval error: " + err.getMessage());
		}
		return null;
	}
}
