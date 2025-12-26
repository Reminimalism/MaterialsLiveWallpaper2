package com.reminimalism.materialslivewallpaper2

import android.app.ActivityManager
import android.content.Context
import android.opengl.GLSurfaceView
import android.service.wallpaper.WallpaperService
import android.view.SurfaceHolder

class MaterialsWallpaperService : WallpaperService()
{
    override fun onCreateEngine(): Engine
    {
        return MaterialsWallpaperEngine()
    }

    inner class MaterialsWallpaperEngine : Engine()
    {
        inner class WallpaperGLSurfaceView(context: Context) : GLSurfaceView(context)
        {
            override fun getHolder(): SurfaceHolder
            {
                return surfaceHolder
            }

            fun onDestroy()
            {
                onDetachedFromWindow()
            }
        }

        private var glSurfaceView: WallpaperGLSurfaceView? = null
        private var renderer: MaterialsWallpaperRenderer? = null

        override fun onCreate(surfaceHolder: SurfaceHolder?)
        {
            super.onCreate(surfaceHolder)

            glSurfaceView = WallpaperGLSurfaceView(this@MaterialsWallpaperService)

            val activityManager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            val configurationInfo = activityManager.deviceConfigurationInfo
            val supportsES20 = configurationInfo.reqGlEsVersion >= 0x20000

            if (!supportsES20)
                return

            glSurfaceView?.setEGLContextClientVersion(2)
            glSurfaceView?.preserveEGLContextOnPause = true
            renderer = MaterialsWallpaperRenderer(this@MaterialsWallpaperService)
            glSurfaceView?.setRenderer(renderer)
        }

        override fun onVisibilityChanged(visible: Boolean)
        {
            super.onVisibilityChanged(visible)

            if (visible)
            {
                glSurfaceView?.onResume()
                renderer?.onResume()
            }
            else
            {
                glSurfaceView?.onPause()
                renderer?.onPause()
            }
        }

        override fun onDestroy()
        {
            super.onDestroy()

            renderer?.onDestroy()
            glSurfaceView?.onDestroy()
        }
    }
}