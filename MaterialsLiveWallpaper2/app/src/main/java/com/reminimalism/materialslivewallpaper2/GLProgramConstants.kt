package com.reminimalism.materialslivewallpaper2

object GLProgramConstants
{
    val vertexShader = """
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
            gl_Position = vec4(position, 1.0); // TODO: Transform the square to phone screen
            frag_normal = normal; // TODO: Transform rotation
            frag_tangent = tangent; // TODO: Transform rotation
            frag_uv = uv;
        }
    """.trimIndent()

    val fragmentShader = """
        precision highp float;
        
        varying vec3 frag_normal;
        varying vec3 frag_tangent;
        varying vec2 frag_uv;
        
        void main()
        {
            vec3 frag_bitangent = cross(frag_normal, frag_tangent);
            gl_FragColor = vec4(frag_uv.x, frag_uv.y, 1.0, 1.0);
        }
    """.trimIndent()
}