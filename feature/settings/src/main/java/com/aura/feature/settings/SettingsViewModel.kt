package com.aura.feature.settings

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor() : ViewModel()
// TODO Phase 5: persist reduced-motion / visual-intensity-tier / cat-visibility via DataStore, expose as StateFlow
