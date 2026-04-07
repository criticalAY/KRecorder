package io.github.criticalay.krecorder

import io.github.criticalay.krecorder.config.AudioEncoding
import io.github.criticalay.krecorder.config.OutputFormat
import io.github.criticalay.krecorder.config.RecorderConfig
import io.github.criticalay.krecorder.state.RecorderState
import io.github.criticalay.krecorder.state.RecorderStatus
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import platform.AVFAudio.AVAudioRecorder
import platform.AVFAudio.AVAudioSession
import platform.AVFAudio.AVAudioSessionCategoryRecord
import platform.AVFAudio.AVEncoderAudioQualityKey
import platform.AVFAudio.AVEncoderBitRateKey
import platform.AVFAudio.AVFormatIDKey
import platform.AVFAudio.AVLinearPCMBitDepthKey
import platform.AVFAudio.AVLinearPCMIsFloatKey
import platform.AVFAudio.AVNumberOfChannelsKey
import platform.AVFAudio.AVSampleRateKey
import platform.AVFAudio.setActive
import platform.CoreAudioTypes.kAudioFormatAppleLossless
import platform.CoreAudioTypes.kAudioFormatLinearPCM
import platform.CoreAudioTypes.kAudioFormatMPEG4AAC
import platform.Foundation.NSDate
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSURL
import platform.Foundation.NSUserDomainMask
import platform.Foundation.timeIntervalSince1970
import kotlin.math.abs
import kotlin.math.log10
import kotlin.math.pow

/**
 * iOS implementation of [KRecorder] using [AVAudioRecorder].
 */
@OptIn(ExperimentalForeignApi::class)
internal class IosRecorder(
    override val config: RecorderConfig,
) : KRecorder {

    private val _state = MutableStateFlow(RecorderState())
    override val state: StateFlow<RecorderState> = _state.asStateFlow()

    private var recorder: AVAudioRecorder? = null
    private var amplitudeJob: Job? = null
    private var startTimeMs: Long = 0L
    private var pausedDurationMs: Long = 0L
    private val scope = CoroutineScope(Dispatchers.Main)

    private val outputFile: String by lazy {
        config.outputPath ?: run {
            val ext = when (config.format) {
                OutputFormat.MP4 -> "m4a"
                OutputFormat.THREE_GPP -> "3gp"
                OutputFormat.OGG -> "ogg"
                OutputFormat.WAV -> "wav"
            }
            val dir = NSFileManager.defaultManager.URLForDirectory(
                directory = NSDocumentDirectory,
                inDomain = NSUserDomainMask,
                appropriateForURL = null,
                create = true,
                error = null,
            )!!
            "${dir.path}/krecorder_${currentTimeMs()}.$ext"
        }
    }

    override fun start() {
        if (_state.value.status == RecorderStatus.RECORDING) return

        try {
            val session = AVAudioSession.sharedInstance()
            session.setCategory(AVAudioSessionCategoryRecord, error = null)
            session.setActive(true, error = null)

            val settings = mapOf<Any?, Any?>(
                AVFormatIDKey to config.encoding.toIosFormatId(),
                AVSampleRateKey to config.sampleRate.hz.toDouble(),
                AVNumberOfChannelsKey to config.channels.count,
                AVEncoderAudioQualityKey to 100L, // max quality
                AVEncoderBitRateKey to 128000L,
            )

            val url = NSURL.fileURLWithPath(outputFile)
            val audioRecorder = AVAudioRecorder(url, settings, null)
            audioRecorder.setMeteringEnabled(true)
            audioRecorder.prepareToRecord()
            audioRecorder.record()

            recorder = audioRecorder
            startTimeMs = currentTimeMs()
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
        recorder?.pause()
        pausedDurationMs = currentTimeMs() - startTimeMs
        amplitudeJob?.cancel()
        _state.value = _state.value.copy(status = RecorderStatus.PAUSED, amplitude = 0f)
    }

    override fun resume() {
        if (_state.value.status != RecorderStatus.PAUSED) return
        recorder?.record()
        startTimeMs = currentTimeMs() - pausedDurationMs
        _state.value = _state.value.copy(status = RecorderStatus.RECORDING)
        startAmplitudePolling()
    }

    override fun stop() {
        if (_state.value.status != RecorderStatus.RECORDING &&
            _state.value.status != RecorderStatus.PAUSED
        ) return

        amplitudeJob?.cancel()
        recorder?.stop()
        _state.value = _state.value.copy(status = RecorderStatus.STOPPED, amplitude = 0f)
    }

    override fun release() {
        amplitudeJob?.cancel()
        recorder?.stop()
        recorder = null
        _state.value = RecorderState()
    }

    private fun startAmplitudePolling() {
        amplitudeJob?.cancel()
        amplitudeJob = scope.launch {
            while (isActive && _state.value.status == RecorderStatus.RECORDING) {
                recorder?.updateMeters()
                val db = recorder?.averagePowerForChannel(0u) ?: -160f

                // Convert dB to 0.0–1.0 (dB range: -160 to 0)
                val normalized = ((db.toFloat() + 160f) / 160f).coerceIn(0f, 1f)
                val elapsed = currentTimeMs() - startTimeMs

                _state.value = _state.value.copy(
                    amplitude = normalized,
                    durationMs = elapsed,
                )
                delay(50)
            }
        }
    }

    private fun currentTimeMs(): Long =
        (NSDate().timeIntervalSince1970 * 1000).toLong()
}

private fun AudioEncoding.toIosFormatId(): Long = when (this) {
    AudioEncoding.AAC -> kAudioFormatMPEG4AAC.toLong()
    AudioEncoding.PCM_16BIT -> kAudioFormatLinearPCM.toLong()
    AudioEncoding.AMR_NB -> kAudioFormatAppleLossless.toLong() // fallback
    AudioEncoding.AMR_WB -> kAudioFormatAppleLossless.toLong() // fallback
    AudioEncoding.OPUS -> kAudioFormatMPEG4AAC.toLong() // fallback to AAC on iOS
}

internal actual fun createPlatformRecorder(config: RecorderConfig): KRecorder =
    IosRecorder(config)
