# KRecorder

A Kotlin Multiplatform audio recording library for Android and iOS.

## Features

- **KMP-first** — single API for Android and iOS
- **Configurable** — sample rate, channels, encoding (AAC/PCM/AMR/Opus), output format
- **Reactive** — `StateFlow<RecorderState>` with amplitude, duration, status
- **Waveform-ready** — amplitude normalized to 0.0–1.0 at ~20fps for visualization
- **Optional UI** — ready-made `RecorderView` + standalone `WaveformView`, or bring your own
- **Lifecycle-safe** — pause/resume/stop/release

## Modules

```
KRecorder/
├── krecorder-core/   ← Recording engine. No UI dependency.
├── krecorder-ui/     ← Optional Compose Multiplatform UI.
└── sample/           ← Demo app showcasing all features.
```

| Module | Description |
|--------|-------------|
| `krecorder-core` | Recording interface + platform implementations. Android: `MediaRecorder`. iOS: `AVAudioRecorder`. |
| `krecorder-ui` | Optional Compose UI: `RecorderView` (full recorder) + `WaveformView` (standalone waveform bars). |
| `sample` | Runnable demo app with config pickers and live recording. |

## Demo App

The `sample` module demonstrates all library features:

- **Config pickers** — tap chips to change sample rate, channels, encoding, and format live
- **Live recording** — waveform visualization, timer with red recording indicator
- **Pause/Resume/Stop** — full lifecycle control
- **Dark theme** — Material3 dark color scheme

### Run the demo

```bash
# Android
./gradlew :sample:installDebug

# iOS (via Xcode)
open iosApp/iosApp.xcodeproj
```

## Quick Start

### Core only (bring your own UI)

```kotlin
val recorder = KRecorder.create(
    config = RecorderConfig(
        sampleRate = SampleRate.RATE_44100,
        channels = Channels.MONO,
        encoding = AudioEncoding.AAC,
        format = OutputFormat.MP4,
    )
)

// Observe state reactively
recorder.state.collect { state ->
    println("${state.formattedDuration} | Amplitude: ${state.amplitude}")
}

recorder.start()
recorder.pause()
recorder.resume()
recorder.stop()     // file at state.value.outputPath
recorder.release()  // free resources
```

### With built-in UI

```kotlin
val recorder = remember { KRecorder.create() }

RecorderView(
    recorder = recorder,
    onRecordingSaved = { filePath -> },
)
```

### Standalone waveform

```kotlin
WaveformView(
    amplitudes = amplitudes,   // List<Float> of 0.0–1.0 values
    barColor = Color.White,
    barWidth = 3.dp,
    barSpacing = 2.dp,
    height = 200.dp,
)
```

## Configuration

| Option | Values | Default |
|--------|--------|---------|
| Sample Rate | `8000`, `16000`, `22050`, `44100`, `48000` Hz | `44100` |
| Channels | `MONO`, `STEREO` | `MONO` |
| Encoding | `AAC`, `PCM_16BIT`, `AMR_NB`, `AMR_WB`, `OPUS` | `AAC` |
| Format | `MP4`, `THREE_GPP`, `OGG`, `WAV` | `MP4` |
| Output Path | Custom path or `null` (auto temp file) | `null` |

## State

`StateFlow<RecorderState>` fields:

| Field | Type | Description |
|-------|------|-------------|
| `status` | `RecorderStatus` | `IDLE` / `RECORDING` / `PAUSED` / `STOPPED` / `ERROR` |
| `durationMs` | `Long` | Elapsed time in milliseconds |
| `amplitude` | `Float` | Audio level 0.0–1.0, updated ~20fps |
| `outputPath` | `String?` | Output file path |
| `formattedDuration` | `String` | `MM:SS.T` format |

## Platform Setup

### Android
```xml
<uses-permission android:name="android.permission.RECORD_AUDIO" />
```

### iOS
```xml
<key>NSMicrophoneUsageDescription</key>
<string>This app needs microphone access to record audio.</string>
```

## Architecture

```
┌─────────────────────────────────────────┐
│              Your App                    │
│  ┌────────────┐  ┌───────────────────┐  │
│  │krecorder-ui│  │ Your custom UI    │  │
│  │RecorderView│  │ WaveformView      │  │
│  └─────┬──────┘  └────────┬──────────┘  │
│        └────────┬─────────┘             │
│        ┌────────┴─────────┐             │
│        │  krecorder-core  │             │
│        │ KRecorder + Flow │             │
│        ├────────┬─────────┤             │
│        │Android │   iOS   │             │
│        │MediaRec│AVAudioRec             │
│        └────────┴─────────┘             │
└─────────────────────────────────────────┘
```

## Tech Stack

- Kotlin 2.3.20 / Compose Multiplatform 1.10.3
- Kotlinx Coroutines 1.10.2 / Gradle 8.14.3
- Android: MediaRecorder / iOS: AVAudioRecorder

## License

Apache 2.0
