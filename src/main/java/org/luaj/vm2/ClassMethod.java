package org.luaj.vm2;

import java.util.ArrayList;

import org.luaj.vm2.lib.TwoArgFunction;
import org.luaj.vm2.lib.VarArgFunction;

/**
 * A class's method: wraps the function a class table was registered with, so that {@code super} knows whose base
 * to look in while it runs.
 * <p>
 * {@code self} is always the instance, so {@code self.__base} is always the instance's class's base: a method
 * that calls its base's method through it calls itself again once two classes in a row do it. {@code super} is
 * the base of the class the running method was registered with instead, so every level goes one class up:
 * <pre>{@code
 * speak = function(self) return "B>" .. super.speak(self) end
 * }</pre>
 * It works the same for interpreted functions and ones compiled with LuaJC: the function itself isn't changed.
 * The running methods are kept per Java thread, and LuaJ runs each coroutine on its own thread, so a method can
 * yield in the middle and resume.
 * <p>
 * {@code super} is only valid while a method runs: a closure made in a method and called after the method has
 * returned (a callback) gets an error, not a base.
 */
public final class ClassMethod extends VarArgFunction {

	/** The classes of the methods running on each thread, innermost last. */
	private static final ThreadLocal<ArrayList<ClassLuaTable>> RUNNING = new ThreadLocal<ArrayList<ClassLuaTable>>() {
		@Override
		protected ArrayList<ClassLuaTable> initialValue() {
			return new ArrayList<ClassLuaTable>();
		}
	};

	private final LuaValue function;
	private final ClassLuaTable owner;

	private ClassMethod(LuaValue function, ClassLuaTable owner) {
		this.function = function;
		this.owner = owner;
	}

	/**
	 * {@code value} as a method of {@code owner}: a function is wrapped (once: one that already is a method keeps
	 * its own class), anything else is returned as it is.
	 *
	 * @param value the value a class table was registered with
	 * @param owner the class it belongs to
	 * @return the method, or {@code value}
	 */
	public static LuaValue wrap(LuaValue value, ClassLuaTable owner) {
		return value.isfunction() && !(value instanceof ClassMethod) ? new ClassMethod(value, owner) : value;
	}

	/** The function the method runs. */
	public LuaValue getFunction() {
		return function;
	}

	/** The class the method belongs to. */
	public ClassLuaTable getOwner() {
		return owner;
	}

	/**
	 * The global {@code super}: {@code super.name} is {@code name} in the base of the running method's class (the
	 * nearest class above it that has one, since registering copies the base's keys).
	 *
	 * @return a new {@code super} table
	 */
	public static LuaTable superTable() {
		LuaTable table = new LuaTable();
		LuaTable meta = new LuaTable();
		meta.rawset(INDEX, new TwoArgFunction() {
			@Override
			public LuaValue call(LuaValue self, LuaValue key) {
				ArrayList<ClassLuaTable> running = RUNNING.get();
				if (running.isEmpty()) {
					return error("super: not inside a class method");
				}
				ClassLuaTable owner = running.get(running.size() - 1);
				LuaValue base = owner.get(LuaClassManager.CLASSTABLE_BASE_KEY);
				if (base.isnil()) {
					return error("super: class '" + owner.getClassName() + "' has no base class");
				}
				return base.get(key);
			}
		});
		table.setmetatable(meta);
		return table;
	}

	// Each call runs the function with the owner noted. The fixed-argument calls pass their arguments on as they
	// are (no Varargs); a tail call in the function still runs inside them (LuaJ evaluates it before returning).

	@Override
	public LuaValue call() {
		ArrayList<ClassLuaTable> running = enter();
		try {
			return function.call();
		} finally {
			running.remove(running.size() - 1);
		}
	}

	@Override
	public LuaValue call(LuaValue a) {
		ArrayList<ClassLuaTable> running = enter();
		try {
			return function.call(a);
		} finally {
			running.remove(running.size() - 1);
		}
	}

	@Override
	public LuaValue call(LuaValue a, LuaValue b) {
		ArrayList<ClassLuaTable> running = enter();
		try {
			return function.call(a, b);
		} finally {
			running.remove(running.size() - 1);
		}
	}

	@Override
	public LuaValue call(LuaValue a, LuaValue b, LuaValue c) {
		ArrayList<ClassLuaTable> running = enter();
		try {
			return function.call(a, b, c);
		} finally {
			running.remove(running.size() - 1);
		}
	}

	@Override
	public Varargs invoke(Varargs args) {
		ArrayList<ClassLuaTable> running = enter();
		try {
			return function.invoke(args).eval();
		} finally {
			running.remove(running.size() - 1);
		}
	}

	private ArrayList<ClassLuaTable> enter() {
		ArrayList<ClassLuaTable> running = RUNNING.get();
		running.add(owner);
		return running;
	}

	@Override
	public String tojstring() {
		return function.tojstring();
	}
}
