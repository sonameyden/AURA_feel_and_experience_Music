package com.aura.core.model

/**
 * The cat companion's animation states. Driven by the current AtmosphereProfile's
 * energy value + song section — NOT by the user (this is a mascot, not a
 * user-customized/gamified avatar, per the design decision in planning).
 */
enum class CatBehaviorState {
    Idle,
    Sleeping,
    Walking,
    Stretching,
    Playing,
    Running,
    Watching
}
