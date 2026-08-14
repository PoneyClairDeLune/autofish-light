package troy.autofish.modded;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import troy.autofish.FabricModAutofish;
import troy.autofish.LogSession;
import troy.autofish.luneApi.*;

/** Common methods used for allowing mod support. */
public class Common {
	private static final Map<String, Boolean> modExistCache = new HashMap<>();

	/** How much durability should be left for rods to be safe. */
	public static final int damageSafeMargin = 1;
	private static Set<TagKey<Item>> eligibleRodTags = new HashSet<>(Arrays.asList(ItemTags.FISHING_ENCHANTABLE));
	private static String lastRodItemId = null;
	private static int lastBobberId = 0;
	private static int lastPlayerBobberId = 0;

	private static FabricModAutofish modInstance = null;
	public static void initialize(FabricModAutofish mod) {
		modInstance = mod;
	}

	/** A list of registered sound events. */
	private static Set<String> bobberSplashSoundList = new HashSet<>(Arrays.asList("entity.fishing_bobber.splash",
	"minecraft:entity.fishing_bobber.splash"));

	/** A cached detector of mods' presence. */
	public static boolean hasMod(String modId) {
		if (modExistCache.containsKey(modId)) {
			return modExistCache.get(modId);
		} else {
			boolean modExistence = EnvUtils.loader.isModLoaded(modId);
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
				LogSession.debug("Found player bobber: " + RegistryUtils.getIdKey(bobber) + ".");
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
	/** Returns true if the entity is a fishing bobber. */
	public static boolean isBobber(Projectile entity) {
		if (entity == null) {
			lastBobberId = 0;
			return false;
		}
		boolean bobberVerdict = entity instanceof FishingHook || Spectrum.isModdedBobber(entity);
		int entityId = entity.getId();
		if (entityId != lastBobberId) {
			LogSession.info("Entity " + RegistryUtils.getIdKey(entity) + (bobberVerdict ? " is" : " is not") + " a bobber.");
		}
		lastBobberId = entityId;
		return bobberVerdict;
	}
	/** Returns true if the block does not obstruct fishing, like a lily pad. */
	public static boolean isFishableFlora(Block block) {
		if (block == null) return false;
		String blockId = RegistryUtils.getIdKey(block);
		if (blockId.equals("minecraft:lily_pad")) return true;
		return false;
	}
	/** Returns true if the liquid is fishable. Should be superceded, as different fishing rods have different allowed liquids to fish in. */
	@Deprecated
	public static boolean isFishableLiquid(Block block) {
		if (block == null) return false;
		String blockId = RegistryUtils.getIdKey(block);
		if (blockId.equals("minecraft:water")) return true;
		if (blockId.equals("minecraft:lava")) return true;
		boolean isFishable = false;
		// Modded section here.
		if (!isFishable && hasMod("spectrum")) {
			isFishable = Spectrum.isFishableLiquid(blockId);
		}
		if (!isFishable && hasMod("gofish")) {
			isFishable = GoFish.isFishableLiquid(blockId);
		}
		return isFishable;
	}
	/** Returns true if the liquid is fishable to the given rod. */
	public static boolean isFishableLiquidTo(ItemStack itemStack, BlockState blockState) {
		if (blockState == null) return false;
		if (itemStack == null || itemStack.count() <= 0) return false;
		FluidState fluidState = blockState.getFluidState();
		if (fluidState == null) return false;
		boolean isFishable = false;
		boolean isHandled = false;
		if (RegistryUtils.getIdKey(itemStack).equalsIgnoreCase("minecraft:fishing_rod")) {
			isFishable = RegistryUtils.isIn(FluidTags.WATER, fluidState);
			isHandled = true;
		}
		// Modded section here.
		// Fallback here.
		if (!isHandled && !isFishable) {
			isFishable = RegistryUtils.isIn(FluidTags.WATER, fluidState) ||
				RegistryUtils.isIn(FluidTags.LAVA, fluidState);
		}
		return isFishable;
	}
	/** Returns true if the liquid is fishable to the rod held by the given player. */
	public static boolean isFishableLiquidTo(Player player, BlockState blockState) {
		if (player == null) return false;
		if (blockState == null) return false;
		return isFishableLiquidTo(PlayerUtils.getHeldStack(player, false), blockState);
	}
	private static boolean isFishingRodInternal(Item rodItem) {
		if (rodItem == null) return false;
		String itemId = RegistryUtils.getIdKey(rodItem);
		if (itemId.equals("minecraft:fishing_rod")) return true;
		boolean isRod = false;
		// Modded section here.
		if (!isRod && hasMod("spectrum")) {
			isRod = Spectrum.isModdedRod(itemId);
		}
		if (!isRod && hasMod("gofish")) {
			isRod = GoFish.isModdedRod(itemId);
		}
		// Log section.
		String currentRodId = RegistryUtils.getIdKey(rodItem);
		if (isRod && currentRodId != lastRodItemId) {
			LogSession.debug("Item " + currentRodId + " is considered a fishing rod by ID.");
		}
		lastRodItemId = currentRodId;
		return isRod;
	}
	private static boolean isFishingRodInternal(ItemStack rodItemStack) {
		if (rodItemStack == null || rodItemStack.count() <= 0) return false;
		boolean isRod = false;
		// Tags go here.
		for (TagKey<Item> itemTag: eligibleRodTags) {
			if (RegistryUtils.isIn(itemTag, rodItemStack)) isRod = true;
		}
		if (isRod) {
			String currentRodId = RegistryUtils.getIdKey(rodItemStack);
			if (currentRodId != lastRodItemId) {
				lastRodItemId = currentRodId;
				LogSession.debug("Item " + currentRodId + " is considered a fishing rod by tag.");
			}
		}
		return isRod;
	}
	/** Returns true if the item is a fishing rod. */
	public static boolean isFishingRod(Item rodItem) {
		if (rodItem == null) return false;
		return isFishingRodInternal(rodItem) || isFishingRodInternal(rodItem.getDefaultInstance());
	}
	/** Returns true if the item is a fishing rod. */
	public static boolean isFishingRod(ItemStack rodItemStack) {
		if (rodItemStack == null || rodItemStack.count() <= 0) return false;
		return isFishingRodInternal(rodItemStack) || isFishingRodInternal(rodItemStack.getItem());
	}
	/** Returns true if the sound event is bobber splash. */
	public static boolean isSplashSound(SoundEvent soundEvent) {
		String soundName = soundEvent.location().toString();
		return bobberSplashSoundList.contains(soundName.toLowerCase());
	}
	/** If true, the rod should not be either reeled or thrown. */
	public static boolean shouldNotReel(ItemStack selectedItem) {
		if (selectedItem == null) return true;
		int currentDamage = selectedItem.getDamageValue();
		int breakThreshold = selectedItem.getMaxDamage();
		LogSession.debug("Item " + RegistryUtils.getIdKey(selectedItem.getItem()) + " has damage at " + currentDamage + "/" + (breakThreshold) + ".");
		boolean noReelingVerdict = false;
		if (breakThreshold > 0) {
			// There's little sense to not use rods that are unbreakable, right?
			noReelingVerdict = modInstance.getConfig().isNoBreak() && currentDamage + damageSafeMargin >= breakThreshold;
		}
		LogSession.debug("The fishing rod shoul" + (noReelingVerdict ? "dn't" : "d") + " reel.");
		return noReelingVerdict;
	}
	/** If true, the rod should not be either reeled or thrown. */
	public static boolean shouldNotReel(Player player, boolean isOffhand) {
		if (player == null) return true;
		return shouldNotReel(PlayerUtils.getHeldStack(player, isOffhand));
	}
}
