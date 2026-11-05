package org.luaj.vm2;

/**
 * An instance of a class: what calling the class (or {@code new}) returns. Its own fields are its own; its metatable
 * is its class, so methods and the class's fields are found there. Setting a field the class has makes the
 * instance's own, which masks the class's for this instance only; the system fields
 * ({@link LuaClassManager#isSystemKey}) can't be set, so they can't be masked.
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

	/**
	 * Stores as any table does, but for the system fields: setting one on an instance would mask the class's, so it's
	 * an error, as it is on a class. Every write of a non-integer key ends here ({@code set}, {@code rawset}).
	 *
	 * @throws LuaError if {@code key} is a system field
	 */
	@Override
	public void hashset(LuaValue key, LuaValue value) {
		if (LuaClassManager.isSystemKey(key)) {
			throw new LuaError("instance of '" + classTable.getClassName() + "': " + key.tojstring() + " can't be set");
		}
		super.hashset(key, value);
	}

}
