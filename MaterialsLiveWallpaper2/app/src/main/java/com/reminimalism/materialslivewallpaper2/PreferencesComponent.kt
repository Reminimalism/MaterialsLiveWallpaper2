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
     * Can only set one listener per key per component
     * Multiple listeners for one key by one component isn't supported.
     * @param owner The listener component. This will be used for unregistering as well.
     * @param key The preference key to listen to changes to.
     */
    fun registerListener(owner: Component, key: String, listener: () -> Unit)
    {
        val components = keyToComponentListeners.getOrPut(key) { mutableMapOf() }
        val keys = componentToKeys.getOrPut(owner) { mutableSetOf() }
        keys.add(key)
        components[owner] = listener
    }

    /**
     * Removes all the listeners registered by the component.
     * @param owner The listener component that has registered listeners previously.
     */
    fun unregisterListeners(owner: Component)
    {
        componentToKeys[owner]?.forEach{ key ->
            keyToComponentListeners[key]?.remove(owner)
        }
        componentToKeys.remove(owner)
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