package com.reminimalism.materialslivewallpaper2

import android.graphics.Bitmap
import android.opengl.GLUtils

class GLTextureCube(
        private val size: Int, private var mipLevels: Int,
        enableDepthBuffer: Boolean, enableFramebuffer: Boolean,
        private val format: GLFormat = GLFormat.RGBA8_UNORM
    )
{
    private var textureHandle: Int = 0
    private var depthBufferHandle: Int = 0
    private var framebufferHandle: Int = 0

    init
    {
        setup(enableDepthBuffer, enableFramebuffer)
    }

    fun getSize(): Int = size
    fun getMipLevels(): Int = mipLevels

    fun getWidth(): Int = size
    fun getHeight(): Int = size

    fun setFilters(minFilter: GLFilter, magFilter: GLFilter)
    {
        if (textureHandle == 0)
            return
        GL.glBindTexture(GL.GL_TEXTURE_CUBE_MAP, textureHandle)
        GL.glTexParameteri(GL.GL_TEXTURE_CUBE_MAP, GL.GL_TEXTURE_MIN_FILTER, minFilter.toGLValue())
        GL.glTexParameteri(GL.GL_TEXTURE_CUBE_MAP, GL.GL_TEXTURE_MAG_FILTER, magFilter.toGLValue())
        GL.glBindTexture(GL.GL_TEXTURE_CUBE_MAP, 0)
    }

    fun setWrapMode(horizontal: GLWrap, vertical: GLWrap)
    {
        if (textureHandle == 0)
            return
        GL.glBindTexture(GL.GL_TEXTURE_CUBE_MAP, textureHandle)
        GL.glTexParameteri(GL.GL_TEXTURE_CUBE_MAP, GL.GL_TEXTURE_WRAP_S, horizontal.toGLValue())
        GL.glTexParameteri(GL.GL_TEXTURE_CUBE_MAP, GL.GL_TEXTURE_WRAP_T, vertical.toGLValue())
        GL.glBindTexture(GL.GL_TEXTURE_CUBE_MAP, 0)
    }

    fun bindFramebuffer(face: Int, mipLevel: Int): Boolean
    {
        if (framebufferHandle == 0)
            return false
        if (face < 0 || face >= 6 || mipLevel < 0 || mipLevel >= mipLevels)
            return false

        GLFramebufferManager.bindFramebuffer(framebufferHandle, format, size, size)

        GL.glFramebufferTexture2D(
            GL.GL_FRAMEBUFFER,
            GL.GL_COLOR_ATTACHMENT0,
            GL.GL_TEXTURE_CUBE_MAP_POSITIVE_X + face,
            textureHandle, mipLevel
        )

        if (mipLevel == 0 && depthBufferHandle != 0)
        {
            GL.glFramebufferRenderbuffer(
                GL.GL_FRAMEBUFFER,
                GL.GL_DEPTH_ATTACHMENT,
                GL.GL_RENDERBUFFER,
                depthBufferHandle
            )
        }
        else
        {
            GL.glFramebufferRenderbuffer(
                GL.GL_FRAMEBUFFER,
                GL.GL_DEPTH_ATTACHMENT,
                GL.GL_RENDERBUFFER,
                0
            )
        }

        return true
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

    fun upload(bitmap: Bitmap, face: Int, mipLevel: Int)
    {
        if (textureHandle == 0)
            return
        if (face < 0 || face >= 6 || mipLevel < 0 || mipLevel >= mipLevels)
            return
        GL.glBindTexture(GL.GL_TEXTURE_CUBE_MAP, textureHandle)
        GLUtils.texSubImage2D(
            GL.GL_TEXTURE_CUBE_MAP_POSITIVE_X + face,
            mipLevel, 0, 0, bitmap
        )
        GL.glBindTexture(GL.GL_TEXTURE_CUBE_MAP, 0)
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
            enableDepthBuffer: Boolean, enableFramebuffer: Boolean
        )
    {
        val textureHandleRef = intArrayOf(0)
        GL.glGenTextures(1, textureHandleRef, 0)
        textureHandle = textureHandleRef[0]

        if (textureHandle == 0)
            return

        GL.glBindTexture(GL.GL_TEXTURE_CUBE_MAP, textureHandle)
        var mipLevelSize = size
        for (mipLevel in 0 until mipLevels)
        {
            for (face in 0 until 6)
            {
                GL.glTexImage2D(
                    GL.GL_TEXTURE_CUBE_MAP_POSITIVE_X + face, mipLevel,
                    format.toGLInternalFormat(),
                    mipLevelSize, mipLevelSize, 0,
                    format.toGLFormat(), format.toGLType(),
                    null
                )
            }
            mipLevelSize /= 2
            if (mipLevelSize < 1)
            {
                mipLevels = mipLevel + 1
                break
            }
        }
        GL.glTexParameteri(GL.GL_TEXTURE_CUBE_MAP, GL.GL_TEXTURE_MIN_FILTER, GL.GL_LINEAR)
        GL.glTexParameteri(GL.GL_TEXTURE_CUBE_MAP, GL.GL_TEXTURE_MAG_FILTER, GL.GL_LINEAR)
        GL.glTexParameteri(GL.GL_TEXTURE_CUBE_MAP, GL.GL_TEXTURE_WRAP_S, GL.GL_CLAMP_TO_EDGE)
        GL.glTexParameteri(GL.GL_TEXTURE_CUBE_MAP, GL.GL_TEXTURE_WRAP_T, GL.GL_CLAMP_TO_EDGE)
        GL.glBindTexture(GL.GL_TEXTURE_CUBE_MAP, 0)

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
                    size, size
                )
                GL.glBindRenderbuffer(GL.GL_RENDERBUFFER, 0)
            }
        }

        if (enableFramebuffer)
        {
            val framebufferHandleRef = intArrayOf(0)
            GL.glGenFramebuffers(1, framebufferHandleRef, 0)
            framebufferHandle = framebufferHandleRef[0]
        }
    }
}