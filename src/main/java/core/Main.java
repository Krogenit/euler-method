package core;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.system.MemoryUtil.NULL;

import java.nio.IntBuffer;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import org.joml.Vector2d;
import org.joml.Vector2f;
import org.joml.Vector4f;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;
import org.lwjgl.system.MemoryStack;

import util.Timer;
import client.TextureLoader;
import client.font.Font;
import client.gui.Button;
import client.input.Keyboard;
import client.input.Mouse;

public class Main 
{
	private class Vec
	{
		double[] vec;
		
		public Vec(int n)
		{
			vec = new double[n];
		}
		
		public double get(int i)
		{
			return vec[i];
		}
		
		public void set(double a, int i)
		{
			vec[i] = a;
		}
	}
	
	private class Matrix
	{
		double[][] mat;
		
		public Matrix(int n)
		{
			mat = new double[n][n];
		}
		
		public double get(int i, int j)
		{
			return mat[i][j];
		}
		
		public void set(double a, int i, int j)
		{
			mat[i][j] = a;
		}
	}
	
	int windowWidth, windowHeight;
	long window;
	
	float maxScale = 10000, minScale = 50;
	Vector2f scale = new Vector2f(250,250);
	Vector2f cameraMove = new Vector2f(-2,1);
	public static Font font;
	int n = 5;
	
	float stippleSize = 0.02f;
	float stippleOffset = 0.06f;
	
	boolean isRenderAnaliticStipples, isRenderEulersStipples,
			isRenderAnaliticPointsCoords, isRenderEulersPointsCoords,
			isRenderDiscrepancy;
	
	float pointsTextScale = 0.0025f, pointsUpScale = 400;
	
	List<Button> buttons = new ArrayList<Button>();
	
	List<Vector2d> eulersPoints = new ArrayList<Vector2d>();
	List<Vector2d> analiticPoints = new ArrayList<Vector2d>();
	double maxDiscrepancy;
	
	public static Vector4f white = new Vector4f(1,1,1,1);
	public static Vector4f black = new Vector4f(1,1,1,1);
	
	public static int interpolationNodeCount = 3;
	public static int funcType = 0;
	
	public void run()
	{
		calculate();
		init();
		loop();

		glfwFreeCallbacks(window);
		glfwDestroyWindow(window);

		glfwTerminate();
		glfwSetErrorCallback(null).free();
	}
	
	private void init()
	{
		GLFWErrorCallback.createPrint(System.err).set();

		if (!glfwInit())
			throw new IllegalStateException("Unable to initialize GLFW");

		glfwDefaultWindowHints();
		glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
		glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);

		GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());
		windowWidth = 1280;
		windowHeight = 720;

		window = glfwCreateWindow(windowWidth, windowHeight, "Window", NULL, NULL);
		if (window == NULL)
			throw new RuntimeException("Failed to create the GLFW window");

		glfwSetKeyCallback(window, (window, key, scancode, action, mods) -> {
			Keyboard.setKeyState(key, action);
		});

		glfwSetCursorPosCallback(window, (window, xpos, ypos) -> {
			Mouse.setPosition(xpos, ypos);
		});
		glfwSetMouseButtonCallback(window, (window, button, action, mods) -> {
			Mouse.onMouseButton(button, action, mods);
		});
		
		glfwSetScrollCallback(window, (window, xoffset, yoffset) -> {
			Mouse.onMouseScroll(xoffset, yoffset);
		});

		try (MemoryStack stack = stackPush())
		{
			IntBuffer pWidth = stack.mallocInt(1);
			IntBuffer pHeight = stack.mallocInt(1);

			glfwGetWindowSize(window, pWidth, pHeight);
			glfwSetWindowPos(window, (vidmode.width() - pWidth.get(0)) / 2, (vidmode.height() - pHeight.get(0)) / 2);
		}

		glfwMakeContextCurrent(window);
		glfwSwapInterval(1);
		glfwShowWindow(window);
	}
	
	private void setupOpenGL()
	{
		glfwSetWindowSizeCallback(window, (window, width1, height1) -> {
			windowWidth = width1;
			windowHeight = height1;
			glViewport(0, 0, width1, height1);
			glLoadIdentity();
			glOrtho(0, width1, height1, 0, 0.0F, 100.0F);
		});

		glEnable(GL_DEPTH_TEST);
		glDepthFunc(GL_LEQUAL);
		glEnable(GL_TEXTURE_2D);
		glEnable(GL_ALPHA_TEST);
		glAlphaFunc(GL_GREATER, 0.1F);
		glCullFace(GL_BACK);

		glViewport(0, 0, windowWidth, windowHeight);
		glLoadIdentity();
		glOrtho(0, windowWidth, windowHeight, 0, 0.0F, 100.0F);

		glClearColor(0.25F, 0.25F, 0.25F, 1.0F);
		glColor4f(1, 1, 1, 1);
	}
	
	private void initButtons()
	{
		buttons.clear();
		Button b = new Button(0, TextureLoader.button_square, 
				new Vector2f(40,130), "N value = " + n);
		b.setTextOrigin(40, 2);
		buttons.add(b);
		b = new Button(1, TextureLoader.button_square, new Vector2f(40,170), 
				"Interpolation node count = " + interpolationNodeCount);
		b.setTextOrigin(40, 2);
		buttons.add(b);
		b = new Button(2, TextureLoader.button_square, new Vector2f(40,210), 
				"Func type = " + funcType);
		b.setTextOrigin(40, 2);
		buttons.add(b);
		b = new Button(3, TextureLoader.button_square, new Vector2f(40,250),
				"Render analitic points coords = " + isRenderAnaliticPointsCoords);
		b.setTextOrigin(40, 2);
		buttons.add(b);
		b = new Button(4, TextureLoader.button_square, new Vector2f(40,290),
				"Render Euler's points coords = " + isRenderEulersPointsCoords);
		b.setTextOrigin(40, 2);
		buttons.add(b);
		b = new Button(5, TextureLoader.button_square, new Vector2f(40,330),
				"Render discrepancy = " + isRenderDiscrepancy);
		b.setTextOrigin(40, 2);
		buttons.add(b);
	}
	
	// ��������� ���� ������� f(x)
	private double f(double x)
	{
		switch(funcType)
		{
		case 0:
			return Math.cos(x);
		case 1:
			return Math.sin(x);
		case 2:
			return Math.exp(x) ;
		case 3:
			return Math.pow(x, 2);
		case 4:
			return Math.log(x);
		case 5:
			return Math.pow(x, 1.0f / 2.0f);
		}
		
		return x;
	}
	
	private void solve(Matrix A, Vec y, Vec coef)
	{
		int i, j, n = interpolationNodeCount;
		for (i = n - 1; i >= 0; i--)
		{
			coef.set(y.get(i), i);
			for (j = i + 1; j < n; j++)
				coef.set(coef.get(i) - A.get(i, j) * coef.get(j), i);
			coef.set(coef.get(i) / A.get(i, i), i);
		}
	}

	private void divideRow(Matrix A, Vec vec, int row, double num)
	{
		if (num != 0)
		{
			for (int i = 0; i < interpolationNodeCount; i++)
				A.set(A.get(row, i) / num, row, i);
			vec.set(vec.get(row) / num, row);
		}
	}

	// ������� �� ����� ������ ������, ���������� �� �����������
	private void substrByCoef(Matrix A, Vec vec, int row1, int row2, double cf)
	{
		for (int i = 0; i < interpolationNodeCount; i++)
			A.set(A.get(row1, i) * cf - A.get(row2, i), row1, i);
		vec.set(vec.get(row1) * cf - vec.get(row2), row1);
	}
	
	private void diag(Matrix A, Vec y)
	{
		int i, j, n = interpolationNodeCount;
		for (i = 0; i < n - 1; i++)
			if (A.get(i, i) != 0)
				for (j = i + 1; j < n; j++)
				{
					divideRow(A, y, j, A.get(j, i));
					substrByCoef(A, y, j, i, A.get(i, i));
				}
	}
	
	private double calculatePolynom(Vec coef, double x)
	{
		int i;
		double res=0;
		for(i=interpolationNodeCount-1; i>=0; i--)
			res = res*x + coef.get(i);
		return res;
	}
	
	private void calculate()
	{
		analiticPoints.clear();
		eulersPoints.clear();
		
		double a = 1.0d;
		double b = 10.0d;
		
		double shag = 1 / (double)n;
		
		int chebN = interpolationNodeCount;
		double[] xvec = new double[chebN];
		Vec yvec = new Vec(chebN);
		Vec coef = new Vec(chebN);
		
		for(int m = 1;m<chebN+1;m++)
		{
			double xi = ((a + b) / 2.0d) + (((b - a) / 2.0d) * Math.cos((Math.PI * (2.0d*(double)m-1.0d)) / (2.0d * (double)chebN)));
			xvec[m-1] = xi;
			double yi = f(xi);
			yvec.set(yi, m-1);
		}
		
		Matrix A = new Matrix(chebN);
		
		for(int i=0; i<chebN; i++) A.set(1, i, 0); 
		for(int i=0; i<chebN; i++)
			for(int j=1; j<chebN; j++)
				A.set(A.get(i, j-1)*xvec[i], i, j);
		
		diag(A, yvec);
		solve(A, yvec, coef);
		
		for(double x=a;x<=b;x+=shag)
		{
			double y = f(x);
			analiticPoints.add(new Vector2d(x ,y));
		}

		for(double x=a;x<=b;x+=shag)
		{
			double y = calculatePolynom(coef, x);
			eulersPoints.add(new Vector2d(x ,y));
		}
	}
	
	private void renderUtil()
	{
		glPushMatrix();
		glTranslatef(25, 25, 0);
		glColor3f(1, 0.25f, 0.25f);
		glLineWidth(5f);
		glBegin(GL_LINES);
		glVertex2d(0, 0);
		glVertex2d(50, 0);
		glEnd();
		glColor3f(0.25f, 0.5f, 1f);
		glBegin(GL_LINES);
		float yOffset = 30f;
		glVertex2d(0, yOffset);
		glVertex2d(50, yOffset);
		glEnd();
		glEnable(GL_TEXTURE_2D);
		font.render("Analitic graph", 0, 0, black);
		font.render("Interpolation graph", 0, yOffset, black);
		String disc = new DecimalFormat("##.####").format(maxDiscrepancy);
		font.render("Max discrepancy = " + disc, 0, 60, black);
		glPopMatrix();
		for(int i=0;i<buttons.size();i++)
		{
			Button b = buttons.get(i);
			b.render();
		}
		glDisable(GL_TEXTURE_2D);
	}
	
	private void renderCoords()
	{
		int maxCoods = 10;
		glColor4f(0.7f, 0.7f, 0.7f, 1f);
		glLineWidth(1f);
		glBegin(GL_LINES);
		glVertex2d(-maxCoods, 0);
		glVertex2d(maxCoods, 0);
		glVertex2d(0, -maxCoods);
		glVertex2d(0, maxCoods);
		
		float grid = 0.05f;
		for(int i=-maxCoods;i<maxCoods;i++)
		{
			glVertex2d(i, -grid);
			glVertex2d(i, grid);
			
			glVertex2d(-grid, -i);
			glVertex2d(grid, -i);
			
			for(double j = i;j<i+1;j+=0.1f)
			{
				float small = grid/10f;
				glVertex2d(-small, -j);
				glVertex2d(small, -j);
				
				glVertex2d(j, -small);
				glVertex2d(j, small);
			}
		}
		glEnd();
		glEnable(GL_TEXTURE_2D);
		glPushMatrix();
		float scale = 0.005f;
		glScalef(scale, scale, scale);
		for(int i=-maxCoods;i<maxCoods;i++)
		{
			font.render(""+i, i*(1/scale), 0, black);
		}
		for(int i=-maxCoods;i<maxCoods;i++)
		{
			font.render(""+i, 0, -i*(1/scale), black);
		}
		font.render("X", 600, 0, black);
		font.render("Y", 0, -600, black);
		glPopMatrix();
		glDisable(GL_TEXTURE_2D);
	}
	
	private void renderDiscrepancy()
	{
		float prev = stippleOffset;
		float prev1 = stippleSize;
		
		stippleOffset = 0.01f;
		stippleSize = 0.005f;
		glColor4f(0.25f, 0.25f, 0.25f, 1f);
		glBegin(GL_LINES);
		for(int i=0;i<analiticPoints.size();i++)
		{
			Vector2d v1 = analiticPoints.get(i);
			Vector2d v2 = eulersPoints.get(i);
			renderStipples(v1.x, -v1.y, v2.x, -v2.y);
		}
		glEnd();
		
		glEnable(GL_TEXTURE_2D);
		glPushMatrix();
		glScalef(pointsTextScale, pointsTextScale, pointsTextScale);
		for(int i=0;i<analiticPoints.size();i++)
		{
			Vector2d v1 = analiticPoints.get(i);
			Vector2d v2 = eulersPoints.get(i);
			double discrepancy = Math.abs(v1.y - v2.y);
			double x = v1.x;
			double y = (v1.y + v2.y) / 2.0d;
			String d = new DecimalFormat("##.####").format(discrepancy);
			font.render(d, (float)x*pointsUpScale, (float)-y*pointsUpScale, 
					new Vector4f(1,0.5f,0.1f,1));
		}
		glPopMatrix();
		glDisable(GL_TEXTURE_2D);
		
		stippleOffset = prev;
		stippleSize = prev1;
	}
	
	private void renderStipples(double x1, double y1, double x2, double y2)
	{
		double xmin = x1 < x2 ? x1 : x2;
		double xmax = x1 < x2 ? x2 : x1;
		double ymin = y1 < y2 ? y1 : y2;
		double ymax = y1 < y2 ? y2 : y1;
		for(double stipple = xmin;stipple<=xmax;stipple+=stippleOffset)
		{
			glVertex2d(stipple, y1);
			if(stipple+stippleSize > xmax)
				glVertex2d(xmax, y1);	
			else
				glVertex2d(stipple+stippleSize, y1);
		}
		for(double stipple = ymin;stipple<=ymax;stipple+=stippleOffset)
		{
			glVertex2d(x1, stipple);
			if(stipple+stippleSize > ymax)
				glVertex2d(x1, ymax);
			else
				glVertex2d(x1, stipple+stippleSize);
		}
	}
	
	private void renderAnaliticGraph()
	{
		glColor4f(1,0,0,1);
		glBegin(GL_LINE_STRIP);
		for(int i=0;i<analiticPoints.size();i++)
		{
			Vector2d vect = analiticPoints.get(i);
			glVertex2d(vect.x, -vect.y);
		}
		glEnd();
		
		if(isRenderAnaliticPointsCoords)
		{
			glEnable(GL_TEXTURE_2D);
			glPushMatrix();
			glScalef(pointsTextScale, pointsTextScale, pointsTextScale);
			for(int i=0;i<analiticPoints.size();i++)
			{
				Vector2d vect = analiticPoints.get(i);
				String xs = new DecimalFormat("##.##").format(vect.x);
				String ys = new DecimalFormat("##.##").format(vect.y);
				font.render( "("+xs+";"+ys+")", (float)vect.x*pointsUpScale, 
						(float)-vect.y*pointsUpScale, black);
			}
			glPopMatrix();
			glDisable(GL_TEXTURE_2D);
		}
		
		if(!isRenderAnaliticStipples)
			return;
		glColor4f(0.25f, 0.25f, 0.25f, 1f);
		glBegin(GL_LINES);
		for(int i=0;i<analiticPoints.size();i++)
		{
			Vector2d vect = analiticPoints.get(i);
			renderStipples(vect.x, -vect.y, 0, 0);
		}
		glEnd();
	}
	
	private void renderEulersGraph()
	{
		glColor4f(0.25f,0.5f,1,1);
		glBegin(GL_LINE_STRIP);
		for(int i=0;i<eulersPoints.size();i++)
		{
			Vector2d vect = eulersPoints.get(i);
			glVertex2d(vect.x, -vect.y);
		}
		glEnd();
		if(isRenderEulersPointsCoords)
		{
			glEnable(GL_TEXTURE_2D);
			glPushMatrix();
			glScalef(pointsTextScale, pointsTextScale, pointsTextScale);
			for(int i=0;i<eulersPoints.size();i++)
			{
				Vector2d vect = eulersPoints.get(i);
				String xs = new DecimalFormat("##.##").format(vect.x);
				String ys = new DecimalFormat("##.##").format(vect.y);
				font.render( "("+xs+";"+ys+")", (float)vect.x*pointsUpScale, 
						(float)-vect.y*pointsUpScale, black);
			}
			glPopMatrix();
			glDisable(GL_TEXTURE_2D);
		}
		
		
		if(!isRenderEulersStipples)
			return;

		glColor4f(0.25f, 0.25f, 0.25f, 1f);
		glBegin(GL_LINES);
		for(int i=0;i<eulersPoints.size();i++)
		{
			Vector2d vect = eulersPoints.get(i);
			renderStipples(vect.x, -vect.y, 0, 0);
		}
		glEnd();
	}
	
	private void renderScene()
	{
		glPushMatrix();
		glTranslatef(windowWidth/2.0f, windowHeight/2.0f, 0);
		glScalef(scale.x, scale.y, 1);
		glTranslatef(cameraMove.x, cameraMove.y, 0);
		renderCoords();
		
		glLineWidth(1f);
		renderAnaliticGraph();
		renderEulersGraph();
		if(isRenderDiscrepancy)
		renderDiscrepancy();
		glPopMatrix();
		renderUtil();
	}
	
	private void onButtonLeftClick(Button b)
	{
		switch(b.getId())
		{
		case 0:
			if(n == 5)
			{
				n = 20;
				pointsTextScale = 0.00075f;
				pointsUpScale = 1 / pointsTextScale;
				stippleSize = 0.01f;
				stippleOffset = 0.04f;
			}
			else if(n == 20)
			{
				n = 100;
				pointsTextScale = 0.00015f;
				pointsUpScale = 1 / pointsTextScale;
				stippleSize = 0.005f;
				stippleOffset = 0.02f;
			}
			else
			{
				n = 5;
				pointsTextScale = 0.0025f;
				pointsUpScale = 1 / pointsTextScale;
				stippleSize = 0.02f;
				stippleOffset = 0.06f;
			}
			calculate();
			b.setText("N value = " + n);
			break;
		case 1:
			interpolationNodeCount++;
			if(interpolationNodeCount > 10)
				interpolationNodeCount = 1;
			b.setText("Interpolation node count = " + interpolationNodeCount);
			break;
		case 2:
			funcType++;
			if(funcType > 5)
				funcType = 0;
			b.setText("Func type = " + funcType);
			break;
		case 3:
			isRenderAnaliticPointsCoords = !isRenderAnaliticPointsCoords;
			b.setText("Render analitic points coords = " + isRenderAnaliticPointsCoords);
			break;
		case 4:
			isRenderEulersPointsCoords = !isRenderEulersPointsCoords;
			b.setText("Render Euler's points coords = " + isRenderEulersPointsCoords);
			break;
		case 5:
			isRenderDiscrepancy = !isRenderDiscrepancy;
			b.setText("Render discrepancy = " + isRenderDiscrepancy);
			break;
		}
	}
	
	private void update()
	{
		calculate();
		if (Mouse.isLeftRelease())
			for(int i=0;i<buttons.size();i++)
			{
				Button b = buttons.get(i);
				if(b.isIntersects())
					onButtonLeftClick(b);
			}
		float speed = (scale.x / maxScale * 1000f);
		scale.y += Mouse.getScroll().y*speed;
		scale.x += Mouse.getScroll().y*speed;
		if(scale.x < minScale)
		{
			scale.x = minScale;
			scale.y = minScale;
		}
		else if(scale.x > maxScale)
		{
			scale.x = maxScale;
			scale.y = maxScale;
		}
		
		float baseSpeed = 25f;
		if(Keyboard.KEY_DOWN.isPress() || Keyboard.KEY_S.isPress())
		{
			cameraMove.y -= baseSpeed/scale.y;
		}
		else if(Keyboard.KEY_UP.isPress() || Keyboard.KEY_W.isPress())
		{
			cameraMove.y += baseSpeed/scale.y;
		}
		if(Keyboard.KEY_LEFT.isPress() || Keyboard.KEY_A.isPress())
		{
			cameraMove.x += baseSpeed/scale.y;
		}
		else if(Keyboard.KEY_RIGHT.isPress() || Keyboard.KEY_D.isPress())
		{
			cameraMove.x -= baseSpeed/scale.y;
		}
	}

	private void loop()
	{
		GL.createCapabilities();

		setupOpenGL();
		initButtons();

		float delta;
		float accumulator = 0f;
		float interval = 1f / 60.0f;

		Timer timer = new Timer();
		font = new Font();

		while (!glfwWindowShouldClose(window))
		{
			delta = timer.getDelta();
			accumulator += delta;

			while (accumulator >= interval)
			{
				update();
				timer.updateUPS();
				accumulator -= interval;
				Mouse.updateState();
			}
			
			glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
			
			renderScene();
		
			glfwSwapBuffers(window);
			glfwPollEvents();
		}
	}
	
	public static void main(String[] args)
	{
		new Main().run();
	}
}
