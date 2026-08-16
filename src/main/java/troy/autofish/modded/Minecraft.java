package troy.autofish.modded;

import java.util.HashSet;
import java.util.Set;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.material.Fluid;

/** The vanilla <i>Minecraft</i> game and <i>Vanilla Backport</i>. */
public class Minecraft extends NamespacedContent {
	/** The vanilla <i>Minecraft</i> game and <i>Vanilla Backport</i>. */
	public Minecraft() {
		super("minecraft", "minecraft", "Minecraft");
	}

	protected boolean populateIds() {
		if (biteSoundIds.isEmpty()) {
			biteSoundIds.add("entity.fishing_bobber.splash");
			biteSoundIds.add("minecraft:entity.fishing_bobber.splash");
		}
		if (blockIdsInherentlylogged.isEmpty()) {
			blockIdsInherentlylogged.add("minecraft:bubble_column");
			blockIdsInherentlylogged.add("minecraft:kelp");
			blockIdsInherentlylogged.add("minecraft:kelp_plant");
			blockIdsInherentlylogged.add("minecraft:seagrass");
			blockIdsInherentlylogged.add("minecraft:tall_seagrass");
		}
		if (blockIdsLiquidlogged.isEmpty()) {
			blockIdsLiquidlogged.add("minecraft:brain_coral_fan");
			blockIdsLiquidlogged.add("minecraft:brain_coral_wall_fan");
			blockIdsLiquidlogged.add("minecraft:bubble_coral_fan");
			blockIdsLiquidlogged.add("minecraft:bubble_coral_wall_fan");
			blockIdsLiquidlogged.add("minecraft:dead_brain_coral_fan");
			blockIdsLiquidlogged.add("minecraft:dead_brain_coral_wall_fan");
			blockIdsLiquidlogged.add("minecraft:dead_bubble_coral_fan");
			blockIdsLiquidlogged.add("minecraft:dead_bubble_coral_wall_fan");
			blockIdsLiquidlogged.add("minecraft:dead_fire_coral_fan");
			blockIdsLiquidlogged.add("minecraft:dead_fire_coral_wall_fan");
			blockIdsLiquidlogged.add("minecraft:dead_horn_coral_fan");
			blockIdsLiquidlogged.add("minecraft:dead_horn_coral_wall_fan");
			blockIdsLiquidlogged.add("minecraft:dead_tube_coral_fan");
			blockIdsLiquidlogged.add("minecraft:dead_tube_coral_wall_fan");
			blockIdsLiquidlogged.add("minecraft:fire_coral_fan");
			blockIdsLiquidlogged.add("minecraft:fire_coral_wall_fan");
			blockIdsLiquidlogged.add("minecraft:glow_lichen");
			blockIdsLiquidlogged.add("minecraft:hanging_roots");
			blockIdsLiquidlogged.add("minecraft:horn_coral_fan");
			blockIdsLiquidlogged.add("minecraft:horn_coral_wall_fan");
			blockIdsLiquidlogged.add("minecraft:light");
			blockIdsLiquidlogged.add("minecraft:mangrove_propagule");
			blockIdsLiquidlogged.add("minecraft:resin_clump");
			blockIdsLiquidlogged.add("minecraft:sculk_vein");
			blockIdsLiquidlogged.add("minecraft:tube_coral_fan");
			blockIdsLiquidlogged.add("minecraft:tube_coral_wall_fan");
		}
		if (blockIdsNegligible.isEmpty()) {
			blockIdsNegligible.add("minecraft:lily_pad");
		}
		if (bobberIds.isEmpty()) {
			bobberIds.add("minecraft:fishing_bobber");
		}
		if (rodIds.isEmpty()) {
			rodIds.add("minecraft:fishing_rod");
		}
		return true;
	}
	protected boolean populateBlockTags() {
		if (blockTagsNegligible.isEmpty()) {
			blockTagsNegligible.add(BlockTags.AIR);
		}
		return true;
	}
	//@SuppressWarnings("unused")
	protected boolean populateFluidTags() {
		if (fluidTags.isEmpty()) {
			Set<TagKey<Fluid>> vanillaFluidTags = new HashSet<>();
			vanillaFluidTags.add(FluidTags.WATER);
			// TODO: Add a config entry allowing lava blocks to be used on vanilla rods. May require implementing conditional mod content flushes and rebuilds.
			if (true) {
				vanillaFluidTags.add(FluidTags.LAVA);
			}
			fluidTags.put("minecraft:fishing_rod", vanillaFluidTags);
		}
		return true;
	}
	protected boolean populateItemTags() {
		// Can be no-op as soon as a counter case is found.
		if (rodTags.isEmpty()) {
			rodTags.add(ItemTags.FISHING_ENCHANTABLE);
		}
		return true;
	}

	public Projectile getBobber(LocalPlayer player) {
		if (player == null) return null;
		return player.fishing;
	}
}
