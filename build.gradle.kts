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

// 테스트 소스는 Checkstyle 검사 대상에서 제외한다.
tasks.named("checkstyleTest") {
	enabled = false
}

// 운영 Java와 함께 MyBatis Mapper XML의 SQL 포맷을 검사한다.
tasks.named<Checkstyle>("checkstyleMain") {
	source(fileTree("src/main/resources/mapper") {
		include("**/*.xml")
	})
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
val lucyXssServletVersion = "2.0.1"

extra["netty.version"] = "4.2.17.Final"
ext["tomcat.version"] = "11.0.25"
// Jackson 2와 Jackson 3 계열의 BOM 버전을 각각 지정한다.
extra["jackson-2-bom.version"] = "2.21.7"
extra["jackson-bom.version"] = "3.1.7"

dependencies {

	// Web
	implementation("org.springframework.boot:spring-boot-starter-web")

	// Lucy 규칙 엔진을 Jakarta Servlet용 요청 필터에서 사용한다.
	implementation("com.navercorp.lucy:lucy-xss-servlet:$lucyXssServletVersion")

	// Actuator
	implementation("org.springframework.boot:spring-boot-starter-actuator")

	// MyBatis 의존성을 통해 JDBC starter와 HikariCP를 함께 사용한다.
	implementation("org.mybatis.spring.boot:mybatis-spring-boot-starter:$mybatisVersion")

	// JPA와 MyBatis가 기본 localDataSource를 공유한다.
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")

	// DataSourceConfig에서 DataSource를 P6DataSource로 감싸 SQL 로깅을 구성한다.
	implementation("p6spy:p6spy:$p6spyVersion")

	// springdoc-openapi (Swagger UI)
	implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:$springdocVersion")

	// PostgreSQL JDBC 드라이버
	runtimeOnly("org.postgresql:postgresql:$postgresqlDriverVersion")

	// Lombok
	compileOnly("org.projectlombok:lombok:$lombokVersion")
	annotationProcessor("org.projectlombok:lombok:$lombokVersion")

	// MapStruct: Lombok이 생성한 접근자를 인식하도록 binding processor를 함께 설정한다.
	implementation("org.mapstruct:mapstruct:$mapstructVersion")
	annotationProcessor("org.mapstruct:mapstruct-processor:$mapstructVersion")
	annotationProcessor("org.projectlombok:lombok-mapstruct-binding:$lombokMapstructBindingVersion")

	// 자동 테스트와 테스트 코드의 애너테이션 처리
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

// sonar.host.url과 sonar.token은 실행 환경에서 -D 옵션 등으로 전달한다.
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
