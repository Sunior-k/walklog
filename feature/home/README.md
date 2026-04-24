# `:feature:home`

홈 화면. 실시간 걸음 수, 주간 요약 카드, 월간 리캡 카드, 걷기 예보 배너.

- `StepRepository`를 직접 주입하고 비즈니스 로직이 필요한 Use Case(`GetWeeklyStepSummaryUseCase`, `GetMonthlyRecapUseCase`)는 `core:domain`을 통해 사용
- `WalkProgressRing` — `ActivityState.WALKING` 상태일 때 링 트랙 pulse 애니메이션 (0.9초 주기로 명도 맥동)
- **걷기 예보 peakHour 보정** — 7일 중 3일 이상 실데이터가 존재하고 peakHour가 오전 6시~오후 10시 범위일 때만 표시 (데이터 희박 시 잘못된 새벽 시간대 노출 방지)
- **정지 알림** (`UserSettingsRepository.notificationsEnabled` 체크):
  - STATIONARY 상태 1시간 지속 → "지금 걸어볼까요?" 알림 발송
  - 발송 가능 시간대: 오전 9시 ~ 오후 9시
  - 발송 후 2시간 cooldown, 하루 최대 3회
  - WALKING 전환 시 타이머 즉시 초기화

## Module dependency graph

<!--region graph-->
```mermaid
---
config:
  layout: elk
  elk:
    nodePlacementStrategy: SIMPLE
---
graph TB
  subgraph :feature
    direction TB
    :feature:home[home]:::android-feature
  end

  subgraph :core
    direction TB
    :core:domain[domain]:::android-library
    :core:data[data]:::android-library
    :core:database[database]:::android-library
    :core:datastore[datastore]:::android-library
    :core:common[common]:::jvm-library
    :core:model[model]:::jvm-library
    :core:analytics[analytics]:::android-library
  end

  :feature:home -.-> :core:domain
  :feature:home -.-> :core:common
  :feature:home -.-> :core:analytics
  :core:domain --> :core:data
  :core:domain --> :core:model
  :core:data --> :core:database
  :core:data --> :core:datastore
  :core:data -.-> :core:common
  :core:database --> :core:model
  :core:datastore --> :core:model

classDef android-application fill:#CAFFBF,stroke:#000,stroke-width:2px,color:#000;
classDef android-feature fill:#FFD6A5,stroke:#000,stroke-width:2px,color:#000;
classDef android-library fill:#9BF6FF,stroke:#000,stroke-width:2px,color:#000;
classDef android-test fill:#A0C4FF,stroke:#000,stroke-width:2px,color:#000;
classDef jvm-library fill:#BDB2FF,stroke:#000,stroke-width:2px,color:#000;
```

<details><summary>📋 Graph legend</summary>

```mermaid
graph TB
  application[application]:::android-application
  feature[feature]:::android-feature
  library[library]:::android-library
  test[test]:::android-test
  jvm[jvm]:::jvm-library

classDef android-application fill:#CAFFBF,stroke:#000,stroke-width:2px,color:#000;
classDef android-feature fill:#FFD6A5,stroke:#000,stroke-width:2px,color:#000;
classDef android-library fill:#9BF6FF,stroke:#000,stroke-width:2px,color:#000;
classDef android-test fill:#A0C4FF,stroke:#000,stroke-width:2px,color:#000;
classDef jvm-library fill:#BDB2FF,stroke:#000,stroke-width:2px,color:#000;
```

</details>

Arrow legend: `-->` = `api()` &nbsp;·&nbsp; `-.->` = `implementation()`
<!--endregion-->
