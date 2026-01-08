package com.reminimalism.materialslivewallpaper2

import android.opengl.GLES20
import java.nio.Buffer
import java.nio.ByteBuffer
import java.nio.ByteOrder

class GLMesh(
    positions: FloatArray = getDefaultPositions(),
    normals: FloatArray = getDefaultNormals(),
    tangents: FloatArray = getDefaultTangents(),
    uvs: FloatArray = getDefaultUVs(),
    indices: ShortArray = getDefaultIndices())
{
    private val bufferObjects = IntArray(3)
    private val positionsIndex = 0
    private val normalsIndex = 1
    private val tangentsIndex = 2
    private val uvsIndex = 3
    private val indicesIndex = 4

    private var indicesSize = indices.size

    private var isCleanedUp = false

    companion object
    {
        fun getDefaultPositions() = floatArrayOf(
            -1f, -1f, 0f,
            +1f, -1f, 0f,
            +1f, +1f, 0f,
            -1f, +1f, 0f
        )

        fun getDefaultNormals() = floatArrayOf(
            0f, 0f, 1f,
            0f, 0f, 1f,
            0f, 0f, 1f,
            0f, 0f, 1f
        )

        fun getDefaultTangents() = floatArrayOf(
            1f, 0f, 0f,
            1f, 0f, 0f,
            1f, 0f, 0f,
            1f, 0f, 0f
        )

        fun getDefaultUVs() = floatArrayOf(
            0f, 0f,
            1f, 0f,
            1f, 1f,
            0f, 1f
        )

        fun getDefaultIndices() = shortArrayOf(
            0, 1, 2,
            0, 2, 3
        )
    }

    init
    {
        createBuffers()

        if (indices.isEmpty() || !setupBuffers(positions, normals, tangents, uvs, indices))
        {
            // Fall back to a default simple quad
            setupBuffers(
                getDefaultPositions(),
                getDefaultNormals(),
                getDefaultTangents(),
                getDefaultUVs(),
                getDefaultIndices()
            )
        }
    }

    fun changeBuffers(
            positions: FloatArray,
            normals: FloatArray,
            tangents: FloatArray,
            uvs: FloatArray,
            indices: ShortArray): Boolean
    {
        if (isCleanedUp)
        {
            Logger.logInternalError("Trying to change buffers on a cleaned up GLMesh.")
            return false
        }
        return setupBuffers(positions, normals, tangents, uvs, indices)
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
        bindFloatVertexBuffer(program.getNormalLocation(), normalsIndex, 3)
        bindFloatVertexBuffer(program.getTangentLocation(), tangentsIndex, 3)
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

    private fun setupBuffers(
            positions: FloatArray,
            normals: FloatArray,
            tangents: FloatArray,
            uvs: FloatArray,
            indices: ShortArray): Boolean
    {
        if (indices.size % 3 != 0)
        {
            Logger.logUserError("Mesh indices aren't a divisible by 3.")
            return false
        }
        for (i in indices)
        {
            val ui = i.toUShort().toInt()
            if (ui >= positions.size || ui >= normals.size || ui >= tangents.size || ui >= uvs.size)
            {
                Logger.logUserError("Mesh indices are out of vertex bounds.")
                return false
            }
        }

        setupFloatVertexBuffer(positionsIndex, positions)
        setupFloatVertexBuffer(normalsIndex, normals)
        setupFloatVertexBuffer(tangentsIndex, tangents)
        setupFloatVertexBuffer(uvsIndex, uvs)

        setupShortIndexBuffer(indices)

        return true
    }

    private fun setupFloatVertexBuffer(bufferObjectIndex: Int, data: FloatArray)
    {
        val buffer = ByteBuffer.allocateDirect(data.size * 4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
            .put(data)
            .position(0)

        setupGLBufferObject(bufferObjectIndex, GLES20.GL_ARRAY_BUFFER, buffer, data.size * 4)

        buffer.clear()
    }

    private fun setupShortIndexBuffer(indices: ShortArray)
    {
        val indexBuffer = ByteBuffer.allocateDirect(indices.size * 2)
            .order(ByteOrder.nativeOrder())
            .asShortBuffer()
            .put(indices)
            .position(0)

        setupGLBufferObject(indicesIndex, GLES20.GL_ELEMENT_ARRAY_BUFFER, indexBuffer, indices.size * 2)

        indexBuffer.clear()

        indicesSize = indices.size
    }

    private fun setupGLBufferObject(bufferObjectIndex: Int, target: Int, buffer: Buffer, size: Int)
    {
        GLES20.glBindBuffer(target, bufferObjects[bufferObjectIndex])
        GLES20.glBufferData(target, size, buffer, GLES20.GL_STATIC_DRAW)
        GLES20.glBindBuffer(target, 0)
    }
}