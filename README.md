# MP3 MAX — Android starter project

A Kotlin + Jetpack Compose Android prototype with a premium black/red UI.

## Included
- Android app structure (Android Studio / Gradle Kotlin DSL)
- Dark premium home screen
- Android system file picker for user-selected media
- Output format and bitrate selection UI
- Authorized-download integration placeholder
- No broad storage permission: uses Android's Storage Access Framework

## Important implementation status
This is a **UI + Android project starter**, not a finished converter APK.
The file picker works in the app, but the actual encoding/export pipeline is intentionally not represented as complete. The Convert button currently reports that the encoding engine still needs integration.

Android's standard Media3 Transformer does not provide MP3 output as a standard supported muxer. For true MP3 encoding, integrate a maintained LAME-based native encoder (LAME is LGPL-licensed) and ensure all native notices/source-offer obligations are met. Do not simply rename AAC/WAV data to `.mp3`.

## Build
1. Install Android Studio (current stable) and Android SDK 35.
2. Open this folder in Android Studio.
3. Let Gradle sync.
4. Run on an Android device or emulator.

## Next engineering steps
1. Integrate a maintained LAME encoder module for PCM/WAV input.
2. Add media demux/decode support for FLAC/M4A/video inputs.
3. Add background conversion with WorkManager and progress notifications.
4. Add Android MediaStore/SAF export and metadata tagging.
5. Add source-specific download adapters only for providers that explicitly authorize downloading; no DRM circumvention.

## Audio quality note
Exporting at 320 kbps does not restore quality absent from a low-bitrate source. It only sets the output encoding bitrate.
