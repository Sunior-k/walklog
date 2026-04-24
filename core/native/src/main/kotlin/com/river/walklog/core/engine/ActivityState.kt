package com.river.walklog.core.engine

/**
 * User activity state classified by [ActivityClassifier].
 * Ordinal order must match the model's output class indices.
 */
enum class ActivityState {
    WALKING,
    STATIONARY,
    UNKNOWN,
}
