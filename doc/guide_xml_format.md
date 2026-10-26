01. 쿼리는 문자열 제외하고 키워드 포함 모두 소문자 사용
02. 컬럼 alias의 단어 구분은 _ 사용
03. 테이블 alias는 a b c 순서로 사용
04. 테이블이 1개일 때 테이블 alias 생략
05. 테이블 alias as 붙이기
06. 컬럼 구분 콤마는 줄 앞에 사용
07. 2개 이상의 공백일 경우 탭 사용
08. alias 사이 공백 1개 사용
09. select from where 키워드 이전 줄 바꿈, 다음 줄 바꿈 하지 말 것
10. 쿼리는 ansi sql, ansi join 사용
11. join, apply 이후에 줄 바꿈 할 것
12. on, and, or 이전에 줄 바꿈 할 것
13. 세미 콜론 줄 바꿈 후 출력 할 것


1. 기본 표기 및 명명 규칙
- 소문자 전환: 문자열 내부 상수('SQ', 'Y', 'P1W' 등) 및 MyBatis 바인딩 변수(#{...})를 제외한 모든 SQL 키워드, 함수명, 데이터 타입은 100% 소문자로 작성합니다.
- 컬럼 Alias: 단어 구분 시 언더바(_)를 사용합니다. (Snake Case)
- 테이블 Alias 순서: 한 블록(절) 내에서 여러 테이블을 사용할 경우 a, b, c 순서로 순차 배정하며, 반드시 as 키워드를 명시합니다. (as a, as b)
- 단일 테이블 Alias 생략: 스캔하는 테이블이 1개일 경우에는 테이블 alias를 철저히 생략합니다.
- Alias 공백: Alias 앞의 as 양옆에는 공백 1개(as)를 사용합니다.
2. 들여쓰기 및 탭(Tab) 정렬 규칙 (Vertical Alignment)
- 기본 공백 규칙: 2개 이상의 연속된 공백은 스페이스가 아닌 탭(\t)을 사용합니다.
- 키워드 직후 시작: select, from, where 등 주요 키워드 이전 및 직후에는 줄바꿈을 하지 않으며, 키워드 바로 뒤에 탭(\t)을 주고 대상(첫 컬럼, 테이블)을 배치합니다.
- 선두 콤마 탭 위치 규칙:
   - 컬럼 구분 콤마(,)는 줄 맨 앞에 위치시킵니다.
   - select 절이 시작된 들여쓰기 깊이(탭 수준)에서 정확히 1 탭(\t) 들어간 위치에 콤마가 수직으로 일치하도록 정렬합니다.
     - (예: N번 탭 위치의 select면, 다음 줄 컬럼들은 N+1번 탭 위치에 \t,\t컬럼명 형태로 정렬)
- CTE(공통 테이블 표현식) 규칙:
   - CTE 명칭에는 반드시 cte_ 접두사를 붙입니다. (예: cte_inp, cte_summary)
   - 신규 CTE가 추가될 때는 줄바꿈 후 콤마(,)로 시작하여 작성합니다.
3. 조인(Join) 및 조건절 규칙
- 표준 ANSI SQL: 모든 조인은 left outer join, inner join, cross join 등 ANSI Join 구성을 준수하며, using() 구문 대신 on 절로 완벽히 명시합니다.
- Join / Apply 줄바꿈: join 또는 apply 키워드 선언 이후에는 줄바꿈을 적용합니다.
- 조건절 줄바꿈: on, where, and, or 키워드 이전에 줄바꿈을 적용합니다.
4. 문장 종료 및 기타 규칙
- 세미콜론 독립 행: 쿼리의 마지막 문장이 끝난 후 줄바꿈을 한 뒤 독립된 행에 세미콜론(;)을 출력합니다.
- 문자열 보존: 'Y', 'N', 'SQ', 'YYYYMMDD' 등 문자열 리터럴 내부의 값은 대소문자를 변경하지 않고 원본 그대로 유지합니다.


# 쿼리는 아래 샘플에 맞춰서 formatting 할 것
select	c.fg_seq
	,	iif (a.sc_type = 1, 'F', 'N') as gGroup
	,	(
			select	sctName
			from	dbo.infozent_sct
			where	sct = a.sct
		) as sct_name
	,	case a.sct
			when 'A203' then 'S'
			else
				case
					when a.sc_type = 1 then 'F'
					else 'N'
				end
		end as sct_type
	,	a.sct
	,	getdate()
from	#tmp_infozent_sct as a
		inner join (
			select	b.fg_seq
			from	customer.dbo.infozent_회원정보 as a
					inner join
					esv4.es.favorite_group as b
				on	b.gCode = a.고객코드
			where	a.서비스만료일 >= getdate()
		) as c
	on	c.fg_seq = a.fg_seq
		left outer join
		esv4.es.favorite_group_except_sct as d
	on	d.fg_seq = c.fg_seq
	and d.sct = a.sct
	and (	(a.sc_type = 1 and d.gGroup = 'F')
		or	(a.sc_type = 2 and d.gGroup = 'N')
		)
where	a.op_type = 'I'
	and a.isn in (
			select	isn
			from	dbo.inews_m
			where	cno = a.cno
		)
;

create table tSample
(	col_1 int
,	col_2 varchar(10)
);

insert into tSample
	(	col_1
	,	col_2
	)
values
	(	1
	,	'a'
	);

update tSample
set		col_2 = 'b'
where	col_1 = 1
;

create index ix_sample_01
on t_sample
(	col_1
,	col_2
);