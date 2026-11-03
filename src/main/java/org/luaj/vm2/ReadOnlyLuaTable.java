/**													*\
 *   JPHYSGEAR GAME ENGINE BY DMITRI POTERIANSKI	*
 *													*
 * FILE IS PART OF THE JPHYSGEAR PROJECT SOURCE,	*
 * SEE LICENSE IN SOURCE FOLDER FOR RESTRICTIONS.	*
 *													*
 *	Copyright (c) Dmitri Poterianski 2017			*
\*													*/
package org.luaj.vm2;

import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;

/**
 * <p>
 * File Name: ReadOnlyLuaTable.java
 * <p>
 * Type Name: ReadOnlyLuaTable
 * <p>
 * Date Created: Jan 6, 2017
 * 
 * <p>
 * Description for type ReadOnlyLuaTable goes here
 * 
 * @author Dmitri Poterianski
 * @version 1.0
 */
public class ReadOnlyLuaTable extends LuaTable {
	public ReadOnlyLuaTable(LuaValue table) {
		presize(table.length(), 0);
		for (Varargs n = table.next(LuaValue.NIL); !n.arg1().isnil(); n = table
				.next(n.arg1())) {
			LuaValue key = n.arg1();
			LuaValue value = n.arg(2);
			super.rawset(key, value.istable() ? new ReadOnlyLuaTable(value) : value);
		}
	}
	public LuaValue setmetatable(LuaValue metatable) { return error("table is read-only"); }
	public void set(int key, LuaValue value) { error("table is read-only"); }
	public void rawset(int key, LuaValue value) { error("table is read-only"); }
	public void rawset(LuaValue key, LuaValue value) { error("table is read-only"); }
	public LuaValue remove(int pos) { return error("table is read-only"); }
}