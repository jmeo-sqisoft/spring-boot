# Java 코드 작성 가이드

[Checkstyle 설정](../config/checkstyle/checkstyle.xml)을 기준으로 Java 코드의
포맷, 이름, import 및 기본 코딩 규칙을 정리합니다.
운영 코드(`src/main/java`)에 적용합니다.

## 목차

- [핵심 규칙](#핵심-규칙)
- [들여쓰기와 줄바꿈](#들여쓰기와-줄바꿈)
- [중괄호와 공백](#중괄호와-공백)
- [이름 작성](#이름-작성)
- [import 작성](#import-작성)
- [필드와 선언](#필드와-선언)
- [기본 코딩 규칙](#기본-코딩-규칙)
- [작성 예시](#작성-예시)
- [검사 실행과 결과](#검사-실행과-결과)

## 핵심 규칙

| 항목 | 작성 기준 | Checkstyle 모듈·속성 |
| --- | --- | --- |
| 파일 | UTF-8, 파일 끝 개행 | `charset`, `NewlineAtEndOfFile` |
| 탭 | 사용 금지, 탭이 있는 모든 줄 보고 | `FileTabCharacter`, `eachLine=true` |
| 들여쓰기 | 공백 4칸 | `Indentation` |
| 한 줄 길이 | 최대 100자, 설정된 예외 적용 | `LineLength` |
| 중괄호 | 여는 중괄호는 같은 줄, 제어문 본문에 중괄호 사용 | `LeftCurly`, `RightCurly`, `NeedBraces` |
| 공백 | 연산자 양옆, 쉼표·세미콜론 뒤 공백 | `WhitespaceAround`, `WhitespaceAfter` |
| 이름 | 타입 PascalCase, 메서드·변수 camelCase, 상수 UPPER_SNAKE_CASE | 이름 검사 모듈 |
| import | 그룹별 정렬, 와일드카드·미사용 import 금지 | `ImportOrder`, `AvoidStarImport`, `UnusedImports` |
| 필드 | 일반 인스턴스 필드는 private, 설정된 예외 허용 | `VisibilityModifier` |
| 선언 | 한 줄에 한 문장, 변수는 개별 선언 | `OneStatementPerLine`, `MultipleVariableDeclarations` |

Java 파일의 탭 금지는 XML 안에 작성하는 SQL의 정렬 규칙과 별개입니다.
Mapper XML의 SQL에는 [XML · SQL 포맷 가이드](guide_xml_format.md)를 적용합니다.

## 들여쓰기와 줄바꿈

### 공백 4칸

탭 대신 공백을 사용하고, 블록 깊이가 증가할 때마다 공백 4칸을 추가합니다.
`tabWidth=4`는 탭의 계산 너비이며 탭 사용을 허용한다는 의미가 아닙니다.

| `Indentation` 속성 | 값 | 의미 |
| --- | --- | --- |
| `basicOffset` | `4` | 기본 들여쓰기 |
| `braceAdjustment` | `0` | 중괄호에 별도 들여쓰기 추가 없음 |
| `caseIndent` | `4` | switch의 case 들여쓰기 |
| `throwsIndent` | `4` | 줄바꿈한 throws 절 들여쓰기 |
| `lineWrappingIndentation` | `4` | 이어지는 줄의 들여쓰기 |
| `arrayInitIndent` | `4` | 배열 초기화 내용 들여쓰기 |

줄이 길어지면 인자, 메서드 체인, 연산식 등을 의미 단위로 나눕니다.
다음 예시처럼 이어지는 줄을 한 단계 들여씁니다.

```java
List<NationInfo> sorted = nations.stream()
    .sorted(Comparator.comparing(NationInfo::getNationName,
        Comparator.nullsLast(Comparator.naturalOrder())))
    .toList();
```

### 한 줄 최대 100자

주석과 애너테이션도 줄 길이 검사 대상입니다.
긴 인자 목록이나 애너테이션 속성은 줄바꿈하고, 문자열 값은 변경하지 않습니다.

```java
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING,
    unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface AreasConverter {
    AreasResponse toResponse(Area area);
}
```

현재 길이 검사에서 제외하는 패턴은 다음과 같습니다.

```regex
^package.*|^import.*|a]+ href|href|http://|https://|ftp://
```

줄 시작이 `package` 또는 `import`인 줄과, 나머지 패턴이 포함된 줄을 제외합니다.
URL이 있는 주석도 해당 줄 전체가 길이 검사에서 제외됩니다.
이 예외는 줄 길이에만 적용되며 탭·이름·import 등의 다른 검사는 그대로 적용됩니다.

## 중괄호와 공백

### 중괄호 배치

클래스·인터페이스·enum·record 선언의 여는 중괄호 바로 다음에는 빈 줄을 넣지 않습니다.
첫 필드·메서드·주석을 다음 줄부터 작성하며, 중첩 타입과 테스트 코드에도 동일하게 적용합니다.
이 규칙은 프로젝트 작성 기준이며 현재 Checkstyle에서 별도로 강제하지는 않습니다.

클래스, 메서드, 생성자, 제어문, 람다, record 등의 여는 중괄호는 같은 줄에 둡니다.
`if`, `else`, `for`, `while`, `do`의 본문은 한 문장이더라도 중괄호로 감쌉니다.
`else`, `catch`, `finally`는 앞 블록의 닫는 중괄호와 같은 줄에 둡니다.

```java
if (hospitalId != null && !hospitalId.isEmpty()) {
    return mapper.getHospitalInfo(hospitalId);
} else {
    return mapper.getClabInfo(clabId);
}
```

### 연산자·구분자·괄호

- 대입, 산술, 비교, 논리, 비트 및 복합 대입 연산자의 양옆에 공백을 둡니다.
- 쉼표와 세미콜론 뒤에는 공백이나 줄바꿈을 둡니다.
- `if`, `while`, `for`, `switch`, `catch` 등의 키워드 뒤에 공백을 둡니다.
- `else`, `do`, `try`, `finally`, `return` 뒤의 공백도 검사합니다.
- 메서드·생성자 이름과 여는 괄호 사이에 공백이나 줄바꿈을 넣지 않습니다.
- 캐스트 타입의 닫는 괄호 뒤에는 공백을 넣지 않습니다.

```java
int total = first + second;
String name = normalize(value, fallback);
String text = (String)value;

for (int index = 0; index < values.size(); index++) {
    process(values.get(index));
}
```

`WhitespaceAround`는 빈 생성자·메서드·타입·루프에 대한 공백 예외를 허용합니다.
빈 블록의 허용 여부는 아래의 `EmptyBlock` 규칙도 함께 확인합니다.

## 이름 작성

| 대상 | 표기·예시 | 검사 정규식 |
| --- | --- | --- |
| 클래스·인터페이스 등 타입 | PascalCase: `SampleService`, `XmlHttpRequest` | `^[A-Z][a-zA-Z0-9]*$` |
| 패키지 | 소문자: `com.stagora.report.sample` | `^[a-z]+(\.[a-z][a-z0-9]*)*$` |
| 메서드 | camelCase: `getNationInfo` | `^[a-z][a-zA-Z0-9]*$` |
| 인스턴스 필드 | camelCase: `institutionName` | `^[a-z][a-zA-Z0-9]*$` |
| 지역 변수 | camelCase: `areaCode` | `^[a-z][a-zA-Z0-9]*$` |
| 매개변수 | camelCase: `hospitalId` | `^[a-z][a-zA-Z0-9]*$` |
| 상수 | UPPER_SNAKE_CASE: `DEFAULT_LANGUAGE` | `^[A-Z][A-Z0-9]*(_[A-Z0-9]+)*$` |

타입 이름의 두문자어도 `XmlHttpRequest`처럼 읽기 쉬운 형태로 작성합니다.
멤버 이름에는 `sName`, `mName` 같은 헝가리안 표기나 `_name`을 사용하지 않습니다.
패키지의 각 단어는 8자 이내를 권장합니다.

이 중 두문자어 표기, 헝가리안 표기 금지, 패키지 길이 권장은 설정 주석·메시지의 작성 기준입니다.
현재 정규식은 `XMLHttpRequest`, `mName`, 8자를 넘는 패키지명을 별도로 거부하지 않습니다.
패키지 정규식은 첫 구간을 제외한 구간에서 첫 글자 뒤의 숫자도 허용합니다.

## import 작성

### 그룹과 정렬

다음 순서로 그룹을 나누고 그룹 사이에 빈 줄을 둡니다.

1. `java`
2. `javax`
3. `org`
4. `com`
5. 그 외 패키지: `io`, `jakarta`, `lombok` 등

각 그룹의 일반 import는 대소문자를 구분하는 사전순으로 정렬합니다.
static import는 기본 `under` 정책에 따라 해당 그룹의 일반 import 뒤에 배치합니다.
static import를 파일 전체의 맨 위로 모으지 않습니다.

```java
import java.util.List;
import java.util.Locale;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import com.stagora.report.sample.model.Area;
```

### 와일드카드와 미사용 import

일반 import와 static import 모두 `*` 대신 필요한 타입·멤버를 명시합니다.
사용하지 않는 import와 같은 패키지의 불필요한 import는 제거합니다.

```java
import java.util.List;
import static java.util.Collections.emptyList;
```

## 필드와 선언

### 접근 제어와 지시자 순서

일반 인스턴스 필드는 `private`으로 선언합니다. `protected` 필드는 허용하지 않습니다.
`VisibilityModifier`에는 `allowPublicFinalFields=true`가 설정되어 있고,
public static final 상수 등 모듈 기본 예외도 적용됩니다.
따라서 모든 필드가 무조건 private이어야 하는 규칙은 아닙니다.

여러 지시자를 사용할 때는 `public static final`처럼 Java의 지시자 순서를 따릅니다.
`ModifierOrder`는 애너테이션과 지시자의 순서도 검사합니다.

```java
public static final String DEFAULT_LANGUAGE = "en";

private final SampleMapper sampleMapper;
private String institutionName;
```

### 배열과 변수 선언

배열의 대괄호는 변수명 뒤가 아닌 타입 뒤에 둡니다.
변수는 한 선언에 하나씩 작성하고 한 줄에 여러 문장을 붙이지 않습니다.

```java
String[] names;
int first = 1;
int second = 2;

process(first);
process(second);
```

## 기본 코딩 규칙

| 모듈 | 작성 기준 |
| --- | --- |
| `MissingSwitchDefault` | switch의 default 누락을 검사합니다. |
| `EmptyBlock` | 설정된 try·finally·if·else·switch의 비어 있는 블록을 검사합니다. `option=TEXT`이므로 주석도 내용으로 인정합니다. |
| `EqualsHashCode` | `equals()`와 `hashCode()`를 함께 재정의합니다. |
| `StringLiteralEquality` | 문자열 리터럴을 `==`, `!=`로 비교하지 않고 `equals()` 등을 사용합니다. |
| `UnnecessaryParentheses` | 의미를 바꾸지 않는 불필요한 괄호를 제거합니다. |

`EmptyBlock`의 현재 검사 대상에는 메서드·생성자·catch·루프가 포함되어 있지 않습니다.
문자열 비교 규칙도 모든 참조 비교를 금지하는 것이 아니라 문자열 리터럴과의 비교를 검사합니다.

```java
boolean korean = "ko".equals(language);
int total = first + second;
```

## 작성 예시

```java
package com.stagora.report.sample.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.stagora.report.sample.dto.AreasConverter;
import com.stagora.report.sample.dto.AreasResponse;
import com.stagora.report.sample.mapper.SampleMapper;
import com.stagora.report.sample.model.Area;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SampleService {
    private final SampleMapper sampleMapper;
    private final AreasConverter areasConverter;

    public List<AreasResponse> getArea() {
        List<Area> areas = sampleMapper.getArea();
        return areasConverter.toResponses(areas);
    }
}
```

## 검사 실행과 결과

프로젝트 루트에서 실행합니다.

```powershell
# 운영 Java 코드
.\gradlew.bat checkstyleMain
```

`checkstyleTest`는 비활성화되어 `build`·`check`에 포함되거나 직접 요청해도 `SKIPPED`로 표시됩니다.
테스트 실행 작업인 `test`는 그대로 실행됩니다.

| 대상 | HTML 보고서 | XML 보고서 |
| --- | --- | --- |
| 운영 코드 | `build/reports/checkstyle/main.html` | `build/reports/checkstyle/main.xml` |

검사 도구 버전은 [빌드 설정](../build.gradle.kts)의 Checkstyle 14.3.0입니다.
현재 위반 심각도는 `warning`이며 `maxWarnings=Int.MAX_VALUE`, `maxErrors=0`으로 설정되어 있습니다.
따라서 `BUILD SUCCESSFUL`만으로 위반이 없다고 판단하지 말고 콘솔과 보고서의 위반 건수를 확인합니다.

Checkstyle은 코드 스타일을 검사합니다. 동작 검증은 별도로 테스트를 실행합니다.

```powershell
.\gradlew.bat test
```
