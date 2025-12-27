package com.reminimalism.materialslivewallpaper2

import android.opengl.GLES20
import java.nio.Buffer
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.IntBuffer
import java.nio.ShortBuffer

class MeshComponent : Component()
{
    private val defaultTriangles = floatArrayOf(
        -1f, -1f, 0f,
        +1f, -1f, 0f,
        +1f, +1f, 0f,
        -1f, +1f, 0f
    )

    private val defaultIndices16 = shortArrayOf(0, 1, 2, 0, 2, 3)
    private val defaultIndices32 = intArrayOf(0, 1, 2, 0, 2, 3)

    private val defaultTrianglesBuffer: FloatBuffer = ByteBuffer
        .allocateDirect(defaultTriangles.size * 4)
        .order(ByteOrder.nativeOrder())
        .asFloatBuffer()

    private val defaultIndicesBuffer16: ShortBuffer = ByteBuffer
        .allocateDirect(defaultIndices16.size * 2)
        .order(ByteOrder.nativeOrder())
        .asShortBuffer()

    private val defaultIndicesBuffer32: IntBuffer = ByteBuffer
        .allocateDirect(defaultIndices32.size * 4)
        .order(ByteOrder.nativeOrder())
        .asIntBuffer()

    private var triangles: FloatArray? = null
    private var indices16: ShortArray? = null
    private var indices32: IntArray? = null

    private var trianglesBuffer: FloatBuffer? = null
    private var indicesBuffer16: ShortBuffer? = null
    private var indicesBuffer32: IntBuffer? = null

    // This works alright: 65535.toShort()
    // Can read indices (if read) as long,
    // and fail if they exceed uint,
    // else, convert to short if in unsigned short range,
    // else, convert to int if in unsigned int range.

    override fun initialize()
    {
        defaultTrianglesBuffer.put(defaultTriangles).position(0)
        defaultIndicesBuffer16.put(defaultIndices16).position(0)
        defaultIndicesBuffer32.put(defaultIndices32).position(0)
    }

    override fun start()
    {
        TODO("Not yet implemented")
    }

    override fun update()
    {
        TODO("Not yet implemented")
    }

    override fun stop()
    {
        TODO("Not yet implemented")
    }

    fun getVerticesGLType(): Int
    {
        return GLES20.GL_FLOAT
    }

    fun getVerticesBuffer(): FloatBuffer
    {
        if (indicesBuffer16 == null && indicesBuffer32 == null)
            return defaultTrianglesBuffer
        return trianglesBuffer ?: defaultTrianglesBuffer
    }

    fun getIndicesGLType(): Int
    {
        return if (isIndicesBuffer16Bit()) GLES20.GL_UNSIGNED_SHORT else GLES20.GL_UNSIGNED_INT
    }

    fun getIndicesBuffer(): Buffer
    {
        return if (isIndicesBuffer16Bit()) getIndicesBuffer16() else getIndicesBuffer32()
    }

    private fun isIndicesBuffer16Bit(): Boolean
    {
        return trianglesBuffer != null && indicesBuffer32 != null && indicesBuffer16 == null
    }

    private fun getIndicesBuffer16(): ShortBuffer
    {
        if (trianglesBuffer == null)
            return defaultIndicesBuffer16
        return indicesBuffer16 ?: defaultIndicesBuffer16
    }

    private fun getIndicesBuffer32(): IntBuffer
    {
        if (trianglesBuffer == null)
            return defaultIndicesBuffer32
        return indicesBuffer32 ?: defaultIndicesBuffer32
    }
}