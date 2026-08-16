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
	/** Returns true when the offhand matches the given predicate while the main hand doesn't. */
	public static boolean matchOffhandItem(Player player, Predicate<? super ItemStack> matcher) {
		if (player == null) return false;
		if (matcher == null) return false;
		ItemStack offhandItemStack = player.getOffhandItem();
		if (ItemUtils.isStackEmpty(offhandItemStack)) return false;
		boolean offhandMatched = matcher.test(offhandItemStack);
		if (offhandMatched) {
			ItemStack mainHandItemStack = player.getMainHandItem();
			if (ItemUtils.isStackEmpty(mainHandItemStack)) return true;
			return !matcher.test(mainHandItemStack);
		};
		return false;
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
}
