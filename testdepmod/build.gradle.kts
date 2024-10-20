buildscript {
    repositories {
        maven {
            name = "forge"
            url = uri("https://bmclapi2.bangbang93.com/maven")
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
            url = uri("https://bmclapi2.bangbang93.com/maven")
            mavenContent {
                includeGroup("com.ibm.icu")
                includeGroup("com.mojang")
                includeGroup("com.paulscode")
                includeGroup("org.lwjgl.lwjgl")
                includeGroup("tv.twitch")
                includeGroup("net.minecraft")
            }
        }
        maven {
            name = "paper"
            url = uri("https://papermc.io/repo/repository/maven-snapshots/")
            mavenContent {
                includeGroup("org.cadixdev")
            }
        }
        maven {
            name = "sonatype"
            url = uri("https://oss.sonatype.org/content/repositories/snapshots/")
            mavenContent {
                includeGroup("org.cadixdev")
            }
        }
        maven {
            name = "maven-mirror"
            url = uri("https://repo.huaweicloud.com/repository/maven/")
        }
        //mavenCentral {}
    }
}

repositories {
    maven {
        url = uri("https://cursemaven.com")
        content {
            includeGroup("curse.maven")
        }
    }
    mavenLocal()
}

dependencies {
    api(project(":testmod"))
    // Uncomment for manual testing with local maven
    // api("testmod:testmod:1.0:dev")
    // For manual testing of dependency code
    // api("curse.maven:nei-358228:4275622-dev-sources")
    // api("com.cloudbees:diff4j:1.1")
}

plugins {
    id("com.gtnewhorizons.retrofuturagradle")
    id("maven-publish")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(8))
        vendor.set(JvmVendorSpec.AZUL)
    }
    withSourcesJar()
    withJavadocJar()
}

minecraft {
    skipSlowTasks.set(true)
}

group = "testdepmod"
version = "1.0"

publishing {
    publications {
        create<MavenPublication>("testdepmod") {
            from(components["java"])
        }
    }
}
