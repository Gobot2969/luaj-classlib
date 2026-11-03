package org.luaj.vm2;

import java.util.HashMap;
import java.util.Map;

public class LuaClassManager {

	// The system's fields on class tables (and through __index, on instances). Double-underscore lowercase, as
	// Lua names its metatable fields: a class table is its instances' metatable. __name is also the key Lua 5.3+
	// names a type by.
	/** The class's name (lowercase). */
	public static final String CLASSNAME_KEY = "__name";
	/** The base class's name, "" for a class with no base. */
	public static final String CLASSNAME_BASE_KEY = "__basename";
	/** The class table itself. */
	public static final String CLASSTABLE_KEY = "__class";
	/** The base class table, nil for a class with no base. */
	public static final String CLASSTABLE_BASE_KEY = "__base";

	/**
	 * A map of all loaded class tables: {@code <String className, LuaTable classTable>}
	 */
	private Map<String, ClassLuaTable> classes = new HashMap<String, ClassLuaTable>();

	protected ClassLuaTable createClassTable(String className, ClassLuaTable baseClassTable) { // TODO check maybe if exist?
		ClassLuaTable classLuaTable = new ClassLuaTable(this, className, baseClassTable);
		return classLuaTable;
	}

	/** Makes {@code table} a class
	 *  // TODO must have string CLASSNAME_KEY
	 * // must optionally have CLASSNAME_BASE_KEY
	 * // must return some lua errors or exceptions from some proper extension of LuaJ's
	 * // if stuff is not found or wrong type etc
	 */
	public LuaTable registerClass(LuaTable table, ClassLuaTable baseClassTable) {
		// Register class portion
		String className = table.get(CLASSNAME_KEY).tojstring(); // TODO Make sure non exists

		ClassLuaTable classTable = createClassTable(className, baseClassTable);
	 	classes.put(className, classTable); // Save class table to classman

		// Copy baseClassTable keys
		if (baseClassTable != null) {
			for (LuaValue key : baseClassTable.keys()) {
				if (key.isstring()) {
					String k = key.tojstring();
					if (k.equals(CLASSNAME_KEY) || k.equals(CLASSNAME_BASE_KEY)) continue;
				}
				classTable.rawset(key, baseClassTable.rawget(key));
			}
		}

		// Copy table keys; its functions become this class's methods (see ClassMethod: super)
		for (LuaValue key : table.keys()) {
			if (key.isstring()) {
				String k = key.tojstring();
				if (k.equals(CLASSNAME_KEY) || k.equals(CLASSNAME_BASE_KEY)) continue;
			}
			classTable.rawset(key, ClassMethod.wrap(table.rawget(key), classTable));
		}
		
		classTable.set("__index", classTable); // TODO check if this even does anything
	
		return classTable;
	}

	public InstanceLuaTable createInstanceTable(ClassLuaTable classTable) {
		return new InstanceLuaTable(classTable);
	}

	public ClassLuaTable getClassTable(String className) {
		return this.classes.get(className);
	}

}
