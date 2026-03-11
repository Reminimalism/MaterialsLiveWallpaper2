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
        uniform mat3 rotation;
        
        void main()
        {
            gl_Position = transform * vec4(position, 1.0);
            frag_normal = rotation * normal;
            frag_tangent = rotation * tangent;
            frag_uv = uv;
        }
    """.trimIndent()

    val fragmentShader = """
        precision highp float;
        
        varying vec3 frag_normal;
        varying vec3 frag_tangent;
        varying vec2 frag_uv;
        
        //uniform mat3 rotation;
        
        void main()
        {
            vec3 normal = normalize(frag_normal);
            vec3 tangent = normalize(frag_tangent);
            vec3 bitangent = cross(normal, tangent);
            gl_FragColor = vec4(frag_uv.x, frag_uv.y, 1.0, 1.0);
            
            // Rotation test
            //vec3 v = vec3(
            //    float(frag_uv.x < 0.5) * float(frag_uv.y < 0.5),
            //    float(frag_uv.x >= 0.5) * float(frag_uv.y < 0.5),
            //    float(frag_uv.y >= 0.5)
            //);
            //gl_FragColor = vec4(rotation * v, 1.0);
        }
    """.trimIndent()
}