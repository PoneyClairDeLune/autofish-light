package troy.autofish.feature;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import troy.autofish.FabricModAutofish;
import troy.autofish.modded.Common;

public class PersistentMode {
	private FabricModAutofish modInstance = null;
	private MixedActions mixedActions = null;
	public PersistentMode(FabricModAutofish instance, MixedActions mixedActions) {
		modInstance = instance;
		this.mixedActions = mixedActions;
	}

	// Constants defining the bobber states.
	public final byte FLOAT_TRAVEL_NORMAL = 0;

	// Constants.
	/** How many checks per second should persistent mode use. */
	public final long checkInterval = 5L;
	/** How many ticks should pass for the old persistent mode to kick in. */
	private final long legacyTickCycle = checkInterval * 10;

	// Variables
	/** The current tick value increased by the persistence mode checker. Not a Minecraft tick. */
	private long tickCurrent = 0L; // 5 ≈ 1s
	/** The last tick the legacy persistent mode check was run on. */
	private long tickLegacyLast = -9223372036854775808L;

	public void tick(boolean hookExists, Minecraft client) {
		tickCurrent ++;
		if (tickCurrent < -4611686018427387904L) {
			tickCurrent = -4611686018427387904L;
			tickLegacyLast = Long.MIN_VALUE;
		}
		if (!modInstance.getConfig().modEnabled()) return;
		if (!modInstance.getConfig().persistentMode()) return;
		if (client.isPaused()) return;
		LocalPlayer player = client.player;
		if (Detections.earlyReturn(player)) return;
		if (modInstance.getConfig().legacyPersistence()) {
			if (tickCurrent - tickLegacyLast < legacyTickCycle) return;
			tickLegacyLast = tickCurrent;
			if (!mixedActions.isRodHeldThenNotify(
				player,
				modInstance.getConfig().noisyDetection()
			)) return;
			if (Common.shouldNotReel(player, false)) return;
			if (hookExists) {
				if (mixedActions.isBobberInWaterThenNotify(
					player,
					modInstance.getConfig().openWaterNewAlgo(),
					modInstance.getConfig().unsafeFluids()
				)) return;
				else Interactions.useRodItem(client.player);
			}
			if (modInstance.getScheduler().isRecastQueued()) return;
			Interactions.useRodItem(player);
		} else {
			// TODO: New persistence mode implementation go here.
		}
	}
}
