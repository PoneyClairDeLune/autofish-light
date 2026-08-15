package troy.autofish.modded;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import cc.ltgc.luneApi.*;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import troy.autofish.FabricModAutofish;
import troy.autofish.LogSession;

/** Common methods used for allowing mod support. */
public class Common {
	//private static final Map<String, Boolean> modExistCache = new HashMap<>();

	/** How much durability should be left for rods to be safe. */
	public static int damageSafeMargin = 1; // TODO: Make it configurable.
	private static String lastRodItemId = null;
	private static int lastBobberId = 0;
	private static int lastPlayerBobberId = 0;
	private static boolean determinedByTag = false;
	private static final Map<String, NamespacedContent> registeredContent = new ConcurrentHashMap<>();
	private static final Iterable<NamespacedContent> registeredContentList = registeredContent.values();

	private static FabricModAutofish modInstance = null;
	private static void addContent(NamespacedContent content) {
		registeredContent.put(content.namespace, content);
	}
	public static void initialize(FabricModAutofish mod) {
		modInstance = mod;
		addContent(new Minecraft());
		addContent(new GoFish());
		addContent(new Spectrum());
	}

	private static Projectile getPlayerBobberInternal(LocalPlayer player) {
		if (player == null) return null;
		for (NamespacedContent content: registeredContentList) {
			Projectile bobber = content.getBobber(player);
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
	/** Returns true if the block does not obstruct fishing, like a lily pad. */
	public static boolean isBlockNegligible(BlockState blockState) {
		if (blockState == null) return false;
		NamespacedContent content = registeredContent.get(RegistryUtils.getNamespace(blockState));
		if (content != null) {
			return content.isBlockNegligible(blockState);
		}
		return false;
	}
	/** Returns true if the entity is a fishing bobber. */
	public static boolean isBobber(Projectile entity) {
		if (entity == null) {
			lastBobberId = 0;
			return false;
		}
		boolean bobberVerdict = entity instanceof FishingHook;
		if (!bobberVerdict) {
			NamespacedContent content = registeredContent.get(RegistryUtils.getNamespace(entity));
			if (content != null) {
				bobberVerdict = content.isBobber(entity);
			}
			/*for (NamespacedContent content: registeredContentList) {
				bobberVerdict = content.isBobber(entity);
				if (bobberVerdict) break;
			}*/
		}
		int entityId = entity.getId();
		if (entityId != lastBobberId) {
			LogSession.info("Entity " + RegistryUtils.getIdKey(entity) + (bobberVerdict ? " is" : " is not") + " a bobber.");
		}
		lastBobberId = entityId;
		return bobberVerdict;
	}
	/** Returns true if the liquid is fishable to the given rod. */
	public static boolean isLiquidFishableTo(ItemStack itemStack, BlockState blockState) {
		if (blockState == null) return false;
		if (itemStack == null || itemStack.count() <= 0) return false;
		FluidState fluidState = blockState.getFluidState();
		return isLiquidFishableTo(itemStack, fluidState);
	}
	/** Returns true if the liquid is fishable to the given rod. */
	public static boolean isLiquidFishableTo(ItemStack itemStack, FluidState fluidState) {
		if (fluidState == null) return false;
		if (itemStack == null || itemStack.count() <= 0) return false;
		boolean isFishable = false;
		String itemId = RegistryUtils.getIdKey(itemStack);
		NamespacedContent content = registeredContent.get(RegistryUtils.getNamespace(itemStack));
		if (content != null) {
			isFishable = content.isLiquidFishableTo(itemId, fluidState);
		}
		return isFishable;
	}
	/** Returns true if the liquid is fishable to the rod held by the given player. */
	public static boolean isLiquidFishableTo(Player player, BlockState blockState) {
		if (player == null) return false;
		if (blockState == null) return false;
		return isLiquidFishableTo(PlayerUtils.getHeldStack(player, false), blockState);
	}
	private static boolean isFishingRodInternal(Item rodItem) {
		if (rodItem == null) return false;
		determinedByTag = false;
		String itemId = RegistryUtils.getIdKey(rodItem);
		NamespacedContent content = registeredContent.get(RegistryUtils.getNamespace(rodItem));
		if (content != null) {
			return content.isRod(itemId);
		}
		return false;
	}
	private static boolean isFishingRodInternal(ItemStack rodItemStack) {
		if (rodItemStack == null || rodItemStack.count() <= 0) return false;
		determinedByTag = true;
		NamespacedContent content = registeredContent.get(RegistryUtils.getNamespace(rodItemStack));
		if (content != null) {
			return content.isRod(rodItemStack);
		}
		return false;
	}
	/** Returns true if the item is a fishing rod. */
	public static boolean isFishingRod(Item rodItem) {
		if (rodItem == null) return false;
		boolean isRod = isFishingRodInternal(rodItem) || isFishingRodInternal(rodItem.getDefaultInstance());
		// Log section.
		String currentRodId = RegistryUtils.getIdKey(rodItem);
		if (isRod && currentRodId != lastRodItemId) {
			LogSession.debug("Item " + currentRodId + " is considered a fishing rod by " + (determinedByTag ? "tag" : "ID") + ".");
		}
		lastRodItemId = currentRodId;
		return isRod;
	}
	/** Returns true if the item is a fishing rod. */
	public static boolean isFishingRod(ItemStack rodItemStack) {
		if (rodItemStack == null || rodItemStack.count() <= 0) return false;
		boolean isRod = isFishingRodInternal(rodItemStack) || isFishingRodInternal(rodItemStack.getItem());
		// Log section.
		String currentRodId = RegistryUtils.getIdKey(rodItemStack);
		if (isRod && currentRodId != lastRodItemId) {
			LogSession.debug("Item " + currentRodId + " is considered a fishing rod by " + (determinedByTag ? "tag" : "ID") + ".");
		}
		lastRodItemId = currentRodId;
		return isRod;
	}
	/** Returns true if the sound event is bobber splash. */
	public static boolean isSplashSound(SoundEvent soundEvent) {
		if (soundEvent == null) return false;
		NamespacedContent content = registeredContent.get(RegistryUtils.getNamespace(soundEvent));
		if (content != null) {
			return content.isFishBiteSound(soundEvent);
		}
		return false;
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
