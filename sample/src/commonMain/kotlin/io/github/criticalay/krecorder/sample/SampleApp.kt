package io.github.criticalay.krecorder.sample

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.github.criticalay.krecorder.KRecorder
import io.github.criticalay.krecorder.config.AudioEncoding
import io.github.criticalay.krecorder.config.Channels
import io.github.criticalay.krecorder.config.OutputFormat
import io.github.criticalay.krecorder.config.RecorderConfig
import io.github.criticalay.krecorder.config.SampleRate
import io.github.criticalay.krecorder.ui.RecorderView

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SampleApp() {
    var selectedSampleRate by remember { mutableStateOf(SampleRate.RATE_44100) }
    var selectedChannels by remember { mutableStateOf(Channels.MONO) }
    var selectedEncoding by remember { mutableStateOf(AudioEncoding.AAC) }
    var selectedFormat by remember { mutableStateOf(OutputFormat.MP4) }

    val recorder = remember(selectedSampleRate, selectedChannels, selectedEncoding, selectedFormat) {
        KRecorder.create(
            RecorderConfig(
                sampleRate = selectedSampleRate,
                channels = selectedChannels,
                encoding = selectedEncoding,
                format = selectedFormat,
            )
        )
    }

    MaterialTheme(colorScheme = darkColorScheme()) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding(),
            ) {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                    Text(
                        text = "KRecorder",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = "KMP Audio Recording Library Demo",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .verticalScroll(rememberScrollState()),
                ) {
                    ConfigSection("Sample Rate") {
                        SampleRate.entries.forEach { rate ->
                            FilterChip(
                                selected = selectedSampleRate == rate,
                                onClick = { selectedSampleRate = rate },
                                label = { Text("${rate.hz} Hz") },
                            )
                        }
                    }
                    ConfigSection("Channels") {
                        Channels.entries.forEach { ch ->
                            FilterChip(
                                selected = selectedChannels == ch,
                                onClick = { selectedChannels = ch },
                                label = { Text(ch.name) },
                            )
                        }
                    }
                    ConfigSection("Encoding") {
                        AudioEncoding.entries.forEach { enc ->
                            FilterChip(
                                selected = selectedEncoding == enc,
                                onClick = { selectedEncoding = enc },
                                label = { Text(enc.name) },
                            )
                        }
                    }
                    ConfigSection("Format") {
                        OutputFormat.entries.forEach { fmt ->
                            FilterChip(
                                selected = selectedFormat == fmt,
                                onClick = { selectedFormat = fmt },
                                label = { Text(fmt.name) },
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                RecorderView(
                    recorder = recorder,
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    onRecordingSaved = { path ->
                        println("Recording saved: $path")
                    },
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ConfigSection(title: String, content: @Composable () -> Unit) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp),
    )
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalArrangement = Arrangement.spacedBy(0.dp),
    ) {
        content()
    }
}
