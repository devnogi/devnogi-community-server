plugins {
	java
	pmd
	id("org.springframework.boot") version "3.5.0"
	id("io.spring.dependency-management") version "1.1.7"
	id("org.asciidoctor.jvm.convert") version "3.3.2"
	id("com.diffplug.spotless") version "6.25.0"
}

group = "until.the.eternity"
version = "1.0.0-SNAPSHOT"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(21)
	}
}

configurations {
	compileOnly {
		extendsFrom(configurations.annotationProcessor.get())
	}
}

repositories {
	mavenCentral()
	gradlePluginPortal()
}

extra["snippetsDir"] = file("build/generated-snippets")

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("com.querydsl:querydsl-jpa:5.0.0:jakarta")
	implementation("org.springframework.boot:spring-boot-starter-data-redis")
	implementation("org.springframework.boot:spring-boot-starter-security")
	implementation("org.springframework.boot:spring-boot-starter-validation")
	implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-aop")
	implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.7.0")
	implementation("org.springframework.boot:spring-boot-starter-data-redis")
    testRuntimeOnly("com.h2database:h2")
	implementation("org.springframework.boot:spring-boot-starter-cache")
	implementation("mysql:mysql-connector-java:8.0.33")
    implementation("io.minio:minio:8.5.7")
	implementation("org.flywaydb:flyway-core")
	implementation("org.flywaydb:flyway-mysql")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    runtimeOnly("mysql:mysql-connector-java")
	implementation("org.springframework.kafka:spring-kafka")
	compileOnly("org.projectlombok:lombok")
	developmentOnly("org.springframework.boot:spring-boot-devtools")
	developmentOnly("org.springframework.boot:spring-boot-docker-compose")
	runtimeOnly("com.mysql:mysql-connector-j")
	annotationProcessor("org.projectlombok:lombok")
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("org.springframework.kafka:spring-kafka-test")
	testImplementation("org.springframework.restdocs:spring-restdocs-mockmvc")
	testImplementation("org.springframework.security:spring-security-test")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    annotationProcessor("com.querydsl:querydsl-apt:5.0.0:jakarta")
    annotationProcessor("jakarta.annotation:jakarta.annotation-api")
    annotationProcessor("jakarta.persistence:jakarta.persistence-api")
}

tasks.withType<Test> {
	useJUnitPlatform()
}

tasks.test {
	useJUnitPlatform {
		excludeTags("redis-integration")
	}
	outputs.dir(project.extra["snippetsDir"]!!)
}

tasks.register<Test>("redisIntegrationTest") {
	group = "verification"
	description = "Run notification integration tests against an explicitly configured Redis 7."
	testClassesDirs = sourceSets["test"].output.classesDirs
	classpath = sourceSets["test"].runtimeClasspath
	useJUnitPlatform {
		includeTags("redis-integration")
	}
	mustRunAfter(tasks.test)
	outputs.upToDateWhen { false }
	doFirst {
		check(!System.getenv("TEST_REDIS_HOST").isNullOrBlank()) {
			"TEST_REDIS_HOST must identify an isolated test Redis; no default or skipped run is allowed."
		}
		val port = System.getenv("TEST_REDIS_PORT")?.toIntOrNull()
		check(port != null && port in 1..65535) {
			"TEST_REDIS_PORT must be an explicit TCP port in 1..65535."
		}
	}
}

tasks.asciidoctor {
	inputs.dir(project.extra["snippetsDir"]!!)
	dependsOn(tasks.test)
}

// Jacoco
apply(from = "./gradle/jacoco.gradle.kts")

tasks.named<JacocoReport>("jacocoTestReport") {
	dependsOn(tasks.test)

	reports {
		xml.required.set(true)
		html.required.set(true)
	}
}

// Spotless
spotless {
	java {
		googleJavaFormat().aosp()
		removeUnusedImports()
		importOrder()
		trimTrailingWhitespace()
		endWithNewline()
	}

	format("misc") {
		target("*.gradle.kts", ".gitignore")
		trimTrailingWhitespace()
		endWithNewline()
	}
}


// Verification must not format sources or regenerate checked-in documentation.
pmd {
    toolVersion = "7.24.0"
    ruleSets = emptyList()
    ruleSetFiles = files("config/pmd/unused.xml")
}

tasks.register("lint") {
    group = "verification"
    description = "Check formatting, imports, and unused code."
    dependsOn("spotlessCheck", "pmdMain", "pmdTest")
}

val enableWorktreeGitConfig = tasks.register<Exec>("enableWorktreeGitConfig") {
    commandLine("git", "config", "--local", "extensions.worktreeConfig", "true")
}

tasks.register<Exec>("installGitHooks") {
    group = "build setup"
    description = "Install Git hooks for this worktree."
    dependsOn(enableWorktreeGitConfig)
    commandLine("git", "config", "--worktree", "core.hooksPath", ".githooks")
}

tasks.register("verify") {
    group = "verification"
    description = "Run tests, configured static checks, and build the application JAR."
    dependsOn("check", "bootJar")
}
