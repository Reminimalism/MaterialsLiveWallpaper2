package com.reminimalism.materialslivewallpaper2

import android.graphics.Bitmap

enum class GLFilter
{
    Nearest,
    Linear,
    NearestMipmapNearest,

    /**
     * Chooses the nearest mip level and does linear filtering on it.
     */
    NearestMipmapLinear,

    /**
     * Linearly interpolates between mip levels but uses nearest for each level.
     */
    LinearMipmapNearest,
    LinearMipmapLinear
}

enum class GLWrap
{
    ClampToEdge,
    Repeat,
    MirroredRepeat
}

enum class GLFormat
{
    R8_UNORM,
    R16_SFLOAT,
    RG8_UNORM,
    RG16_SFLOAT,
    RGBA8_UNORM,
    RGBA16_SFLOAT
}

fun GLFilter.toGLValue(): Int
{
    return when (this)
    {
        GLFilter.Nearest -> GL.GL_NEAREST
        GLFilter.Linear -> GL.GL_LINEAR
        GLFilter.NearestMipmapNearest -> GL.GL_NEAREST_MIPMAP_NEAREST
        GLFilter.NearestMipmapLinear -> GL.GL_NEAREST_MIPMAP_LINEAR
        GLFilter.LinearMipmapNearest -> GL.GL_LINEAR_MIPMAP_NEAREST
        GLFilter.LinearMipmapLinear -> GL.GL_LINEAR_MIPMAP_LINEAR
    }
}

fun GLWrap.toGLValue(): Int
{
    return when (this)
    {
        GLWrap.ClampToEdge -> GL.GL_CLAMP_TO_EDGE
        GLWrap.Repeat -> GL.GL_REPEAT
        GLWrap.MirroredRepeat -> GL.GL_MIRRORED_REPEAT
    }
}

fun GLFormat.toGLInternalFormat(): Int
{
    return when (this)
    {
        GLFormat.R8_UNORM -> GL.GL_R8
        GLFormat.R16_SFLOAT -> GL.GL_R16F
        GLFormat.RG8_UNORM -> GL.GL_RG8
        GLFormat.RG16_SFLOAT -> GL.GL_RG16F
        GLFormat.RGBA8_UNORM -> GL.GL_RGBA8
        GLFormat.RGBA16_SFLOAT -> GL.GL_RGBA16F
    }
}

fun GLFormat.toGLFormat(): Int
{
    return when (this)
    {
        GLFormat.R8_UNORM -> GL.GL_RED
        GLFormat.R16_SFLOAT -> GL.GL_RED
        GLFormat.RG8_UNORM -> GL.GL_RG
        GLFormat.RG16_SFLOAT -> GL.GL_RG
        GLFormat.RGBA8_UNORM -> GL.GL_RGBA
        GLFormat.RGBA16_SFLOAT -> GL.GL_RGBA
    }
}

fun GLFormat.toGLType(): Int
{
    return when (this)
    {
        GLFormat.R8_UNORM -> GL.GL_UNSIGNED_BYTE
        GLFormat.R16_SFLOAT -> GL.GL_HALF_FLOAT
        GLFormat.RG8_UNORM -> GL.GL_UNSIGNED_BYTE
        GLFormat.RG16_SFLOAT -> GL.GL_HALF_FLOAT
        GLFormat.RGBA8_UNORM -> GL.GL_UNSIGNED_BYTE
        GLFormat.RGBA16_SFLOAT -> GL.GL_HALF_FLOAT
    }
}

fun GLFormat.toByteSize(): Int
{
    return when (this)
    {
        GLFormat.R8_UNORM -> 1
        GLFormat.R16_SFLOAT -> 2
        GLFormat.RG8_UNORM -> 2
        GLFormat.RG16_SFLOAT -> 4
        GLFormat.RGBA8_UNORM -> 4
        GLFormat.RGBA16_SFLOAT -> 8
    }
}

fun GLFormat.toBitmapConfig(): Bitmap.Config?
{
    return when (this)
    {
        GLFormat.R8_UNORM -> null
        GLFormat.R16_SFLOAT -> null
        GLFormat.RG8_UNORM -> null
        GLFormat.RG16_SFLOAT -> null
        GLFormat.RGBA8_UNORM -> Bitmap.Config.ARGB_8888
        GLFormat.RGBA16_SFLOAT -> Bitmap.Config.RGBA_F16
    }
}