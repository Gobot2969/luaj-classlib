# TODO

Open work on luaj-classlib, beside the intended behaviour in the [README](README.md#intended-behaviour).

## Known bugs

Where the library doesn't do what the README says yet.

- **Writes to the system fields are ignored silently** (`C.__classname = "x"` is stored, but `__classname` still
  reads the real name); they should raise an error.
- **Methods added to a class after it's registered have no `super`** (`function C:f() super.f(self) end`
  after `class.register` raises "not inside a class method"). Wrapping functions as they're set on a class
  table (`ClassLuaTable.rawset`) would fix it.

- **A base passed as `class.get(...)` before it's registered becomes no base, silently.**
  `class.register(t, class.get("Gun"))` before `Gun` is registered passes nil, so the class is registered
  as a root instead of failing rule 2 (README, Hierarchy). Naming the base in the table
  (`__basename = "Gun"`) raises an error instead; `register` could also take the name as its second
  argument (`class.register(t, "Gun")`).

- **`super.__constr` when no ancestor has a constructor** is nil, so calling it errors with "attempt to
  call a nil value". It could be a function that does nothing, so a constructor can always call up.

## Potential gaps

Not decided: behaviour that may need changing.

- **`pairs` over a class shows some system fields, not others.** It lists `__classname`, `__shortname`,
  `__basename` and `__index` (stored in the table) but not `__class` or `__base` (answered by
  `ClassLuaTable.hashget` only).
  Either store all of them or none.
- **`getmetatable(instance)` is the class table itself**, so any script holding an instance can change its
  class. A `__metatable` field on class tables would hide it, if classes should be protected.

## Later

- **Metamethods for instances**, starting with `__tostring` (`dog: 0x...` instead of `table: 0x...`;
  LuaJ doesn't name tables by `__name` the way Lua 5.3+ does). Then whichever of `__eq`, `__lt`, `__le`,
  `__concat`, the arithmetic ones and `__call` classes should get, and how a class declares them.
