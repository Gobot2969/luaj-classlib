# TODO

Open work on luaj-classlib, beside the intended behaviour in the [README](README.md#intended-behaviour).

## Known bugs

Where the library doesn't do what the README says yet.

- **`class.get` of an unknown name crashes** (a Java `NullPointerException`) instead of returning nil.
- **Names aren't case-insensitive yet.** Classes are stored under `__name` as written, while their
  `__name` is lowercased, so `{ __name = "Dog" }` is found by `class.get("Dog")` only.
- **Lowercasing follows the JVM's language.** On a Turkish locale `"ITEM"` becomes `"ıtem"` (dotless i);
  use `toLowerCase(Locale.ROOT)`.
- **Misuse gives Java exceptions, not Lua errors:** a base that isn't a class or `new` on something that
  isn't one (`ClassCastException`). A missing `__name` is accepted (the class is named `"nil"`), and a
  name registered twice replaces the first class silently.
- **Writes to the system fields are ignored silently** (`C.__name = "x"` is stored, but `__name` still
  reads the real name); they should raise an error.
- **Methods added to a class after it's registered have no `super`** (`function C:f() super.f(self) end`
  after `class.register` raises "not inside a class method"). Wrapping functions as they're set on a class
  table (`ClassLuaTable.rawset`) would fix it.

## Potential gaps

Not decided: behaviour that may need changing.

- **`pairs` over a class shows some system fields, not others.** It lists `__name`, `__basename` and
  `__index` (stored in the table) but not `__class` or `__base` (answered by `ClassLuaTable.hashget` only).
  Either store all of them or none.
- **`getmetatable(instance)` is the class table itself**, so any script holding an instance can change its
  class. A `__metatable` field on class tables would hide it, if classes should be protected.

## Later

- **Metamethods for instances**, starting with `__tostring` (`dog: 0x...` instead of `table: 0x...`;
  LuaJ doesn't name tables by `__name` the way Lua 5.3+ does). Then whichever of `__eq`, `__lt`, `__le`,
  `__concat`, the arithmetic ones and `__call` classes should get, and how a class declares them.
