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

package io.github.criticalay.krecorder.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.criticalay.krecorder.KRecorder
import io.github.criticalay.krecorder.state.RecorderStatus

/**
 * Full recorder UI with waveform visualization, timer, and controls.
 *
 * Matches the style of iOS Voice Memos / Android Sound Recorder:
 * - Large waveform area in a dark card
 * - Recording timer with red indicator dot
 * - Pause and Stop buttons
 *
 * ## Usage
 * ```kotlin
 * val recorder = remember { KRecorder.create() }
 *
 * RecorderView(
 *     recorder = recorder,
 *     onRecordingSaved = { filePath ->
 *         // Handle saved recording
 *     },
 * )
 * ```
 *
 * @param recorder A [KRecorder] instance.
 * @param onRecordingSaved Called with the output file path when recording stops.
 * @param waveformColor Color for waveform bars. Defaults to white.
 * @param cardColor Background color for the waveform card. Defaults to dark surface.
 * @param accentColor Color for the stop button and recording indicator. Defaults to error color.
 */
@Composable
fun RecorderView(
    recorder: KRecorder,
    modifier: Modifier = Modifier,
    onRecordingSaved: (String) -> Unit = {},
    waveformColor: Color = Color.White,
    cardColor: Color = MaterialTheme.colorScheme.surfaceContainerHighest,
    accentColor: Color = MaterialTheme.colorScheme.error,
) {
    val state by recorder.state.collectAsState()
    val amplitudes = remember { mutableStateListOf<Float>() }

    // Collect amplitude samples
    if (state.isRecording) {
        amplitudes.add(state.amplitude)
    }
    if (state.status == RecorderStatus.STOPPED) {
        val path = state.outputPath
        if (path != null) {
            onRecordingSaved(path)
        }
    }

    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // ── Waveform card ──
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 16.dp),
            shape = RoundedCornerShape(24.dp),
            color = cardColor,
        ) {
            Box(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                contentAlignment = Alignment.Center,
            ) {
                WaveformView(
                    amplitudes = amplitudes,
                    barColor = waveformColor,
                    modifier = Modifier.fillMaxWidth(),
                    height = 200.dp,
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // ── Recording indicator + timer ──
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            if (state.isRecording) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .background(accentColor, CircleShape),
                )
                Spacer(modifier = Modifier.width(10.dp))
            }
            Text(
                text = state.formattedDuration,
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Light,
                letterSpacing = 2.sp,
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // ── Controls ──
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            when (state.status) {
                RecorderStatus.IDLE -> {
                    Button(
                        onClick = { recorder.start() },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        contentPadding = PaddingValues(vertical = 16.dp),
                    ) {
                        Text("Start recording", fontWeight = FontWeight.SemiBold)
                    }
                }

                RecorderStatus.RECORDING -> {
                    // Pause button
                    Button(
                        onClick = { recorder.pause() },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(20.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                            contentColor = MaterialTheme.colorScheme.onSurface,
                        ),
                        contentPadding = PaddingValues(vertical = 16.dp),
                    ) {
                        Text("\u2759\u2759  Pause", fontWeight = FontWeight.SemiBold)
                    }
                    // Stop button
                    Button(
                        onClick = {
                            recorder.stop()
                            amplitudes.clear()
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(20.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = accentColor.copy(alpha = 0.15f),
                            contentColor = accentColor,
                        ),
                        contentPadding = PaddingValues(vertical = 16.dp),
                    ) {
                        Text("\u25A0  Stop", fontWeight = FontWeight.SemiBold)
                    }
                }

                RecorderStatus.PAUSED -> {
                    // Resume button
                    Button(
                        onClick = { recorder.resume() },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(20.dp),
                        contentPadding = PaddingValues(vertical = 16.dp),
                    ) {
                        Text("\u25B6  Resume", fontWeight = FontWeight.SemiBold)
                    }
                    // Stop button
                    Button(
                        onClick = {
                            recorder.stop()
                            amplitudes.clear()
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(20.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = accentColor.copy(alpha = 0.15f),
                            contentColor = accentColor,
                        ),
                        contentPadding = PaddingValues(vertical = 16.dp),
                    ) {
                        Text("\u25A0  Stop", fontWeight = FontWeight.SemiBold)
                    }
                }

                RecorderStatus.STOPPED -> {
                    Button(
                        onClick = {
                            recorder.release()
                            amplitudes.clear()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        contentPadding = PaddingValues(vertical = 16.dp),
                    ) {
                        Text("New recording", fontWeight = FontWeight.SemiBold)
                    }
                }

                RecorderStatus.ERROR -> {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = state.errorMessage ?: "An error occurred",
                            color = accentColor,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = { recorder.release() },
                            shape = RoundedCornerShape(20.dp),
                        ) {
                            Text("Try again")
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}
