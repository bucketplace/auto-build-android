# auto-build-android
안드로이드 빌드 자동화 도구

### 요구사항
---
1. 매일 정해진 시간대(7시, 17시)에 qa브랜치가 빌드된다.

    1-1. 빌드결과물의 버전네임은 빌드할 때마다 증가한다.

    1-2. 빌드결과물은 디플로이게이트에 배포된다.

    1-3. qa브랜치에 머지된 지라카드들의 상태가 qa in progress로 바뀐다.
   

2. 슬랙에서 /build 명령어를 입력하면 qa브랜치가 빌드된다.


### 구현 스펙
---
**1번에 대한 구현**

- [x]  주기적으로 bitrise에서 빌드하기(오전 7시, 오후 5시)
    - [x]  bitrise에서 git으로부터 버전네임을 읽어오기(github api, app/build.gradle의 버전네임)
    - [x]  bitrise가 서버에게 빌드를 진행해도 되는지 물어보기(/builds/available?app_version=9.9.1)
    - [x]  서버는 request qa 상태의 카드 유무를 확인하기([https://docs.atlassian.com/software/jira/docs/api/REST/8.4.2/#api/2/search-search](https://docs.atlassian.com/software/jira/docs/api/REST/8.4.2/#api/2/search-search))
    - [x]  카드가 있으면 bitrise에게 true로 응답하기
- [x]  빌드 시 bitrise에서 서버로부터 새 빌드번호를 받아오기(/build_numbers/new?app_version=9.9.1)
    - [x]  서버에서 json db에 앱버전별로 빌드번호를 관리하고 새 빌드번호를 bitrise에 넘겨주기(json db library)
- [ ]  빌드 완료 시 bitrise에서 서버로 ready for qa 카드들의 상태를 다음 단계로 바꿔달라고 요청하기(/jira_issues/ready_for_qa/to/qa_in_progress?app_version=9.9.1)
    - [ ]  서버에서 ready for qa 카드들의 상태를 qa in progress로 바꾸기
    - [ ]  상태가 바뀐 카드들의 목록을 텍스트로 응답하기

**2번에 대한 구현**

- [x]  슬랙에서 서버로 build를 요청하기(/builds/request)
    - [x]  서버에서 bitrise trigger api를 호출하기
