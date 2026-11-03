-- Runs with LuaJ's standard libraries and luaj-classlib's `class` library already in the globals.
print(_VERSION, "| class library:", class ~= nil, "| package library:", package ~= nil)

-- Set package search path to scripts as well
package.path = package.path .. ";./scripts/?.lua;./?.lua"


---------------------------------------------------------------
-- Inline examples
---------------------------------------------------------------
-- A class: a plain table of methods, bound at the end.
local CAnimal = {
    CLASSNAME="Animal"
}
function CAnimal:speak() return "..." end
CAnimal = class.register(CAnimal)
-- A subclass, extending Animal.
local CDog = {
    CLASSNAME="Dog"
}
function CDog:speak() return "Woof" end
CDog = class.register(CDog, CAnimal)

local dogInstance = new(CDog)
print("Dog says", dogInstance:speak())

-- Arguments after the script name: ./gradlew run --args="scripts/main.lua one two"
if arg and #arg > 0 then print("arguments:", table.concat(arg, ", ")) end




---------------------------------------------------------------
-- Oldschool Risingear way inspired by GMod
-- you supply the global beforehand and register after it runs
---------------------------------------------------------------
ENT = {}
require("class_a_oldschool")
CEntity = class.register( ENT )
ENT = nil -- Clear from globals whenever you're done all of these
local ent1 = new(CEntity) -- Should work
ent1:info()

---------------------------------------------------------------
-- Neat file. Uses its own local table inside itself,
-- then the file itself returns the class registry result
---------------------------------------------------------------
local ent2 = require("class_b_neatfile")
ent2:info()


-- Attempt to write readonly info
ent2.CLASS = {}
