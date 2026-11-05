package org.luaj.vm2.lib;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/** super, run interpreted and compiled with LuaJC: the README says it works the same in both. */
class SuperTest extends ClassLibTestBase {

	/** Five classes, each extending the one before; 1st, 3rd and 5th implement hello, 3rd and 5th call super. */
	private static final String CHAIN = "local log = {}"
			+ " class.register({ __classname = 'c1', hello = function(self) log[#log + 1] = 'c1' end })"
			+ " class.register({ __classname = 'c2', __basename = 'c1' })"
			+ " class.register({ __classname = 'c3', __basename = 'c2', hello = function(self) log[#log + 1] = 'c3' super.hello(self) end })"
			+ " class.register({ __classname = 'c4', __basename = 'c3' })"
			+ " class.register({ __classname = 'c5', __basename = 'c4', hello = function(self) log[#log + 1] = 'c5' super.hello(self) end })";

	@ParameterizedTest(name = "luajc={0}")
	@ValueSource(booleans = { false, true })
	void goesOneClassUpAtEveryLevelAndSkipsClassesWithoutTheMethod(boolean luajc) {
		globals = globals(luajc);
		assertEquals("c5 c3 c1", eval(CHAIN + " new('c5'):hello() return table.concat(log, ' ')").tojstring());
	}

	@ParameterizedTest(name = "luajc={0}")
	@ValueSource(booleans = { false, true })
	void worksThroughTailCalls(boolean luajc) {
		globals = globals(luajc);
		assertEquals("A", eval("class.register({ __classname = 'a', f = function(self) return 'A' end })"
				+ " class.register({ __classname = 'b', __basename = 'a', f = function(self) return super.f(self) end })"
				+ " return new('b'):f()").tojstring());
	}

	@ParameterizedTest(name = "luajc={0}")
	@ValueSource(booleans = { false, true })
	void staysRightAfterAnErrorInTheBase(boolean luajc) {
		globals = globals(luajc);
		assertEquals("caught B>A", eval("class.register({ __classname = 'a', f = function(self) error('no') end, g = function(self) return 'A' end })"
				+ " class.register({ __classname = 'b', __basename = 'a', g = function(self) return 'B>' .. super.g(self) end,"
				+ "   f = function(self) local ok = pcall(super.f, self) return (ok and 'not ' or '') .. 'caught ' .. self:g() end })"
				+ " return new('b'):f()").tojstring());
	}

	@ParameterizedTest(name = "luajc={0}")
	@ValueSource(booleans = { false, true })
	void survivesAYieldInTheMiddleOfAMethod(boolean luajc) {
		globals = globals(luajc);
		assertEquals("B>A", eval("class.register({ __classname = 'a', f = function(self) coroutine.yield() return 'A' end, g = function(self) return 'g' end })"
				+ " class.register({ __classname = 'b', __basename = 'a', f = function(self) return 'B>' .. super.f(self) end })"
				+ " local b = new('b') local co = coroutine.create(function() return b:f() end)"
				+ " coroutine.resume(co) b:g() local _, r = coroutine.resume(co) return r").tojstring());
	}

	@ParameterizedTest(name = "luajc={0}")
	@ValueSource(booleans = { false, true })
	void isAnErrorOutsideAMethodOrInARootClass(boolean luajc) {
		globals = globals(luajc);
		assertTrue(error("return super.f").contains("super: not inside a class method"));
		assertTrue(error("class.register({ __classname = 'r', f = function(self) return super.f(self) end }) return new('r'):f()")
				.contains("super: class 'r' has no base class"));
	}
}
