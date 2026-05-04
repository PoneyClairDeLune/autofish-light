package troy.autofish.util;

import java.lang.reflect.Method;

import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import troy.autofish.Autofish;

public class Spectrum {
	private static final String moddedAccessorClassId = "de.dafuqs.spectrum.api.entity.PlayerEntityAccessor";
	private static final String moddedBobberClassId = "de.dafuqs.spectrum.entity.entity.SpectrumFishingBobberEntity";
	public static Entity getModdedBobber(ClientPlayerEntity player) {
		if (!Common.hasMod("spectrum")) return null;
		// This should immediately cause a crash if it throws.
		try {
			//Object playerAccessor = player;
			Class<?> playerAccessorClass = Class.forName(moddedAccessorClassId);
			if (playerAccessorClass.isInstance(player)) {
				Method bobberRetriever = playerAccessorClass.getMethod("getSpectrumBobber");
				return (Entity) bobberRetriever.invoke(player);
			}
		} catch (Exception err) {
			Autofish.logSession.error("Spectrum bobber retrieval error: " + err.getMessage());
		}
		return null;
	}
	public static boolean isModdedBobber(Entity entity) {
		if (!Common.hasMod("spectrum")) return false;
		try {
			Class<?> spectrumBobberClass = Class.forName(moddedBobberClassId);
			return spectrumBobberClass.isInstance(entity);
		} catch (Exception err) {
			Autofish.logSession.error("Spectrum bobber detection error: " + err.getMessage());
		}
		return false;
	}
}
