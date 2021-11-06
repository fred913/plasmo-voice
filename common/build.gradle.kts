val minecraftVersion: String by rootProject
val fabricLoaderVersion: String by rootProject
val fabricVersion: String by rootProject
val modVersion: String by rootProject
val mavenGroup: String by rootProject

dependencies {
    minecraft("com.mojang:minecraft:${minecraftVersion}")
    mappings(minecraft.officialMojangMappings())

    modImplementation("net.fabricmc:fabric-loader:${fabricLoaderVersion}")

    implementation(project(":api")) {
        isTransitive = false
    }

    // Lombok
    compileOnly("org.projectlombok:lombok:1.18.20")
    annotationProcessor("org.projectlombok:lombok:1.18.20")

    implementation("com.google.code.findbugs:jsr305:3.0.2")

    // YAML for server config
    compileOnly("org.yaml:snakeyaml:1.29")

    // Opus
    compileOnly("su.plo.voice:opus:1.1.2")

    // RNNoise
    compileOnly("su.plo.voice:rnnoise:1.0.0")
}

architectury {
    common(true)
}

configurations {
    create("dev")
}

tasks {
    artifacts {
        add("dev", jar)
    }
}