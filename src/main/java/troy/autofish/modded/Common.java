package troy.autofish.modded;

import java.util.HashMap;
import java.util.Set;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import troy.autofish.FabricModAutofish;
import troy.autofish.LogSession;

/** Common methods used for allowing mod support. */
public class Common {
	private static FabricModAutofish modInstance = null;
	public static void initialize(FabricModAutofish mod) {
		modInstance = mod;
	}

	public static final FabricLoader fabricInstance = FabricLoader.getInstance();
	private static final HashMap<String, Boolean> modExistCache = new HashMap<>();

	/** How much durability should be left for rods to be safe. */
	public static final int damageSafeMargin = 1;
	private static int lastBobberId = 0;
	private static int lastPlayerBobberId = 0;

	/** A list of registered sound events. */
	private static Set<String> bobberSplashSoundList = Set.of(
		"entity.fishing_bobber.splash",
		"minecraft:entity.fishing_bobber.splash"
	);

	/** A cached detector of mods' presence. */
	public static Boolean hasMod(String modId) {
		if (modExistCache.containsKey(modId)) {
			return modExistCache.get(modId);
		} else {
			boolean modExistence = fabricInstance.isModLoaded(modId);
			modExistCache.put(modId, modExistence);
			LogSession.info("Mod \"" + modId + "\" " + (modExistence ? "exists" : "does not exist") + ".");
			return modExistence;
		}
	}
	private static Projectile getPlayerBobberInternal(LocalPlayer player) {
		if (player == null) return null;
		Projectile bobber = player.fishing;
		// Vanilla Minecraft.
		if (bobber != null) return bobber;
		// Add more mods here.
		if (hasMod("spectrum")) {
			bobber = Spectrum.getModdedBobber(player);
			if (bobber != null) return bobber;
		}
		return null;
	}
	/** Grabs the bobber of a player. */
	public static Projectile getPlayerBobber(LocalPlayer player) {
		Projectile bobber = getPlayerBobberInternal(player);
		int bobberId = bobber == null ? 0 : bobber.getId();
		if (bobberId != lastPlayerBobberId) {
			if (bobber == null) {
				LogSession.debug("No bobber has been found.");
			} else {
				LogSession.debug("Found player bobber: " + getRegistryKey(bobber) + ".");
			}
		}
		lastPlayerBobberId = bobberId;
		return bobber;
	}
	/** Returns the owner of the bobber. */
	public static Player getPlayerOwner(Projectile entity) {
		if (entity == null) return null;
		Entity owner = entity.getOwner();
		if (owner instanceof Player) return (Player) owner;
		return null;
	}
	/** Utility method for returning registry keys. */
	public static String getRegistryKey(Block block) {
		if (block == null) return null;
		return BuiltInRegistries.BLOCK.getKey(block).toString();
	}
	/** Utility method for returning registry keys. */
	public static String getRegistryKey(Item item) {
		if (item == null) return null;
		return BuiltInRegistries.ITEM.getKey(item).toString();
	}
	/** Utility method for returning registry keys. */
	public static String getRegistryKey(Entity entity) {
		if (entity == null) return null;
		return BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).toString();
	}
	/** Returns true if the entity is a fishing bobber. */
	public static boolean isBobber(Projectile entity) {
		if (entity == null) {
			lastBobberId = 0;
			return false;
		};
		boolean bobberVerdict = entity instanceof FishingHook || Spectrum.isModdedBobber(entity);
		int entityId = entity.getId();
		if (entityId != lastBobberId) {
			LogSession.info("Entity " + getRegistryKey(entity) + (bobberVerdict ? " is" : " is not") + " a bobber.");
		}
		lastBobberId = entityId;
		return bobberVerdict;
	}
	/** Returns true if the block does not obstruct fishing, like a lily pad. */
	public static boolean isFishableFlora(Block block) {
		if (block == null) return false;
		String blockId = getRegistryKey(block);
		if (blockId.equals("minecraft:lily_pad")) return true;
		return false;
	}
	/** Returns true if the liquid is fishable. Should be superceded, as different fishing rods have different allowed liquids to fish in. */
	public static boolean isFishableLiquid(Block block) {
		if (block == null) return false;
		String blockId = getRegistryKey(block);
		if (blockId.equals("minecraft:water")) return true;
		boolean isFishable = false;
		// Modded section here.
		if (!isFishable && hasMod("spectrum")) {
			isFishable = Spectrum.isFishableLiquid(blockId);
		}
		return isFishable;
	}
	/** Returns true if the item is a fishing rod. */
	public static boolean isFishingRod(Item rodItem) {
		if (rodItem == null) return false;
		String itemId = getRegistryKey(rodItem);
		if (itemId.equals("minecraft:fishing_rod")) return true;
		boolean isRod = false;
		// Tags can go here.
		// Modded section here.
		if (!isRod && hasMod("spectrum")) {
			isRod = Spectrum.isModdedRod(itemId);
		}
		return isRod;
	}
	/** Returns true if the liquid is fishable with the given rod. */
	public static boolean isLiquidFishableIn(Item rodItem, Block block) {
		// WIP
		return false;
	}
	/** Returns true if the sound event is bobber splash. */
	public static boolean isSplashSound(SoundEvent soundEvent) {
		String soundName = soundEvent.location().toString();
		return bobberSplashSoundList.contains(soundName.toLowerCase());
	}
	/** If true, the rod should not be either reeled or thrown. */
	public static boolean shouldNotReel(ItemStack itemStack) {
		ItemStack selectedItem = itemStack;
		int currentDamage = selectedItem.getDamageValue();
		int breakThreshold = selectedItem.getMaxDamage();
		LogSession.debug("Item " + Common.getRegistryKey(selectedItem.getItem()) + " has damage at " + currentDamage + "/" + (breakThreshold) + ".");
		boolean noReelingVerdict = false;
		if (breakThreshold > 0) {
			// There's little sense to not use rods that are unbreakable, right?
			noReelingVerdict = modInstance.getConfig().isNoBreak() && currentDamage + damageSafeMargin >= breakThreshold;
		}
		LogSession.debug("The fishing rod shoul" + (noReelingVerdict ? "dn't" : "d") + " reel.");
		return noReelingVerdict;
	}
}
