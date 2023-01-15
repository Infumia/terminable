import com.diffplug.gradle.spotless.SpotlessExtension
import com.diffplug.spotless.LineEnding

plugins {
  java
  `java-library`
  `maven-publish`
  signing
  id("com.diffplug.spotless") version "6.13.0"
  id("io.github.gradle-nexus.publish-plugin") version "1.1.0"
}

val signRequired = !rootProject.property("dev").toString().toBoolean()

group = "tr.com.infumia"

java { toolchain { languageVersion.set(JavaLanguageVersion.of(17)) } }

tasks {
  compileJava { options.encoding = Charsets.UTF_8.name() }

  javadoc {
    options.encoding = Charsets.UTF_8.name()
    (options as StandardJavadocDocletOptions).tags("todo")
  }

  val javadocJar by
      creating(Jar::class) {
        dependsOn("javadoc")
        archiveClassifier.set("javadoc")
        from(javadoc)
      }

  val sourcesJar by
      creating(Jar::class) {
        dependsOn("classes")
        archiveClassifier.set("sources")
        from(sourceSets["main"].allSource)
      }

  build {
    dependsOn(spotlessApply)
    dependsOn(jar)
    dependsOn(sourcesJar)
    dependsOn(javadocJar)
  }
}

repositories { mavenCentral() }

dependencies {
  compileOnlyApi(libs.lombok)
  compileOnlyApi(libs.annotations)

  annotationProcessor(libs.lombok)
  annotationProcessor(libs.annotations)

  testAnnotationProcessor(libs.lombok)
  testAnnotationProcessor(libs.annotations)
}

val spotlessApply = property("spotless.apply").toString().toBoolean()

if (spotlessApply) {
  configure<SpotlessExtension> {
    lineEndings = LineEnding.UNIX
    isEnforceCheck = false

    format("encoding") {
      target("*.*")
      encoding("UTF-8")
      endWithNewline()
      trimTrailingWhitespace()
    }

    kotlinGradle {
      target("**/*.gradle.kts")
      endWithNewline()
      indentWithSpaces(2)
      trimTrailingWhitespace()
      ktfmt("0.42")
    }

    java {
      target("**/src/**/java/**/*.java")
      importOrder()
      removeUnusedImports()
      endWithNewline()
      indentWithSpaces(2)
      trimTrailingWhitespace()
      prettier(mapOf("prettier" to "2.7.1", "prettier-plugin-java" to "1.6.2"))
          .config(
              mapOf("parser" to "java", "tabWidth" to 2, "useTabs" to false, "printWidth" to 100))
    }
  }
}

publishing {
  publications {
    val publication =
        create<MavenPublication>("mavenJava") {
          groupId = project.group.toString()
          artifactId = "terminable"
          version = project.version.toString()

          from(components["java"])
          artifact(tasks["sourcesJar"])
          artifact(tasks["javadocJar"])
          pom {
            name.set("Terminable")
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
              connection.set("scm:git:git://github.com/infumia/terminable.git")
              developerConnection.set("scm:git:ssh://github.com/infumia/terminable.git")
              url.set("https://github.com/infumia/terminable")
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

nexusPublishing { repositories { sonatype() } }
