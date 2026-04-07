package io.github.criticalay.krecorder

import android.content.Context

internal var appContext: Context? = null

/**
 * Initialize KRecorder with an Android [Context].
 * Call this once in your Application.onCreate() or Activity.onCreate().
 *
 * ```kotlin
 * class MainActivity : ComponentActivity() {
 *     override fun onCreate(savedInstanceState: Bundle?) {
 *         super.onCreate(savedInstanceState)
 *         initKRecorder(applicationContext)
 *     }
 * }
 * ```
 */
actual fun initKRecorder(context: Any) {
    appContext = (context as Context).applicationContext
}
