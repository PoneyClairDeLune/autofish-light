package troy.autofish.modded;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import cc.ltgc.luneApi.*;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.Item;
import troy.autofish.LogSession;

public class Spectrum {
	private static final Set<String> modBobbers = new HashSet<>(Arrays.asList("spectrum:lagoon_fishing_bobber", "spectrum:bedrock_fishing_bobber", "spectrum:molten_fishing_bobber"));
	private static final Set<String> modRods = new HashSet<>(Arrays.asList("spectrum:lagoon_rod", "spectrum:bedrock_fishing_rod", "spectrum:molten_rod"));
	private static final Set<TagKey<Item>> modRodTags = new HashSet<>();
	private static Class<?> accessorBobber = null;
	private static Class<?> accessorTagFluid = null;
	private static Class<?> accessorTagItem = null;

	private static void populateFluidTags() {
		if (!Common.hasMod("spectrum")) return;
		try {
			if (accessorTagFluid == null) {
				accessorTagFluid = Class.forName("de.dafuqs.spectrum.registries.SpectrumFluidTags");
			}
		} catch (Exception err) {
			LogSession.error("Spectrum tag retrieval error: " + err.getMessage());
		}
	}
	private static void populateItemTags() {
		if (!Common.hasMod("spectrum")) return;
		try {
			if (accessorTagItem == null) {
				accessorTagItem = Class.forName("de.dafuqs.spectrum.registries.SpectrumItemTags");
			}
			Field fishingRodTag = accessorTagItem.getField("FISHING_RODS");
			fishingRodTag.get(null);
		} catch (Exception err) {
			LogSession.error("Spectrum tag retrieval error: " + err.getMessage());
		}
	}
	static {
		populateFluidTags();
		populateItemTags();
	}

	public static Projectile getModdedBobber(LocalPlayer player) {
		if (!Common.hasMod("spectrum")) return null;
		// This should immediately cause a crash if it throws.
		try {
			//Object playerAccessor = player;
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
	public static boolean isLiquidFishableIn(String itemId, String blockId) {
		// WIP
		return false;
	}
	public static boolean isModdedBobber(Projectile entity) {
		if (!Common.hasMod("spectrum")) return false;
		return modBobbers.contains(RegistryUtils.getIdKey(entity));
	}
	public static boolean isModdedRod(String itemId) {
		if (!Common.hasMod("spectrum")) return false;
		return modRods.contains(itemId);
	}
}
