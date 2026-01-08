package com.reminimalism.materialslivewallpaper2

import android.opengl.GLES20

class GLProgram(vertexShader: String, fragmentShader: String)
{
    private val position = "position"
    private val uv = "uv"

    private var programHandle: Int = 0

    private var positionLocation: Int = 0
    private var uvLocation: Int = 0

    init
    {
        setupProgram(vertexShader, fragmentShader)

        if (programHandle == 0)
        {
            positionLocation = 0
            uvLocation = 0
        }
        else
        {
            positionLocation = GLES20.glGetAttribLocation(programHandle, position)
            uvLocation = GLES20.glGetAttribLocation(programHandle, uv)
        }
    }

    fun draw(mesh: GLMesh)
    {
        if (programHandle == 0)
            return
        mesh.draw(this)
    }

    fun destroy()
    {
        if (programHandle == 0)
            return
        GLES20.glDeleteProgram(programHandle)
        programHandle = 0
    }

    fun isDestroyed(): Boolean = programHandle == 0

    fun getPositionLocation(): Int
    {
        return positionLocation
    }

    fun getUVLocation(): Int
    {
        return uvLocation
    }

    private fun setupProgram(vertexShader: String, fragmentShader: String)
    {
        val vertHandle = compileShader(vertexShader, GLES20.GL_VERTEX_SHADER)
        val fragHandle = compileShader(fragmentShader, GLES20.GL_FRAGMENT_SHADER)

        if (vertHandle == 0 || fragHandle == 0)
        {
            programHandle = 0
            if (vertHandle != 0)
                GLES20.glDeleteShader(vertHandle)
            if (fragHandle != 0)
                GLES20.glDeleteShader(fragHandle)
            return
        }

        programHandle = GLES20.glCreateProgram()
        if (programHandle == 0)
        {
            GLES20.glDeleteShader(vertHandle)
            GLES20.glDeleteShader(fragHandle)
            Logger.logInternalError("Could not create program")
            return
        }

        GLES20.glAttachShader(programHandle, vertHandle)
        GLES20.glAttachShader(programHandle, fragHandle)

        GLES20.glBindAttribLocation(programHandle, 0, position)
        GLES20.glBindAttribLocation(programHandle, 1, uv)

        GLES20.glLinkProgram(programHandle)

        val linkStatus = IntArray(1)
        GLES20.glGetProgramiv(programHandle, GLES20.GL_LINK_STATUS, linkStatus, 0)

        if (linkStatus[0] == 0)
        {
            GLES20.glDeleteProgram(programHandle)
            programHandle = 0
            Logger.logInternalError("Error linking program.")
        }

        GLES20.glDeleteShader(vertHandle)
        GLES20.glDeleteShader(fragHandle)
    }

    private fun compileShader(source: String, shaderType: Int): Int
    {
        val shaderHandle = GLES20.glCreateShader(shaderType)
        if (shaderHandle == 0)
        {
            Logger.logInternalError("Could not create shader")
            GLES20.glDeleteShader(shaderHandle)
            return 0
        }
        GLES20.glShaderSource(shaderHandle, source)
        GLES20.glCompileShader(shaderHandle)
        val compileStatus = IntArray(1)
        GLES20.glGetShaderiv(shaderHandle, GLES20.GL_COMPILE_STATUS, compileStatus, 0)
        if (compileStatus[0] == 0)
        {
            Logger.logUserError("Shader compile error: " + GLES20.glGetShaderInfoLog(shaderHandle))
            GLES20.glDeleteShader(shaderHandle)
            return 0
        }
        return shaderHandle
    }
}