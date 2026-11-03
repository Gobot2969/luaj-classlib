plugins {
    `java-library`
    `maven-publish`
}

description = "Java class libraries for LuaJ scripts"

dependencies {
    // In the public API (libraries take and return LuaValue), so consumers get LuaJ too.
    api(libs.luaj)
}

java {
    withSourcesJar()
    withJavadocJar()
}

// The library runs on Java 8, like LuaJ itself; any JDK that runs Gradle (17+) builds it.
tasks.compileJava {
    options.release = 8
    options.encoding = "UTF-8"
    // -Xlint:-options: newer JDKs call the Java 8 target obsolete
    options.compilerArgs.addAll(listOf("-Xlint:all", "-Xlint:-options", "-Werror"))
}

tasks.javadoc {
    options {
        this as StandardJavadocDocletOptions
        encoding = "UTF-8"
        addBooleanOption("Xdoclint:all,-missing", true)
        addStringOption("-release", "8")
    }
}

tasks.jar {
    manifest {
        attributes(
            "Automatic-Module-Name" to "io.github.dimmskii.luaj.classlib",
            "Implementation-Title" to project.name,
            "Implementation-Version" to project.version,
        )
    }
}

// JitPack rewrites the artifact's coordinates; its rewrite of Gradle module metadata has pointed the
// sources variant at the binary jar, so consumers get the POM only.
tasks.withType<GenerateModuleMetadata>().configureEach {
    enabled = false
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
            pom {
                name = project.name
                description = project.description
                url = "https://github.com/dimmskii/luaj-classlib"
                licenses {
                    license {
                        name = "MIT License"
                        url = "https://opensource.org/licenses/MIT"
                    }
                }
                developers {
                    developer {
                        id = "dimmskii"
                        name = "Dmitri"
                        url = "https://github.com/dimmskii"
                    }
                }
                scm {
                    url = "https://github.com/dimmskii/luaj-classlib"
                    connection = "scm:git:https://github.com/dimmskii/luaj-classlib.git"
                    developerConnection = "scm:git:ssh://git@github.com/dimmskii/luaj-classlib.git"
                }
            }
        }
    }
}
