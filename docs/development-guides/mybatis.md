# MyBatis XML · SQL 포맷 가이드

MyBatis Mapper XML에 작성하는 SQL의 표기, 정렬, 조인 및 문장 종료 규칙을 정리합니다.
SQL을 포맷할 때는 아래 규칙과 샘플을 함께 참고합니다.

## 목차

- [핵심 규칙](#핵심-규칙)
- [상세 작성 기준](#상세-작성-기준)
- [SQL 포맷 샘플](#sql-포맷-샘플)

## 핵심 규칙

| 번호 | 항목 | 작성 기준 |
| --- | --- | --- |
| 01 | 대소문자 | 문자열 리터럴과 MyBatis 바인딩 변수를 제외한 SQL 키워드·함수명·데이터 타입은 소문자로 작성합니다. |
| 02 | 컬럼 별칭 | 단어는 언더바(`_`)로 구분합니다. |
| 03 | 테이블 별칭 순서 | 한 쿼리 블록에서 `a`, `b`, `c` 순서로 부여합니다. |
| 05 | 별칭 선언 | 테이블 별칭 앞에 `as`를 명시합니다. |
| 06 | 컬럼 구분 | 콤마(`,`)를 줄 앞에 배치합니다. |
| 08 | 별칭 공백 | `as` 양옆에는 공백을 한 칸씩 사용합니다. |
| 09 | 주요 절 | `select`, `from`, `where`는 새 줄에서 시작하고, 첫 대상은 같은 줄에 작성합니다. |
| 10 | 조인 방식 | ANSI SQL 및 ANSI JOIN을 사용하고, 조인 조건은 `on`으로 명시합니다. |

## 상세 작성 기준

### 기본 표기와 별칭

- 문자열 리터럴 `'SQ'`, `'Y'`, `'P1W'`, `'YYYYMMDD'` 등의 값은 원본 대소문자를 유지합니다.
- MyBatis 바인딩 변수 `#{...}`는 Java 프로퍼티와 연결되므로 이름을 변경하지 않습니다.
- 컬럼 별칭은 `area_code`, `nation_name`처럼 작성합니다.
- 테이블 별칭은 `as a`, `as b`처럼 선언하며, 각 쿼리 블록을 기준으로 순서를 부여합니다.

### 탭과 수직 정렬

- `select`, `from`, `where` 뒤에는 탭을 넣고 첫 컬럼·테이블·조건을 같은 줄에 배치합니다.
- 다음 컬럼의 콤마는 `select`의 들여쓰기보다 한 탭 안쪽에 정렬합니다.

| 위치 | 정렬 형태 |
| --- | --- |
| 첫 컬럼 | `select` |
| 다음 컬럼 | `      컬럼명` |
| 다음 컬럼 | `    , 컬럼명` |
| 첫 테이블 | `from 테이블명` |
| 첫 조건 | `where 조건식` |

### CTE 이름과 구분

- CTE 이름에는 `cte_` 접두사를 붙입니다. 예: `cte_inp`, `cte_summary`.
- CTE를 추가할 때는 줄을 바꾸고 콤마로 시작합니다.

### 조인과 조건절

- `inner join`, `left outer join`, `cross join` 등 ANSI JOIN 구문을 사용합니다.
- `using()` 대신 `on`으로 조인 조건을 명시합니다.

### 문장 종료

마지막 SQL 구문 뒤에서 줄을 바꾸고 종료합니다.

## SQL 포맷 샘플

기존 샘플 SQL의 내용과 탭 정렬은 그대로 유지했습니다. 샘플에는 SQL Server 전용 문법과
기존 식별자 표기가 포함되어 있으므로, 실제 작성 시 대상 DB 문법과 위 명명 규칙을 함께 적용합니다.

### 조회 · 서브쿼리 · 조인

```sql
select
      c.fg_seq
    , iif (a.sc_type = 1, 'F', 'N') as gGroup
    , (
        select
              sctName
            , sctId
        from dbo.infozent_sct
        where sct = a.sct
      ) as sct_name
    , case a.sct when 'A203' then 'S'
                 else case when a.sc_type = 1 then 'F'
                           else 'N'
                      end
      end as sct_type
    , a.sct
    , getdate()
from tmp_infozent_sct as a
inner join (
        select b.fg_seq
        from customer.dbo.infozent_회원정보 as a
        inner join esv4.es.favorite_group as b on b.gCode = a.고객코드
        where a.서비스만료일 >= getdate()
    ) as c on c.fg_seq = a.fg_seq
left outer join esv4.es.favorite_group_except_sct as d on d.fg_seq = c.fg_seq and d.sct = a.sct and ((a.sc_type = 1 and d.gGroup = 'F') or (a.sc_type = 2 and d.gGroup = 'N'))
left outer join esv4.es.favorite_group_except_sct as e
             on d.fg_seq = c.fg_seq
            and d.sct = a.sct
            and ((a.sc_type = 1 and d.gGroup = 'F') or (a.sc_type = 2 and d.gGroup = 'N'))
where a.op_type = 'I'
  and a.isn in (
        select
            isn
        from dbo.inews_m
        where cno = a.cno
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

