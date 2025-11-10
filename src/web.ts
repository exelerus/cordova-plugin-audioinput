import { WebPlugin } from '@capacitor/core';

import type {
  AudioInputPlugin,
  AudioInputOptions,
  AudioDataEvent,
  AudioErrorEvent,
  AudioFinishedEvent,
} from './definitions';

/**
 * Web implementation of AudioInput plugin
 * Uses Web Audio API for browser-based audio capture
 */
export class AudioInputWeb extends WebPlugin implements AudioInputPlugin {
  private audioContext: AudioContext | null = null;
  private mediaStream: MediaStream | null = null;
  private scriptProcessor: ScriptProcessorNode | null = null;
  private micGainNode: GainNode | null = null;
  private capturing = false;
  private options: AudioInputOptions = {};

  async initialize(options: AudioInputOptions): Promise<void> {
    this.options = { ...this.options, ...options };
    return Promise.resolve();
  }

  async checkMicrophonePermission(): Promise<{ granted: boolean }> {
    // In web, we can only check after requesting
    // Return true if we already have a stream
    return { granted: this.mediaStream !== null };
  }

  async getMicrophonePermission(): Promise<{ granted: boolean }> {
    try {
      const stream = await navigator.mediaDevices.getUserMedia({ audio: true });

      // We got permission, but we'll close this stream for now
      // It will be reopened in start()
      stream.getTracks().forEach(track => track.stop());

      return { granted: true };
    } catch (error) {
      console.error('Microphone permission denied:', error);
      return { granted: false };
    }
  }

  async start(options: AudioInputOptions): Promise<void> {
    if (this.capturing) {
      throw new Error('Already capturing audio');
    }

    this.options = { ...this.options, ...options };

    try {
      // Request microphone access
      this.mediaStream = await navigator.mediaDevices.getUserMedia({
        audio: {
          echoCancellation: false,
          autoGainControl: false,
          noiseSuppression: false,
        },
      });

      // Create audio context
      const AudioContext = window.AudioContext || (window as any).webkitAudioContext;
      this.audioContext = new AudioContext();

      // Create nodes
      const source = this.audioContext.createMediaStreamSource(this.mediaStream);
      this.micGainNode = this.audioContext.createGain();

      // Create script processor for audio data
      const bufferSize = this.options.bufferSize || 16384;
      this.scriptProcessor = this.audioContext.createScriptProcessor(bufferSize, 1, 1);

      this.scriptProcessor.onaudioprocess = (event) => {
        if (!this.capturing) return;

        const inputData = event.inputBuffer.getChannelData(0);
        const samples = this.processSamples(inputData);

        this.notifyListeners('audioData', { data: samples } as AudioDataEvent);
      };

      // Connect the audio graph
      source.connect(this.micGainNode);
      this.micGainNode.connect(this.scriptProcessor);
      this.scriptProcessor.connect(this.audioContext.destination);

      this.capturing = true;
    } catch (error: any) {
      this.notifyListeners('audioError', {
        message: error.message || 'Failed to start audio capture',
      } as AudioErrorEvent);
      throw error;
    }
  }

  async stop(): Promise<{ fileUrl?: string }> {
    this.capturing = false;

    if (this.scriptProcessor) {
      this.scriptProcessor.disconnect();
      this.scriptProcessor = null;
    }

    if (this.micGainNode) {
      this.micGainNode.disconnect();
      this.micGainNode = null;
    }

    if (this.mediaStream) {
      this.mediaStream.getTracks().forEach(track => track.stop());
      this.mediaStream = null;
    }

    if (this.audioContext) {
      await this.audioContext.close();
      this.audioContext = null;
    }

    return {};
  }

  /**
   * Process audio samples according to options
   */
  private processSamples(inputData: Float32Array): number[] {
    const normalize = this.options.normalize !== false;
    const normalizationFactor = this.options.normalizationFactor || 32767.0;

    if (normalize) {
      // Return as Float32Array (already normalized -1 to 1)
      return Array.from(inputData);
    } else {
      // Convert to Int16Array
      const output = new Array(inputData.length);
      for (let i = 0; i < inputData.length; i++) {
        const sample = Math.max(-1, Math.min(1, inputData[i]));
        output[i] = Math.floor(sample * normalizationFactor);
      }
      return output;
    }
  }
}
