package troy.autofish;

import cc.ltgc.luneApi.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.NonNullList;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundSystemChatPacket;
import net.minecraft.util.StringUtil;
import net.minecraft.util.Util;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import troy.autofish.feature.Detections;
import troy.autofish.feature.Interactions;
import troy.autofish.feature.MixedActions;
import troy.autofish.feature.PersistentMode;
import troy.autofish.modded.Common;
import troy.autofish.monitor.FishMonitorMP;
import troy.autofish.monitor.FishMonitorMPMotion;
import troy.autofish.monitor.FishMonitorMPSound;
import troy.autofish.scheduler.ActionType;

public class Autofish {
	private FabricModAutofish modAutofish;
	private FishMonitorMP fishMonitorMP;
	private MixedActions mixedActions;
	private PersistentMode persistentMode;

	private boolean hookExists = false;
	private long hookRemovedAt = 0L;
	public long timeMillis = 0L;

	public Autofish(FabricModAutofish modAutofish) {
		this.modAutofish = modAutofish;
		setDetection();
		Common.initialize(modAutofish);
		mixedActions = new MixedActions(modAutofish);
		persistentMode = new PersistentMode(modAutofish, mixedActions);
		LogSession.info("Autofish is now activated!");

		// Initiate the repeating action for persistent mode casting.
		// Invocation of the new implementation here.
		modAutofish.getScheduler().scheduleRepeatingAction(1000 / persistentMode.checkInterval, this::persistenceModeTick);
	}

	public void tick(Minecraft client) {
		if (client.level == null || client.player == null) return;
		if (
			modAutofish.getConfig().modEnabled()
		) {
			timeMillis = Util.getMillis(); // Update current working time for this tick.
			Projectile bobber = Common.getPlayerBobber(client.player);
			if (isRodHeld(client.player)) {
				if (bobber == null) {
					removeHook();
					return;
				}
				hookExists = true;
				// Multiplayer catch listener
				fishMonitorMP.hookTick(this, client, bobber);
			} else {
				removeHook();
			}
		}
	}

	/**
	* Callback from mixin when sound and motion packets are received
	* For multiplayer detection only
	*/
	public void handlePacket(Packet<?> packet) {
		if (modAutofish.getConfig().modEnabled()) {
			fishMonitorMP.handlePacket(this, packet, EnvUtils.client());
		}
	}

	/**
	* Callback from mixin when chat packets are received
	* For multiplayer detection only
	*/
	public void handleChat(ClientboundSystemChatPacket packet) {
		if (
			!modAutofish.getConfig().modEnabled() ||
			EnvUtils.client().isLocalServer() ||
			!isRodHeld(EnvUtils.client().player)
		) return;
		// Check if the hook either exists or was just removed.
		// This prevents false casts if a rod is held but isn't used for fishing.
		if (hookExists || (timeMillis - hookRemovedAt < 2000)) {
			// No-op if the matcher string is blank.
			if (modAutofish.getConfig().isClearLagRegexEmpty()) return;
			// Check if it matches.
			if (modAutofish.getConfig().matchClearLagPattern(
				StringUtil.stripColor(
					packet.content().getString()
				)
			)) {
				queueRecast();
			}
		}
	}

	public void reelRod() {
		boolean emitReelingLogs = !modAutofish.getConfig().noisyDetection();
		if (!modAutofish.getScheduler().isRecastQueued()) { // Prevents double reels.
			LocalPlayer player = EnvUtils.client().player;
			if (player != null) {
				checkAndNotifyOpenWater(Common.getPlayerBobber(player), player);
			}
			if (emitReelingLogs) LogSession.info("Reeling scheduled.");
			// Queue actions.
			queueRodSwitch();
			queueRecast();
			modAutofish.getScheduler().scheduleAction(ActionType.REEL_IN, modAutofish.getConfig().getReelInDelay(), () -> Interactions.useRodItem(player));
		} else {
			if (emitReelingLogs) LogSession.info("Reeling prevented.");
		}
	}

	public void queueRecast() {
		modAutofish.getScheduler().scheduleAction(
			ActionType.RECAST,
			getRandomDelay() + modAutofish.getConfig().getReelInDelay(),
			() -> {
				if (hookExists) return;
				if (!isRodHeld(EnvUtils.client().player)) return;
				ItemStack heldOnHand = PlayerUtils.getHeldStack(EnvUtils.client().player, Detections::isPredicateFishingRod);
				if (Common.shouldNotReel(heldOnHand)) return;
				Interactions.useRodItem(EnvUtils.client().player);
			}
		);
	}

	private void queueRodSwitch() {
		modAutofish.getScheduler().scheduleAction(
			ActionType.ROD_SWITCH,
			(long) (getRandomDelay() * 0.83) + modAutofish.getConfig().getReelInDelay(),
			() -> {
				if (!modAutofish.getConfig().multiRod()) return;
				switchToFirstRod(EnvUtils.client().player);
			}
		);
	}

	/**
	* When the hook disappears, call this method.
	*/
	private void removeHook() {
		if (hookExists) {
			hookExists = false;
			hookRemovedAt = timeMillis;
			fishMonitorMP.handleHookRemoved();
		}
	}

	public void switchToFirstRod(LocalPlayer player) {
		if (player == null) return;
		Inventory inventory = player.getInventory();
		NonNullList<ItemStack> mainInventory = inventory.getNonEquipmentItems();
		int inventorySize = mainInventory.size();
		for (int i = 0; i < inventorySize; i ++) {
			if (i >= 9) break; // Hotbar only.
			ItemStack slotStack = mainInventory.get(i);
			if (Common.isFishingRod(slotStack)) {
				if (modAutofish.getConfig().rodBreakAvoided()) {
					if (slotStack.getDamageValue() + Common.damageSafeMargin() < slotStack.getMaxDamage()) {
						inventory.setSelectedSlot(i);
						return;
					}
				} else {
					inventory.setSelectedSlot(i);
					return;
				}
			}
		}
	}

	public void setDetection() {
		if (modAutofish.getConfig().soundUsed()) {
			fishMonitorMP = new FishMonitorMPSound();
		} else {
			fishMonitorMP = new FishMonitorMPMotion();
		}
	}

	private long getRandomDelay(){
		return (
			Math.random() >= 0.5 ?
			(long) (modAutofish.getConfig().getRecastDelay() * (1 - (Math.random() * modAutofish.getConfig().getRandomDelay() * 0.01))) :
			(long) (modAutofish.getConfig().getRecastDelay() * (1 + (Math.random() * modAutofish.getConfig().getRandomDelay() * 0.01)))
		);
	}

	// Rewritten sections go below.
	// Combined actions.
	private void checkAndNotifyOpenWater(Projectile bobber, Player player) {
		mixedActions.isInOpenWaterThenNotify(
			bobber, player,
			modAutofish.getConfig().openWaterDetected(),
			modAutofish.getConfig().openWaterNewAlgo(),
			modAutofish.getConfig().unsafeFluids(),
			modAutofish.getConfig().noisyDetection()
		);
	}
	private boolean isRodHeld(Player player) {
		return mixedActions.isRodHeldThenNotify(
			player,
			modAutofish.getConfig().noisyDetection()
		);
	}
	private void persistenceModeTick() {
		persistentMode.tick(
			hookExists,
			EnvUtils.client()
		);
	}
}
