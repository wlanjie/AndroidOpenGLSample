/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.wlanjie.opengl.sample;
/*
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLUtils;
import android.opengl.Matrix;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;
import javax.microedition.khronos.opengles.GL10;

/**
 * A simple GLSurfaceView sub-class that demonstrate how to perform
 * OpenGL ES 2.0 rendering into a GL Surface. Note the following important
 * details:
 *
 * - The class must use a custom context factory to enable 2.0 rendering.
 *   See ContextFactory class definition below.
 *
 * - The class must use a custom EGLConfigChooser to be able to select
 *   an EGLConfig that supports 2.0. This is done by providing a config
 *   specification to eglChooseConfig() that has the attribute
 *   EGL10.ELG_RENDERABLE_TYPE containing the EGL_OPENGL_ES2_BIT flag
 *   set. See ConfigChooser class definition below.
 *
 * - The class must select the surface's format, then choose an EGLConfig
 *   that matches it exactly (with regards to red/green/blue/alpha channels
 *   bit depths). Failure to do so would result in an EGL_BAD_MATCH error.
 */
class RenderView extends GLSurfaceView {

    enum DrawType {
        Triangles,
        Rectangle
    }

    private static String TAG = "GL2JNIView";
    private static final boolean DEBUG = false;
    private static DrawType drawType;

    public RenderView(Context context, DrawType type) {
        super(context);
        drawType = type;
        init(false, 0, 0);
    }

    public RenderView(Context context, boolean translucent, int depth, int stencil) {
        super(context);
        init(translucent, depth, stencil);
    }

    private void init(boolean translucent, int depth, int stencil) {

        /* By default, GLSurfaceView() creates a RGB_565 opaque surface.
         * If we want a translucent one, we should change the surface's
         * format here, using PixelFormat.TRANSLUCENT for GL Surfaces
         * is interpreted as any 32-bit surface with alpha by SurfaceFlinger.
         */
        if (translucent) {
            this.getHolder().setFormat(PixelFormat.TRANSLUCENT);
        }

        /* Setup the context factory for 2.0 rendering.
         * See ContextFactory class definition below
         */
        setEGLContextFactory(new ContextFactory());

        /* We need to choose an EGLConfig that matches the format of
         * our surface exactly. This is going to be done in our
         * custom config chooser. See ConfigChooser class definition
         * below.
         */
        setEGLConfigChooser( translucent ?
                             new ConfigChooser(8, 8, 8, 8, depth, stencil) :
                             new ConfigChooser(5, 6, 5, 0, depth, stencil) );

        /* Set the renderer responsible for frame rendering */
        setRenderer(new Renderer());
    }

    private static class ContextFactory implements EGLContextFactory {
        private static int EGL_CONTEXT_CLIENT_VERSION = 0x3098;
        public EGLContext createContext(EGL10 egl, EGLDisplay display, EGLConfig eglConfig) {
            Log.w(TAG, "creating OpenGL ES 2.0 context");
            checkEglError("Before eglCreateContext", egl);
            int[] attrib_list = {EGL_CONTEXT_CLIENT_VERSION, 2, EGL10.EGL_NONE };
            EGLContext context = egl.eglCreateContext(display, eglConfig, EGL10.EGL_NO_CONTEXT, attrib_list);
            checkEglError("After eglCreateContext", egl);
            return context;
        }

        public void destroyContext(EGL10 egl, EGLDisplay display, EGLContext context) {
            egl.eglDestroyContext(display, context);
        }
    }

    private static void checkEglError(String prompt, EGL10 egl) {
        int error;
        while ((error = egl.eglGetError()) != EGL10.EGL_SUCCESS) {
            Log.e(TAG, String.format("%s: EGL error: 0x%x", prompt, error));
        }
    }

    private static class ConfigChooser implements EGLConfigChooser {

        public ConfigChooser(int r, int g, int b, int a, int depth, int stencil) {
            mRedSize = r;
            mGreenSize = g;
            mBlueSize = b;
            mAlphaSize = a;
            mDepthSize = depth;
            mStencilSize = stencil;
        }

        /* This EGL config specification is used to specify 2.0 rendering.
         * We use a minimum size of 4 bits for red/green/blue, but will
         * perform actual matching in chooseConfig() below.
         */
        private static int EGL_OPENGL_ES2_BIT = 4;
        private static int[] s_configAttribs2 =
        {
            EGL10.EGL_RED_SIZE, 4,
            EGL10.EGL_GREEN_SIZE, 4,
            EGL10.EGL_BLUE_SIZE, 4,
            EGL10.EGL_RENDERABLE_TYPE, EGL_OPENGL_ES2_BIT,
            EGL10.EGL_NONE
        };

        public EGLConfig chooseConfig(EGL10 egl, EGLDisplay display) {

            /* Get the number of minimally matching EGL configurations
             */
            int[] num_config = new int[1];
            egl.eglChooseConfig(display, s_configAttribs2, null, 0, num_config);

            int numConfigs = num_config[0];

            if (numConfigs <= 0) {
                throw new IllegalArgumentException("No configs match configSpec");
            }

            /* Allocate then read the array of minimally matching EGL configs
             */
            EGLConfig[] configs = new EGLConfig[numConfigs];
            egl.eglChooseConfig(display, s_configAttribs2, configs, numConfigs, num_config);

            if (DEBUG) {
                 printConfigs(egl, display, configs);
            }
            /* Now return the "best" one
             */
            return chooseConfig(egl, display, configs);
        }

        public EGLConfig chooseConfig(EGL10 egl, EGLDisplay display,
                EGLConfig[] configs) {
            for(EGLConfig config : configs) {
                int d = findConfigAttrib(egl, display, config,
                        EGL10.EGL_DEPTH_SIZE, 0);
                int s = findConfigAttrib(egl, display, config,
                        EGL10.EGL_STENCIL_SIZE, 0);

                // We need at least mDepthSize and mStencilSize bits
                if (d < mDepthSize || s < mStencilSize)
                    continue;

                // We want an *exact* match for red/green/blue/alpha
                int r = findConfigAttrib(egl, display, config,
                        EGL10.EGL_RED_SIZE, 0);
                int g = findConfigAttrib(egl, display, config,
                            EGL10.EGL_GREEN_SIZE, 0);
                int b = findConfigAttrib(egl, display, config,
                            EGL10.EGL_BLUE_SIZE, 0);
                int a = findConfigAttrib(egl, display, config,
                        EGL10.EGL_ALPHA_SIZE, 0);

                if (r == mRedSize && g == mGreenSize && b == mBlueSize && a == mAlphaSize)
                    return config;
            }
            return null;
        }

        private int findConfigAttrib(EGL10 egl, EGLDisplay display,
                EGLConfig config, int attribute, int defaultValue) {

            if (egl.eglGetConfigAttrib(display, config, attribute, mValue)) {
                return mValue[0];
            }
            return defaultValue;
        }

        private void printConfigs(EGL10 egl, EGLDisplay display,
            EGLConfig[] configs) {
            int numConfigs = configs.length;
            Log.w(TAG, String.format("%d configurations", numConfigs));
            for (int i = 0; i < numConfigs; i++) {
                Log.w(TAG, String.format("Configuration %d:\n", i));
                printConfig(egl, display, configs[i]);
            }
        }

        private void printConfig(EGL10 egl, EGLDisplay display,
                EGLConfig config) {
            int[] attributes = {
                    EGL10.EGL_BUFFER_SIZE,
                    EGL10.EGL_ALPHA_SIZE,
                    EGL10.EGL_BLUE_SIZE,
                    EGL10.EGL_GREEN_SIZE,
                    EGL10.EGL_RED_SIZE,
                    EGL10.EGL_DEPTH_SIZE,
                    EGL10.EGL_STENCIL_SIZE,
                    EGL10.EGL_CONFIG_CAVEAT,
                    EGL10.EGL_CONFIG_ID,
                    EGL10.EGL_LEVEL,
                    EGL10.EGL_MAX_PBUFFER_HEIGHT,
                    EGL10.EGL_MAX_PBUFFER_PIXELS,
                    EGL10.EGL_MAX_PBUFFER_WIDTH,
                    EGL10.EGL_NATIVE_RENDERABLE,
                    EGL10.EGL_NATIVE_VISUAL_ID,
                    EGL10.EGL_NATIVE_VISUAL_TYPE,
                    0x3030, // EGL10.EGL_PRESERVED_RESOURCES,
                    EGL10.EGL_SAMPLES,
                    EGL10.EGL_SAMPLE_BUFFERS,
                    EGL10.EGL_SURFACE_TYPE,
                    EGL10.EGL_TRANSPARENT_TYPE,
                    EGL10.EGL_TRANSPARENT_RED_VALUE,
                    EGL10.EGL_TRANSPARENT_GREEN_VALUE,
                    EGL10.EGL_TRANSPARENT_BLUE_VALUE,
                    0x3039, // EGL10.EGL_BIND_TO_TEXTURE_RGB,
                    0x303A, // EGL10.EGL_BIND_TO_TEXTURE_RGBA,
                    0x303B, // EGL10.EGL_MIN_SWAP_INTERVAL,
                    0x303C, // EGL10.EGL_MAX_SWAP_INTERVAL,
                    EGL10.EGL_LUMINANCE_SIZE,
                    EGL10.EGL_ALPHA_MASK_SIZE,
                    EGL10.EGL_COLOR_BUFFER_TYPE,
                    EGL10.EGL_RENDERABLE_TYPE,
                    0x3042 // EGL10.EGL_CONFORMANT
            };
            String[] names = {
                    "EGL_BUFFER_SIZE",
                    "EGL_ALPHA_SIZE",
                    "EGL_BLUE_SIZE",
                    "EGL_GREEN_SIZE",
                    "EGL_RED_SIZE",
                    "EGL_DEPTH_SIZE",
                    "EGL_STENCIL_SIZE",
                    "EGL_CONFIG_CAVEAT",
                    "EGL_CONFIG_ID",
                    "EGL_LEVEL",
                    "EGL_MAX_PBUFFER_HEIGHT",
                    "EGL_MAX_PBUFFER_PIXELS",
                    "EGL_MAX_PBUFFER_WIDTH",
                    "EGL_NATIVE_RENDERABLE",
                    "EGL_NATIVE_VISUAL_ID",
                    "EGL_NATIVE_VISUAL_TYPE",
                    "EGL_PRESERVED_RESOURCES",
                    "EGL_SAMPLES",
                    "EGL_SAMPLE_BUFFERS",
                    "EGL_SURFACE_TYPE",
                    "EGL_TRANSPARENT_TYPE",
                    "EGL_TRANSPARENT_RED_VALUE",
                    "EGL_TRANSPARENT_GREEN_VALUE",
                    "EGL_TRANSPARENT_BLUE_VALUE",
                    "EGL_BIND_TO_TEXTURE_RGB",
                    "EGL_BIND_TO_TEXTURE_RGBA",
                    "EGL_MIN_SWAP_INTERVAL",
                    "EGL_MAX_SWAP_INTERVAL",
                    "EGL_LUMINANCE_SIZE",
                    "EGL_ALPHA_MASK_SIZE",
                    "EGL_COLOR_BUFFER_TYPE",
                    "EGL_RENDERABLE_TYPE",
                    "EGL_CONFORMANT"
            };
            int[] value = new int[1];
            for (int i = 0; i < attributes.length; i++) {
                int attribute = attributes[i];
                String name = names[i];
                if ( egl.eglGetConfigAttrib(display, config, attribute, value)) {
                    Log.w(TAG, String.format("  %s: %d\n", name, value[0]));
                } else {
                    // Log.w(TAG, String.format("  %s: failed\n", name));
                    while (egl.eglGetError() != EGL10.EGL_SUCCESS);
                }
            }
        }

        // Subclasses can adjust these values:
        protected int mRedSize;
        protected int mGreenSize;
        protected int mBlueSize;
        protected int mAlphaSize;
        protected int mDepthSize;
        protected int mStencilSize;
        private int[] mValue = new int[1];
    }

    private int vertexShader;
    private int fragmentShader;

    /**
     * 你可能注意到纹理上下颠倒了！这是因为OpenGL要求y轴0.0坐标是在图片的底部的，但是图片的y轴0.0坐标通常在顶部
     * 我们可以改变顶点数据的纹理坐标，翻转y值（用1减去y坐标）。
     * 我们可以编辑顶点着色器来自动翻转y坐标，替换TexCoord的值
     * 为TexCoord = vec2(texCoord.x, 1.0f - texCoord.y);。
     * 上面提供的解决方案仅仅通过一些黑科技让图片翻转。它们在大多数情况下都能正常工作，然而实际上这种方案的效果取决于你的实现和纹理，所以最好的解决方案是调整你的图片加载器，或者以一种y原点符合OpenGL需求的方式编辑你的纹理图像
     */
    private String vertexSource = "attribute vec4 aPosition;\n" +
        "attribute vec2 aTexCoord;\n" +
        "varying vec2 vTexCoord;\n" +
        "uniform mat4 uMatrix;\n" +
        "void main() {\n" +
        "    vTexCoord = vec2(aTexCoord.x, 1.0 - aTexCoord.y);\n" +
        "    gl_Position = uMatrix * aPosition;\n" +
        "}\n";

    private String fragmentSource = "precision mediump float;\n" +
        "varying vec2 vTexCoord;\n" +
        "uniform sampler2D sTexture;\n" +
        "void main() {\n" +
        "    gl_FragColor = texture2D(sTexture,vTexCoord);\n" +
        "}\n";

    private int program;
    private int textureId;

    private final float[] mVertexData = {
        1.0f,  1.0f, 0.0f, // 右上角
        1.0f, -1.0f, 0.0f, // 右下角
        -1.0f, -1.0f, 0.0f, // 左下角
        -1.0f,  1.0f, 0.0f // 在上角
    };

    private final short[] mIndexData = {
        0, 1, 3, // 第一个三角形
        1, 2, 3  // 第二个三角形
    };

    //纹理坐标
    private final float[] mTextureVertexData = {
        1.0f, 1.0f, // 右上角
        1.0f, 0.0f, // 右下角
        0.0f, 0.0f, // 左下角
        0.0f, 1.0f // 左上角
//        0.8f, 0.8f,
//        0.8f, 0.2f,
//        0.2f, 0.2f,
//        0.2f, 0.8f
    };

    private final float[] mProjectionMatrix = new float[16];
    private final float[] mCameraMatrix = new float[16];
    private final float[] mMVPMatrix = new float[16];

    private ShortBuffer mIndexBuffer;
    private FloatBuffer mVertexBuffer;
    private FloatBuffer mTextureVertexBuffer;
    public  float[] createIdentityMtx() {
        float[] m = new float[16];
        Matrix.setIdentityM(m, 0);
        return m;
    }

    private class Renderer implements GLSurfaceView.Renderer {
        public void onDrawFrame(GL10 gl) {

            GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);
            GLES20.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
            GLES20.glUseProgram(program);
            int mMatrix = GLES20.glGetUniformLocation(program, "uMatrix");
            GLES20.glUniformMatrix4fv(mMatrix, 1, false, mMVPMatrix, 0);
            int mPosition = GLES20.glGetAttribLocation(program, "aPosition");
            GLES20.glEnableVertexAttribArray(mPosition);
            GLES20.glVertexAttribPointer(mPosition, 3, GLES20.GL_FLOAT, false,
                12, mVertexBuffer);

            int aTextureCoordHandle = GLES20.glGetAttribLocation(program, "aTexCoord");
            GLES20.glEnableVertexAttribArray(aTextureCoordHandle);
            GLES20.glVertexAttribPointer(aTextureCoordHandle, 2, GLES20.GL_FLOAT, false, 8, mTextureVertexBuffer);

            GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);

            int uTextureSamplerHandle = GLES20.glGetUniformLocation(program, "sTexture");
            GLES20.glUniform1i(uTextureSamplerHandle, 0);

            GLES20.glDrawElements(GLES20.GL_TRIANGLES, mIndexData.length, GLES20.GL_UNSIGNED_SHORT, mIndexBuffer);
        }

        public void onSurfaceChanged(GL10 gl, int width, int height) {
            float ratio = width > height ? width / height : height / width;
            /**
             * 具体解释参见： https://blog.piasy.com/2016/06/07/Open-gl-es-android-2-part-1/
             * 前两个参数不用多说，Javadoc 里面就有，m 是保存变换矩阵的数组，offset 是开始保存的下标偏移量。后面的 6 个参数就是那篇博客中提到的参数了，那我们为什么要为它们设置这样的值呢？
             * surface view 的宽高为 width 和 height，我们要把 OpenGL 坐标系投影到 surface view 上。
             * 首先我们忽略坐标系的方向问题（方向问题由相机视觉解决），
             * 以 OpenGL 的方向为准，我们把坐标原点置于 view 中心，这样 view 的 y 轴范围就是 [- height / 2, height / 2]，x 轴范围就是 [- width / 2, width / 2]
             * 如果我们希望 OpenGL 坐标系 y 坐标范围充满 surface view 的高，
             * 那我们就需要让 [-1, 1] 和 [- height / 2, height / 2] 映射起来。
             * 怎么做呢？除以 height/ 2 即可。此时 x 轴范围就变成了 [- width / height, width / height]，也就是 [-ratio, ratio] 了，
             * 我们当然可以把 x 坐标范围归一化为 [-1, 1]，这时我们的矩阵代码需要变成这样，
             * float ratio = (float) height / width;
             * Matrix.frustumM(mProjectionMatrix, 0, -1, 1, -ratio, ratio, 3, 7);
             * 那最后的 near 和 far 是怎么确定的呢？它们是投影时的近平面和远平面的 z 坐标。这两个值其实是和相机视觉一起用的，所以我们在下节中再介绍，现在只需要记住，0 < near < far 即可
             */
            Matrix.frustumM(mProjectionMatrix, 0, -1, 1, -ratio, ratio, 3, 7);

            /**
             * 我们需要传入 9 个坐标值，(eyeX, eyeY, eyeX)，(centerX, centerY, centerZ)，(upX, upY, upZ)。
             * eye 表示相机的坐标点，center 表示物体（目标，或者图形）的中心坐标点，up 表示方向向量。
             * 通常情况下，我们都把 center 设置为坐标原点。而由于上节中介绍的投影变换是投影到 x, y 平面，
             * 所以相机都在 z 轴上，至于在 z 轴的哪个点，就要结合调用 Matrix.frustumM 时的 near 和 far 参数了，near <= z <= far 时我们才能看到渲染的内容，否则屏幕上就是空白了
             * up 向量的长度无关紧要，重要的是方向。左图中，我们把 up 设置为 y 轴方向，即 (0, 1, 0)，我们让 up 向量指向我们的正上方（这时不需要动），
             * 那此时我们看到的样式就是 OpenGL 渲染出来的样子。而在右图中，我们把 up 设置为 x 轴方向，即 (1, 0, 0)，这时我们就需要先把图逆时针旋转 90°，所以 OpenGL 渲染出来的将是逆时针旋转 90° 之后的
             */
            Matrix.setLookAtM(mCameraMatrix, 0, 0, 0, 3, 0, 0, 0, 0, 1, 0);
            Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mCameraMatrix, 0);
        }

        public void onSurfaceCreated(GL10 gl, EGLConfig config) {

            mIndexBuffer= ByteBuffer.allocateDirect(mIndexData.length * 2)
                .order(ByteOrder.nativeOrder())
                .asShortBuffer()
                .put(mIndexData);
            mIndexBuffer.position(0);

            mVertexBuffer = ByteBuffer.allocateDirect(mVertexData.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(mVertexData);
            mVertexBuffer.position(0);

            mTextureVertexBuffer = ByteBuffer.allocateDirect(mTextureVertexData.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(mTextureVertexData);
            mTextureVertexBuffer.position(0);

            // Do nothing.
            program = GLES20.glCreateProgram();
            vertexShader = GLES20.glCreateShader(GLES20.GL_VERTEX_SHADER);
            GLES20.glShaderSource(vertexShader, vertexSource);
            GLES20.glCompileShader(vertexShader);

            fragmentShader = GLES20.glCreateShader(GLES20.GL_FRAGMENT_SHADER);
            GLES20.glShaderSource(fragmentShader, fragmentSource);
            GLES20.glCompileShader(fragmentShader);

            GLES20.glAttachShader(program, vertexShader);
            GLES20.glAttachShader(program, fragmentShader);
            GLES20.glLinkProgram(program);
            textureId = loadTexture(getContext(), R.drawable.test);
        }
    }

    public int loadTexture(Context context, int resourceId) {

        final int[] textureObjectIds = new int[1];
        GLES20.glGenTextures(1, textureObjectIds, 0);
        if (textureObjectIds[0] == 0) {
            Log.d(TAG, ">> create texture fail");
            return 0;
        }

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inScaled = false;
        Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), resourceId, options);

        if (bitmap == null) {
            Log.d(TAG, ">> load bitmap fail");
            GLES20.glDeleteTextures(1, textureObjectIds, 0);
            return 0;
        }

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureObjectIds[0]);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR_MIPMAP_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR_MIPMAP_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_LINEAR);
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);
//        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, 0, bitmap.getWidth(), bitmap.getHeight(), 0, 0, 0, buffer);
        bitmap.recycle();

        //与target相关联的纹理图像生成一组完整的mipmap
        GLES20.glGenerateMipmap(GLES20.GL_TEXTURE_2D);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        return textureObjectIds[0];
    }
}
