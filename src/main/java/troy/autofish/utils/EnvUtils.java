// 2026 © Lumière Élevé
// The code below is licensed under GNU LGPL 3.0+ as part of Lune API.

package troy.autofish.utils;

import net.minecraft.client.Minecraft;

/** Execution environment utilities. */
public class EnvUtils {
	public static Minecraft client;

	static {
		if (client == null) client = Minecraft.getInstance();
	}
}
