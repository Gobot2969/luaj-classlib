package org.luaj.vm2.lib;

import org.luaj.vm2.ClassLuaTable;
import org.luaj.vm2.ClassMethod;
import org.luaj.vm2.InstanceLuaTable;
import org.luaj.vm2.LuaClassManager;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;

/**
 * Subclass of {@link LibFunction} which implements the {@code class} library.
 */
public class ClassLib extends TwoArgFunction {
	/** The name of the library's table: the global {@code class}. */
	private static final String PACKAGE_NAME = "class";

	/** Registers and looks up this library's classes. */
	private LuaClassManager classManager;

	/** Creates the library; {@link org.luaj.vm2.Globals#load(LuaValue)} installs it. */
	public ClassLib() {
		this.classManager = new LuaClassManager();
	}

	/**
	 * Creates the library with a class manager of your own (to make class or instance tables of your own).
	 *
	 * @param classManager registers and looks up the library's classes
	 */
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
		env.set("isclass", new IsClassFunction());
		env.set("isobj", new IsObjFunction());
		env.set("new", new NewFunction());
		env.set("super", ClassMethod.superTable());
		if (!env.get("package").isnil()) env.get("package").get("loaded").set(PACKAGE_NAME, lib);
		return lib;
	}

	/** {@code class.get(name)}: the class registered under exactly that name, or nil. */
	final class GetFunction extends OneArgFunction {
		@Override
		public LuaValue call(LuaValue className) {
			ClassLuaTable classTable = classManager.getClassTable(className.checkjstring());
			return classTable != null ? classTable : NIL;
		}
	}

	/**
	 * {@code class.register(class [, base])}: Lua's side of {@link LuaClassManager#registerClass(LuaValue, LuaValue)}.
	 * {@code class} is the class's table or just its name (an empty class); {@code base} a class, a class name or nil.
	 */
	final class RegisterFunction extends VarArgFunction {
		@Override
		public Varargs invoke(Varargs args) {
			LuaValue classDef = args.arg1();
			if (!classDef.istable() && (!classDef.isstring() || classDef.isnumber())) {
				return argerror(1, "table or class name expected, got " + classDef.typename());
			}
			LuaValue base = args.arg(2);
			if (!base.isnil() && !(base instanceof ClassLuaTable) && (!base.isstring() || base.isnumber())) {
				return argerror(2, "class or class name expected, got " + base.typename());
			}
			if (args.narg() > 2) {
				return argerror(3, "class.register takes a class and, optionally, its base");
			}
			return classManager.registerClass(classDef, base);
		}
	}

	/**
	 * {@code new(class, ...)} or {@code new(name, ...)}: a new instance of the class, or of the class registered by
	 * that name; the other arguments go to its constructor.
	 */
	final class NewFunction extends VarArgFunction {
		@Override
		public Varargs invoke(Varargs args) {// Returns an InstanceLuaTable
			LuaValue table = args.arg1();
			if (table.isstring() && !table.isnumber()) {
				ClassLuaTable named = classManager.getClassTable(table.tojstring());
				if (named == null) {
					return argerror(1, "no class registered as '" + table.tojstring() + "'");
				}
				return named.createInstance(args.subargs(2));
			}
			if (!(table instanceof ClassLuaTable)) {
				return argerror(1, "class or class name expected, got " + table.typename());
			}
			return ((ClassLuaTable) table).createInstance(args.subargs(2));
		}
	}

	/** {@code isclass(value)}: whether the value is a class (any value; false for anything else). */
	final class IsClassFunction extends OneArgFunction {
		@Override
		public LuaValue call(LuaValue arg) {// Returns a LuaBoolean
			return valueOf(arg instanceof ClassLuaTable);
		}
	}

	/** {@code isobj(value)}: whether the value is an instance of a class (any value; false for anything else). */
	final class IsObjFunction extends OneArgFunction {
		@Override
		public LuaValue call(LuaValue arg) {// Returns a LuaBoolean
			return valueOf(arg instanceof InstanceLuaTable);
		}
	}
}
