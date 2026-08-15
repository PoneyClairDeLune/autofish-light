package troy.autofish.modded;

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

/** Generalized namespaced content. */
public abstract class NamespacedContent {
	/** Stores the last states of probing mod existence. */
	private static final Map<String, Boolean> modExistOld = new ConcurrentHashMap<>();
	/** ID of the mod (e.g. "spectrum"). */
	public final String id;
	/** Readable name of the mod (e.g. "Spectrum"). */
	public final String name;
	/** Namespace of the mod (e.g. "spectrum"). */
	public final String namespace;
	/** List of entity IDs modded bobbers are under. */
	public final Set<String> bobberIds = new HashSet<>();
	/** List of fluid tags modded rods are matched against. */
	public final Map<String, Set<TagKey<Fluid>>> fluidTags = new ConcurrentHashMap<>();
	/** List of item IDs modded rods are under. */
	public final Set<String> rodIds = new HashSet<>();
	/** List of item tags modded rods are under. */
	public final Set<TagKey<Item>> rodTags = new HashSet<>();

	/** Method used to populate pure IDs. Don't use it if not implemented. */
	protected void populateIds() {};
	/** Method used to populate fluid tags, which may call reflectors. Don't use it if not implemented. */
	protected void populateFluidTags() {};
	/** Method used to populate item tags, which may call reflectors. Don't use it if not implemented. */
	protected void populateItemTags() {};

	public NamespacedContent(String id, String namespace, String readableName) {
		this.id = id;
		this.namespace = namespace;
		this.name = readableName;
		populateIds();
		populateFluidTags();
		populateItemTags();
	}

	public boolean hasMod() {
		boolean modExistence = EnvUtils.loader.isModLoaded(id);
		if (modExistence != modExistOld.get(id)) {
			LogSession.info("Mod \"" + id + "\" " + (modExistence ? "exists" : "does not exist") + ".");
		}
		modExistOld.put(id, modExistence);
		return modExistence;
	}

	/** Method used to retrieve bobbers that require custom logic differing from vanilla ones, which likely calls reflectors. Don't use it if not implemented. */
	public Projectile getBobber(LocalPlayer player) {
		return null;
	};
	/** Method used to test if a given fluid is considered valid for a given rod item ID. */
	public boolean isLiquidFishableTo(String itemId, FluidState fluidState) {
		if (!Common.hasMod(id)) return false;
		populateFluidTags();
		Set<TagKey<Fluid>> validFluidTags = fluidTags.get(itemId);
		if (validFluidTags == null) return false;
		for (TagKey<Fluid> fluidTag: validFluidTags) {
			if (RegistryUtils.isIn(fluidTag, fluidState)) return true;
		}
		return false;
	}
	/** Method used to test if a given projectile entity is considered a bobber. */
	public boolean isBobber(Projectile entity) {
		populateIds();
		return bobberIds.contains(RegistryUtils.getIdKey(entity));
	}
	/** Method used to test if a given item is considered a fishing rod. */
	public boolean isRod(ItemStack itemStack) {
		if (!Common.hasMod(id)) return false;
		populateItemTags();
		for (TagKey<Item> itemTag: rodTags) {
			if (RegistryUtils.isIn(itemTag, itemStack)) return true;
		}
		return false;
	}
	/** Method used to test if a given item ID is considered a fishing rod. */
	public boolean isRod(String itemId) {
		populateIds();
		return rodIds.contains(itemId);
	}
}
