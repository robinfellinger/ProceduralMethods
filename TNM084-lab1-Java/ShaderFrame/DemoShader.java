public class DemoShader extends Shader {

	void shader(double[] p, double u, double v, double t) {

		double g;
		g = 0.1 + 0.5*SimplexNoise.noise((u-0.9)*16.0, (0.5-v)*16.0, t*0.2);
		
		double h;
		h = 0.5 + 0.9*PerlinSimplexNoise.noise((u+Math.sin(t*0.02))*15.0, (Math.cos(0.02 *t) +v)*15.0, t*0.2);
		p[0]=Math.sin(t*0.3) - 0.1;
		p[1]= g * Math.sin(t*0.3) *5 -2*Math.cos(0.1 *t);
		p[2]=Math.cos(h);
	}

}
