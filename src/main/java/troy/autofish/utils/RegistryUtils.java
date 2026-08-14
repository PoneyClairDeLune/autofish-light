// 2026 © Lumière Élevé
// The code below is licensed under GNU LGPL 3.0+ as part of Lune API.

package troy.autofish.utils;

import java.util.HashMap;
import java.util.Map;

//import net.minecraft.core.component.DataComponentMap;
//import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
//import net.minecraft.resources.ResourceKey;
//import net.minecraft.core.DefaultedRegistry;
//import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
//import net.minecraft.tags.BlockTags;
//import net.minecraft.tags.EntityTypeTags;
//import net.minecraft.tags.FluidTags;
//import net.minecraft.tags.ItemTags;
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
	public static final String NAMESPACE_DELIMITER = ":";

	private static final Map<String, Identifier> fullIdCache = new HashMap<>();
	/*private static final Map<ResourceKey<? extends Registry<?>>, Map<String, TagKey<?>>> splitTagCache = new HashMap<>();

	@SuppressWarnings("unchecked")
	private static <T> TagKey<T> unsafelyRestoreType(TagKey<?> source) {
		return (TagKey<T>) source; // Unchecked cast!
	}*/

	/** Retrieve cached tags. Namespace "minecraft" and path "logs_that_burn" will be assembled into "minecraft:logs_that_burn" first to query the cache, before the actual identifier creation ever happens. */
	public static Identifier getIdentifier(String namespace, String path) {
		if (namespace == null || namespace.length() <= 0) {
			namespace = "minecraft";
		}
		String fullPath = namespace + NAMESPACE_DELIMITER + path;
		if (fullIdCache.containsKey(fullPath)) return fullIdCache.get(fullPath);
		Identifier targetId = Identifier.fromNamespaceAndPath(namespace, path);
		fullIdCache.put(fullPath, targetId);
		return targetId;
	}
	/** Retrieve cached tags. Recommended due to faster cache hits. */
	public static Identifier getIdentifier(String fullPath) {
		if (!fullPath.contains(":")) {
			fullPath = "minecraft:" + fullPath;
		}
		if (fullIdCache.containsKey(fullPath)) return fullIdCache.get(fullPath);
		Identifier targetId = Identifier.parse(fullPath);
		fullIdCache.put(fullPath, targetId);
		return targetId;
	}
	/** Retrieve tag containers. */
	/*public static <T> Object getTagContainer(Registry<T> registry, TagKey<T> tagKey, Identifier id) {
		Object tagContainer = registry.get(id);
		return tagContainer;
	}*/
	/** Retrieve cached tag keys. */
	/*public static <T> TagKey<T> getTagKey(ResourceKey<Registry<T>> registry, String fullPath) {
		// Example registry: net.minecraft.core.registries.Registries.BLOCK
		Map<String, TagKey<?>> registryCache = splitTagCache.get(registry);
		if (registryCache == null) {
			registryCache = new HashMap<>();
			splitTagCache.put(registry, registryCache);
		}
		TagKey<T> targetTag = unsafelyRestoreType(registryCache.get(fullPath));
		if (targetTag != null) return targetTag; // Unchecked cast on this line!
		targetTag = TagKey.create(registry, getId(fullPath));
		registryCache.put(fullPath, targetTag);
		return targetTag;
	}*/
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
