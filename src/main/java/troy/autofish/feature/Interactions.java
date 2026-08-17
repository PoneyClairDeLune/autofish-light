package troy.autofish.feature;

import cc.ltgc.luneApi.PlayerUtils;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

public class Interactions {
	/** Notify the player of the open water check status. */
	public static boolean notifyOpenWater(Player player, Player bobberOwner, boolean checkResult, boolean useNewerMethod) {
		if (bobberOwner != null && (!useNewerMethod || player == bobberOwner)) {
			bobberOwner.sendOverlayMessage(
				Component.translatable(checkResult ? "info.autofish.open_water_detection.success" : "info.autofish.open_water_detection.fail")
			);
			return true;
		} else return false;
	}
	/** Use the fishing rod item. */
	public static void useRodItem(LocalPlayer player) {
		if (Detections.earlyReturn(player)) return;
		boolean targetHand = Detections.isOffhand(player);
		//LogSession.debug("Selected " + (targetHand ? "main " : "off") + "hand.");
		PlayerUtils.useItem(player, targetHand);
		return;
	}
	/** Cancel fishing rod usage by attempting to switch away from fishing rods, and returns <code>true</code> when successful. Useful for avoiding rod usage when avoidable with this method, while still allowing hard cancellation via explicit usage. */
	public static boolean cancelRodUsage(LocalPlayer player) {
		// TODO: Implement natural slot shifting - Detect closest unmatched slot on either direction of scrolling, then decide which direction to scroll to accordingly. Should be useful to help evade overly stringent server-side anti-cheat.
		return false;
	}
}
