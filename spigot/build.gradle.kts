val paperApiVersion: String by rootProject
val minecraftVersion: String by rootProject

dependencies {
    compileOnly("com.destroystokyo.paper:paper-api:${paperApiVersion}")

    // Lombok
    compileOnly("org.projectlombok:lombok:1.18.20")
    annotationProcessor("org.projectlombok:lombok:1.18.20")
}

repositories {
    mavenCentral()
    mavenLocal()
    maven {
        url = uri("https://repo.extendedclip.com/content/repositories/placeholderapi/")
    }
    maven {
        url = uri("https://papermc.io/repo/repository/maven-public/")
    }
}