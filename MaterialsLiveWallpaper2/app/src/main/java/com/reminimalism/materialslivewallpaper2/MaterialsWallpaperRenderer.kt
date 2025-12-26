package com.reminimalism.materialslivewallpaper2

import android.content.Context
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class MaterialsWallpaperRenderer(context: Context) : GLSurfaceView.Renderer
{
    private val componentContainer = ComponentContainer(listOf(
        PreferencesComponent(context),
        SensorsComponent(context)
    ))

    init
    {
        componentContainer.initialize()
    }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?)
    {
        componentContainer.start()
    }

    fun onPause()
    {
        componentContainer.pause()
    }

    fun onResume()
    {
        componentContainer.resume()
    }

    fun onDestroy()
    {
        componentContainer.stop()
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int)
    {
        GLES20.glViewport(0, 0, width, height)
        // TODO
    }

    override fun onDrawFrame(gl: GL10?)
    {
        componentContainer.update()
    }
}