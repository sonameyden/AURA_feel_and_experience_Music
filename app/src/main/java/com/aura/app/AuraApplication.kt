package com.aura.app

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Application entry point. Root of the Hilt dependency graph.
 * All modules (core:data, core:audio, core:common, every feature module)
 * hang off this graph — nothing in the app constructs its own singletons manually.
 */
@HiltAndroidApp
class AuraApplication : Application()
