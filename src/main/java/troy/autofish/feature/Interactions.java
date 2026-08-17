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
}
