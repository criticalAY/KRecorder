package io.github.criticalay.krecorder.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Waveform visualization that renders amplitude bars.
 *
 * Feed this a list of amplitude samples (0.0–1.0) collected from
 * [io.github.criticalay.krecorder.state.RecorderState.amplitude].
 *
 * Can be used standalone without the full [RecorderView].
 *
 * @param amplitudes List of amplitude values (0.0–1.0). Newer samples at the end.
 * @param barColor Color for the waveform bars. Defaults to primary color.
 * @param barWidth Width of each bar.
 * @param barSpacing Space between bars.
 * @param maxBars Maximum number of bars to display. Older samples are cropped.
 * @param height Height of the waveform area.
 */
@Composable
fun WaveformView(
    amplitudes: List<Float>,
    modifier: Modifier = Modifier,
    barColor: Color = MaterialTheme.colorScheme.onSurface,
    barWidth: Dp = 3.dp,
    barSpacing: Dp = 2.dp,
    maxBars: Int = 100,
    height: Dp = 160.dp,
) {
    val visibleAmplitudes = remember(amplitudes.size) {
        if (amplitudes.size > maxBars) amplitudes.takeLast(maxBars) else amplitudes
    }

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(height),
    ) {
        val barWidthPx = barWidth.toPx()
        val spacingPx = barSpacing.toPx()
        val totalBarWidth = barWidthPx + spacingPx
        val centerY = size.height / 2f
        val maxBarHeight = size.height * 0.8f
        val minBarHeight = 4.dp.toPx()

        // Draw from right to left (newest at right)
        val startX = size.width - (visibleAmplitudes.size * totalBarWidth)

        visibleAmplitudes.forEachIndexed { index, amplitude ->
            val barHeight = (amplitude * maxBarHeight).coerceAtLeast(minBarHeight)
            val x = startX + (index * totalBarWidth)

            if (x >= 0) {
                drawRoundRect(
                    color = barColor,
                    topLeft = Offset(x, centerY - barHeight / 2f),
                    size = Size(barWidthPx, barHeight),
                    cornerRadius = CornerRadius(barWidthPx / 2f),
                )
            }
        }
    }
}
