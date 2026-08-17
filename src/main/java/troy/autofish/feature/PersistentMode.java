package troy.autofish.feature;

import cc.ltgc.luneApi.PlayerUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import troy.autofish.FabricModAutofish;
import troy.autofish.modded.Common;
import troy.autofish.scheduler.ActionType;

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
	/** How long should one cycle of the two-cycle rod canceller take. */
	public final long rodCancelDelay = 200; // TODO: Delay should be scheduled to accomodate the observed server latency as well, capped at 1000ms.
	/** How many ticks should pass for the old persistent mode to kick in. */
	private final long legacyTickCycle = checkInterval * 10;

	// Variables
	/** The current tick value increased by the persistence mode checker. Not a Minecraft tick. */
	private long tickCurrent = 0L; // 5 ≈ 1s
	/** The last tick the legacy persistent mode check was run on. */
	private long tickLegacyLast = -4611686018427387904L;

	public void tick(boolean hookExists, Minecraft client) {
		tickCurrent ++;
		if (tickCurrent < -2305843009213693952L) {
			tickCurrent = -2305843009213693952L;
			tickLegacyLast = -4611686018427387904L;
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
				// TODO: Allow the player to disable natural rod cancellation.
				if (true && mixedActions.cancelRodUsage(player, rodCancelDelay)) {
					modInstance.getScheduler().scheduleAction(
						ActionType.RESTORE_ROD_STATUS,
						rodCancelDelay + 50,
						() -> {
							if (modInstance.getScheduler().isRecastQueued()) return;
							// The line from the rod on the main hand should be reeled-in regardless. Time to recast!
							if (Common.isFishingRod(PlayerUtils.getHeldStack(player, false))) Interactions.useRodItem(player);
						}
					);
					return;
				} else {
					// Failed to use the slot switch strategy, but still needs to reel the line in.
					Interactions.useRodItem(player);
				}
			}
			if (modInstance.getScheduler().isRecastQueued()) return;
			// Line should now be broken. Time to restore the bobber.
			Interactions.useRodItem(player);
		} else {
			// TODO: New persistence mode implementation go here.
		}
	}
}
