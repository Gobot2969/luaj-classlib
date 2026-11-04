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
	 * This class's own default constructor, which does nothing: what {@link LuaClassManager#CONSTRUCTOR_KEY} answers
	 * when the class has no constructor of its own or copied from its base. Made once, one per class.
	 */
	private final LuaValue defaultConstructor = new DefaultConstructor();

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
				case LuaClassManager.CONSTRUCTOR_KEY: {
					LuaValue constructor = super.hashget(key);
					return constructor.isnil() ? defaultConstructor : constructor;
				}

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
	 * The constructor this class's instances run: its {@link LuaClassManager#CONSTRUCTOR_KEY}, its own or the one
	 * copied from its base when it was registered, or else the class's own default, which does nothing (one table
	 * read, no lookup up the hierarchy).
	 *
	 * @return the constructor, never nil
	 */
	public LuaValue getConstructor() {
		return rawget(LuaClassManager.CONSTRUCTOR_KEY);
	}

	/** @return this class */
	public ClassLuaTable getClassTable() {
		return this.classTable;
	}

	/** @return the base class, or {@code null} for a class with no base */
	public ClassLuaTable getBaseClassTable() {
		return this.baseClassTable;
	}

	/** A constructor that does nothing: each class's own default ({@link #getConstructor}). */
	private static final class DefaultConstructor extends VarArgFunction {

		@Override
		public Varargs invoke(Varargs args) {
			return NONE;
		}

	}

	/** {@code Class.__new(...)}: a new instance of this class, the arguments given to its constructor. */
	private class NewFunction extends VarArgFunction {

		@Override
		public Varargs invoke(Varargs args) {
			return createInstance(args);
		}

	}

}
