import net.fabricmc.loom.api.LoomGradleExtensionAPI

val minecraftVersion: String by rootProject
val fabricLoaderVersion: String by rootProject
val fabricVersion: String by rootProject

base {
    archivesBaseName = "pv-api-example"
}

project.version = "1.0.0-example"
project.group = "su.plo.voice.example"

configure<LoomGradleExtensionAPI> {
    silentMojangMappingsLicense()
}

dependencies {
    minecraft("com.mojang:minecraft:${minecraftVersion}")
    mappings(loom.officialMojangMappings())

    modApi("net.fabricmc:fabric-loader:${fabricLoaderVersion}")
    modApi("net.fabricmc.fabric-api:fabric-api:${fabricVersion}")

    // lavaplayer
    implementation("com.sedmelluq:lavaplayer:1.3.77")

    // you can use this
//    modCompileOnly("su.plo.voice:api:2.0.0")
//    modRuntimeOnly("su.plo.voice:fabric:2.0.0")

    // this will work only with root project
    modCompileOnly(project(":api"))
    modRuntimeOnly(project(":fabric"))

    // Lombok
    compileOnly("org.projectlombok:lombok:1.18.20")
    annotationProcessor("org.projectlombok:lombok:1.18.20")
}

repositories {
    maven {
        url = uri("https://m2.dv8tion.net/releases")
    }
}