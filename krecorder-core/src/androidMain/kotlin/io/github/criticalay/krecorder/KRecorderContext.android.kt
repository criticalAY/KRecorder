/*
 * Copyright 2026 Ashish Yadav <mailtoashish693@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
