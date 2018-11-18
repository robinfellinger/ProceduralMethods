/** Java reference implementation of "Perlin Simplex Noise".
 *
 * Algorithm and original Java code by Ken Perlin (www.kenperlin.com).
 *
 * Math.floor() replaced by a much faster typecast method.
 * Javadoc comments added, some edits made to improve readability.
 *
 * Note that this method is not fast in Java. The bit-twiddling,
 * the many small methods and the numerous small array accesses
 * are really killing the performance of the algorithm.
 * This is a Java model for a hardware-friendly algorithm - there
 * are significantly faster ways of doing this in software.
 *
 * @author Original author Ken Perlin
 * @author Minor touch-ups 2004 Stefan Gustavson
 *
 */

public final class PerlinSimplexNoise {

	static int i,j,k, A[] = {0,0,0};
	static double u,v,w;

/** Floating point simplex noise - a fixed point version would be faster.
 * @param x Input x coordinate
 * @param y Input y coordinate
 * @param z Input z coordinate
 * @return Coherent noise value for (x,y,z)
 */
  public static double noise(double x, double y, double z) {

		double s = (x+y+z)/3.0;
		i = fastfloor(x+s); j = fastfloor(y+s); k = fastfloor(z+s);
		s = (i+j+k)/6.0; u = x-i+s; v = y-j+s; w = z-k+s;
		A[0] = A[1] = A[2] = 0;
		int hi = (u>=w ? (u>=v ? 0 : 1) : (v>=w ? 1 : 2));
		int lo = (u< w ? (u< v ? 0 : 1) : (v< w ? 1 : 2));
		return 4.0 * (K(hi) + K(3-hi-lo) + K(lo) + K(0));
	}

	private static double K(int a) {
		double s = (A[0] + A[1] + A[2]) / 6.0;
		double x = u - A[0] + s;
		double y = v - A[1] + s;
		double z = w - A[2] + s;
		double t = 0.6 - x*x - y*y - z*z;
		int h = shuffle(i + A[0], j + A[1], k + A[2]);
		A[a]++;
		if (t < 0)
		  return 0;
		int b5 = h>>5 & 1;
		int b4 = h>>4 & 1;
		int b3 = h>>3 & 1;
		int b2 = h>>2 & 1;
		int b = h & 3;
		double p = (b==1 ? x: (b==2 ? y : z));
		double q = (b==1 ? y : (b==2 ? z : x));
		double r = (b==1 ? z : (b==2 ? x : y));
		p = (b5==b3 ? -p : p);
		q = (b5==b4 ? -q : q);
		r = (b5!=(b4^b3) ? -r : r);
		t *= t;
		return 8 * t * t * (p + (b==0 ? q+r : b2==0 ? q : r));
	}

	private static int shuffle(int i, int j, int k) {
		return b(i,j,k,0) + b(j,k,i,1) + b(k,i,j,2) + b(i,j,k,3) +
		b(j,k,i,4) + b(k,i,j,5) + b(i,j,k,6) + b(j,k,i,7);
	}

    private static int fastfloor(double x) {
      int xi = (int)x;
      return x<xi ? xi-1 : xi;
    }
 
    private static int b(int i, int j, int k, int B) {
      return T[b(i,B)<<2 | b(j,B)<<1 | b(k,B)];
    }

	private static int b(int N, int B) {
	  return N>>B & 1;
	}

	private static int T[] = {0x15,0x38,0x32,0x2c,0x0d,0x13,0x07,0x2a};
}
