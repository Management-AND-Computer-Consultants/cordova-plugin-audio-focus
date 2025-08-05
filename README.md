# Cordova Audio Focus Plugin

A Cordova plugin that provides audio focus management for microphone usage in Cordova applications.

## Installation

```bash
cordova plugin add cordova-plugin-audio-focus
```

## Supported Platforms

- Android (API level 21+)
- iOS (iOS 9.0+)
- Browser (mock implementation)

## Usage

### Request Audio Focus

Request audio focus to gain priority access to the audio system for microphone usage:

```javascript
navigator.audioFocus.requestMicFocus(
    function(successMessage) {
        console.log('Audio focus granted: ' + successMessage);
        // Your app now has audio focus
    },
    function(errorMessage) {
        console.log('Audio focus failed: ' + errorMessage);
        // Handle the error
    }
);
```

### Release Audio Focus

Release the audio focus when your app no longer needs it:

```javascript
navigator.audioFocus.releaseMicFocus(
    function(successMessage) {
        console.log('Audio focus released: ' + successMessage);
        // Audio focus has been released
    },
    function(errorMessage) {
        console.log('Audio focus release failed: ' + errorMessage);
        // Handle the error
    }
);
```

## API Reference

### `requestMicFocus(successCallback, errorCallback)`

Requests audio focus for microphone usage.

**Parameters:**
- `successCallback` (Function): Called when audio focus is granted. Receives a success message.
- `errorCallback` (Function): Called when audio focus request fails. Receives an error message.

**Returns:** `"focus_granted"` on success, `"focus_failed"` on failure.

### `releaseMicFocus(successCallback, errorCallback)`

Releases the audio focus that was previously requested.

**Parameters:**
- `successCallback` (Function): Called when audio focus is released. Receives a success message.
- `errorCallback` (Function): Called when audio focus release fails. Receives an error message.

**Returns:** `"focus_released"` on success, `"focus_release_failed"` on failure.

### `prepareForRecording(successCallback, errorCallback)`

Prepares the audio session specifically for recording on Android 14+. This method sets up the proper audio mode and focus for microphone recording. **Use this before calling `getUserMedia()` on Android 14+ devices.**

**Parameters:**
- `successCallback` (Function): Called when audio session is ready. Receives a success message.
- `errorCallback` (Function): Called when preparation fails. Receives an error message.

**Returns:** `"recording_ready"` on success, `"recording_failed"` on failure.

**Important:** This method is especially important for Android 14+ devices where audio session management has changed.

## Platform-Specific Behavior

### Android

- Uses `AudioManager.requestAudioFocus()` for Android API 26+ (Oreo)
- Falls back to deprecated method for older Android versions
- Requires `MODIFY_AUDIO_SETTINGS` and `RECORD_AUDIO` permissions
- **Android 14+**: Uses `MODE_IN_COMMUNICATION` and specific audio attributes for better microphone access
- **Android 14+**: Requires calling `prepareForRecording()` before `getUserMedia()`

### iOS

- Uses `AVAudioSession` to manage audio focus
- Sets audio session category to `AVAudioSessionCategoryPlayAndRecord`
- Activates the audio session for recording

### Browser

- Provides mock implementation for API compatibility
- Always returns success to maintain consistent behavior

## Permissions

### Android

The plugin will automatically add the following permissions to your `AndroidManifest.xml`:

```xml
<uses-permission android:name="android.permission.MODIFY_AUDIO_SETTINGS" />
<uses-permission android:name="android.permission.RECORD_AUDIO" />
```

**Note:** The `RECORD_AUDIO` permission is required for microphone access and will be requested at runtime when you call `requestMicFocus()`.

## Example

```javascript
// For Android 14+, use prepareForRecording() before getUserMedia()
// For older versions, you can use requestMicFocus()

function startRecording() {
    // Check if we're on Android 14+ (API 34+)
    const isAndroid14Plus = cordova.platformId === 'android' && 
                           parseInt(cordova.platformVersion) >= 34;
    
    if (isAndroid14Plus) {
        // Android 14+ specific preparation
        navigator.audioFocus.prepareForRecording(
            function(successMessage) {
                console.log('Recording ready:', successMessage);
                // Now safe to call getUserMedia()
                startActualRecording();
            },
            function(errorMessage) {
                console.log('Recording preparation failed:', errorMessage);
                alert('Failed to prepare audio session. Please try again.');
            }
        );
    } else {
        // For older Android versions, use the standard method
        navigator.audioFocus.requestMicFocus(
            function(successMessage) {
                console.log('Audio focus granted:', successMessage);
                startActualRecording();
            },
            function(errorMessage) {
                console.log('Audio focus failed:', errorMessage);
                if (errorMessage === 'permission_denied') {
                    alert('Microphone permission is required for recording. Please grant permission in settings.');
                } else {
                    alert('Failed to access microphone. Please try again.');
                }
            }
        );
    }
}

function startActualRecording() {
    // Your getUserMedia() call goes here
    navigator.mediaDevices.getUserMedia({ audio: true })
        .then(function(stream) {
            // Your recording logic
            console.log('Recording started successfully');
        })
        .catch(function(error) {
            console.error('getUserMedia error:', error);
            alert('Failed to access microphone: ' + error.message);
        });
}

function startRecording() {
    // Your recording logic here
    console.log('Starting recording...');
}

// Release audio focus when done
function stopRecording() {
    navigator.audioFocus.releaseMicFocus(
        function(successMessage) {
            console.log('Audio focus released');
            // Clean up recording resources
        },
        function(errorMessage) {
            console.log('Failed to release audio focus: ' + errorMessage);
        }
    );
}
```

## License

This project is licensed under the Apache License, Version 2.0. See the [LICENSE](LICENSE) file for details.

## Contributing

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## Issues

If you find any issues, please report them on the [GitHub issues page](https://github.com/your-username/cordova-plugin-audio-focus/issues). 