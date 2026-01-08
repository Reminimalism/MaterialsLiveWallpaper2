package com.reminimalism.materialslivewallpaper2

class MeshComponent : Component()
{
    private var meshes: MutableList<GLMesh> = mutableListOf()

    // This works alright: 65535.toShort()
    // Can read indices (if read) as long,
    // and fail if they exceed uint,
    // else, convert to short if in unsigned short range,
    // else, convert to int if in unsigned int range.

    fun getMeshes(): List<GLMesh> = meshes

    override fun initialize()
    {
    }

    override fun start()
    {
    }

    override fun update()
    {
    }

    override fun stop()
    {
    }
}