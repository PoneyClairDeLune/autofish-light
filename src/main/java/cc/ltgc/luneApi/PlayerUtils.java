// 2026 © Lumière Élevé
// The code below is licensed under GNU LGPL 3.0+ as part of Lune API.

package cc.ltgc.luneApi;

import java.util.function.Predicate;

import net.minecraft.core.NonNullList;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;

/** Utility methods for players. Should only be used in worlds. */
public class PlayerUtils {
	/** The vanilla hotbar size default. */
	public static final int MAX_HOTBAR_SLOTS = 9;

	/** Retrieve the correct hand from a boolean. */
	public static InteractionHand getHand(boolean isOffhand) {
		return isOffhand ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
	}
	/** Returns the held item stack of the specified player. */
	public static ItemStack getHeldStack(Player player, boolean isOffhand) {
		if (player == null) return ItemStack.EMPTY;
		ItemStack itemStack = isOffhand ? player.getOffhandItem() : player.getMainHandItem();
		return itemStack;
	}
	/** Returns the held item stack of the specified player if the given predicate matches. */
	public static ItemStack getHeldStack(Player player, Predicate<? super ItemStack> matcher) {
		if (matcher == null) return ItemStack.EMPTY;
		return getHeldStack(player, matchOffhandItem(player, matcher));
	}
	/** Returns the closest slot in the player's hotbar that matches the given predicate, including the current one. Any negative value should be considered invalid. Returns <code>-1</code> when none matched. */
	public static int getClosestAllMatchInHotbar(Inventory playerInventory, Predicate<? super ItemStack> matcher, boolean invertResult) {
		return getClosestAllMatchInHotbar(playerInventory, matcher, PlayerUtils.MAX_HOTBAR_SLOTS, invertResult);
	}
	/** Returns the closest slot in the player's hotbar that matches the given predicate, including the current one. Any negative value should be considered invalid. Returns <code>-1</code> when none matched. */
	public static int getClosestAllMatchInHotbar(Inventory playerInventory, Predicate<? super ItemStack> matcher, int customHotbarSize, boolean invertResult) {
		if (playerInventory == null || matcher == null) return -1;
		if (customHotbarSize <= 0) return -1;
		int currentSlot = playerInventory.getSelectedSlot();
		if (matcher.test(playerInventory.getItem(currentSlot)) != invertResult) return currentSlot;
		return getClosestMatchInHotbar(playerInventory, matcher, customHotbarSize, invertResult);
	}
	/** Returns the closest slot in the player's hotbar that matches the given predicate, excluding the current one. Any negative value should be considered invalid. Returns <code>-1</code> when none matched. */
	public static int getClosestMatchInHotbar(Inventory playerInventory, Predicate<? super ItemStack> matcher, boolean invertResult) {
		return getClosestMatchInHotbar(playerInventory, matcher, PlayerUtils.MAX_HOTBAR_SLOTS, invertResult);
	}
	/** Returns the closest slot in the player's hotbar that matches the given predicate, excluding the current one. Any negative value should be considered invalid. Returns <code>-1</code> when none matched. */
	public static int getClosestMatchInHotbar(Inventory playerInventory, Predicate<? super ItemStack> matcher, int customHotbarSize, boolean invertResult) {
		if (playerInventory == null || matcher == null) return -1;
		if (customHotbarSize <= 0) return -1;
		final NonNullList<ItemStack> inventoryMain = playerInventory.getNonEquipmentItems();
		final int currentSlot = playerInventory.getSelectedSlot();
		final int hotbarSize = Math.min(customHotbarSize, inventoryMain.size()); // Only go through the hotbar.
		int closestSlotLeft = 127, closestSlotRight = 127; // Initialize to clearly invalid values.
		final int boundSlotLeft = currentSlot - (hotbarSize >> 1);
		final int boundSlotRight = currentSlot + (hotbarSize >> 1);
		for (int slot = currentSlot - 1; slot >= boundSlotLeft; slot --) {
			final int actualSlot = PlayerUtils.wrapHotbarSlot(slot, hotbarSize);
			ItemStack items = inventoryMain.get(actualSlot);
			if (matcher.test(items) != invertResult) {
				closestSlotLeft = actualSlot;
				break;
			}
		}
		for (int slot = currentSlot + 1; slot <= boundSlotRight; slot ++) {
			final int actualSlot = PlayerUtils.wrapHotbarSlot(slot, hotbarSize);
			ItemStack items = inventoryMain.get(actualSlot);
			if (matcher.test(items) != invertResult) {
				closestSlotRight = actualSlot;
				break;
			}
		}
		if (closestSlotLeft == closestSlotRight) {
			// The only case this can be true is when both are 127, the magic value of failure.
			return -1;
		} else if (closestSlotLeft == 127) {
			return closestSlotRight;
		} else if (closestSlotRight == 127) {
			return closestSlotLeft;
		}
		int distanceLeft = currentSlot - closestSlotLeft;
		if (distanceLeft < 0) distanceLeft += hotbarSize;
		int distanceRight = closestSlotRight - currentSlot;
		if (distanceRight < 0) distanceRight += hotbarSize;
		if (distanceLeft == distanceRight) {
			return Math.random() < 0.5 ? closestSlotLeft : closestSlotRight;
		} else {
			return distanceLeft < distanceRight ? closestSlotLeft : closestSlotRight;
		}
	}
	/** Returns a number about the matching state. 0b01 is main, 0b10 is off. */
	public static byte matchItemOnHands(Player player, Predicate<? super ItemStack> matcher) {
		if (player == null) return 0;
		if (matcher == null) return 0;
		final ItemStack itemStackMainHand = player.getMainHandItem();
		final ItemStack itemStackOffhand = player.getOffhandItem();
		byte testResult = 0;
		if (!ItemUtils.isStackEmpty(itemStackMainHand)) {
			if (matcher.test(itemStackMainHand)) testResult |= 1;
		}
		if (!ItemUtils.isStackEmpty(itemStackOffhand)) {
			if (matcher.test(itemStackOffhand)) testResult |= 2;
		}
		return testResult;
	}
	/** Returns true when the offhand matches the given predicate while the main hand doesn't. */
	public static boolean matchOffhandItem(Player player, Predicate<? super ItemStack> matcher) {
		byte matchVerdict = matchItemOnHands(player, matcher);
		if ((matchVerdict & 1) > 0) {
			return false;
		} else if (matchVerdict == 2) {
			return true;
		} else {
			return false;
		}
	}
	/** Change the selected hotbar slot (<code>[0, 8]</code>). */
	public static void selectHotbarSlot(Player player, int slotIndex) {
		if (player == null) return;
		selectHotbarSlot(player.getInventory(), slotIndex);
	}
	/** Change the selected hotbar slot (<code>[0, 8]</code>). */
	public static void selectHotbarSlot(Inventory playerInventory, int slotIndex) {
		if (playerInventory == null) return;
		playerInventory.setSelectedSlot(slotIndex);
	}
	/** Returns true when the item stack on the defined hand is used. */
	public static boolean useItem(Player player, boolean isOffhand) {
		if (EnvUtils.client().level == null) return false;
		if (EnvUtils.client().gameMode == null) return false;
		if (player == null) return false;
		InteractionHand targetHand = getHand(isOffhand);
		InteractionResult usageResult = EnvUtils.client().gameMode.useItem(player, targetHand);
		if (usageResult != null && usageResult.consumesAction()) {
			EnvUtils.client().player.swing(targetHand);
			EnvUtils.client().gameRenderer.itemInHandRenderer.itemUsed(targetHand);
			return true;
		}
		return false;
	}
	/** Wraps the input around valid hotbar slots. */
	public static int wrapHotbarSlot(int slot) {
		return wrapHotbarSlot(slot, MAX_HOTBAR_SLOTS);
	}
	/** Wraps the input around valid hotbar slots with a customized size. */
	public static int wrapHotbarSlot(int slot, int size) {
		slot %= size;
		if (slot < 0) slot += size;
		return slot;
	}
}
