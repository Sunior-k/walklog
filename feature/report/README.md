# `:feature:report`

주간 리포트 화면. 카드 기반 대시보드 UI로 구성되며 3개의 카드 섹션을 제공합니다.

- **요일별 막대 그래프 카드** — 월~일 7개 막대, 최다 걸음 요일 하이라이트
- **총 걸음 수 카드** — 주간 합계 + 아이콘
- **목표 달성 카드** — `achievedDays / 7` + `WalkLogLinearProgressBar`
- 공유 카드 미리보기 접기/펼치기 토글 ("보기" / "접기") — 하단 고정 공유 버튼
- 실 데이터 없을 때 빈 상태(Empty) UI 표시 — 더미 데이터 제거
- 빈 상태·에러 상태에서 공유 버튼 자동 숨김
- 다크 테마 완전 대응 (`WalkLogTheme.colors.*` 토큰 참조)
- Compose `GraphicsLayer` → Bitmap → `FileProvider` URI 이미지 공유

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
    :feature:report[report]:::android-feature
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

  :feature:report -.-> :core:domain
  :feature:report -.-> :core:analytics
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
