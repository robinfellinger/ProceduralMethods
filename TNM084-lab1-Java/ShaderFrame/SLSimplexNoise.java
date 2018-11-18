/** SLSimplexNoise.java: a model study for a
 * hardware-friendly GLSL simplex noise algorithm.
 *
 * This version is actually quite a lot faster than
 * Ken Perlins reference Java implementation.
 *
 */

public class SLSimplexNoise {

  private static int grad[][] = {{1,1,0},{-1,1,0},{1,-1,0},{-1,-1,0},
                                 {1,0,1},{-1,0,1},{1,0,-1},{-1,0,-1},
                                 {0,1,1},{0,-1,1},{0,1,-1},{0,-1,-1},
                                 {1,1,0},{0,-1,1},{-1,1,0},{0,-1,-1}};

  private static int perm[] = {151,160,137,91,90,15,
  131,13,201,95,96,53,194,233,7,225,140,36,103,30,69,142,8,99,37,240,21,10,23,
  190, 6,148,247,120,234,75,0,26,197,62,94,252,219,203,117,35,11,32,57,177,33,
  88,237,149,56,87,174,20,125,136,171,168, 68,175,74,165,71,134,139,48,27,166,
  77,146,158,231,83,111,229,122,60,211,133,230,220,105,92,41,55,46,245,40,244,
  102,143,54, 65,25,63,161, 1,216,80,73,209,76,132,187,208, 89,18,169,200,196,
  135,130,116,188,159,86,164,100,109,198,173,186, 3,64,52,217,226,250,124,123,
  5,202,38,147,118,126,255,82,85,212,207,206,59,227,47,16,58,17,182,189,28,42,
  223,183,170,213,119,248,152, 2,44,154,163, 70,221,153,101,155,167, 43,172,9,
  129,22,39,253, 19,98,108,110,79,113,224,232,178,185, 112,104,218,246,97,228,
  251,34,242,193,238,210,144,12,191,179,162,241, 81,51,145,235,249,14,239,107,
  49,192,214, 31,181,199,106,157,184, 84,204,176,115,121,50,45,127, 4,150,254,
  138,236,205,93,222,114,67,29,24,72,243,141,128,195,78,66,215,61,156,180,
  151,160,137,91,90,15,
  131,13,201,95,96,53,194,233,7,225,140,36,103,30,69,142,8,99,37,240,21,10,23,
  190, 6,148,247,120,234,75,0,26,197,62,94,252,219,203,117,35,11,32,57,177,33,
  88,237,149,56,87,174,20,125,136,171,168, 68,175,74,165,71,134,139,48,27,166,
  77,146,158,231,83,111,229,122,60,211,133,230,220,105,92,41,55,46,245,40,244,
  102,143,54, 65,25,63,161, 1,216,80,73,209,76,132,187,208, 89,18,169,200,196,
  135,130,116,188,159,86,164,100,109,198,173,186, 3,64,52,217,226,250,124,123,
  5,202,38,147,118,126,255,82,85,212,207,206,59,227,47,16,58,17,182,189,28,42,
  223,183,170,213,119,248,152, 2,44,154,163, 70,221,153,101,155,167, 43,172,9,
  129,22,39,253, 19,98,108,110,79,113,224,232,178,185, 112,104,218,246,97,228,
  251,34,242,193,238,210,144,12,191,179,162,241, 81,51,145,235,249,14,239,107,
  49,192,214, 31,181,199,106,157,184, 84,204,176,115,121,50,45,127, 4,150,254,
  138,236,205,93,222,114,67,29,24,72,243,141,128,195,78,66,215,61,156,180};

  private static int fastfloor(double x) { return x>0 ? (int)x : (int)x-1; }

  private static double dot(int g[], double x, double y, double z) {
    return g[0]*x + g[1]*y + g[2]*z; // "dot(g, P)" built-in GLSL function
  }

  private static double dot(int g[], double x, double y) {
    return g[0]*x + g[1]*y; // "dot(g, P)" built-in GLSL function
  }

  // A 2D simplex noise implementation, to learn the basics of the algorithm
  public static double noise(double xin, double yin)
  {
  	double n0, n1, n2; // Noise contributions from the three corners

  	// Skew the input space to determine which simplex cell we're in
    final double F2 = 0.5*(Math.sqrt(3.0)-1.0);
  	double s = (xin+yin)/2.0*F2; // Hairy factor for 2D
  	int i = fastfloor(xin+s);
  	int j = fastfloor(yin+s);

    final double G2 = (3.0-Math.sqrt(3.0))/6.0;
    double t = (i+j)*G2;
    double X0 = i-t; // Unskew the cell origin back to (x,y) space
    double Y0 = j-t;
    double u0 = xin-X0; // The x,y distances from the cell origin
    double v0 = yin-Y0;

    // For the 2D case, the simplex shape is an equilateral triangle.
    // Determine which simplex we are in.
  	int i1, j1; // Offsets for second (middle) corner of simplex in (i,j) coords
    if(u0>v0) {i1=1; j1=0;} // lower triangle, XY order: (0,0)->(1,0)->(1,1)
    else {i1=0; j1=1;}      // upper triangle, YX order: (0,0)->(0,1)->(1,1)

    // A step of (1,0) in (i,j) means a step of (1-c,-c) in (x,y), and
    // a step of (0,1) in (i,j) means a step of (-c,1-c) in (x,y), where
    // c = (3-sqrt(3))/6 (This did take me some effort to work out.)

    double u1 = u0 - i1 + G2; // Offsets for middle corner in (x,y) coords
    double v1 = v0 - j1 + G2;
    double u2 = u0 - 1.0 + 2.0 * G2; // Offsets for last corner in (x,y) coords
    double v2 = v0 - 1.0 + 2.0 * G2;

    // Work out the hashed gradient indices of the three simplex corners
    int ii = i & 0xff;
    int jj = j & 0xff;
    int gi0 = perm[ii+perm[jj]] & 0x0f;
    int gi1 = perm[ii+i1+perm[jj+j1]] & 0x0f;
    int gi2 = perm[ii+1+perm[jj+1]] & 0x0f;

    // Calculate the contribution from the three corners
    double t0 = 0.5 - u0*u0-v0*v0;
    if(t0<0) n0 = 0.0;
    else {
      t0 *= t0;
      n0 = t0 * t0 * dot(grad[gi0], u0, v0);
    }

    double t1 = 0.5 - u1*u1-v1*v1;
    if(t1<0) n1 = 0.0;
    else {
      t1 *= t1;
      n1 = t1 * t1 * dot(grad[gi1], u1, v1);
    }

    double t2 = 0.5 - u2*u2-v2*v2;
    if(t2<0) n2 = 0.0;
    else {
      t2 *= t2;
      n2 = t2 * t2 * dot(grad[gi2], u2, v2);
    }

    // Add contributions from each corner to get the final noise value
    return 70.0 * (n0 + n1 + n2);
  }


  // A 3D simplex noise implementation, reimplemented from scratch
  public static double noise(double xin, double yin, double zin)
  {
    double n0, n1, n2, n3; // Noise contributions from the four corners

    // Skew the input space to determine which simplex cell we're in
    double s = (xin+yin+zin)/3.0; // Very nice and simple skew factor for 3D
    int i = fastfloor(xin+s);
    int j = fastfloor(yin+s);
    int k = fastfloor(zin+s);

    double t = (i+j+k)/6.0; // Very nice and simple unskew factor, too
    double X0 = i-t; // Unskew the cell origin back to (x,y,z) space
    double Y0 = j-t;
    double Z0 = k-t;
    double u0 = xin-X0; // The x,y,z distances from the cell origin
    double v0 = yin-Y0;
    double w0 = zin-Z0;

    // For the 3D case, the simplex shape is a slightly irregular tetrahedron.
    // Determine which simplex we are in.
    int i1, j1, k1; // Offsets for second corner of simplex in (i,j,k) coords
    int i2, j2, k2; // Offsets for third corner of simplex in (i,j,k) coords

    // The six lines of code below are REALLY hard to decipher,
    // and run slower in Java, but this method might be faster in GLSL
    // if real branching (conditional execution) cannot be performed.
    /*
    i1 = (u0>v0 ? (u0>w0 ? 1 : 0) : 0);
    j1 = (u0>v0 ? 0 : (v0>w0 ? 1 : 0));
    k1 = (v0>w0 ? 0 : (u0>w0 ? 0 : 1));
    i2 = (u0>v0 ? 1 : (u0>w0 ? 1 : 0));
    j2 = (u0>v0 ? (v0>w0 ? 1 : 0) : 1);
    k2 = (v0>w0 ? (u0>w0 ? 0 : 1) : 1);
    */

    // This is a more readable but less GLSL-friendly way to do it,
    // which runs quite a lot faster in Java than the code above.
    if(u0>=v0) {
      if(v0>=w0)
        { i1=1; j1=0; k1=0; i2=1; j2=1; k2=0; } // X Y Z order
        else if(u0>=w0) { i1=1; j1=0; k1=0; i2=1; j2=0; k2=1; } // X Z Y order
        else { i1=0; j1=0; k1=1; i2=1; j2=0; k2=1; } // Z X Y order
      }
    else { // u0<v0
      if(v0<w0) { i1=0; j1=0; k1=1; i2=0; j2=1; k2=1; } // Z Y X order
      else if(u0<w0) { i1=0; j1=1; k1=0; i2=0; j2=1; k2=1; } // Y Z X order
      else { i1=0; j1=1; k1=0; i2=1; j2=1; k2=0; } // Y X Z order
    }

    // And finally, here's is a way to do it with a lookup table.
    // Array indexing is hideously slow in Java, but should be very
    // fast in C and GLSL.
    // The LUT way of doing it translates nicely to 4D and even 5D,
    // and is easily implemented as a texture lookup in GLSL.
    /*
    final int o1[][]={{0,0,1},{1,0,0},{0,1,0},{0,1,0},
                      {0,0,1},{1,0,0},{1,0,0},{1,0,0}};
    final int o2[][]={{0,1,1},{1,1,0},{0,1,1},{1,1,0},
                      {1,0,1},{1,0,1},{1,1,0},{1,1,0}};
    int simplex = (u0>v0?4:0) + (v0>w0?2:0) + (u0>w0?1:0);
    i1 = o1[simplex][0];
    j1 = o1[simplex][1];
    k1 = o1[simplex][2];
    i2 = o2[simplex][0];
    j2 = o2[simplex][1];
    k2 = o2[simplex][2];
    */

    // A step of (1,0,0) in (i,j,k) means a step of (1-c,-c,-c) in (x,y,z),
    // a step of (0,1,0) in (i,j,k) means a step of (-c,1-c,-c) in (x,y,z), and
    // a step of (0,0,1) in (i,j,k) means a step of (-c,-c,1-c) in (x,y,z), where
    // c = 1/6.
    final double c = 1.0/6.0;

    double u1 = u0 - i1 + c; // Offsets for second corner in (x,y,z) coords
    double v1 = v0 - j1 + c;
    double w1 = w0 - k1 + c;
    double u2 = u0 - i2 + 2.0 * c; // Offsets for third corner in (x,y,z) coords
    double v2 = v0 - j2 + 2.0 * c;
    double w2 = w0 - k2 + 2.0 * c;
    double u3 = u0 - 1.0 + 3.0 * c; // Offsets for last corner in (x,y,z) coords
    double v3 = v0 - 1.0 + 3.0 * c;
    double w3 = w0 - 1.0 + 3.0 * c;

    // Work out the hashed gradient indices of the four simplex corners
    int ii = i & 0xff;
    int jj = j & 0xff;
    int kk = k & 0xff;
    int gi0 = perm[ii+perm[jj+perm[kk]]] & 0x0f;
    int gi1 = perm[ii+i1+perm[jj+j1+perm[kk+k1]]] & 0x0f;
    int gi2 = perm[ii+i2+perm[jj+j2+perm[kk+k2]]] & 0x0f;
    int gi3 = perm[ii+1+perm[jj+1+perm[kk+1]]] & 0x0f;

    // Calculate the contribution from the four corners
    double t0 = 0.6 - u0*u0 - v0*v0 - w0*w0;
    if(t0<0) n0 = 0.0;
    else {
      t0 *= t0;
      n0 = t0 * t0 * dot(grad[gi0], u0, v0, w0);
    }

    double t1 = 0.6 - u1*u1 - v1*v1 - w1*w1;
    if(t1<0) n1 = 0.0;
    else {
      t1 *= t1;
      n1 = t1 * t1 * dot(grad[gi1], u1, v1, w1);
    }

    double t2 = 0.6 - u2*u2 - v2*v2 - w2*w2;
    if(t2<0) n2 = 0.0;
    else {
      t2 *= t2;
      n2 = t2 * t2 * dot(grad[gi2], u2, v2, w2);
    }

    double t3 = 0.6 - u3*u3 - v3*v3 - w3*w3;
    if(t3<0) n3 = 0.0;
    else {
      t3 *= t3;
      n3 = t3 * t3 * dot(grad[gi3], u3, v3, w3);
    }

    // Add contributions from each corner to get the final noise value
    return 32.0*(n0 + n1 + n2 + n3); // This scales to just inside [-1,1]
  }

}
