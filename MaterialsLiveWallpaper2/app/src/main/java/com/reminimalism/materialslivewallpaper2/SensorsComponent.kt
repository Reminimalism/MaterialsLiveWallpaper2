package com.reminimalism.materialslivewallpaper2

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager

class SensorsComponent(context: Context) : Component()
{
    private val sensorManager: SensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager

    private var rotationVector = floatArrayOf(0f, 0f, 0f, 1f)

    // TODO: Rotation matrix lazy update with a changed flag on change,
    //       and a getter for the matrix

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
                }
            }
        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
    }

    override fun initialize()
    {
    }

    override fun start()
    {
        register()
    }

    override fun update()
    {
    }

    override fun pause()
    {
        unregister()
    }

    override fun resume()
    {
        register()
    }

    override fun stop()
    {
        unregister()
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