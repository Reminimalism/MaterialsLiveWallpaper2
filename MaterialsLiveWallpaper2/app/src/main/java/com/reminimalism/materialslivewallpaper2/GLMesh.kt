package com.reminimalism.materialslivewallpaper2

import android.opengl.GLES20
import java.nio.Buffer
import java.nio.ByteBuffer
import java.nio.ByteOrder

class GLMesh(positions: FloatArray, uvs: FloatArray, indices: ShortArray)
{
    private val bufferObjects = IntArray(3)
    private val positionsIndex = 0
    private val uvsIndex = 1
    private val indicesIndex = 2

    private var indicesSize = indices.size

    private var isCleanedUp = false

    // Note for loading custom models:
    // This works alright: 65535.toShort()
    // Can read indices (if read) as long,
    // and fail if they exceed uint,
    // else, convert to short if in unsigned short range,
    // else, convert to int if in unsigned int range.

    init
    {
        createBuffers()

        if (!setupBuffers(positions, uvs, indices))
        {
            // Fall back to a default simple quad
            setupBuffers(
                floatArrayOf(
                    -1f, -1f, 0f,
                    +1f, -1f, 0f,
                    +1f, +1f, 0f,
                    -1f, +1f, 0f
                ),
                floatArrayOf(
                    0f, 0f,
                    1f, 0f,
                    1f, 1f,
                    0f, 1f
                ),
                shortArrayOf(
                    0, 1, 2,
                    0, 2, 3
                )
            )
        }
    }

    fun changeBuffers(positions: FloatArray, uvs: FloatArray, indices: ShortArray): Boolean
    {
        if (isCleanedUp)
        {
            Logger.logInternalError("Trying to change buffers on a cleaned up GLMesh.")
            return false
        }
        return setupBuffers(positions, uvs, indices)
    }

    fun draw(program: GLProgram)
    {
        if (isCleanedUp)
        {
            Logger.logInternalError("Trying to draw a cleaned up GLMesh.")
            return
        }

        if (program.isDestroyed())
        {
            Logger.logInternalError("Trying to draw GLMesh using a cleaned up program.")
            return
        }

        bindFloatVertexBuffer(program.getPositionLocation(), positionsIndex, 3)
        bindFloatVertexBuffer(program.getUVLocation(), uvsIndex, 2)

        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, bufferObjects[indicesIndex])

        GLES20.glDrawElements(GLES20.GL_TRIANGLES, indicesSize, GLES20.GL_UNSIGNED_SHORT, 0)

        GLES20.glDisableVertexAttribArray(program.getPositionLocation())
        GLES20.glDisableVertexAttribArray(program.getUVLocation())
    }

    fun destroy()
    {
        if (isCleanedUp)
            return
        isCleanedUp = true
        GLES20.glDeleteBuffers(bufferObjects.size, bufferObjects, 0)
    }

    fun isDestroyed(): Boolean = isCleanedUp

    private fun bindFloatVertexBuffer(programAttribLocation: Int, bufferObjectIndex: Int, vertexSize: Int)
    {
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, bufferObjects[bufferObjectIndex])
        GLES20.glVertexAttribPointer(programAttribLocation, vertexSize, GLES20.GL_FLOAT, false, vertexSize * 4, 0)
        GLES20.glEnableVertexAttribArray(programAttribLocation)
    }

    private fun createBuffers()
    {
        GLES20.glGenBuffers(bufferObjects.size, bufferObjects, 0)
    }

    private fun setupBuffers(positions: FloatArray, uvs: FloatArray, indices: ShortArray): Boolean
    {
        for (i in indices)
        {
            val ui = i.toUShort().toInt()
            if (ui >= positions.size || ui >= uvs.size)
            {
                Logger.logUserError("Mesh indices are out of vertex bounds.")
                return false
            }
        }

        val positionsBuffer = ByteBuffer.allocateDirect(positions.size * 4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
            .put(positions)
            .position(0)

        setupGLBufferObject(positionsIndex, GLES20.GL_ARRAY_BUFFER, positionsBuffer, positions.size * 4)

        positionsBuffer.clear()

        val uvBuffer = ByteBuffer.allocateDirect(uvs.size * 4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
            .put(uvs)
            .position(0)

        setupGLBufferObject(uvsIndex, GLES20.GL_ARRAY_BUFFER, uvBuffer, uvs.size * 4)

        uvBuffer.clear()

        val indexBuffer = ByteBuffer.allocateDirect(indices.size * 2)
            .order(ByteOrder.nativeOrder())
            .asShortBuffer()
            .put(indices)
            .position(0)

        setupGLBufferObject(indicesIndex, GLES20.GL_ELEMENT_ARRAY_BUFFER, indexBuffer, indices.size * 2)

        indexBuffer.clear()

        indicesSize = indices.size

        return true
    }

    private fun setupGLBufferObject(bufferObjectIndex: Int, target: Int, buffer: Buffer, size: Int)
    {
        GLES20.glBindBuffer(target, bufferObjects[bufferObjectIndex])
        GLES20.glBufferData(target, size, buffer, GLES20.GL_STATIC_DRAW)
        GLES20.glBindBuffer(target, 0)
    }
}