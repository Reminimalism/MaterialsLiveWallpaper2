package com.reminimalism.materialslivewallpaper2

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import com.reminimalism.materialslivewallpaper2.PreferencesComponent.Companion.getBool
import kotlin.math.max

class SensorsComponent(context: Context) : Component()
{
    private val sensorManager: SensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager

    private var rotationVector = floatArrayOf(0f, 0f, 0f, 1f)
    private var rotationChanged = false

    private var preferencesComponent: PreferencesComponent? = null

    private var rotationSmoothingEnabled = true

    private var lastUpdateTime: Long = 0L
    private var nextUpdateTime: Long = 0L
    private var updateTimeDiff: Long = 1L

    private var rotationMatrixA = floatArrayOf(
        1f, 0f, 0f,
        0f, 1f, 0f,
        0f, 0f, 1f
    )
    private var rotationMatrixB = floatArrayOf(
        1f, 0f, 0f,
        0f, 1f, 0f,
        0f, 0f, 1f
    )

    private var rotationMatrix = floatArrayOf(
        1f, 0f, 0f,
        0f, 1f, 0f,
        0f, 0f, 1f
    )

    private val rotationSensorListener = object : SensorEventListener
    {
        override fun onSensorChanged(event: SensorEvent?)
        {
            event?.let()
            {
                if (event.sensor.type == Sensor.TYPE_ROTATION_VECTOR)
                {
                    //System.arraycopy(event.values, 0, rotationVector, 0, 4);
                    event.values.copyInto(rotationVector, 0, 0, 4)
                    rotationChanged = true
                }
            }
        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
    }

    fun getRotationMatrix() = rotationMatrix

    override fun initialize()
    {
        preferencesComponent = getComponent()

        rotationSmoothingEnabled = preferencesComponent.getBool(
            ROTATION_SMOOTHING, ROTATION_SMOOTHING_DEFAULT
        )

        preferencesComponent?.registerListener(this, ROTATION_SMOOTHING) {
            preferencesComponent?.let()
            {
                rotationSmoothingEnabled = preferencesComponent.getBool(
                    ROTATION_SMOOTHING, ROTATION_SMOOTHING_DEFAULT
                )
            }
            resetTime()
            if (rotationSmoothingEnabled)
            {
                rotationMatrix.copyInto(rotationMatrixA)
                rotationMatrix.copyInto(rotationMatrixB)
            }
        }
    }

    override fun start()
    {
        register()
    }

    override fun pause()
    {
        unregister()
    }

    override fun resume()
    {
        register()
        resetTime()
    }

    override fun stop()
    {
        unregister()
        preferencesComponent?.unregisterListeners(this)
    }

    private fun resetTime()
    {
        lastUpdateTime = 0
        nextUpdateTime = 0
    }

    private fun swapMatrices()
    {
        val temp = rotationMatrixA
        rotationMatrixA = rotationMatrixB
        rotationMatrixB = temp
    }

    override fun update()
    {
        if (!rotationSmoothingEnabled)
        {
            SensorManager.getRotationMatrixFromVector(rotationMatrix, rotationVector)
            return
        }

        val time = System.currentTimeMillis()

        if (rotationChanged)
        {
            rotationChanged = false
            swapMatrices() // matrix A is now set to the previous matrix B
            SensorManager.getRotationMatrixFromVector(rotationMatrixB, rotationVector)
            if (lastUpdateTime == 0L)
            {
                rotationMatrixB.copyInto(rotationMatrixA)
                rotationMatrixB.copyInto(rotationMatrix)
                lastUpdateTime = time
                updateTimeDiff = 1
            }
            else
            {
                val last = lastUpdateTime
                lastUpdateTime = time
                updateTimeDiff = max(1, lastUpdateTime - last)
            }
            nextUpdateTime = lastUpdateTime + updateTimeDiff
        }

        if (time > nextUpdateTime)
        {
            rotationMatrixB.copyInto(rotationMatrix)
        }
        else
        {
            val t = (time - lastUpdateTime).toFloat() / updateTimeDiff.toFloat()
            for (i in 0..8)
            {
                rotationMatrix[i] = rotationMatrixA[i] +
                        (rotationMatrixB[i] - rotationMatrixA[i]) * t
            }
        }
    }

    private fun register()
    {
        val rotationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
        sensorManager.registerListener(
            rotationSensorListener, rotationSensor, SensorManager.SENSOR_DELAY_GAME
        )
    }

    private fun unregister()
    {
        sensorManager.unregisterListener(rotationSensorListener)
    }
}