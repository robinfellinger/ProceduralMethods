#include <stdio.h>  // For file input in soupReadOBJ()
#include <stdlib.h> // For malloc() and free()
#include <string.h> // For strcmp()
#include <math.h>   // For sin() and cos() in soupCreateSphere()
#include <GLFW/glfw3.h>

#ifdef __WIN32__
#include <GL/glext.h>
#endif

#include "tnm084.h"  // To be able to use OpenGL extensions below

#include "triangleSoup.h"


/* Initialize a triangleSoup object to all zeros */
void soupInit(triangleSoup *soup) {
	soup->vao = 0;
	soup->vertexbuffer = 0;
	soup->indexbuffer = 0;
	soup->vertexarray = NULL;
	soup->indexarray = NULL;
	soup->nverts = 0;
	soup->ntris = 0;
}


/* Clean up allocated data in a triangleSoup object */
void soupDelete(triangleSoup *soup) {

	if(glIsVertexArray(soup->vao)) {
		glDeleteVertexArrays(1, &(soup->vao));
	}
	soup->vao = 0;

	if(glIsBuffer(soup->vertexbuffer)) {
		glDeleteBuffers(1, &(soup->vertexbuffer));
	}
	soup->vertexbuffer = 0;

	if(glIsBuffer(soup->indexbuffer)) {
		glDeleteBuffers(1, &(soup->indexbuffer));
	}
	soup->indexbuffer = 0;

	if(soup->vertexarray) {
		free((void*)soup->vertexarray);
	}
	if(soup->indexarray) 	{
		free((void*)soup->indexarray);
	}
	soup->nverts = 0;
	soup->ntris = 0;

};


/* Create a simple box geometry */
void soupCreateBox(triangleSoup *soup, float xsize, float ysize, float zsize) {
	/* Not yet implemented */
};

/*
 * soupCreateSphere(triangleSoup soup, float radius, int segments)
 *
 * Create a triangleSoup objectwith vertex and index arrays
 * to draw a textured sphere with normals.
 * Increasing the parameter 'segments' yields more triangles.
 * The vertex array is on interleaved format. For each vertex, there
 * are 8 floats: three for the vertex coordinates (x, y, z), three
 * for the normal vector (n_x, n_y, n_z) and finally two for texture
 * coordinates (s, t). The arrays are allocated by malloc() inside the
 * function and should be disposed of using free() when they are no longer
 * needed, e.g with the function soupDelete().
 *
 * Author: Stefan Gustavson (stegu@itn.liu.se) 2013.
 * This code is in the public domain.
 */
void soupCreateSphere(triangleSoup *soup, float radius, int segments) {

	int i, j, base, i0;
	float x, y, z, R;
	double theta, phi;
	int vsegs, hsegs;
	int stride = 8;

	// Delete any previous content in the triangleSoup object
	soupDelete(soup);
  
	vsegs = segments;
	if (vsegs < 2) vsegs = 2;
	hsegs = vsegs * 2;
	soup->nverts = 1 + (vsegs-1) * (hsegs+1) + 1; // top + middle + bottom
	soup->ntris = hsegs + (vsegs-2) * hsegs * 2 + hsegs; // top + middle + bottom
	soup->vertexarray = (float*)malloc(soup->nverts * 8 * sizeof(float));
	soup->indexarray = (unsigned int*)malloc(soup->ntris * 3 * sizeof(int));

	// The vertex array: 3D xyz, 3D normal, 2D st (8 floats per vertex)
	// First vertex: top pole (+y is "up" in object local coords)
	soup->vertexarray[0] = 0.0f;
	soup->vertexarray[1] = radius;
	soup->vertexarray[2] = 0.0f;
	soup->vertexarray[3] = 0.0f;
	soup->vertexarray[4] = 1.0f;
	soup->vertexarray[5] = 0.0f;
	soup->vertexarray[6] = 0.5f;
	soup->vertexarray[7] = 1.0f;
	// Last vertex: bottom pole
	base = (soup->nverts-1)*stride;
	soup->vertexarray[base] = 0.0f;
	soup->vertexarray[base+1] = -radius;
	soup->vertexarray[base+2] = 0.0f;
	soup->vertexarray[base+3] = 0.0f;
	soup->vertexarray[base+4] = -1.0f;
	soup->vertexarray[base+5] = 0.0f;
	soup->vertexarray[base+6] = 0.5f;
	soup->vertexarray[base+7] = 0.0f;
	// All other vertices:
	// vsegs-1 latitude rings of hsegs+1 vertices each
	// (duplicates at texture seam s=0 / s=1)
	for(j=0; j<vsegs-1; j++) { // vsegs-1 latitude rings of vertices
		theta = (double)(j+1)/vsegs*M_PI;
		y = cos(theta);
		R = sin(theta);
		for (i=0; i<=hsegs; i++) { // hsegs+1 vertices in each ring (duplicate for texcoords)
        	phi = (double)i/hsegs*2.0*M_PI;
        	z = R*cos(phi);
        	x = R*sin(phi);
			base = (1+j*(hsegs+1)+i)*stride;
    		soup->vertexarray[base] = radius*x;
    		soup->vertexarray[base+1] = radius*y;
    		soup->vertexarray[base+2] = radius*z;
    		soup->vertexarray[base+3] = x;
    		soup->vertexarray[base+4] = y;
    		soup->vertexarray[base+5] = z;
    		soup->vertexarray[base+6] = (float)i/hsegs;
    		soup->vertexarray[base+7] = 1.0f-(float)(j+1)/vsegs;
		}
	}

	// The index array: triplets of integers, one for each triangle
	// Top cap
	for(i=0; i<hsegs; i++) {
    	soup->indexarray[3*i]=0;
		soup->indexarray[3*i+1]=1+i;
		soup->indexarray[3*i+2]=2+i;
	}
	// Middle part (possibly empty if vsegs=2)
	for(j=0; j<vsegs-2; j++) {
		for(i=0; i<hsegs; i++) {
			base = 3*(hsegs + 2*(j*hsegs + i));
			i0 = 1 + j*(hsegs+1) + i;
			soup->indexarray[base] = i0;
			soup->indexarray[base+1] = i0+hsegs+1;
			soup->indexarray[base+2] = i0+1;
			soup->indexarray[base+3] = i0+1;
			soup->indexarray[base+4] = i0+hsegs+1;
			soup->indexarray[base+5] = i0+hsegs+2;
		}
	}
	// Bottom cap
	base = 3*(hsegs + 2*(vsegs-2)*hsegs);
	for(i=0; i<hsegs; i++) {
		soup->indexarray[base+3*i] = soup->nverts-1;
		soup->indexarray[base+3*i+1] = soup->nverts-2-i;
		soup->indexarray[base+3*i+2] = soup->nverts-3-i;
	}

	// Generate one vertex array object (VAO) and bind it
	glGenVertexArrays(1, &(soup->vao));
	glBindVertexArray(soup->vao);

	// Generate two buffer IDs
	glGenBuffers(1, &(soup->vertexbuffer));
	glGenBuffers(1, &(soup->indexbuffer));

 	// Activate the vertex buffer
	glBindBuffer(GL_ARRAY_BUFFER, soup->vertexbuffer);
 	// Present our vertex coordinates to OpenGL
	glBufferData(GL_ARRAY_BUFFER,
		8*soup->nverts * sizeof(GLfloat), soup->vertexarray, GL_STATIC_DRAW);
	// Specify how many attribute arrays we have in our VAO
	glEnableVertexAttribArray(0); // Vertex coordinates
	glEnableVertexAttribArray(1); // Normals
	glEnableVertexAttribArray(2); // Texture coordinates
	// Specify how OpenGL should interpret the vertex buffer data:
	// Attributes 0, 1, 2 (must match the lines above and the layout in the shader)
	// Number of dimensions (3 means vec3 in the shader, 2 means vec2)
	// Type GL_FLOAT
	// Not normalized (GL_FALSE)
	// Stride 8 (interleaved array with 8 floats per vertex)
	// Array buffer offset 0, 3, 6 (offset into first vertex)
	glVertexAttribPointer(0, 3, GL_FLOAT, GL_FALSE,
		8*sizeof(GLfloat), (void*)0); // xyz coordinates
	glVertexAttribPointer(1, 3, GL_FLOAT, GL_FALSE,
		8*sizeof(GLfloat), (void*)(3*sizeof(GLfloat))); // normals
	glVertexAttribPointer(2, 2, GL_FLOAT, GL_FALSE,
		8*sizeof(GLfloat), (void*)(6*sizeof(GLfloat))); // texcoords

 	// Activate the index buffer
 	glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, soup->indexbuffer);
 	// Present our vertex indices to OpenGL
 	glBufferData(GL_ELEMENT_ARRAY_BUFFER,
	 	3*soup->ntris*sizeof(GLuint), soup->indexarray, GL_STATIC_DRAW);

	// Deactivate (unbind) the VAO and the buffers again.
	// Do NOT unbind the buffers while the VAO is still bound.
	// The index buffer is an essential part of the VAO state.
	glBindVertexArray(0);
	glBindBuffer(GL_ARRAY_BUFFER, 0);
 	glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);

};


/*
 * soupReadObj(triangleSoup* soup, char* filename)
 *
 * Load triangleSoup geometry data from an OBJ file.
 * The vertex array is on interleaved format. For each vertex, there
 * are 8 floats: three for the vertex coordinates (x, y, z), three
 * for the normal vector (n_x, n_y, n_z) and finally two for texture
 * coordinates (s, t). The returned arrays are allocated by malloc()
 * inside the function and should be disposed of using free() when
 * they are no longer needed, e.g. by calling soupDelete().
 *
 * Author: Stefan Gustavson (stegu@itn.liu.se) 2013.
 * This code is in the public domain.
 */
void soupReadOBJ(triangleSoup* soup, char* filename) {

	FILE *objfile;

	int numverts = 0;
	int numnormals = 0;
	int numtexcoords = 0;
	int numfaces = 0;
	int i_v = 0;
	int i_n = 0;
	int i_t = 0;
	int i_f = 0;
	float *verts, *normals, *texcoords;

	char line[256];
	char tag[3];
	int v1,v2,v3,n1,n2,n3,t1,t2,t3;
	int numargs, readerror, currentv;

	objfile = fopen(filename, "r");
	
	// Scan through the file to count the number of data elements
	while(fgets(line, 256, objfile)) {
		sscanf(line, "%2s ", tag);
		if(!strcmp(tag, "v")) numverts++;
		else if(!strcmp(tag, "vn")) numnormals++;
		else if(!strcmp(tag, "vt")) numtexcoords++;
		else if(!strcmp(tag, "f")) numfaces++;
		//else printf("Ignoring line starting with \"%s\"\n", tag);
	}
	
	printf("loadObj(\"%s\"): found %d vertices, %d normals, %d texcoords, %d faces.\n",
		filename, numverts, numnormals, numtexcoords, numfaces);

	verts = (float*)malloc(3*numverts*sizeof(float));
	normals = (float*)malloc(3*numnormals*sizeof(float));
	texcoords = (float*)malloc(2*numtexcoords*sizeof(float));

	soup->vertexarray = (float*)malloc(8*3*numfaces*sizeof(float));
	soup->indexarray = (unsigned int*)malloc(3*numfaces*sizeof(unsigned int));
	soup->nverts = 3*numfaces;
	soup->ntris = numfaces;

	rewind(objfile); // Start from the top again to read data

	readerror = 0;
	while(fgets(line, 256, objfile)) {
		tag[0] = '\0';
		sscanf(line, "%2s ", tag);
		if(!strcmp(tag, "v")) {
//			printf("Reading vertex %d\n", i_v+1);
			numargs = sscanf(line, "v %f %f %f",
				&verts[3*i_v], &verts[3*i_v+1], &verts[3*i_v+2]);
			if(numargs != 3) {
				printf("Malformed vertex data found at vertex %d.\n", i_v+1);
				printf("Aborting.\n");
				readerror = 1;
				break;
			}
//			printf("Read vertex coord %d: %8.2f %8.2f %8.2f\n",
//			i_v, verts[3*i_v], verts[3*i_v+1], verts[3*i_v+2]);
			i_v++;
		}
		else if(!strcmp(tag, "vn")) {
//			printf("Reading normal %d\n", i_n+1);
			numargs = sscanf(line, "vn %f %f %f",
				&normals[3*i_n], &normals[3*i_n+1], &normals[3*i_n+2]);
			if(numargs != 3) {
				printf("Malformed normal data found at normal %d.\n", i_n+1);
				printf("Aborting.\n");
				readerror = 1;
				break;
			}
			i_n++;
		}
		else if(!strcmp(tag, "vt"))  {
//          printf("Reading texcoord %d\n", i_t+1);
			numargs = sscanf(line, "vt %f %f",
				&texcoords[2*i_t], &texcoords[2*i_t+1]);
			if(numargs != 2) {
				printf("Malformed texcoord data found at texcoord %d.\n", i_t+1);
				printf("Aborting.\n");
				readerror = 1;
				break;
			}
			i_t++;
		}
		else if(!strcmp(tag, "f")) {
//			printf("Reading face %d\n", i_f+1);
			numargs = sscanf(line, "f %d/%d/%d %d/%d/%d %d/%d/%d",
				&v1, &t1, &n1, &v2, &t2, &n2, &v3, &t3, &n3);
			if(numargs != 9) {
				printf("Malformed face data found at face %d.\n", i_f+1);
				printf("Aborting.\n");
				readerror = 1;
				break;
			}
//			printf("Read vertex data %d/%d/%d %d/%d/%d %d/%d/%d\n",
//			v1, t1, n1, v2, t2, n2, v3, t3, n3);
			v1--; v2--; v3--; n1--; n2--; n3--; t1--; t2--; t3--;
			currentv = 8*3*i_f;
			soup->vertexarray[currentv] = verts[3*v1];
			soup->vertexarray[currentv+1] = verts[3*v1+1];
			soup->vertexarray[currentv+2] = verts[3*v1+2];
			soup->vertexarray[currentv+3] = normals[3*n1];
			soup->vertexarray[currentv+4] = normals[3*n1+1];
			soup->vertexarray[currentv+5] = normals[3*n1+2];
			soup->vertexarray[currentv+6] = texcoords[2*t1];
			soup->vertexarray[currentv+7] = texcoords[2*t1+1];
			soup->vertexarray[currentv+8] = verts[3*v2];
			soup->vertexarray[currentv+9] = verts[3*v2+1];
			soup->vertexarray[currentv+10] = verts[3*v2+2];
			soup->vertexarray[currentv+11] = normals[3*n2];
			soup->vertexarray[currentv+12] = normals[3*n2+1];
			soup->vertexarray[currentv+13] = normals[3*n2+2];
			soup->vertexarray[currentv+14] = texcoords[2*t2];
			soup->vertexarray[currentv+15] = texcoords[2*t2+1];
			soup->vertexarray[currentv+16] = verts[3*v3];
			soup->vertexarray[currentv+17] = verts[3*v3+1];
			soup->vertexarray[currentv+18] = verts[3*v3+2];
			soup->vertexarray[currentv+19] = normals[3*n3];
			soup->vertexarray[currentv+20] = normals[3*n3+1];
			soup->vertexarray[currentv+21] = normals[3*n3+2];
			soup->vertexarray[currentv+22] = texcoords[2*t3];
			soup->vertexarray[currentv+23] = texcoords[2*t3+1];
			soup->indexarray[3*i_f] = 3*i_f;
			soup->indexarray[3*i_f+1] = 3*i_f+1;
			soup->indexarray[3*i_f+2] = 3*i_f+2;
			i_f++;
		};
	}

	// Free the temporary arrays we created
	free(verts);
	free(normals);
	free(texcoords);
	fclose(objfile);

	if(readerror) { // Delete corrupt data and bail out if a read error occured
		soupDelete(soup);
		return;
	}
		
	// Generate one vertex array object (VAO) and bind it
	glGenVertexArrays(1, &(soup->vao));
	glBindVertexArray(soup->vao);

	// Generate two buffer IDs
	glGenBuffers(1, &(soup->vertexbuffer));
	glGenBuffers(1, &(soup->indexbuffer));

 	// Activate the vertex buffer
	glBindBuffer(GL_ARRAY_BUFFER, soup->vertexbuffer);
 	// Present our vertex coordinates to OpenGL
	glBufferData(GL_ARRAY_BUFFER,
		8*soup->nverts * sizeof(GLfloat), soup->vertexarray, GL_STATIC_DRAW);

	// Specify how many attribute arrays we have in our VAO
	glEnableVertexAttribArray(0); // Vertex coordinates
	glEnableVertexAttribArray(1); // Normals
	glEnableVertexAttribArray(2); // Texture coordinates
	// Specify how OpenGL should interpret the vertex buffer data:
	// Attributes 0, 1, 2 (must match the lines above and the layout in the shader)
	// Number of dimensions (3 means vec3 in the shader, 2 means vec2)
	// Type GL_FLOAT
	// Not normalized (GL_FALSE)
	// Stride 8 (interleaved array with 8 floats per vertex)
	// Array buffer offset 0, 3, 6 (offset into first vertex)
	glVertexAttribPointer(0, 3, GL_FLOAT, GL_FALSE,
		8*sizeof(GLfloat), (void*)0); // xyz coordinates
	glVertexAttribPointer(1, 3, GL_FLOAT, GL_FALSE,
		8*sizeof(GLfloat), (void*)(3*sizeof(GLfloat))); // normals
	glVertexAttribPointer(2, 2, GL_FLOAT, GL_FALSE,
		8*sizeof(GLfloat), (void*)(6*sizeof(GLfloat))); // texcoords

 	// Activate the index buffer
 	glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, soup->indexbuffer);
 	// Present our vertex indices to OpenGL
 	glBufferData(GL_ELEMENT_ARRAY_BUFFER,
	 	3*soup->ntris*sizeof(GLuint), soup->indexarray, GL_STATIC_DRAW);

	// Deactivate (unbind) the VAO and the buffers again.
	// Do NOT unbind the buffers while the VAO is still bound.
	// The index buffer is an essential part of the VAO state.
	glBindVertexArray(0);
	glBindBuffer(GL_ARRAY_BUFFER, 0);
 	glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);

	return;
};

/* Print data from a triangleSoup object, for debugging purposes */
void soupPrint(triangleSoup soup) {
     int i;

     printf("triangleSoup vertex data:\n\n");
     for(i=0; i<soup.nverts; i++) {
         printf("%d: %8.2f %8.2f %8.2f\n", i,
         soup.vertexarray[8*i], soup.vertexarray[8*i+1], soup.vertexarray[8*i+2]);
     }
     printf("\ntriangleSoup face index data:\n\n");
     for(i=0; i<soup.ntris; i++) {
         printf("%d: %d %d %d\n", i,
         soup.indexarray[3*i], soup.indexarray[3*i+1], soup.indexarray[3*i+2]);
     }
};

/* Print information about a triangleSoup object (stats and extents) */
void soupPrintInfo(triangleSoup soup) {
     int i;
     float x, y, z, xmin, xmax, ymin, ymax, zmin, zmax;

     printf("triangleSoup information:\n");
     printf("vertices : %d\n", soup.nverts);
     printf("triangles: %d\n", soup.ntris);
     xmin = xmax = soup.vertexarray[0];
     ymin = ymax = soup.vertexarray[1];
     zmin = zmax = soup.vertexarray[2];
     for(i=1; i<soup.nverts; i++) {
         x = soup.vertexarray[8*i];
         y = soup.vertexarray[8*i+1];
         z = soup.vertexarray[8*i+2];
//         printf("x y z : %8.2f %8.2f %8.2f\n", x, y, z);
         if(x<xmin) xmin = x;
         if(x>xmax) xmax = x;
         if(y<ymin) ymin = y;
         if(y>ymax) ymax = y;
         if(z<zmin) zmin = z;
         if(z>zmax) zmax = z;
     }
     printf("xmin: %8.2f\n", xmin);
     printf("xmax: %8.2f\n", xmax);
     printf("ymin: %8.2f\n", ymin);
     printf("ymax: %8.2f\n", ymax);
     printf("zmin: %8.2f\n", zmin);
     printf("zmax: %8.2f\n", zmax);
};

/* Render the geometry in a triangleSoup object */
void soupRender(triangleSoup soup) {
	
	glBindVertexArray(soup.vao);	
	glDrawElements(GL_TRIANGLES, 3 * soup.ntris, GL_UNSIGNED_INT, (void*)0);
	// (mode, vertex count, type, element array buffer offset)
	glBindVertexArray(0);	

};
