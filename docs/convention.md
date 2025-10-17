# OndeDayEconomy 컨벤션

본 문서는 OndeDayEconomy 프로젝트의 Git 워크플로우와 디렉터리 구조 규칙을 명확하고 일관되게 적용하기 위한 가이드입니다. Codex 및 기타 AI 도구도 이 규칙을 그대로 따릅니다.

---

## 폴더 구조 — 도메인 중심 설계

도메인(비즈니스 로직) 기준으로 파일을 구성하여 유지보수성과 확장성을 극대화합니다.

```
src/
 ├── domain/
 │    ├── keyword/
 │    ├── quiz/
 │    ├── user/
 │    └── news/
 ├── global/
 │    ├── config/
 │    ├── exception/
 │    └── util/
 ├── infra/
 └── api/
```

- 목적: `domain` 하위의 각 폴더는 하나의 비즈니스 모듈을 의미합니다.
- 공용: `global` 디렉터리는 프로젝트 전반에서 공유되는 설정, 예외, 유틸리티를 보관합니다.

---

## 브랜치 네이밍 규칙

이슈 단위로 브랜치를 생성하며, 작업 유형과 이슈 번호를 조합합니다.

- 패턴: `type/issue-number`
- 예시:
    - `feat/1`        → 신규 기능 구현
    - `refactor/5`    → 코드 리팩터링
    - `fix/125`       → 버그 수정

추가 예: `feat/12`, `fix/44`, `refactor/101`

---

## 커밋 메시지 컨벤션

### 제목 형식

`<type>: <title> (#issue-number)`

- Type: 항상 소문자 사용
- Title: 명사형으로 간결히 작성 (동사/문장 형태 지양)
- 마침표(`.`) 금지
- 이슈 번호는 괄호로 감싸서 제목 끝에 표기

예시

```
feat: create keyword domain (#12)
fix: resolve news crawling error (#45)
refactor: improve quiz API performance (#67)
```

### 커밋 타입(태그)

- `feat`: 새로운 기능 추가
- `add`: 파일/리소스 추가 (예: yml, 설정 파일)
- `fix`: 버그 수정
- `refactor`: 리팩터링(동작 변화 없이 구조/성능 개선)
- `style`: 스타일/포맷/주석 변경(로직 변화 없음)
- `docs`: 문서 변경(README, 가이드 등)
- `test`: 테스트 코드 추가/수정/삭제(비즈니스 로직 변경 없음)
- `build`: 빌드 스크립트/의존성 변경
- `chore`: 기타 잡무(에셋, 패키지 매니저 등)
- `rename`: 파일/폴더명 또는 위치 변경만 수행
- `remove`: 파일 삭제만 수행
- `hotfix`: 긴급 수정(프로덕션 급한 패치)

### 본문(Body) 가이드

- 제목과 본문은 한 줄 공백으로 구분
- How(어떻게)보다 What/Why(무엇을/왜)를 중심으로 작성
- 여러 변경/이유가 있을 경우 불릿 포인트 사용 권장

예시

```
feat: implement news data crawling logic (#15)

- 다수 언론사 크롤러 추가
- 키워드 기반 데이터 수집을 통해 분석 가능
```

---

## Codex 요약

- 브랜치 형식: `type/issue-number`
- 커밋 제목 형식: `<type>: <명사구> (#issue-number)`
- 제목 끝 마침표 금지
- 커밋 본문: 변경의 의도(왜)와 내용(무엇)을 간결히 설명

위 규칙을 준수하면 일관된 이력 관리와 자동화된 워크플로우 구성에 도움이 됩니다.

