package com.reminimalism.materialslivewallpaper2

class ComponentContainer(private val components: List<Component>)
{
    val typeToComponent: Map<Class<out Component>, Component> = components.associateBy { it::class.java }
    val typeToComponents: Map<Class<out Component>, List<Component>> = components.groupBy { it::class.java }

    inline fun <reified T : Component> getComponent(): T?
    {
        return typeToComponent[T::class.java] as? T
    }

    inline fun <reified T : Component> getComponents(): List<T>?
    {
        return typeToComponents[T::class.java]?.map { it as T }
    }

    fun getAllComponents(): List<Component>
    {
        return components
    }

    init
    {
        for (component in components)
            component.setContainer(this)
    }

    fun initialize()
    {
        for (component in components)
            component.initialize()
    }

    fun start()
    {
        for (component in components)
            component.start()
    }

    fun update()
    {
        for (component in components)
            component.update()
    }

    fun pause()
    {
        for (component in components)
            component.pause()
    }

    fun resume()
    {
        for (component in components)
            component.resume()
    }

    fun stop()
    {
        for (component in components)
            component.stop()
    }
}