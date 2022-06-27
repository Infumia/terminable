import com.diffplug.spotless.LineEnding

plugins {
  java
  `java-library`
  `maven-publish`
  signing
  id("com.diffplug.spotless") version "6.7.2"
  id("io.github.gradle-nexus.publish-plugin") version "1.1.0"
}

val signRequired = !rootProject.property("dev").toString().toBoolean()

group = "tr.com.infumia"

java {
  toolchain {
    languageVersion.set(JavaLanguageVersion.of(17))
  }
}

tasks {
  compileJava {
    options.encoding = Charsets.UTF_8.name()
  }

  javadoc {
    options.encoding = Charsets.UTF_8.name()
    (options as StandardJavadocDocletOptions).tags("todo")
  }

  val javadocJar by creating(Jar::class) {
    dependsOn("javadoc")
    from(javadoc)
  }

  val sourcesJar by creating(Jar::class) {
    dependsOn("classes")
    from(sourceSets["main"].allSource)
  }

  build {
    dependsOn(jar)
    dependsOn(sourcesJar)
    dependsOn(javadocJar)
  }
}

repositories {
  mavenCentral()
  maven("https://oss.sonatype.org/content/repositories/snapshots/")
  mavenLocal()
}

dependencies {
  compileOnlyApi(libs.lombok)
  compileOnlyApi(libs.annotations)

  annotationProcessor(libs.lombok)
  annotationProcessor(libs.annotations)

  testAnnotationProcessor(libs.lombok)
  testAnnotationProcessor(libs.annotations)
}

spotless {
  lineEndings = LineEnding.UNIX
  isEnforceCheck = false

  java {
    importOrder()
    removeUnusedImports()
    endWithNewline()
    indentWithSpaces(2)
    trimTrailingWhitespace()
    prettier(
      mapOf(
        "prettier" to "2.7.1",
        "prettier-plugin-java" to "1.6.2"
      )
    ).config(
      mapOf(
        "parser" to "java",
        "tabWidth" to 2,
        "useTabs" to false
      )
    )
  }
}

publishing {
  publications {
    val publication = create<MavenPublication>("mavenJava") {
      groupId = project.group.toString()
      artifactId = "terminable"
      version = project.version.toString()

      from(components["java"])
      artifact(tasks["sourcesJar"])
      artifact(tasks["javadocJar"])
      pom {
        name.set("Salmi")
        description.set("Fully Customizable Tab plugin for Velocity servers.")
        url.set("https://infumia.com.tr/")
        licenses {
          license {
            name.set("MIT License")
            url.set("https://mit-license.org/license.txt")
          }
        }
        developers {
          developer {
            id.set("portlek")
            name.set("Hasan Demirtaş")
            email.set("utsukushihito@outlook.com")
          }
        }
        scm {
          connection.set("scm:git:git://github.com/infumia/salmi.git")
          developerConnection.set("scm:git:ssh://github.com/infumia/salmi.git")
          url.set("https://github.com/infumia/salmi")
        }
      }
    }

    signing {
      isRequired = signRequired
      if (isRequired) {
        useGpgCmd()
        sign(publication)
      }
    }
  }
}


nexusPublishing {
  repositories {
    sonatype()
  }
}
