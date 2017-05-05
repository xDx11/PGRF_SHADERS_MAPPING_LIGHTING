package cviceni_grid;

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.util.texture.Texture;

import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseEvent;
import java.awt.event.KeyListener;
import java.awt.event.KeyEvent;

import oglutils.OGLBuffers;
import oglutils.OGLTextRenderer;
import oglutils.OGLTexture;
import oglutils.OGLTexture2D;
import oglutils.OGLUtils;
import oglutils.ShaderUtils;
import oglutils.ToFloatArray;
import transforms.Camera;
import transforms.Mat4;
import transforms.Mat4PerspRH;
import transforms.Vec3D;


public class Renderer implements GLEventListener, MouseListener,
		MouseMotionListener, KeyListener {

	double x, y, z;
	int width, height, ox, oy, switchShaderType, switchPolygonMode, switchLightMode, switchFunctionType, switchReflectorMode, switchVertexMode, switchTexture;
	final static int NORMAL_TYPE = 1;
	final static int NORMAL_TYPE_FRAG_LIGHT = 2;
	final static int POLYGON_MODE_FILL = 1;
	final static int POLYGON_MODE_LINE = 2;
	final static int LIGHT_MODE_POSITION = 1;
	final static int LIGHT_MODE_COLOR = 2;
	final static int LIGHT_MODE_NORMAL = 3;
	final static int LIGHT_MODE_DIFFUSION = 4;
	final static int LIGHT_MODE_BLINN_PHONG_HALF_VECTOR = 5;
	final static int LIGHT_MODE_BLINN_PHONG_REFLECT_VECTOR = 6;
	final static int LIGHT_MODE_TEXTURE_NORMAL = 7;
	final static int LIGHT_MODE_TEXTURE_PARALLAX = 8;
	final static int FUNCTION_GRID = 1;
	final static int FUNCTION_CARTESIAN_1 = 2;
	final static int FUNCTION_CARTESIAN_2 = 3;
	final static int FUNCTION_CARTESIAN_3 = 4;	
	final static int FUNCTION_CYLINDER_1 = 5;
	final static int FUNCTION_CYLINDER_2 = 6;
	final static int FUNCTION_CYLINDER_3 = 7;
	final static int FUNCTION_SFERIC_1 = 8;
	final static int FUNCTION_SFERIC_2 = 9;
	final static int FUNCTION_SFERIC_3 = 10;
	final static int REFLECTOR_ON = 1;
	final static int REFLECTOR_OFF = 2;
	final static int LIGHT_MODE_VERTEX = 1;
	final static int LIGHT_MODE_FRAGMENT_BLINN = 2;
	final static int LIGHT_MODE_FRAGMENT_PHONG = 3;
	final static int TEXTURE_BRICKS = 1;
	final static int TEXTURE_OLD_BRICKS = 2;
	final static int TEXTURE_GROUND = 3;
	final static int TEXTURE_SIDEWALK = 4;
	
	Vec3D lightPosition;
	//Vec3D lightPosition;
	Vec3D eyePosition;
	Vec3D spotDir;
	Texture tex;
	Texture texNormal;	
	OGLTexture2D texture, texture2, texture3, texture4;
	OGLTexture2D textureNormal, textureNormal2, textureNormal3, textureNormal4;
	OGLTexture2D textureHeight, textureHeight2, textureHeight3, textureHeight4;
	OGLTexture.Viewer viewer;
	

	OGLBuffers buffers;
	OGLTextRenderer textRenderer = new OGLTextRenderer();

	int shaderProgram, shaderProgramLightFrag;
	int locMat, locMatLF;
	int locMatView, locMatViewLF;
	int locMatProj;
	int loc_lightPosition, loc_lightPositionLF;
	int loc_eyePosition, loc_eyePositionLF;
	int loc_reflectorLF, loc_reflectorNormal,  loc_spotDir;
	int loc_lightMode, loc_lightVertexMode;
	int loc_function_normal;
	int loc_function_LF;

	Camera cam = new Camera();
	Mat4 proj; // created in reshape()
	
	float time = 0;

	public void init(GLAutoDrawable glDrawable) {
		
		
		GL2 gl = glDrawable.getGL().getGL2();
		
		OGLUtils.printOGLparameters(gl);
		OGLUtils.shaderCheck(gl);						
		
		textRenderer = new OGLTextRenderer();		
		viewer = new OGLTexture2D.Viewer(gl);
		
		shaderProgram = ShaderUtils.loadProgram(gl, "/cviceni/start");
		shaderProgramLightFrag = ShaderUtils.loadProgram(gl, "/cviceni/startLF");		
		
		createBuffers(gl);	
		
		//Shader - Per Pixel LIGHTING
		locMatView = gl.glGetUniformLocation(shaderProgram, "viewMat");
		locMatProj = gl.glGetUniformLocation(shaderProgram, "projMat");
		locMat = gl.glGetUniformLocation(shaderProgram, "mat");
		loc_lightPosition = gl.glGetUniformLocation(shaderProgram, "lightPosition");
		loc_eyePosition = gl.glGetUniformLocation(shaderProgram, "eyePosition");
		loc_function_normal = gl.glGetUniformLocation(shaderProgram, "functionType");
		loc_lightMode = gl.glGetUniformLocation(shaderProgram, "lightMode");
		loc_spotDir = gl.glGetUniformLocation(shaderProgram, "spotDir");
		loc_reflectorNormal = gl.glGetUniformLocation(shaderProgram, "reflectorMode");
		
		//Shader - Per Vertex & Per Pixel LIGHTING
		locMatLF = gl.glGetUniformLocation(shaderProgramLightFrag, "mat");
		locMatViewLF = gl.glGetUniformLocation(shaderProgramLightFrag, "viewMat");
		loc_lightPositionLF = gl.glGetUniformLocation(shaderProgramLightFrag, "lightPosition");
		loc_eyePositionLF = gl.glGetUniformLocation(shaderProgramLightFrag, "eyePosition");
		loc_reflectorLF = gl.glGetUniformLocation(shaderProgramLightFrag, "reflectorMode");
		loc_lightVertexMode = gl.glGetUniformLocation(shaderProgramLightFrag, "vertexMode");
		loc_function_LF = gl.glGetUniformLocation(shaderProgramLightFrag, "functionType");		
		
		switchShaderType = NORMAL_TYPE;
		switchPolygonMode = POLYGON_MODE_FILL;
		switchLightMode = LIGHT_MODE_POSITION;
		switchFunctionType = FUNCTION_GRID;
		switchReflectorMode = REFLECTOR_OFF;
		switchVertexMode = LIGHT_MODE_VERTEX;
		switchTexture = TEXTURE_OLD_BRICKS;

		lightPosition = new Vec3D(20.0, 20.0, 10.0);
		eyePosition = new Vec3D(20.0, 20.0, 10.0);
		
		cam = cam.withPosition(eyePosition)
				.withAzimuth(Math.PI * 1.25)
				.withZenith(Math.PI * -0.125);
		gl.glEnable(GL2.GL_DEPTH_TEST);			
        
		// Texturovani
        texture = new OGLTexture2D(gl, "/textures/bricks.jpg");
        textureNormal = new OGLTexture2D(gl, "/textures/bricksn.png");
		textureHeight = new OGLTexture2D(gl, "/textures/bricksh.png");
		
        gl.glTexParameteri(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_WRAP_S, GL2.GL_REPEAT);
        gl.glTexParameteri(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_WRAP_T, GL2.GL_REPEAT);        
        
        texture2 = new OGLTexture2D(gl, "/textures/Brick_OldDestroyed_1k_d.tga");
		textureNormal2 = new OGLTexture2D(gl, "/textures/Brick_OldDestroyed_1k_n.tga");
		textureHeight2 = new OGLTexture2D(gl, "/textures/Brick_OldDestroyed_1k_h.tga");
		
		texture3 = new OGLTexture2D(gl, "/textures/Ground_Dirt_1k_d.tga");
		textureNormal3 = new OGLTexture2D(gl, "/textures/Ground_Dirt_1k_n.tga");
		textureHeight3 = new OGLTexture2D(gl, "/textures/Ground_Dirt_1k_h.tga");
        
		texture4 = new OGLTexture2D(gl, "/textures/Concrete_sidewalk_1k_d.tga");
		textureNormal4 = new OGLTexture2D(gl, "/textures/Concrete_sidewalk_1k_n.tga");
		textureHeight4 = new OGLTexture2D(gl, "/textures/Concrete_sidewalk_1k_h.tga");
		
        gl.glTexParameteri(GL2.GL_TEXTURE_2D,GL2.GL_TEXTURE_MAG_FILTER,GL2.GL_REPEAT);
        gl.glTexParameteri(GL2.GL_TEXTURE_2D,GL2.GL_TEXTURE_MIN_FILTER,GL2.GL_REPEAT);
		
	}

	void createBuffers(GL2 gl) {
		buffers = GridFactory_stripV2.createOGLBuffers(gl, 101, 101);		
	}

	
	public void display(GLAutoDrawable glDrawable) {
		GL2 gl = glDrawable.getGL().getGL2();
		
		gl.glClearColor(0.1f, 0.1f, 0.1f, 1.0f);
		gl.glClear(GL2.GL_COLOR_BUFFER_BIT | GL2.GL_DEPTH_BUFFER_BIT);								
		
		if(switchPolygonMode == POLYGON_MODE_FILL) gl.glPolygonMode(GL2.GL_FRONT_AND_BACK, GL2.GL_FILL);			
		else gl.glPolygonMode(GL2.GL_FRONT_AND_BACK, GL2.GL_LINE);							
		
		lightPosition = new Vec3D(20.0, 20.0, 10.0);		
		
		switch (switchShaderType) {
			case NORMAL_TYPE:				
				gl.glUseProgram(shaderProgram);				
				switch(switchTexture){
					case TEXTURE_BRICKS:
						texture.bind(shaderProgram, "tex", 2);			
						textureNormal.bind(shaderProgram, "texNormal", 1);
						textureHeight.bind(shaderProgram, "texHeight", 0);
						break;
					case TEXTURE_OLD_BRICKS:
						texture2.bind(shaderProgram, "tex", 2);			
						textureNormal2.bind(shaderProgram, "texNormal", 1);
						textureHeight2.bind(shaderProgram, "texHeight", 0);
						break;
					case TEXTURE_GROUND:
						texture3.bind(shaderProgram, "tex", 2);			
						textureNormal3.bind(shaderProgram, "texNormal", 1);
						textureHeight3.bind(shaderProgram, "texHeight", 0);
						break;
					case TEXTURE_SIDEWALK:
						texture4.bind(shaderProgram, "tex", 2);			
						textureNormal4.bind(shaderProgram, "texNormal", 1);
						textureHeight4.bind(shaderProgram, "texHeight", 0);
						break;						
				}				
	
				if(switchReflectorMode == REFLECTOR_ON){					
					lightPosition = cam.getPosition();
				} else {
					lightPosition = new Vec3D(20.0, 20.0, 10.0);
				}				
				gl.glUniformMatrix4fv(locMatProj, 1, false,	ToFloatArray.convert(proj), 0);
				gl.glUniformMatrix4fv(locMatView, 1, false,	ToFloatArray.convert(cam.getViewMatrix()), 0);
				gl.glUniformMatrix4fv(locMat, 1, false,	ToFloatArray.convert(cam.getViewMatrix().mul(proj)), 0);
				gl.glUniform3f(loc_lightPosition, (float)lightPosition.getX(), (float)lightPosition.getY(), (float)lightPosition.getZ());						
				gl.glUniform1i(loc_lightMode, switchLightMode);
				gl.glUniform1i(loc_reflectorNormal, switchReflectorMode);
				gl.glUniform1i(loc_function_normal, switchFunctionType);			
				buffers.draw(GL2.GL_TRIANGLE_STRIP, shaderProgram);		
				normalShaderDrawString(glDrawable); 							
				break;
			case NORMAL_TYPE_FRAG_LIGHT:
				if(switchReflectorMode == REFLECTOR_ON){
					lightPosition = cam.getPosition();
				} else {
					lightPosition = new Vec3D(20.0, 20.0, 10.0);
				}				
				gl.glUseProgram(shaderProgramLightFrag);
				gl.glUniformMatrix4fv(locMatLF, 1, false, ToFloatArray.convert(cam.getViewMatrix().mul(proj)), 0);
				gl.glUniformMatrix4fv(locMatViewLF, 1, false,	ToFloatArray.convert(cam.getViewMatrix()), 0);
				gl.glUniform3f(loc_lightPositionLF, (float)lightPosition.getX(), (float)lightPosition.getY(), (float)lightPosition.getZ());
				gl.glUniform1i(loc_reflectorLF, switchReflectorMode);
				gl.glUniform1i(loc_lightVertexMode, switchVertexMode);
				gl.glUniform1i(loc_function_LF, switchFunctionType);
				buffers.draw(GL2.GL_TRIANGLE_STRIP, shaderProgramLightFrag);							
				lightingShaderDrawString(glDrawable);				
				break;
			
			default:
				break;
			}	
		
		if(switchPolygonMode == POLYGON_MODE_FILL){			
			textRenderer.drawStr2D(glDrawable, 3, height - 50, "P - Polygon mode: Fill");
		} else {
			textRenderer.drawStr2D(glDrawable, 3, height - 50, "P - Polygon mode: Line");
		}
			
		//viewer.view(texture, -1, -1, 0.5);
		textRenderer.drawStr2D(glDrawable, 3, 3, "WASD + mouse - Controls ");
		textRenderer.drawStr2D(glDrawable, width - 90, 3, " (c) PGRF UHK");

	}

	public void reshape(GLAutoDrawable drawable, int x, int y, int width,
			int height) {
		this.width = width;
		this.height = height;
		proj = new Mat4PerspRH(Math.PI / 4, height / (double) width, 0.1, 1000.0);
	}

	public void displayChanged(GLAutoDrawable drawable, boolean modeChanged,
			boolean deviceChanged) {
	}

	public void mouseClicked(MouseEvent e) {
	}

	public void mouseEntered(MouseEvent e) {
	}

	public void mouseExited(MouseEvent e) {
	}

	public void mousePressed(MouseEvent e) {
		ox = e.getX();
		oy = e.getY();
	}

	public void mouseReleased(MouseEvent e) {
	}

	public void mouseDragged(MouseEvent e) {
		cam = cam.addAzimuth((double) Math.PI * (ox - e.getX()) / width)
				.addZenith((double) Math.PI * (e.getY() - oy) / width);
		ox = e.getX();
		oy = e.getY();
	}

	public void mouseMoved(MouseEvent e) {
	}

	public void keyPressed(KeyEvent e) {
		switch (e.getKeyCode()) {
		case KeyEvent.VK_W:
			cam = cam.forward(0.5);
			break;
		case KeyEvent.VK_D:
			cam = cam.right(0.5);
			break;
		case KeyEvent.VK_S:
			cam = cam.backward(0.5);
			break;
		case KeyEvent.VK_A:
			cam = cam.left(0.5);
			break;
		case KeyEvent.VK_CONTROL:
			cam = cam.down(1);
			break;
		case KeyEvent.VK_SHIFT:
			cam = cam.up(1);
			break;
		case KeyEvent.VK_SPACE:
			cam = cam.withFirstPerson(!cam.getFirstPerson());
			break;
		case KeyEvent.VK_G:
			cam = cam.mulRadius(0.9f);
			break;
		case KeyEvent.VK_H:
			cam = cam.mulRadius(1.1f);
			break;
		case KeyEvent.VK_T:
			//switchShaderType++;
			if(switchShaderType==2) {
				switchShaderType = 1;
			} else {
				switchShaderType = 2;
				switchFunctionType=1;				
			}
			//System.out.println(switchShaderType);
			break;
		case KeyEvent.VK_P:
			switchPolygonMode++;
			if(switchPolygonMode>2) switchPolygonMode = 1;
			//System.out.println(switchShaderType);
			break;
		case KeyEvent.VK_L:
			switchLightMode++;
			if(switchLightMode>8) switchLightMode = 1;
			//System.out.println(switchShaderType);
			break;
		case KeyEvent.VK_K:
			switchLightMode--;
			if(switchLightMode<1) switchLightMode = 8;
			//System.out.println(switchShaderType);
			break;
		case KeyEvent.VK_R:
			switchReflectorMode++;
			if(switchReflectorMode>2) switchReflectorMode = 1;
			//System.out.println(switchShaderType);
			break;			
		case KeyEvent.VK_F:
			switchVertexMode++;			
			if(switchShaderType == NORMAL_TYPE_FRAG_LIGHT){
				if(switchVertexMode>3) {
					switchVertexMode = 1;					
				}
			} else {
				if(switchVertexMode>5) switchVertexMode = 1;
			}
			System.out.println(switchVertexMode);						
			//System.out.println(switchShaderType);
			break;				
		case KeyEvent.VK_U:
			if(switchShaderType == NORMAL_TYPE){
				if(switchFunctionType > 9) switchFunctionType=1;
				else switchFunctionType++;			
			} else {
				if(switchFunctionType > 1) switchFunctionType=1;
				else switchFunctionType++;			
			}
			
			break;
		case KeyEvent.VK_I:
			switchTexture++;
			if(switchTexture>4) switchTexture = 1;
			break;	
		/*		
		case KeyEvent.VK_M:
			x = lightPosition.getX();
			x-=0.1;
			y = lightPosition.getY();
			y-=0.1;
			lightPosition = new Vec3D(x, y, lightPosition.getZ());			
			break;
		case KeyEvent.VK_N:
			x = lightPosition.getX();
			x+=0.1;
			y = lightPosition.getY();
			y+=0.1;
			lightPosition = new Vec3D(x, y, lightPosition.getZ());			
			break;
		case KeyEvent.VK_V:
			x = lightPosition.getX();
			x-=0.1;
			y = lightPosition.getY();
			y-=0.1;
			z = lightPosition.getZ();
			z-=0.1;
			lightPosition = new Vec3D(x, y, z);			
			break;
		case KeyEvent.VK_C:
			x = lightPosition.getX();
			x+=0.1;
			y = lightPosition.getY();
			y+=0.1;
			z = lightPosition.getZ();
			z+=0.1;
			lightPosition = new Vec3D(x, y, z);			
			break;
		*/
		}
	}

	public void keyReleased(KeyEvent e) {
	}

	public void keyTyped(KeyEvent e) {
	}

	public void dispose(GLAutoDrawable glDrawable) {
		GL2 gl = glDrawable.getGL().getGL2();
		gl.glDeleteProgram(shaderProgram);
	}
	
	private void normalShaderDrawString(GLAutoDrawable glDrawable){
		textRenderer.drawStr2D(glDrawable, 3, height - 20, "T - Shader: Per Pixel");
		switch(switchFunctionType){
			case FUNCTION_CARTESIAN_1:
				textRenderer.drawStr2D(glDrawable, 3, height - 35, "U - Model:  Cartesian - cos(sqrt(x*x + y*y))");
				break;
			case FUNCTION_CARTESIAN_2:
				textRenderer.drawStr2D(glDrawable, 3, height - 35, "U - Model:  Cartesian - 'Navy Sail' - own");
				break;
			case FUNCTION_CARTESIAN_3:
				textRenderer.drawStr2D(glDrawable, 3, height - 35, "U - Model:  Cartesian - 'Function 3' - own");
				break;
			case FUNCTION_GRID:
				textRenderer.drawStr2D(glDrawable, 3, height - 35, "U - Model:  GRID");
				break;
			case FUNCTION_CYLINDER_1:
				textRenderer.drawStr2D(glDrawable, 3, height - 35, "U - Model:  Cylinder - Sombrero");
				break;
			case FUNCTION_CYLINDER_2:
				textRenderer.drawStr2D(glDrawable, 3, height - 35, "U - Model:  Cylinder - Tent'");
				break;
			case FUNCTION_CYLINDER_3:
				textRenderer.drawStr2D(glDrawable, 3, height - 35, "U - Model:  Cylinder - 'Function 3' - own");
				break;
			case FUNCTION_SFERIC_1:
				textRenderer.drawStr2D(glDrawable, 3, height - 35, "U - Model:  Sferic - Sphere");
				break;
			case FUNCTION_SFERIC_2:
				textRenderer.drawStr2D(glDrawable, 3, height - 35, "U - Model:  Sferic - Elephant head'");
				break;
			case FUNCTION_SFERIC_3:
				textRenderer.drawStr2D(glDrawable, 3, height - 35, "U - Model:  Sferic - 'Function 3' - own");
				break;
		}
		
		switch(switchLightMode){
			case LIGHT_MODE_POSITION:
				textRenderer.drawStr2D(glDrawable, 3, height - 65, "K, L - Light mode:  Position");
				break;
			case LIGHT_MODE_COLOR:
				textRenderer.drawStr2D(glDrawable, 3, height - 65, "K, L - Light mode:  Color");
				break;
			case LIGHT_MODE_NORMAL:
				textRenderer.drawStr2D(glDrawable, 3, height - 65, "K, L - Light mode:  Normals vector");
				break;
			case LIGHT_MODE_DIFFUSION:
				textRenderer.drawStr2D(glDrawable, 3, height - 65, "K, L - Light mode:  Diffusion");
				textRenderer.drawStr2D(glDrawable, 3, height - 80, "      Attenuation:  No");
				break;
			case LIGHT_MODE_BLINN_PHONG_HALF_VECTOR:
				textRenderer.drawStr2D(glDrawable, 3, height - 65, "K, L - Light mode:  Blinn-Phong - Half vector");
				drawStringReflector(glDrawable);
				break;				
			case LIGHT_MODE_BLINN_PHONG_REFLECT_VECTOR:
				textRenderer.drawStr2D(glDrawable, 3, height - 65, "K, L - Light mode:  Blinn-Phong - Reflect vector");
				drawStringReflector(glDrawable);
				break;
			case LIGHT_MODE_TEXTURE_NORMAL:
				textRenderer.drawStr2D(glDrawable, 3, height - 65, "K, L - Light mode:  Normal mapping");
				textRenderer.drawStr2D(glDrawable, 3, height - 110, "I - Change texture");
				drawStringReflector(glDrawable);
				break;
			case LIGHT_MODE_TEXTURE_PARALLAX:
				textRenderer.drawStr2D(glDrawable, 3, height - 65, "K, L - Light mode:  Parallax mapping");
				textRenderer.drawStr2D(glDrawable, 3, height - 110, "I - Change texture");
				drawStringReflector(glDrawable);
				break;
		}
		
		
	}
	
	private void drawStringReflector(GLAutoDrawable glDrawable){
		if(switchReflectorMode == REFLECTOR_ON)	textRenderer.drawStr2D(glDrawable, 3, height - 80, "R - Reflector:  On");
		else 									textRenderer.drawStr2D(glDrawable, 3, height - 80, "R - Reflector:  Off");
		textRenderer.drawStr2D(glDrawable, 3, height - 95, "      Attenuation:  Yes");
		
	}
	
	private void lightingShaderDrawString(GLAutoDrawable glDrawable){
		textRenderer.drawStr2D(glDrawable, 3, height - 20, "T - Shader: Per Vertex & Per Pixel LIGHTING");
		
		switch(switchFunctionType){
		case 1:
			textRenderer.drawStr2D(glDrawable, 3, height - 35, "U - Model:  Cartesian - cos(sqrt(x*x + y*y))");
			break;
		case 2:
			textRenderer.drawStr2D(glDrawable, 3, height - 35, "U - Model:  Sferic - Sphere");
			break;
		}
		
		switch(switchVertexMode){
			case LIGHT_MODE_VERTEX:
				textRenderer.drawStr2D(glDrawable, 3, height - 65, "F - Light:  Vertex - Blinn-Phong - Half vector");
				break;
			case LIGHT_MODE_FRAGMENT_BLINN:
				textRenderer.drawStr2D(glDrawable, 3, height - 65, "F - Light:  Fragment - Blinn-Phong - Reflect vector");
				break;
			case LIGHT_MODE_FRAGMENT_PHONG:
				textRenderer.drawStr2D(glDrawable, 3, height - 65, "F - Light:  Fragment - Blinn-Phong - Half vector");
				break;
		
		}
		if(switchReflectorMode == REFLECTOR_ON)	textRenderer.drawStr2D(glDrawable, 3, height - 80, "R - Reflector:  On");
		else 									textRenderer.drawStr2D(glDrawable, 3, height - 80, "R - Reflector:  Off");
		textRenderer.drawStr2D(glDrawable, 3, height - 95, "      Attenuation:  Yes");
	}

}