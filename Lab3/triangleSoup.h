/* A struct to hold geometry data and send it off for rendering */
typedef struct {
       GLuint vao;          // Vertex array object, the main handle for geometry
       GLuint vertexbuffer; // Buffer ID to bind to GL_ARRAY_BUFFER
       GLuint indexbuffer;  // Buffer ID to bind to GL_ELEMENT_ARRAY_BUFFER
       GLfloat *vertexarray; // Vertex array on interleaved format: x y z nx ny nz s t
       GLuint *indexarray;   // Element index array
       int nverts; // Number of vertices in the vertex array
       int ntris;  // Number of triangles in the index array (may be zero)
} triangleSoup;

/* Initialize a triangleSoup object to all zeros */
void soupInit(triangleSoup *soup);

/* Clean up allocated data in a triangleSoup object */
void soupDelete(triangleSoup *soup);

/* Create a simple box geometry */
void soupCreateBox(triangleSoup *soup, float xsize, float ysize, float zsize);

/* Create a sphere (approximated by polygon segments) */
void soupCreateSphere(triangleSoup *soup, float radius, int segments);

/* Load geometry from an OBJ file */
void soupReadOBJ(triangleSoup* soup, char* filename);

/* Print data from a triangleSoup object, for debugging purposes */
void soupPrint(triangleSoup soup);

/* Print information about a triangleSoup object (stats and extents) */
void soupPrintInfo(triangleSoup soup);

/* Render the geometry in a triangleSoup object */
void soupRender(triangleSoup soup);

