package org.luaj.vm2;

public class ClassLuaTable extends LuaTable {

	protected LuaClassManager classManager;

	private String className;
	private LuaString classNameLua;
	private String baseClassName;
	private LuaString baseClassNameLua;

	private ClassLuaTable classTable;
	private ClassLuaTable baseClassTable;

	public ClassLuaTable(LuaClassManager classManager, String className, ClassLuaTable baseClassTable) {
		this.classManager = classManager;

		this.classTable = this;
		setClassName(className);

		this.baseClassTable = baseClassTable;
		setBaseClassName(baseClassTable == null ? "" : baseClassTable.getClassName());
	}

	@Override
	protected LuaValue hashget(LuaValue key) {
		if (key.isstring()) {
			switch(key.tojstring()) {
				case LuaClassManager.CLASSNAME_KEY:
					return classNameLua;
				case LuaClassManager.CLASSNAME_BASE_KEY:
					return baseClassNameLua;
				case LuaClassManager.CLASSTABLE_KEY:
					return classTable;
				case LuaClassManager.CLASSTABLE_BASE_KEY:
					return baseClassTable == null ? NIL : baseClassTable; // nil, not Java null, for a root class
				default:
					break;
			}
			
		}
		return super.hashget(key);
	}

	public String getClassName() {
		return className;
	}

	void setClassName(String className) {
		this.className = LuaClassManager.normalizeName(className);
		this.classNameLua = LuaValue.valueOf(this.className);
		this.set(LuaClassManager.CLASSNAME_KEY, this.classNameLua);
	}

	public String getBaseClassName() {
		return baseClassName;
	}

	void setBaseClassName(String baseClassName) {
		this.baseClassName = LuaClassManager.normalizeName(baseClassName);
		this.baseClassNameLua = LuaValue.valueOf(this.baseClassName);
		this.set(LuaClassManager.CLASSNAME_BASE_KEY, this.baseClassNameLua);
	}

	public InstanceLuaTable createInstance() {
		InstanceLuaTable instanceTable = classManager.createInstanceTable(this);
		instanceTable.setmetatable(this);
		return instanceTable;
	}

	public ClassLuaTable getClassTable() {
		return this.classTable;
	}

	public ClassLuaTable getBaseClassTable() {
		return this.baseClassTable;
	}

}
