// 2026 © Lumière Élevé
// The code below is licensed under GNU LGPL 3.0+ as part of Lune API.

package cc.ltgc.luneApi;

import java.util.function.Predicate;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.player.Player;

/** Utility methods for players. Should only be used in worlds. */
public class PlayerUtils {
	/** The vanilla hotbar size default. */
	private static final int maxHotbarSlots = 9;

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
		player.getInventory().setSelectedSlot(slotIndex);
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
		return wrapHotbarSlot(slot, maxHotbarSlots);
	}
	/** Wraps the input around valid hotbar slots with a customized size. */
	public static int wrapHotbarSlot(int slot, int size) {
		slot %= size;
		if (slot < 0) slot += size;
		return slot;
	}
}
