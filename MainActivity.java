/**
 * 
 * @author Jacob Payne
 * @author Shannon Jones
 * 
 * @date Winter 2013
 * 
 * Porting CIS 367 projects 1 and 2 from OpenGL to OpenGLES for Android:
 * The Portal 2 Companion Cube traveling in an ellipse around
 * a cone while rotating
 * 
 */

package edu.gvsu.payne.proj3;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.opengl.GLSurfaceView;
import android.opengl.GLU;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.app.Activity;

public class MainActivity extends Activity {
	static boolean pause = false;

	private GLSurfaceView glView;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		/* remove the title bar at the top of screen */
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		/* remove the status bar, make it full screen */
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, 
				WindowManager.LayoutParams.FLAG_FULLSCREEN);

		glView = new GLSurfaceView(this);
		glView.setRenderer(new renderView());
		//setContentView(glView);
		setContentView(R.layout.activity_glview);

		glView.requestRender();

		/* we are going to replace the dummy view with our own GLView */
		View dummy = (View) findViewById(R.id.graphics_view);
		/* identify dummy's parent */
		ViewGroup top = (ViewGroup) dummy.getParent();

		/* copy over all layout parameters from dummy to your GLView */
		glView.setLayoutParams(dummy.getLayoutParams());

		/* replace by removing and adding */
		int idx = top.indexOfChild(dummy);
		top.removeViewAt(idx);
		top.addView (glView, idx);
	}

	public void onToggleClicked(View v) {
		if (pause) {
			pause = false;
		} else {
			pause = true;
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		glView.onPause();
	}

	@Override
	protected void onResume() {
		super.onResume();
		glView.onResume();
	}
}


/**
 * GLSurfaceView render implementation
 */
class renderView implements GLSurfaceView.Renderer {

	float[] cubeCF = {1,0,0,0,
			0,1,0,0,
			0,0,1,0,
			0,0,0,1};
	ByteBuffer buff;
	FloatBuffer faceVertexBuff;
	FloatBuffer faceColorBuff;
	ShortBuffer faceOrderBuff;

	float x_coord = 0;
	float y_coord = 0;
	int z_rot= 0;
	int animTime = 0;

	public renderView () {
		float[] faceVertexData = {-1f, 1f, 1f, // 0
				1f, 1f, 1f,   // 1
				1f, -1f, 1f,  // 2
				-1f, -1f, 1f,  // 3
				-1f, 1f, -1f, // 4
				1f, 1f, -1f,  // 5
				1f, -1f, -1f, // 6
				-1f, -1f, -1f /* 7 */};
		short[] faceOrderData = {0,2,1, // front t1
				3,2,0, // front t2
				4,7,3, // left t1
				0,4,3, // left t2
				1,2,5, // right t1
				5,2,6, // right t2
				4,0,5, // top t1
				5,0,1, // top t2
				5,6,4, // back t2
				4,6,7, // back t2
				2,3,7, // bottom t1
				2,7,6 /* bottom t2 */};
		float[] faceColorData = {
				1, 0, 0, 1, // 0
				1, 1, 0, 1, // 1
				0, 0, 1, 1, // 2
				0, 1, 1, 1, // 3
				0, 0, 1, 1, // 4
				1, 0, 1, 1, // 5
				0, 1, 0, 1, // 6
				1, 0, 1, 1  /* 7*/};
		faceVertexBuff = ByteBuffer.allocateDirect(faceVertexData.length*4).order(ByteOrder.nativeOrder()).asFloatBuffer();
		faceVertexBuff.put(faceVertexData);
		faceVertexBuff.position(0);
		faceColorBuff = ByteBuffer.allocateDirect(faceColorData.length*4).order(ByteOrder.nativeOrder()).asFloatBuffer();
		faceColorBuff.put(faceColorData);
		faceColorBuff.position(0);
		faceOrderBuff = ByteBuffer.allocateDirect(faceOrderData.length*4).order(ByteOrder.nativeOrder()).asShortBuffer();
		faceOrderBuff.put(faceOrderData);
		faceOrderBuff.position(0);
	}


	/**
	 * Display callback
	 */
	@Override
	public void onDrawFrame(GL10 gl) {
		/* Clear the buffer, clear the matrix */
		gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);

		gl.glLoadIdentity();

		gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
		//	     gl.glEnableClientState(GL10.GL_NORMAL_ARRAY);
		gl.glEnableClientState(GL10.GL_COLOR_ARRAY);

		gl.glVertexPointer(3, GL10.GL_FLOAT, 0, faceVertexBuff);
		gl.glColorPointer(4, GL10.GL_FLOAT, 0, faceColorBuff);
		//gl.glNormalPointer(GL10.GL_FLOAT, 0, faceOrderBuff);

		GLU.gluLookAt(gl, 0, -5, -8, 0, -4, 0, 0, 1, 0);
		gl.glPushMatrix();
		gl.glTranslatef(x_coord, y_coord, 0);
		gl.glRotatef(z_rot, 1, 0, 0);
		gl.glDrawElements(GL10.GL_TRIANGLES, 36, GL10.GL_UNSIGNED_SHORT, faceOrderBuff);
		gl.glPopMatrix();

		if (MainActivity.pause == false) {
			z_rot++;
			if (animTime>359) animTime=0;

			//for(j)
			float theta = (float) (animTime * (Math.PI/180.0f));

			x_coord = (float) (2-(Math.sin(theta)+1)*2);
			y_coord = (float) (.2-(Math.cos(theta)+2)*2);

			animTime++;
		}
	}


	/**
	 * Resized callback
	 */
	@Override
	public void onSurfaceChanged(GL10 gl, int width, int height) {
		gl.glViewport(0, 0, width, height);
		gl.glMatrixMode(GL10.GL_PROJECTION);
		gl.glLoadIdentity();
		GLU.gluPerspective(gl, 45f, (float) width / (float) height, 0.1f, 100f);
		gl.glMatrixMode(GL10.GL_MODELVIEW);
	}


	/**
	 * Location for init() code
	 */
	@Override
	public void onSurfaceCreated(GL10 gl, EGLConfig config) {
		gl.glClearColor(.5f, 1.0f, 0, 1.0f);
		gl.glShadeModel(GL10.GL_SMOOTH);
		gl.glCullFace(GL10.GL_BACK);
		gl.glEnable(GL10.GL_CULL_FACE);
		gl.glEnable(GL10.GL_DEPTH_TEST);
		
		
	}
}