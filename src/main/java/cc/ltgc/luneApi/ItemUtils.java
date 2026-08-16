// 2026 © Lumière Élevé
// The code below is licensed under GNU LGPL 3.0+ as part of Lune API.

package cc.ltgc.luneApi;

import net.minecraft.world.item.ItemStack;

public class ItemUtils {
	/** Returns <code>true</code> if the given stack is empty. */
	public static boolean isStackEmpty(ItemStack itemStack) {
		// Minecraft still allows negative item counts!
		if (itemStack == null || itemStack == ItemStack.EMPTY || itemStack.count() == 0) return true;
		return false;
	}
}
