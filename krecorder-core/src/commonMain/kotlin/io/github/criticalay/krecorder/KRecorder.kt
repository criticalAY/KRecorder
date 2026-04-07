package io.github.criticalay.krecorder

import io.github.criticalay.krecorder.config.RecorderConfig
import io.github.criticalay.krecorder.state.RecorderState
import kotlinx.coroutines.flow.StateFlow

/**
 * KRecorder — A Kotlin Multiplatform audio recording library.
 *
 * ## Usage
 * ```kotlin
 * val recorder = KRecorder.create(
 *     config = RecorderConfig(
 *         sampleRate = SampleRate.RATE_44100,
 *         channels = Channels.MONO,
 *         encoding = AudioEncoding.AAC,
 *         format = OutputFormat.MP4,
 *     )
 * )
 *
 * // Observe state reactively
 * recorder.state.collect { state ->
 *     println("Status: ${state.status}, Duration: ${state.formattedDuration}")
 *     // Use state.amplitude (0.0–1.0) to drive waveform UI
 * }
 *
 * recorder.start()
 * recorder.pause()
 * recorder.resume()
 * recorder.stop()    // file saved at state.value.outputPath
 * recorder.release() // free resources
 * ```
 *
 * ## Platform implementations
 * - **Android**: Uses `MediaRecorder` (API 24+) with `AudioRecord` fallback for PCM.
 * - **iOS**: Uses `AVAudioRecorder` from AVFoundation.
 *
 * ## Waveform data
 * The [state] flow emits a new [RecorderState] ~20 times/sec while recording, with
 * [RecorderState.amplitude] normalized to 0.0–1.0. Collect this to build your own
 * waveform visualization, or use the optional `krecorder-ui` module for a ready-made UI.
 */
interface KRecorder {

    /** Reactive state of the recorder. Emits amplitude updates during recording. */
    val state: StateFlow<RecorderState>

    /** The configuration this recorder was created with. */
    val config: RecorderConfig

    /** Start recording. Throws if already recording. */
    fun start()

    /** Pause the current recording. No-op if not recording. */
    fun pause()

    /** Resume a paused recording. No-op if not paused. */
    fun resume()

    /** Stop recording and finalize the output file. */
    fun stop()

    /** Release all resources. The recorder cannot be reused after this. */
    fun release()

    companion object {
        /**
         * Create a platform-specific [KRecorder] instance.
         *
         * @param config Recording configuration (sample rate, channels, encoding, format).
         */
        fun create(config: RecorderConfig = RecorderConfig()): KRecorder =
            createPlatformRecorder(config)
    }
}

/**
 * Platform-specific factory. Implemented via expect/actual.
 */
internal expect fun createPlatformRecorder(config: RecorderConfig): KRecorder
