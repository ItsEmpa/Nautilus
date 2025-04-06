import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    idea
    kotlin("jvm")
    java
}
repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation("com.google.devtools.ksp:symbol-processing-api:2.0.20-1.0.25")

    implementation("com.squareup:kotlinpoet:1.13.0")
    implementation("com.squareup:kotlinpoet-ksp:1.13.0")
}

tasks.withType<JavaCompile> {
    options.release.set(8)
}

val compileKotlin: KotlinCompile by tasks
compileKotlin.kotlinOptions {
    jvmTarget = "1.8"
}
