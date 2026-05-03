# `:feature:history`

월간 걸음 기록 달력 화면. XML Fragment + RecyclerView 기반 (하이브리드 구조 의도적 선택).

- `GridLayoutManager(7)` 7열 달력 그리드 — 요일 헤더 · 빈 셀 · 날짜 셀 3가지 ViewType
- 날짜 셀: 달성일(초록 원) · 오늘(테두리 원) · 부분 달성(회색 원) · 미기록(투명) 시각 구분
- `YearMonth` API 월 단위 탐색 (현재 달 이후 이동 불가)
- 하단 통계 바: 이달 총 걸음 수 · 목표 달성률
- 실 데이터만 표시 — 기록 없는 날은 미기록(투명) 셀로 렌더링

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
    :feature:history[history]:::android-feature
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

  :feature:history -.-> :core:domain
  :feature:history -.-> :core:analytics
  :core:domain --> :core:data
  :core:domain --> :core:model
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
