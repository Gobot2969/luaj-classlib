-- game.weapon.pistol: extends game.weapon.gun, overrides fire and calls the gun's with super.
require("game.weapon.gun")

return class.register({
    __classname = "game.weapon.pistol",
    __basename = "game.weapon.gun",
    fire = function(self)
        return "pew (" .. super.fire(self) .. ")"
    end,
})
