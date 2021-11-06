val minecraftVersion: String by rootProject
val fabricLoaderVersion: String by rootProject

dependencies {
    // architectury requires "minecraft" dep for all subprojects
    minecraft("com.mojang:minecraft:${minecraftVersion}")
    mappings(minecraft.officialMojangMappings())

    modImplementation("net.fabricmc:fabric-loader:${fabricLoaderVersion}")

    compileOnly("com.google.code.findbugs:jsr305:3.0.2")

    // Lombok
    compileOnly("org.projectlombok:lombok:1.18.20")
    annotationProcessor("org.projectlombok:lombok:1.18.20")
}

architectury {
    common(true)
}