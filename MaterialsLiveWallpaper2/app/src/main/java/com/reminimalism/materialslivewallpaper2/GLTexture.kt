package com.reminimalism.materialslivewallpaper2

import android.graphics.Bitmap
import android.opengl.GLUtils

class GLTexture(
        private val width: Int, private val height: Int,
        enableDepthBuffer: Boolean, enableFramebuffer: Boolean,
        format: GLFormat = GLFormat.RGBA8_UNORM, bitmap: Bitmap? = null
    )
{
    private var textureHandle: Int = 0
    private var depthBufferHandle: Int = 0
    private var framebufferHandle: Int = 0
    private var format: GLFormat? = null

    constructor(width: Int, height: Int, enableDepthBuffer: Boolean, enableFramebuffer: Boolean, format: GLFormat)
            : this(width, height, enableDepthBuffer, enableFramebuffer, format, null)

    constructor(width: Int, height: Int, enableDepthBuffer: Boolean, enableFramebuffer: Boolean, bitmap: Bitmap)
            : this(width, height, enableDepthBuffer, enableFramebuffer, GLFormat.RGBA8_UNORM, bitmap)

    init
    {
        setup(enableDepthBuffer, enableFramebuffer, format, bitmap)
    }

    fun getWidth(): Int = width
    fun getHeight(): Int = height

    fun setFilters(minFilter: GLFilter, magFilter: GLFilter)
    {
        if (textureHandle == 0)
            return
        GL.glBindTexture(GL.GL_TEXTURE_2D, textureHandle)
        GL.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER, minFilter.toGLValue())
        GL.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER, magFilter.toGLValue())
        GL.glBindTexture(GL.GL_TEXTURE20, 0)
    }

    fun setWrapMode(horizontal: GLWrap, vertical: GLWrap)
    {
        if (textureHandle == 0)
            return
        GL.glBindTexture(GL.GL_TEXTURE_2D, textureHandle)
        GL.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_S, horizontal.toGLValue())
        GL.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_T, vertical.toGLValue())
        GL.glBindTexture(GL.GL_TEXTURE20, 0)
    }

    fun bindFramebuffer(): Boolean
    {
        if (framebufferHandle != 0)
        {
            GLFramebufferManager.bindFramebuffer(framebufferHandle, format, width, height)
            return true
        }
        return false
    }

    /**
     * Unbinds this framebuffer if bound.
     * To unbind any framebuffer that's bound,
     * call GLFramebufferManager.unbindFramebuffer().
     */
    fun unbindFramebuffer(): Boolean
    {
        if (framebufferHandle != 0
                && framebufferHandle == GLFramebufferManager.getCurrentFramebufferHandle())
        {
            GLFramebufferManager.unbindFramebuffer()
            return true
        }
        return false
    }

    fun upload(bitmap: Bitmap)
    {
        if (textureHandle == 0)
            return
        GL.glBindTexture(GL.GL_TEXTURE_2D, textureHandle)
        GLUtils.texSubImage2D(GL.GL_TEXTURE_2D, 0, 0, 0, bitmap)
        GL.glBindTexture(GL.GL_TEXTURE_2D, 0)
    }

    fun destroy()
    {
        val handleRef = intArrayOf(0)

        if (framebufferHandle != 0)
        {
            unbindFramebuffer()
            handleRef[0] = framebufferHandle
            GL.glDeleteFramebuffers(1, handleRef, 0)
            framebufferHandle = 0
        }

        if (depthBufferHandle != 0)
        {
            handleRef[0] = depthBufferHandle
            GL.glDeleteRenderbuffers(1, handleRef, 0)
            depthBufferHandle = 0
        }

        if (textureHandle != 0)
        {
            handleRef[0] = textureHandle
            GL.glDeleteTextures(1, handleRef, 0)
            textureHandle = 0
        }
    }

    private fun setup(
            enableDepthBuffer: Boolean, enableFramebuffer: Boolean,
            format: GLFormat, bitmap: Bitmap?
        )
    {
        val textureHandleRef = intArrayOf(0)
        GL.glGenTextures(1, textureHandleRef, 0)
        textureHandle = textureHandleRef[0]

        if (textureHandle == 0)
            return

        GL.glBindTexture(GL.GL_TEXTURE_2D, textureHandle)
        if (bitmap != null)
        {
            GLUtils.texImage2D(GL.GL_TEXTURE_2D, 0, bitmap, 0)
        }
        else
        {
            this.format = format
            GL.glTexImage2D(
                GL.GL_TEXTURE_2D, 0, format.toGLInternalFormat(),
                width, height, 0,
                format.toGLFormat(), format.toGLType(), null
            )
        }
        GL.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER, GL.GL_LINEAR)
        GL.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER, GL.GL_LINEAR)
        GL.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_S, GL.GL_CLAMP_TO_EDGE)
        GL.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_T, GL.GL_CLAMP_TO_EDGE)
        GL.glBindTexture(GL.GL_TEXTURE20, 0)

        if (enableDepthBuffer)
        {
            val depthHandleRef = intArrayOf(0)
            GL.glGenRenderbuffers(1, depthHandleRef, 0)
            depthBufferHandle = depthHandleRef[0]

            if (depthBufferHandle != 0)
            {
                GL.glBindRenderbuffer(GL.GL_RENDERBUFFER, depthBufferHandle)
                GL.glRenderbufferStorage(
                    GL.GL_RENDERBUFFER,
                    GL.GL_DEPTH_COMPONENT16,
                    width, height
                )
                GL.glBindRenderbuffer(GL.GL_RENDERBUFFER, 0)
            }
        }

        if (enableFramebuffer)
        {
            val framebufferHandleRef = intArrayOf(0)
            GL.glGenFramebuffers(1, framebufferHandleRef, 0)
            framebufferHandle = framebufferHandleRef[0]

            if (framebufferHandle != 0)
            {
                GL.glBindFramebuffer(GL.GL_FRAMEBUFFER, framebufferHandle)

                GL.glFramebufferTexture2D(
                    GL.GL_FRAMEBUFFER,
                    GL.GL_COLOR_ATTACHMENT0,
                    GL.GL_TEXTURE_2D,
                    textureHandle,
                    0
                )

                if (depthBufferHandle != 0)
                {
                    GL.glFramebufferRenderbuffer(
                        GL.GL_FRAMEBUFFER,
                        GL.GL_DEPTH_ATTACHMENT,
                        GL.GL_RENDERBUFFER,
                        depthBufferHandle
                    )
                }

                val status = GL.glCheckFramebufferStatus(GL.GL_FRAMEBUFFER)
                GL.glBindFramebuffer(GL.GL_FRAMEBUFFER, 0)

                if (status != GL.GL_FRAMEBUFFER_COMPLETE)
                {
                    GL.glDeleteFramebuffers(1, framebufferHandleRef, 0)
                    framebufferHandle = 0
                }
            }
        }
    }
}