// 2026 © Lumière Élevé
// The code below is licensed under GNU LGPL 3.0+ as part of Lune API.

package cc.ltgc.luneApi;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

public class ReflectorUtils {
	private static final Map<String, Class<?>> accessorCache = new ConcurrentHashMap<>();
	/** Retrieve the class accessor from a given class name.
	* <br/>If exception is to be thrown, the types follow the exact same as <code>java.lang.Class.forName()</code>. */
	public static Class<?> getAccessor(String className, boolean throwException) throws Exception {
		try {
			Class<?> accessor = accessorCache.get(className);
			if (accessor != null) return accessor;
			accessor = Class.forName(className);
			if (accessor != null) {
				accessorCache.put(className, accessor);
				return accessor;
			}
		} catch (Exception e) {
			if (throwException) {
				throw e;
			}
		}
		return null;
	}
	/** Get the public field from a given class name. For static methods, use <code>null</code> for <code>thisArg</code>.
	* <br/>If exception is to be thrown, the types follow the exact same as <code>java.lang.Class.forName()</code> and <code>java.lang.Class.getField</code>. */
	public static <T> T getField(String className, String fieldName, boolean throwException, Object thisArg) throws Exception {
		try {
			Class<?> accessor = getAccessor(className, true);
			Field field = accessor.getField(fieldName);
			if (field != null) return unsafeCast(field.get(thisArg));
		} catch (Exception e) {
			if (throwException) throw e;
			return null;
		}
		return null;
	}
	/** Run the public method from a given class name. For static methods, use <code>null</code> for <code>thisArg</code>.
	* <br/>If exception is to be thrown, the types follow the exact same as <code>java.lang.Class.forName()</code> and <code>java.lang.Class.invoke</code>.
	* <br/>Only for methods with zero arguments. For other methods, use the accessor directly with a restricted unsafe cast. */
	public static <T> T invokeMethod(String className, String methodName, boolean throwException, Object thisArg) throws Exception {
		try {
			Class<?> accessor = getAccessor(className, true);
			Method method = accessor.getMethod(methodName);
			if (method != null) return unsafeCast(method.invoke(thisArg));
		} catch (Exception e) {
			if (throwException) throw e;
			return null;
		}
		return null;
	}
	/** <b>Unsafe operation</b>, use with caution. Cast the unknown reference-type value to a given type. You are responsible for confirming if the casted type is correct. */
	@SuppressWarnings("unchecked")
	public static <T> T unsafeCast(Object castVictim) {
		return (T) castVictim;
	}
}
