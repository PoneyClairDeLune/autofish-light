// 2026 © Lumière Élevé
// The code below is licensed under GNU LGPL 3.0+ as part of Lune API.

package cc.ltgc.luneApi;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.SharedConstants;
import net.minecraft.client.ClientBrandRetriever;
import net.minecraft.client.Minecraft;
//import troy.autofish.LogSession;

/** Execution environment utilities. */
public class EnvUtils {
	private static Map<String, String> arguments;

	/** The Minecraft client. */
	public static Minecraft client() {
		return Minecraft.getInstance();
	}
	/** Example: <code>Minecraft 1.21.1 Fabric Fabulously Optimized</code> */
	public static String clientInstanceName() {
		return client().getLaunchedVersion();
	}
	/** Branding specifier of the client launcher.
	* <br>Example: <code>ExampleLauncher</code> */
	public static String clientLauncher() {
		return Minecraft.getLauncherBrand();
	}
	/** Example: <code>fabric</code> */
	public static String clientLoaderName() {
		return ClientBrandRetriever.getClientModName();
	}
	/** Example: <code>ExampleLauncher 1.23.456/ModLoader</code> */
	public static String clientVersionType() {
		return Minecraft.getInstance().getVersionType();
	}
	/** The game version ID. */
	public static String gameVersion() {
		return SharedConstants.getCurrentVersion().id();
	}
	/** The game version string. */
	public static String gameVersionString() {
		return SharedConstants.getCurrentVersion().name();
	}
	/** The mod loader instance. */
	public static FabricLoader loader() {
		return FabricLoader.getInstance();
	}

	/** Reusable initialization for launch arguments. */
	public static boolean populate() {
		if (arguments != null && !arguments.isEmpty()) return true;
		FabricLoader loader = loader();
		if (loader == null) return false;
		String[] launchArgs = loader.getLaunchArguments(true);
		if (launchArgs == null) return false;
		String mapKey = null;
		Map<String, String> newMap = new HashMap<>();
		for (String e: launchArgs) {
			if (e.startsWith("--")) {
				if (mapKey != null) {
					newMap.put(mapKey, null);
				}
				mapKey = e;
			} else if (e.startsWith("-")) {
				mapKey = null;
			} else {
				if (mapKey != null) {
					newMap.put(mapKey, e);
					mapKey = null;
				}
			}
		}
		if (mapKey != null) {
			newMap.put(mapKey, null);
		}
		arguments = Collections.unmodifiableMap(newMap);
		return true;
	}

	static {
		//if (client == null) client = Minecraft.getInstance();
		populate();
	}

	/** Retrive the value of a specific launch argument. */
	public static String getArgument(String arg) {
		if (!populate()) return null;
		return arguments.get(arg);
	}
	/** See if a launch argument is present. */
	public static boolean hasArgument(String arg) {
		if (!populate()) return false;
		return arguments.containsKey(arg);
	}
	/** List all launch arguments. */
	public static Set<String> listArguments() {
		if (!populate()) return null;
		return arguments.keySet();
	}
}
