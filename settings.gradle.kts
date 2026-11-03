rootProject.name = "luaj-classlib"

dependencyResolutionManagement {
    repositoriesMode = RepositoriesMode.FAIL_ON_PROJECT_REPOS
    repositories {
        mavenCentral()
        // LuaJ is built by JitPack from github.com/dimmskii/luaj; only that group is looked up there.
        exclusiveContent {
            forRepository { maven("https://jitpack.io") }
            filter { includeGroup("com.github.dimmskii") }
        }
    }
}
