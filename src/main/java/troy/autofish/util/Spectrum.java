package troy.autofish.util;

import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;

import troy.autofish.Autofish;

public class Spectrum {
	private static final String spectrumBobberClassId = "de.dafuqs.spectrum.entity.entity.SpectrumFishingBobberEntity";
	public static Entity getModdedBobber(ClientPlayerEntity player) {
		if (!Common.hasMod("spectrum")) return null;
		return null;
	}
	public static boolean isModdedBobber(Entity entity) {
		if (!Common.hasMod("spectrum")) return false;
		try {
			Class<?> spectrumBobberClass = Class.forName(spectrumBobberClassId);
			return spectrumBobberClass.isInstance(entity);
		} catch (Exception err) {
			Autofish.logSession.error("Spectrum bobber detection error: " + err.getMessage());
		}
		return false;
	}
}
