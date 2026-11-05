import java.io.File;

import org.luaj.vm2.Globals;
import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;
import org.luaj.vm2.lib.ClassLib;
import org.luaj.vm2.lib.jse.JsePlatform;

/**
 * Runs a Lua file with LuaJ's standard libraries and luaj-classlib's {@code class} library in its globals.
 * <p>
 * Usage: {@code LuaJClassLibTest [script.lua [args...]]} (or {@code ./gradlew runLua --args="script.lua"}); without a script, {@code scripts/main.lua}. The script's arguments are
 * in {@code arg} and in {@code ...}, as with the {@code lua} command.
 */
public final class LuaJClassLibTest {

    private LuaJClassLibTest() {
    }

    /** Creates globals with the standard libraries and {@code class}. */
    public static Globals newGlobals() {
        Globals globals = JsePlatform.standardGlobals();
        globals.load(new ClassLib());
        return globals;
    }

    public static void main(String[] args) {
        String script = args.length > 0 ? args[0] : "scripts/main.lua";
        if (!new File(script).isFile()) {
            System.err.println("No such Lua file: " + new File(script).getAbsolutePath());
            System.exit(1);
        }
        LuaValue[] scriptArgs = new LuaValue[Math.max(0, args.length - 1)];
        for (int i = 0; i < scriptArgs.length; i++) {
            scriptArgs[i] = LuaValue.valueOf(args[i + 1]);
        }
        Globals globals = newGlobals();
        try {
            globals.set("arg", argTable(script, scriptArgs));
            LuaValue chunk = globals.loadfile(script);
            Varargs vargs = LuaValue.varargsOf(scriptArgs);
            chunk.invoke(vargs);
        } catch (LuaError e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }
    }

    // arg[0] is the script, arg[1..n] its arguments, as the lua command sets them.
    private static LuaValue argTable(String script, LuaValue[] scriptArgs) {
        LuaValue arg = LuaValue.tableOf();
        arg.set(0, script);
        for (int i = 0; i < scriptArgs.length; i++) {
            arg.set(i + 1, scriptArgs[i]);
        }
        return arg;
    }
}
