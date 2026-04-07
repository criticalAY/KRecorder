package io.github.criticalay.krecorder.config

/**
 * Audio sample rate in Hz.
 */
enum class SampleRate(val hz: Int) {
    RATE_8000(8000),
    RATE_16000(16000),
    RATE_22050(22050),
    RATE_44100(44100),
    RATE_48000(48000),
}

/**
 * Number of audio channels.
 */
enum class Channels(val count: Int) {
    MONO(1),
    STEREO(2),
}

/**
 * Audio encoding/codec.
 */
enum class AudioEncoding {
    /** AAC (Advanced Audio Coding) — best quality/size ratio, widely supported. */
    AAC,

    /** PCM 16-bit — uncompressed, lossless, large files. */
    PCM_16BIT,

    /** AMR Narrowband — small files, low quality. Good for voice memos. */
    AMR_NB,

    /** AMR Wideband — better quality than AMR_NB, still compact. */
    AMR_WB,

    /** Opus — modern, excellent quality at low bitrates. */
    OPUS,
}

/**
 * Output container format for the recorded file.
 */
enum class OutputFormat {
    /** MPEG-4 container (.m4a / .mp4) — pairs with AAC. */
    MP4,

    /** 3GPP container (.3gp) — pairs with AMR. */
    THREE_GPP,

    /** OGG container (.ogg) — pairs with Opus. */
    OGG,

    /** WAV container (.wav) — pairs with PCM. */
    WAV,
}

/**
 * Configuration for an audio recording session.
 *
 * @param sampleRate Audio sample rate. Default [SampleRate.RATE_44100].
 * @param channels Mono or stereo. Default [Channels.MONO].
 * @param encoding Audio codec. Default [AudioEncoding.AAC].
 * @param format Output container format. Default [OutputFormat.MP4].
 * @param outputPath Full path for the output file. Platform-specific.
 *                   If null, a temp file is created in the platform's cache directory.
 */
data class RecorderConfig(
    val sampleRate: SampleRate = SampleRate.RATE_44100,
    val channels: Channels = Channels.MONO,
    val encoding: AudioEncoding = AudioEncoding.AAC,
    val format: OutputFormat = OutputFormat.MP4,
    val outputPath: String? = null,
)
