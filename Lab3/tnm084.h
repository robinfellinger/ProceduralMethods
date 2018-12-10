/*
 * Framework library functions for the course TNM084
 * Author: Stefan Gustavson 2013 (stefan.gustavson@liu.se)
 * This code is in the public domain.
 */

#ifdef __WIN32__
/* Global function pointers for everything we need beyond OpenGL 1.1 */
extern PFNGLCREATEPROGRAMPROC           glCreateProgram;
extern PFNGLDELETEPROGRAMPROC           glDeleteProgram;
extern PFNGLUSEPROGRAMPROC              glUseProgram;
extern PFNGLCREATESHADERPROC            glCreateShader;
extern PFNGLDELETESHADERPROC            glDeleteShader;
extern PFNGLSHADERSOURCEPROC            glShaderSource;
extern PFNGLCOMPILESHADERPROC           glCompileShader;
extern PFNGLGETSHADERIVPROC             glGetShaderiv;
extern PFNGLGETPROGRAMIVPROC            glGetProgramiv;
extern PFNGLATTACHSHADERPROC            glAttachShader;
extern PFNGLGETSHADERINFOLOGPROC        glGetShaderInfoLog;
extern PFNGLGETPROGRAMINFOLOGPROC       glGetProgramInfoLog;
extern PFNGLLINKPROGRAMPROC             glLinkProgram;
extern PFNGLGETUNIFORMLOCATIONPROC      glGetUniformLocation;
extern PFNGLUNIFORM1FPROC               glUniform1f;
extern PFNGLUNIFORM1FVPROC              glUniform1fv;
extern PFNGLUNIFORM1IPROC               glUniform1i;
extern PFNGLUNIFORMMATRIX4FVPROC        glUniformMatrix4fv;
extern PFNGLGENBUFFERSPROC              glGenBuffers;
extern PFNGLISBUFFERPROC                glIsBuffer;
extern PFNGLBINDBUFFERPROC              glBindBuffer;
extern PFNGLBUFFERDATAPROC              glBufferData;
extern PFNGLDELETEBUFFERSPROC           glDeleteBuffers;
extern PFNGLGENVERTEXARRAYSPROC         glGenVertexArrays;
extern PFNGLISVERTEXARRAYPROC           glIsVertexArray;
extern PFNGLBINDVERTEXARRAYPROC         glBindVertexArray;
extern PFNGLDELETEVERTEXARRAYSPROC      glDeleteVertexArrays;
extern PFNGLENABLEVERTEXATTRIBARRAYPROC glEnableVertexAttribArray;
extern PFNGLVERTEXATTRIBPOINTERPROC     glVertexAttribPointer;
extern PFNGLDISABLEVERTEXATTRIBARRAYPROC glDisableVertexAttribArray;
extern PFNGLACTIVETEXTUREPROC           glActiveTexture;
extern PFNGLGENERATEMIPMAPPROC          glGenerateMipmap;
#endif


/*
 * printError() - Signal an error.
 */
void printError(const char *errtype, const char *errmsg);

/*
 * Override the Win32 filelength() function.
 */
long filelength(FILE *file);

/*
 * loadExtensions() - Load OpenGL extensions.
 */
void loadExtensions();

/*
 * readShaderFile(filename) - read a shader source string from a file
 */
unsigned char* readShaderFile(const char *filename);

/*
 * createShader() - create, load, compile and link the GLSL shader objects.
 */
GLuint createShader(char *vertexshaderfile, char *fragmentshaderfile);

/*
 * computeFPS() - Calculate, display and return frame rate statistics.
 */
double computeFPS(GLFWwindow *window);

/*
 * mat4rotx() - create a rotation matrix for rotation around the X axis
 */
void mat4rotx(GLfloat M[], float angle);

/*
 * mat4roty() - create a rotation matrix for rotation around the Y axis
 */
void mat4roty(GLfloat M[], float angle);

/*
 * mat4rotz() - create a rotation matrix for rotation around the Z axis
 */
void mat4rotz(GLfloat M[], float angle);

/*
 * mat4mult() - multiply two matrices
 */
void mat4mult(GLfloat M1[], GLfloat M2[], GLfloat Mout[]);

/*
 * mat4print() - print the elements of a matrix to the console (for debugging)
 */
void mat4print(GLfloat M[]);

