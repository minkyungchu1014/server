plugins {
	java
	id("org.springframework.boot") version "3.2.0"
	id("io.spring.dependency-management") version "1.1.7"
}

fun getGitHash(): String {
	return providers.exec {
		commandLine("git", "rev-parse", "--short", "HEAD")
	}.standardOutput.asText.get().trim()
}

group = "kr.hhplus.be"
version = getGitHash()

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(17)
	}
}

repositories {
	mavenCentral()
}

dependencyManagement {
	imports {
		mavenBom("org.springframework.cloud:spring-cloud-dependencies:2024.0.0")
	}
}

dependencies {
    // Spring
	implementation("org.springframework.boot:spring-boot-starter-actuator")
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("com.zaxxer:HikariCP:5.0.1") // HikariCP 의존성 추가
	implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.2.0")

    // DB
	runtimeOnly("com.mysql:mysql-connector-j")

	// lombok
	compileOnly("org.projectlombok:lombok:1.18.28") // 최신 버전 확인
	annotationProcessor("org.projectlombok:lombok:1.18.28")

	// Test
    testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("org.springframework.boot:spring-boot-testcontainers")
	testImplementation("org.testcontainers:junit-jupiter")
	testImplementation("org.testcontainers:mysql")
	testImplementation("io.rest-assured:rest-assured")
	testImplementation("org.testcontainers:jdbc:1.19.3")
}

tasks.withType<JavaCompile> {
	options.annotationProcessorPath = configurations["annotationProcessor"]
}
tasks.withType<Test> {
	useJUnitPlatform()
	systemProperty("spring.profiles.active", "test")
}
tasks.withType<Test> {
	useJUnitPlatform()
	systemProperty("user.timezone", "UTC")
}
