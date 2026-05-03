# `:feature:settings`

설정 화면. XML Fragment + ViewBinding 구현. `UserSettingsRepository`를 직접 주입하므로 `core:domain`이 아닌 `core:data`에 의존합니다.

- 일일 걸음 목표 변경 (SeekBar, 5,000~20,000보, 500보 단위)
- 회복 미션 걸음 수 조정 (SeekBar, 3,000보~)
- 알림 On/Off 토글
- 라이트 / 다크 / 시스템 테마 선택 → `AppCompatDelegate.setDefaultNightMode()` 즉시 적용
- `DataStore`에 실시간 저장, 앱 재시작 없이 영구 유지

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
    :feature:settings[settings]:::android-feature
  end

  subgraph :core
    direction TB
    :core:data[data]:::android-library
    :core:database[database]:::android-library
    :core:datastore[datastore]:::android-library
    :core:common[common]:::jvm-library
    :core:model[model]:::jvm-library
    :core:analytics[analytics]:::android-library
  end

  :feature:settings -.-> :core:data
  :feature:settings -.-> :core:analytics
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
