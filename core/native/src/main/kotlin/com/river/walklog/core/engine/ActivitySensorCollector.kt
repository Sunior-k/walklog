package com.river.walklog.core.engine

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Collects TYPE_ACCELEROMETER + TYPE_GYROSCOPE at SENSOR_DELAY_FASTEST,
 * buffers samples into non-overlapping windows of [ActivityClassifier.WINDOW_SIZE] steps,
 * and emits each complete window as a flat FloatArray for classification.
 *
 * Layout per sample: [ax, ay, az, gx, gy, gz] — same channel order as the LiteRT model.
 * The gyro slot is held from the most recent gyroscope event; accel events drive the
 * window fill rate (one sample per accel reading once gyro has been primed).
 */
@Singleton
class ActivitySensorCollector @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager

    fun observeWindows(): Flow<FloatArray> = callbackFlow {
        val accelSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        val gyroSensor  = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)

        if (accelSensor == null || gyroSensor == null) {
            close(UnsupportedOperationException("Accelerometer or gyroscope not available on this device"))
            return@callbackFlow
        }

        val window      = FloatArray(ActivityClassifier.WINDOW_SIZE * ActivityClassifier.CHANNELS)
        val lastGyro    = FloatArray(3)
        var gyroReady   = false
        var sampleCount = 0

        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                when (event.sensor.type) {
                    Sensor.TYPE_GYROSCOPE -> {
                        lastGyro[0] = event.values[0]
                        lastGyro[1] = event.values[1]
                        lastGyro[2] = event.values[2]
                        gyroReady = true
                    }
                    Sensor.TYPE_ACCELEROMETER -> {
                        if (!gyroReady) return
                        val offset = sampleCount * ActivityClassifier.CHANNELS
                        window[offset]     = event.values[0]
                        window[offset + 1] = event.values[1]
                        window[offset + 2] = event.values[2]
                        window[offset + 3] = lastGyro[0]
                        window[offset + 4] = lastGyro[1]
                        window[offset + 5] = lastGyro[2]
                        sampleCount++
                        if (sampleCount == ActivityClassifier.WINDOW_SIZE) {
                            trySend(window.copyOf())
                            sampleCount = 0
                        }
                    }
                }
            }

            override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) = Unit
        }

        sensorManager.registerListener(listener, accelSensor, SensorManager.SENSOR_DELAY_FASTEST)
        sensorManager.registerListener(listener, gyroSensor,  SensorManager.SENSOR_DELAY_FASTEST)

        awaitClose { sensorManager.unregisterListener(listener) }
    }
}
