package com.reminimalism.materialslivewallpaper2

import android.opengl.GLES20

class RendererComponent : Component()
{
    private var program: GLProgram? = null

    private var meshComponent: MeshComponent? = null
    private var sensorsComponent: SensorsComponent? = null

    private var aspect: Float = 1f

    // TODO: Maybe think about having different transforms or something
    //       in a way to control anchor,
    //       maybe even per vertex anchor (not different transforms)?
    //       Like being centered, or scaled around one of the 4 corners?
    //       Like having a frame on the edges of the device?
    //       Maybe even be able to control uv independently? sliding it inside the mesh?!
    //       Example: vertices wrapped around the screen, with the uv not stretching.
    //                Like having the option to scale uv with aspect in the vertex shader.
    //       Not sure how useful this would be but need to think about it.
    //       Can just have the normal centered transform for now.

    private var transform = floatArrayOf(
        1f, 0f, 0f, 0f,
        0f, 1f, 0f, 0f,
        0f, 0f, 1f, 0f,
        0f, 0f, 0f, 1f
    )

    private var defaultRotation = floatArrayOf(
        1f, 0f, 0f,
        0f, 1f, 0f,
        0f, 0f, 1f
    )

    override fun initialize()
    {
        meshComponent = getComponent()
        sensorsComponent = getComponent()
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
        program?.setParams(
            transform,
            sensorsComponent?.getRotationMatrix() ?: defaultRotation,
            0.25f
        )
        meshComponent?.let {
            for (mesh in it.getMeshes())
                program?.draw(mesh)
        }
    }

    fun updateSurface(width: Int, height: Int)
    {
        aspect = width.toFloat() / height.toFloat()

        if (aspect < 1f && aspect != 0f)
        {
            transform[0] = 1f / aspect
            transform[5] = 1f
        }
        else
        {
            transform[0] = 1f
            transform[5] = aspect
        }
    }

    override fun stop()
    {
        program?.destroy()
    }
}