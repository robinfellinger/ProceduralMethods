#version 330 core

layout(location = 0) in vec3 Position;
layout(location = 1) in vec3 Normal;
layout(location = 2) in vec2 TexCoord;

uniform mat4 MV;
uniform mat4 P;
uniform float time;

out vec3 interpolatedNormal;
out vec2 fragPos;

void main(){
  vec3 pos = Position + Normal*(abs(sin(time)))/5;
  gl_Position = (P * MV) * vec4(pos, 1.0);
  interpolatedNormal = mat3(MV) * Normal;
  fragPos = TexCoord;
}

