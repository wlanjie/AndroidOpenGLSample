//
// Created by wlanjie on 2017/3/18.
//

#ifndef ANDROIDOPENGLSAMPLE_TRIANGLES_H
#define ANDROIDOPENGLSAMPLE_TRIANGLES_H

#include "opengl.h"

class Triangles : public OpenGL {

public:
    Triangles();
    ~Triangles();

private:
    GLuint vbo;
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


#endif //ANDROIDOPENGLSAMPLE_TRIANGLES_H
