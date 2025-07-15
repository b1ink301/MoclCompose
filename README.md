# MoclAndroid

## 다모앙, 클리앙, 네이버카페, 미코 훑어보기 안드로이드 Compose 기반 앱

---

Kotlin, MVI(orbit), Clean Architecture, 
Hilt, Compose, Build-Logic, Coroutine, Room, 
WorkManager, Paging3, toml, etc..

Html Parser : Ksoup
Html Widget : Custom View
 - Html 위젯은 일부 태그가 유실되는 버그 존재.. 
 - Markown HTML plugin 으로 대체 가능하나 video tag 지원하지 않음

### Build AppBundle : Gradle

```shell
./gradlew bundleRelease appDistributionUploadRelease
```