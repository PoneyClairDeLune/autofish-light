// 2026 © Lumière Élevé
// The code below is licensed under GNU LGPL 3.0+ as part of Lune API.

package cc.ltgc.luneApi;

import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.IdentifierException;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
//import troy.autofish.LogSession;

/** Utility methods for registry access. */
public class RegistryUtils {
	public static final String DEFAULT_NAMESPACE = "minecraft";
	public static final String NAMESPACE_DELIMITER = ":";

	private static final Map<String, Identifier> fullIdCache = new ConcurrentHashMap<>();

	/** Retrieve cached tags. Namespace "minecraft" and path "logs_that_burn" will be assembled into "minecraft:logs_that_burn" first to query the cache, before the actual identifier creation ever happens.
	* <br/>Avoid creating ephemeral identifier objects. */
	public static Identifier getIdentifier(String namespace, String path) {
		if (namespace == null || namespace.length() <= 0) {
			namespace = DEFAULT_NAMESPACE;
		}
		String fullPath = namespace + NAMESPACE_DELIMITER + path;
		Identifier cacheResult = fullIdCache.get(fullPath);
		if (cacheResult != null) return cacheResult;
		Identifier targetId = Identifier.fromNamespaceAndPath(namespace, path);
		fullIdCache.put(fullPath, targetId);
		return targetId;
	}
	/** Retrieve cached tags. Recommended due to faster cache hits.
	* <br/>Avoid creating ephemeral identifier objects. */
	public static Identifier getIdentifier(String fullPath) {
		if (!fullPath.contains(NAMESPACE_DELIMITER)) {
			fullPath = DEFAULT_NAMESPACE + NAMESPACE_DELIMITER + fullPath;
		}
		if (fullIdCache.containsKey(fullPath)) return fullIdCache.get(fullPath);
		Identifier targetId = Identifier.parse(fullPath);
		fullIdCache.put(fullPath, targetId);
		return targetId;
	}
	/** Retrieve registry ID keys like "minecraft:stone". */
	public static String getIdKey(Block block) {
		if (block == null) return null;
		return BuiltInRegistries.BLOCK.getKey(block).toString();
	}
	/** Retrieve registry ID keys like "minecraft:stone". */
	public static String getIdKey(BlockState blockState) {
		if (blockState == null) return null;
		return getIdKey(blockState.getBlock());
	}
	/** Retrieve registry ID keys like "minecraft:horse". */
	public static String getIdKey(Entity entity) {
		if (entity == null) return null;
		return getIdKey(entity.getType());
	}
	/** Retrieve registry ID keys like "minecraft:horse". */
	public static String getIdKey(EntityType<? extends Entity> entityType) {
		if (entityType == null) return null;
		return BuiltInRegistries.ENTITY_TYPE.getKey(entityType).toString();
	}
	/** Retrieve registry ID keys like "minecraft:water". */
	public static String getIdKey(Fluid fluid) {
		if (fluid == null) return null;
		return BuiltInRegistries.FLUID.getKey(fluid).toString();
	}
	/** Retrieve registry ID keys like "minecraft:water". */
	public static String getIdKey(FluidState fluidState) {
		if (fluidState == null) return null;
		return getIdKey(fluidState.getType());
	}
	/** Retrieve registry ID keys like "minecraft:stick". */
	public static String getIdKey(Item item) {
		if (item == null) return null;
		return BuiltInRegistries.ITEM.getKey(item).toString();
	}
	/** Retrieve registry ID keys like "minecraft:stick". */
	public static String getIdKey(ItemStack itemStack) {
		if (itemStack == null || itemStack == ItemStack.EMPTY) return null;
		return getIdKey(itemStack.getItem());
	}
	/** Retrieve registry ID keys like "minecraft:stone". */
	public static String getNamespace(Block block) {
		if (block == null) return null;
		return BuiltInRegistries.BLOCK.getKey(block).getNamespace();
	}
	/** Retrieve registry ID keys like "minecraft:stone". */
	public static String getNamespace(BlockState blockState) {
		if (blockState == null) return null;
		return getNamespace(blockState.getBlock());
	}
	/** Retrieve registry ID keys like "minecraft:horse". */
	public static String getNamespace(Entity entity) {
		if (entity == null) return null;
		return getNamespace(entity.getType());
	}
	/** Retrieve registry ID keys like "minecraft:horse". */
	public static String getNamespace(EntityType<? extends Entity> entityType) {
		if (entityType == null) return null;
		return BuiltInRegistries.ENTITY_TYPE.getKey(entityType).getNamespace();
	}
	/** Retrieve registry ID keys like "minecraft:water". */
	public static String getNamespace(Fluid fluid) {
		if (fluid == null) return null;
		return BuiltInRegistries.FLUID.getKey(fluid).getNamespace();
	}
	/** Retrieve registry ID keys like "minecraft:water". */
	public static String getNamespace(FluidState fluidState) {
		if (fluidState == null) return null;
		return getNamespace(fluidState.getType());
	}
	/** Retrieve registry ID keys like "minecraft:stick". */
	public static String getNamespace(Item item) {
		if (item == null) return null;
		return BuiltInRegistries.ITEM.getKey(item).getNamespace();
	}
	/** Retrieve registry ID keys like "minecraft:stick". */
	public static String getNamespace(ItemStack itemStack) {
		if (itemStack == null || itemStack == ItemStack.EMPTY) return null;
		return getNamespace(itemStack.getItem());
	}
	public static String getNamespace(SoundEvent soundEvent) {
		if (soundEvent == null) return null;
		return soundEvent.location().getNamespace();
	}
	/** Retrieve the namespace of the full identifier string when the related source object is not available, which should be avoided. Identifier objects should use the namespace attribute directly whenever available.
	* <br/>While modded items may not always have their namespaces match the mod ID (e.g. <i>Vanilla Backport</i>, which populates <code>minecraft:*</code>), this method is still useful for a crude validation test.
	* <br/>Path is not validated at all, as this method does not deal with paths. */
	public static String getNamespace(String idKey) {
		// The namespace validation process here has been verified to match Minecraft's own Identifier construction process on JE 26.1.2 without constructing new Identifier objects every time.
		if (idKey == null) throw new IdentifierException("ID cannot be null.");
		int colonPos = idKey.indexOf(NAMESPACE_DELIMITER);
		if (colonPos < 0) return DEFAULT_NAMESPACE;
		if (colonPos == 0) throw new IdentifierException("Namespace cannot be blank.");
		String namespace = idKey.substring(0, colonPos);
		if (!Identifier.isValidNamespace(namespace)) {
			throw new IdentifierException("Namespace contains invalid characters.");
		}
		return namespace;
	}
	/** Test if a given target is in a tag. */
	public static boolean isIn(TagKey<Block> blockTag, Block block) {
		if (block == null) return false;
		return isIn(blockTag, block.defaultBlockState());
	}
	/** Test if a given target is in a tag. */
	public static boolean isIn(TagKey<Block> blockTag, BlockState blockState) {
		return blockState.is(blockTag);
	}
	/** Test if a given target is in a tag. */
	/*public static boolean isIn(TagKey<EntityType<? extends Entity>> entityTypeTag, Entity entity) {
		return isIn(entityTypeTag, entity.getType());
	}*/
	/** Test if a given target is in a tag. */
	/*public static boolean isIn(TagKey<EntityType<? extends Entity>> entityTypeTag, EntityType<? extends Entity> entityType) {
		return entityType.is(entityTypeTag);
	}*/
	/** Test if a given target is in a tag. */
	public static boolean isIn(TagKey<Fluid> fluidTag, Fluid fluid) {
		if (fluid == null) return false;
		return isIn(fluidTag, fluid.defaultFluidState());
	}
	/** Test if a given target is in a tag. */
	public static boolean isIn(TagKey<Fluid> fluidTag, FluidState fluidState) {
		return fluidState.is(fluidTag);
	}
	/** Test if a given target is in a tag. */
	public static boolean isIn(TagKey<Item> itemTag, Item item) {
		if (item == null) return false;
		return isIn(itemTag, item.getDefaultInstance());
	}
	/** Test if a given target is in a tag. */
	public static boolean isIn(TagKey<Item> itemTag, ItemStack itemStack) {
		return itemStack.is(itemTag);
	}
}
