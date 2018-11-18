/** Java reference implementations of "Perlin Improved Noise".
 *
 * Algorithm and original Java code by Ken Perlin (www.kenperlin.com).
 *
 * Edits, clarifications and touch-ups by Stefan Gustavson:
 * Small bugfix in 2003, as announced by Ken Perlin.
 * Internal algorithm details made "private", floating
 * point and fixed point methods collected in one class.
 * Math.floor() replaced by a much faster typecast method.
 * Javadoc and other comments added, general readability improved.
 *
 * @author Copyright 2002-2003 Ken Perlin
 * @author Bug fix and touch-ups 2003-2004 Stefan Gustavson
 */

public final class ImprovedNoise {

/** Floating point Perlin noise - fixed point version below is mostly faster!
 * @param x Input x coordinate
 * @param y Input y coordinate
 * @param z Input z coordinate
 * @return Coherent noise value for (x,y,z)
 */
  public static double noise(double x, double y, double z) {
    int X = fastfloor(x) & 255,                 // Find the unit cube that
        Y = fastfloor(y) & 255,                 // contains the point x,y,z.
        Z = fastfloor(z) & 255;
    x -= fastfloor(x);                          // Find the relative x,y,z of
    y -= fastfloor(y);                          // of the point in that cube.
    z -= fastfloor(z);
    double u = fade(x),                                // Compute fade curves
           v = fade(y),                                // for each of x,y,z.
           w = fade(z);
    int A = p[X  ]+Y, AA = p[A]+Z, AB = p[A+1]+Z,      // Hash coordinates of
        B = p[X+1]+Y, BA = p[B]+Z, BB = p[B+1]+Z;      // the 8 cube corners,

    return lerp(w, lerp(v, lerp(u, grad(p[AA  ], x  , y  , z   ),  // and add
                                   grad(p[BA  ], x-1, y  , z   )), // blended
                           lerp(u, grad(p[AB  ], x  , y-1, z   ),  // results
                                   grad(p[BB  ], x-1, y-1, z   ))),// from 8
                   lerp(v, lerp(u, grad(p[AA+1], x  , y  , z-1 ),  // corners
                                   grad(p[BA+1], x-1, y  , z-1 )), // of cube.
                           lerp(u, grad(p[AB+1], x  , y-1, z-1 ),
                                   grad(p[BB+1], x-1, y-1, z-1 ))));
  }

  // Internal helper methods and data structures for floating point version

  // Like in C/C++, this is a lot faster than the native method Math.floor().
  private static int fastfloor(double x) { int xi = (int)x; return x<xi ? xi-1 : xi; }

  // The fade function is 6t^5-15t^4+10t^3, which has zero first and second
  // derivatives at 0 and 1. This is what makes this an "improved noise".
  private static double fade(double t) { return t*t*t*(t*(t*6-15)+10); }

  // The acronym "lerp" is common tech-speak for "linear interpolation".
  private static double lerp(double t, double a, double b) {return a+t*(b-a);}

  // This is the pseudo-random gradient generation.
  // Note that grad() is the only "random" element of the algorithm.
  // It's really not very random at all, but it's not noticeably regular.
  private static double grad(int hash, double x, double y, double z) {
    int h = hash & 15;                      // Convert low 4 bits of hash code
    double u = h<8 ? x : y;                 // into 12 gradient direction.
    double v = h<4 ? y : h==12||h==14 ? x : z;
    return ((h&1) == 0 ? u : -u) + ((h&2) == 0 ? v : -v);
  }

  // Permutation array, used for both floating point and fixed point versions
  private static final int p[] = new int[512];
  private static final int permutation[] = { 151,160,137,91,90,15,
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
  138,236,205,93,222,114,67,29,24,72,243,141,128,195,78,66,215,61,156,180 };
  // Replicate this 256 element array twice into p[] to avoid index wrapping
  // ("static" code in a class is run only once, on loading of the class.)
  static { for (int i=0; i < 256 ; i++) p[256+i] = p[i] = permutation[i]; }


/** Integer, fixed-point Perlin noise - a lot faster on most platforms.
 * The fixed point representation is 16:16, so 65536 (2^16) means "1.0".
 * @param x Input x coordinate
 * @param y Input y coordinate
 * @param z Input z coordinate
 * @return Coherent noise value for (x,y,z)
 */
   public static int noise(int x, int y, int z) {
      int X = x>>16 & 255, Y = y>>16 & 255, Z = z>>16 & 255, N = 1<<16;
      x &= N-1; y &= N-1; z &= N-1;
      int u=fade(x),v=fade(y),w=fade(z), A=p[X  ]+Y, AA=p[A]+Z, AB=p[A+1]+Z,
                                         B=p[X+1]+Y, BA=p[B]+Z, BB=p[B+1]+Z;
      return lerp(w, lerp(v, lerp(u, grad(p[AA  ], x   , y   , z   ),  
                                     grad(p[BA  ], x-N , y   , z   )), 
                             lerp(u, grad(p[AB  ], x   , y-N , z   ),  
                                     grad(p[BB  ], x-N , y-N , z   ))),
                     lerp(v, lerp(u, grad(p[AA+1], x   , y   , z-N ),  
                                     grad(p[BA+1], x-N , y   , z-N )), 
                             lerp(u, grad(p[AB+1], x   , y-N , z-N ),
                                     grad(p[BB+1], x-N , y-N , z-N ))));
   }

   // Internal helper methods and data structures for fixed point version

   private static int lerp(int t, int a, int b) { return a+(t*(b-a)>>12); }

   private static int grad(int hash, int x, int y, int z) {
      int h = hash&15;
      int u = h<8?x:y;
      int v = h<4?y:h==12||h==14?x:z;
      return ((h&1) == 0 ? u : -u) + ((h&2) == 0 ? v : -v);
   }

   private static int fade(int t) {
      int t0 = fade[t >> 8], t1 = fade[Math.min(255, (t >> 8) + 1)];
      return t0 + ( (t & 255) * (t1 - t0) >> 8 );
   }

   // A lookup table in an array to speed up the fade function.
   private static int fade[] = new int[256];

   // Static initialisation code for the fixed-point fade[] lookup table.
   static { for(int i=0; i<256; i++) fade[i]=(int)((1<<12)*fade(i/256.0));}
}
