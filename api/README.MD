# API (WIP)

## Adding Plasmo Voice to the project
### Maven
```xml
<project>
    <repositories>
        <repository>
            <id>plasmo-repo</id>
            <url>https://repo.plo.su</url>
        </repository>
    </repositories>
    
    <dependencies>
        <dependency>
            <groupId>su.plo.voice</groupId>
            <artifactId>api</artifactId>
            <version>2.0.0</version>
            <scope>provided</scope>
        </dependency>
    </dependencies>
</project>
```
### Groovy DSL
```groovy
repositories {
    maven { url 'https://repo.plo.su/' }
}

dependencies {
    compileOnly 'su.plo.voice:api:2.0.0'
}
```
### Kotlin DSL
```
repositories {
    maven {
        url = uri("https://repo.plo.su")
    }
}

dependencies {
    compileOnly("su.plo.voice:api:2.0.0")
}
```

## Obtaining an instance of the API
### Using the singleton (static access)
```java
import su.plo.voice.api.PlasmoVoiceProvider;
// ...

PlasmoVoiceAPI api = PlasmoVoiceProvider.getAPI();
```

## Examples
- [Fabric](//)