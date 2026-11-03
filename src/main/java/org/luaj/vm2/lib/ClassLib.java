package org.luaj.vm2.lib;

import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;

/**
 * Subclass of {@link LibFunction} which implements the {@code class} library.
 * <p>
 * A script defines a class as a plain table, then binds it at the end:
 * <pre>{@code
 * local ENT = {}
 * function ENT:Think() ... end
 * return class.bind(ENT)             -- or class.bind(ENT, BaseEnt) to extend BaseEnt
 * }</pre>
 * Java does the same with {@link #bind(LuaTable, LuaTable)}.
 * <p>
 * Loaded like the other libraries:
 * <pre>{@code
 * Globals globals = JsePlatform.standardGlobals();
 * globals.load(new ClassLib());
 * }</pre>
 * which sets the global {@code class} and {@code package.loaded.class}.
 * <p>
 * A stub for now: {@code bind} returns the table it's given and does nothing else.
 */
public class ClassLib extends TwoArgFunction {

	/** The name of the library's table: the global {@code class}. */
	public static final String NAME = "class";

	/** Creates the library; {@link org.luaj.vm2.Globals#load(LuaValue)} installs it. */
	public ClassLib() {
	}

	/**
	 * Installs the library into {@code env}.
	 *
	 * @param modname the module name ({@code require}'s argument), unused
	 * @param env     the environment, normally the {@link org.luaj.vm2.Globals}
	 * @return the library's table
	 */
	@Override
	public LuaValue call(LuaValue modname, LuaValue env) {
		LuaTable lib = new LuaTable();
		lib.set("bind", new bind());
		env.set(NAME, lib);
		if (!env.get("package").isnil()) env.get("package").get("loaded").set(NAME, lib);
		return lib;
	}

	/**
	 * Makes {@code table} a class, extending {@code superclass} when one is given.
	 * <p>
	 * Stub: returns {@code table} unchanged.
	 *
	 * @param table      the class's table (its methods and fields)
	 * @param superclass the class it extends, or {@code null} for none
	 * @return the class
	 */
	public static LuaTable bind(LuaTable table, LuaTable superclass) {
		// TODO bind: the class system (LuaClassManager, ClassLuaTable, InstanceLuaTable) goes here.
		return table;
	}

	/** Makes {@code table} a class with no superclass. Stub: returns {@code table}. */
	public static LuaTable bind(LuaTable table) {
		return bind(table, null);
	}

	/** {@code class.bind(table [, superclass])}: Lua's side of {@link ClassLib#bind(LuaTable, LuaTable)}. */
	static final class bind extends TwoArgFunction {
		@Override
		public LuaValue call(LuaValue table, LuaValue superclass) {
			return ClassLib.bind(table.checktable(), superclass.isnil() ? null : superclass.checktable());
		}
	}
}
