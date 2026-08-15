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
		if (blockIds.isEmpty()) {
			blockIds.add("minecraft:lily_pad");
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
		if (blockTags.isEmpty()) {
			blockTags.add(BlockTags.AIR);
		}
		return true;
	}
	//@SuppressWarnings("unused")
	protected boolean populateFluidTags() {
		if (fluidTags.isEmpty()) {
			Set<TagKey<Fluid>> vanillaFluidTags = new HashSet<>();
			vanillaFluidTags.add(FluidTags.WATER);
			// TODO: Add a config entry allowing lava blocks to be used on vanilla rods.
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
