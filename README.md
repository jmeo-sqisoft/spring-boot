# spring-boot

참조 프로젝트의 공통 설정을 바탕으로 만든 Spring Boot + MyBatis + JPA + Lombok 기반 Java 프로젝트입니다.
업무 Java, Entity, Repository, Mapper XML과 `report-core`는 포함하지 않습니다.

## 구성

- JDK 21 (LTS), Spring Boot 4.1.1, Gradle 9.6.1 (Kotlin DSL)
- MyBatis 4.0.1, Spring Data JPA 4.1.1, PostgreSQL 42.7.13
- HikariCP 7.0.2, p6spy 3.9.1 (DataSource 수동 래핑, JPA SQL 포맷 유지)
- Lombok 1.18.46, springdoc-openapi 3.0.3 (Swagger UI), Actuator 4.1.1
- Checkstyle 13.5.0, JaCoCo, SonarQube Gradle Plugin 6.3.1.5724
- 한국어/영어 메시지와 CORS 공통 설정

group: `com.seegene.local`, artifact: `spring-boot`

버전과 의존성 override는 참조 프로젝트의 실제 빌드 파일을 기준으로 유지했습니다.
Spring Data JPA, HikariCP, Actuator 버전은 Spring Boot 4.1.1의 의존성 관리로 결정되며, 현재 빌드 산출물에서 확인한 버전입니다.
Gradle Wrapper 스크립트와 JAR가 포함되어 있습니다. 최초 빌드에는 배포본과 의존성을 받을 네트워크 연결이 필요합니다.

## 실행 전 준비

JDK 21과 PostgreSQL을 준비하고 사용할 DB와 계정을 생성합니다.
Key Vault 대신 환경 변수로 접속 정보를 전달합니다. PowerShell 예시:

```powershell
$env:BE_DB_URL = 'jdbc:postgresql://localhost:5432/local'
$env:BE_DB_USER = 'local'
$env:BE_DB_PASSWORD = 'your-password'
$env:SPRING_PROFILES_ACTIVE = 'dev'
.\gradlew.bat bootRun
```

| 환경 변수 | 기본값 / 용도 |
| --- | --- |
| `BE_DB_URL` | `jdbc:postgresql://localhost:5432/local` |
| `BE_DB_USER` | `local` |
| `BE_DB_PASSWORD` | 빈 문자열; 실행 환경에서 설정 |
| `BE_DB_SCHEMA` | `public` |
| `SERVER_PORT` | `8080` |
| `BE_DB_MAX_POOL_SIZE` / `BE_DB_MIN_IDLE` | `10` / `2` |
| `BE_DB_CONN_TIMEOUT` | `30000` ms |
| `BE_DB_IDLE_TIMEOUT` / `BE_DB_MAX_LIFETIME` | `600000` / `1800000` ms |
| `BE_DB_VALID_TIMEOUT` | `5000` ms |
| `BE_DB_CONNECTION_INIT_SQL` | UTC 및 읽기 전용 트랜잭션 설정 |

풀 설정은 원본처럼 `BE_DB_READ_*` 변수가 `BE_DB_*`보다 우선합니다.
`spring.datasource.local` 설정 키로 DataSource를 구성합니다.
MyBatis와 JPA가 이 DataSource를 공유합니다.

원본의 읽기 전용 연결 초기화 SQL을 유지했습니다. 쓰기 기능이 필요하면 다음과 같이 변경합니다.

```powershell
$env:BE_DB_CONNECTION_INIT_SQL = "SET timezone = 'UTC'; SET default_transaction_read_only = off"
```

JPA의 `ddl-auto: none`, `generate-ddl: false`를 유지하므로 테이블은 자동 생성하지 않습니다.
실제 실행에는 접근 가능한 PostgreSQL이 필요합니다.

## 프로파일

공통 `application.yml` 위에 `application-{profile}.yml`이 적용됩니다.
미지정 시 공통 설정만 사용합니다.

| 프로파일 | SQL 로깅 | 애플리케이션 로그 | Swagger |
| --- | --- | --- | --- |
| 공통 / dev | 활성 | debug | 활성 |
| uat | 활성 | debug | 비활성 |
| prd | 비활성 | warn | 비활성 |

`stg`, `perf`와 Azure Key Vault, Managed Identity, JWE 관련 의존성 및 설정은 제외했습니다.

## 확인

- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/local-api-docs`
- Actuator: `http://localhost:8080/actuator/health`
- Liveness / Readiness: `/actuator/health/liveness`, `/actuator/health/readiness`

업무 Controller가 없어 Swagger에 업무 API가 표시되지 않는 것이 정상입니다.
Readiness는 원본처럼 `readinessState`만 확인하며 DB 연결 상태를 포함하지 않습니다.

## 패키지 구조

```text
com.seegene.local
├── Application.java
└── global/config/
    ├── AppProperties.java
    ├── CorsConfig.java
    ├── DataSourceConfig.java
    ├── JpaAwareSqlLogFormatter.java
    ├── LocaleConfig.java
    └── OpenApiConfig.java

src/main/resources/
├── application.yml
├── application-dev.yml
├── application-uat.yml
├── application-prd.yml
└── messages/
    ├── messages.properties
    ├── messages_ko.properties
    └── messages_en.properties
```

새 Java 소스는 `com.seegene.local` 아래에 작성합니다. MyBatis는 `@Mapper` 인터페이스를 탐색하며
XML은 `src/main/resources/mapper/` 아래에 추가합니다. 별칭 패키지는 `com.seegene.local`입니다.
JPA Entity와 Repository도 같은 루트 아래에서 자동 탐색합니다.

메시지는 원본 그대로 유지했습니다. `MessageSource.getMessage(key, args, locale)`로 조회하며
기본 로케일은 한국어입니다. CORS 허용 Origin은 `app.cors.allowed-origin-patterns`로 설정하고,
기본값은 localhost입니다. 원본 구현처럼 CORS 필터는 모든 프로파일에서 등록됩니다.

## 빌드 및 검증

```powershell
.\gradlew.bat build jacocoTestReport
```

테스트는 dev/uat/prd 각각의 컨텍스트, DataSource 공유, SQL 로깅 전환, Swagger 설정,
메시지 로딩을 확인합니다. 테스트에서만 JDBC 메타데이터 조회를 끄므로 DB 없이 실행할 수 있습니다.
실제 PostgreSQL 쿼리는 별도 확인이 필요합니다.

- 실행 JAR: `build/libs/spring-boot-0.0.1-SNAPSHOT.jar`
- 실행 WAR: `build/libs/spring-boot-0.0.1-SNAPSHOT.war`
- 테스트: `build/reports/tests/test/index.html`
- Checkstyle: `build/reports/checkstyle/`
- JaCoCo: `build/reports/jacoco/test/html/index.html`

```powershell
java -jar build/libs/spring-boot-0.0.1-SNAPSHOT.jar --spring.profiles.active=dev
```

WAR 플러그인은 원본처럼 유지했습니다. 외부 컨테이너용 초기화 코드는 포함하지 않습니다.
SonarQube 서버와 토큰은 별도로 설정해야 하며 CI 워크플로는 포함하지 않았습니다.
Gradle 홈이 `C:\.gradle`로 잡히는 환경에서는 `--gradle-user-home C:\Users\user\.gradle`을 추가합니다.
