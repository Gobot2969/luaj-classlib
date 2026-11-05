# TODO

Open work on luaj-classlib, beside the intended behaviour in the [README](README.md#intended-behaviour).

## Known bugs

Where the library doesn't do what the README says yet.

None known.

## Potential gaps

Not decided: behaviour that may need changing.

- **`pairs` over a class shows some system fields, not others.** It lists `__classname`, `__shortname`,
  `__basename` and `__index` (stored in the table) but not `__class` or `__base` (answered by
  `ClassLuaTable.hashget` only).
  Either store all of them or none.
- **`getmetatable(instance)` is the class table itself**, so any script holding an instance can change its
  class. A `__metatable` field on class tables would hide it, if classes should be protected.

## Later

- **A default `__tostring` for instances** (`dog: 0x...` instead of `table: 0x...`; LuaJ doesn't name
  tables by `__name` the way Lua 5.3+ does). Classes can already give their instances any metamethod by
  defining it (README, "Operators and other metamethods"); this is only about the default.
