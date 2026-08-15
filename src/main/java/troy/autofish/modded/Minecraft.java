package troy.autofish.modded;

import java.util.HashSet;
import java.util.Set;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.material.Fluid;

/** The vanilla <i>Minecraft</i> game and <i>Vanilla Backport</i>. */
public class Minecraft extends NamespacedContent {
	public Minecraft() {
		super("minecraft", "minecraft", "Minecraft");
	}

	public void populateIds() {
		if (bobberIds.size() <= 0) {
			bobberIds.add("minecraft:fishing_bobber");
		}
		if (rodIds.size() <= 0) {
			rodIds.add("minecraft:fishing_rod");
		}
	}
	@SuppressWarnings("unused")
	public void populateFluidTags() {
		Set<TagKey<Fluid>> vanillaFluidTags = new HashSet<>();
		vanillaFluidTags.add(FluidTags.WATER);
		// TODO: Add a config entry allowing lava blocks to be used on vanilla rods.
		if (false) {
			vanillaFluidTags.add(FluidTags.LAVA);
		}
		fluidTags.put("minecraft:fishing_rod", vanillaFluidTags);
	}
	public void populateItemTags() {
		// Can be no-op as soon as a counter case is found.
		rodTags.add(ItemTags.FISHING_ENCHANTABLE);
	}

	public Projectile getBobber(LocalPlayer player) {
		if (player == null) return null;
		return player.fishing;
	}
}
