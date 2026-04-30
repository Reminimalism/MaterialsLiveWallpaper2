package com.reminimalism.materialslivewallpaper2

import android.content.Context
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
        GL.glClearColor(0f, 0f, 0f, 1f)
        GL.glEnable(GL.GL_BLEND)
        GL.glBlendFunc(GL.GL_ONE, GL.GL_ONE)

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
        GLFramebufferManager.updateScreenSize(width, height)
        componentContainer.getComponent<RendererComponent>()?.updateSurface(width, height)
    }

    override fun onDrawFrame(gl: GL10?)
    {
        componentContainer.update()
    }
}