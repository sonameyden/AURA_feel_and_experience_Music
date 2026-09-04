package com.aura.core.model

/**
 * Visual quality tier — Section 44/11 of the project spec. Controls
 * kaleidoscope ring count, particle count, and secondary-geometry density.
 * Not yet persisted anywhere real — SettingsScreen's TODO (Phase 5) is to
 * wire this to an actual user-facing selector via DataStore. Until then,
 * every caller defaults to Medium.
 */
enum class VisualIntensity {
    Low,
    Medium,
    High
}
