#version 330

layout (location =0) in vec3 position;
layout (location =1) in vec2 texCoord;

out vec2 outTexCoord;

uniform mat4 projMat;
uniform mat4 modelViewMat;

void main()
{
    gl_Position = projMat * modelViewMat * vec4(position, 1.0);
	outTexCoord = texCoord;
}