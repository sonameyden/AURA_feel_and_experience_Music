package com.aura.feature.nowplaying.visuals

import android.content.Context
import app.rive.runtime.kotlin.core.Rive
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Rive requires a one-time Rive.init(context) call before any RiveAnimationView
 * is created. Since only feature:nowplaying depends on rive-android (not the
 * app module), this is done lazily on first use here rather than in
 * AuraApplication — simplest option without adding a Rive dependency to a
 * module that otherwise wouldn't need it.
 */
object RiveInitializer {
    private val initialized = AtomicBoolean(false)

    fun ensureInitialized(context: Context) {
        if (initialized.compareAndSet(false, true)) {
            runCatching { Rive.init(context) }
        }
    }
}
