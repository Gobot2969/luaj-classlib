package org.luaj.vm2.lib;

import java.util.HashMap;
import java.util.Map;

import org.luaj.vm2.ClassLuaTable;
import org.luaj.vm2.InstanceLuaTable;
import org.luaj.vm2.ClassTableFactory;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;

/**
 * Subclass of {@link LibFunction} which implements the {@code class} library.
 */
public class ClassLib extends TwoArgFunction {

	public static final String CLASSNAME_KEY = "CLASSNAME";
	public static final String CLASSNAME_BASE_KEY = "CLASSNAME_BASE";
	public static final String CLASSTABLE_KEY = "CLASS";
	public static final String CLASSTABLE_BASE_KEY = "CLASS_BASE";


	/** The name of the library's table: the global {@code class}. */
	private static final String PACKAGE_NAME = "class";

	/**
	 * A map of all loaded class tables: {@code <String className, LuaTable classTable>}
	 */
	private Map<String, ClassLuaTable> classes = new HashMap<String, ClassLuaTable>();

	private ClassTableFactory classManager;

	/** Creates the library; {@link org.luaj.vm2.Globals#load(LuaValue)} installs it. */
	public ClassLib() {
		this.classManager = new DefaultClassTableFactory();
	}

	public ClassLib(ClassTableFactory classManager) {
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
		lib.set("register", new RegisterFunction());
		env.set(PACKAGE_NAME, lib);
		env.set("new", new NewFunction());
		if (!env.get("package").isnil()) env.get("package").get("loaded").set(PACKAGE_NAME, lib);
		return lib;
	}

	private void updateInheritance(String className) {

		ClassLuaTable classTable = this.classes.get(className);
		ClassLuaTable baseClassTable;

		baseClassTable = classes.get(classTable.getBaseClassName());
		if (baseClassTable == null) {
			return;
		}

		// Set the class table's metatable to base class.
		//classTable.setmetatable(baseClassTable); // This is oldmethod. Now we use newmethod which just copies values. No meta but instance to class relations.

		
	}

	/** Makes {@code table} a class
	 *  // TODO must have string CLASSNAME_KEY
	 * // must optionally have CLASSNAME_BASE_KEY or CLASSNAME_BASE
	 * // must return some lua errors or exceptions from some proper extension of LuaJ's
	 * // if stuff is not found or wrong type etc
	 */
	public LuaTable registerClass(LuaTable table, LuaTable baseClassTable) {
		// Register class portion
		String className = table.get(CLASSNAME_KEY).tojstring(); // TODO Make sure non exists
		String baseClassName = baseClassTable.get(CLASSNAME_KEY).tojstring(); // TODO Could fail

		ClassLuaTable classTable = classManager.createClassTable(className, baseClassName);
	 	classes.put(className, classTable); // Save class table to classman

		// Copy baseClassTable keys
		for (LuaValue key : baseClassTable.keys()) {
			if (key.isstring()) {
				String k = key.tojstring();
				if (k.equals(CLASSNAME_KEY) || k.equals(CLASSNAME_BASE_KEY)) continue;
			}
			classTable.rawset(key, baseClassTable.rawget(key));
		}

		// Copy table keys
		for (LuaValue key : table.keys()) {
			if (key.isstring()) {
				String k = key.tojstring();
				if (k.equals(CLASSNAME_KEY) || k.equals(CLASSNAME_BASE_KEY)) continue;
			}
			classTable.rawset(key, table.rawget(key));
		}
		
		classTable.set("__index", classTable); // TODO check if this even does anything
	
		return classTable;
	}

	/** {@code class.register(table [, superclass])}: Lua's side of {@link ClassLib#register(LuaTable, LuaTable)}. */
	final class RegisterFunction extends TwoArgFunction {
		@Override
		public LuaValue call(LuaValue table, LuaValue baseClassTable) {
			return registerClass(table.checktable(), baseClassTable.isnil() ? null : baseClassTable.checktable());
		}
	}

	final class NewFunction extends OneArgFunction {
		@Override
		public LuaValue call(LuaValue table) {// Returns an InstanceLuaTable
			return ((ClassLuaTable)table).createInstance(); // TODO HERE CHECK if it's either string or the ClassLuaTable itself and return its new InstanceLuaTable. 
		}
	}

	final class DefaultClassTableFactory implements ClassTableFactory {

		@Override
		public ClassLuaTable createClassTable(String className, String baseClassName) { // TODO check maybe if exist?
			ClassLuaTable classLuaTable = new ClassLuaTable(this, className, baseClassName);
			return classLuaTable;
		}

		@Override
		public InstanceLuaTable createInstanceTable(ClassLuaTable classTable) {
			return new InstanceLuaTable(classTable);
		}
	}
}
