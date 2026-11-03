package org.luaj.vm2.lib;

import org.luaj.vm2.ClassLuaTable;
import org.luaj.vm2.ClassMethod;
import org.luaj.vm2.LuaClassManager;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;

/**
 * Subclass of {@link LibFunction} which implements the {@code class} library.
 */
public class ClassLib extends TwoArgFunction {
	/** The name of the library's table: the global {@code class}. */
	private static final String PACKAGE_NAME = "class";

	/**  */
	private LuaClassManager classManager;

	/** Creates the library; {@link org.luaj.vm2.Globals#load(LuaValue)} installs it. */
	public ClassLib() {
		this.classManager = new LuaClassManager();
	}

	/** */
	public ClassLib(LuaClassManager classManager) {
		this.classManager = classManager;
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
		lib.set("get", new GetFunction());
		lib.set("register", new RegisterFunction());
		env.set(PACKAGE_NAME, lib);
		env.set("new", new NewFunction());
		env.set("super", ClassMethod.superTable());
		if (!env.get("package").isnil()) env.get("package").get("loaded").set(PACKAGE_NAME, lib);
		return lib;
	}

	final class GetFunction extends OneArgFunction {
		@Override
		public LuaValue call(LuaValue className) {
			ClassLuaTable classTable = classManager.getClassTable(className.checkjstring());
			return classTable != null ? classTable : NIL;
		}
	}

	/** {@code class.register(table [, superclass])}: Lua's side of {@link ClassLib#register(LuaTable, LuaTable)}. */
	final class RegisterFunction extends TwoArgFunction {
		@Override
		public LuaValue call(LuaValue table, LuaValue baseClassTable) {
			if (!baseClassTable.isnil() && !(baseClassTable instanceof ClassLuaTable)) {
				return argerror(2, "class expected, got " + baseClassTable.typename());
			}
			return classManager.registerClass(table.checktable(), baseClassTable.isnil() ? null : (ClassLuaTable) baseClassTable);
		}
	}

	final class NewFunction extends OneArgFunction {
		@Override
		public LuaValue call(LuaValue table) {// Returns an InstanceLuaTable
			if (!(table instanceof ClassLuaTable)) {
				return argerror(1, "class expected, got " + table.typename());
			}
			return ((ClassLuaTable) table).createInstance(); // TODO also take a class name (a string) and look it up
		}
	}
}
