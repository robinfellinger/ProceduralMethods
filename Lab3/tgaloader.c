/* tgaloader.c */
/* Modified, stripped-down and cleaned-up version of TGA loader from NeHe tutorial 33 */
/* Stefan Gustavson (stefan.gustavson@liu.se 2013-11-20 */

#include "tgaloader.h"

/*
 * loadTGA(Texture * texture, char * filename)
 * Open and test the file to make sure it is a valid TGA file	
 */

int loadTGA(Texture *texture, char *filename)
{
	FILE * fTGA;
	TGAHeader tgaheader;

	GLubyte uTGAcompare[12] = {0,0,2, 0,0,0,0,0,0,0,0,0}; // Uncompressed TGA Header
	GLubyte cTGAcompare[12] = {0,0,10,0,0,0,0,0,0,0,0,0}; // RLE Compressed TGA Header

	fTGA = fopen(filename, "rb");

	if(fTGA == NULL) // If the file didn't open...
	{
		fprintf(stderr, "Could not open texture file.\n");	// Display an error message
		return GL_FALSE;									// Exit function with "failure"
	}

	if(fread(&tgaheader, sizeof(TGAHeader), 1, fTGA) == 0)	// Attempt to read 12 byte header from file
	{
		fprintf(stderr, "Could not read file header.\n");	// If it fails, display an error message 
		if(fTGA != NULL)									// Check to see if file is still open
		{
			fclose(fTGA);									// If it is, close it
		}
		return GL_FALSE;									// Exit with failure
	}

	if(memcmp(uTGAcompare, &tgaheader, sizeof(tgaheader)) == 0)	// See if header matches the predefined header of 
	{															// an Uncompressed TGA image
		loadUncompressedTGA(texture, fTGA);			// If so, jump to Uncompressed TGA loading code
	}
	else if(memcmp(cTGAcompare, &tgaheader, sizeof(tgaheader)) == 0) // See if header matches the predefined header of
	{																 // an RLE compressed TGA image
		fprintf(stderr, "RLE compressed TGA files are not supported.\n");
	}
	else															// If header matches neither type
	{
		fprintf(stderr, "Unsupported image file format.\n");		// Unknown file type, or unknown TGA version
		fclose(fTGA);
		return GL_FALSE;											// Exit with failure
	}
	return GL_TRUE;													// All is well, return "success"
}

int loadUncompressedTGA(Texture *texture, FILE *fTGA) // Load an uncompressed TGA 
{													 	// (based on NeHe's TGA loading code)
	GLubyte temp;
	GLuint cswap;
	TGA tga;												// TGA image data

	if(fread(tga.header, sizeof(tga.header), 1, fTGA) == 0)		// Read TGA header
	{										
		fprintf(stderr, "Could not read info header.\n");		// Display error
		if(fTGA != NULL)										// if file is still open
		{
			fclose(fTGA);										// Close it
		}
		return GL_FALSE;										// Exit with failure
	}

	texture->width  = tga.header[1] * 256 + tga.header[0];		// Determine The TGA Width	(highbyte*256+lowbyte)
	texture->height = tga.header[3] * 256 + tga.header[2];		// Determine The TGA Height	(highbyte*256+lowbyte)
	texture->bpp	= tga.header[4];							// Determine the bits per pixel
	tga.width		= texture->width;							// Copy width into local structure						
	tga.height		= texture->height;							// Copy height into local structure
	tga.bpp			= texture->bpp;								// Copy BPP into local structure

	if((texture->width <= 0) || (texture->height <= 0)
		|| ((texture->bpp != 24) && (texture->bpp !=32)))		// Make sure all information is valid
	{
		fprintf(stderr, "Invalid texture information.\n");		// Display Error
		if(fTGA != NULL)										// Check if file is still open
		{
			fclose(fTGA);										// If so, close it
		}
		return GL_FALSE;										// Return "failure"
	}

	if(texture->bpp == 24)										// If the the image is 24 BPP
	{
		texture->type	= GL_RGB;								// Set Image type to GL_RGB
		printf("Texture type is GL_RGB\n");
	}
	else														// Else it's 32 BPP
	{
		texture->type	= GL_RGBA;								// Set image type to GL_RGBA
		printf("Texture type is GL_RGBA\n");
	}

	tga.bytesPerPixel	= (tga.bpp / 8);						// Compute the number of BYTES per pixel
	tga.imageSize		= (tga.bytesPerPixel * tga.width * tga.height);	// Compute the total amount of memory needed
	texture->imageData	= (GLubyte *)malloc(tga.imageSize);				// Allocate that much memory

	if(texture->imageData == NULL)										// If no space was allocated
	{
		fprintf(stderr, "Could not allocate memory for image.\n");		// Display Error
		fclose(fTGA);													// Close the file
		return GL_FALSE;												// Return "failure"
	}

	if(fread(texture->imageData, 1, tga.imageSize, fTGA) != tga.imageSize)	// Attempt to read image data
	{
		fprintf(stderr, "Could not read image data.\n");					// Display error
		if(texture->imageData != NULL)										// If image data was allocated
		{
			free(texture->imageData);										// Deallocate that data
		}
		fclose(fTGA);														// Close file
		return GL_FALSE;													// Return "failure"
	}

	// stegu 2013-11-19: Stupid, slow and outdated in-place XOR byte swapping code removed. Ugh.
	for(cswap = 0; cswap < (int)tga.imageSize; cswap += tga.bytesPerPixel)
	{
		temp = texture->imageData[cswap];
		texture->imageData[cswap] = texture->imageData[cswap+2];
		texture->imageData[cswap+2] = temp;
	}

	fclose(fTGA);			// Close file
	return GL_TRUE;			// Return success
}

/*
 * Load and activate a 2D texture from a TGA file
 */
void createTexture(Texture *texture, char *filename) {
    loadTGA(texture, filename);
	glEnable(GL_TEXTURE_2D); // Required for glBuildMipmap() to work (!)
	glGenTextures(1, &(texture->texID));     // Create The texture ID
    glBindTexture ( GL_TEXTURE_2D , texture->texID );
    // Set parameters to determine how the texture is resized
    glTexParameteri ( GL_TEXTURE_2D , GL_TEXTURE_MIN_FILTER , GL_LINEAR_MIPMAP_LINEAR );
    glTexParameteri ( GL_TEXTURE_2D , GL_TEXTURE_MAG_FILTER , GL_LINEAR );
    // Set parameters to determine how the texture wraps at edges
    glTexParameteri ( GL_TEXTURE_2D , GL_TEXTURE_WRAP_S , GL_REPEAT );
    glTexParameteri ( GL_TEXTURE_2D , GL_TEXTURE_WRAP_T , GL_REPEAT );
    // Read the texture data from file and upload it to the GPU
	glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, texture->width, texture->height, 0,
		texture->type, GL_UNSIGNED_BYTE, texture->imageData);
	glGenerateMipmap(GL_TEXTURE_2D);
}

