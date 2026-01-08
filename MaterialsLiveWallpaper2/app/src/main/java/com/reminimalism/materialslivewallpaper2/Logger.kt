package com.reminimalism.materialslivewallpaper2

import android.util.Log

object Logger
{
    fun logInternalError(text: String)
    {
        Log.d("InternalError", text)
    }

    fun logUserError(text: String)
    {
        Log.d("UserError", text)
    }
}