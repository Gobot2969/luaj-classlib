package org.luaj.vm2;

import org.luaj.vm2.LuaTable;

public class InstanceLuaTable extends LuaTable {
	
	ClassLuaTable classTable;
	
	public InstanceLuaTable(ClassLuaTable classTable) {
		this.classTable = classTable;
	}

	public ClassLuaTable getClassTable() {
		return classTable;
	}
	
	

}
