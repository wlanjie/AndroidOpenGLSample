//
// Created by wlanjie on 2017/3/18.
//

#ifndef ANDROIDOPENGLSAMPLE_OPENGL_H
#define ANDROIDOPENGLSAMPLE_OPENGL_H

#include <GLES2/gl2.h>
#include <GLES2/gl2ext.h>

class OpenGL {

public:
    OpenGL();
    ~OpenGL();

public:
    virtual void init(int width, int height) = 0;

    virtual void draw() = 0;

    virtual void release() = 0;

    void checkGlError(const char* op);

private:
    virtual GLuint loadShader(GLuint shaderType, const char *shaderSource) = 0;

    virtual void createProgram() = 0;

};

#endif //ANDROIDOPENGLSAMPLE_OPENGL_H
