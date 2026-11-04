package org.luaj.vm2;

import org.luaj.vm2.lib.VarArgFunction;

/**
 * A class: the table {@code class.register} returns. Holds the class's fields and methods (its base's copied in),
 * answers the system fields ({@link LuaClassManager#CLASSNAME_KEY} and the others), and is the metatable of its
 * instances.
 */
public class ClassLuaTable extends LuaTable {

	/** The manager the class is registered with; it makes the class's instances. */
	protected LuaClassManager classManager;

	private String className;
	private LuaString classNameLua;
	private String shortName;
	private LuaString shortNameLua;
	private String baseClassName;
	private LuaString baseClassNameLua;

	private ClassLuaTable classTable;
	private ClassLuaTable baseClassTable;

	/** {@link LuaClassManager#NEW_FUNC_KEY}: one function per class, so {@code C.__new == C.__new}. */
	private final NewFunction newFunction = new NewFunction();

	/**
	 * Creates an empty class; {@link LuaClassManager#registerClass} fills and registers it.
	 *
	 * @param classManager   the manager registering it
	 * @param className      its name
	 * @param baseClassTable its base class, or {@code null} for none
	 */
	public ClassLuaTable(LuaClassManager classManager, String className, ClassLuaTable baseClassTable) {
		this.classManager = classManager;

		this.classTable = this;
		setClassName(className);

		this.baseClassTable = baseClassTable;
		setBaseClassName(baseClassTable == null ? "" : baseClassTable.getClassName());
	}

	/** The system fields come from the class itself, whatever the table holds under their names. */
	@Override
	protected LuaValue hashget(LuaValue key) {
		if (key.isstring()) {
			switch(key.tojstring()) {
				// Fields
				case LuaClassManager.CLASSNAME_KEY:
					return classNameLua;
				case LuaClassManager.CLASSNAME_SHORT_KEY:
					return shortNameLua;
				case LuaClassManager.CLASSNAME_BASE_KEY:
					return baseClassNameLua;
				case LuaClassManager.CLASSTABLE_KEY:
					return classTable;
				case LuaClassManager.CLASSTABLE_BASE_KEY:
					return baseClassTable == null ? NIL : baseClassTable; // nil, not Java null, for a root class
				// Methods
				case LuaClassManager.NEW_FUNC_KEY:
					return newFunction;

				// Default
				default:
					break;
			}
			
		}
		return super.hashget(key);
	}

	/** @return the class's name, as registered */
	public String getClassName() {
		return className;
	}

	void setClassName(String className) {
		this.className = className;
		this.classNameLua = LuaValue.valueOf(this.className);
		this.set(LuaClassManager.CLASSNAME_KEY, this.classNameLua);
		this.shortName = LuaClassManager.shortName(className);
		this.shortNameLua = LuaValue.valueOf(this.shortName);
		this.set(LuaClassManager.CLASSNAME_SHORT_KEY, this.shortNameLua);
	}

	/** @return the last part of the class's name, after its last dot; the whole name if it has none */
	public String getShortName() {
		return shortName;
	}

	/** @return the base class's name, {@code ""} for a class with no base */
	public String getBaseClassName() {
		return baseClassName;
	}

	void setBaseClassName(String baseClassName) {
		this.baseClassName = baseClassName;
		this.baseClassNameLua = LuaValue.valueOf(this.baseClassName);
		this.set(LuaClassManager.CLASSNAME_BASE_KEY, this.baseClassNameLua);
	}

	/**
	 * Makes a new instance of this class, running its constructor without arguments.
	 *
	 * @return the new instance
	 */
	public InstanceLuaTable createInstance() {
		return createInstance(NONE);
	}

	/**
	 * Makes a new instance of this class, its constructor given {@code args} ({@link LuaClassManager#newInstance}).
	 *
	 * @param args the constructor's arguments (after the instance)
	 * @return the new instance
	 */
	public InstanceLuaTable createInstance(Varargs args) {
		return classManager.newInstance(this, args);
	}

	/**
	 * The constructor this class's instances run: its own {@link LuaClassManager#CONSTRUCTOR_KEY}, or else its
	 * nearest ancestor's (constructors aren't copied into subclasses).
	 *
	 * @return the constructor, or nil if neither the class nor any ancestor has one
	 */
	public LuaValue getConstructor() {
		for (ClassLuaTable c = this; c != null; c = c.baseClassTable) {
			LuaValue constructor = c.rawget(LuaClassManager.CONSTRUCTOR_KEY);
			if (!constructor.isnil()) {
				return constructor;
			}
		}
		return NIL;
	}

	/** @return this class */
	public ClassLuaTable getClassTable() {
		return this.classTable;
	}

	/** @return the base class, or {@code null} for a class with no base */
	public ClassLuaTable getBaseClassTable() {
		return this.baseClassTable;
	}

	/** {@code Class.__new(...)}: a new instance of this class, the arguments given to its constructor. */
	private class NewFunction extends VarArgFunction {

		@Override
		public Varargs invoke(Varargs args) {
			return createInstance(args);
		}

	}

}
