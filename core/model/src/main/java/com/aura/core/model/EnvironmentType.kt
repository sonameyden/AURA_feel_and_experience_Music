package com.aura.core.model

/**
 * The 8 core environments from the concept spec (Section 11 / 12–19 of the
 * original documents). Each maps to exactly one Rive scenery asset —
 * see AURA_Project_Specification.md Section 8 for the asset filenames.
 */
enum class EnvironmentType {
    Heaven,
    Nature,
    Ocean,
    Dream,
    Romantic,
    Melancholic,
    Hopeful,
    Energetic;

    /**
     * Filename convention every environment's .riv asset follows.
     * Used by EnvironmentBackground to look up the right Rive file —
     * see the placeholder-first pattern in the project spec, Section 8.
     */
    fun riveAssetFileName(): String = when (this) {
        Heaven -> "env_heaven.riv"
        Nature -> "env_nature.riv"
        Ocean -> "env_ocean.riv"
        Dream -> "env_dream.riv"
        Romantic -> "env_romantic.riv"
        Melancholic -> "env_melancholic.riv"
        Hopeful -> "env_hopeful.riv"
        Energetic -> "env_energetic.riv"
    }
}
