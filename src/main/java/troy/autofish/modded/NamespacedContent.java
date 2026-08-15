package troy.autofish.modded;

import java.util.concurrent.ConcurrentHashMap;
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

public abstract class NamespacedContent {
	/** Stores the last states of probing mod existence. */
	private static final Map<String, Boolean> modExistOld = new ConcurrentHashMap<>();
	/** ID of the mod (e.g. "spectrum"). */
	public final String modId;
	/** Namespace of the mod (e.g. "spectrum"). */
	public final String modNamespace;
	/** List of entity IDs modded bobbers are under. */
	public Set<String> modBobbers;
	/** List of fluid tags modded rods are matched against. */
	public Map<String, Set<TagKey<Fluid>>> modFluidTags;
	/** List of item IDs modded rods are under. */
	public Set<String> modRods;
	/** List of item tags modded rods are under. */
	public Set<TagKey<Item>> modRodTags;

	/** Method used to populate pure IDs. Don't use it if not implemented. */
	@Deprecated
	protected void populateIds() {}
	/** Method used to populate fluid tags, which may call reflectors. Don't use it if not implemented. */
	@Deprecated
	protected void populateFluidTags() {}
	/** Method used to populate item tags, which may call reflectors. Don't use it if not implemented. */
	@Deprecated
	protected void populateItemTags() {}

	public NamespacedContent(String modId, String modNamespace) {
		this.modId = modId;
		this.modNamespace = modNamespace;
		populateIds();
		populateFluidTags();
		populateItemTags();
	}

	public boolean hasMod() {
		boolean modExistence = EnvUtils.loader.isModLoaded(modId);
		if (modExistence != modExistOld.get(modId)) {
			LogSession.info("Mod \"" + modId + "\" " + (modExistence ? "exists" : "does not exist") + ".");
		}
		modExistOld.put(modId, modExistence);
		return modExistence;
	}

	/** Method used to retrieve bobbers that require custom logic differing from vanilla ones, which likely calls reflectors. Don't use it if not implemented. */
	@Deprecated
	public Projectile getModdedBobber(LocalPlayer player) {
		return null;
	}
	/** Method used to test if a given fluid is considered valid for a given rod item ID. */
	public boolean isLiquidFishableTo(String itemId, FluidState fluidState) {
		if (!Common.hasMod(modId)) return false;
		populateFluidTags();
		Set<TagKey<Fluid>> validFluidTags = modFluidTags.get(itemId);
		if (validFluidTags == null) return false;
		for (TagKey<Fluid> fluidTag: validFluidTags) {
			if (RegistryUtils.isIn(fluidTag, fluidState)) return true;
		}
		return false;
	}
	/** Method used to test if a given projectile entity is considered a bobber. */
	public boolean isBobber(Projectile entity) {
		populateIds();
		return modBobbers.contains(RegistryUtils.getIdKey(entity));
	}
	/** Method used to test if a given item is considered a fishing rod. */
	public boolean isRod(ItemStack itemStack) {
		if (!Common.hasMod(modId)) return false;
		populateItemTags();
		for (TagKey<Item> itemTag: modRodTags) {
			if (RegistryUtils.isIn(itemTag, itemStack)) return true;
		}
		return false;
	}
	/** Method used to test if a given item ID is considered a fishing rod. */
	public boolean isRod(String itemId) {
		populateIds();
		return modRods.contains(itemId);
	}
}
