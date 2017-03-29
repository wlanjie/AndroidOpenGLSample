//
// Created by wlanjie on 2017/3/23.
//

#include <malloc.h>

#include "texturetriangles.h"
#include "log.h"

TextureTriangles::TextureTriangles() :
        program(0), vbo(0), shader(0) {
}

TextureTriangles::~TextureTriangles() {

}

void TextureTriangles::init(int width, int height) {
    glViewport(0, 0, width, height);

    glGenBuffers(1, &vbo);
    glBindBuffer(GL_ARRAY_BUFFER, vbo);
    glBufferData(GL_ARRAY_BUFFER, sizeof(vertex), vertex, GL_STATIC_DRAW);

    createProgram();
    glUseProgram(program);

    GLint positionLocation = glGetAttribLocation(program, "vPosition");
    glEnableVertexAttribArray((GLuint) positionLocation);
    glVertexAttribPointer((GLuint) positionLocation, 3, GL_FLOAT, GL_FALSE, 0, 0);


    glGenTextures(1, &textures);
    glBindTexture(GL_TEXTURE_2D, textures);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_LINEAR);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_LINEAR);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);

    glTexImage2D(GL_TEXTURE_2D, 0, GL_RGB, width, height, 0, GL_RGB, GL_UNSIGNED_BYTE, NULL);
    glGenerateMipmap(GL_TEXTURE_2D);
    glBindTexture(GL_TEXTURE_2D, 0);
}

void TextureTriangles::draw() {
    glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
    glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
    glActiveTexture(GL_TEXTURE0);
    glBindTexture(GL_TEXTURE_2D, textures);
    glUniform1i(glGetUniformLocation(program, "sTexture"), 0);


    glDrawArrays(GL_TRIANGLES, 0, 3);
    glBindTexture(GL_TEXTURE_2D, 0);
}

void TextureTriangles::release() {

}

GLuint TextureTriangles::loadShader(GLuint shaderTyped, const char *shaderSource) {
    GLuint shader = glCreateShader(shaderTyped);
    if (shader) {
        glShaderSource(shader, 1, &shaderSource, NULL);
        glCompileShader(shader);
        GLint status = GL_FALSE;
        glGetShaderiv(shader, GL_COMPILE_STATUS, &status);
        if (status != GL_TRUE) {
            GLint infoLen;
            glGetShaderiv(shader, GL_INFO_LOG_LENGTH, &infoLen);
            if (infoLen) {
                char *buf = (char *) malloc((size_t) infoLen);
                if (buf) {
                    glGetShaderInfoLog(shader, infoLen, NULL, buf);
                    LOGE("Could not compile %s\n", buf);
                    free(buf);
                }
            }
        }
    }
    return shader;
}

void TextureTriangles::createProgram() {
    program = glCreateProgram();
    if (program) {
        GLuint vertextShader = loadShader(GL_VERTEX_SHADER, vertexSource);
        GLuint fragmentShader = loadShader(GL_FRAGMENT_SHADER, fragmentSource);
        glAttachShader(program, vertextShader);
        checkGlError("attachVertexShader");
        glAttachShader(program, fragmentShader);
        checkGlError("attachFragmentShader");
        glLinkProgram(program);
        checkGlError("linkProgram");
        GLint linkStatus = GL_FALSE;
        glGetProgramiv(program, GL_LINK_STATUS, &linkStatus);
        if (linkStatus != GL_TRUE) {
            GLint infoLen;
            glGetProgramiv(program, GL_INFO_LOG_LENGTH, &infoLen);
            if (infoLen) {
                char *buf = (char *) malloc((size_t) infoLen);
                if (buf) {
                    glGetProgramInfoLog(program, infoLen, NULL, buf);
                    LOGE("Could not link %s\n", buf);
                    free(buf);
                }
                glDeleteProgram(program);
                program = 0;
            }
        }
        glDeleteShader(vertextShader);
        glDeleteShader(fragmentShader);
    }
}