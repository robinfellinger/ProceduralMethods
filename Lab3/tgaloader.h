/* tgaloader.h */
/* Modified, stripped-down and cleaned-up version of TGA loader from NeHe tutorial 33 */
/* Stefan Gustavson (stefan.gustavson@liu.se 2013-11-20 */

#include <stdio.h>
#include <stdlib.h>
#include <GLFW/glfw3.h>

#ifdef __WIN32__
#include <GL/glext.h>
#endif

#include "tnm084.h" // To have access to glGenerateMipmap() extension in createTexture()

typedef	struct									
{
	GLubyte	*imageData;	// Image data (3 or 4 bytes per pixel)
	GLuint	bpp;		// Image color depth in bits per pixel
	GLuint	width;		// Image width
	GLuint	height;		// Image height
	GLuint	texID;		// Texture ID for OpenGL
	GLuint	type;		// Image type (3 bytes per pixel: GL_RGB, 4 bytes: GL_RGBA)
} Texture;	

typedef struct
{
	GLubyte Header[12];	// TGA File Header
} TGAHeader;

typedef struct
{
	GLubyte		header[6];								// First 6 useful bytes from the header
	GLuint		bytesPerPixel;							// Holds number of bytes per pixel used in the TGA file
	GLuint		imageSize;								// Data size in bytes
	GLuint		type;
	GLuint		height;									//Height of Image
	GLuint		width;									//Width ofImage
	GLuint		bpp;									// Bits Per Pixel
} TGA;

int loadTGA(Texture *texture, char *filename);		// Load a TGA file
int loadUncompressedTGA(Texture *texture, FILE *tgafile);	// Load an uncompressed file
void createTexture(Texture *texture, char *filename); // Load GL texture from file

