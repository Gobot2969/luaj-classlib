package org.luaj.vm2;

public interface LuaClassManager {

	public ClassLuaTable createClassTable(String className, String baseClassName);
	public InstanceLuaTable createInstanceTable(ClassLuaTable classTable);

}
