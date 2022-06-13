

group = "com.elvarg"

repositories {
    mavenCentral()
}


plugins {
    id("java")
    kotlin("jvm") version "1.3.72"
    kotlin("plugin.lombok") version "1.5.21"
}


dependencies {
    // https://mvnrepository.com/artifact/com.google.guava/guava
    implementation("com.google.guava:guava:31.1-jre")
    // https://mvnrepository.com/artifact/org.apache.commons/commons-lang3
    implementation("org.apache.commons:commons-lang3:3.12.0")
    // https://mvnrepository.com/artifact/com.google.code.gson/gson
    implementation("com.google.code.gson:gson:2.9.0")
    // https://mvnrepository.com/artifact/io.netty/netty-all
    implementation("io.netty:netty-all:5.0.0.Alpha2")



}
