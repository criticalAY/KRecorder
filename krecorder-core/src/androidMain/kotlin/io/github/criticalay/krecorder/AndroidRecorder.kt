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
import android.media.MediaRecorder
import android.os.Build
import io.github.criticalay.krecorder.config.AudioEncoding
import io.github.criticalay.krecorder.config.Channels
import io.github.criticalay.krecorder.config.OutputFormat
import io.github.criticalay.krecorder.config.RecorderConfig
import io.github.criticalay.krecorder.state.RecorderState
import io.github.criticalay.krecorder.state.RecorderStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File

/**
 * Android implementation of [KRecorder] using [MediaRecorder].
 */
internal class AndroidRecorder(
    override val config: RecorderConfig,
) : KRecorder {

    private val _state = MutableStateFlow(RecorderState())
    override val state: StateFlow<RecorderState> = _state.asStateFlow()

    private var mediaRecorder: MediaRecorder? = null
    private var amplitudeJob: Job? = null
    private var startTimeMs: Long = 0L
    private var pausedDurationMs: Long = 0L
    private val scope = CoroutineScope(Dispatchers.Main)

    private val context: Context
        get() = requireNotNull(appContext) {
            "KRecorder not initialized. Call initKRecorder(context) first."
        }

    private val outputFile: String by lazy {
        config.outputPath ?: run {
            val ext = when (config.format) {
                OutputFormat.MP4 -> "m4a"
                OutputFormat.THREE_GPP -> "3gp"
                OutputFormat.OGG -> "ogg"
                OutputFormat.WAV -> "wav"
            }
            File(context.cacheDir, "krecorder_${System.currentTimeMillis()}.$ext").absolutePath
        }
    }

    override fun start() {
        if (_state.value.status == RecorderStatus.RECORDING) return

        try {
            val recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }

            recorder.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(config.format.toAndroid())
                setAudioEncoder(config.encoding.toAndroid())
                setAudioSamplingRate(config.sampleRate.hz)
                setAudioChannels(config.channels.count)
                setOutputFile(outputFile)
                prepare()
                start()
            }

            mediaRecorder = recorder
            startTimeMs = System.currentTimeMillis()
            pausedDurationMs = 0L
            _state.value = RecorderState(
                status = RecorderStatus.RECORDING,
                outputPath = outputFile,
            )
            startAmplitudePolling()
        } catch (e: Exception) {
            _state.value = RecorderState(
                status = RecorderStatus.ERROR,
                errorMessage = e.message ?: "Failed to start recording",
            )
        }
    }

    override fun pause() {
        if (_state.value.status != RecorderStatus.RECORDING) return
        try {
            mediaRecorder?.pause()
            pausedDurationMs = System.currentTimeMillis() - startTimeMs
            amplitudeJob?.cancel()
            _state.value = _state.value.copy(
                status = RecorderStatus.PAUSED,
                amplitude = 0f,
            )
        } catch (e: Exception) {
            _state.value = _state.value.copy(
                status = RecorderStatus.ERROR,
                errorMessage = e.message,
            )
        }
    }

    override fun resume() {
        if (_state.value.status != RecorderStatus.PAUSED) return
        try {
            mediaRecorder?.resume()
            startTimeMs = System.currentTimeMillis() - pausedDurationMs
            _state.value = _state.value.copy(status = RecorderStatus.RECORDING)
            startAmplitudePolling()
        } catch (e: Exception) {
            _state.value = _state.value.copy(
                status = RecorderStatus.ERROR,
                errorMessage = e.message,
            )
        }
    }

    override fun stop() {
        if (_state.value.status != RecorderStatus.RECORDING &&
            _state.value.status != RecorderStatus.PAUSED
        ) return

        try {
            amplitudeJob?.cancel()
            mediaRecorder?.stop()
            _state.value = _state.value.copy(
                status = RecorderStatus.STOPPED,
                amplitude = 0f,
            )
        } catch (e: Exception) {
            _state.value = _state.value.copy(
                status = RecorderStatus.ERROR,
                errorMessage = e.message,
            )
        }
    }

    override fun release() {
        amplitudeJob?.cancel()
        try {
            mediaRecorder?.release()
        } catch (_: Exception) { }
        mediaRecorder = null
        _state.value = RecorderState()
    }

    private fun startAmplitudePolling() {
        amplitudeJob?.cancel()
        amplitudeJob = scope.launch {
            while (isActive && _state.value.status == RecorderStatus.RECORDING) {
                val maxAmplitude = try {
                    mediaRecorder?.maxAmplitude ?: 0
                } catch (_: Exception) { 0 }

                // Normalize: MediaRecorder.maxAmplitude max is ~32767
                val normalized = (maxAmplitude / 32767f).coerceIn(0f, 1f)
                val elapsed = System.currentTimeMillis() - startTimeMs

                _state.value = _state.value.copy(
                    amplitude = normalized,
                    durationMs = elapsed,
                )
                delay(50) // ~20 updates per second
            }
        }
    }
}

private fun OutputFormat.toAndroid(): Int = when (this) {
    OutputFormat.MP4 -> MediaRecorder.OutputFormat.MPEG_4
    OutputFormat.THREE_GPP -> MediaRecorder.OutputFormat.THREE_GPP
    OutputFormat.OGG -> MediaRecorder.OutputFormat.OGG
    OutputFormat.WAV -> MediaRecorder.OutputFormat.DEFAULT
}

private fun AudioEncoding.toAndroid(): Int = when (this) {
    AudioEncoding.AAC -> MediaRecorder.AudioEncoder.AAC
    AudioEncoding.PCM_16BIT -> MediaRecorder.AudioEncoder.DEFAULT
    AudioEncoding.AMR_NB -> MediaRecorder.AudioEncoder.AMR_NB
    AudioEncoding.AMR_WB -> MediaRecorder.AudioEncoder.AMR_WB
    AudioEncoding.OPUS -> MediaRecorder.AudioEncoder.OPUS
}

internal actual fun createPlatformRecorder(config: RecorderConfig): KRecorder =
    AndroidRecorder(config)
