typedef struct {
	float phi;
	float theta;
	double lastTime;
} rotatorKey;

typedef struct {
	float phi;
	float theta;
	int lastX;
	int lastY;
	int lastLeft;
	int lastRight;
} rotatorMouse;

void initRotatorKey(rotatorKey *state);

void pollRotatorKey(GLFWwindow* window, rotatorKey *state);

void initRotatorMouse(rotatorMouse *state);

void pollRotatorMouse(GLFWwindow* window, rotatorMouse *state);

