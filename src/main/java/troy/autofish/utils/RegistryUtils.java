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
//import net.minecraft.tags.FluidTags;
//import net.minecraft.tags.ItemTags;
//import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
//import net.minecraft.world.level.material.Fluid;
//import troy.autofish.LogSession;

public class RegistryUtils {
	public static final String NAMESPACE_DELIMITER = ":";

	private static final Map<String, Identifier> fullIdCache = new HashMap<>();
	/*private static final Map<ResourceKey<? extends Registry<?>>, Map<String, TagKey<?>>> splitTagCache = new HashMap<>();

	@SuppressWarnings("unchecked")
	private static <T> TagKey<T> unsafelyRestoreType(TagKey<?> source) {
		return (TagKey<T>) source; // Unchecked cast!
	}*/

	/** Utility method for returning cached tags. Namespace "minecraft" and path "logs_that_burn" will be assembled into "minecraft:logs_that_burn" first to query the cache, before the actual identifier creation ever happens. */
	public static Identifier getId(String namespace, String path) {
		if (namespace == null || namespace.length() <= 0) {
			namespace = "minecraft";
		}
		String fullPath = namespace + NAMESPACE_DELIMITER + path;
		if (fullIdCache.containsKey(fullPath)) return fullIdCache.get(fullPath);
		Identifier targetId = Identifier.fromNamespaceAndPath(namespace, path);
		fullIdCache.put(fullPath, targetId);
		return targetId;
	};
	/** Utility method for returning cached tags. Recommended due to faster cache hits. */
	public static Identifier getId(String fullPath) {
		if (!fullPath.contains(":")) {
			fullPath = "minecraft:" + fullPath;
		}
		if (fullIdCache.containsKey(fullPath)) return fullIdCache.get(fullPath);
		Identifier targetId = Identifier.parse(fullPath);
		fullIdCache.put(fullPath, targetId);
		return targetId;
	};
	/** Utility method for returning tag containers. */
	/*public static <T> Object getTagContainer(Registry<T> registry, TagKey<T> tagKey, Identifier id) {
		Object tagContainer = registry.get(id);
		return tagContainer;
	}*/
	/** Utility method for returning cached tag keys. */
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
	};*/
	/** Utility method for returning registry ID keys like "minecraft:stone". */
	public static String getRegistryIDKey(Block block) {
		if (block == null) return null;
		return BuiltInRegistries.BLOCK.getKey(block).toString();
	}
	/** Utility method for returning registry ID keys like "minecraft:stick". */
	public static String getRegistryIDKey(Item item) {
		if (item == null) return null;
		return BuiltInRegistries.ITEM.getKey(item).toString();
	}
	/** Utility method for returning registry ID keys like "minecraft:stick". */
	public static String getRegistryIDKey(ItemStack itemStack) {
		if (itemStack == null || itemStack == ItemStack.EMPTY) return null;
		Item item = itemStack.getItem();
		return getRegistryIDKey(item);
	}
	/** Utility method for returning registry ID keys like "minecraft:horse". */
	public static String getRegistryIDKey(Entity entity) {
		if (entity == null) return null;
		return BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).toString();
	}
}
