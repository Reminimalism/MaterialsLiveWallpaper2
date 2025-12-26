package com.reminimalism.materialslivewallpaper2

import android.content.Context
import android.content.SharedPreferences

class PreferencesComponent(context: Context) : Component()
{
    private val preferences: SharedPreferences = getAppPreferences(context)

    private val keyToComponentListeners = mutableMapOf<String, MutableMap<Component, () -> Unit>>()
    private val componentToKeys = mutableMapOf<Component, MutableSet<String>>()

    private val changedKeys = mutableSetOf<String>()

    private val preferencesChangeListener = SharedPreferences.OnSharedPreferenceChangeListener { pref, key ->
        if (pref == preferences && key != null)
        {
            changedKeys.add(key)
        }
    }

    private fun invokeChange(key: String)
    {
        keyToComponentListeners[key]?.forEach({ componentListeners ->
            componentListeners.value.invoke()
        })
    }

    /**
     * Can only add one listener per key per component
     * @param key The preference key to listen to changes to.
     */
    fun registerListener(component: Component, key: String, listener: () -> Unit)
    {
        val components = keyToComponentListeners.getOrPut(key) { mutableMapOf() }
        val keys = componentToKeys.getOrPut(component) { mutableSetOf() }
        keys.add(key)
        components[component] = listener
    }

    /**
     * Removes all the listeners registered by the component.
     */
    fun unregisterListeners(component: Component)
    {
        componentToKeys[component]?.forEach{ key ->
            keyToComponentListeners[key]?.remove(component)
        }
        componentToKeys.remove(component)
    }

    override fun initialize()
    {
        preferences.registerOnSharedPreferenceChangeListener(preferencesChangeListener)
    }

    override fun start()
    {
    }

    override fun update()
    {
        for (changedKey in changedKeys)
            invokeChange(changedKey)
    }

    override fun stop()
    {
        preferences.unregisterOnSharedPreferenceChangeListener(preferencesChangeListener)
    }
}