plugins {
    id("java")
    id("com.gradleup.shadow") version "9.4.1"
}

group = "com.cesarcosmico"
version = "3.0.0"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
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
    compileOnly("io.papermc.paper:paper-api:26.2.build.112-stable")
    compileOnly("com.github.MilkBowl:VaultAPI:1.7.1")
    compileOnly("dev.jsinco.brewery:thebrewingproject-api:3.3.3")
    compileOnly("dev.jsinco.brewery:thebrewingproject-bukkit:3.3.3")
    compileOnly("com.dre.brewery:BreweryX:3.7.0")
    compileOnly("me.clip:placeholderapi:2.12.3")

    // Provided at runtime by the PluginLoader (see TheBrewingMarketLoader)
    compileOnly("com.zaxxer:HikariCP:7.1.0")

    implementation("org.bstats:bstats-bukkit:3.2.1")
}

tasks.withType<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar> {
    archiveClassifier.set("")

    relocate("org.bstats", "com.cesarcosmico.thebrewingmarket.lib.bstats")

    mergeServiceFiles()
}