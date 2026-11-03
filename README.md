# luaj-classlib

Java class libraries for [LuaJ](https://github.com/dimmskii/luaj) scripts. Runs on Java 8 and later.

## Usage

Releases are built by [JitPack](https://jitpack.io/#dimmskii/luaj-classlib) from this repository's tags.
LuaJ ([luaj-jitpack](https://github.com/dimmskii/luaj-jitpack)) comes with it.

Gradle (Kotlin DSL):

```kotlin
repositories {
    mavenCentral()
    maven("https://jitpack.io")
}

dependencies {
    implementation("com.github.dimmskii:luaj-classlib:0.1.0")
}
```

Maven:

```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>

<dependency>
    <groupId>com.github.dimmskii</groupId>
    <artifactId>luaj-classlib</artifactId>
    <version>0.1.0</version>
</dependency>
```

Load the library into a script's globals, like LuaJ's own:

```java
Globals globals = JsePlatform.standardGlobals();
globals.load(new ClassLib());   // org.luaj.vm2.lib.ClassLib: the globals class, new and super
```

Then, in Lua:

```lua
local Animal = class.register({ __classname = "animal",
    speak = function(self) return "..." end })

local Dog = class.register({ __classname = "dog",
    speak = function(self) return "Woof, not " .. super.speak(self) end }, Animal)

print(new(Dog):speak())   -- Woof, not ...
```

## Intended behaviour

What the library is meant to do. Where it doesn't yet, [TODO.md](TODO.md) lists it.

**Installing.** `globals.load(new ClassLib())` sets the globals `class` (also `package.loaded.class`), `new`
and `super`. `class` holds `class.register` and `class.get`.

**Defining a class.** A class is defined as a plain table: its `__classname` (a string), and its methods and
fields. `class.register(table [, base])` makes it a class and returns the class table. That's a new table:
the one passed in is only read, so a script can build it however it likes (a local, a global like `ENT`
filled by another file) and drop it after. `base` is a class returned by `class.register` or `class.get`.

**Names.** A class's name is its `__classname`, exactly as written: names are case-sensitive (`"Dog"` and
`"dog"` are two classes). `.` is a delimiter (`"weapon.pistol"`), so a name can't be empty, start or end
with a dot, or have two dots in a row. Each name is registered once. `class.get(name)` returns the class
registered under exactly that name, or nil.

**Hierarchy.** Classes don't have to share a common ancestor: any class, at any point in a hierarchy, may
have no base (nil), and there can be any number of root classes. The only rules:

1. A class may not inherit from itself, directly or through other classes.
2. When a class is registered, its base class (and so all of its ancestors) must already be registered.

Both hold by construction: a base is a class that `class.register` returned before, and a class only exists
once its own `class.register` returns, so no chain can come back to it.

**Inheritance.** Registering gives the class a copy of its base's fields and methods, then its own: its
own replace the base's of the same name. The copy is taken when the class is registered: methods added to
a base afterwards don't reach classes already registered on it. Register bases first, and define a class's
methods before registering it.

**Instances.** `new(Class)` makes an instance: a table of its own, whose metatable is its class (with
`__index` the class). Fields set on an instance are the instance's; methods and the class's fields are
found on the class.

**System fields.** Every class has these, and its instances see them through the class. The library keeps
them; scripts read them and don't set them.

| Field | Is |
|---|---|
| `__classname` | the class's name, as registered |
| `__classname_base` | its base's name, `""` for a class with no base |
| `__class` | the class table itself |
| `__class_base` | the base class table, nil for a class with no base |

**super.** Inside a class's method, `super.name` is `name` in the base of the class *the method was
registered with*, not the instance's class: so every level of a chain of supercalls goes one class up, and
classes that don't implement the method are skipped. Call it with a dot and pass `self`:
`super.speak(self, ...)`. It works for interpreted functions and ones compiled with LuaJC, through tail
calls, errors and coroutines (a method can yield in the middle). It's only valid while a method runs: used
outside one (including from a callback called after its method returned), or in a class with no base, it
raises a Lua error. The methods are the functions the table held when it was registered.

**Errors.** Misuse (a missing, non-string or invalid `__classname`, a base that isn't a class, `new` on
something that isn't a class, a name registered twice) raises a Lua error naming the problem.

## Building

Needs JDK 17 or later (to run Gradle); the jar targets Java 8.

```sh
./gradlew build              # jar, sources and javadoc jars in build/libs
./gradlew publishToMavenLocal  # into ~/.m2 as io.github.dimmskii:luaj-classlib:<version>
```

The version for local builds is in `gradle.properties`; dependency versions are in
`gradle/libs.versions.toml`.

## Releasing

Tag a commit on `main` with the version and push the tag; JitPack builds it the first time it's
requested (log: `https://jitpack.io/com/github/dimmskii/luaj-classlib/<tag>/build.log`).

```sh
git tag 0.1.0
git push origin 0.1.0
```

## License

MIT, see [LICENSE](LICENSE).
