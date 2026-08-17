package troy.autofish.feature;

import cc.ltgc.luneApi.EnvUtils;
import cc.ltgc.luneApi.PlayerUtils;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import troy.autofish.modded.Common;

public class Detections {
	/** Common method to determine if a method should return early. */
	public static boolean earlyReturn(Player player) {
		if (player == null) return true;
		if (EnvUtils.client().level == null) return true;
		if (EnvUtils.client().gameMode == null) return true;
		return false;
	}

	/** Detect if the bobber is in water. */
	public static boolean isBobberInWater(LocalPlayer player, Projectile bobber, BlockState containedBlock, boolean useNewerMethod, boolean useUnsafeFluid) {
		if (
			player == null ||
			bobber == null ||
			containedBlock == null
		) return false;
		if (useNewerMethod) return Common.isLiquidloggedValid(player, containedBlock, useUnsafeFluid);
		return Common.isLiquidFishableTo(player, containedBlock, useUnsafeFluid);
	}
	/** Detect if the bobber entity is in open water. Useful to determine if treasure items are going to be obtained. */
	public static boolean isInOpenWater(Projectile bobber, Player player, boolean useNewerMethod, boolean useUnsafeFluid) {
		/*
		Source: https://minecraft.wiki/w/Fishing#Items_obtainable
		The open water check is passed when all horizontal layers within the bounding box match only one of the two criteria below.
		1. All of them are air blocks or lily pads.
		2. All of them are water source blocks, or waterlogged blocks without collision. Flowing water is prohibited.
		As an example, if a layer has both air blocks and water source blocks, the check fails. And even with the second criteria, if the fishing rod is in the bubble column directly, the check fails.
		The detection bounding box is defined as 5×4×5 (w×h×d), centered around the bobber entity. 	As such, the box is defined as [a-2, a+2] on both horizontal directions, however this left ambiguity on the vertical axis. Is it (a-2, a+2] or [a-2, a+2) that formed the four-block height?
		*/
		if (bobber == null) return false;
		// Old detection method inherited with some refactoring.
		Level bobberWorld = bobber.level();
		int blockX = bobber.getBlockX();
		int blockY = bobber.getBlockY();
		int blockZ = bobber.getBlockZ();
		boolean verdict = false;
		// Bounding box check
		for (int deltaY = (useNewerMethod ? -1 : -2); deltaY <= 2; deltaY ++) {
			boolean liquidVerdict = BlockPos.betweenClosedStream(
				blockX - 2, blockY + deltaY, blockZ - 2,
				blockX + 2, blockY + deltaY, blockZ + 2
			).allMatch(blockPos -> {
				BlockState blockState = bobberWorld.getBlockState(blockPos);
				if (useNewerMethod) return Common.isLiquidloggedValid(player, blockState, useUnsafeFluid);
				return Common.isLiquidFishableTo(player, blockState, useUnsafeFluid);
			});
			boolean blockVerdict = BlockPos.betweenClosedStream(
				blockX - 2, blockY + deltaY, blockZ - 2,
				blockX + 2, blockY + deltaY, blockZ + 2
			).allMatch(blockPos -> Common.isBlockNegligible(bobberWorld.getBlockState(blockPos)));
			verdict = liquidVerdict || blockVerdict;
			if (!verdict) break;
		}
		// Final check
		if (useNewerMethod && verdict) {
			// The older implementation does not directly check for bubble columns.
			if (
				bobberWorld.getBlockState(
					bobber.blockPosition()
				).getBlock() == Blocks.BUBBLE_COLUMN
			) verdict = false;
		}
		return verdict;
	}
	/** Determine if the item on the offhand should be used. */
	public static boolean isOffhand(Player player) {
		return PlayerUtils.matchOffhandItem(player, Detections::isPredicateFishingRod);
	}
	/** The predicate used to determine if the offhand should be used. */
	public static boolean isPredicateFishingRod(ItemStack itemStack) {
		return Common.isFishingRod(itemStack);
	}
	/** Detect if a fishing rod is held. */
	public static boolean isRodHeld(Player player) {
		if (earlyReturn(player)) return false;
		ItemStack heldItemStack = PlayerUtils.getHeldStack(player, Detections::isPredicateFishingRod);
		return Common.isFishingRod(heldItemStack);
	}
}
