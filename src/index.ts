import { registerPlugin } from '@capacitor/core';

import type { AudioInputPlugin } from './definitions';

/**
 * Audio Input Plugin
 *
 * Provides real-time audio capture from the device microphone.
 * Supports both Cordova and Capacitor platforms.
 *
 * @example
 * ```typescript
 * import { AudioInput } from 'cordova-plugin-audioinput';
 *
 * // Initialize
 * await AudioInput.initialize({
 *   sampleRate: 44100,
 *   bufferSize: 16384,
 *   channels: 1,
 *   format: 'PCM_16BIT',
 *   normalize: true
 * });
 *
 * // Check permission
 * const { granted } = await AudioInput.checkMicrophonePermission();
 *
 * // Request permission if needed
 * if (!granted) {
 *   await AudioInput.getMicrophonePermission();
 * }
 *
 * // Listen for audio data
 * AudioInput.addListener('audioData', (event) => {
 *   console.log('Audio samples:', event.data);
 * });
 *
 * // Start recording
 * await AudioInput.start({
 *   sampleRate: 44100,
 *   bufferSize: 16384
 * });
 *
 * // Stop recording
 * await AudioInput.stop();
 * ```
 */
const AudioInput = registerPlugin<AudioInputPlugin>('AudioInput', {
  web: () => import('./web').then(m => new m.AudioInputWeb()),
});

export * from './definitions';
export { AudioInput };
