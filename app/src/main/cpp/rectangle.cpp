//
// Created by wlanjie on 2017/3/18.
//

#include "rectangle.h"
#include "log.h"

#include <malloc.h>

GLfloat rectangleVertex[] = {
        0.5f, 0.5f, 0.0f, // 右上角
        0.5f, -0.5f, 0.0f, // 右下角
        -0.5f, -0.5f, 0.0f, // 左下角
        -0.5f, 0.5f, 0.0f // 左上角
};

GLuint rectangleIndex[] = {
        0, 1, 3,
        1, 2, 3
};

auto rectangleVertexSource = "attribute vec4 vPosition;\n"
        "void main() {\n"
        "   gl_Position = vPosition;\n"
        "}\n";

auto rectangleFragmentSource = "precision mediump float;\n"
        "void main() {\n"
        "   gl_FragColor = vec4(1.0, 0.5, 0.2, 1.0);\n"
        "}\n";


Rectangle::Rectangle() {

}

Rectangle::~Rectangle() {

}

void Rectangle::init(int width, int height) {
    glViewport(0, 0, width, height);

    glGenBuffers(1, &vbo);
    glBindBuffer(GL_ARRAY_BUFFER, vbo);
    glBufferData(GL_ARRAY_BUFFER, sizeof(rectangleVertex), rectangleVertex, GL_STATIC_DRAW);

    glGenBuffers(1, &ebo);
    glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ebo);
    glBufferData(GL_ELEMENT_ARRAY_BUFFER, sizeof(rectangleIndex), rectangleIndex, GL_STATIC_DRAW);

    createProgram();
    if (!program) {
        return;
    }

    positionLocation = glGetAttribLocation(program, "vPosition");

    glUseProgram(program);

    glEnableVertexAttribArray((GLuint) positionLocation);
    glBindBuffer(GL_ARRAY_BUFFER, vbo);
    // 使用索引绘制必须先要激活 GL_VERTEX_ARRAY 才会有数据显示
    glVertexAttribPointer((GLuint) positionLocation, 3, GL_FLOAT, GL_FALSE, 0, 0);
//    glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ebo);
    glBindBuffer(GL_ARRAY_BUFFER, 0);
}

void Rectangle::draw() {
    glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
    glClear(GL_DEPTH_BUFFER_BIT | GL_COLOR_BUFFER_BIT);
    // 使用当前绑定的GL_ELEMENT_ARRAY_BUFFER来绘制,这里当前绑定的是rectangleIndex
    glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_INT, 0);
}

GLuint Rectangle::loadShader(GLuint shaderType, const char *shaderSource) {
    GLuint shader = glCreateShader(shaderType);
    if (shader) {
        glShaderSource(shader, 1, &shaderSource, NULL);
        glCompileShader(shader);
        GLint compiled;
        glGetShaderiv(shader, GL_COMPILE_STATUS, &compiled);
        if (!compiled) {
            GLint infoLen;
            glGetShaderiv(shader, GL_INFO_LOG_LENGTH, &infoLen);
            if (infoLen) {
                char *buf = (char *) malloc((size_t) infoLen);
                if (buf) {
                    glGetShaderInfoLog(shader, infoLen, NULL, buf);
                    LOGE("Could not compile %d:\n%s\n", shaderType, buf);
                    free(buf);
                }
                glDeleteShader(shader);
                shader = 0;
            }
        }
    }

    return shader;
}

void Rectangle::createProgram() {
    program = glCreateProgram();
    if (program) {
        GLuint vertex = loadShader(GL_VERTEX_SHADER, rectangleVertexSource);
        GLuint fragment = loadShader(GL_FRAGMENT_SHADER, rectangleFragmentSource);
        glAttachShader(program, vertex);
        checkGlError("glAttachVertexShader");
        glAttachShader(program, fragment);
        checkGlError("glAttachFragmentShader");
        glLinkProgram(program);
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
        glDeleteShader(vertex);
        glDeleteShader(fragment);
    }
}

void Rectangle::release() {
    glDeleteBuffers(1, &vbo);
    glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
    glDeleteProgram(program);
}
