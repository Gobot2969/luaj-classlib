package org.luaj.vm2;

import org.luaj.vm2.LuaString;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;

public class ClassLuaTable extends LuaTable {
	
	protected LuaClassManager classManager;
	
	private String className;
	private LuaString classNameLua;
	private String baseClassName;
	private LuaString baseClassNameLua;
	private String classScript;
	private LuaString classScriptLua;
	private String classmetaScript;
	private LuaString classmetaScriptLua;
	
	private LuaTable classmetaLua;
	
	protected ClassLuaTable(LuaClassManager classManager, String className) {
		this.classManager = classManager;
		setClassName(className);
	}
	
	
	protected void onRegistered() {}
	
	protected void onLoadPre() {}
	
	protected void onLoadPost() {}
	
	@Override
	protected LuaValue hashget(LuaValue key) {
		if (key.isstring()) {
			switch(key.tojstring()) {
				case LuaClassManager.CLASSNAME_VAR:
					return classNameLua;
				case LuaClassManager.CLASSNAME_BASE_VAR:
					return baseClassNameLua;
				case LuaClassManager.CLASS_SCRIPT_VAR:
					return classScriptLua;
				case LuaClassManager.CLASSMETA_SCRIPT_VAR:
					return classmetaScriptLua;
				case LuaClassManager.CLASSMETA_VAR:
					return classmetaLua;
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
		this.set(LuaClassManager.CLASSNAME_VAR, this.classNameLua);
	}

	public String getBaseClassName() {
		return baseClassName;
	}

	void setBaseClassName(String baseClassName) {
		this.baseClassName = baseClassName.toLowerCase();
		this.baseClassNameLua = LuaValue.valueOf(this.baseClassName);
		this.set(LuaClassManager.CLASSNAME_BASE_VAR, this.baseClassNameLua);
	}
	
	
	
	
	public String getClassScript() {
		return classScript;
	}
	
	void setClassScript(String classScript) {
		this.classScript = classScript;
		this.classScriptLua = LuaValue.valueOf(this.classScript);
		this.set(LuaClassManager.CLASS_SCRIPT_VAR, this.classScriptLua);
	}
	
	
	public String getClassmetaScript() {
		return classmetaScript;
	}
	
	void setClassmetaScript(String classmetaScript) {
		this.classmetaScript = classmetaScript;
		this.classmetaScriptLua = LuaValue.valueOf(this.classmetaScript);
		this.set(LuaClassManager.CLASSMETA_SCRIPT_VAR, this.classmetaScriptLua);
	}
	
	
	public LuaTable getClassmeta() {
		return classmetaLua;
	}
	
	void setClassmeta(LuaTable classmeta) {
		this.classmetaLua = new ReadOnlyLuaTable(classmeta); // TODO should it be copied into readonly like here or let outside world do it...? 
	}
	
	public InstanceLuaTable createInstance() {
		InstanceLuaTable instanceTable = classManager.createInstanceTable(this);
		instanceTable.setmetatable(this);
		return instanceTable;
	}

}
