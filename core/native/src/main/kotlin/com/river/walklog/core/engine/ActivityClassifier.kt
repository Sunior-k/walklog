package com.river.walklog.core.engine

import android.content.Context
import com.river.walklog.core.common.ActivityStateProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.common.FileUtil
import java.io.Closeable

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
) : ActivityStateProvider, Closeable {

    private val interpreter: Interpreter? = runCatching {
        val model = FileUtil.loadMappedFile(context, MODEL_ASSET)
        Interpreter(model, Interpreter.Options().apply { numThreads = 2 })
    }.getOrNull()

    private val _isStationary = MutableStateFlow(false)
    override val isStationary: StateFlow<Boolean> = _isStationary.asStateFlow()

    /**
     * Emits the classified [ActivityState] for each complete 1-second sensor window.
     * Also updates [isStationary] so that [OfflineFirstStepRepository] can gate step recording.
     * Cancels automatically when the collector's underlying sensor subscription is cancelled.
     */
    fun observeActivityState(): Flow<ActivityState> =
        sensorCollector.observeWindows()
            .map { window -> classify(window) }
            .onEach { state -> _isStationary.value = state == ActivityState.STATIONARY }
            .flowOn(Dispatchers.Default)

    /**
     * Classifies the current activity from a sliding sensor window.
     *
     * @param sensorWindow FloatArray of size [WINDOW_SIZE × CHANNELS].
     *                     Layout: [t0_ax, t0_ay, t0_az, t0_gx, t0_gy, t0_gz, t1_ax, …]
     */
    fun classify(sensorWindow: FloatArray): ActivityState {
        val interp = interpreter ?: return ActivityState.UNKNOWN

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
            ?: ActivityState.UNKNOWN.ordinal
        return ActivityState.entries[maxIdx]
    }

    override fun close() = interpreter?.close() ?: Unit

    companion object {
        private const val MODEL_ASSET   = "activity_classifier.tflite"
        const val WINDOW_SIZE           = 50  // 50 Hz × 1 s = 50 time steps
        const val CHANNELS              = 6   // accel x/y/z + gyro x/y/z
        private val OUTPUT_CLASSES      = ActivityState.entries.size
    }
}
