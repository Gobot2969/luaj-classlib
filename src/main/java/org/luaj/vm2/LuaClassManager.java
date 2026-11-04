package org.luaj.vm2;

import java.util.HashMap;
import java.util.Map;

import org.luaj.vm2.lib.VarArgFunction;

/**
 * Registers and looks up classes for one {@link org.luaj.vm2.lib.ClassLib}: makes class tables from the tables
 * scripts register, and instance tables of them. Subclass it to make class or instance tables of your own
 * ({@link #createClassTable}, {@link #createInstanceTable}).
 */
public class LuaClassManager {

	// The system's fields on class tables (and through __index, on instances). Double-underscore lowercase, as
	// Lua names its metatable fields: a class table is its instances' metatable.
	/** The class's name, as registered (case-sensitive), e.g. {@code "weapon.pistol"}. */
	public static final String CLASSNAME_KEY = "__classname";
	/** The last part of the class's name, after its last dot ({@code "pistol"}); the whole name if it has none. */
	public static final String CLASSNAME_SHORT_KEY = "__shortname";
	/** The base class's {@link #CLASSNAME_KEY}, "" for a class with no base. */
	public static final String CLASSNAME_BASE_KEY = "__basename";
	/** The class table itself. */
	public static final String CLASSTABLE_KEY = "__class";
	/** The base class table, nil for a class with no base. */
	public static final String CLASSTABLE_BASE_KEY = "__base";
	/**
	 * The class's constructor: a method {@code new} calls on each new instance, with {@code new}'s other arguments
	 * ({@code __constr = function(self, ...) end}). Copied into subclasses like any other method: a class without its
	 * own has its nearest ancestor's, and a root class without one gets {@link #DEFAULT_CONSTRUCTOR}; so every class
	 * has one, and {@code super.__constr} can always be called from a class with a base.
	 */
	public static final String CONSTRUCTOR_KEY = "__constr";
	/**
	 * The constructor a root class without its own gets (and its subclasses without theirs, by copy): it does
	 * nothing. One function shared by all of them.
	 */
	public static final LuaValue DEFAULT_CONSTRUCTOR = new VarArgFunction() {
		@Override
		public Varargs invoke(Varargs args) {
			return NONE;
		}

		@Override
		public String tojstring() {
			return "default constructor";
		}
	};
	/** {@code Class.__new(...)}: a new instance of the class, its constructor given the arguments (as {@code new(Class, ...)}). */
	public static final String NEW_FUNC_KEY = "__new";

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
	 * The short name of a class name: its last part, after its last dot ({@code "weapon.pistol"} gives
	 * {@code "pistol"}), or the whole name if it has none. {@link #checkName} guarantees there is a last part.
	 *
	 * @param className a valid class name
	 * @return its short name
	 */
	public static String shortName(String className) {
		return className.substring(className.lastIndexOf('.') + 1);
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

		// A constructor must be a function
		LuaValue constructor = table.rawget(CONSTRUCTOR_KEY);
		if (!constructor.isnil() && !constructor.isfunction()) {
			throw new LuaError("class '" + className + "': " + CONSTRUCTOR_KEY + " must be a function, got " + constructor.typename());
		}

		// Copy baseClassTable keys (its methods, constructor included), but __new: the class answers that itself
		if (baseClassTable != null) {
			for (LuaValue key : baseClassTable.keys()) {
				if (key.isstring()) {
					String k = key.tojstring();
					if (k.equals(CLASSNAME_KEY) || k.equals(CLASSNAME_SHORT_KEY) || k.equals(CLASSNAME_BASE_KEY)) continue;
					if (k.equals(NEW_FUNC_KEY)) continue;
				}
				classTable.rawset(key, baseClassTable.rawget(key));
			}
		}

		// Copy table keys; its functions become this class's methods (see ClassMethod: super), its constructor too
		for (LuaValue key : table.keys()) {
			if (key.isstring()) {
				String k = key.tojstring();
				if (k.equals(CLASSNAME_KEY) || k.equals(CLASSNAME_SHORT_KEY) || k.equals(CLASSNAME_BASE_KEY)) continue;
				if (k.equals(NEW_FUNC_KEY)) continue; // the class answers it itself
			}
			classTable.rawset(key, ClassMethod.wrap(table.rawget(key), classTable));
		}

		// No constructor of its own or from its base: only a root class without one. It gets the default (which its
		// subclasses copy), so every class has a constructor
		if (classTable.rawget(CONSTRUCTOR_KEY).isnil()) {
			classTable.rawset(CONSTRUCTOR_KEY, DEFAULT_CONSTRUCTOR);
		}
		
		classTable.set("__index", classTable); // Instances (whose metatable is this class) find its methods and fields through it
	
		return classTable;
	}

	/**
	 * Makes a new instance of a class: its table ({@link #createInstanceTable}), with the class as its metatable, on
	 * which the class's constructor ({@link ClassLuaTable#getConstructor}), if it has one, then runs with
	 * {@code args}.
	 *
	 * @param classTable the instance's class
	 * @param args       the constructor's arguments (after the instance)
	 * @return the new instance
	 */
	public InstanceLuaTable newInstance(ClassLuaTable classTable, Varargs args) {
		InstanceLuaTable instanceTable = createInstanceTable(classTable);
		instanceTable.setmetatable(classTable);
		LuaValue constructor = classTable.getConstructor();
		if (!constructor.isnil()) {
			constructor.invoke(LuaValue.varargsOf(instanceTable, args));
		}
		return instanceTable;
	}

	/**
	 * Creates the table of a new instance of a class, nothing more: {@link #newInstance} gives it its metatable and
	 * runs the constructor. Override it to use a subclass of {@link InstanceLuaTable}.
	 *
	 * @param classTable the instance's class
	 * @return the new instance table
	 */
	protected InstanceLuaTable createInstanceTable(ClassLuaTable classTable) {
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
