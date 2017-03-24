package com.learnopengles.android.lesson1;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.os.SystemClock;


public class LessonOneRenderer implements GLSurfaceView.Renderer  //需要实现自定义渲染器
{

	private float[] mModelMatrix = new float[16];     //存储模型矩阵
	private float[] mViewMatrix = new float[16];      //存储view矩阵
	private float[] mProjectionMatrix = new float[16];//存储投影矩阵(将场景投射到二维视图)
	private float[] mMVPMatrix = new float[16];       //最终组合矩阵(将被传递到着色器程序)
	

	private final FloatBuffer mTriangle1Vertices;     //模型数据存储在浮动缓冲区
	private final FloatBuffer mTriangle2Vertices;
	private final FloatBuffer mTriangle3Vertices;


	private int mMVPMatrixHandle; //传递矩阵中的变化
	private int mPositionHandle;  //传递模型位置信息
	private int mColorHandle;     //传递模型颜色信息

	private final int mBytesPerFloat = 4;               //每个浮点多少字节
	private final int mStrideBytes = 7 * mBytesPerFloat;//每个定点多少元素
	private final int mPositionOffset = 0;              //偏移的位置数据
	private final int mPositionDataSize = 3;            //元素中位置数据的大小
	private final int mColorDataSize = 4;               //元素中颜色数据的大小
	private final int mColorOffset = 3;                 //颜色数据的偏移量


	public LessonOneRenderer() { 	//初始化模型数据(等边三角形的定义点)

		final float[] triangle1VerticesData = {//第一个三角形 红绿蓝

	            -0.5f, -0.25f, 0.0f,    // X, Y, Z,     //左上角数据
	            1.0f, 0.0f, 0.0f, 1.0f,	// R, G, B, A

	            0.5f, -0.25f, 0.0f,                     //右上角数据
	            0.0f, 0.0f, 1.0f, 1.0f,

	            0.0f, 0.559016994f, 0.0f,               //左下角数据
	            0.0f, 1.0f, 0.0f, 1.0f};

		final float[] triangle2VerticesData = {//第二个三角形 黄 青 洋红

	            -0.5f, -0.25f, 0.0f,   // X, Y, Z,
	            1.0f, 1.0f, 0.0f, 1.0f,// R, G, B, A
	            
	            0.5f, -0.25f, 0.0f, 
	            0.0f, 1.0f, 1.0f, 1.0f,
	            
	            0.0f, 0.559016994f, 0.0f, 
	            1.0f, 0.0f, 1.0f, 1.0f};
		

		final float[] triangle3VerticesData = {//第二个三角形 白 灰 黑
	            -0.5f, -0.25f, 0.0f,   // X, Y, Z,
	            1.0f, 1.0f, 1.0f, 1.0f,// R, G, B, A
	            
	            0.5f, -0.25f, 0.0f, 
	            0.5f, 0.5f, 0.5f, 1.0f,
	            
	            0.0f, 0.559016994f, 0.0f, 
	            0.0f, 0.0f, 0.0f, 1.0f};
		
		//初始化刚定义的数据到缓冲区
		mTriangle1Vertices = ByteBuffer.allocateDirect(triangle1VerticesData.length * mBytesPerFloat).order(ByteOrder.nativeOrder()).asFloatBuffer();
		mTriangle2Vertices = ByteBuffer.allocateDirect(triangle2VerticesData.length * mBytesPerFloat).order(ByteOrder.nativeOrder()).asFloatBuffer();
		mTriangle3Vertices = ByteBuffer.allocateDirect(triangle3VerticesData.length * mBytesPerFloat).order(ByteOrder.nativeOrder()).asFloatBuffer();
					
		mTriangle1Vertices.put(triangle1VerticesData).position(0);
		mTriangle2Vertices.put(triangle2VerticesData).position(0);
		mTriangle3Vertices.put(triangle3VerticesData).position(0);
	}
	
	@Override
	public void onSurfaceCreated(GL10 glUnused, EGLConfig config) 
	{

		GLES20.glClearColor(0.5f, 0.5f, 0.5f, 0.5f);//设置背景颜色为灰色
	

		final float eyeX = 0.0f;//初始化眼睛到原点后面
		final float eyeY = 0.0f;
		final float eyeZ = 1.5f;


		final float lookX = 0.0f; //眼睛到物体的距离
		final float lookY = 0.0f;
		final float lookZ = -5.0f;


		final float upX = 0.0f;//设置头位置在摄像机处
		final float upY = 1.0f;
		final float upZ = 0.0f;

		//设置视图矩阵,表示相机位置
		Matrix.setLookAtM( mViewMatrix, 0,
				eyeX, eyeY, eyeZ,    //相机坐标
				lookX, lookY, lookZ, //目标坐标，与gl中的VPN是不一样的
				upX, upY, upZ       //相机正上方向量VUV
		);

		//定点着色器
		final String vertexShader =
		    "uniform mat4 u_MVPMatrix;      \n"	   // 常数表示组合模型/视图/投影矩阵。
		  + "attribute vec4 a_Position;     \n"		//每个顶点位置信息
		  + "attribute vec4 a_Color;        \n"		//每个顶点颜色信息
		  + "varying vec4 v_Color;          \n"		// 传递到片段着色器

		  + "void main()                    \n"		//顶点着色器的入口点
		  + "{                              \n"
		  + "   v_Color = a_Color;          \n"		// 将颜色传递到片段着色器,插入三角形内
		  + "   gl_Position = u_MVPMatrix   \n" 	// gl_Position存储最终位置的特殊变量
		  + "               * a_Position;   \n"     //通过矩阵相乘得到屏幕上最终的坐标点
		  + "}                              \n";

		//片段着色器
		final String fragmentShader =
			"precision mediump float;       \n"		// 设置默认精度为中等

		  + "varying vec4 v_Color;          \n"		// 顶点着色器在每个片段的三角形上插值的颜色

		  + "void main()                    \n"		//我们的片段着色器入口点
		  + "{                              \n"
		  + "   gl_FragColor = v_Color;     \n"		//通过管道直接传递颜色
		  + "}                              \n";												
		
		// 顶点着色器中加载   
		int vertexShaderHandle = GLES20.glCreateShader(GLES20.GL_VERTEX_SHADER);

		if (vertexShaderHandle != 0) 
		{
			// Pass in the shader source.
			GLES20.glShaderSource(vertexShaderHandle, vertexShader);

			// Compile the shader.
			GLES20.glCompileShader(vertexShaderHandle);

			// Get the compilation status.
			final int[] compileStatus = new int[1];
			GLES20.glGetShaderiv(vertexShaderHandle, GLES20.GL_COMPILE_STATUS, compileStatus, 0);

			// If the compilation failed, delete the shader.
			if (compileStatus[0] == 0) 
			{				
				GLES20.glDeleteShader(vertexShaderHandle);
				vertexShaderHandle = 0;
			}
		}

		if (vertexShaderHandle == 0)
		{
			throw new RuntimeException("Error creating vertex shader.");
		}
		
		// Load in the fragment shader shader.
		int fragmentShaderHandle = GLES20.glCreateShader(GLES20.GL_FRAGMENT_SHADER);

		if (fragmentShaderHandle != 0) 
		{
			// Pass in the shader source.
			GLES20.glShaderSource(fragmentShaderHandle, fragmentShader);

			// Compile the shader.
			GLES20.glCompileShader(fragmentShaderHandle);

			// Get the compilation status.
			final int[] compileStatus = new int[1];
			GLES20.glGetShaderiv(fragmentShaderHandle, GLES20.GL_COMPILE_STATUS, compileStatus, 0);

			// If the compilation failed, delete the shader.
			if (compileStatus[0] == 0) 
			{				
				GLES20.glDeleteShader(fragmentShaderHandle);
				fragmentShaderHandle = 0;
			}
		}

		if (fragmentShaderHandle == 0)
		{
			throw new RuntimeException("Error creating fragment shader.");
		}
		
		// Create a program object and store the handle to it.
		int programHandle = GLES20.glCreateProgram();
		
		if (programHandle != 0) 
		{
			// Bind the vertex shader to the program.
			GLES20.glAttachShader(programHandle, vertexShaderHandle);			

			// Bind the fragment shader to the program.
			GLES20.glAttachShader(programHandle, fragmentShaderHandle);
			
			// Bind attributes
			GLES20.glBindAttribLocation(programHandle, 0, "a_Position");
			GLES20.glBindAttribLocation(programHandle, 1, "a_Color");
			
			// Link the two shaders together into a program.
			GLES20.glLinkProgram(programHandle);

			// Get the link status.
			final int[] linkStatus = new int[1];
			GLES20.glGetProgramiv(programHandle, GLES20.GL_LINK_STATUS, linkStatus, 0);

			// If the link failed, delete the program.
			if (linkStatus[0] == 0) 
			{				
				GLES20.glDeleteProgram(programHandle);
				programHandle = 0;
			}
		}
		
		if (programHandle == 0)
		{
			throw new RuntimeException("Error creating program.");
		}
        
        // Set program handles. These will later be used to pass in values to the program.
        mMVPMatrixHandle = GLES20.glGetUniformLocation(programHandle, "u_MVPMatrix");        
        mPositionHandle = GLES20.glGetAttribLocation(programHandle, "a_Position");
        mColorHandle = GLES20.glGetAttribLocation(programHandle, "a_Color");        
        
        // Tell OpenGL to use this program when rendering.
        GLES20.glUseProgram(programHandle);        
	}	
	
	@Override
	public void onSurfaceChanged(GL10 glUnused, int width, int height) 
	{
		// Set the OpenGL viewport to the same size as the surface.
		GLES20.glViewport(0, 0, width, height);

		// Create a new perspective projection matrix. The height will stay the same
		// while the width will vary as per aspect ratio.
		final float ratio = (float) width / height;
		final float left = -ratio;
		final float right = ratio;
		final float bottom = -1.0f;
		final float top = 1.0f;
		final float near = 1.0f;
		final float far = 10.0f;
		
		Matrix.frustumM(mProjectionMatrix, 0, left, right, bottom, top, near, far);
	}	

	@Override
	public void onDrawFrame(GL10 glUnused) 
	{
		GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);			        
                
        // Do a complete rotation every 10 seconds.
        long time = SystemClock.uptimeMillis() % 10000L;
        float angleInDegrees = (360.0f / 10000.0f) * ((int) time);
        
        // Draw the triangle facing straight on.
        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.rotateM(mModelMatrix, 0, angleInDegrees, 0.0f, 0.0f, 1.0f);        
        drawTriangle(mTriangle1Vertices);
        
        // Draw one translated a bit down and rotated to be flat on the ground.
        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.translateM(mModelMatrix, 0, 0.0f, -1.0f, 0.0f);
        Matrix.rotateM(mModelMatrix, 0, 90.0f, 1.0f, 0.0f, 0.0f);
        Matrix.rotateM(mModelMatrix, 0, angleInDegrees, 0.0f, 0.0f, 1.0f);        
        drawTriangle(mTriangle2Vertices);
        
        // Draw one translated a bit to the right and rotated to be facing to the left.
        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.translateM(mModelMatrix, 0, 1.0f, 0.0f, 0.0f);
        Matrix.rotateM(mModelMatrix, 0, 90.0f, 0.0f, 1.0f, 0.0f);
        Matrix.rotateM(mModelMatrix, 0, angleInDegrees, 0.0f, 0.0f, 1.0f);
        drawTriangle(mTriangle3Vertices);
	}	
	
	/**
	 * Draws a triangle from the given vertex data.
	 * 
	 * @param aTriangleBuffer The buffer containing the vertex data.
	 */
	private void drawTriangle(final FloatBuffer aTriangleBuffer)
	{		
		// Pass in the position information
		aTriangleBuffer.position(mPositionOffset);
        GLES20.glVertexAttribPointer(mPositionHandle, mPositionDataSize, GLES20.GL_FLOAT, false,
        		mStrideBytes, aTriangleBuffer);        
                
        GLES20.glEnableVertexAttribArray(mPositionHandle);        
        
        // Pass in the color information
        aTriangleBuffer.position(mColorOffset);
        GLES20.glVertexAttribPointer(mColorHandle, mColorDataSize, GLES20.GL_FLOAT, false,
        		mStrideBytes, aTriangleBuffer);        
        
        GLES20.glEnableVertexAttribArray(mColorHandle);
        
		// This multiplies the view matrix by the model matrix, and stores the result in the MVP matrix
        // (which currently contains model * view).
        Matrix.multiplyMM(mMVPMatrix, 0, mViewMatrix, 0, mModelMatrix, 0);
        
        // This multiplies the modelview matrix by the projection matrix, and stores the result in the MVP matrix
        // (which now contains model * view * projection).
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mMVPMatrix, 0);

        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mMVPMatrix, 0);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 3);                               
	}
}
