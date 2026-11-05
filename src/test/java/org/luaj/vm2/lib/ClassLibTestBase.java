package org.luaj.vm2.lib;

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.luaj.vm2.Globals;
import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;
import org.luaj.vm2.lib.jse.JsePlatform;
import org.luaj.vm2.luajc.LuaJC;

/** Fresh globals with the class library for each test, and helpers to run Lua in them. */
abstract class ClassLibTestBase {

	/** Globals with the standard libraries and {@link ClassLib}; scripts compiled to Java bytecode if {@code luajc}. */
	static Globals globals(boolean luajc) {
		Globals globals = JsePlatform.standardGlobals();
		if (luajc) {
			LuaJC.install(globals);
		}
		globals.load(new ClassLib());
		return globals;
	}

	Globals globals = globals(false);

	/** Runs a chunk and returns what it returns. */
	Varargs run(String lua) {
		return globals.load(lua, "test").invoke();
	}

	/** Runs a chunk and returns its first result. */
	LuaValue eval(String lua) {
		return run(lua).arg1();
	}

	/** Runs a chunk that must raise a Lua error, and returns the error's message. */
	String error(String lua) {
		return assertThrows(LuaError.class, () -> run(lua)).getMessage();
	}
}
