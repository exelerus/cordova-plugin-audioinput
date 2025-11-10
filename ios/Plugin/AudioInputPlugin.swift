import Foundation
import Capacitor
import AVFoundation

/**
 * Capacitor plugin for audio input capture
 * Thin wrapper around the existing AudioReceiver Objective-C class
 */
@objc(AudioInputPlugin)
public class AudioInputPlugin: CAPPlugin, AudioReceiverProtocol {

    private var audioReceiver: AudioReceiver?
    private var fileUrl: String?

    private var sampleRate: Int32 = 44100
    private var bufferSize: Int32 = 16384
    private var channels: Int16 = 1
    private var format: String = "PCM_16BIT"
    private var audioSourceType: Int32 = 0

    @objc func initialize(_ call: CAPPluginCall) {
        sampleRate = Int32(call.getInt("sampleRate") ?? 44100)
        bufferSize = Int32(call.getInt("bufferSize") ?? 16384)
        channels = Int16(call.getInt("channels") ?? 1)
        format = call.getString("format") ?? "PCM_16BIT"
        audioSourceType = Int32(call.getInt("audioSourceType") ?? 0)
        fileUrl = call.getString("fileUrl")

        call.resolve()
    }

    @objc func checkMicrophonePermission(_ call: CAPPluginCall) {
        let status = AVAudioSession.sharedInstance().recordPermission
        let granted = status == .granted
        call.resolve(["granted": granted])
    }

    @objc func getMicrophonePermission(_ call: CAPPluginCall) {
        let status = AVAudioSession.sharedInstance().recordPermission

        if status == .granted {
            call.resolve(["granted": true])
            return
        }

        AVAudioSession.sharedInstance().requestRecordPermission { granted in
            call.resolve(["granted": granted])
        }
    }

    @objc func start(_ call: CAPPluginCall) {
        // Update options if provided
        if let sr = call.getInt("sampleRate") {
            sampleRate = Int32(sr)
        }
        if let bs = call.getInt("bufferSize") {
            bufferSize = Int32(bs)
        }
        if let ch = call.getInt("channels") {
            channels = Int16(ch)
        }
        if let fmt = call.getString("format") {
            format = fmt
        }
        if let ast = call.getInt("audioSourceType") {
            audioSourceType = Int32(ast)
        }
        fileUrl = call.getString("fileUrl")

        // Check permission
        let status = AVAudioSession.sharedInstance().recordPermission
        if status != .granted {
            call.reject("Microphone permission not granted")
            return
        }

        // Stop existing receiver if any
        if let receiver = audioReceiver {
            receiver.stop()
            audioReceiver = nil
        }

        // Create and start new receiver (uses existing ObjC class!)
        audioReceiver = AudioReceiver(
            sampleRate,
            bufferSize: bufferSize,
            noOfChannels: channels,
            audioFormat: format,
            sourceType: audioSourceType,
            fileUrl: fileUrl
        )

        audioReceiver?.delegate = self
        audioReceiver?.start()

        call.resolve()
    }

    @objc func stop(_ call: CAPPluginCall) {
        audioReceiver?.stop()
        audioReceiver = nil
        call.resolve()
    }

    // MARK: - AudioReceiverProtocol delegate methods

    public func didReceiveAudioData(_ data: UnsafeMutablePointer<Int16>!, dataLength length: Int32) {
        DispatchQueue.main.async { [weak self] in
            guard let self = self else { return }

            // Convert C array to Swift array
            var samples: [Int16] = []
            for i in 0..<Int(length) {
                samples.append(data[i])
            }

            // Notify listeners
            self.notifyListeners("audioData", data: [
                "data": samples
            ])
        }
    }

    public func didEncounterError(_ msg: String!) {
        DispatchQueue.main.async { [weak self] in
            self?.notifyListeners("audioError", data: [
                "message": msg ?? "Unknown error"
            ])
        }
    }

    public func didFinish(_ url: String!) {
        DispatchQueue.main.async { [weak self] in
            self?.notifyListeners("audioInputFinished", data: [
                "fileUrl": url ?? ""
            ])
        }
    }

    deinit {
        audioReceiver?.stop()
        audioReceiver = nil
    }
}
