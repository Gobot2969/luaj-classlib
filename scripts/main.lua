-- Runs with LuaJ's standard libraries and luaj-classlib's `class` library already in the globals.
print(_VERSION, "| class library:", class ~= nil, "| package library:", package ~= nil)

-- Set package search path to scripts as well
package.path = package.path .. ";./scripts/?.lua;./?.lua"


---------------------------------------------------------------
-- Inline examples
---------------------------------------------------------------
-- A class: a plain table of methods, bound at the end.
local Animal = {}
function Animal:speak() return "..." end
Animal = class.bind(Animal)
-- A subclass, extending Animal.
local Dog = {}
function Dog:speak() return "Woof" end
Dog = class.bind(Dog, Animal)

print("Dog says", Dog:speak())

-- Arguments after the script name: ./gradlew run --args="scripts/main.lua one two"
if arg and #arg > 0 then print("arguments:", table.concat(arg, ", ")) end




---------------------------------------------------------------
-- Oldschool Risingear way inspired by GMod
-- you supply the global beforehand and register after it runs
---------------------------------------------------------------
ENT = {}
require("class_a_oldschool")
CEntity = class.bind( ENT )
ENT = nil -- Clear from globals whenever you're done all of these
--local ent1 = new(CEntity) -- Should work
--local ent2 = new("Entity") -- Should work