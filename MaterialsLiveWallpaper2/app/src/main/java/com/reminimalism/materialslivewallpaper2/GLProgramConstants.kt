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
        
        #ifndef ANISOTROPY
            #define ANISOTROPY 1
        #endif
        
        #ifndef ANISOTROPY_SAMPLES
            #define ANISOTROPY_SAMPLES 8
        #endif
        
        varying vec3 frag_normal;
        varying vec3 frag_tangent;
        varying vec3 frag_view;
        varying vec2 frag_uv;
        
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
        
        struct Surface
        {
            vec3 normal;
            vec3 diffuse;
            vec3 specular;
            float roughness;
            vec2 anisotropy;
        };
        
        vec3 sample_env(vec3 dir, float roughness)
        {
            // Ground tiles & top lights that requires heavy sampling for roughness
            //vec2 plane = dir.xy / dir.z;
            //
            //vec2 planemod = mod(plane * 5.0, 2.0);
            //float light = float(dir.z >= 0.02) * max(0.0, dir.z - 0.02);
            //light *= floor(planemod.x) * floor(planemod.y);
            //
            //planemod = mod(plane * 10.0, 2.0);
            //float tiles = float(dir.z <= -0.02) * max(0.0, -dir.z - 0.02);
            //tiles *= float(planemod.x >= 0.2) * float(planemod.y >= 0.2);
            //
            //float ambient = 1.0 - dir.z * dir.z;
            //ambient = 0.05 + 0.1 * ambient * ambient;
            //
            //float color = ambient + light * 2.0 + tiles;
            //return vec3(color, color, color);
            
            // Easy environment with fake roughness
            float t = max(max(abs(dir.x), abs(dir.y)), abs(dir.z));
            vec3 dir_cube = dir / t;
            t = sqrt(dot(dir_cube, dir_cube) - 1.0);
            t = mod(t * 4.0, 1.0);
            t = float(t > 0.5) * (1.0 - t) + float(t <= 0.5) * t;
            t *= 2.0;
            t = t * t;
            t = 1.0 - t;
            t = t * t - 0.5;
            roughness = (1.1/1.0) * roughness / (roughness + 0.1);
            float brightness = 0.5 + (1.0 - roughness) * clamp(t / max(0.001, roughness), -0.4, 0.5);
            brightness *= 1.25 + dir.z * 0.75;
            return vec3(brightness, brightness, brightness);
        }
        
        Surface get_surface(vec2 uv)
        {
            // Gold
            Surface result;
            result.roughness = mod(floor(uv.x * 8.0) + floor(uv.y * 8.0), 3.0) * (0.2/2.0);
            result.roughness = 0.0;
            vec2 noise = 0.1 * result.roughness * (vec2(rand(uv), rand(uv * 10.0)) * 2.0 - 1.0);
            result.normal = normalize(vec3(noise.x, noise.y, 1.0));
            result.diffuse = vec3(0, 0, 0);
            result.specular = vec3(1.0, 0.8, 0.35);
            result.anisotropy = vec2(0.0, 0.04);
            result.roughness = 0.0;
            return result;
        }
        
        vec3 tone_map(vec3 color)
        {
            float max_color = max(max(color.x, color.y), color.z);
            
            // Method 1
            //float scale = 1.0 / (max_color + 0.5);
            //return color * scale + 1.5 * max(0.0, 0.67 - scale);
            
            // Method 2
            //float scale = 1.0 / (max_color + 1.0);
            //float white = max_color / (max_color + 10.0);
            //return (color * scale + white) / (scale + white);
            
            // Method 3
            float scale = 1.0 / max(1.0, max_color);
            float white = max(0.0, max_color - 1.0);
            white = white / (white + 2.0);
            return color * scale * (1.0 - white) + white;
        }
        
        vec3 calculate_specular(vec3 specular_color, float roughness, vec3 view, vec3 normal)
        {
            float view_normal_dot = dot(view, normal);
            vec3 l = (view_normal_dot * 2.0) * normal - view;
            float l_up = max(0.0, view_normal_dot);
            return specular_color * sample_env(l, l_up * roughness);
        }
        
        void main()
        {
            vec3 normal = normalize(frag_normal);
            vec3 tangent = normalize(frag_tangent);
            vec3 bitangent = cross(normal, tangent);
            vec3 view = normalize(frag_view);
            
            Surface surface = get_surface(frag_uv);
            normal = surface.normal.z * normal
                   + surface.normal.x * tangent
                   + surface.normal.y * bitangent;
            
            // Fresnel
            // Probably no need for fresnel really as it's not meaningful in wallpaper usage
            // 6.0 exponent for non-metal, 50+ for metal (0.5+ specular component)
            float fresnel = pow(
                1.0 - max(0.0, dot(view, normal)),
                6.0 + dot(surface.specular, vec3(1.0, 1.0, 1.0)) * 35.0
            );
            surface.specular += (vec3(1.0, 1.0, 1.0) - surface.specular) * fresnel;
            surface.diffuse *= (1.0 - fresnel);
            
            #if ANISOTROPY
            
            vec3 anisotropy = surface.anisotropy.x * tangent + surface.anisotropy.y * bitangent;
            
            float offset_noise = rand(frag_uv);
            float offset = (offset_noise - 0.5) / float(ANISOTROPY_SAMPLES + 1);
            //offset = (offset * offset + offset) * 0.5;
            vec3 normal_ani = normalize(normal + offset * anisotropy);
            
            vec3 color = calculate_specular(surface.specular, surface.roughness, view, normal_ani);
            
            for (int i = 1; i <= ANISOTROPY_SAMPLES; i++)
            {
                offset_noise = rand(frag_uv + vec2(float(i), 0));
                offset = (float(i) - 0.5 + offset_noise) / float(ANISOTROPY_SAMPLES + 1);
                //offset = (offset * offset + offset) * 0.5;
                normal_ani = normalize(normal + offset * anisotropy);
                color += calculate_specular(
                    surface.specular, surface.roughness, view, normal_ani
                );
                normal_ani = normalize(normal - offset * anisotropy);
                color += calculate_specular(
                    surface.specular, surface.roughness, view, normal_ani
                );
            }
            color /= 1.0 + float(ANISOTROPY_SAMPLES * 2);
            
            #else
            
            vec3 color = calculate_specular(surface.specular, surface.roughness, view, normal);
            
            #endif
            
            color += surface.diffuse * sample_env(normal, 1.0);
            color = tone_map(color);
            gl_FragColor = vec4(sqrt(color), 1.0);
            
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