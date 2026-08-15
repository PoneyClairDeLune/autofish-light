package troy.autofish.modded;

import java.util.concurrent.ConcurrentHashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import cc.ltgc.luneApi.*;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.sounds.SoundEvent;
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
	/** List of sound IDs fish bite sounds are under. */
	public final Set<String> biteSoundIds = new HashSet<>();
	/** List of entity IDs bobbers are under. */
	public final Set<String> bobberIds = new HashSet<>();
	/** List of fluid tags rods are matched against. */
	public final Map<String, Set<TagKey<Fluid>>> fluidTags = new ConcurrentHashMap<>();
	/** List of item IDs rods are under. */
	public final Set<String> rodIds = new HashSet<>();
	/** List of item tags rods are under. */
	public final Set<TagKey<Item>> rodTags = new HashSet<>();

	/** Method used to populate pure IDs. Should return <code>true</code> if the fields are populated. Don't use it if not implemented. */
	@Deprecated
	protected boolean populateIds() {
		return false;
	};
	/** Method used to populate fluid tags, which may call reflectors. Should return <code>true</code> if the fields are populated. Don't use it if not implemented. */
	@Deprecated
	protected boolean populateFluidTags() {
		return false;
	};
	/** Method used to populate item tags, which may call reflectors. Should return <code>true</code> if the fields are populated. Don't use it if not implemented. */
	@Deprecated
	protected boolean populateItemTags() {
		return false;
	};

	public NamespacedContent(String id, String namespace, String readableName) {
		this.id = id;
		this.namespace = namespace;
		this.name = readableName;
		//populateIds();
		//populateFluidTags();
		//populateItemTags();
	}

	/** Method used to test if the mod has been loaded at all. */
	public boolean hasMod() {
		boolean modExistence = EnvUtils.loader.isModLoaded(id);
		if (modExistence != modExistOld.get(id)) {
			LogSession.info("Mod \"" + id + "\" " + (modExistence ? "exists" : "does not exist") + ".");
		}
		modExistOld.put(id, modExistence);
		return modExistence;
	}

	/** Method used to retrieve bobbers that require custom logic differing from vanilla ones, which likely calls reflectors. Don't use it if not implemented. */
	//@Deprecated
	public Projectile getBobber(LocalPlayer player) {
		return null;
	};
	/** Method used to test if a given projectile entity is considered a bobber. */
	public boolean isBobber(Projectile entity) {
		if (populateIds()) {
			return bobberIds.contains(RegistryUtils.getIdKey(entity));
		}
		return false;
	}
	/** Method used to test if a given sound event is considered to be from fish biting the hook. */
	public boolean isFishBiteSound(SoundEvent soundEvent) {
		if (populateIds()) {
			String soundName = soundEvent.location().toString();
			return biteSoundIds.contains(soundName.toLowerCase());
		}
		return false;
	}
	/** Method used to test if a given fluid is considered valid for a given rod item ID. */
	public boolean isLiquidFishableTo(String itemId, FluidState fluidState) {
		if (!hasMod()) return false;
		if (populateFluidTags()) {
			Set<TagKey<Fluid>> validFluidTags = fluidTags.get(itemId);
			if (validFluidTags == null) return false;
			for (TagKey<Fluid> fluidTag: validFluidTags) {
				if (RegistryUtils.isIn(fluidTag, fluidState)) return true;
			}
		}
		return false;
	}
	/** Method used to test if a given item is considered a fishing rod. */
	public boolean isRod(ItemStack itemStack) {
		if (!hasMod()) return false;
		if (populateItemTags()) {
			for (TagKey<Item> itemTag: rodTags) {
				if (RegistryUtils.isIn(itemTag, itemStack)) return true;
			}
		}
		return false;
	}
	/** Method used to test if a given item ID is considered a fishing rod. */
	public boolean isRod(String itemId) {
		if (populateIds()) {
			return rodIds.contains(itemId);
		}
		return false;
	}
}
