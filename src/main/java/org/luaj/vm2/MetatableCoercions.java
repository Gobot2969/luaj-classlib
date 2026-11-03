package org.luaj.vm2;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;

import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.jse.CoerceJavaToLua;

/**
 * Gives the Java objects of a class a metatable when they are coerced to Lua, so they can have operators. They stay
 * LuaJ's usual reflective userdata (fields, methods); only the metatable is added.
 * <p>
 * Every route from Java to Lua goes through {@link CoerceJavaToLua#coerce(Object)}: values made by libraries and
 * global functions, Java methods' return values, field reads and array elements. coerce looks the object's exact class
 * up in LuaJ's coercion table, and this registers there, so every instance of a registered class gets the metatable
 * (subclasses don't: register each class).
 * <p>
 * The table and its Coercion interface are package-private in LuaJ, so this reaches them by reflection: it takes LuaJ's
 * own instance coercion and registers a proxy of the interface that calls it and attaches the metatable.
 */
public final class MetatableCoercions {

	private static final String COERCION_INTERFACE = "org.luaj.vm2.lib.jse.CoerceJavaToLua$Coercion";

	private MetatableCoercions() {
	}

	/** From now on, instances of exactly this class get the metatable when coerced to Lua. Call once, at startup. */
	@SuppressWarnings("unchecked")
	public static synchronized void register(Class<?> type, final LuaValue metatable) {
		try {
			Field table = CoerceJavaToLua.class.getDeclaredField("COERCIONS");
			table.setAccessible(true);
			Field instanceField = CoerceJavaToLua.class.getDeclaredField("instanceCoercion");
			instanceField.setAccessible(true);
			final Object instanceCoercion = instanceField.get(null);
			Class<?> coercionType = Class.forName(COERCION_INTERFACE, true, CoerceJavaToLua.class.getClassLoader());
			final Method coerce = coercionType.getDeclaredMethod("coerce", Object.class);
			coerce.setAccessible(true);
			Object coercion = Proxy.newProxyInstance(coercionType.getClassLoader(), new Class<?>[] { coercionType }, new InvocationHandler() {
				@Override
				public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
					if (method.getName().equals("coerce")) {
						LuaValue value;
						try {
							value = (LuaValue) coerce.invoke(instanceCoercion, args[0]);
						} catch (InvocationTargetException e) {
							throw e.getCause();
						}
						value.setmetatable(metatable);
						return value;
					}
					// Object's methods, on the proxy itself
					if (method.getName().equals("equals")) {
						return proxy == args[0];
					} else if (method.getName().equals("hashCode")) {
						return System.identityHashCode(proxy);
					}
					return "MetatableCoercion";
				}
			});
			((Map<Class<?>, Object>) table.get(null)).put(type, coercion);
		} catch (ReflectiveOperationException e) {
			throw new IllegalStateException("LuaJ's coercion table isn't where it was (" + e + ")", e);
		}
	}
}
