This is a testbed for trying out animated procedural textures
in Java. The Java files included in this JCreator 5.0 project are:

ShaderMain - a class with a simple main() method to run a ShaderPanel
ShaderPanel - a Swing component that handles the drawing and the animation
Shader - an abstract class that does the actual procedural pattern
DemoShader - one concrete implementation of Shader, to get you started

Additionally, these static classes provide some useful functions:

ImprovedNoise - Ken Perlin's "Improved Noise" in Java.
PerlinSimplexNoise - Ken Perlin's "Simplex Noise" implementation.
SimplexNoise - a faster and more readable version of simplex noise.
WorleyNoise - Worley's "Cellular noise" badly ported from C to Java.

Note that most of the code was written for clarity, not speed.
The ShaderPanel and Shader pair is *not* the fastest way of
doing procedural images in Java. The separate method call
through the Shader object for each pixel takes extra time,
and the conversion between a double[3] array and the integer
pixel data is slower than if 8-bit integer data had been used
from the beginning. But a reasonably complicated procedural
pattern will take most of the processing power anyway, thus
making the extra time spent more or less negligible.


Stefan Gustavson 2012-11-06 (stegu@itn.liu.se)
