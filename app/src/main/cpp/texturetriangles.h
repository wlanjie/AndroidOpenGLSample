//
// Created by wlanjie on 2017/3/23.
//

#ifndef ANDROIDOPENGLSAMPLE_TEXTURETRIANGLES_H
#define ANDROIDOPENGLSAMPLE_TEXTURETRIANGLES_H

#include "opengl.h"

class TextureTriangles : public OpenGL {

public:
    TextureTriangles();
    ~TextureTriangles();

private:
    GLuint vbo;
    GLuint shader;
    GLuint program;
    // 纹理坐标从0,0点开始,即屏幕左下角
    GLfloat texCoords[6] = {
            0.0f, 0.0f, // 左下角
            1.0f, 0.0f, // 右下角
            0.5f, 1.0f // 上中
    };

    GLfloat vertex[9] = {
        -1.0f, -1.0f, 0.0f, // 左下角
        1.0f, -1.0f, 0.0f, // 右下角
        0.0f, 1.0f, 0.0f // 上中
    };
    GLuint textures;

private:
    const char *vertexSource = "attribute vec4 vPosition;\n"
                "varying vec2 vTexCoord;\n"
                "void main() {\n"
                "   gl_Position = vPosition;\n"
                "}\n";

    const char *fragmentSource = "precision mediump float;\n"
                "varying vec2 vTexCoord;\n"
                "uniform sampler2D sTexture;\n"
                "void main() {\n"
                "   gl_FragColor = texture2D(sTexture, vTexCoord);\n"
                "}\n";

public:
    virtual void init(int width, int height);

    virtual void draw();

    virtual void release();

private:
    virtual GLuint loadShader(GLuint shaderTyped, const char *shaderSource);

    virtual void createProgram();
};


#endif //ANDROIDOPENGLSAMPLE_TEXTURETRIANGLES_H
