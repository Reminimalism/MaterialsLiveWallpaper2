package com.reminimalism.materialslivewallpaper2

abstract class Component
{
    private var container: ComponentContainer? = null

    fun getContainer(): ComponentContainer?
    {
        return container
    }

    fun setContainer(container: ComponentContainer)
    {
        this.container = container
    }

    protected inline fun <reified T : Component> getComponent(): T?
    {
        return getContainer()?.getComponent<T>()
    }

    protected inline fun <reified T : Component> getComponents(): List<T>?
    {
        return getContainer()?.getComponents<T>()
    }

    abstract fun initialize()
    abstract fun start()
    abstract fun update()
    open fun pause() {}
    open fun resume() {}
    abstract fun stop()
}