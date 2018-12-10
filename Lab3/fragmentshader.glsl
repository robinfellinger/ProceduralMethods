#version 330 core

uniform float time;
uniform sampler2D tex;

in vec3 interpolatedNormal;
in vec2 fragPos;

out vec4 color;

void main() {
	vec3 groundcolor = texture(tex,fragPos).rgb;

	float alpha = texture(tex, fragPos+vec2(sin(-0.02*time), 0.0)).a; //Makes the clouds move
	vec3 cloudcolor = vec3(1.0, 0.2, 0.2);
	vec3 diffusecolor = mix(groundcolor, cloudcolor, 0.8);
	
	vec3 nNormal = normalize(interpolatedNormal);
	float diffuselighting = max(0.0, nNormal.z);

	color = vec4(diffusecolor*diffuselighting, 0.5);
}

