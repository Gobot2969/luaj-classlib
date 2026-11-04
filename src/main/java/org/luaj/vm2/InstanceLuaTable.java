package org.luaj.vm2;

/**
 * An instance of a class: what {@code new} returns. Its own fields are its own; its metatable is its class, so
 * methods and the class's fields are found there.
 */
public class InstanceLuaTable extends LuaTable {

	/** The instance's class. */
	ClassLuaTable classTable;

	/**
	 * Creates an instance of a class; {@link LuaClassManager#newInstance} gives it its metatable and runs the constructor.
	 *
	 * @param classTable its class
	 */
	public InstanceLuaTable(ClassLuaTable classTable) {
		this.classTable = classTable;
	}

	/** @return the instance's class */
	public ClassLuaTable getClassTable() {
		return classTable;
	}

}
