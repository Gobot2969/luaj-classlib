-- luaj-classlib, a walkthrough. Run: ./gradlew runLua --args="scripts/demo.lua"
-- Every block checks itself with assert, so a clean run is also a test.

-- Classes are loaded with the package library: game.weapon.pistol is scripts/demo/game/weapon/pistol.lua.
package.path = package.path .. ";./scripts/demo/?.lua"

---------------------------------------------------------------------------------------------------
-- 1. A class: a table with a __classname and members, registered. new makes instances.
---------------------------------------------------------------------------------------------------
local Point = class.register({
    __classname = "demo.point",
    __constr = function(self, x, y)
        self.x, self.y = x, y
    end,
    length = function(self)
        return math.sqrt(self.x * self.x + self.y * self.y)
    end,
})

local p = new(Point, 3, 4)
print("1. point length:", p:length())
assert(p:length() == 5)
-- The same, three ways: by class, by name, through the class's __new (call it with a dot).
assert(new("demo.point", 3, 4):length() == 5 and Point.__new(3, 4):length() == 5)
assert(isclass(Point) and isobj(p) and not isclass(p) and not isobj({}))

---------------------------------------------------------------------------------------------------
-- 2. Names: case-sensitive, '.' delimits parts. The library keeps these fields on every class.
---------------------------------------------------------------------------------------------------
print("2. names:", p.__classname, p.__shortname, "[" .. p.__basename .. "]", p.__class == Point, p.__base)
assert(class.get("demo.point") == Point and class.get("Demo.Point") == nil)
-- __shortname (the part after the last dot) is there to use at will, e.g. as a display name:
assert(p.__shortname == "point")

---------------------------------------------------------------------------------------------------
-- 3. Classes in files, loaded with require. Each file requires its base, so bases register first.
---------------------------------------------------------------------------------------------------
local Pistol = require("game.weapon.pistol")
local Gun = class.get("game.weapon.gun")
print("3. loaded:", Pistol.__classname, "extends", Pistol.__basename, "extends", Gun.__basename)
assert(Pistol.__base == Gun and Gun.__base == class.get("game.entity"))

---------------------------------------------------------------------------------------------------
-- 4. Inheritance and super. A class gets its base's members (kept as copies) and can override
--    them; super.name(self, ...) calls the base's, one class up at every level.
---------------------------------------------------------------------------------------------------
local pistol = new(Pistol, "sidearm", 1)                -- the gun's constructor, copied into Pistol
print("4. " .. pistol:describe() .. ":", pistol:fire(), pistol:fire())
assert(pistol.ammo == 0 and pistol:fire() == "pew (click)")

-- super goes up from the class that wrote the method, so chains don't loop, and skip gaps:
local A = class.register({ __classname = "chain.a", hi = function(self) return "a" end })
local B = class.register({ __classname = "chain.b", __basename = "chain.a" })   -- no hi of its own
local C = class.register({ __classname = "chain.c", __basename = "chain.b",
    hi = function(self) return "c>" .. super.hi(self) end })
assert(new(C):hi() == "c>a")

-- Classes stay mutable: a method set on B later reaches its subclasses and enters the chain.
B.hi = function(self) return "b>" .. super.hi(self) end
print("4. after B.hi is set:", new(C):hi())
assert(new(C):hi() == "c>b>a")
B.hi = nil                                              -- given up: inherited from A again
assert(new(C):hi() == "c>a")

---------------------------------------------------------------------------------------------------
-- 5. Constructors: __constr gets new's other arguments. Without one, a class runs its base's;
--    with none at all, a default that does nothing (so super.__constr is always callable).
---------------------------------------------------------------------------------------------------
local gun = new(Gun, "rifle", 30)
print("5. constructed:", gun.name, gun.ammo, "| defaults:", new(A) ~= nil)

---------------------------------------------------------------------------------------------------
-- 6. "Forced coincidence": make class names match real namespaces. Classes named like their
--    module paths can also be exposed as nested globals, starting from the global namespace.
---------------------------------------------------------------------------------------------------
local function expose(cls)
    local at = _G
    for part in cls.__classname:gmatch("([^.]+)%.") do  -- every part but the last
        at[part] = at[part] or {}
        at = at[part]
    end
    at[cls.__shortname] = cls                           -- the last part: the short name
end
for _, name in ipairs({ "game.entity", "game.weapon.gun", "game.weapon.pistol" }) do
    expose(class.get(name))
end
print("6. globals:", game.weapon.pistol == Pistol, new(game.weapon.gun, "smg", 2):fire())
assert(game.entity == class.get("game.entity"))

---------------------------------------------------------------------------------------------------
-- 7. Misuse raises Lua errors.
---------------------------------------------------------------------------------------------------
local function fails(f, ...) local ok, err = pcall(f, ...) assert(not ok) return err end
print("7. errors:")
print("  ", fails(class.register, { __classname = "demo.point" }))                    -- registered twice
print("  ", fails(class.register, { __classname = "demo..bad" }))                     -- empty part
print("  ", fails(class.register, { __classname = "demo.orphan", __basename = "nope" }))
print("  ", fails(class.register, { __classname = "demo.two" }, Point))               -- one argument only
print("  ", fails(new, "no.such.class"))

print("demo: ok")
