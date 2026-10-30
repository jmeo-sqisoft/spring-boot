# spring-boot

Spring Boot + MyBatis + JPA + Lombok 기반 Java 프로젝트입니다.

## 구성

- JDK 21 (LTS)
- Spring Boot 4.1.1
- MyBatis (`mybatis-spring-boot-starter` 4.0.1)
- Spring Data JPA (`spring-boot-starter-data-jpa`, MyBatis와 DataSource 공유)
- Lombok 1.18.46
- MapStruct 1.6.3 (`lombok-mapstruct-binding` 0.2.0으로 Lombok 연동)
- PostgreSQL JDBC Driver (`postgresql` 42.7.13)
- p6spy (SQL 로깅, 3.9.1 — `global/config/DataSourceConfig`에서 DataSource를 수동으로 래핑)
- springdoc-openapi (Swagger UI, 3.0.3)
- Actuator (`spring-boot-starter-actuator`)
- HikariCP (JDBC 의존성을 통해 기본 포함)
- Checkstyle 14.3.0 (규칙: `config/checkstyle/checkstyle.xml`)
- JaCoCo (테스트 커버리지 보고서)
- SonarQube Gradle Plugin 6.3.1.5724 (서버와 토큰 별도 설정)
- Gradle 9.6.1 (Kotlin DSL)

group: `com.stagora`, artifact: `spring-boot`

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
| `DB_READ_MAX_POOL_SIZE` / `DB_READ_MIN_IDLE` | `10` / `2` |
| `DB_READ_CONN_TIMEOUT` | `30000` ms |
| `DB_READ_IDLE_TIMEOUT` / `DB_READ_MAX_LIFETIME` | `600000` / `1800000` ms |
| `DB_READ_VALID_TIMEOUT` | `5000` ms |
| `HEALTH_DETAILS` | `when_authorized`. Actuator health 상세 정보 공개 기준 |

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

| Method | Path | 설명 | 쿼리 파라미터 |
| --- | --- | --- | --- |
| GET | `/apis/report/v1/sample/areas` | 지역 정보 전체 목록 조회 | 없음 |
| GET | `/apis/report/v1/sample/institutioninfo` | 기관 정보 조회 | `clabId`, `hospitalId` 중 하나 이상 필요 |
| GET | `/apis/report/v1/sample/nations` | 지역별 국가 목록과 국가 하위 지역 조회 | 없음 |

성공 응답은 `StaResponse<T>` 형태의 `code`, `message`, `data`를 사용하며 성공 코드는 `S0000`입니다.

## 패키지 구조

현재 프로젝트의 주요 파일 구조입니다. `global`에는 공통 응답·설정·예외·유틸리티,
`report.sample`에는 샘플 REST API 구현을 모았습니다.

```text
spring-boot/
├── config/
│   └── checkstyle/                       # Java·Mapper XML 스타일 검사 규칙
├── doc/
├── gradle/
│   └── wrapper/                          # Gradle 실행 버전과 Wrapper
├── src/
│   └── main/
│       ├── java/com/stagora/
│       │   ├── Application.java          # Spring Boot 진입점
│       │   ├── global/                   # 공통 API 기반 코드
│       │   │   ├── api/                  # 공통 API 응답과 컨트롤러 지원
│       │   │   ├── config/               # DataSource·다국어·CORS·OpenAPI 설정
│       │   │   ├── constants/            # 공통 언어 코드·메시지 키
│       │   │   ├── enums/                # 기관 유형·데이터 유형
│       │   │   ├── exception/            # 공통 API 예외
│       │   │   └── util/                 # 언어·문자열 처리
│       │   └── report/sample/
│       │       ├── controller/           # SampleController
│       │       ├── dto/                  # 요청·응답 객체와 MapStruct 변환기
│       │       ├── mapper/               # SampleMapper 인터페이스
│       │       ├── model/                # MyBatis 조회 결과 모델
│       │       └── service/              # SampleService
│       └── resources/
│           ├── mapper/sample/            # SampleMapper의 SQL·결과 매핑
│           ├── messages/                 # 기본·한국어·영어 메시지
│           ├── application.yml           # 공통 설정
│           ├── application-dev.yml       # 개발 환경
│           ├── application-uat.yml       # 사용자 인수 테스트 환경
│           └── application-prd.yml       # 운영 환경
├── .gitignore                            # Git 추적 제외 설정
├── build.gradle.kts                      # 의존성과 빌드·검증 설정
├── gradle.properties                     # Gradle JVM·캐시 설정
├── gradlew                               # Unix 계열 Gradle 실행 스크립트
├── gradlew.bat                           # Windows Gradle 실행 스크립트
├── README.md
└── settings.gradle.kts                   # 프로젝트 이름과 모듈 구성
```

## XML 작성 규칙

Mapper XML의 SQL은 [XML · SQL 포맷 가이드](doc/guide_xml_format.md)를 따릅니다.
소문자 SQL, 탭 정렬, 선두 콤마, 테이블 별칭, JOIN 줄바꿈과 독립된 세미콜론 규칙을 사용하며
문자열 리터럴과 MyBatis 바인딩 변수는 원본을 유지합니다.

## 빌드 및 검증

### 빌드 및 커버리지

프로젝트 빌드와 테스트 커버리지 보고서 생성을 함께 실행합니다.

```powershell
.\gradlew.bat build jacocoTestReport
```

결과는 `build/reports/jacoco/test/html/index.html`에서 확인합니다.

### 테스트

테스트만 실행하려면 다음 명령을 사용합니다. 외부 DB 연결 없이 실행할 수 있습니다.

```powershell
.\gradlew.bat test
```

### 코드 스타일 검사

Checkstyle로 운영 Java 소스(`src/main/java`)와 Mapper XML(`src/main/resources/mapper`)을 검사합니다.

```powershell
.\gradlew.bat checkstyleMain
```

### 산출물 및 보고서

- 실행 JAR: `build/libs/spring-boot-0.0.1-SNAPSHOT.jar`
- 실행 WAR: `build/libs/spring-boot-0.0.1-SNAPSHOT.war`
- 테스트: `build/reports/tests/test/index.html`
- Checkstyle: `build/reports/checkstyle/`
- JaCoCo: `build/reports/jacoco/test/html/index.html`
