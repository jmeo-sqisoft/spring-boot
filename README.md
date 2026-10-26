# spring-boot

Spring Boot + MyBatis + JPA + Lombok 기반 Java 프로젝트입니다.

## 구성

- JDK 21 (LTS), Spring Boot 4.1.1, Gradle 9.6.1 (Kotlin DSL)
- MyBatis 4.0.1, Spring Data JPA 4.1.1, PostgreSQL 42.7.13
- HikariCP 7.0.2, p6spy 3.9.1 (DataSource 수동 래핑, JPA SQL 포맷 유지)
- Lombok 1.18.46, springdoc-openapi 3.0.3 (Swagger UI), Actuator 4.1.1
- Checkstyle 13.5.0, JaCoCo, SonarQube Gradle Plugin 6.3.1.5724
- 한국어/영어 메시지와 CORS 공통 설정

group: `com.seegene.local`, artifact: `spring-boot`

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

## 프로파일

공통 `application.yml` 위에 `application-{profile}.yml`이 적용됩니다.
미지정 시 공통 설정만 사용합니다.

| 프로파일 | SQL 로깅 | 애플리케이션 로그 | Swagger |
| --- | --- | --- | --- |
| 공통 / dev | 활성 | debug | 활성 |
| uat | 활성 | debug | 비활성 |
| prd | 비활성 | warn | 비활성 |

## 확인

- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/local-api-docs`
- Actuator: `http://localhost:8080/actuator/health`
- Liveness / Readiness: `/actuator/health/liveness`, `/actuator/health/readiness`

## REST API 목록

기본 주소는 `http://localhost:8080`이며, 아래 경로는 Controller의 클래스·메서드 매핑을 합친 전체 경로입니다.

| Method | Path | 설명 | 인증여부 |
| --- | --- | --- | --- |
| GET | `/apis/local/v1/masterdata/areas` | 지역 정보 전체 목록 조회 | 불필요 |

## 패키지 구조

현재 프로젝트의 주요 파일 구조입니다. `base`에는 공통 응답과 유틸리티,
`base.config`에는 공통 설정, `server`에는 REST API 구현을 모았습니다.
빈 패키지, IDE 설정과 빌드 캐시·산출물 디렉터리는 생략했습니다.

```text
spring-boot/
├── config/
│   └── checkstyle/                       # 코드 스타일 검사 규칙
│       ├── checkstyle.xml
│       └── suppressions.xml
├── doc/
│   └── 01-project-setup.md                # 초기 구성 기록과 학습 안내
├── gradle/
│   └── wrapper/                          # Gradle 실행 버전과 Wrapper
│       ├── gradle-wrapper.jar
│       └── gradle-wrapper.properties
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/seegene/local/
│   │   │       ├── Application.java      # Spring Boot 진입점
│   │   │       ├── base/                 # 공통 API 기반 코드
│   │   │       │   ├── config/           # com.seegene.local.base.config
│   │   │       │   │   ├── AppProperties.java            # app.* 설정 바인딩
│   │   │       │   │   ├── CorsConfig.java               # CORS 허용 설정
│   │   │       │   │   ├── DataSourceConfig.java         # DataSource와 SQL 로깅 설정
│   │   │       │   │   ├── JpaAwareSqlLogFormatter.java  # JPA SQL 로그 포맷
│   │   │       │   │   ├── LocaleConfig.java             # 다국어 메시지 설정
│   │   │       │   │   └── OpenApiConfig.java            # Swagger 문서 정보
│   │   │       │   ├── constants/
│   │   │       │   │   ├── Example.java  # 공통 예시·기본값
│   │   │       │   │   └── Messages.java # 메시지 키 상수
│   │   │       │   ├── controller/
│   │   │       │   │   └── AbstractApiController.java  # 공통 성공 응답 처리
│   │   │       │   ├── dto/response/
│   │   │       │   │   └── SspResponse.java            # code/message/data 응답
│   │   │       │   ├── enums/
│   │   │       │   │   └── ApiResponseCode.java        # API 응답 코드
│   │   │       │   └── util/
│   │   │       │       └── StringUtil.java             # 문자열 유틸리티
│   │   │       └── server/               # REST API 구현
│   │   │           ├── controller/
│   │   │           │   └── AreaController.java  # 지역 목록 조회 API
│   │   │           ├── dto/vo/
│   │   │           │   └── AreaVO.java          # 지역 코드·지역명
│   │   │           ├── mapper/
│   │   │           │   └── AreaMapper.java      # MyBatis 조회 인터페이스
│   │   │           └── service/
│   │   │               └── AreaService.java     # 지역 조회 서비스
│   │   └── resources/
│   │       ├── mapper/
│   │       │   └── AreaMapper.xml         # 지역 목록 조회 SQL
│   │       ├── messages/                 # 기본·한국어·영어 메시지
│   │       │   ├── messages.properties
│   │       │   ├── messages_ko.properties
│   │       │   └── messages_en.properties
│   │       ├── application.yml           # 공통 설정
│   │       ├── application-dev.yml       # 개발 환경
│   │       ├── application-uat.yml       # 사용자 인수 테스트 환경
│   │       └── application-prd.yml       # 운영 환경
│   └── test/
│       └── java/com/seegene/local/
│           └── ApplicationConfigurationTest.java  # 프로파일별 설정 검증
├── .gitignore                            # Git 추적 제외 설정
├── build.gradle.kts                      # 의존성과 빌드·검증 설정
├── gradle.properties                     # Gradle JVM·캐시 설정
├── gradlew                               # Unix 계열 Gradle 실행 스크립트
├── gradlew.bat                           # Windows Gradle 실행 스크립트
├── README.md
└── settings.gradle.kts                   # 프로젝트 이름과 모듈 구성
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
