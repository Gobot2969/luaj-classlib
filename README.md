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

Load a library into a script's globals, like LuaJ's own:

```java
Globals globals = JsePlatform.standardGlobals();
globals.load(new ClassLib());
globals.load("print(classlib.version)").call();
```

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
