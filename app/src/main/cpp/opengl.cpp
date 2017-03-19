//
// Created by wlanjie on 2017/3/18.
//

#include "opengl.h"
#include "log.h"

OpenGL::OpenGL() {

}

OpenGL::~OpenGL() {

}

void OpenGL::checkGlError(const char* op) {
    for (GLint error = glGetError(); error; error = glGetError()) {
        LOGI("after %s() glError (0x%x)\n", op, error);
    }
}