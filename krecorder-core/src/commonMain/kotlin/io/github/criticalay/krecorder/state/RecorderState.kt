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

package io.github.criticalay.krecorder.state

/**
 * Represents the current state of the audio recorder.
 */
enum class RecorderStatus {
    /** No recording in progress. Ready to start. */
    IDLE,

    /** Actively recording audio. */
    RECORDING,

    /** Recording is paused. Can be resumed or stopped. */
    PAUSED,

    /** Recording has been stopped. File is saved. */
    STOPPED,

    /** An error occurred during recording. */
    ERROR,
}

/**
 * Full state snapshot of the recorder at any point in time.
 *
 * @param status Current recording status.
 * @param durationMs Elapsed recording time in milliseconds.
 * @param amplitude Current audio amplitude normalized to 0.0–1.0.
 *                  Useful for driving waveform visualizations.
 * @param outputPath Path to the output file (set after recording starts).
 * @param errorMessage Human-readable error message if [status] is [RecorderStatus.ERROR].
 */
data class RecorderState(
    val status: RecorderStatus = RecorderStatus.IDLE,
    val durationMs: Long = 0L,
    val amplitude: Float = 0f,
    val outputPath: String? = null,
    val errorMessage: String? = null,
) {
    val isRecording: Boolean get() = status == RecorderStatus.RECORDING
    val isPaused: Boolean get() = status == RecorderStatus.PAUSED
    val isIdle: Boolean get() = status == RecorderStatus.IDLE

    /** Duration formatted as MM:SS.T */
    val formattedDuration: String
        get() {
            val totalSeconds = durationMs / 1000
            val minutes = totalSeconds / 60
            val seconds = totalSeconds % 60
            val tenths = (durationMs % 1000) / 100
            return "${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}.$tenths"
        }
}
