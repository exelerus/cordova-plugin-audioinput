package com.exelerus.audioinput

import android.Manifest
import android.os.Handler
import android.os.Looper
import android.os.Message
import com.getcapacitor.JSObject
import com.getcapacitor.Plugin
import com.getcapacitor.PluginCall
import com.getcapacitor.PluginMethod
import com.getcapacitor.annotation.CapacitorPlugin
import com.getcapacitor.annotation.Permission
import com.exelerus.cordova.audioinputcapture.AudioInputReceiver
import org.json.JSONArray
import java.lang.ref.WeakReference
import java.net.URI

/**
 * Capacitor plugin for audio input capture
 * Thin wrapper around the existing AudioInputReceiver class
 */
@CapacitorPlugin(
    name = "AudioInput",
    permissions = [
        Permission(
            strings = [Manifest.permission.RECORD_AUDIO],
            alias = "microphone"
        )
    ]
)
class AudioInputPlugin : Plugin() {

    private var receiver: AudioInputReceiver? = null
    private val handler = AudioInputHandler(this)

    private var sampleRate: Int = 44100
    private var bufferSize: Int = 16384
    private var channels: Int = 1
    private var format: String = "PCM_16BIT"
    private var audioSource: Int = 0
    private var fileUrl: URI? = null

    @PluginMethod
    fun initialize(call: PluginCall) {
        try {
            sampleRate = call.getInt("sampleRate", 44100)!!
            bufferSize = call.getInt("bufferSize", 16384)!!
            channels = call.getInt("channels", 1)!!
            format = call.getString("format", "PCM_16BIT")!!
            audioSource = call.getInt("audioSourceType", 0)!!

            val fileUrlString = call.getString("fileUrl")
            fileUrl = if (fileUrlString != null) URI(fileUrlString) else null

            call.resolve()
        } catch (e: Exception) {
            call.reject("Initialization failed: ${e.message}", e)
        }
    }

    @PluginMethod
    fun checkMicrophonePermission(call: PluginCall) {
        val granted = getPermissionState("microphone") == com.getcapacitor.PermissionState.GRANTED
        val ret = JSObject()
        ret.put("granted", granted)
        call.resolve(ret)
    }

    @PluginMethod
    fun getMicrophonePermission(call: PluginCall) {
        if (getPermissionState("microphone") == com.getcapacitor.PermissionState.GRANTED) {
            val ret = JSObject()
            ret.put("granted", true)
            call.resolve(ret)
        } else {
            requestPermissionForAlias("microphone", call, "microphonePermissionCallback")
        }
    }

    @PluginMethod
    fun start(call: PluginCall) {
        try {
            // Update options if provided
            sampleRate = call.getInt("sampleRate", sampleRate)!!
            bufferSize = call.getInt("bufferSize", bufferSize)!!
            channels = call.getInt("channels", channels)!!
            format = call.getString("format", format)!!
            audioSource = call.getInt("audioSourceType", audioSource)!!

            val fileUrlString = call.getString("fileUrl")
            fileUrl = if (fileUrlString != null) URI(fileUrlString) else null

            // Check permission
            if (getPermissionState("microphone") != com.getcapacitor.PermissionState.GRANTED) {
                call.reject("Microphone permission not granted")
                return
            }

            // Stop existing receiver if any
            receiver?.interrupt()

            // Create and start new receiver (uses existing Java class!)
            receiver = AudioInputReceiver(
                sampleRate,
                bufferSize,
                channels,
                format,
                audioSource,
                fileUrl
            )
            receiver?.setHandler(handler)
            receiver?.start()

            call.resolve()
        } catch (e: Exception) {
            call.reject("Failed to start audio capture: ${e.message}", e)
        }
    }

    @PluginMethod
    fun stop(call: PluginCall) {
        try {
            receiver?.interrupt()
            receiver = null

            val ret = JSObject()
            call.resolve(ret)
        } catch (e: Exception) {
            call.reject("Failed to stop audio capture: ${e.message}", e)
        }
    }

    override fun handleOnDestroy() {
        receiver?.interrupt()
        receiver = null
        super.handleOnDestroy()
    }

    /**
     * Permission callback
     */
    @com.getcapacitor.annotation.PermissionCallback
    private fun microphonePermissionCallback(call: PluginCall) {
        val granted = getPermissionState("microphone") == com.getcapacitor.PermissionState.GRANTED
        val ret = JSObject()
        ret.put("granted", granted)
        call.resolve(ret)
    }

    /**
     * Handler for receiving audio data from AudioInputReceiver
     */
    private class AudioInputHandler(plugin: AudioInputPlugin) : Handler(Looper.getMainLooper()) {
        private val pluginRef = WeakReference(plugin)

        override fun handleMessage(msg: Message) {
            val plugin = pluginRef.get() ?: return

            try {
                val data = msg.data.getString("data")
                val error = msg.data.getString("error")
                val file = msg.data.getString("file")

                when {
                    data != null -> {
                        // Audio data received - convert to JSON array format
                        val ret = JSObject()
                        ret.put("data", JSONArray(data))
                        plugin.notifyListeners("audioData", ret)
                    }
                    error != null -> {
                        // Error occurred
                        val ret = JSObject()
                        ret.put("message", error)
                        plugin.notifyListeners("audioError", ret)
                    }
                    file != null -> {
                        // File recording finished
                        val ret = JSObject()
                        ret.put("fileUrl", file)
                        plugin.notifyListeners("audioInputFinished", ret)
                    }
                }
            } catch (e: Exception) {
                val ret = JSObject()
                ret.put("message", "Handler error: ${e.message}")
                plugin.notifyListeners("audioError", ret)
            }
        }
    }
}
