# `:feature:mission`

미션 상세 화면. 오늘 미션 / 회복 미션 / 달성 완료 상태를 단일 화면에서 분기 처리.

- `MissionTypeBadge` — 오늘 미션 / 회복 미션 / 달성 완료 배지 (배경색 + 텍스트 분기)
- `MissionHeadlineSection` — 미션 타입별 대제목 · 부제목
- `MissionProgressCard` — 현재/목표 걸음 수 · 남은 걸음 수 · 보상 텍스트 · `WalkLogLinearProgressBar`
- `MissionGuideCard` — peakHour 기반 추천 시간대 포함, 미션 타입별 안내 문구 3종
- Bottom CTA: "지금 걸으러 가기" / "이미 달성했어요" — 달성 시 버튼 비활성화 + 색상 전환

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
    :feature:mission[mission]:::android-feature
  end

  subgraph :core
    direction TB
    :core:data[data]:::android-library
    :core:database[database]:::android-library
    :core:datastore[datastore]:::android-library
    :core:native[native]:::android-library
    :core:common[common]:::jvm-library
    :core:model[model]:::jvm-library
    :core:analytics[analytics]:::android-library
  end

  :feature:mission -.-> :core:data
  :feature:mission -.-> :core:analytics
  :feature:mission -.-> :core:native
  :core:native --> :core:common
  :core:data --> :core:model
  :core:data -.-> :core:database
  :core:data -.-> :core:datastore
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
