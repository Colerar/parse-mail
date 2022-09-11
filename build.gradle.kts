import org.gradle.internal.os.OperatingSystem
import org.jlleitschuh.gradle.ktlint.KtlintExtension
import java.nio.file.Files

plugins {
  kotlin("jvm") version "1.7.10" apply false
  kotlin("plugin.serialization") version "1.7.10" apply false
  id("org.jlleitschuh.gradle.ktlint") version "11.0.0"
  id("org.jlleitschuh.gradle.ktlint-idea") version "11.0.0"
}

subprojects {
  apply(plugin = "org.jlleitschuh.gradle.ktlint")
  configure<KtlintExtension> {
    disabledRules.set(setOf("no-wildcard-imports", "import-ordering"))
    filter {
      fun exclude(path: String) = exclude {
        projectDir.toURI().relativize(it.file.toURI()).normalize().path.contains(path)
      }
      setOf("/generated/", "/build/", "resources").forEach { exclude(it) }
    }
  }
}

allprojects {
  repositories {
    mavenCentral()
  }
  version = "0.1.0"
}

fun installGitHooks() {
  val projectDir = project.rootProject.rootDir
  if (!File(projectDir, ".git").exists()) return
  val target = File(projectDir, ".git/hooks")
  val source = File(projectDir, ".git-hooks")
  if (target.canonicalFile == source) return
  target.deleteRecursively()
  if (OperatingSystem.current().isWindows) {
    source.copyRecursively(target, overwrite = true)
  } else {
    Files.createSymbolicLink(target.toPath(), source.toPath())
  }
}
installGitHooks()
