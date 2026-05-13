# `:core:domain`

비즈니스 로직을 캡슐화하는 Use Case 모음. Use Case는 `core:data`의 Repository 계약과 `core:model`만 참조하고, native/LiteRT/JNI 구현 세부사항은 알지 않습니다.

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
  subgraph :core
    direction TB
    :core:domain[domain]:::android-library
    :core:data[data]:::android-library
    :core:database[database]:::android-library
    :core:datastore[datastore]:::android-library
    :core:common[common]:::jvm-library
    :core:model[model]:::jvm-library
    :core:testing[testing]:::android-library
  end

  :core:domain --> :core:data
  :core:domain --> :core:model
  :core:domain -.-> :core:testing
  :core:data --> :core:model
  :core:data -.-> :core:database
  :core:data -.-> :core:datastore
  :core:data -.-> :core:common
  :core:database --> :core:model
  :core:datastore --> :core:model
  :core:testing --> :core:data
  :core:testing --> :core:model

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

Arrow legend: `-->` = `api()` &nbsp;·&nbsp; `-.->` = `implementation()` / `testImplementation()`
<!--endregion-->
