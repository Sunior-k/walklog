package com.river.walklog.core.engine

import android.content.Context
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import org.tensorflow.lite.Interpreter
import java.io.Closeable
import java.io.FileInputStream
import java.nio.channels.FileChannel

/**
 * On-device Human Activity Recognition classifier backed by a LiteRT model.
 *
 * Input  : [WINDOW_SIZE × CHANNELS] float window of raw sensor data.
 *          Channels: accel_x, accel_y, accel_z, gyro_x, gyro_y, gyro_z (m/s² and rad/s).
 * Output : [ActivityState] — the class with the highest softmax probability.
 *
 * Model  : HAR model trained on UCI HAR Dataset (Anguita et al., 2013).
 *          Expected asset path: assets/activity_classifier.tflite
 *          If the model file is absent the classifier returns UNKNOWN for all inputs.
 *
 * References:
 *   - LiteRT Android guide  : https://ai.google.dev/edge/litert/inference#load-model
 *   - UCI HAR Dataset       : https://archive.ics.uci.edu/dataset/240/human+activity+recognition+using+smartphones
 */
class ActivityClassifier(
    context: Context,
    private val sensorCollector: ActivitySensorCollector,
    private val defaultDispatcher: CoroutineDispatcher,
) : Closeable {

    private val interpreter: Interpreter? = runCatching {
        val model = context.assets.openFd(MODEL_ASSET).use { descriptor ->
            FileInputStream(descriptor.fileDescriptor).channel.use { channel ->
                channel.map(
                    FileChannel.MapMode.READ_ONLY,
                    descriptor.startOffset,
                    descriptor.declaredLength,
                )
            }
        }
        Interpreter(model, Interpreter.Options().apply { numThreads = 2 })
    }.getOrNull()

    val isModelAvailable: Boolean get() = interpreter != null

    private val _isStationary = MutableStateFlow(false)
    val isStationary: StateFlow<Boolean> = _isStationary.asStateFlow()

    /**
     * Emits the classified [EngineActivityState] for each complete 1-second sensor window.
     * Also updates [isStationary].
     * Cancels automatically when the collector's underlying sensor subscription is cancelled.
     */
    fun observeActivityState(): Flow<EngineActivityState> =
        sensorCollector.observeWindows()
            .map { window -> classify(window) }
            .onEach { state -> _isStationary.value = state == EngineActivityState.STATIONARY }
            .flowOn(defaultDispatcher)

    /**
     * Classifies the current activity from a sliding sensor window.
     *
     * @param sensorWindow FloatArray of size [WINDOW_SIZE × CHANNELS].
     *                     Layout: [t0_ax, t0_ay, t0_az, t0_gx, t0_gy, t0_gz, t1_ax, …]
     */
    fun classify(sensorWindow: FloatArray): EngineActivityState {
        val interp = interpreter ?: return EngineActivityState.UNKNOWN

        require(sensorWindow.size == WINDOW_SIZE * CHANNELS) {
            "sensorWindow must be ${WINDOW_SIZE * CHANNELS} values " +
                "(${WINDOW_SIZE} steps × $CHANNELS channels), got ${sensorWindow.size}"
        }

        val input = Array(1) { Array(WINDOW_SIZE) { FloatArray(CHANNELS) } }
        for (t in 0 until WINDOW_SIZE) {
            for (c in 0 until CHANNELS) {
                input[0][t][c] = sensorWindow[t * CHANNELS + c]
            }
        }

        val output = Array(1) { FloatArray(OUTPUT_CLASSES) }
        interp.run(input, output)

        val maxIdx = output[0].indices.maxByOrNull { output[0][it] }
            ?: EngineActivityState.UNKNOWN.ordinal
        return EngineActivityState.entries[maxIdx]
    }

    override fun close() = interpreter?.close() ?: Unit

    companion object {
        private const val MODEL_ASSET   = "activity_classifier.tflite"
        const val WINDOW_SIZE           = 50  // 50 Hz × 1 s = 50 time steps
        const val CHANNELS              = 6   // accel x/y/z + gyro x/y/z
        private val OUTPUT_CLASSES      = EngineActivityState.entries.size
    }
}
