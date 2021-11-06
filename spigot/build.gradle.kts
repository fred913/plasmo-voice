val paperApiVersion: String by rootProject
val minecraftVersion: String by rootProject

dependencies {
    minecraft("com.mojang:minecraft:${minecraftVersion}")
    mappings(minecraft.officialMojangMappings())

    compileOnly("com.destroystokyo.paper:paper-api:${paperApiVersion}")

    // Lombok
    compileOnly("org.projectlombok:lombok:1.18.20")
    annotationProcessor("org.projectlombok:lombok:1.18.20")

    testCompileOnly("org.projectlombok:lombok:1.18.20")
    testAnnotationProcessor("org.projectlombok:lombok:1.18.20")
}

repositories {
    maven {
        url = uri("https://repo.extendedclip.com/content/repositories/placeholderapi/")
    }

    mavenCentral()
    mavenLocal()
}