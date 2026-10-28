# spring-boot

Spring Boot + MyBatis + JPA + Lombok 기반 Java 프로젝트입니다.

## 구성

- JDK 21 (LTS), Spring Boot 4.1.1, Gradle 9.6.1 (Kotlin DSL)
- MyBatis Spring Boot Starter 4.0.1, Spring Data JPA 4.1.1, PostgreSQL JDBC Driver 42.7.13
- HikariCP 7.0.2, p6spy 3.9.1 (DataSource 수동 래핑, JPA SQL 포맷 유지)
- Lombok 1.18.46, springdoc-openapi 3.0.3 (Swagger UI), Actuator 4.1.1
- Checkstyle 14.3.0, JaCoCo, SonarQube Gradle Plugin 6.3.1.5724
- 한국어/영어 메시지와 CORS 공통 설정

group: `com.stagora`, artifact: `spring-boot`

Spring Data JPA, HikariCP, Actuator 버전은 Spring Boot의 의존성 관리에 따릅니다.
Gradle Wrapper가 포함되어 있으므로 Gradle을 별도로 설치할 필요는 없습니다.

## 실행 전 준비

JDK 21과 PostgreSQL을 준비하고 사용할 DB와 계정을 생성합니다.
`application.yml`에 지정된 환경 변수로 접속 정보를 전달합니다.
접속 DB는 `stastics`, 기본 조회 스키마는 `public,sta_sqi`입니다. PowerShell 예시:

```powershell
${env:DB-READ-HOST} = 'localhost:5432'
${env:DB-USERNAME} = 'your-user'
${env:DB-PASSWORD} = 'your-password'
$env:SPRING_PROFILES_ACTIVE = 'dev'
.\gradlew.bat bootRun
```

| 환경 변수 | 기본값 / 용도 |
| --- | --- |
| `DB-READ-HOST` | 필수. PostgreSQL 호스트와 포트 (`localhost:5432` 등) |
| `DB-USERNAME` | 필수. DB 계정 |
| `DB-PASSWORD` | 필수. DB 비밀번호 |
| `SERVER_PORT` | `8080` |
| `BE_DB_MAX_POOL_SIZE` / `BE_DB_MIN_IDLE` | `10` / `2` |
| `BE_DB_CONN_TIMEOUT` | `30000` ms |
| `BE_DB_IDLE_TIMEOUT` / `BE_DB_MAX_LIFETIME` | `600000` / `1800000` ms |
| `BE_DB_VALID_TIMEOUT` | `5000` ms |
| `BE_HEALTH_DETAILS` | `when_authorized`. Actuator health 상세 정보 공개 기준 |

풀 설정은 원본처럼 `BE_DB_READ_*` 변수가 `BE_DB_*`보다 우선합니다.
`spring.datasource.local` 설정 키로 DataSource를 구성합니다.
MyBatis와 JPA가 이 DataSource를 공유합니다.

연결 초기화 SQL은 UTC와 읽기 전용 트랜잭션을 설정합니다. 쓰기 기능이 필요하면
`application.yml`의 `spring.datasource.local.hikari.connection-init-sql`을 다음과 같이 변경합니다.

```yaml
connection-init-sql: "SET timezone = 'UTC'; SET default_transaction_read_only = off"
```

JPA는 `open-in-view: false`, `generate-ddl: false`, `ddl-auto: none`을 사용하며 테이블을 자동 생성하지 않습니다.

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

Swagger는 공통/dev에서 활성화되고 uat/prd에서는 비활성화됩니다.
Readiness는 `readinessState`만 확인하며 DB 연결 상태는 포함하지 않습니다.

## REST API 목록

기본 주소는 `http://localhost:8080`이며, 아래 경로는 Controller의 클래스·메서드 매핑을 합친 전체 경로입니다.

| Method | Path | 설명 | 인증여부 |
| --- | --- | --- | --- |
| GET | `/apis/local/v1/areas` | 지역 정보 전체 목록 조회 | 불필요 |
| GET | `/apis/gmcs/v1/nations` | 지역별 국가 목록과 국가 하위 지역 조회 | 불필요 |

```powershell
curl.exe http://localhost:8080/apis/local/v1/areas
curl.exe -H "Accept-Language: ko" http://localhost:8080/apis/gmcs/v1/nations
```

성공 응답은 `SspResponse<T>` 형태의 `code`, `message`, `data`를 사용하며 성공 코드는 `S0000`입니다.

## 패키지 구조

현재 프로젝트의 주요 파일 구조입니다. `common`에는 공통 응답·예외·유틸리티,
`common.config`에는 공통 설정, `local`에는 REST API 구현을 모았습니다.
빈 패키지, IDE 설정과 빌드 캐시·산출물 디렉터리는 생략했습니다.

```text
spring-boot/
├── config/
│   └── checkstyle/                       # 코드 스타일 검사 규칙
├── doc/
│   └── guide_xml_format.md                # Mapper XML의 SQL 포맷 가이드
├── gradle/
│   └── wrapper/                          # Gradle 실행 버전과 Wrapper
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/stagora/
│   │   │       ├── Application.java      # Spring Boot 진입점
│   │   │       ├── common/               # 공통 API 기반 코드
│   │   │       │   ├── api/              # 공통 API 응답과 컨트롤러 지원
│   │   │       │   │   ├── AbstractApiController.java
│   │   │       │   │   ├── SspResponse.java
│   │   │       │   │   └── ApiResponseCode.java
│   │   │       │   ├── config/           # com.stagora.common.config
│   │   │       │   ├── constants/
│   │   │       │   ├── enums/
│   │   │       │   ├── exception/
│   │   │       │   └── util/
│   │   │       └── local/                # REST API 구현
│   │   │           ├── controller/
│   │   │           ├── dto/
│   │   │           │   ├── mapper/                  # MapStruct VO → 응답 DTO 변환
│   │   │           │   ├── response/                # Controller에서 반환하는 응답 DTO
│   │   │           │   └── vo/                      # MyBatis DB 조회 결과
│   │   │           ├── mapper/                       # MyBatis DB 조회 인터페이스
│   │   │           └── service/
│   │   └── resources/
│   │       ├── mapper/
│   │       ├── messages/                 # 기본·한국어·영어 메시지
│   │       ├── application.yml           # 공통 설정
│   │       ├── application-dev.yml       # 개발 환경
│   │       ├── application-uat.yml       # 사용자 인수 테스트 환경
│   │       └── application-prd.yml       # 운영 환경
│   └── test/
├── .gitignore                            # Git 추적 제외 설정
├── build.gradle.kts                      # 의존성과 빌드·검증 설정
├── gradle.properties                     # Gradle JVM·캐시 설정
├── gradlew                               # Unix 계열 Gradle 실행 스크립트
├── gradlew.bat                           # Windows Gradle 실행 스크립트
├── README.md
└── settings.gradle.kts                   # 프로젝트 이름과 모듈 구성
```

Java 소스는 `com.stagora` 아래에 작성합니다. MyBatis는 `org.apache.ibatis.annotations.Mapper`가 붙은 인터페이스를 탐색하며
XML은 `src/main/resources/mapper/` 아래에 추가합니다. 별칭 패키지는 `com.stagora`입니다.
`common.api`에는 공통 컨트롤러 지원(`AbstractApiController`), 응답 형식(`SspResponse`),
응답 코드(`ApiResponseCode`)를 모았습니다. 기관 유형 등의 enum은 `common.enums`에 둡니다.
`local.mapper`는 MyBatis DB 조회용, `local.dto.mapper`는 MapStruct 객체 변환용입니다.
지역·기관·국가 조회는 모두 `Controller → Service → MyBatis Mapper → XML SQL` 순서로 조회하고,
서비스에서 조회 결과 VO를 MapStruct Mapper로 변환하여 응답 DTO를 반환합니다.
국가 조회는 `NationInfoGroupVO`에 지역별 국가·하위 지역 목록을 담고,
`NationInfoResponseMapper`로 변환한 뒤 서비스에서 지역명과 국가명 순으로 정렬합니다.
MapStruct Mapper는 `org.mapstruct.Mapper`를 사용하고 Spring 빈으로 주입합니다.
생성 구현체는 빌드 시 `build/generated/sources/annotationProcessor/java/main/` 아래에 생성됩니다.
JPA Entity와 Repository도 같은 루트 아래에서 자동 탐색합니다.

메시지는 `MessageSource.getMessage(key, args, locale)`로 조회하며
기본 로케일은 한국어입니다. CORS 허용 Origin은 `app.cors.allowed-origin-patterns`로 설정하고,
기본값은 localhost입니다. 원본 구현처럼 CORS 필터는 모든 프로파일에서 등록됩니다.

## SQL 로깅과 XML 작성 규칙

`app.sql-logging.enabled: true`이면 DataSource를 p6spy로 감싸고, 파라미터가 치환된 SQL만 출력합니다.
JPA SQL에는 줄바꿈 포맷을 적용합니다. MyBatis는 `NoLoggingImpl`을 사용해 `Preparing`·`Parameters`
로그를 끄고, Hibernate의 자체 SQL·바인딩 로그도 비활성화하여 중복을 막았습니다.
prd에서는 SQL 로깅을 비활성화합니다.

Mapper XML의 SQL은 [XML · SQL 포맷 가이드](doc/guide_xml_format.md)를 따릅니다.
소문자 SQL, 탭 정렬, 선두 콤마, 테이블 별칭, JOIN 줄바꿈과 독립된 세미콜론 규칙을 사용하며
문자열 리터럴과 MyBatis 바인딩 변수는 원본을 유지합니다.

## 빌드 및 검증

```powershell
.\gradlew.bat build jacocoTestReport
```

Checkstyle로 운영 Java 소스(`src/main/java`)를 검사합니다.

```powershell
# 운영 소스 검사
.\gradlew.bat checkstyleMain
```

검사 결과는 콘솔과 `build/reports/checkstyle/main.html`에서 확인합니다.

테스트에는 프로파일별 설정 검증, SQL 로그 포맷 검증, 국가 조회 정렬·언어 처리와 Mapper XML 로딩 검증이 있습니다.
서비스 단위 테스트와 로그 포맷 테스트는 DB 없이 실행할 수 있습니다.

```powershell
.\gradlew.bat test --tests "*NationInfoServiceTest" --tests "*JpaAwareSqlLogFormatterTest"
```

현재 `ApplicationConfigurationTest`의 풀 이름·ApplicationName 기대값은 `local` 기준이지만
YAML은 `gmcs-be-hikari-pool`·`gmcs`를 사용하므로 전체 테스트 실행 시 이 불일치로 실패합니다.

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
