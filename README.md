# MoclCompose

> 다모앙 · 클리앙 · 네이버카페 · 미코를 한 곳에서 훑어보는 안드로이드 Compose 앱

---

## 지원 사이트

| 사이트 | 파싱 방식 |
|------|---------|
| 다모앙 (Damoang) | SvelteKit hydration 스크립트 1차 / DOM 2차 fallback |
| 클리앙 (Clien) | Jsoup DOM 파싱 |
| 네이버 카페 | 모바일 카페 REST API + Retrofit |
| 미코 (Meeco) | Jsoup DOM 파싱 |

## 주요 기능

- 게시판 목록 / 상세 / 댓글 보기
- 무한 스크롤(Paging3) 기반 리스트
- 시간 표시는 모든 사이트 공통으로 `n분 전 / n시간 전` 상대시간 포맷
- 닉네임 · 시간 · 조회수를 분리된 필드로 받아 UI에서 직접 조합 (`InfoRow`)
- 본문 HTML 자체 파서/렌더러 (`htmlcompose`) — 이미지·비디오·유튜브·코드블럭·인용문·리스트 지원
- ExoPlayer 풀을 사용한 인라인 비디오 재생 (스크롤 시 자원 재사용)
- 이미지 다운로드 진행률 표시 (Coil + OkHttp progress interceptor)
- **사용자 폰트 크기 조절** — 상세화면 더보기 메뉴 → "폰트 크기"
  - 슬라이더 / +/- 버튼 / 미리보기 제공, 0.8x ~ 1.6x
  - `LocalDensity` override 방식이라 메인/목록/상세/본문 HTML 전부 자동 적용

## 기술 스택

- **언어/툴체인**: Kotlin 2.3.x, AGP 9.x, Gradle 9.x
- **UI**: Jetpack Compose (BOM `2026.05`), Material3, Fragula 네비게이션
- **아키텍처**: Clean Architecture (`app` / `domain` / `data` / `common` / `htmlcompose`)
- **상태관리**: Orbit MVI
- **DI**: Hilt
- **비동기**: Coroutines + Flow
- **네트워크**: Retrofit + OkHttp, persistent CookieJar, Gson
- **저장소**: Room, Kotpref (사용자 설정)
- **이미지**: Coil 3 + 진행률 인터셉터
- **HTML 파서**: Ksoup / Jsoup, 자체 `HtmlParser` (`htmlcompose` 모듈)
- **퍼포먼스**: Baseline Profiles, Macrobenchmark
- **CI**: GitHub Actions (Firebase App Distribution 배포)

## 모듈 구성

```
:app          — UI(Compose), 네비게이션, ViewModel, DI 진입점
:domain       — 모델, UseCase, Repository 인터페이스
:data         — Repository 구현, 사이트별 파서, Retrofit, Kotpref
:common       — 공통 유틸
:htmlcompose  — HTML → Compose 블록 변환 + 인라인 미디어 렌더러
:baselineprofile — Baseline profile 생성 모듈
:build-logic  — Convention plugin (android.application / library / compose / kotlin / test 등)
```

## 빌드

### 앱 번들 (릴리즈 + Firebase App Distribution 업로드)

```shell
./gradlew bundleRelease appDistributionUploadRelease
```

### 디버그 APK

```shell
./gradlew :app:assembleDebug
```

### Baseline Profile 생성

```shell
./gradlew :baselineprofile:pixel6Api34BenchmarkAndroidTest
```

## 키스토어

릴리즈 서명은 프로젝트 루트 상위의 `../keystore/key.properties` 파일을 우선 사용하고, 없으면 환경변수(`KEYSTORE_*`)에서 로드합니다.
