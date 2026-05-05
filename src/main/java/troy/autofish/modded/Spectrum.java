package troy.autofish.modded;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;

import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import troy.autofish.LogSession;

public class Spectrum {
	private static Class<?> moddedAccessor = null;
	private static final String moddedAccessorClassId = "de.dafuqs.spectrum.api.entity.PlayerEntityAccessor";
	private static final HashSet<String> moddedBobbers = new HashSet<>(Arrays.asList("spectrum:lagoon_fishing_bobber", "spectrum:bedrock_fishing_bobber", "spectrum:molten_fishing_bobber"));
	private static final HashSet<String> moddedRods = new HashSet<>(Arrays.asList("spectrum:lagoon_rod", "spectrum:bedrock_fishing_rod", "spectrum:molten_rod"));
	public static ProjectileEntity getModdedBobber(ClientPlayerEntity player) {
		if (!Common.hasMod("spectrum")) return null;
		// This should immediately cause a crash if it throws.
		try {
			//Object playerAccessor = player;
			if (moddedAccessor == null) {
				moddedAccessor = Class.forName(moddedAccessorClassId);
				LogSession.info("Created a modded accessor for Spectrum.");
			}
			Method bobberRetriever = moddedAccessor.getMethod("getSpectrumBobber");
			if (bobberRetriever != null) {
				return (ProjectileEntity) bobberRetriever.invoke(player);
			}
		} catch (Exception err) {
			LogSession.error("Spectrum bobber retrieval error: " + err.getMessage());
		}
		return null;
	}
	public static boolean isModdedBobber(ProjectileEntity entity) {
		if (!Common.hasMod("spectrum")) return false;
		return moddedBobbers.contains(Common.getRegistryKey(entity));
	}
}
