package com.reminimalism.materialslivewallpaper2

class GLProgramConstants
{
    private val vertexShader = """
        attribute vec3 position;
        attribute vec3 normal;
        attribute vec3 tangent;
        attribute vec2 uv;
        
        varying vec3 frag_normal;
        varying vec3 frag_tangent;
        varying vec2 frag_uv;
        
        uniform mat4 transform;
        
        void main()
        {
            gl_Position = vec4(position, 1.0);
            frag_normal = transform * normal;
            frag_tangent = transform * tangent;
            frag_uv = uv;
        }
    """.trimIndent()

    private val fragmentShader = """
        precision highp float;
        
        varying vec3 frag_normal;
        varying vec3 frag_tangent;
        varying vec2 frag_uv;
        
        void main()
        {
            frag_bitangent = cross(normal, tangent);
            gl_FragColor = vec4(1.0, 1.0, 1.0, 1.0);
        }
    """.trimIndent()
}