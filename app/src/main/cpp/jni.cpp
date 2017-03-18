#include <jni.h>
#include <string>
#include "opengl.h"
#include "triangles.h"

OpenGL *openGL;

extern "C"
void Java_com_wlanjie_opengl_sample_GL2JNILib_init(
        JNIEnv* env,
        jobject object, jint width, jint height, jint type) {
    if (type == 0) {
        openGL = new Triangles();
    }

    openGL->init(width, height);
}

extern "C"
void Java_com_wlanjie_opengl_sample_GL2JNILib_draw(JNIEnv *env, jobject object) {
    openGL->draw();
}

extern "C"
void Java_com_wlanjie_opengl_sample_GL2JNILib_release(JNIEnv *env, jobject object) {
    openGL->release();
    delete openGL;
}