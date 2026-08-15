package troy.autofish.modded;

import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import cc.ltgc.luneApi.*;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import troy.autofish.LogSession;

public class Spectrum {
	public static final String modId = "spectrum";
	public static final String modNamespace = "spectrum";
	private static final Set<String> modBobbers = new HashSet<>();
	private static final Map<String, Set<TagKey<Fluid>>> modFluidTags = new ConcurrentHashMap<>();
	private static final Set<String> modRods = new HashSet<>();
	private static final Set<TagKey<Item>> modRodTags = new HashSet<>();
	private static Class<?> accessorBobber = null;

	private static void populateIds() {
		// Explicitly hardcoded bobbers
		if (modBobbers.size() <= 0) {
			modBobbers.add("spectrum:lagoon_fishing_bobber");
			modBobbers.add("spectrum:molten_fishing_bobber");
			modBobbers.add("spectrum:bedrock_fishing_bobber");
		}
		// Explicitly hardcoded rods
		if (modRods.size() <= 0) {
			modRods.add("spectrum:lagoon_rod");
			modRods.add("spectrum:molten_rod");
			modRods.add("spectrum:bedrock_fishing_rod");
		}
	}
	private static void populateFluidTags() {
		if (!Common.hasMod(modId)) return;
		if (modFluidTags.size() > 0) return;
		try {
			Set<TagKey<Fluid>> lagoonFluidTags = new HashSet<>();
			lagoonFluidTags.add(ReflectorUtils.getField("de.dafuqs.spectrum.registries.SpectrumFluidTags", "LAGOON_ROD_FISHABLE_IN", true, null));
			Set<TagKey<Fluid>> moltenFluidTags = new HashSet<>();
			moltenFluidTags.add(ReflectorUtils.getField("de.dafuqs.spectrum.registries.SpectrumFluidTags", "MOLTEN_ROD_FISHABLE_IN", true, null));
			Set<TagKey<Fluid>> bedrockFluidTags = new HashSet<>();
			bedrockFluidTags.add(ReflectorUtils.getField("de.dafuqs.spectrum.registries.SpectrumFluidTags", "BEDROCK_ROD_FISHABLE_IN", true, null));
			// Postpone commit to make any error visible.
			modFluidTags.put("spectrum:lagoon_rod", lagoonFluidTags);
			modFluidTags.put("spectrum:molten_rod", moltenFluidTags);
			modFluidTags.put("spectrum:bedrock_fishing_rod", bedrockFluidTags);
		} catch (Exception err) {
			LogSession.error("Spectrum fluid tag retrieval error: " + err.getMessage());
		}
	}
	private static void populateItemTags() {
		if (!Common.hasMod(modId)) return;
		if (modRodTags.size() > 0) return;
		try {
			modRodTags.add(ReflectorUtils.getField("de.dafuqs.spectrum.registries.SpectrumItemTags", "FISHING_RODS", true, null));
		} catch (Exception err) {
			LogSession.error("Spectrum block tag retrieval error: " + err.getMessage());
		}
	}
	static {
		populateIds();
		populateFluidTags();
		populateItemTags();
	}

	public static Projectile getModdedBobber(LocalPlayer player) {
		if (!Common.hasMod(modId)) return null;
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
	@Deprecated
	public static boolean isFishableLiquid(String blockId) {
		// No idea how to retrieve tags yet, so hardcoding for now.
		switch (blockId) {
			case "minecraft:lava":
			case "spectrum:dragonrot":
			case "spectrum:liquid_crystal":
			case "spectrum:midnight_solution":
			case "spectrum:sludge": {
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
	public static boolean isModdedBobber(Projectile entity) {
		populateIds();
		return modBobbers.contains(RegistryUtils.getIdKey(entity));
	}
	public static boolean isModdedRod(ItemStack itemStack) {
		if (!Common.hasMod(modId)) return false;
		populateItemTags();
		for (TagKey<Item> itemTag: modRodTags) {
			if (RegistryUtils.isIn(itemTag, itemStack)) return true;
		}
		return false;
	}
	public static boolean isModdedRod(String itemId) {
		populateIds();
		return modRods.contains(itemId);
	}
}
