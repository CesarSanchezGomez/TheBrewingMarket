plugins {
    id("java")
    id("com.gradleup.shadow") version "9.1.0"
}

group = "com.cesarcosmico"
version = "1.1.0"

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
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.11-R0.1-SNAPSHOT")
    compileOnly("com.github.MilkBowl:VaultAPI:1.7.1")
    compileOnly("com.github.BreweryTeam:TheBrewingProject:v3.0.0-beta.0")

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

    relocate("com.zaxxer.hikari", "com.cesarcosmico.brewmarket.lib.hikari")
    relocate("com.mysql", "com.cesarcosmico.brewmarket.lib.mysql")
    relocate("org.mariadb", "com.cesarcosmico.brewmarket.lib.mariadb")

    mergeServiceFiles()
}