package com.reminimalism.materialslivewallpaper2

import android.content.Context
import android.content.SharedPreferences
import android.hardware.SensorManager
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class MaterialsWallpaperRenderer(val context: Context) : GLSurfaceView.Renderer
{
    private val sensorManager: SensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val preferences: SharedPreferences = getAppPreferences(context)

    init
    {
        // TODO
    }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?)
    {
        // TODO
    }

    fun onPause()
    {
        // TODO
    }

    fun onResume()
    {
        // TODO
    }

    fun onDestroy()
    {
        // TODO
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int)
    {
        GLES20.glViewport(0, 0, width, height)
        // TODO
    }

    override fun onDrawFrame(gl: GL10?)
    {
        // TODO
    }
}