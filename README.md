# cordova-plugin-audioinput

Audio input capture plugin for **Cordova** and **Capacitor** - Real-time microphone access with streaming and file recording support.

This plugin enables audio capture from the device microphone, forwarding raw audio data in (near) real-time to the web layer of your application. It provides similar functionality to `Navigator.getUserMedia()` with broader platform support.

**🎉 Version 2.0** now supports both **Cordova** and **Capacitor** from a single codebase!

## ✨ Features

- **Real-time audio streaming** - Get PCM audio data as it's captured
- **Web Audio API integration** - Use as an AudioNode in your audio processing chain
- **File recording** - Save directly to WAV files
- **Cross-platform** - Android, iOS, and browser support
- **Dual ecosystem** - Works with both Cordova and Capacitor
- **TypeScript support** - Full type definitions included
- **Optimized performance** - Buffer pooling and efficient data transfer
- **Flexible configuration** - Multiple sample rates, formats, and audio sources

## 📦 Installation

### Cordova

From the Cordova Plugin Repository:
```bash
cordova plugin add cordova-plugin-audioinput
```

Or from GitHub:
```bash
cordova plugin add https://github.com/edimuj/cordova-plugin-audioinput.git
```

### Capacitor

```bash
npm install cordova-plugin-audioinput
npx cap sync
```

## 🎯 Supported Platforms

| Platform | Cordova | Capacitor | Notes |
|----------|---------|-----------|-------|
| Android  | ✅      | ✅        | API 22+ |
| iOS      | ✅      | ✅        | iOS 13+ |
| Browser  | ✅      | ✅        | Web Audio API |

## 🚀 Quick Start

### Capacitor (TypeScript)

```typescript
import { AudioInput } from 'cordova-plugin-audioinput';

// Initialize with configuration
await AudioInput.initialize({
  sampleRate: 44100,
  bufferSize: 16384,
  channels: 1,
  format: 'PCM_16BIT',
  normalize: true
});

// Check/request microphone permission
const { granted } = await AudioInput.checkMicrophonePermission();
if (!granted) {
  await AudioInput.getMicrophonePermission();
}

// Listen for audio data
AudioInput.addListener('audioData', (event) => {
  console.log(`Received ${event.data.length} samples`);
  // Process audio data...
});

// Start capturing
await AudioInput.start({
  sampleRate: 44100,
  bufferSize: 16384
});

// Stop capturing
await AudioInput.stop();
```

### Cordova (JavaScript)

```javascript
// Check permission first
audioinput.checkMicrophonePermission(function(hasPermission) {
  if (hasPermission) {
    startCapture();
  } else {
    audioinput.getMicrophonePermission(function(granted) {
      if (granted) {
        startCapture();
      }
    });
  }
});

function startCapture() {
  // Listen for audio data
  window.addEventListener('audioinput', function(event) {
    console.log('Received ' + event.data.length + ' samples');
    // Process audio data...
  });

  // Start capturing
  audioinput.start({
    sampleRate: 44100,
    bufferSize: 16384,
    channels: 1,
    format: audioinput.FORMAT.PCM_16BIT,
    normalize: true
  });
}

// Stop capturing
audioinput.stop();
```

## 📖 Usage Examples

### Method 1: Web Audio API Integration (AudioNode)

This method lets the plugin handle data conversion and provides an AudioNode for Web Audio API integration.

#### Capacitor
```typescript
import { AudioInput } from 'cordova-plugin-audioinput';

async function setupWebAudio() {
  // Request permission
  const { granted } = await AudioInput.getMicrophonePermission();
  if (!granted) return;

  // Start with Web Audio integration
  // Note: For Capacitor, use the Cordova API via window.audioinput for streamToWebAudio
  const audioinput = (window as any).audioinput;

  audioinput.start({
    streamToWebAudio: true
  });

  // Connect to speakers to hear the captured audio
  audioinput.connect(audioinput.getAudioContext().destination);
}
```

#### Cordova
```javascript
function startCapture() {
  audioinput.start({
    streamToWebAudio: true
  });

  // Connect to device speakers
  audioinput.connect(audioinput.getAudioContext().destination);
}

// Check and request permission
audioinput.checkMicrophonePermission(function(hasPermission) {
  if (hasPermission) {
    startCapture();
  } else {
    audioinput.getMicrophonePermission(function(granted) {
      if (granted) startCapture();
    });
  }
});
```

### Method 2: Event-Based Raw Audio Data

Use this method for direct access to raw audio data for custom processing.

#### Capacitor
```typescript
import { AudioInput } from 'cordova-plugin-audioinput';

async function setupRawAudio() {
  // Request permission
  await AudioInput.getMicrophonePermission();

  // Listen for audio data
  AudioInput.addListener('audioData', (event) => {
    // event.data is an array of audio samples
    processAudioData(event.data);
  });

  // Listen for errors
  AudioInput.addListener('audioError', (event) => {
    console.error('Audio error:', event.message);
  });

  // Start capturing
  await AudioInput.start({
    sampleRate: 44100,
    bufferSize: 8192,
    channels: 1,
    format: 'PCM_16BIT',
    normalize: true
  });
}

function processAudioData(samples: number[]) {
  // Your audio processing logic here
  console.log(`Processing ${samples.length} samples`);
}

// Stop when done
async function stopRecording() {
  await AudioInput.stop();
  await AudioInput.removeAllListeners();
}
```

#### Cordova
```javascript
function onAudioInput(event) {
  // event.data is an array of audio samples
  console.log('Audio data received: ' + event.data.length + ' samples');
  processAudioData(event.data);
}

function onAudioInputError(error) {
  console.error('Audio error:', JSON.stringify(error));
}

// Listen to events
window.addEventListener('audioinput', onAudioInput, false);
window.addEventListener('audioinputerror', onAudioInputError, false);

// Start capturing
audioinput.start({
  sampleRate: 44100,
  bufferSize: 8192,
  channels: 1,
  format: audioinput.FORMAT.PCM_16BIT,
  normalize: true
});

// Stop capturing
audioinput.stop();
```

### Method 3: Recording to Files

Save audio directly to WAV files on the device.

#### Capacitor
```typescript
import { AudioInput } from 'cordova-plugin-audioinput';
import { Filesystem, Directory } from '@capacitor/filesystem';

async function recordToFile() {
  await AudioInput.getMicrophonePermission();

  // Listen for recording finished event
  AudioInput.addListener('audioInputFinished', async (event) => {
    console.log('Recording saved to:', event.fileUrl);
    // File is now available at event.fileUrl
  });

  // Start recording to file
  const fileUrl = 'file:///path/to/recording.wav';
  await AudioInput.start({
    sampleRate: 16000,
    bufferSize: 8192,
    channels: 1,
    format: 'PCM_16BIT',
    fileUrl: fileUrl
  });

  // When ready to stop
  const result = await AudioInput.stop();
  console.log('Stopped, file at:', result.fileUrl);
}
```

#### Cordova (with cordova-plugin-file)
```javascript
// Get access to the file system
window.requestFileSystem(window.TEMPORARY, 5*1024*1024, function(fs) {
  fileSystem = fs;

  // Initialize with file system directory
  var captureCfg = {
    sampleRate: 16000,
    bufferSize: 8192,
    channels: 1,
    format: audioinput.FORMAT.PCM_16BIT,
    fileUrl: cordova.file.cacheDirectory
  };

  audioinput.initialize(captureCfg, function() {
    console.log('Initialized with file system access');
  });
});

// Start recording to file
var captureCfg = {
  fileUrl: cordova.file.cacheDirectory + 'recording.wav'
};

audioinput.start(captureCfg);

// Stop and get file URL
audioinput.stop(function(fileUrl) {
  console.log('Recording saved to:', fileUrl);

  // Read the file
  window.resolveLocalFileSystemURL(fileUrl, function(fileEntry) {
    fileEntry.file(function(file) {
      var reader = new FileReader();
      reader.onloadend = function() {
        var blob = new Blob([new Uint8Array(this.result)], { type: 'audio/wav' });
        // Use the blob...
      };
      reader.readAsArrayBuffer(file);
    });
  });
});
```

## ⚙️ Configuration Options

### AudioInputOptions (Capacitor) / captureCfg (Cordova)

```typescript
interface AudioInputOptions {
  // Sample rate in Hz
  sampleRate?: number;          // Default: 44100
                                // Available: 8000, 11025, 16000, 22050, 32000, 44100, 48000

  // Buffer size in bytes (should be power of 2, <= 16384)
  bufferSize?: number;          // Default: 16384

  // Number of channels
  channels?: number;            // Default: 1 (Mono)
                                // 1 = Mono, 2 = Stereo

  // Audio format
  format?: 'PCM_16BIT' | 'PCM_8BIT';  // Default: 'PCM_16BIT'

  // Normalize audio data to -1.0 to 1.0 range
  normalize?: boolean;          // Default: true

  // Normalization factor (audio divided by this value)
  normalizationFactor?: number; // Default: 32767.0

  // Audio source type
  audioSourceType?: number;     // Default: 0 (DEFAULT)
                                // 0 = DEFAULT
                                // 1 = MIC (Android only)
                                // 5 = CAMCORDER
                                // 6 = VOICE_RECOGNITION (Android only)
                                // 7 = VOICE_COMMUNICATION
                                // 9 = UNPROCESSED

  // File URL for saving (when set, no data events are fired)
  fileUrl?: string;             // Example: 'file:///path/to/file.wav'

  // Cordova-specific options (use via window.audioinput)
  streamToWebAudio?: boolean;   // Let plugin handle Web Audio conversion
  audioContext?: AudioContext;  // Provide your own AudioContext
  concatenateMaxChunks?: number;// Chunks to merge (lower = lower latency)
}
```

### Helper Constants

#### Capacitor (TypeScript)
```typescript
import { SampleRate, AudioSourceType } from 'cordova-plugin-audioinput';

const config = {
  sampleRate: SampleRate.CD_AUDIO_44100Hz,
  audioSourceType: AudioSourceType.VOICE_COMMUNICATION
};
```

#### Cordova (JavaScript)
```javascript
var config = {
  sampleRate: audioinput.SAMPLERATE.CD_AUDIO_44100Hz,
  channels: audioinput.CHANNELS.MONO,
  format: audioinput.FORMAT.PCM_16BIT,
  audioSourceType: audioinput.AUDIOSOURCE_TYPE.VOICE_COMMUNICATION
};
```

Available constants:
- `SAMPLERATE`: `TELEPHONE_8000Hz`, `CD_QUARTER_11025Hz`, `VOIP_16000Hz`, `CD_HALF_22050Hz`, `MINI_DV_32000Hz`, `CD_AUDIO_44100Hz`, `DVD_AUDIO_48000Hz`
- `CHANNELS`: `MONO`, `STEREO`
- `FORMAT`: `PCM_16BIT`, `PCM_8BIT`
- `AUDIOSOURCE_TYPE`: `DEFAULT`, `MIC`, `CAMCORDER`, `VOICE_RECOGNITION`, `VOICE_COMMUNICATION`, `UNPROCESSED`

## 📚 API Reference

### Capacitor API

```typescript
import { AudioInput } from 'cordova-plugin-audioinput';

// Initialize (optional - can also configure in start())
await AudioInput.initialize(options: AudioInputOptions): Promise<void>

// Check microphone permission (doesn't prompt user)
await AudioInput.checkMicrophonePermission(): Promise<{ granted: boolean }>

// Request microphone permission (prompts user if needed)
await AudioInput.getMicrophonePermission(): Promise<{ granted: boolean }>

// Start audio capture
await AudioInput.start(options: AudioInputOptions): Promise<void>

// Stop audio capture
await AudioInput.stop(): Promise<{ fileUrl?: string }>

// Add event listener
AudioInput.addListener(
  'audioData' | 'audioError' | 'audioInputFinished',
  callback
): PluginListenerHandle

// Remove all listeners
await AudioInput.removeAllListeners(): Promise<void>
```

### Cordova API

```javascript
// Initialize (optional)
audioinput.initialize(captureCfg, onComplete)

// Check microphone permission
audioinput.checkMicrophonePermission(callback)

// Request microphone permission
audioinput.getMicrophonePermission(callback)

// Start capturing
audioinput.start(captureCfg)

// Stop capturing
audioinput.stop(onStopped)

// Check if capturing
audioinput.isCapturing(): boolean

// Get current configuration
audioinput.getCfg(): object

// Web Audio API methods (when streamToWebAudio: true)
audioinput.connect(audioNode)
audioinput.disconnect()
audioinput.getAudioContext(): AudioContext
```

### Events

#### Capacitor Events
- `audioData` - Fired when audio data is available (if not recording to file)
- `audioError` - Fired when an error occurs
- `audioInputFinished` - Fired when file recording completes

#### Cordova Events
- `audioinput` - Fired when audio data is available (if not recording to file)
- `audioinputerror` - Fired when an error occurs
- `audioinputfinished` - Fired when file recording completes (has `file` property)

## 🔧 Advanced Usage

### Custom Audio Processing Chain (Web Audio API)

```javascript
// Cordova example - works in both platforms via window.audioinput
var audioContext = new AudioContext();

audioinput.start({
  streamToWebAudio: true,
  audioContext: audioContext
});

// Create a custom processing chain
var analyser = audioContext.createAnalyser();
var filter = audioContext.createBiquadFilter();

filter.type = 'lowpass';
filter.frequency.value = 1000;

// Connect: mic → filter → analyser → speakers
audioinput.connect(filter);
filter.connect(analyser);
analyser.connect(audioContext.destination);

// Visualize audio
function visualize() {
  var dataArray = new Uint8Array(analyser.frequencyBinCount);
  analyser.getByteFrequencyData(dataArray);
  // Draw visualization...
  requestAnimationFrame(visualize);
}
visualize();
```

### Voice Activity Detection

```typescript
// Capacitor example
import { AudioInput } from 'cordova-plugin-audioinput';

let silenceThreshold = 0.01;  // Adjust based on your needs
let isSpeaking = false;

AudioInput.addListener('audioData', (event) => {
  // Calculate RMS (Root Mean Square) for volume detection
  const samples = event.data;
  let sum = 0;
  for (let i = 0; i < samples.length; i++) {
    sum += samples[i] * samples[i];
  }
  const rms = Math.sqrt(sum / samples.length);

  // Detect speech
  if (rms > silenceThreshold && !isSpeaking) {
    console.log('Speech started');
    isSpeaking = true;
  } else if (rms <= silenceThreshold && isSpeaking) {
    console.log('Speech stopped');
    isSpeaking = false;
  }
});

await AudioInput.start({ normalize: true });
```

## 💾 Demo Apps

- [app-audioinput-demo](https://github.com/edimuj/app-audioinput-demo) - Cordova demo app
- The `demo` folder contains usage examples:
  - `webaudio-demo` - Web Audio API AudioNode integration
  - `events-demo` - Event-based raw audio data handling
  - `wav-demo` - WAV encoding and playback
  - `file-demo` - File saving (requires cordova-plugin-file)

## 🆕 What's New in v2.0

- ✅ **Capacitor support** - Full Capacitor plugin implementation
- ✅ **TypeScript** - Complete type definitions for Capacitor
- ✅ **Modern languages** - Kotlin (Android) and Swift (iOS) wrappers
- ✅ **Promise-based API** - Async/await support in Capacitor
- ✅ **Performance optimizations** - Buffer pooling, efficient Base64 encoding
- ✅ **Bug fixes** - Multiple critical bugs fixed
- ✅ **100% backward compatible** - Existing Cordova apps work unchanged

See [CHANGELOG.md](CHANGELOG.md) for full details.

## 🐛 Known Issues & Limitations

- Not all audio configuration combinations are supported by all devices
- Default settings work on most devices
- Bluetooth microphone support varies by device
- File recording always produces WAV format

## 🤝 Contributing

Contributions are welcome! Please ensure changes don't break backward compatibility.

1. Fork the project
2. Create your feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## 💖 Support This Project

If you find this plugin useful, please:
- ⭐ Star the project on GitHub
- 📢 Share it with others
- 💰 [Donate via PayPal](https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=R9WGMBB2BMS34)

Your support helps keep this project maintained and improved!

## 📜 License

[MIT License](https://github.com/edimuj/cordova-plugin-audioinput/blob/master/LICENSE)

## 👏 Credits

- **Created by**: Edin Mujkanovic
- **Contributors**: [All contributors](https://github.com/edimuj/cordova-plugin-audioinput/graphs/contributors)
- **v2.0 Capacitor support**: Enhanced with modern architecture and optimizations

## 🔗 Links

- [GitHub Repository](https://github.com/edimuj/cordova-plugin-audioinput)
- [npm Package](https://www.npmjs.com/package/cordova-plugin-audioinput)
- [Issue Tracker](https://github.com/edimuj/cordova-plugin-audioinput/issues)
- [Changelog](CHANGELOG.md)
