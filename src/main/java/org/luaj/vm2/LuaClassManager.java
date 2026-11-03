package org.luaj.vm2;

import java.util.HashMap;
import java.util.Map;

/**
 * Registers and looks up classes for one {@link org.luaj.vm2.lib.ClassLib}: makes class tables from the tables
 * scripts register, and instance tables of them. Subclass it to make class or instance tables of your own
 * ({@link #createClassTable}, {@link #createInstanceTable}).
 */
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

	/**
	 * Makes the table of a new class; {@link #registerClass} fills it. Override it to use a subclass of
	 * {@link ClassLuaTable}.
	 *
	 * @param className      the class's name, already checked and not registered yet
	 * @param baseClassTable its base class, or {@code null} for none
	 * @return the new class table
	 */
	protected ClassLuaTable createClassTable(String className, ClassLuaTable baseClassTable) {
		ClassLuaTable classLuaTable = new ClassLuaTable(this, className, baseClassTable);
		return classLuaTable;
	}

	/**
	 * Makes {@code table} a class and registers it.
	 * <p>
	 * {@code table} must have a {@link #CLASSNAME_KEY} string, a valid name ({@link #checkName}) not registered yet.
	 * Its base is {@code baseClassTable}, or else the class its {@link #CLASSNAME_BASE_KEY} string names, which must
	 * be registered already; with neither, the class has no base. When both are given they must be the same class.
	 * <p>
	 * The new class gets a copy of its base's fields and methods, then {@code table}'s: its functions become this
	 * class's methods ({@link ClassMethod}). {@code table} itself is only read.
	 *
	 * @param table          the class's name, base name and members
	 * @param baseClassTable its base class, or {@code null} to take it from {@code table} (or have none)
	 * @return the new class table
	 * @throws LuaError if the name is missing, not a string, invalid or already registered, or the base named in
	 *                  {@code table} isn't registered or isn't {@code baseClassTable}
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

		// The base as the table names it, if it does: it must be registered, and be the base given (if any)
		LuaValue baseName = table.get(CLASSNAME_BASE_KEY);
		if (!baseName.isnil()) {
			if (!baseName.isstring() || baseName.isnumber()) {
				throw new LuaError("class " + CLASSNAME_BASE_KEY + " must be a string, got " + baseName.typename());
			}
			ClassLuaTable named = getClassTable(baseName.tojstring());
			if (named == null) {
				throw new LuaError("class '" + className + "': base class '" + baseName.tojstring() + "' isn't registered");
			}
			if (baseClassTable != null && baseClassTable != named) {
				throw new LuaError("class '" + className + "': " + CLASSNAME_BASE_KEY + " '" + baseName.tojstring()
						+ "' isn't the base class given ('" + baseClassTable.getClassName() + "')");
			}
			baseClassTable = named;
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
		
		classTable.set("__index", classTable); // Instances (whose metatable is this class) find its methods and fields through it
	
		return classTable;
	}

	/**
	 * Makes the table of a new instance of a class; {@link ClassLuaTable#createInstance} gives it its metatable.
	 * Override it to use a subclass of {@link InstanceLuaTable}.
	 *
	 * @param classTable the instance's class
	 * @return the new instance table
	 */
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
