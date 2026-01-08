package com.reminimalism.materialslivewallpaper2

class RendererComponent : Component()
{
    private val program = GLProgram(
        GLProgramConstants.vertexShader,
        GLProgramConstants.fragmentShader
    )

    private var meshComponent: MeshComponent? = null

    override fun initialize()
    {
        meshComponent = getComponent()
    }

    override fun start()
    {
    }

    override fun update()
    {
        meshComponent?.let {
            for (mesh in it.getMeshes())
                program.draw(mesh)
        }
    }

    override fun stop()
    {
    }
}