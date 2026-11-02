# Git 작업 가이드

브랜치 전략, 커밋 메시지 형식과 Pull Request(PR) 작성 절차를 정리합니다.

## 목차

- [브랜치 전략](#브랜치-전략)
- [커밋 메시지 컨벤션](#커밋-메시지-컨벤션)
- [Pull Request 가이드](#pull-request-가이드)

## 브랜치 전략

### 브랜치 역할

| 브랜치 | 역할 | 배포 환경 |
| --- | --- | --- |
| `main` | 운영 배포 코드 관리 | Production |
| `develop` | 개발 변경 통합과 다음 배포 준비 | Development |
| `feature/*` | 새로운 기능 개발 | 별도 배포 없음 |
| `fix/*` | 일반 버그 수정 | 별도 배포 없음 |
| `refactor/*` | 기능 변경 없는 리팩터링 | 별도 배포 없음 |
| `release/*` | 배포 전 QA와 버그 수정 | Staging |
| `hotfix/*` | 운영 긴급 수정 | 수정 후 운영 반영 |

일반 작업 브랜치는 최신 `develop`에서 생성합니다.
운영 긴급 수정은 현재 운영 코드인 `main`에서 `hotfix/*` 브랜치를 생성합니다.

```text
main                         # 운영 배포 코드
├── hotfix/긴급수정            # 운영 긴급 수정
└── develop                  # 개발 변경 통합
    ├── feature/기능명        # 기능 개발
    ├── fix/버그명            # 버그 수정
    ├── refactor/대상         # 리팩터링
    └── release/버전          # QA와 배포 준비
```

위 구조는 브랜치의 역할과 생성 기준을 나타냅니다.

### 브랜치 이름

| 형식 | 용도 | 예시 |
| --- | --- | --- |
| `feature/기능명` | 새 기능 개발 | `feature/login-api` |
| `fix/버그명` | 버그 수정 | `fix/login-null-token` |
| `refactor/대상` | 코드 구조 개선 | `refactor/sample-service` |
| `release/버전` | 배포 준비 | `release/1.0.0` |
| `hotfix/긴급수정` | 운영 긴급 패치 | `hotfix/login-error` |

### 일반 작업과 배포 흐름

1. 최신 `develop`에서 작업 브랜치를 생성합니다.
2. 작업 완료 후 `develop`을 대상으로 PR을 생성합니다.
3. 리뷰와 승인을 거쳐 병합하고 개발 환경에서 테스트합니다.
4. 배포 준비가 되면 `develop`에서 `release/버전` 브랜치를 생성합니다.
5. Staging에서 QA를 진행하고 발견한 문제를 수정합니다.
6. QA 완료 후 릴리스 변경을 `main`과 `develop`에 모두 반영합니다.
7. 운영 배포 시 `main`의 배포 커밋에 `v1.0.0` 형식의 태그를 생성합니다.

운영 긴급 수정도 리뷰와 검증 후 `main`에 반영하고,
동일한 수정이 다음 배포에서 누락되지 않도록 `develop`에도 반영합니다.

## 커밋 메시지 컨벤션

### 형식

```text
<type>: <subject>

<body>
```

- `type`과 제목 사이에는 콜론과 공백 한 칸을 둡니다.
- 제목(`subject`)은 50자 이내로 작성합니다.
- 첫 글자의 대문자 표기를 강제하지 않습니다.
- 제목 끝에는 마침표를 붙이지 않습니다.
- 제목은 변경 동작을 나타내는 명령형으로 작성합니다. 예: `Add`, `Fix`, `Update`, `추가`, `수정`.
- 본문이 필요하면 제목과 본문 사이에 빈 줄을 두고 변경 내용을 설명합니다.

### 타입

| 타입 | 설명 |
| --- | --- |
| `feat` | 새로운 기능 |
| `fix` | 버그 수정 |
| `docs` | 문서 변경 |
| `refactor` | 기능 변경 없는 리팩터링 |
| `test` | 테스트 추가·수정 |
| `chore` | 빌드·설정 변경 |
| `style` | 기능 변경 없는 코드 포맷 수정 |
| `perf` | 성능 개선 |

### 작성 예시

```text
feat: 회원가입 API 추가
- 이메일 인증 기능 포함
- 비밀번호 암호화 적용
```

```text
fix: 로그인 시 토큰 만료 오류 수정
```

```text
chore: Gradle 의존성 업데이트
```

## Pull Request 가이드

### 1. 작업 브랜치 생성

`develop`의 최신 상태를 받은 후 작업을 시작합니다.

```bash
git checkout develop
git pull origin develop
git checkout -b feature/login-api
```

기능 개발 외 작업은 목적에 따라 `fix/*`, `refactor/*` 등의 이름을 사용합니다.

### 2. 작업과 커밋

변경 내용을 확인하고 커밋 메시지 컨벤션에 맞춰 커밋합니다.

```bash
git status
git add .
git diff --cached
git commit -m "feat: 로그인 API 구현"
```

### 3. Push와 PR 생성

작업 브랜치를 원격 저장소에 Push합니다.

```bash
git push -u origin feature/login-api
```

GitHub 저장소에서 **Compare & pull request**를 선택합니다.
일반 작업의 대상 브랜치(base)는 `develop`, 변경 브랜치(compare)는 작업 브랜치로 지정합니다.

### 4. PR 작성

PR에는 다음 항목을 작성합니다.

| 항목 | 작성 내용 |
| --- | --- |
| 작업 내용 | 무엇을 변경했는지와 변경 목적 |
| 관련 이슈 | 관련 JIRA 티켓 번호나 Issue 링크 |
| 변경 사항 | 주요 코드·설정 변경과 영향 범위 |
| 테스트 결과 | 실행한 테스트 명령, 성공·실패 결과와 필요한 로그 |
| 체크리스트 | 기존 테스트 통과, 환경 변수 문서 반영, 비식별화 확인 |

`.github/PULL_REQUEST_TEMPLATE.md`가 있으면 PR 생성 시 템플릿이 자동으로 표시됩니다.
현재 프로젝트에는 해당 파일이 없으므로 아래 양식을 PR 본문에 사용할 수 있습니다.

```markdown
## 작업 내용

- 변경 목적과 결과

## 관련 이슈

- JIRA 티켓 또는 Issue 링크

## 변경 사항

- 주요 코드·설정 변경

## 테스트 결과

- 실행 명령과 결과

## 체크리스트

- [ ] 기존 테스트 통과
- [ ] 새 환경 변수를 추가했다면 문서 반영
- [ ] 로그·화면·첨부 자료 비식별화 확인(해당 시)
```

### 5. 리뷰와 병합

- **Reviewers**: 해당 개발 파트의 PL을 지정합니다.
- **Assignees**: PR 작성자를 지정합니다.
- 리뷰 의견을 반영하고 승인(Approve)이 완료되면 대상 브랜치로 병합합니다.
- 일반 작업은 `develop`으로, 릴리스·긴급 수정은 브랜치 전략에 정한 대상에 반영합니다.

### 6. 완료 후 정리

병합 후 작업 브랜치에서 다른 브랜치로 이동하고 로컬 작업 브랜치를 삭제합니다.

```bash
git checkout develop
git pull origin develop
git branch -d feature/login-api
```

원격 작업 브랜치는 GitHub의 병합 완료 화면에서 **Delete branch**로 삭제합니다.
