package com.reminimalism.materialslivewallpaper2

class GLProgram(vertexShader: String, fragmentShader: String)
{
    private val position = "position"
    private val normal = "normal"
    private val tangent = "tangent"
    private val uv = "uv"

    private val transform = "transform"
    private val rotation = "rotation"
    private val fovTangent = "fov_tangent"

    private var programHandle: Int = 0

    private var positionLocation: Int = 0
    private var normalLocation: Int = 0
    private var tangentLocation: Int = 0
    private var uvLocation: Int = 0

    private var transformLocation: Int = 0
    private var rotationLocation: Int = 0
    private var fovTangentLocation: Int = 0

    init
    {
        setupProgramAndLocations(vertexShader, fragmentShader)
    }

    fun use()
    {
        GL.glUseProgram(programHandle)
    }

    fun setParams(transform: FloatArray, rotation: FloatArray, fovTangent: Float)
    {
        if (programHandle == 0)
            return
        GL.glUniformMatrix4fv(transformLocation, 1, false, transform, 0)
        GL.glUniformMatrix3fv(rotationLocation, 1, true, rotation, 0)
        GL.glUniform1f(fovTangentLocation, fovTangent)
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
        GL.glDeleteProgram(programHandle)
        programHandle = 0
    }

    fun isDestroyed(): Boolean = programHandle == 0

    fun getPositionLocation(): Int
    {
        return positionLocation
    }

    fun getNormalLocation(): Int
    {
        return normalLocation
    }

    fun getTangentLocation(): Int
    {
        return tangentLocation
    }

    fun getUVLocation(): Int
    {
        return uvLocation
    }

    private fun setupProgramAndLocations(vertexShader: String, fragmentShader: String)
    {
        setupProgram(vertexShader, fragmentShader)

        if (programHandle == 0)
        {
            positionLocation = 0
            normalLocation = 0
            tangentLocation = 0
            uvLocation = 0

            transformLocation = 0
            rotationLocation = 0
            fovTangentLocation = 0
        }
        else
        {
            positionLocation = GL.glGetAttribLocation(programHandle, position)
            normalLocation = GL.glGetAttribLocation(programHandle, normal)
            tangentLocation = GL.glGetAttribLocation(programHandle, tangent)
            uvLocation = GL.glGetAttribLocation(programHandle, uv)

            transformLocation = GL.glGetUniformLocation(programHandle, transform)
            rotationLocation = GL.glGetUniformLocation(programHandle, rotation)
            fovTangentLocation = GL.glGetUniformLocation(programHandle, fovTangent)
        }
    }

    private fun setupProgram(vertexShader: String, fragmentShader: String)
    {
        val vertHandle = compileShader(vertexShader, GL.GL_VERTEX_SHADER)
        val fragHandle = compileShader(fragmentShader, GL.GL_FRAGMENT_SHADER)

        if (vertHandle == 0 || fragHandle == 0)
        {
            programHandle = 0
            if (vertHandle != 0)
                GL.glDeleteShader(vertHandle)
            if (fragHandle != 0)
                GL.glDeleteShader(fragHandle)
            return
        }

        programHandle = GL.glCreateProgram()
        if (programHandle == 0)
        {
            GL.glDeleteShader(vertHandle)
            GL.glDeleteShader(fragHandle)
            Logger.logInternalError("Could not create program")
            return
        }

        GL.glAttachShader(programHandle, vertHandle)
        GL.glAttachShader(programHandle, fragHandle)

        GL.glBindAttribLocation(programHandle, 0, position)
        GL.glBindAttribLocation(programHandle, 1, normal)
        GL.glBindAttribLocation(programHandle, 2, tangent)
        GL.glBindAttribLocation(programHandle, 3, uv)

        GL.glLinkProgram(programHandle)

        val linkStatus = IntArray(1)
        GL.glGetProgramiv(programHandle, GL.GL_LINK_STATUS, linkStatus, 0)

        if (linkStatus[0] == 0)
        {
            GL.glDeleteProgram(programHandle)
            programHandle = 0
            Logger.logInternalError("Error linking program.")
        }

        GL.glDeleteShader(vertHandle)
        GL.glDeleteShader(fragHandle)
    }

    private fun compileShader(source: String, shaderType: Int): Int
    {
        val shaderHandle = GL.glCreateShader(shaderType)
        if (shaderHandle == 0)
        {
            val error = GL.glGetError()
            Logger.logInternalError("Could not create shader: $error")
            return 0
        }
        GL.glShaderSource(shaderHandle, source)
        GL.glCompileShader(shaderHandle)
        val compileStatus = IntArray(1)
        GL.glGetShaderiv(shaderHandle, GL.GL_COMPILE_STATUS, compileStatus, 0)
        if (compileStatus[0] == 0)
        {
            val error = GL.glGetShaderInfoLog(shaderHandle)
            Logger.logUserError("Shader compile error: $error")
            GL.glDeleteShader(shaderHandle)
            return 0
        }
        return shaderHandle
    }
}