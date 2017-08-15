package com.learnopengles.android.lesson1;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.Arrays;

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
	private final FloatBuffer mTriangle1Colors;       //模型数据存储在浮动缓冲区
//	private final FloatBuffer mTriangle2Vertices;
//	private final FloatBuffer mTriangle3Vertices;


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
		Log.i("opengl0","函数 renderer构造函数");

		final float[] triangle1VerticesData = {//第一个三角形 红绿蓝
	              0.5f, 0.25f, 0.0f,    // X, Y, Z,     //左上角数据
	             -0.5f, 0.25f, 0.0f,                     //右上角数据
				  0.5f, 0.1f, 0.0f,                      //左下角数据
				 -0.5f, 0.1f, 0.0f,
		};

		final float[] triangle1ColorData = {//颜色   rgb  a
				1.0f, 0.1f, 0.0f, 1.0f,     //红
				1.0f, 0.3f, 0.0f, 1.0f,
//				0.0f, 0.5f, 0.0f, 1.0f,
//				1.0f, 0.7f, 0.0f, 1.0f      //黄
		};
//		final float[] triangle1VerticesData = {//第一个三角形 红绿蓝
//				-1.0f,  1.0f, 0.0f,  // 0, Top Left
//				-1.0f, -1.0f, 0.0f,  // 1, Bottom Left
//				1.0f, -1.0f, 0.0f,   // 2, Bottom Right
////	           -1.0f,  1.0f, 0.0f,  //新增  这里闭合三角形
//				1.0f,  1.0f, 0.0f,   // 3, Top Right
////	           -1.0f,  1.0f, 0.0f,  //这里闭合了4边
////
//		};
		mTriangle1Vertices =
				ByteBuffer.allocateDirect(triangle1VerticesData.length * mBytesPerFloat)
				          .order(ByteOrder.nativeOrder())
						  .asFloatBuffer();
		mTriangle1Vertices.put(triangle1VerticesData)
				          .position(0);//三角形数据转为opengl用格式

		mTriangle1Colors =
				ByteBuffer.allocateDirect(triangle1ColorData.length * mBytesPerFloat)
						.order(ByteOrder.nativeOrder())
						.asFloatBuffer();
		mTriangle1Colors.put(triangle1ColorData)
				.position(0);//三角形数据转为opengl用格式
	}
	
	@Override public void onSurfaceCreated(GL10 glUnused, EGLConfig config)
	{
        Log.i("opengl0","函数 onSurfaceCreated");
		GLES20.glClearColor(0.5f, 0.5f, 0.5f, 0.5f);//设置背景颜色为灰色
		final float eyeX = 0.0f;                    //初始化眼睛到原点后面
		final float eyeY = 1.5f;
		final float eyeZ = 1.6f;


		final float lookX = 0.0f; //眼睛到物体的距离
		final float lookY = 0.0f;
		final float lookZ = 0.0f;

		final float upX = 0.0f;//设置头位置在摄像机处
		final float upY = 1.0f;
		final float upZ = 1.0f;

		//设置视图矩阵,表示相机位置
		Matrix.setLookAtM(
				mViewMatrix,         //存储生成矩阵元素的float[]类型数组
				0,                   //填充起始偏移量
				eyeX, eyeY, eyeZ,    //相机位置 xyz
				lookX, lookY, lookZ, //观察点位置xyz,与gl中的VPN是不一样的
				upX, upY, upZ        //up向量在xyz轴上的分量
		);


//		Matrix.orthoM(               //正交投影
//				mProjectionMatrix,   //存储生成矩阵元素的float[]类型数组
//				0,                   //填充起始偏移量
//				left,right,          //near面的left,right 近平面左右侧边对应的x坐标
//				bootom,top,          //near面的bootom,top 近平面上下侧边对应的y坐标
//				near,far             //near面 far面与视点的距离  视景体近平面,与远平面 距视点的距离
//		);



		// 自定义a_Position 位置 ,a_color传颜色
		// 定点着色器   只在编译时用到
		final String vertexShader =
		    "uniform mat4 u_MVPMatrix;      \n"	   //总变换矩阵  mat4  4*4的浮点矩阵  uniform修饰全局变量 一般表示变换矩阵，材质，光照参数，颜色
		  + "attribute vec4 a_Position;     \n"		//每个顶点位置信息 attribute 传递数据到顶点着色器（vertex shader）中
		  + "attribute vec4 a_Color;        \n"		//每个顶点颜色信息,surfacecreate中用到
		  + "varying   vec4 v_Color;        \n"		// 传递到片段着色器   varying顶点着色器的输出
		  //varying--顶点着色器计算每个顶点的值（颜色，纹理坐标等）写入varying变量，然后片元着色器使用该值


		  + "void main()                    \n"		//顶点着色器的入口点
		  + "{                              \n"
		  + "   v_Color = a_Color;          \n"		// 将颜色传递到片段着色器,插入三角形内
		  + "   gl_Position = u_MVPMatrix * a_Position;   \n"//gl_Position存储最终位置的特殊变量
		  + "}                              \n";    // 通过矩阵相乘得到屏幕上最终的坐标点

		//片段着色器
		final String fragmentShader =
			"precision mediump float;       \n"		// 设置默认精度为中等
		  + "varying vec4 v_Color;          \n"		// 顶点着色器在每个片段的三角形上插值的颜色

		  + "void main()                    \n"		//我们的片段着色器入口点
		  + "{                              \n"
		  + "   gl_FragColor = v_Color;     \n"		//通过管道直接传递颜色
		  + "}                              \n";												
		
		// 顶点着色器中加载   
		int vertexShaderHandle = GLES20.glCreateShader(GLES20.GL_VERTEX_SHADER);//创建一个容纳shader的容器

		if (vertexShaderHandle != 0) 
		{
			GLES20.glShaderSource(vertexShaderHandle, vertexShader);//传递着色器源
			GLES20.glCompileShader(vertexShaderHandle);             //编译着色器
			final int[] compileStatus = new int[1];                 //编译状态
			GLES20.glGetShaderiv(vertexShaderHandle, GLES20.GL_COMPILE_STATUS, compileStatus, 0);//获取编译状态
			if (compileStatus[0] == 0) //如果编译失败,删除着色器
			{				
				GLES20.glDeleteShader(vertexShaderHandle);
				vertexShaderHandle = 0;
			}
		}

		if (vertexShaderHandle == 0){throw new RuntimeException("Error creating vertex shader."); } //创建定点着色器失败

		int fragmentShaderHandle = GLES20.glCreateShader(GLES20.GL_FRAGMENT_SHADER);//在片段着色器中加载

		if (fragmentShaderHandle != 0) 
		{
			GLES20.glShaderSource(fragmentShaderHandle, fragmentShader);//传递着色器源
			GLES20.glCompileShader(fragmentShaderHandle);                 //编译着色器代码
			final int[] compileStatus = new int[1];
			GLES20.glGetShaderiv(fragmentShaderHandle, GLES20.GL_COMPILE_STATUS, compileStatus, 0);
			if (compileStatus[0] == 0) 
			{				
				GLES20.glDeleteShader(fragmentShaderHandle);
				fragmentShaderHandle = 0;
			}
		}

		if (fragmentShaderHandle == 0) { throw new RuntimeException("Error creating fragment shader."); }

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


		GLES20.glEnable(GLES20.GL_CULL_FACE);//打开背面剪裁
		GLES20.glDisable(GLES20.GL_CULL_FACE);//关闭背面剪裁
		GLES20.glFrontFace(GLES20.GL_CCW);//设置逆时针卷绕为正面,默认设置如此,所以一般不需要明确设置
		GLES20.glFrontFace(GLES20.GL_CW);//设置顺时针卷染为正面.
	}
	
	@Override
	public void onSurfaceChanged(GL10 glUnused, int width, int height) 
	{
		Log.i("opengl0","函数 onSurfaceChanged");
		GLES20.glViewport(0, 0, width, height);    //将opengl视口设置为跟surface同样大小
		//设置视口  0.0==x.y -->为视口矩形左上侧点在屏幕坐标系内的坐标
		//width.height 为视口的宽度与高度


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
//		glUnused.glTranslatef(0, 0, -4);
		GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);	//清屏 Log.i(tag,"进入onDrawFrame()");
		Matrix.setIdentityM(mModelMatrix, 0);

		// 每10秒做一次完成的旋转
//        long time = SystemClock.uptimeMillis() % 2000L;
//		float angleInDegrees = (360.0f / 2000.0f) * ((int) time);
        

 //初始化模型矩阵为单位矩阵 必须初始否则不显示图
//		Matrix.translateM(mModelMatrix, 0, 0.0f, 1.0f, 0.0f);              //矩阵平移,偏移量  x,y,z(平移量)//矩阵归位
//        Matrix.rotateM(mModelMatrix, 0, angleInDegrees, 0.0f, 0.0f, 1.0f); //矩阵旋转
        drawTriangle(mTriangle1Vertices);
	}	


	//以给定的顶点数据和颜色值,画一个三角形
	private void drawTriangle(final FloatBuffer aTriangleBuffer)
	{

		aTriangleBuffer.position(mPositionOffset);                    //传递位置信息 从0开始
		GLES20.glVertexAttribPointer(mPositionHandle, 3,
				GLES20.GL_FLOAT, false,3*4, aTriangleBuffer);         //将顶点数据传进渲染管线
        GLES20.glEnableVertexAttribArray(mPositionHandle);            //启用传入的数据



//        GLES20.glVertexAttribPointer(mPositionHandle, mPositionDataSize,
//				GLES20.GL_FLOAT, false,mStrideBytes, aTriangleBuffer);//将顶点数据传进渲染管线

        aTriangleBuffer.position(0);                                //类似上面 传递颜色 从第三个开始读取数组   mColorOffset
        GLES20.glVertexAttribPointer(mColorHandle, 4,
				GLES20.GL_FLOAT, false, 4*4, mTriangle1Colors);
        GLES20.glEnableVertexAttribArray(mColorHandle);

             String viewmatrix  = Arrays.toString(mViewMatrix);//java util 的包
		     String modelmatrix = Arrays.toString(mModelMatrix);

		Log.i("矩阵","变换之前mViewMatrix："+viewmatrix+",\nmModelMatrix:"+modelmatrix);
		//  应用投影和视口变换（进行投影变换）
        Matrix.multiplyMM(mMVPMatrix, 0, mViewMatrix, 0, mModelMatrix, 0);    //视图矩阵乘以模型矩阵 结果存在mMVPMatrix中


		String viewmatrix2  = Arrays.toString(mViewMatrix);//java util 的包
		String modelmatrix2 = Arrays.toString(mModelMatrix);
		Log.i("矩阵","变换之后mViewMatrix："+viewmatrix2+",\nmModelMatrix:"+modelmatrix2);

		Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mMVPMatrix, 0);//然后再乘以投影矩阵结果存放在mMVPMatrix中
        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mMVPMatrix, 0);//默认将数组以列向量的形式存放在矩阵中

        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 3); // 绘制三角形 最后3的倍数 整除
//		GLES20.col(1.0f, 1.0f, 0.5f, 0.5f);
//		GLES20.glEnable(0);
		GLES20.glLineWidth(16);
//		GLES20.glDrawArrays(GLES20.GL_POINTS, 0, 4); // 最后 个数
//		GLES20.glDrawArrays(GLES20.GL_LINE_LOOP, 0, 4); //  闭环折线
//		GLES20.glDrawArrays(GLES20.GL_LINES, 0, 4); // GL_LINE_STRIP折线
	}

	public void test(){//测试 访问render内部数据
		Log.i("test","点击test");
        Log.i("test","view矩阵："+             Arrays.toString(mViewMatrix) );
		Log.i("test","model矩阵："+            Arrays.toString(mModelMatrix));
		Log.i("test","mMVPMatrix矩阵："+       Arrays.toString(mMVPMatrix));
		Log.i("test","mProjectionMatrix矩阵："+Arrays.toString(mProjectionMatrix));

		Matrix.multiplyMM(mMVPMatrix, 0, mViewMatrix, 0, mModelMatrix, 0);
		Log.i("test","view*model 结果矩阵："+Arrays.toString(mMVPMatrix) );
		Log.i("test","mProjectionMatrix矩阵："+Arrays.toString(mProjectionMatrix));

	}



}
