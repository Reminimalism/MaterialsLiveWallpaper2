package com.reminimalism.materialslivewallpaper2

import android.graphics.Bitmap
import java.nio.ByteBuffer
import java.nio.ByteOrder
import androidx.core.graphics.createBitmap

open class GLFramebufferManager
{
    companion object
    {
        private var screenWidth: Int = 16
        private var screenHeight: Int = 16

        private var currentFramebufferHandle: Int = 0
        private var currentFramebufferFormat: GLFormat? = null
        private var currentWidth: Int = 16
        private var currentHeight: Int = 16

        private fun setViewport(width: Int, height: Int)
        {
            currentWidth = width
            currentHeight = height
            GL.glViewport(0, 0, width, height)
        }

        fun updateScreenSize(width: Int, height: Int)
        {
            screenWidth = width
            screenHeight = height
            if (currentFramebufferHandle == 0)
                setViewport(width, height)
        }

        /**
         * Must only be called by texture classes themselves.
         */
        fun bindFramebuffer(framebufferHandle: Int, format: GLFormat?, width: Int, height: Int)
        {
            GL.glBindFramebuffer(GL.GL_FRAMEBUFFER, framebufferHandle)
            setViewport(width, height)
            currentFramebufferHandle = framebufferHandle
            currentFramebufferFormat = format
        }

        /**
         * Unbinds any framebuffer that's bound.
         */
        fun unbindFramebuffer()
        {
            GL.glBindFramebuffer(GL.GL_FRAMEBUFFER, 0)
            setViewport(screenWidth, screenHeight)
            currentFramebufferHandle = 0
            currentFramebufferFormat = null
        }

        fun getCurrentFramebufferHandle() = currentFramebufferHandle
        fun getCurrentFramebufferFormat() = currentFramebufferFormat

        fun bindFramebuffer(texture: GLTexture): Boolean
        {
            return texture.bindFramebuffer()
        }

        fun unbindFramebuffer(texture: GLTexture): Boolean
        {
            return texture.unbindFramebuffer()
        }

        /**
         * Only returns a non-null if there's a framebuffer bound
         * that has not been loaded from a bitmap itself
         * and it has a valid format.
         */
        fun exportBitmapFromFramebuffer(): Bitmap?
        {
            if (currentFramebufferHandle == 0)
                return null

            currentFramebufferFormat?.let {
                val bitmapConfig = it.toBitmapConfig() ?: return null

                val bufferSize = currentWidth * currentHeight * it.toByteSize()
                var buffer = ByteBuffer.allocateDirect(bufferSize).apply {
                    order(ByteOrder.nativeOrder())
                }

                GL.glReadPixels(
                    0, 0,
                    currentWidth, currentHeight,
                    it.toGLFormat(), it.toGLType(),
                    buffer
                )

                val error = GL.glGetError()
                if (error != GL.GL_NO_ERROR)
                {
                    return null
                }

                if (bitmapConfig == Bitmap.Config.ARGB_8888)
                {
                    buffer.rewind()
                    val bufferArray = ByteArray(bufferSize)
                    buffer.get(bufferArray)

                    // RGBA to ARGB for bitmap
                    for (i in bufferArray.indices step 4)
                    {
                        val a = bufferArray[i + 3]
                        bufferArray[i + 3] = bufferArray[i + 2] // b
                        bufferArray[i + 2] = bufferArray[i + 1] // g
                        bufferArray[i + 1] = bufferArray[i] // r
                        bufferArray[i] = a // a
                    }

                    buffer = ByteBuffer.wrap(bufferArray)
                }

                val bitmap = createBitmap(currentWidth, currentHeight, bitmapConfig)
                buffer.rewind()
                bitmap.copyPixelsFromBuffer(buffer)
                return bitmap
            }

            return null
        }
    }
}