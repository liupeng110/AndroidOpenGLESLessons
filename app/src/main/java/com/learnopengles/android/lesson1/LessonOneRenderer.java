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
import android.util.Log;


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

	private String tag ="LessonOneRenderer";


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
				eyeX, eyeY, eyeZ,    //相机位置
				lookX, lookY, lookZ, //观察点位置，与gl中的VPN是不一样的
				upX, upY, upZ       //顶部朝向
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
			GLES20.glShaderSource(vertexShaderHandle, vertexShader);//传递着色器源
			GLES20.glCompileShader(vertexShaderHandle);             //编译着色器
			final int[] compileStatus = new int[1];
			GLES20.glGetShaderiv(vertexShaderHandle, GLES20.GL_COMPILE_STATUS, compileStatus, 0);//获取编译状态

			if (compileStatus[0] == 0) //如果编译失败,删除着色器
			{				
				GLES20.glDeleteShader(vertexShaderHandle);
				vertexShaderHandle = 0;
			}
		}

		if (vertexShaderHandle == 0) //创建定点着色器失败
		{
			throw new RuntimeException("Error creating vertex shader.");
		}

		int fragmentShaderHandle = GLES20.glCreateShader(GLES20.GL_FRAGMENT_SHADER);//在片段着色器中加载

		if (fragmentShaderHandle != 0) 
		{
			GLES20.glShaderSource(fragmentShaderHandle, fragmentShader);//传递着色器源
			GLES20.glCompileShader(fragmentShaderHandle);
			final int[] compileStatus = new int[1];
			GLES20.glGetShaderiv(fragmentShaderHandle, GLES20.GL_COMPILE_STATUS, compileStatus, 0);
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

		int programHandle = GLES20.glCreateProgram();//创建一个程序对象并把handle保存到它
		
		if (programHandle != 0) 
		{
			GLES20.glAttachShader(programHandle, vertexShaderHandle);  //将定点着色器绑定到程序
			GLES20.glAttachShader(programHandle, fragmentShaderHandle);//将片段着色器绑定到程序
			GLES20.glBindAttribLocation(programHandle, 0, "a_Position");//绑定属性
			GLES20.glBindAttribLocation(programHandle, 1, "a_Color");
			GLES20.glLinkProgram(programHandle);                       //将两个着色器链接到一个程序中

			final int[] linkStatus = new int[1];
			GLES20.glGetProgramiv(programHandle, GLES20.GL_LINK_STATUS, linkStatus, 0);//获取链接状态


			if (linkStatus[0] == 0) //链接失败进行删除
			{				
				GLES20.glDeleteProgram(programHandle);
				programHandle = 0;
			}
		}
		
		if (programHandle == 0)
		{
			throw new RuntimeException("Error creating program.");
		}

		//设置程序句柄,值将被传递到程序
        mMVPMatrixHandle = GLES20.glGetUniformLocation(programHandle, "u_MVPMatrix");        
        mPositionHandle = GLES20.glGetAttribLocation(programHandle, "a_Position");
        mColorHandle = GLES20.glGetAttribLocation(programHandle, "a_Color");        

        GLES20.glUseProgram(programHandle);                       //告诉opengl渲染时候使用这些数据
	}	
	
	@Override
	public void onSurfaceChanged(GL10 glUnused, int width, int height) 
	{
		GLES20.glViewport(0, 0, width, height);    //将opengl视口设置为跟surface同样大小
		final float ratio = (float) width / height;//创建一个新的透明投影矩阵,高度保持不变 宽度随纵横比而变
		final float left = -ratio;
		final float right = ratio;
		final float bottom = -1.0f;
		final float top = 1.0f;
		final float near = 1.0f;
		final float far = 10.0f;
		Matrix.frustumM(mProjectionMatrix, 0, left, right, bottom, top, near, far);//视椎体
	}	


	@Override
	public void onDrawFrame(GL10 glUnused) 
	{
		GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);	//清屏
		Log.i(tag,"进入onDrawFrame()");

		// 每10秒做一次完成的旋转
        long time = SystemClock.uptimeMillis() % 2000L;
		float angleInDegrees = (360.0f / 2000.0f) * ((int) time);
        
        //绘制第一个三角形
        Matrix.setIdentityM(mModelMatrix, 0);
		Matrix.translateM(mModelMatrix, 0, 0.0f, 1.0f, 0.0f);              //矩阵平移,偏移量  x,y,z(平移量)//矩阵归位
        Matrix.rotateM(mModelMatrix, 0, angleInDegrees, 0.0f, 0.0f, 1.0f); //矩阵旋转
        drawTriangle(mTriangle1Vertices);                                  //绘制第一个三角形


		//绘制第二个三角形
//        Matrix.setIdentityM(mModelMatrix, 0);                             //矩阵归位
//        Matrix.translateM(mModelMatrix, 0, 0.0f, -1.0f, 0.0f);            //矩阵平移,偏移量  x,y,z(平移量)
//        Matrix.rotateM(mModelMatrix, 0, 90.0f, 1.0f, 0.0f, 0.0f);
//        Matrix.rotateM(mModelMatrix, 0, angleInDegrees, 0.0f, 0.0f, 1.0f);//angle旋转角度    x, y, z(需要旋转的轴)
//        drawTriangle(mTriangle2Vertices);


//		//绘制第三个三角形  灰色
//        Matrix.setIdentityM(mModelMatrix, 0);
//        Matrix.translateM(mModelMatrix, 0, 0.0f, 0.5f, 0.0f);
//        Matrix.rotateM(mModelMatrix, 0, 90.0f, 1.0f, -1.0f, 0.0f);
//        Matrix.rotateM(mModelMatrix, 0, angleInDegrees, 0.0f, 0.0f, 1.0f);
//        drawTriangle(mTriangle3Vertices);
	}	


	//从给定的定点数据,画一个三角形
	private void drawTriangle(final FloatBuffer aTriangleBuffer)
	{
		aTriangleBuffer.position(mPositionOffset);//传递位置信息
        GLES20.glVertexAttribPointer(mPositionHandle, mPositionDataSize, GLES20.GL_FLOAT, false,mStrideBytes, aTriangleBuffer);
        GLES20.glEnableVertexAttribArray(mPositionHandle);        
        

        aTriangleBuffer.position(mColorOffset); //传递颜色信息
        GLES20.glVertexAttribPointer(mColorHandle, mColorDataSize, GLES20.GL_FLOAT, false, mStrideBytes, aTriangleBuffer);
        GLES20.glEnableVertexAttribArray(mColorHandle);
        

        Matrix.multiplyMM(mMVPMatrix, 0, mViewMatrix, 0, mModelMatrix, 0);    //视图矩阵乘以模型矩阵 结果存在mMVPMatrix中
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mMVPMatrix, 0);//然后再乘以投影矩阵结果存放在mMVPMatrix中

        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mMVPMatrix, 0);//默认将数组以列向量的形式存放在矩阵中
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 3);                               
	}
}
