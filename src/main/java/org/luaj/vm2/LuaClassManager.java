package org.luaj.vm2;

import java.util.HashMap;
import java.util.Map;

public class LuaClassManager {

	// The system's fields on class tables (and through __index, on instances). Double-underscore lowercase, as
	// Lua names its metatable fields: a class table is its instances' metatable.
	/** The class's name, as registered (case-sensitive). */
	public static final String CLASSNAME_KEY = "__classname";
	/** The base class's name, "" for a class with no base. */
	public static final String CLASSNAME_BASE_KEY = "__classname_base";
	/** The class table itself. */
	public static final String CLASSTABLE_KEY = "__class";
	/** The base class table, nil for a class with no base. */
	public static final String CLASSTABLE_BASE_KEY = "__class_base";

	/**
	 * A map of all loaded class tables: {@code <String className, ClassLuaTable classTable>}, by exact name
	 */
	private Map<String, ClassLuaTable> classes = new HashMap<String, ClassLuaTable>();

	/**
	 * What's wrong with a class name, if anything. Names are case-sensitive ("Dog" and "dog" are two classes) and
	 * use {@code .} as a delimiter ("weapon.pistol"), so a name can't be empty, start or end with a dot, or have
	 * two dots in a row.
	 *
	 * @param className a class name
	 * @return why it isn't a valid class name, or {@code null} if it is
	 */
	public static String checkName(String className) {
		if (className.isEmpty()) {
			return "must not be empty";
		}
		if (className.startsWith(".")) {
			return "must not start with '.'";
		}
		if (className.endsWith(".")) {
			return "must not end with '.'";
		}
		if (className.contains("..")) {
			return "must not have two '.' in a row";
		}
		return null;
	}

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
		LuaValue name = table.get(CLASSNAME_KEY);
		if (!name.isstring() || name.isnumber()) {
			throw new LuaError("class " + CLASSNAME_KEY + " must be a string, got " + name.typename());
		}
		String className = name.tojstring();
		String invalid = checkName(className);
		if (invalid != null) {
			throw new LuaError("class " + CLASSNAME_KEY + " '" + className + "' " + invalid);
		}
		if (classes.containsKey(className)) {
			throw new LuaError("class '" + className + "' is already registered");
		}

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

	/**
	 * The registered class of exactly that name (case-sensitive).
	 *
	 * @param className the class's name
	 * @return the class, or {@code null} if none is registered by that name
	 */
	public ClassLuaTable getClassTable(String className) {
		return this.classes.get(className);
	}

}
