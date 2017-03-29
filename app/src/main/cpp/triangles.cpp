//
// Created by wlanjie on 2017/3/18.
//

#include "triangles.h"
#include "log.h"

#include <malloc.h>

GLfloat vertex[] = {
        -0.5f, -0.5f, 0.0f, // 左下角
        0.5f, -0.5f, 0.0f, // 右下角
        0.0f, 0.5f, 0.0f // 上角
};

auto vertexSource = "attribute vec4 vPosition;\n"
        "attribute vec2 texCoord;\n"
        "void main() {\n"
        "   gl_Position = vPosition;\n"
        "}\n";

auto fragmentSource = "precision mediump float;\n"
        "void main() {\n"
        "   gl_FragColor = vec4(1.0, 0.5, 0.2, 1.0);\n"
        "}\n";

Triangles::Triangles() {

}

Triangles::~Triangles() {

}

void Triangles::init(int width, int height) {
    glViewport(0, 0, width, height);
    // 使用glGenBuffers函数和一个缓冲ID生成一个VBO对象
    glGenBuffers(1, &vbo);
    // 使用glGenBuffers函数和一个缓冲ID生成一个VBO对象：    OpenGL有很多缓冲对象类型，顶点缓冲对象的缓冲类型是GL_ARRAY_BUFFER。OpenGL允许我们同时绑定多个缓冲，只要它们是不同的缓冲类型。我们可以使用glBindBuffer函数把新创建的缓冲绑定到GL_ARRAY_BUFFER目标上
    glBindBuffer(GL_ARRAY_BUFFER, vbo);
    // 我们使用的任何（在GL_ARRAY_BUFFER目标上的）缓冲调用都会用来配置当前绑定的缓冲(VBO)。然后我们可以调用glBufferData函数，它会把之前定义的顶点数据复制到缓冲的内存中
    // glBufferData是一个专门用来把用户定义的数据复制到当前绑定缓冲的函数。它的第一个参数是目标缓冲的类型：顶点缓冲对象当前绑定到GL_ARRAY_BUFFER目标上。第二个参数指定传输数据的大小(以字节为单位)；用一个简单的sizeof计算出顶点数据大小就行
    glBufferData(GL_ARRAY_BUFFER, sizeof(vertex), vertex, GL_STATIC_DRAW);

    createProgram();
    if (!program) {
        return;
    }
    positionLocation = glGetAttribLocation(program, "vPosition");

    glUseProgram(program);
    glEnableVertexAttribArray((GLuint) positionLocation);
    glBindBuffer(GL_ARRAY_BUFFER, vbo);
    /**
     * 使用glVertexAttribPointer函数告诉管线怎么解释顶点缓冲中的数据
     * 位置数据被储存为32-bit（4字节）浮点值。
        每个位置包含3个这样的值。
        在这3个值之间没有空隙（或其他值）。这几个值在数组中紧密排列。
        数据中第一个值在缓冲开始的位置。
     *
     *
     * 第一个参数指定我们要配置的顶点属性。这里使用 glGetAttribLocation获取到的vPosition的位置属性.
     * 还记得我们在顶点着色器中使用layout(location = 0)定义了position顶点属性的位置值(Location)吗？它可以把顶点属性的位置值设置为0。因为我们希望把数据传递到这一个顶点属性中，所以这里我们传入0。
     *  第二个参数指定顶点属性的大小。顶点属性是一个vec3，它由3个值组成，所以大小是3。
     *  第三个参数指定数据的类型，这里是GL_FLOAT(GLSL中vec*都是由浮点数值组成的)。
     *  下个参数定义我们是否希望数据被标准化(Normalize)。如果我们设置为GL_TRUE，所有数据都会被映射到0（对于有符号型signed数据是-1）到1之间。我们把它设置为GL_FALSE。
     *  第五个参数叫做步长(Stride)，它告诉我们在连续的顶点属性组之间的间隔。由于下个组位置数据在3个GLfloat之后，我们把步长设置为3 * sizeof(GLfloat)。要注意的是由于我们知道这个数组是紧密排列的（在两个顶点属性之间没有空隙）我们也可以设置为0来让OpenGL决定具体步长是多少（只有当数值是紧密排列时才可用）。一旦我们有更多的顶点属性，我们就必须更小心地定义每个顶点属性之间的间隔，我们在后面会看到更多的例子(译注: 这个参数的意思简单说就是从这个属性第二次出现的地方到整个数组0位置之间有多少字节)。
     *  最后一个参数的类型是GLvoid*，所以需要我们进行这个奇怪的强制类型转换。它表示位置数据在缓冲中起始位置的偏移量(Offset)。由于位置数据在数组的开头，所以这里是0。我们会在后面详细解释这个参数。
     */
    glVertexAttribPointer((GLuint) positionLocation, 3, GL_FLOAT, GL_FALSE, 0, 0);
//    glVertexAttribPointer(positionLocation, 2, GL_FLOAT, GL_FALSE, 3 * sizeof(GLfloat), (GLvoid *) 0);
    glBindBuffer(GL_ARRAY_BUFFER, 0);
}

void Triangles::draw() {
    glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
    glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
    glDrawArrays(GL_TRIANGLES, 0, 3);
}

GLuint Triangles::loadShader(GLuint shaderType, const char *shaderSource) {
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

void Triangles::createProgram() {
    program = glCreateProgram();
    if (program) {
        GLuint vertex = loadShader(GL_VERTEX_SHADER, vertexSource);
        GLuint fragment = loadShader(GL_FRAGMENT_SHADER, fragmentSource);
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

void Triangles::release() {
    glDeleteBuffers(1, &vbo);
    glDeleteProgram(program);
}