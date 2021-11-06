import net.fabricmc.loom.api.LoomGradleExtensionAPI

val minecraftVersion: String by rootProject

plugins {
    java
    id("architectury-plugin") version("3.4-SNAPSHOT")
    id("dev.architectury.loom") version("0.10.0.186") apply(false) // 188 version is broken
    id("com.github.johnrengelman.shadow") version("7.0.0") apply(false)
    id("com.matthewprenger.cursegradle") version("1.4.0") apply(false)
}

architectury {
    minecraft = minecraftVersion
}

subprojects {
    apply(plugin = "dev.architectury.loom")

    configure<LoomGradleExtensionAPI> {
        silentMojangMappingsLicense()

        launches {
            named("client") {
//                property("fabric.log.level", "debug")
            }
        }
    }
}

allprojects {
    apply(plugin = "java")
    apply(plugin = "com.github.johnrengelman.shadow")
    apply(plugin = "architectury-plugin")
    apply(plugin = "com.matthewprenger.cursegradle")

//    java { toolchain { languageVersion.set(JavaLanguageVersion.of(16)) } }

    tasks.withType<JavaCompile>().configureEach {
        options.encoding = "UTF-8"
        options.release.set(16)
    }

    repositories {
        maven {
            url = uri("https://repo.plo.su")
        }

        mavenCentral()
        mavenLocal()
    }

    java {
        withSourcesJar()
    }
}