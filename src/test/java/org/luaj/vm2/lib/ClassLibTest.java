package org.luaj.vm2.lib;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.luaj.vm2.ClassLuaTable;
import org.luaj.vm2.InstanceLuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;

/** The class library's documented behaviour (README, "Intended behaviour"), but super (SuperTest). */
class ClassLibTest extends ClassLibTestBase {

	@Test
	void installsItsGlobals() {
		Varargs v = run("return class, package.loaded.class, new, super, isclass, isobj, class.register, class.get");
		for (int i = 1; i <= 8; i++) {
			assertFalse(v.arg(i).isnil(), "global " + i);
		}
		assertTrue(v.arg(1) == v.arg(2));
	}

	@Nested
	class Registering {

		@Test
		void returnsANewClassTable() {
			Varargs v = run("local t = { __classname = 'demo.point' } local C = class.register(t) return C, t, isclass(C)");
			assertTrue(v.arg1() instanceof ClassLuaTable);
			assertTrue(v.arg1() != v.arg(2));
			assertTrue(v.arg(3).toboolean());
		}

		@Test
		void keepsTheSystemFields() {
			Varargs v = run("local A = class.register({ __classname = 'game.entity' })"
					+ " local B = class.register({ __classname = 'game.weapon.gun', __basename = 'game.entity' })"
					+ " return B.__classname, B.__shortname, B.__basename, B.__class == B, B.__base == A,"
					+ " A.__shortname, A.__basename, A.__base");
			assertEquals("game.weapon.gun", v.arg(1).tojstring());
			assertEquals("gun", v.arg(2).tojstring());
			assertEquals("game.entity", v.arg(3).tojstring());
			assertTrue(v.arg(4).toboolean());
			assertTrue(v.arg(5).toboolean());
			assertEquals("entity", v.arg(6).tojstring());
			assertEquals("", v.arg(7).tojstring());
			assertTrue(v.arg(8).isnil());
		}

		@Test
		void namesAreCaseSensitive() {
			Varargs v = run("local a = class.register({ __classname = 'Dog' }) local b = class.register({ __classname = 'dog' })"
					+ " return a ~= b, class.get('Dog') == a, class.get('dog') == b, class.get('DOG')");
			assertTrue(v.arg(1).toboolean());
			assertTrue(v.arg(2).toboolean());
			assertTrue(v.arg(3).toboolean());
			assertTrue(v.arg(4).isnil());
		}

		@Test
		void rejectsInvalidNames() {
			assertTrue(error("class.register({})").contains("must be a string, got nil"));
			assertTrue(error("class.register({ __classname = 5 })").contains("must be a string, got number"));
			assertTrue(error("class.register({ __classname = '' })").contains("must not be empty"));
			assertTrue(error("class.register({ __classname = '.a' })").contains("must not start with '.'"));
			assertTrue(error("class.register({ __classname = 'a.' })").contains("must not end with '.'"));
			assertTrue(error("class.register({ __classname = 'a..b' })").contains("must not have two '.' in a row"));
		}

		@Test
		void rejectsANameRegisteredTwice() {
			run("class.register({ __classname = 'dup' })");
			assertTrue(error("class.register({ __classname = 'dup' })").contains("'dup' is already registered"));
		}

		@Test
		void takesTheTableOnly() {
			run("class.register({ __classname = 'base' })");
			assertTrue(error("class.register({ __classname = 'x' }, class.get('base'))").contains("bad argument #2"));
			assertTrue(error("class.register({ __classname = 'y' }, nil)").contains("bad argument #2"));
			assertTrue(error("class.register()").contains("table expected"));
		}

		@Test
		void requiresARegisteredBase() {
			assertTrue(error("class.register({ __classname = 'x', __basename = 'nope' })").contains("base class 'nope' isn't registered"));
			assertTrue(error("class.register({ __classname = 'y', __basename = 5 })").contains("__basename must be a string"));
		}

		@Test
		void leavesNothingBehindWhenItFails() {
			error("class.register({ __classname = 'x', __basename = 'nope' })");
			assertTrue(eval("return class.get('x')").isnil());
		}
	}

	@Nested
	class Inheritance {

		@Test
		void copiesTheBasesMembersAndLetsTheClassOverrideThem() {
			Varargs v = run("local A = class.register({ __classname = 'a', f = function() return 'A.f' end, g = function() return 'A.g' end })"
					+ " local B = class.register({ __classname = 'b', __basename = 'a', g = function() return 'B.g' end })"
					+ " return B.f(), B.g(), A.g()");
			assertEquals("A.f", v.arg(1).tojstring());
			assertEquals("B.g", v.arg(2).tojstring());
			assertEquals("A.g", v.arg(3).tojstring());
		}

		@Test
		void takesTheCopyWhenTheClassRegisters() {
			LuaValue later = eval("local A = class.register({ __classname = 'a' })"
					+ " local B = class.register({ __classname = 'b', __basename = 'a' })"
					+ " A.later = function() end return B.later");
			assertTrue(later.isnil());
		}

		@Test
		void allowsAnyNumberOfRootClasses() {
			Varargs v = run("local A = class.register({ __classname = 'a' }) local X = class.register({ __classname = 'x' })"
					+ " local B = class.register({ __classname = 'b', __basename = 'a' }) return A.__base, X.__base, B.__base == A");
			assertTrue(v.arg(1).isnil());
			assertTrue(v.arg(2).isnil());
			assertTrue(v.arg(3).toboolean());
		}
	}

	@Nested
	class Instances {

		@Test
		void comeFromNewByClassOrNameOrTheClassesNew() {
			Varargs v = run("local C = class.register({ __classname = 'demo.c' })"
					+ " local a, b, c = new(C), new('demo.c'), C.__new()"
					+ " return a, getmetatable(a) == C, getmetatable(b) == C, getmetatable(c) == C, C.__new == C.__new");
			assertTrue(v.arg1() instanceof InstanceLuaTable);
			for (int i = 2; i <= 5; i++) {
				assertTrue(v.arg(i).toboolean(), "result " + i);
			}
		}

		@Test
		void haveTheirOwnFieldsAndFindTheClassesThroughIt() {
			Varargs v = run("local C = class.register({ __classname = 'c', kind = 'shared', who = function(self) return self.name end })"
					+ " local a, b = new(C), new(C) a.name = 'a' b.name = 'b'"
					+ " return a:who(), b:who(), a.kind, a.__classname, rawget(a, 'kind')");
			assertEquals("a", v.arg(1).tojstring());
			assertEquals("b", v.arg(2).tojstring());
			assertEquals("shared", v.arg(3).tojstring());
			assertEquals("c", v.arg(4).tojstring());
			assertTrue(v.arg(5).isnil());
		}

		@Test
		void isclassAndIsobjAnswerForAnyValue() {
			Varargs v = run("local C = class.register({ __classname = 'c' }) local i = new(C)"
					+ " return isclass(C), isobj(i), isclass(i), isobj(C), isclass({}), isobj('x'), isclass(nil), isobj(5)");
			assertTrue(v.arg(1).toboolean());
			assertTrue(v.arg(2).toboolean());
			for (int i = 3; i <= 8; i++) {
				assertFalse(v.arg(i).toboolean(), "result " + i);
			}
		}

		@Test
		void newRejectsWhatIsntAClass() {
			assertTrue(error("new({})").contains("class or class name expected, got table"));
			assertTrue(error("new(5)").contains("class or class name expected, got number"));
			assertTrue(error("new('nope')").contains("no class registered as 'nope'"));
		}
	}

	@Nested
	class Constructors {

		@Test
		void getNewsOtherArguments() {
			Varargs v = run("local C = class.register({ __classname = 'c', __constr = function(self, a, b) self.a, self.b = a, b end })"
					+ " local x, y, z = new(C, 1, 2), new('c', 3, 4), C.__new(5, 6) return x.a, x.b, y.b, z.a");
			assertEquals(1, v.arg(1).toint());
			assertEquals(2, v.arg(2).toint());
			assertEquals(4, v.arg(3).toint());
			assertEquals(5, v.arg(4).toint());
		}

		@Test
		void areCopiedIntoSubclassesLikeMethods() {
			LuaValue a = eval("class.register({ __classname = 'a', __constr = function(self, x) self.x = x end })"
					+ " local B = class.register({ __classname = 'b', __basename = 'a' }) return new(B, 7).x");
			assertEquals(7, a.toint());
		}

		@Test
		void chainWithSuper() {
			LuaValue log = eval("local log = {}"
					+ " class.register({ __classname = 'a', __constr = function(self) log[#log + 1] = 'a' end })"
					+ " class.register({ __classname = 'b', __basename = 'a' })"
					+ " class.register({ __classname = 'c', __basename = 'b', __constr = function(self) super.__constr(self) log[#log + 1] = 'c' end })"
					+ " new('c') return table.concat(log, '>')");
			assertEquals("a>c", log.tojstring());
		}

		@Test
		void defaultToOneThatDoesNothing() {
			Varargs v = run("local A = class.register({ __classname = 'a' })"
					+ " local B = class.register({ __classname = 'b', __basename = 'a', __constr = function(self) super.__constr(self) self.ok = true end })"
					+ " return type(A.__constr), new(B).ok");
			assertEquals("function", v.arg(1).tojstring());
			assertTrue(v.arg(2).toboolean());
		}

		@Test
		void mustBeFunctions() {
			assertTrue(error("class.register({ __classname = 'c', __constr = 5 })").contains("__constr must be a function, got number"));
		}

		@Test
		void raiseTheirErrorsFromNew() {
			assertTrue(error("new(class.register({ __classname = 'c', __constr = function() error('boom') end }))").contains("boom"));
		}
	}
}
