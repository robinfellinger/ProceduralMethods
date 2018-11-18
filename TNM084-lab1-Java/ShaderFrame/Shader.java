/* Abstract class to define a simple procedural shader.
 * It contains a single method, shader(). The method
 * uses one of its parameters for output to avoid creating
 * a new output array for each invocation. This saves time.
 * The input is a (u,v) texture coodinate pair and a time parameter.
 * The output is a double[3] array with RGB values.
 */

abstract class Shader {

	abstract void shader(double[] p, double u, double v, double t);

}
