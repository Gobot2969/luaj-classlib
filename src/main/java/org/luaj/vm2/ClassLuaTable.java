package org.luaj.vm2;

import org.luaj.vm2.lib.ClassLib;

public class ClassLuaTable extends LuaTable {

	protected ClassTableFactory classTableFactory;

	private String className;
	private LuaString classNameLua;
	private String baseClassName;
	private LuaString baseClassNameLua;

	private ClassLuaTable classTable;
	private ClassLuaTable baseClassTable;

	public ClassLuaTable(ClassTableFactory classTableFactory, String className, ClassLuaTable baseClassTable) {
		this.classTableFactory = classTableFactory;
		setClassName(className);
		setBaseClassName(baseClassTable.getClassName());

		this.classTable = this;
		this.baseClassTable = baseClassTable;
	}

	@Override
	protected LuaValue hashget(LuaValue key) {
		if (key.isstring()) {
			switch(key.tojstring()) {
				case ClassLib.CLASSNAME_KEY:
					return classNameLua;
				case ClassLib.CLASSNAME_BASE_KEY:
					return baseClassNameLua;
				case ClassLib.CLASSTABLE_KEY:
					return classTable;
				case ClassLib.CLASSTABLE_BASE_KEY:
					return baseClassTable;
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
		this.className = className.toLowerCase();
		this.classNameLua = LuaValue.valueOf(this.className);
		this.set(ClassLib.CLASSNAME_KEY, this.classNameLua);
	}

	public String getBaseClassName() {
		return baseClassName;
	}

	void setBaseClassName(String baseClassName) {
		this.baseClassName = baseClassName.toLowerCase();
		this.baseClassNameLua = LuaValue.valueOf(this.baseClassName);
		this.set(ClassLib.CLASSNAME_BASE_KEY, this.baseClassNameLua);
	}

	public InstanceLuaTable createInstance() {
		InstanceLuaTable instanceTable = classTableFactory.createInstanceTable(this);
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
