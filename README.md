# auto-build-android
안드로이드 빌드 자동화 도구

### 개발&실행 환경
---
- 서버 인프라 : Google Cloud Platform Compute Engine
- 서버 언어 : kotlin
- 서버 프레임워크 : Ktor
- 클라이언트 : Slack Command, Bitrise Script Step
- CI 서비스 : Bitrise
- API 서비스 : Github API, Jira API, Slack API


### 요구사항
---

1. 매일 정해진 시간대(7시, 17시)에 qa브랜치가 빌드된다.

    1-1. 빌드 결과물의 앱버전이 새로운 앱버전일 때 버전네임(m.n.p.b) 빌드번호가 1로 초기화 된다.
    
    1-2. 빌드 결과물의 앱버전이 기존 앱버전과 같을 때 버전네임(m.n.p.b) 빌드번호가 1씩 올라간다.

    1-3. 빌드 결과물은 디플로이게이트에 배포된다.
    
    1-4. READY FOR QA 상태의 지라 이슈들의 상태가 QA IN PROGRESS로 바뀐다.

    1-5. 상태가 바뀐 지라 이슈들의 목록이 슬랙으로 전송된다.
    

2. 슬랙에서 /build 명령어를 입력하면 qa브랜치 빌드가 시작된다.


### 구현 스펙
---

**1번에 대한 구현**

- [x]  주기적으로 bitrise에서 빌드하기(오전 7시, 오후 5시)

    - [x]  bitrise에서 git으로부터 버전네임을 읽어오기(github api, app/build.gradle의 버전네임)
    
    - [x]  bitrise가 서버에게 빌드를 진행해도 되는지 물어보기(/builds/can?app_version=9.9.1)
    
    - [x]  서버는 READY FOR QA 상태의 지라 이슈 유무를 확인하기([https://docs.atlassian.com/software/jira/docs/api/REST/8.4.2/#api/2/search-search](https://docs.atlassian.com/software/jira/docs/api/REST/8.4.2/#api/2/search-search))

    - [x]  이슈가 있으면 bitrise에게 true로 응답하기

- [x]  빌드 시 bitrise에서 서버로부터 새 빌드번호를 받아오기(/build_numbers/new?app_version=9.9.1)

    - [x]  서버에서 json db에 앱버전별로 빌드번호를 관리하고 새 빌드번호를 bitrise에 넘겨주기(json db library)
    
- [x]  빌드 완료 시 bitrise에서 서버로 READY FOR QA 지라 이슈들의 상태를 다음 단계로 바꿔달라고 요청하기(/issues/status/change?app_version=9.9.1)

    - [x]  서버에서 READY FOR QA 지라 이슈들의 상태를 QA IN PROGRESS로 바꾸기
    
    - [x]  상태가 바뀐 이슈들의 목록을 텍스트로 응답하기
    
    - [x]  bitrise가 이슈 목록 텍스트를 slack로 전송하기

    - [x]  bitrise가 apk를 디플로이게이트로 배포하기

**2번에 대한 구현**

- [x]  슬랙에서 서버로 build를 요청하기(/builds/request)

    - [x]  서버에서 bitrise trigger api를 호출하기
