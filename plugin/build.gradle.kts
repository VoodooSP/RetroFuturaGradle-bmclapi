import com.github.jengelman.gradle.plugins.shadow.tasks.ConfigureShadowRelocation
import java.util.*

/*
 * This file was generated by the Gradle 'init' task.
 *
 * This generated file contains a sample Gradle plugin project to get you started.
 * For more details take a look at the Writing Custom Plugins chapter in the Gradle
 * User Manual available at https://docs.gradle.org/7.6/userguide/custom_plugins.html
 */

plugins {
  // Apply the Java Gradle plugin development plugin to add support for developing Gradle plugins
  id("java-gradle-plugin")
  id("com.github.johnrengelman.shadow") version "7.1.2"
  id("com.palantir.git-version") version "0.15.0"
  id("maven-publish")
  id("com.diffplug.spotless") version "6.12.0"
}

repositories {
  maven {
    name = "forge"
    url = uri("https://maven.minecraftforge.net")
    mavenContent {
      includeGroup("net.minecraftforge")
      includeGroup("net.minecraftforge.srg2source")
      includeGroup("de.oceanlabs.mcp")
      includeGroup("cpw.mods")
    }
  }
  maven {
    // because Srg2Source needs an eclipse dependency.
    name = "eclipse"
    url = uri("https://repo.eclipse.org/content/groups/eclipse/")
    mavenContent { includeGroup("org.eclipse.jdt") }
  }
  maven {
    name = "mojang"
    url = uri("https://libraries.minecraft.net/")
    mavenContent {
      includeGroup("com.ibm.icu")
      includeGroup("com.mojang")
      includeGroup("com.paulscode")
      includeGroup("org.lwjgl.lwjgl")
      includeGroup("tv.twitch")
      includeGroup("net.minecraft")
    }
  }
  mavenCentral {}
  gradlePluginPortal()
}

val gitVersion: groovy.lang.Closure<String> by extra

group = "com.gtnewhorizons"

version = gitVersion().removeSuffix(".dirty")

dependencies {
  shadow(localGroovy())
  shadow(gradleApi())
  // Apache Commons utilities
  implementation("org.apache.commons:commons-lang3:3.12.0")
  implementation("commons-io:commons-io:2.11.0")
  implementation("commons-codec:commons-codec:1.15")
  implementation("org.apache.commons:commons-compress:1.22")
  // Guava utilities
  implementation("com.google.guava:guava:31.1-jre")
  // CSV reader, also used by SpecialSource
  implementation("com.opencsv:opencsv:5.7.1")
  // Diffing&Patching
  implementation("org.ow2.asm:asm:9.4")
  implementation("com.cloudbees:diff4j:1.1")
  implementation("com.github.jponge:lzma-java:1.3")
  implementation("net.md-5:SpecialSource:1.11.0")
  // "MCP stuff"
  implementation(project(":oldasmwrapper", "shadow"))
  // Startup classes
  compileOnly("com.mojang:authlib:1.5.16") { isTransitive = false }
  compileOnly("net.minecraft:launchwrapper:1.12") { isTransitive = false }
  // Provides a file-downloading task implementation for Gradle
  implementation(
      group = "de.undercouch.download",
      name = "de.undercouch.download.gradle.plugin",
      version = "5.3.0")
  // JSON handling for Minecraft manifests etc.
  implementation("com.google.code.gson:gson:2.10")
  // Forge utilities (to be merged into the source tree in the future)

  // Use JUnit Jupiter for testing.
  testImplementation("org.junit.jupiter:junit-jupiter:5.9.1")

  constraints {
    implementation("org.apache.logging.log4j:log4j-core") {
      version {
        strictly("[2.17, 3[")
        prefer("2.19.0")
      }
      because(
          "CVE-2021-44228, CVE-2021-45046, CVE-2021-45105: Log4j vulnerable to remote code execution and other critical security vulnerabilities")
    }
  }
}

val depGradleApi = dependencies.gradleApi()

configurations.api.configure { dependencies.remove(depGradleApi) }

java {
  toolchain {
    languageVersion.set(JavaLanguageVersion.of(8))
    vendor.set(JvmVendorSpec.ADOPTIUM)
  }
}

tasks.named<org.gradle.jvm.tasks.Jar>("jar").configure { from("LICENSE", "docs") }

spotless {
  encoding("UTF-8")
  java {
    toggleOffOn()
    importOrder()
    removeUnusedImports()
    palantirJavaFormat("1.1.0")
  }
  kotlinGradle {
    toggleOffOn()
    ktfmt("0.39")
    trimTrailingWhitespace()
    indentWithSpaces(4)
    endWithNewline()
  }
}

gradlePlugin {
  // Define the plugin
  plugins {
    website.set("https://github.com/GTNewHorizons/RetroFuturaGradle")
    vcsUrl.set("https://github.com/GTNewHorizons/RetroFuturaGradle.git")
    isAutomatedPublishing = false
    create("userDev") {
      id = "com.gtnewhorizons.retrofuturagradle"
      implementationClass = "com.gtnewhorizons.retrofuturagradle.UserDevPlugin"
      displayName = "RetroFuturaGradle"
      description = "Provides a Minecraft 1.7.10 and Forge modding toolchain"
      tags.set(listOf("minecraft", "modding"))
    }
  }
}

java {
  sourceSets {
    // Add the GradleStart tree as sources for IDE support
    create("gradleStart") {
      compileClasspath = configurations.compileClasspath.get()
      java {
        setSrcDirs(Collections.emptyList<String>())
        source(sourceSets.main.get().resources)
      }
    }
  }
}

// Relocate all dependencies into a subpackage
// https://imperceptiblethoughts.com/shadow/configuration/relocation/#filtering-relocation
val taskRelocateShadowJar =
    tasks.register<ConfigureShadowRelocation>("relocateShadowJar") {
      target = tasks.shadowJar.get()
      prefix = "com.gtnewhorizons.retrofuturagradle.shadow"
    }

tasks.shadowJar.configure {
  dependsOn(taskRelocateShadowJar)
  archiveClassifier.set("")
}

tasks.jar.configure { enabled = false }

// Add a source set for the functional test suite
val functionalTestSourceSet = sourceSets.create("functionalTest") {}

configurations["functionalTestImplementation"].extendsFrom(configurations["testImplementation"])

// Add a task to run the functional tests
val functionalTest by
    tasks.registering(Test::class) {
      testClassesDirs = functionalTestSourceSet.output.classesDirs
      classpath = functionalTestSourceSet.runtimeClasspath
      useJUnitPlatform()
    }

gradlePlugin.testSourceSets(functionalTestSourceSet)

tasks.named<Task>("check") {
  // Run the functional tests as part of `check`
  dependsOn(functionalTest)
}

tasks.named<Test>("test") {
  // Use JUnit Jupiter for unit tests.
  useJUnitPlatform()
}

publishing {
  publications {
    create<MavenPublication>("retrofuturagradle") { shadow.component(this) }
    // From org.gradle.plugin.devel.plugins.MavenPluginPublishPlugin.createMavenMarkerPublication
    for (declaration in gradlePlugin.plugins) {
      create<MavenPublication>(declaration.name + "PluginMarkerMaven") {
        artifactId = declaration.id + ".gradle.plugin"
        groupId = declaration.id
        pom {
          name.set(declaration.displayName)
          description.set(declaration.description)
          withXml {
            val root = asElement()
            val document = root.ownerDocument
            val dependencies = root.appendChild(document.createElement("dependencies"))
            val dependency = dependencies.appendChild(document.createElement("dependency"))
            val groupId = dependency.appendChild(document.createElement("groupId"))
            groupId.textContent = project.group.toString()
            val artifactId = dependency.appendChild(document.createElement("artifactId"))
            artifactId.textContent = project.name
            val version = dependency.appendChild(document.createElement("version"))
            version.textContent = project.version.toString()
          }
        }
      }
    }
  }
}
