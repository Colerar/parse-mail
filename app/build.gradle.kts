plugins {
  kotlin("jvm")
  kotlin("plugin.serialization")
  id("com.github.gmazzo.buildconfig") version "3.1.0"
  id("com.github.johnrengelman.shadow") version "7.1.2"
  application
}

dependencies {
  implementation(platform("org.jetbrains.kotlin:kotlin-bom"))
  implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

  implementation("io.github.microutils:kotlin-logging-jvm:2.1.23")
  implementation("ch.qos.logback:logback-classic:1.4.0")
  implementation("org.apache.logging.log4j:log4j-core:2.18.0")

  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")

  implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.4.0")
  implementation("com.github.doyaaaaaken:kotlin-csv-jvm:1.6.0")

  implementation("com.squareup.okio:okio:3.2.0")

  implementation("com.github.ajalt.clikt:clikt:3.5.0")

  implementation("org.simplejavamail:simple-java-mail:7.5.0")
  implementation("org.simplejavamail:outlook-module:7.5.0")

  testImplementation("org.jetbrains.kotlin:kotlin-test")
  testImplementation("org.jetbrains.kotlin:kotlin-test-junit")
}

application {
  applicationName = "parse-mail"
  mainClass.set("moe.sdl.parsemail.MainKt")
}
