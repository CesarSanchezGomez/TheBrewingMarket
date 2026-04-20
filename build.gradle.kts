plugins {
    id("java")
    id("com.gradleup.shadow") version "9.1.0"
}

group = "com.cesarcosmico"
version = "2.2.1"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

repositories {
    mavenCentral()
    maven {
        name = "papermc-repo"
        setUrl("https://repo.papermc.io/repository/maven-public/")
    }
    maven {
        name = "jitpack"
        setUrl("https://jitpack.io")
    }
    maven {
        name = "jsinco-releases"
        setUrl("https://repo.jsinco.dev/releases")
    }
    maven {
        name = "breweryteam"
        setUrl("https://repo.breweryteam.dev/releases")
    }
    maven {
        name = "extendedclip"
        setUrl("https://repo.extendedclip.com/releases/")
    }
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.11-R0.1-SNAPSHOT")
    compileOnly("com.github.MilkBowl:VaultAPI:1.7.1")
    compileOnly("dev.jsinco.brewery:thebrewingproject-api:3.0.0")
    compileOnly("dev.jsinco.brewery:thebrewingproject-bukkit:3.0.0")
    compileOnly("com.dre.brewery:BreweryX:3.6.5")
    compileOnly("me.clip:placeholderapi:2.12.2")

    // Bundled via shadowJar
    implementation("com.zaxxer:HikariCP:7.0.2")
    implementation("com.mysql:mysql-connector-j:8.0.33")
    implementation("org.mariadb.jdbc:mariadb-java-client:3.5.7")

    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar> {
    archiveClassifier.set("")

    relocate("com.zaxxer.hikari", "com.cesarcosmico.thebrewingmarket.lib.hikari")
    relocate("com.mysql", "com.cesarcosmico.thebrewingmarket.lib.mysql")
    relocate("org.mariadb", "com.cesarcosmico.thebrewingmarket.lib.mariadb")

    mergeServiceFiles()
}
