plugins {
  kotlin("jvm")
  kotlin("plugin.serialization")
  id("com.github.gmazzo.buildconfig")
  id("org.jlleitschuh.gradle.ktlint")
  id("org.jlleitschuh.gradle.ktlint-idea")

  id("com.github.johnrengelman.shadow")
  application
}

dependencies {
  implementation(platform(libs.kotlin.bom))
  implementation(libs.kotlin.stdlib.jdk8)

  implementation(libs.kotlin.logging.jvm)
  implementation(libs.logback.classic)
  implementation(libs.kotlinx.coroutines.core)
  implementation(libs.kotlin.csv.jvm)

  implementation(libs.clikt)

  implementation(libs.simple.java.mail)

  testImplementation(libs.kotlin.test)
  testImplementation(libs.kotlin.test.junit)
}

application {
  applicationName = "parse-mail"
  mainClass.set("moe.sdl.parsemail.AppKt")
}
