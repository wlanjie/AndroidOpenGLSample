//
// Created by wlanjie on 2017/3/18.
//

#ifndef ANDROIDOPENGLSAMPLE_RECTANGLE_H
#define ANDROIDOPENGLSAMPLE_RECTANGLE_H

#include "opengl.h"

/**
 * 使用索引缓存绘制四方形
 */
class Rectangle : public OpenGL {
public:
    Rectangle();
    ~Rectangle();

private:
    GLuint vbo;
    GLuint ebo;
    GLuint shader;
    GLuint program;
    GLint positionLocation;

public:
    virtual void init(int width, int height);

    virtual void draw();

private:
    virtual GLuint loadShader(GLuint shaderType, const char *shaderSource);

    virtual void createProgram();

    virtual void release();
};

#endif //ANDROIDOPENGLSAMPLE_RECTANGLE_H
