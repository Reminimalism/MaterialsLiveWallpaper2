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
        SensorsComponent(context),
        MeshComponent(),
        RendererComponent()
    ))

    init
    {
        componentContainer.initialize()
    }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?)
    {
        GLES20.glClearColor(0f, 0f, 0f, 1f)
        GLES20.glEnable(GLES20.GL_BLEND)
        GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE)

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