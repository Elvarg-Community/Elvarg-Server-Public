

group = "com.elvarg"

repositories {
    mavenCentral()
}

plugins {
    kotlin("jvm") version "1.3.72"
    kotlin("plugin.lombok") version "1.5.21"
}


dependencies {
    implementation("com.google.guava:guava:31.1-jre")
    implementation("org.apache.commons:commons-lang3:3.12.0")
    implementation("com.google.code.gson:gson:2.9.0")
    implementation("io.netty:netty-all:5.0.0.Alpha2")

}
