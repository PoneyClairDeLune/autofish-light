package troy.autofish.modded;

import java.lang.reflect.Method;

import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import troy.autofish.LogSession;

public class Spectrum {
	private static Class<?> moddedAccessor = null;
	private static final String moddedAccessorClassId = "de.dafuqs.spectrum.api.entity.PlayerEntityAccessor";
	private static Class<?> moddedBobber = null;
	private static final String moddedBobberId = "de.dafuqs.spectrum.entity.entity.SpectrumFishingBobberEntity";
	public static ProjectileEntity getModdedBobber(ClientPlayerEntity player) {
		if (!Common.hasMod("spectrum")) return null;
		// This should immediately cause a crash if it throws.
		try {
			//Object playerAccessor = player;
			if (moddedAccessor == null) {
				moddedAccessor = Class.forName(moddedAccessorClassId);
			}
			if (moddedAccessor.isInstance(player)) {
				Method bobberRetriever = moddedAccessor.getMethod("getSpectrumBobber");
				return (ProjectileEntity) bobberRetriever.invoke(player);
			}
		} catch (Exception err) {
			LogSession.error("Spectrum bobber retrieval error: " + err.getMessage());
		}
		return null;
	}
	public static boolean isModdedBobber(ProjectileEntity entity) {
		if (!Common.hasMod("spectrum")) return false;
		try {
			if (moddedBobber == null) {
				moddedBobber = Class.forName(moddedBobberId);
			}
			return moddedBobber.isInstance(entity);
		} catch (Exception err) {
			LogSession.error("Spectrum bobber detection error: " + err.getMessage());
		}
		return false;
	}
}
