package troy.autofish.modded;

import java.util.HashMap;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.Block;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.FishingBobberEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
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
	private static ProjectileEntity lastBobber = null;
	private static ProjectileEntity lastPlayerBobber = null;

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
	private static ProjectileEntity getPlayerBobberInternal(ClientPlayerEntity player) {
		if (player == null) return null;
		ProjectileEntity bobber = player.fishHook;
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
	public static ProjectileEntity getPlayerBobber(ClientPlayerEntity player) {
		ProjectileEntity bobber = getPlayerBobberInternal(player);
		if (bobber != lastPlayerBobber) {
			if (bobber == null) {
				LogSession.info("No bobber has been found.");
			} else {
				LogSession.info("Found player bobber: " + getRegistryKey(bobber) + ".");
			}
		}
		lastPlayerBobber = bobber;
		return bobber;
	}
	/** Returns the owner of the bobber. */
	public static PlayerEntity getPlayerOwner(ProjectileEntity entity) {
		if (entity == null) return null;
		Entity owner = entity.getOwner();
		if (owner instanceof PlayerEntity) return (PlayerEntity) owner;
		return null;
	}
	/** Utility method for returning registry keys. */
	public static String getRegistryKey(Block block) {
		if (block == null) return null;
		return Registries.BLOCK.getId(block).toString();
	}
	/** Utility method for returning registry keys. */
	public static String getRegistryKey(Item item) {
		if (item == null) return null;
		return Registries.ITEM.getId(item).toString();
	}
	/** Utility method for returning registry keys. */
	public static String getRegistryKey(Entity entity) {
		if (entity == null) return null;
		return Registries.ENTITY_TYPE.getId(entity.getType()).toString();
	}
	/** Returns if the entity is a fishing bobber. */
	public static boolean isBobber(ProjectileEntity entity) {
		if (entity == null) {
			lastBobber = null;
			return false;
		};
		boolean bobberVerdict = entity instanceof FishingBobberEntity || Spectrum.isModdedBobber(entity);
		if (lastBobber != entity) {
			LogSession.info("Entity " + getRegistryKey(entity) + (bobberVerdict ? " is" : " is not") + " a bobber.");
		}
		lastBobber = entity;
		return bobberVerdict;
	}
	/**
	* If true, the rod should not be either reeled or thrown.
	*/
	public static boolean shouldNotReel(ItemStack itemStack) {
		ItemStack selectedItem = itemStack;
		int currentDamage = selectedItem.getDamage();
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
