val mavenUser: String by rootProject
val mavenPassword: String by rootProject

val minecraftVersion: String by rootProject
val fabricLoaderVersion: String by rootProject

val mavenGroup: String by rootProject
val modVersion: String by rootProject

project.group = mavenGroup
project.version = modVersion

dependencies {
    compileOnly("com.google.code.findbugs:jsr305:3.0.2")

    // Lombok
    compileOnly("org.projectlombok:lombok:1.18.20")
    annotationProcessor("org.projectlombok:lombok:1.18.20")
}

architectury {
}

publishing {
    repositories {
        maven {
            credentials {
                username = mavenUser
                password = mavenPassword
            }
            url = uri("https://repo.plo.su/public/")
        }
    }
    publications {
        register("api", MavenPublication::class) {
            artifact(tasks.sourcesJar.get())
        }
    }
}