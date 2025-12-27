package com.reminimalism.materialslivewallpaper2

import android.content.Context
import android.content.SharedPreferences

class PreferencesComponent(context: Context) : Component()
{
    private val preferences: SharedPreferences = getAppPreferences(context)

    private val keyToComponentListeners = mutableMapOf<String, MutableMap<Component, () -> Unit>>()
    private val componentToKeys = mutableMapOf<Component, MutableSet<String>>()

    private val changedKeys = mutableSetOf<String>()
    private val currentCallbacks = mutableSetOf<() -> Unit>()

    private val preferencesChangeListener = SharedPreferences.OnSharedPreferenceChangeListener { pref, key ->
        if (pref == preferences && key != null)
        {
            changedKeys.add(key)
        }
    }

    private fun addToCurrentCallbacks(key: String)
    {
        keyToComponentListeners[key]?.forEach { componentListener ->
            currentCallbacks.add { componentListener.value }
        }
    }

    /**
     * Can only set one listener per key per component
     * Multiple listeners for one key by one component isn't supported.
     * @param owner The listener component. This will be used for unregistering as well.
     * @param key The preference key to listen to changes to.
     */
    fun registerListener(owner: Component, key: String, listener: () -> Unit)
    {
        val keys = componentToKeys.getOrPut(owner) { mutableSetOf() }
        val components = keyToComponentListeners.getOrPut(key) { mutableMapOf() }
        keys.add(key)
        components[owner] = listener
    }

    /**
     * Sets a shared listener for multiple keys' changes.
     * This is useful to get one callback for any number of changes
     * done to any number of the keys.
     * Can only set one listener per key per component
     * Multiple listeners for one key by one component isn't supported.
     * @param owner The listener component. This will be used for unregistering as well.
     * @param keys The preference keys to listen to changes to.
     */
    fun registerListener(owner: Component, keys: Collection<String>, listener: () -> Unit)
    {
        val componentKeys = componentToKeys.getOrPut(owner) { mutableSetOf() }
        for (key in keys)
        {
            val components = keyToComponentListeners.getOrPut(key) { mutableMapOf() }
            componentKeys.add(key)
            components[owner] = listener
        }
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
        if (changedKeys.isEmpty())
            return

        currentCallbacks.clear()
        for (changedKey in changedKeys)
            addToCurrentCallbacks(changedKey)
        changedKeys.clear()

        for (callback in currentCallbacks)
            callback.invoke()
        currentCallbacks.clear()
    }

    override fun stop()
    {
        preferences.unregisterOnSharedPreferenceChangeListener(preferencesChangeListener)
    }
}