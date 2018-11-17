#version 330 core

layout (location = 0) in vec4 vert;

uniform  mat4 mvpMat;

void main()
{
	gl_Position = mvpMat * vert;
}
