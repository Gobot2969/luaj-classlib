package org.luaj.vm2;

public interface ClassTableFactory {

	public ClassLuaTable createClassTable(String className, String baseClassName);
	public InstanceLuaTable createInstanceTable(ClassLuaTable classTable);

}
