package troy.autofish.modded;

import java.util.HashMap;
import java.util.Map;

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
import troy.autofish.config.Config;

public class Common {
	private static FabricModAutofish modInstance = null;
	public static void initialize(FabricModAutofish mod) {
		modInstance = mod;
	}

	public static final FabricLoader fabricInstance = FabricLoader.getInstance();
	private static final Map<String, Boolean> modExistCache = new HashMap<>();

	public static final int damageSafeMargin = 1;
	private static ProjectileEntity lastBobber = null;
	private static ProjectileEntity lastPlayerBobber = null;

	// Just a simple cached detector of mods.
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
	public static PlayerEntity getPlayerOwner(ProjectileEntity entity) {
		if (entity == null) return null;
		Entity owner = entity.getOwner();
		if (owner instanceof PlayerEntity) return (PlayerEntity) owner;
		return null;
	}
	public static String getRegistryKey(Block block) {
		if (block == null) return null;
		return Registries.BLOCK.getId(block).toString();
	}
	public static String getRegistryKey(Item item) {
		if (item == null) return null;
		return Registries.ITEM.getId(item).toString();
	}
	public static String getRegistryKey(Entity entity) {
		if (entity == null) return null;
		return Registries.ENTITY_TYPE.getId(entity.getType()).toString();
	}
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
	public static boolean shouldNotReel(ItemStack itemStack) {
		ItemStack selectedItem = itemStack;
		int currentDamage = selectedItem.getDamage();
		int breakThreshold = selectedItem.getMaxDamage();
		LogSession.info("Item " + Common.getRegistryKey(selectedItem.getItem()) + " has damage at " + currentDamage + "/" + (breakThreshold) + ".");
		if (breakThreshold <= 0) return false; // It's unbreakable!
		return (modInstance.getConfig().isNoBreak() && currentDamage + damageSafeMargin >= breakThreshold);
	}
}
