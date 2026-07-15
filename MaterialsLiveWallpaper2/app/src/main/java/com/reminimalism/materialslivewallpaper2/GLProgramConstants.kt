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
            #define ANISOTROPY_SAMPLES 4
        #endif
        
        #ifndef CLEAR_COAT
            #define CLEAR_COAT 0
        #endif
        
        #ifndef FRESNEL_EFFECT
            #define FRESNEL_EFFECT 0
        #endif
        
        #ifndef TONE_MAP
            #define TONE_MAP 1
        #endif
        
        #ifndef EXPOSURE
            #define EXPOSURE 1.0
        #endif
        
        #ifndef TONE_MAPPING_METHOD
            #define TONE_MAPPING_METHOD 1
        #endif
        
        #define SDF_COUNT 8.0
        #define SDF_SIZE (1.0 / SDF_COUNT)
        
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
            vec3 diffuse;
            vec3 specular;
            vec3 normal;
            float roughness;
            vec2 anisotropy;
            
            #if CLEAR_COAT
            vec3 coat_normal;
            float coat_specular;
            float coat_roughness;
            #endif
        };
        
        struct SDFPoint
        {
            float value;
            vec2  dv;
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
            float roughness2 = (1.1/1.0) * roughness / (roughness + 0.1);
            float brightness = 0.5 + (1.0 - roughness) * clamp(t / max(0.001, roughness2), -0.45, 0.5);
            brightness *= 1.25 + dir.z * 0.75;
            return vec3(brightness, brightness, brightness);
        }
        
        float generate_M(vec2 uv)
        {
            // App icon generation demo
            vec2 uv_centered = uv * 2.0 - 1.0;
            float text = clamp(1.0 - 10.0 * abs(uv.x - 0.35), 0.0, 1.0)
                       + clamp(1.0 - 10.0 * abs(uv.x - 0.65), 0.0, 1.0);
            float v = clamp(1.0 - 5.0 * abs(abs(uv_centered.x) - uv.y + 0.4), 0.0, 1.0);
            v *= float(uv.x <= 0.65) * float(uv.x >= 0.35);
            text = max(text, v);
            text = min(text, clamp(2.5 - 10.0 * abs(uv.y - 0.5), 0.0, 1.0));
            return text;
        }
        
        float generate_o(vec2 uv)
        {
            // App icon generation demo
            vec2 uv_centered = uv * 2.0 - 1.0;
            float result = clamp((0.3 * SDF_COUNT) - (SDF_COUNT/2.0) * abs(length(uv_centered)), 0.0, 1.0);
            return result;
        }
        
        SDFPoint get_sdf(vec2 uv)
        {
            SDFPoint result;
            
            // There seems to be no ddx/ddy/fwidth available so...
            const float d = 0.00025;
            vec4 d_values = vec4(
                generate_o(uv + vec2(-d, -d)), // |. |
                generate_o(uv + vec2(d, -d)),  // | .|
                generate_o(uv + vec2(-d, d)),  // |' |
                generate_o(uv + vec2(d, d))    // | '|
            );
            result.value = dot(d_values, vec4(0.25, 0.25, 0.25, 0.25));
            result.dv = vec2(
                dot(d_values.yw - d_values.xz, vec2(SDF_SIZE * 0.5/d, SDF_SIZE * 0.5/d)),
                dot(d_values.zw - d_values.xy, vec2(SDF_SIZE * 0.5/d, SDF_SIZE * 0.5/d))
            );
            
            return result;
        }
        
        Surface get_surface(vec2 uv)
        {
            // Gold
            Surface result;
            result.diffuse = vec3(0, 0, 0);
            result.specular = vec3(1.0, 0.8, 0.35);
            
            #if ANISOTROPY == 0
            // Tiled roughness levels
            //result.roughness = mod(floor(uv.x * 8.0) + floor(uv.y * 8.0), 3.0) * (0.2/2.0);
            //vec2 noise = 0.1 * result.roughness * (vec2(rand(uv), rand(uv * 10.0)) * 2.0 - 1.0);
            // The above 2 lines are replaced with modified numbers to mimic the parameters
            // better with the fake roughness of the environment for now
            result.roughness = mod(floor(uv.x * 8.0) + floor(uv.y * 8.0), 3.0) * (0.6/2.0);
            vec2 noise = 0.033 * result.roughness * (vec2(rand(uv), rand(uv * 10.0)) * 2.0 - 1.0);
            result.normal = normalize(vec3(noise.x, noise.y, 1.0));
            result.anisotropy = vec2(0.0, 0.0);
            #endif
            
            #if ANISOTROPY == 1
            // Circular brush
            vec2 uv_centered = uv * 2.0 - 1.0;
            //float in_circle = float(dot(uv_centered, uv_centered) < 0.5);
            //float in_circle = 1.0;
            float in_circle = 1.0 - max(0.0, length(uv_centered));
            result.normal = vec3(0.0, 0.0, 1.0);
            result.roughness = 0.02 + 0.08 * in_circle;
            result.anisotropy = normalize(uv_centered) * 0.1 * in_circle;
            #endif
            
            // App icon generation demo
            //uv = uv * 2.0 - 0.5;
            //vec2 uv_centered = uv * 2.0 - 1.0;
            //// There seems to be no ddx/ddy/fwidth available so...
            ////const float d = 0.002;
            ////vec4 d_values = vec4(
            ////    generate_M(uv + vec2(-d, -d)), // |. |
            ////    generate_M(uv + vec2(d, -d)),  // | .|
            ////    generate_M(uv + vec2(-d, d)),  // |' |
            ////    generate_M(uv + vec2(d, d))    // | '|
            ////);
            ////SDFPoint sdf;
            ////sdf.value = dot(d_values, vec4(0.25, 0.25, 0.25, 0.25));
            ////sdf.dv.x = (d_values.y - d_values.x + d_values.w - d_values.z) / (d * 2.0);
            ////sdf.dv.y = (d_values.z - d_values.x + d_values.w - d_values.y) / (d * 2.0);
            //
            //SDFPoint sdf = get_sdf(uv);
            //
            //float is_inside = float(sdf.value >= 0.5);
            //float is_outside = 1.0 - is_inside;
            //result.diffuse = vec3(is_outside, is_outside, is_outside);
            //result.specular *= is_inside;
            //float curve = sdf.value * 2.0 - 1.0;
            //curve = 1.0 - curve * curve;
            //curve *= curve;
            ////result.normal = is_inside * vec3(0.0, 0.0, 1.0)
            ////              + is_outside * vec3(-sdf.dv * curve * 0.05, 1.0);
            //result.normal = is_inside * vec3(-sdf.dv * 0.1, 1.0)
            //              + is_outside * vec3(-sdf.dv * curve * 0.05, 1.0);
            //result.normal = normalize(result.normal);
            //result.roughness = 0.1;
            //result.anisotropy = normalize(uv_centered) * 0.1;
            
            #if CLEAR_COAT
            result.coat_normal = vec3(0.0, 0.0, 1.0);
            result.coat_specular = 0.05;
            result.coat_roughness = 0.0;
            #endif
            
            return result;
            
            // TODO:
            // Maybe apply ^2 for roughness and anisotropy
            // when sampling from textures
            // or on CPU if numbers are given directly.
            // Have to test, but this could give
            // a more useful range to work with.
            // Maybe not and just keep the linearity instead.
        }
        
        vec3 apply_exposure(vec3 color)
        {
            #if TONE_MAPPING_METHOD == 1
            return color * (float(EXPOSURE) * 1.25);
            #else
            return color * float(EXPOSURE);
            #endif
        }
        
        vec3 tone_map(vec3 color)
        {
            float max_color = max(max(color.x, color.y), color.z);
            
            // Method 1: Basic
            //float scale = 1.0 / (max_color + 0.5);
            //return color * scale + 1.5 * max(0.0, 0.67 - scale);
            
            #if TONE_MAPPING_METHOD == 0
            
            // Method 2: Not so accurate with smooth gradients when overexposed
            
            float t = min(1.0, max_color);
            // Gradual transition from linear
            float scale = (1.0 - t) + t / (max_color + 0.25);
            float white = max_color / (max_color + 6.0);
            return color * scale * (1.0 + white) + white;
            //return (color * scale + white) / (1.0 + white); // The above line is better
            
            #elif TONE_MAPPING_METHOD == 1
            
            // Method 3: Accurate with smooth gradients when overexposed
            //           Slightly darker: 1.25 * exposure corrects it
            
            float max_color_1 = max(0.0, max_color - 1.0);
            // Continuous but not gradual transition from linear
            float scale = 0.8 * min(1.0, max_color) + 0.2 * max_color_1 / (max_color_1 + 0.2);
            scale /= max(max_color, 0.001); // Avoid division by zero for black pixels
            float white = max_color_1 / (max_color_1 + 1.0);
            white *= white;
            return color * scale * (1.0 - white) + white;
            
            #else // TONE_MAPPING_METHOD
            
            // Method 4: Very crisp and accurate but not the smoothest gradients when overexposed
            float scale = 1.0 / max(1.0, max_color);
            float white = max(0.0, max_color - 1.0);
            white = white / (white + 2.0);
            return color * scale * (1.0 - white) + white;
            
            #endif // TONE_MAPPING_METHOD
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
            vec3 mesh_normal = normalize(frag_normal);
            vec3 tangent = normalize(frag_tangent);
            vec3 bitangent = cross(mesh_normal, tangent);
            vec3 view = normalize(frag_view);
            
            Surface surface = get_surface(frag_uv);
            vec3 normal = surface.normal.z * mesh_normal
                        + surface.normal.x * tangent
                        + surface.normal.y * bitangent;
            
            #if FRESNEL_EFFECT
            
            // Fresnel
            // Probably no need for fresnel usually
            // 6.0 exponent for non-metal, 50+ for metal (0.5+ specular component)
            float fresnel = pow(
                1.0 - max(0.0, dot(view, normal)),
                6.0 + dot(surface.specular, vec3(1.0, 1.0, 1.0)) * 35.0
            );
            surface.specular += (vec3(1.0, 1.0, 1.0) - surface.specular) * fresnel;
            surface.diffuse *= (1.0 - fresnel);
            #if CLEAR_COAT
            surface.coat_specular += (1.0 - surface.coat_specular) * fresnel;
            #endif
            
            #endif // FRESNEL_EFFECT
            
            #if ANISOTROPY
            
            vec3 anisotropy = surface.anisotropy.x * tangent + surface.anisotropy.y * bitangent;
            
            vec3 color = vec3(0.0, 0.0, 0.0);
            
            for (int i = -ANISOTROPY_SAMPLES; i <= ANISOTROPY_SAMPLES; i++)
            {
                float offset_noise = rand(frag_uv + vec2(float(i), 0));
                float offset = ((float(i) - 0.5) + offset_noise) / (float(ANISOTROPY_SAMPLES) + 0.5);
                vec3 normal_ani = normalize(normal + offset * anisotropy);
                color += calculate_specular(
                    surface.specular, surface.roughness, view, normal_ani
                );
            }
            color /= 1.0 + float(ANISOTROPY_SAMPLES * 2);
            
            #else
            
            vec3 color = calculate_specular(surface.specular, surface.roughness, view, normal);
            
            #endif // ANISOTROPY
            
            #if CLEAR_COAT
            
            vec3 coat_normal = surface.coat_normal.z * mesh_normal
                             + surface.coat_normal.x * tangent
                             + surface.coat_normal.y * bitangent;
            
            color = (1.0 - surface.coat_specular) * color + calculate_specular(
                vec3(surface.coat_specular, surface.coat_specular, surface.coat_specular),
                surface.coat_roughness, view, coat_normal
            );
            
            #endif // CLEAR_COAT
            
            color += surface.diffuse * sample_env(normal, 1.0);
            
            color = apply_exposure(color);
            
            #if TONE_MAP
            color = tone_map(color);
            color = sqrt(color);
            #endif
            
            gl_FragColor = vec4(color, 1.0);
            
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