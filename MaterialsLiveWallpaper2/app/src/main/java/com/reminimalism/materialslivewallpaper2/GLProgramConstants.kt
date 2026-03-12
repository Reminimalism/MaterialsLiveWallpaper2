package com.reminimalism.materialslivewallpaper2

object GLProgramConstants
{
    val vertexShader = """
        attribute vec3 position;
        attribute vec3 normal;
        attribute vec3 tangent;
        attribute vec2 uv;
        
        uniform mat4 transform;
        uniform mat3 rotation;
        uniform float fov_tangent;
        
        varying vec3 frag_normal;
        varying vec3 frag_tangent;
        varying vec3 frag_view;
        varying vec2 frag_uv;
        
        void main()
        {
            gl_Position = transform * vec4(position, 1.0);
            frag_normal = rotation * normal;
            frag_tangent = rotation * tangent;
            frag_uv = uv;
            frag_view = rotation * vec3(-fov_tangent * position.xy / gl_Position.w, 1.0);
        }
    """.trimIndent()

    val fragmentShader = """
        precision highp float;
        
        varying vec3 frag_normal;
        varying vec3 frag_tangent;
        varying vec3 frag_view;
        varying vec2 frag_uv;
        
        vec3 sample_env(vec3 dir)
        {
            vec2 plane = dir.xy / dir.z;
            
            vec2 planemod = mod(plane * 5.0, 2.0);
            float light = float(dir.z >= 0.02) * max(0.0, dir.z - 0.02);
            light *= floor(planemod.x) * floor(planemod.y);
            
            planemod = mod(plane * 10.0, 2.0);
            float tiles = float(dir.z <= -0.02) * max(0.0, -dir.z - 0.02);
            tiles *= float(planemod.x >= 0.2) * float(planemod.y >= 0.2);
            
            float ambient = 1.0 - dir.z * dir.z;
            ambient = 0.05 + 0.1 * ambient * ambient;
            float color = ambient + light * 2.0 + tiles;
            return vec3(color, color, color);
        }
        
        float rand(float seed)
        {
            seed = mod(seed * seed * 3456.78 + 912.0, 1.0);
            seed = mod(seed * seed * 12345.67 + 891.0, 1.0);
            return seed;
        }
        
        float rand(vec2 seed)
        {
            return rand(rand(seed.x) + seed.y);
        }
        
        vec3 tone_map(vec3 color)
        {
            float max_color = max(max(color.x, color.y), color.z);
            float scale = 10.0 / (max_color + 10.0);
            return color * scale + max(0.0, (0.9 - scale));
        }
        
        void main()
        {
            vec3 normal = normalize(frag_normal);
            vec3 tangent = normalize(frag_tangent);
            vec3 bitangent = cross(normal, tangent);
            vec3 view = normalize(frag_view);
            
            // Gold material shader fun test
            //float roughness = frag_uv.y * frag_uv.y * 0.2;
            float roughness = mod(floor(frag_uv.x * 8.0) + floor(frag_uv.y * 8.0), 3.0) * (0.2/2.0);
            vec2 radius = vec2(
                roughness,
                roughness
            );
            vec2 noise = 0.1 * radius * (vec2(rand(frag_uv), rand(frag_uv * 10.0)) * 2.0 - 1.0);
            normal = normalize(normal + tangent * noise.x + bitangent * noise.y);
            vec3 l = (dot(view, normal) * 2.0) * normal - view;
            vec3 env = sample_env(l);
            float sum = 1.0;
            for (int x = -4; x <= 4; x++)
            {
                for (int y = -4; y <= 4; y++)
                {
                    vec2 offset = vec2(float(x) * (1.0/8.0), float(y) * (1.0/8.0));
                    float intensity = 1.0 - min(1.0, dot(offset, offset));
                    offset *= radius;
                    sum += intensity;
                    env += sample_env(l + tangent * offset.x + bitangent * offset.y) * intensity;
                }
            }
            env /= sum;
            vec3 col = vec3(1.0, 0.8, 0.35) * env;
            col = tone_map(col);
            gl_FragColor = vec4(sqrt(col), 1.0);
            
            // UV test
            //gl_FragColor = vec4(frag_uv.x, frag_uv.y, 1.0, 1.0);
            
            // Diagonal sections
            //gl_FragColor = vec4(
            //    float(frag_uv.x + frag_uv.y < 1.0),
            //    float(frag_uv.x - frag_uv.y < 0.0),
            //    1.0, 1.0
            //);
            
            // View test
            //gl_FragColor = vec4(view * 0.5 + 0.5, 1.0);
        }
    """.trimIndent()
}