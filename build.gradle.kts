plugins {
    `java-library`
}

group = "net.hollowcube"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven(url = "https://jitpack.io")
}

dependencies {
    compileOnlyApi(libs.minestom)
    testImplementation(libs.minestom)

    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation(libs.bundles.logback)
}

tasks.test {
    useJUnitPlatform()
}