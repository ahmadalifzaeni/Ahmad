package com.example.data

import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.media.AudioManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.BatteryManager
import android.os.Build
import android.provider.Settings
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class DeviceStatusRepository(private val context: Context) {

    private val audioManager: AudioManager? =
        context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
    private val batteryManager: BatteryManager? =
        context.getSystemService(Context.BATTERY_SERVICE) as? BatteryManager
    private val connectivityManager: ConnectivityManager? =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
    private val cameraManager: CameraManager? =
        context.getSystemService(Context.CAMERA_SERVICE) as? CameraManager
    private val sensorManager: SensorManager? =
        context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager

    private var cachedTorchOn: Boolean = false

    fun readStatus(isDefaultLauncher: Boolean, currentBrightness: Float): DeviceStatus {
        val batteryPct = runCatching {
            batteryManager?.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
                ?.takeIf { it in 0..100 } ?: 85
        }.getOrDefault(85)

        val isCharging = runCatching {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                batteryManager?.isCharging ?: false
            } else {
                false
            }
        }.getOrDefault(false)

        var wifi = false
        var cellular = false
        runCatching {
            val activeNet = connectivityManager?.activeNetwork
            val caps = connectivityManager?.getNetworkCapabilities(activeNet)
            wifi = caps?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true
            cellular = caps?.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) == true
        }

        val volumeFraction = runCatching {
            val maxVol = audioManager?.getStreamMaxVolume(AudioManager.STREAM_MUSIC)?.coerceAtLeast(1) ?: 15
            val curVol = audioManager?.getStreamVolume(AudioManager.STREAM_MUSIC) ?: 8
            (curVol.toFloat() / maxVol.toFloat()).coerceIn(0f, 1f)
        }.getOrDefault(0.6f)

        var availRamGb = 6.4f
        var totalRamGb = 12.0f
        runCatching {
            val am = context.getSystemService(Context.ACTIVITY_SERVICE) as? android.app.ActivityManager
            if (am != null) {
                val memInfo = android.app.ActivityManager.MemoryInfo()
                am.getMemoryInfo(memInfo)
                val gb = 1024f * 1024f * 1024f
                availRamGb = (memInfo.availMem / gb).coerceAtLeast(0.1f)
                totalRamGb = (memInfo.totalMem / gb).coerceAtLeast(1.0f)
            }
        }

        val isAirplaneOn = runCatching {
            Settings.Global.getInt(
                context.contentResolver,
                Settings.Global.AIRPLANE_MODE_ON,
                0
            ) != 0
        }.getOrDefault(false)

        return DeviceStatus(
            batteryPercent = batteryPct,
            isCharging = isCharging,
            isWifiConnected = wifi,
            isCellularConnected = cellular,
            isAirplaneModeOn = isAirplaneOn,
            mediaVolumeFraction = volumeFraction,
            screenBrightnessFraction = currentBrightness,
            isTorchOn = cachedTorchOn,
            isDefaultLauncher = isDefaultLauncher,
            availableRamGb = availRamGb,
            totalRamGb = totalRamGb
        )
    }

    fun setMediaVolume(fraction: Float) {
        runCatching {
            val maxVol = audioManager?.getStreamMaxVolume(AudioManager.STREAM_MUSIC) ?: return
            val target = (fraction.coerceIn(0f, 1f) * maxVol).toInt()
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, target, 0)
        }
    }

    fun toggleFlashlight(): Boolean {
        return runCatching {
            val mgr = cameraManager ?: return false
            val cameraId = mgr.cameraIdList.firstOrNull { id ->
                val chars = mgr.getCameraCharacteristics(id)
                chars.get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true &&
                    chars.get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_BACK
            } ?: mgr.cameraIdList.firstOrNull() ?: return false

            val nextState = !cachedTorchOn
            mgr.setTorchMode(cameraId, nextState)
            cachedTorchOn = nextState
            nextState
        }.getOrDefault(false)
    }

    fun openInternetPanel() {
        runCatching {
            val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                Intent(Settings.Panel.ACTION_INTERNET_CONNECTIVITY)
            } else {
                Intent(Settings.ACTION_WIFI_SETTINGS)
            }.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        }
    }

    fun openAirplaneModeSettings() {
        runCatching {
            context.startActivity(
                Intent(Settings.ACTION_AIRPLANE_MODE_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
        }.onFailure {
            openSystemSettings()
        }
    }

    fun openNotificationSettings() {
        runCatching {
            val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                Intent(Settings.ACTION_ALL_APPS_NOTIFICATION_SETTINGS)
            } else {
                Intent(Settings.ACTION_SETTINGS)
            }.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        }.onFailure {
            openSystemSettings()
        }
    }

    fun openBluetoothSettings() {
        runCatching {
            context.startActivity(
                Intent(Settings.ACTION_BLUETOOTH_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
        }
    }

    fun openDisplaySettings() {
        runCatching {
            context.startActivity(
                Intent(Settings.ACTION_DISPLAY_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
        }
    }

    fun openSystemSettings() {
        runCatching {
            context.startActivity(
                Intent(Settings.ACTION_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
        }
    }

    /**
     * Emits normalized device tilt (x in [-1f, 1f], y in [-1f, 1f]) from Gravity/Accelerometer
     * sensor with low-pass filtering for silky 3D parallax wallpaper & icon lighting.
     */
    fun observeDeviceTilt(): Flow<Pair<Float, Float>> = callbackFlow {
        val mgr = sensorManager
        val sensor = mgr?.getDefaultSensor(Sensor.TYPE_GRAVITY)
            ?: mgr?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        if (mgr == null || sensor == null) {
            trySend(0f to 0f)
            awaitClose {}
            return@callbackFlow
        }

        var smoothX = 0f
        var smoothY = 0f
        val alpha = 0.12f

        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                // Invert X so tilting right shifts background gently left like depth parallax
                val rawX = (-event.values[0] / 5.5f).coerceIn(-1f, 1f)
                val rawY = ((event.values[1] - 4.5f) / 5.5f).coerceIn(-1f, 1f)
                smoothX += alpha * (rawX - smoothX)
                smoothY += alpha * (rawY - smoothY)
                trySend(smoothX to smoothY)
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
        }

        mgr.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_GAME)
        awaitClose {
            mgr.unregisterListener(listener)
        }
    }
}
