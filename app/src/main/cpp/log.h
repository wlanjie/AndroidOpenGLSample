//
// Created by wlanjie on 2017/3/18.
//

#ifndef ANDROIDOPENGLSAMPLE_LOG_H
#define ANDROIDOPENGLSAMPLE_LOG_H

#include <android/log.h>

#define LOG_TAG "opengl"

#define  LOGI(...)  __android_log_print(ANDROID_LOG_INFO,LOG_TAG,__VA_ARGS__)
#define  LOGE(...)  __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)

#endif //ANDROIDOPENGLSAMPLE_LOG_H
