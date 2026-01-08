package com.reminimalism.materialslivewallpaper2

import android.opengl.GLES20

class RendererComponent : Component()
{
    private var program: GLProgram? = null

    private var meshComponent: MeshComponent? = null

    override fun initialize()
    {
        meshComponent = getComponent()
    }

    override fun start()
    {
        program = GLProgram(
            GLProgramConstants.vertexShader,
            GLProgramConstants.fragmentShader
        )
    }

    override fun update()
    {
        GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT or GLES20.GL_COLOR_BUFFER_BIT)
        program?.use()
        meshComponent?.let {
            for (mesh in it.getMeshes())
                program?.draw(mesh)
        }
    }

    override fun stop()
    {
    }
}