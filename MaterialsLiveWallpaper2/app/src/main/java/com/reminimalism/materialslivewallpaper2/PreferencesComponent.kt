package com.reminimalism.materialslivewallpaper2

import android.content.Context
import android.content.SharedPreferences

class PreferencesComponent(context: Context) : Component()
{
    private val preferences: SharedPreferences = getAppPreferences(context)

    private val changedKeys = mutableSetOf<String>()

    // TODO: Implement a mechanism for key dependencies to get informed about the changes
    //       All dependencies of a key must be informed once
    //       It could be a component register mechanism

    private val preferencesChangeListener = SharedPreferences.OnSharedPreferenceChangeListener { pref, key ->
        if (pref == preferences && key != null)
            changedKeys.add(key)
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
    }

    override fun stop()
    {
        preferences.unregisterOnSharedPreferenceChangeListener(preferencesChangeListener);
    }
}