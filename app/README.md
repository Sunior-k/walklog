# `:app`

앱 진입점. `WalkLogApplication`(Hilt 루트), `MainActivity`(BottomNavigationView + XML NavGraph), 모든 기능 모듈의 DI 바인딩 조합.

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
  :app[app]:::android-application

  subgraph :feature
    direction TB
    :feature:home[home]:::android-feature
    :feature:mission[mission]:::android-feature
    :feature:recap[recap]:::android-feature
    :feature:report[report]:::android-feature
    :feature:history[history]:::android-feature
    :feature:forecast[forecast]:::android-feature
    :feature:onboarding[onboarding]:::android-feature
    :feature:settings[settings]:::android-feature
    :feature:widget[widget]:::android-feature
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
    :core:designsystem[designsystem]:::android-library
  end

  :app -.-> :feature:home
  :app -.-> :feature:mission
  :app -.-> :feature:recap
  :app -.-> :feature:report
  :app -.-> :feature:history
  :app -.-> :feature:forecast
  :app -.-> :feature:onboarding
  :app -.-> :feature:settings
  :app -.-> :feature:widget
  :app -.-> :core:analytics
  :app -.-> :core:data
  :app -.-> :core:database
  :app -.-> :core:datastore
  :app -.-> :core:designsystem

  :feature:home -.-> :core:domain
  :feature:mission -.-> :core:domain
  :feature:recap -.-> :core:domain
  :feature:report -.-> :core:domain
  :feature:history -.-> :core:domain
  :feature:forecast -.-> :core:domain
  :feature:widget -.-> :core:domain
  :feature:onboarding -.-> :core:data
  :feature:settings -.-> :core:data

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
