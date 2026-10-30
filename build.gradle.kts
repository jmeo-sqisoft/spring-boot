plugins {
	java
	jacoco
	checkstyle
	war
	id("org.springframework.boot") version "4.1.1"
	id("io.spring.dependency-management") version "1.1.7"
	id("org.sonarqube") version "6.3.1.5724"
}

group = "com.stagora"
version = "0.0.1-SNAPSHOT"
description = "Local REST API"

java {
	toolchain {
		languageVersion.set(JavaLanguageVersion.of(21))
	}
}

configurations {
	compileOnly {
		extendsFrom(configurations.annotationProcessor.get())
	}
}

// 정적 스타일 검사. 규칙: config/checkstyle/checkstyle.xml (Checkstyle 공식 DTD 기반)
checkstyle {
	toolVersion = "14.3.0"
	maxWarnings = Int.MAX_VALUE
	maxErrors = 0
	configDirectory = rootProject.file("config/checkstyle")
}

repositories {
	mavenCentral()
}

val mybatisVersion = "4.0.1"
val springdocVersion = "3.0.3"
val p6spyVersion = "3.9.1"
val lombokVersion = "1.18.46"
val mapstructVersion = "1.6.3"
val lombokMapstructBindingVersion = "0.2.0"
val postgresqlDriverVersion = "42.7.13"

extra["netty.version"] = "4.2.17.Final"
ext["tomcat.version"] = "11.0.25"
// Keep the dependency version overrides from the reference project.
extra["jackson-2-bom.version"] = "2.21.7"
extra["jackson-bom.version"] = "3.1.7"

dependencies {

	// Web
	implementation("org.springframework.boot:spring-boot-starter-web")

	// Actuator
	implementation("org.springframework.boot:spring-boot-starter-actuator")

	// MyBatis (brings spring-boot-starter-jdbc + HikariCP transitively)
	implementation("org.mybatis.spring.boot:mybatis-spring-boot-starter:$mybatisVersion")

	// JPA shares the primary localDataSource with MyBatis.
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")

	// p6spy SQL logging. The old gavlyukovskiy datasource-decorator-spring-boot-starter
	// (which auto-wraps the DataSource) is not compatible with Spring Boot 4's
	// autoconfiguration ordering, so the DataSource is wrapped manually in
	// global/config/DataSourceConfig instead.
	implementation("p6spy:p6spy:$p6spyVersion")

	// springdoc-openapi (Swagger UI)
	implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:$springdocVersion")

	// PostgreSQL driver
	runtimeOnly("org.postgresql:postgresql:$postgresqlDriverVersion")

	// Lombok
	compileOnly("org.projectlombok:lombok:$lombokVersion")
	annotationProcessor("org.projectlombok:lombok:$lombokVersion")

	// MapStruct: Lombok이 생성한 접근자를 인식하도록 binding processor를 함께 설정한다.
	implementation("org.mapstruct:mapstruct:$mapstructVersion")
	annotationProcessor("org.mapstruct:mapstruct-processor:$mapstructVersion")
	annotationProcessor("org.projectlombok:lombok-mapstruct-binding:$lombokMapstructBindingVersion")

	// Test
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
	testCompileOnly("org.projectlombok:lombok:$lombokVersion")
	testAnnotationProcessor("org.projectlombok:lombok:$lombokVersion")
	testAnnotationProcessor("org.mapstruct:mapstruct-processor:$mapstructVersion")
	testAnnotationProcessor("org.projectlombok:lombok-mapstruct-binding:$lombokMapstructBindingVersion")
}

tasks.named<Test>("test") {
	useJUnitPlatform()
}

tasks.bootJar {
	enabled = true
}

tasks.assemble {
	dependsOn(tasks.bootJar)
}

// SonarQube 커버리지 연동용 XML 리포트
tasks.jacocoTestReport {
	dependsOn(tasks.test)
	reports {
		xml.required.set(true)
		html.required.set(true)
	}
}

// host.url/token은 실행 환경에서 -D 플래그로 주입
sonar {
	properties {
		property("sonar.projectKey", "spring-boot")
		property("sonar.projectName", "Local REST API")
		property("sonar.java.coveragePlugin", "jacoco")
		property("sonar.coverage.jacoco.xmlReportPaths", "build/reports/jacoco/test/jacocoTestReport.xml")
	}
}

tasks.named("sonar").configure { dependsOn(tasks.jacocoTestReport) }

tasks.compileJava {
	options.encoding = "UTF-8"
}

tasks.compileTestJava {
	options.encoding = "UTF-8"
}
