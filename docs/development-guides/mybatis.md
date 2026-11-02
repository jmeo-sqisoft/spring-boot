# MyBatis XML · SQL 포맷 가이드

MyBatis Mapper XML에 작성하는 SQL의 표기와 정렬 기준을 정리합니다.

## 목차

- [핵심 규칙](#핵심-규칙)
- [상세 작성 기준](#상세-작성-기준)
- [SQL 포맷 샘플](#sql-포맷-샘플)

## 핵심 규칙

| 번호 | 항목 | 작성 기준 |
| --- | --- | --- |
| 01 | 대소문자 | SQL 키워드·함수명·데이터 타입은 소문자로 작성하고, 기존 식별자·문자열·바인딩 변수의 표기는 유지합니다. |
| 02 | 식별자와 별칭 | 기존 테이블·컬럼·별칭의 이름과 대소문자를 임의로 변경하지 않습니다. |
| 03 | 테이블 별칭 | `as a`, `as b`처럼 짧은 소문자 별칭을 사용하고, 같은 쿼리 블록에서 중복하지 않습니다. |
| 04 | 컬럼 구분 | 첫 컬럼은 공백 6칸, 다음 컬럼은 공백 4칸 뒤 `, `로 시작해 식의 시작 위치를 맞춥니다. |
| 05 | SELECT | `select`는 단독 줄에 작성하고 첫 컬럼은 다음 줄에 배치합니다. |
| 06 | FROM·WHERE | `from 테이블명`, `where 조건식`처럼 키워드와 첫 대상을 같은 줄에 작성합니다. |
| 07 | JOIN | JOIN 키워드와 대상은 같은 줄에 작성하고, `on`은 같은 줄 또는 다음 줄에 배치합니다. |
| 08 | 파라미터 바인딩 | `#{...}`로 바인딩하고, `${...}`는 사용하지 않습니다. |
| 09 | 탭 | 사용 금지 |
| 10 | 들여쓰기 | 기본 블록 들여쓰기는 공백 4칸으로 하고, 컬럼·조건은 샘플처럼 시작 위치를 맞춥니다. |
| 11 | INSERT·UPDATE | 입력 컬럼·값과 SET 대입식도 선두 콤마로 구분하고 각 식의 시작 위치를 맞춥니다. |
| 12 | 문장 종료 | 마지막 구문에서 줄을 바꿔 끝내고, 세미콜론을 사용하는 경우 `;`만 별도 줄에 작성합니다. |

## 상세 작성 기준

### 기본 표기와 별칭

- SQL 키워드·함수명·데이터 타입은 `select`, `case`, `count`, `varchar`처럼 소문자로 작성합니다.
- 컬럼 별칭과 테이블 별칭에는 `as`를 명시하고 양옆에 공백 한 칸을 둡니다.
- 테이블 별칭은 `a`, `b`, `c`와 같이 짧은 소문자를 사용합니다. 사용 순서보다 참조의 일관성을 우선합니다.
- 서브쿼리는 별도의 쿼리 블록이므로 바깥 쿼리의 별칭을 다시 사용할 수 있습니다.
  다만 상관 서브쿼리는 바깥 참조와 내부 별칭이 혼동되지 않도록 구분합니다.

### 공백과 컬럼 정렬

- XML과 SQL 모두 탭을 사용하지 않습니다. 기본 블록 들여쓰기는 공백 4칸입니다.
- 최상위 `select`는 단독 줄에 작성합니다. 첫 컬럼은 다음 줄에서 공백 6칸 뒤에 작성합니다.
- 다음 컬럼은 공백 4칸 뒤에 `, `를 붙여 첫 컬럼과 식의 시작 위치를 맞춥니다.
- 서브쿼리는 공백 4칸 단위로 들여쓰고, 내부 SELECT의 컬럼은 키워드보다 한 단계 이상 안쪽에 배치합니다.
- `case`의 `when`, `else`, `end`와 괄호로 묶은 식은 블록 구조에 맞춰 정렬합니다.
- 공백 정렬은 SQL 문자열 내부의 공백이나 실제 값을 변경하는 기준이 아닙니다.

| 위치 | 정렬 형태 |
| --- | --- |
| 조회 시작 | `select` 단독 줄 |
| 첫 컬럼 | 공백 6칸 + 컬럼 또는 식 |
| 다음 컬럼 | 공백 4칸 + `, ` + 컬럼 또는 식 |
| 첫 테이블 | `from 테이블명 as 별칭` |
| 첫 조건 | `where 조건식` |
| 다음 WHERE 조건 | 공백 2칸 + `and 조건식` 또는 `or 조건식` |

### 서브쿼리와 조인

- SELECT 목록의 서브쿼리는 `, (`로 시작하고, 닫는 괄호 뒤에 `as 별칭`을 작성합니다.
- FROM/JOIN의 서브쿼리도 `join (`처럼 시작하고, 닫는 괄호 뒤에 별칭을 작성합니다.
- `inner join 테이블명`, `left outer join 테이블명`처럼 JOIN 대상은 키워드와 같은 줄에 둡니다.
- 짧은 조인 조건은 대상 뒤에 `on 조건식`으로 이어 쓸 수 있습니다.
- 조인 조건이 길면 `on`을 다음 줄로 내리고, 이어지는 `and`·`or` 조건의 식 시작 위치를 맞춥니다.
- 조인 조건은 `using (...)` 대신 `on`으로 명시합니다. `cross join`에는 조건을 붙이지 않습니다.
- AND와 OR를 섞으면 샘플처럼 괄호로 의도한 조건 묶음을 명확하게 표시합니다.

### CTE 이름과 구분

- CTE 이름에는 `cte_` 접두사를 붙입니다. 예: `cte_inp`, `cte_summary`.
- CTE를 추가할 때는 줄을 바꾸고 콤마로 시작합니다.
 
### 파라미터 바인딩

- Mapper에 전달하는 값은 `#{...}`로 바인딩합니다. `${...}`는 사용하지 않습니다.
- 바인딩 변수 이름은 Java 프로퍼티나 Mapper 파라미터와 일치시킵니다.
- 샘플의 `'I'`, `'F'`, `'b'`, 숫자 등은 고정값을 보여 주는 예시입니다.
  요청이나 서비스에서 전달받는 값은 SQL 문자열에 직접 넣지 않고 바인딩합니다.

### 데이터 입력과 수정

- INSERT 컬럼과 VALUES 값은 각각 줄을 나누고, 항목 순서를 일치시킵니다.
- 첫 항목은 공백 6칸, 다음 항목은 공백 4칸 뒤 `, `로 시작합니다.
- UPDATE는 `set   col_2 = 값`처럼 첫 대입식의 시작 위치를 맞춥니다.
- 다음 대입식은 공백 4칸 뒤 `, `로 시작하고, WHERE 조건은 다음 줄에 작성합니다.

### 문장 종료

- 문서 샘플처럼 마지막 구문 뒤에서 줄을 바꿔 종료합니다.
- 세미콜론은 필수 형식으로 강제하지 않습니다. 사용하는 경우 INSERT 샘플처럼 `;`만 별도 줄에 둡니다.

## SQL 포맷 샘플

### 조회 · 서브쿼리 · 조인

```sql
select
      c.fg_seq
    , case when a.sc_type = 1 then 'F'
           else 'N'
      end as g_group
    , (
        select
              sctName
            , sctId
        from sampleZ as x
        where x.sct = a.sct
      ) as sct_name
    , case a.sct when 'A203' then 'S'
                 else case when a.sc_type = 1 then 'F'
                           else 'N'
                      end
      end as sct_type
    , a.sct
    , now()
from sample as a
inner join (
        select y.fg_seq
        from sampleX as x
        inner join sampleY as y on y.code = a.cust_code
        where a.svc_date >= getdate()
    ) as b on b.fg_seq = a.fg_seq
left outer join sample1 as c on c.fg_seq = a.fg_seq and c.sct = a.sct and ((a.sc_type = 1 and c.gGroup = 'F') or (a.sc_type = 2 and c.gGroup = 'N'))
left outer join sample2 as d
             on d.fg_seq = c.fg_seq
            and d.sct = c.sct
            and ((d.sc_type = 1 and d.gGroup = 'F') or (d.sc_type = 2 and d.gGroup = 'N'))
where a.op_type = 'I'
  and a.isn in (
        select
            x.isn
        from sample2 x
        where x.cno = a.cno
  )
```

### 데이터 입력

```sql
insert into sample (
      col_1
    , col_2
) values (
      1
    , 'a'
)
```

### 데이터 수정

```sql
update sample
set   col_2 = 'b'
    , col_3 = 'c'
where col_1 = 1
```

