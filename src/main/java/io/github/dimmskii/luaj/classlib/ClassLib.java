package io.github.dimmskii.luaj.classlib;

import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.TwoArgFunction;

/**
 * The {@code classlib} library for LuaJ scripts.
 *
 * <p>Loaded like LuaJ's own libraries:
 * <pre>{@code
 * Globals globals = JsePlatform.standardGlobals();
 * globals.load(new ClassLib());
 * }</pre>
 * after which scripts see a global table {@code classlib}, also in {@code package.loaded}.
 */
public class ClassLib extends TwoArgFunction {

    /** The name of the library's table. */
    public static final String NAME = "classlib";

    /** Creates the library; {@link org.luaj.vm2.Globals#load(LuaValue)} installs it. */
    public ClassLib() {
    }

    /**
     * Installs the library into {@code env}.
     *
     * @param modname the module name ({@code require}'s argument), unused
     * @param env     the environment, normally the {@link org.luaj.vm2.Globals}
     * @return the library's table
     */
    @Override
    public LuaValue call(LuaValue modname, LuaValue env) {
        LuaTable lib = new LuaTable();
        lib.set("version", valueOf(version()));
        env.set(NAME, lib);
        LuaValue loaded = env.get("package").get("loaded");
        if (loaded.istable()) {
            loaded.set(NAME, lib);
        }
        return lib;
    }

    /**
     * The library's version, from its jar's manifest.
     *
     * @return the version, or {@code "dev"} when not running from a built jar
     */
    public static String version() {
        String v = ClassLib.class.getPackage().getImplementationVersion();
        return v != null ? v : "dev";
    }
}
