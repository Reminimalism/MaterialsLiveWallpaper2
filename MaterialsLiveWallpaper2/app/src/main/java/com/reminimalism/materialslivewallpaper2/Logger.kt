package com.reminimalism.materialslivewallpaper2

import android.util.Log

object Logger
{
    fun logInternalError(text: String)
    {
        Log.e("InternalError", text)
    }

    fun logUserError(text: String)
    {
        Log.e("UserError", text)
    }
}