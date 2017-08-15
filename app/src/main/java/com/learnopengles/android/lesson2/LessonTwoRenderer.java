package com.learnopengles.android.lesson2;

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


public class LessonTwoRenderer implements GLSurfaceView.Renderer 
{

	private static final String TAG = "LessonTwoRenderer";
	private float[] mModelMatrix = new float[16];     //模型矩阵
	private float[] mViewMatrix = new float[16];      //view矩阵
	private float[] mProjectionMatrix = new float[16];//投影矩阵
	private float[] mMVPMatrix = new float[16];       //
	

	private float[] mLightModelMatrix = new float[16];
	private final FloatBuffer mCubePositions;//保存model数据
	private final FloatBuffer mCubeColors;
	private final FloatBuffer mCubeNormals;

	private int mMVPMatrixHandle;
	private int mMVMatrixHandle;
	private int mLightPosHandle;
	private int mPositionHandle;//声明顶点位置属性引用
	private int mColorHandle;
	private int mNormalHandle;
	private final int mBytesPerFloat = 4;
	private final int mPositionDataSize = 3;
	private final int mColorDataSize = 4;
	private final int mNormalDataSize = 3;
	
	/** Used to hold a light centered on the origin in model space. We need a 4th coordinate so we can get translations to work when
	 *  we multiply this by our transformation matrices. */
	private final float[] mLightPosInModelSpace = new float[] {0.0f, 0.0f, 0.0f, 1.0f};
	
	/** Used to hold the current position of the light in world space (after transformation via model matrix). */
	private final float[] mLightPosInWorldSpace = new float[4];
	
	/** Used to hold the transformed position of the light in eye space (after transformation via modelview matrix) */
	private final float[] mLightPosInEyeSpace = new float[4];
	
	/** This is a handle to our per-vertex cube shading program. */
	private int mPerVertexProgramHandle;
		
	/** This is a handle to our light point program. */
	private int mPointProgramHandle;	
						
	/**
	 * Initialize the model data.
	 */
	public LessonTwoRenderer()
	{	
		// Define points for a cube.		
		
		// X, Y, Z  立方体位置坐标
		final float[] cubePositionData =
		{
				// In OpenGL counter-clockwise winding is default. This means that when we look at a triangle, 
				// if the points are counter-clockwise we are looking at the "front". If not we are looking at
				// the back. OpenGL has an optimization where all back-facing triangles are culled, since they
				// usually represent the backside of an object and aren't visible anyways.
				
				// Front face
				-1.0f, 1.0f, 1.0f,				
				-1.0f, -1.0f, 1.0f,
				1.0f, 1.0f, 1.0f, 
				-1.0f, -1.0f, 1.0f, 				
				1.0f, -1.0f, 1.0f,
				1.0f, 1.0f, 1.0f,
				
				// Right face
				1.0f, 1.0f, 1.0f,				
				1.0f, -1.0f, 1.0f,
				1.0f, 1.0f, -1.0f,
				1.0f, -1.0f, 1.0f,				
				1.0f, -1.0f, -1.0f,
				1.0f, 1.0f, -1.0f,
				
				// Back face
				1.0f, 1.0f, -1.0f,				
				1.0f, -1.0f, -1.0f,
				-1.0f, 1.0f, -1.0f,
				1.0f, -1.0f, -1.0f,				
				-1.0f, -1.0f, -1.0f,
				-1.0f, 1.0f, -1.0f,
				
				// Left face
				-1.0f, 1.0f, -1.0f,				
				-1.0f, -1.0f, -1.0f,
				-1.0f, 1.0f, 1.0f, 
				-1.0f, -1.0f, -1.0f,				
				-1.0f, -1.0f, 1.0f, 
				-1.0f, 1.0f, 1.0f, 
				
				// Top face
				-1.0f, 1.0f, -1.0f,				
				-1.0f, 1.0f, 1.0f, 
				1.0f, 1.0f, -1.0f, 
				-1.0f, 1.0f, 1.0f, 				
				1.0f, 1.0f, 1.0f, 
				1.0f, 1.0f, -1.0f,
				
				// Bottom face
				1.0f, -1.0f, -1.0f,				
				1.0f, -1.0f, 1.0f, 
				-1.0f, -1.0f, -1.0f,
				1.0f, -1.0f, 1.0f, 				
				-1.0f, -1.0f, 1.0f,
				-1.0f, -1.0f, -1.0f,
		};	
		
		// R, G, B, A   立方体颜色数据
		final float[] cubeColorData =
		{				
				// Front face (red)
				1.0f, 0.0f, 0.0f, 1.0f,				
				1.0f, 0.0f, 0.0f, 1.0f,
				1.0f, 0.0f, 0.0f, 1.0f,
				1.0f, 0.0f, 0.0f, 1.0f,				
				1.0f, 0.0f, 0.0f, 1.0f,
				1.0f, 0.0f, 0.0f, 1.0f,
				
				// Right face (green)
				0.0f, 1.0f, 0.0f, 1.0f,				
				0.0f, 1.0f, 0.0f, 1.0f,
				0.0f, 1.0f, 0.0f, 1.0f,
				0.0f, 1.0f, 0.0f, 1.0f,				
				0.0f, 1.0f, 0.0f, 1.0f,
				0.0f, 1.0f, 0.0f, 1.0f,
				
				// Back face (blue)
				0.0f, 0.0f, 1.0f, 1.0f,				
				0.0f, 0.0f, 1.0f, 1.0f,
				0.0f, 0.0f, 1.0f, 1.0f,
				0.0f, 0.0f, 1.0f, 1.0f,				
				0.0f, 0.0f, 1.0f, 1.0f,
				0.0f, 0.0f, 1.0f, 1.0f,
				
				// Left face (yellow)
				1.0f, 1.0f, 0.0f, 1.0f,				
				1.0f, 1.0f, 0.0f, 1.0f,
				1.0f, 1.0f, 0.0f, 1.0f,
				1.0f, 1.0f, 0.0f, 1.0f,				
				1.0f, 1.0f, 0.0f, 1.0f,
				1.0f, 1.0f, 0.0f, 1.0f,
				
				// Top face (cyan)
				0.0f, 1.0f, 1.0f, 1.0f,				
				0.0f, 1.0f, 1.0f, 1.0f,
				0.0f, 1.0f, 1.0f, 1.0f,
				0.0f, 1.0f, 1.0f, 1.0f,				
				0.0f, 1.0f, 1.0f, 1.0f,
				0.0f, 1.0f, 1.0f, 1.0f,
				
				// Bottom face (magenta)
				1.0f, 0.0f, 1.0f, 1.0f,				
				1.0f, 0.0f, 1.0f, 1.0f,
				1.0f, 0.0f, 1.0f, 1.0f,
				1.0f, 0.0f, 1.0f, 1.0f,				
				1.0f, 0.0f, 1.0f, 1.0f,
				1.0f, 0.0f, 1.0f, 1.0f
		};
		
		// X, Y, Z    法线数据
		// 正常用于光计算，是一个向量点
		// orthogonal to the plane of the surface. For a cube model, the normals should be orthogonal to the points of each face.
		//对表面的平面正交。对于多维数据集模型，法线应该与每个面的点正交
		final float[] cubeNormalData =
		{
				// Front face
				0.0f, 0.0f, 1.0f,
				0.0f, 0.0f, 1.0f,
				0.0f, 0.0f, 1.0f,
				0.0f, 0.0f, 1.0f,
				0.0f, 0.0f, 1.0f,
				0.0f, 0.0f, 1.0f,

				// Right face
				1.0f, 0.0f, 0.0f,
				1.0f, 0.0f, 0.0f,
				1.0f, 0.0f, 0.0f,
				1.0f, 0.0f, 0.0f,
				1.0f, 0.0f, 0.0f,
				1.0f, 0.0f, 0.0f,

				// Back face
				0.0f, 0.0f, -1.0f,
				0.0f, 0.0f, -1.0f,
				0.0f, 0.0f, -1.0f,
				0.0f, 0.0f, -1.0f,
				0.0f, 0.0f, -1.0f,
				0.0f, 0.0f, -1.0f,

				// Left face
				-1.0f, 0.0f, 0.0f,
				-1.0f, 0.0f, 0.0f,
				-1.0f, 0.0f, 0.0f,
				-1.0f, 0.0f, 0.0f,
				-1.0f, 0.0f, 0.0f,
				-1.0f, 0.0f, 0.0f,

				// Top face
				0.0f, 1.0f, 0.0f,
				0.0f, 1.0f, 0.0f,
				0.0f, 1.0f, 0.0f,
				0.0f, 1.0f, 0.0f,
				0.0f, 1.0f, 0.0f,
				0.0f, 1.0f, 0.0f,

				// Bottom face
				0.0f, -1.0f, 0.0f,
				0.0f, -1.0f, 0.0f,
				0.0f, -1.0f, 0.0f,
				0.0f, -1.0f, 0.0f,
				0.0f, -1.0f, 0.0f,
				0.0f, -1.0f, 0.0f
		};
		
		//初始化缓冲数据
		mCubePositions = ByteBuffer.allocateDirect(cubePositionData.length * mBytesPerFloat)
        .order(ByteOrder.nativeOrder()).asFloatBuffer(); //设置字节顺序为本地操作系统顺序
		mCubePositions.put(cubePositionData).position(0);//将数组中的顶点数据送入缓冲
		
		mCubeColors = ByteBuffer.allocateDirect(cubeColorData.length * mBytesPerFloat)
        .order(ByteOrder.nativeOrder()).asFloatBuffer();
		mCubeColors.put(cubeColorData).position(0);
		
		mCubeNormals = ByteBuffer.allocateDirect(cubeNormalData.length * mBytesPerFloat)
        .order(ByteOrder.nativeOrder()).asFloatBuffer();							
		mCubeNormals.put(cubeNormalData).position(0);
	}
	
	protected String getVertexShader()
	{
		 final String vertexShader =
			"uniform mat4 u_MVPMatrix;      \n"		// 总变换矩阵
		  + "uniform mat4 u_MVMatrix;       \n"		// 常数表示组合模型/视图矩阵。
		  + "uniform vec3 u_LightPos;       \n"	    // 光在眼空间中的位置。 向量

		  + "attribute vec4 a_Position;     \n"		//每个顶点位置信息
		  + "attribute vec4 a_Color;        \n"		//每个顶点颜色信息
		  + "attribute vec3 a_Normal;       \n"		// 每个定点的法线信息

		  + "varying vec4 v_Color;          \n"		// 这将被传递到片段着色器。
		  + "void main()                    \n" 	// 顶点着色器入口点
		  + "{                              \n"
		  + "   vec3 modelViewVertex = vec3(u_MVMatrix * a_Position);              \n"// 将顶点变换成眼空间  向量
		  + "   vec3 modelViewNormal = vec3(u_MVMatrix * vec4(a_Normal, 0.0));     \n"// 将法线方向变换成眼空间
		  + "   float distance = length(u_LightPos - modelViewVertex);             \n"// 将用于衰减
		  + "   vec3 lightVector = normalize(u_LightPos - modelViewVertex);        \n"//从光到顶点得到照明方向矢量
		  + "   float diffuse = max(dot(modelViewNormal, lightVector), 0.1);       \n"// 计算光矢量和顶点法线的点积。如果法线和光矢量是 或指向同一方向，然后将得到最大照明。
		  + "   diffuse = diffuse *  (1.0 + (0.05 * distance * distance) );        \n"//设置基于距离的光衰减
		  + "   v_Color = a_Color * diffuse;                                       \n"//乘以颜色的照明水平 将被插值在三角形内
		  + "   gl_Position = u_MVPMatrix * a_Position;                            \n"  //gl_position是用来存储最终位置特殊的变量。
		  + "}                                                                     \n"; //矩阵相乘的顶点by the the normalized to get the终点在屏幕坐标

		return vertexShader;
	}
	
	protected String getFragmentShader()
	{
		final String fragmentShader =
			"precision mediump float;       \n"		// 设置默认精度为中等
													// 片段着色器中的精度。
		  + "varying vec4 v_Color;          \n"		// 这是从顶点着色器插值的颜色
		  											// 每个三角形的片段
		  + "void main()                    \n"		// 我们的片段着色器的入口点。
		  + "{                              \n"
		  + "   gl_FragColor = v_Color;     \n"		// 通过管道直接传递颜色。
		  + "}                              \n";
		
		return fragmentShader;
	}
	
	@Override
	public void onSurfaceCreated(GL10 glUnused, EGLConfig config) 
	{

		GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);//设置opengl背景颜色
		GLES20.glEnable(GLES20.GL_CULL_FACE);//使用剔除移除背面。
		GLES20.glEnable(GLES20.GL_DEPTH_TEST);//启用深度测试

		final float eyeX = 0.0f;	//把眼睛放在原点的前面。
		final float eyeY = 0.0f;
		final float eyeZ = -0.5f;

		final float lookX = 0.0f;	//看向的点
		final float lookY = 0.0f;
		final float lookZ = -5.0f;

		final float upX = 0.0f;//设置我们的向量。这是我们的头将指向我们持有相机。
		final float upY = 1.0f;
		final float upZ = 0.0f;

		// 设置视图矩阵。这个矩阵可以表示相机的位置。
		// 注：在OpenGL 1，一个模型视图矩阵，这是一个组合的模型
		// 在OpenGL 2中，我们可以单独跟踪这些矩阵，如果我们选择。
		Matrix.setLookAtM(mViewMatrix, 0, eyeX, eyeY, eyeZ, lookX, lookY, lookZ, upX, upY, upZ);		

		final String vertexShader = getVertexShader();   		
 		final String fragmentShader = getFragmentShader();			
		
		final int vertexShaderHandle = compileShader(GLES20.GL_VERTEX_SHADER, vertexShader);		
		final int fragmentShaderHandle = compileShader(GLES20.GL_FRAGMENT_SHADER, fragmentShader);		
		
		mPerVertexProgramHandle = createAndLinkProgram(vertexShaderHandle, fragmentShaderHandle, 
				new String[] {"a_Position",  "a_Color", "a_Normal"});								                                							       
        

        final String pointVertexShader =    //为我们的点定义一个简单的着色器程序。
        	"uniform mat4 u_MVPMatrix;                     \n"
          +	"attribute vec4 a_Position;                    \n"
          + "void main()                                   \n"
          + "{                                             \n"
          + "   gl_Position = u_MVPMatrix * a_Position;    \n"
          + "   gl_PointSize = 55.0;                       \n"//小圆点size
          + "}                                             \n";
        
        final String pointFragmentShader = 
        	"precision mediump float;                   \n"
          + "void main()                                \n"
          + "{                                          \n"
          + "  gl_FragColor = vec4(0.5,0.5,0.5, 1.0);   \n"//光源点 颜色
          + "}                                          \n";

        final int pointVertexShaderHandle   = compileShader(GLES20.GL_VERTEX_SHADER, pointVertexShader);
        final int pointFragmentShaderHandle = compileShader(GLES20.GL_FRAGMENT_SHADER, pointFragmentShader);
        mPointProgramHandle = createAndLinkProgram(pointVertexShaderHandle, pointFragmentShaderHandle, 
        		new String[] {"a_Position"});                 
	}	
		
	@Override public void onSurfaceChanged(GL10 glUnused, int width, int height)
	{
		GLES20.glViewport(0, 0, width, height);//将OpenGL视口设置为与表面相同的大小。

		// 创建一个新的透视投影矩阵。高度将保持不变，宽度随纵横比而变化。
		final float ratio = (float) width / height;
		final float left = -ratio;
		final float right = ratio;
		final float bottom = -1.0f;
		final float top = 1.0f;
		final float near = 1.0f;
		final float far = 25.0f;
		
		Matrix.frustumM(mProjectionMatrix, 0, left, right, bottom, top, near, far);//光锥
	}	

	@Override
	public void onDrawFrame(GL10 glUnused) 
	{
		GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
        long time = SystemClock.uptimeMillis() % 10000L; //利用onDrawFrame重复调用,10秒旋转一周
        float angleInDegrees = (360.0f / 10000.0f) * ((int) time);
        GLES20.glUseProgram(mPerVertexProgramHandle); // 设置顶点照明程序
        
        // 立方体绘制设置程序句柄
        mMVPMatrixHandle = GLES20.glGetUniformLocation(mPerVertexProgramHandle, "u_MVPMatrix");
        mMVMatrixHandle  = GLES20.glGetUniformLocation(mPerVertexProgramHandle, "u_MVMatrix");
        mLightPosHandle  = GLES20.glGetUniformLocation(mPerVertexProgramHandle, "u_LightPos");//填入的光源位置 坐标
        mPositionHandle  = GLES20.glGetAttribLocation(mPerVertexProgramHandle, "a_Position"); //获取顶点位置属性引用的值
        mColorHandle     = GLES20.glGetAttribLocation(mPerVertexProgramHandle, "a_Color");
        mNormalHandle    = GLES20.glGetAttribLocation(mPerVertexProgramHandle, "a_Normal");
        
        // 计算光的位置。旋转然后推入距离
        Matrix.setIdentityM(mLightModelMatrix, 0);
        Matrix.translateM(mLightModelMatrix, 0, 0.0f, 0.0f, -5.0f);      
        Matrix.rotateM(mLightModelMatrix, 0, angleInDegrees, 0.0f, 1.0f, 0.0f);
        Matrix.translateM(mLightModelMatrix, 0, 0.0f, 0.0f, 2.0f);
               
        Matrix.multiplyMV(mLightPosInWorldSpace, 0, mLightModelMatrix, 0, mLightPosInModelSpace, 0);
        Matrix.multiplyMV(mLightPosInEyeSpace, 0, mViewMatrix, 0, mLightPosInWorldSpace, 0);                        
        
        //绘制立方体
        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.translateM(mModelMatrix, 0, 4.0f, 0.0f, -7.0f);
        Matrix.rotateM(mModelMatrix, 0, angleInDegrees, 1.0f, 0.0f, 0.0f);        
        drawCube();

//        Matrix.setIdentityM(mModelMatrix, 0);
//        Matrix.translateM(mModelMatrix, 0, -4.0f, 0.0f, -7.0f);
//        Matrix.rotateM(mModelMatrix, 0, angleInDegrees, 0.0f, 1.0f, 0.0f);
//        drawCube();
//
//        Matrix.setIdentityM(mModelMatrix, 0);
//        Matrix.translateM(mModelMatrix, 0, 0.0f, 4.0f, -7.0f);
//        Matrix.rotateM(mModelMatrix, 0, angleInDegrees, 0.0f, 0.0f, 1.0f);
//        drawCube();
//
//        Matrix.setIdentityM(mModelMatrix, 0);
//        Matrix.translateM(mModelMatrix, 0, 0.0f, -4.0f, -7.0f);
//        drawCube();
//
//        Matrix.setIdentityM(mModelMatrix, 0);
//        Matrix.translateM(mModelMatrix, 0, 0.0f, 0.0f, -5.0f);
//        Matrix.rotateM(mModelMatrix, 0, angleInDegrees, 1.0f, 1.0f, 0.0f);
//        drawCube();

        GLES20.glUseProgram(mPointProgramHandle); //画点指示灯
        drawLight();
	}				

	private void drawCube()//绘制立方体
	{
		mCubePositions.position(0);	//传递位置信息
        GLES20.glVertexAttribPointer(
        		mPositionHandle,   //顶点位置属性引用
				3,                 //每顶点一组的数据个数(这里是xyz因此为3)
				GLES20.GL_FLOAT,   //数据类型
				false, 0,          //是否规格化
				mCubePositions);   //存放了数据的缓冲
        GLES20.glEnableVertexAttribArray(mPositionHandle);//启用顶点位置数据

        mCubeColors.position(0);        //传递颜色信息
        GLES20.glVertexAttribPointer(mColorHandle, mColorDataSize, GLES20.GL_FLOAT, false, 0, mCubeColors);
        GLES20.glEnableVertexAttribArray(mColorHandle);

        mCubeNormals.position(0);       //传递法线信息
        GLES20.glVertexAttribPointer(mNormalHandle, mNormalDataSize, GLES20.GL_FLOAT, false, 0, mCubeNormals);
        GLES20.glEnableVertexAttribArray(mNormalHandle);

		//视图矩阵乘以模型矩阵 结果存储在MVP矩阵
        Matrix.multiplyMM(mMVPMatrix, 0, mViewMatrix, 0, mModelMatrix, 0);
        GLES20.glUniformMatrix4fv(mMVMatrixHandle, 1, false, mMVPMatrix, 0); //将最终组合矩阵传递到顶点渲染器中
        
        //然后再乘以投影矩阵 结果存储在MVP矩阵
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mMVPMatrix, 0);
        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mMVPMatrix, 0);//将最终组合矩阵传递到顶点渲染器中
        
        //在眼睛空间的光线位置传递
        GLES20.glUniform3f(mLightPosHandle, mLightPosInEyeSpace[0], mLightPosInEyeSpace[1], mLightPosInEyeSpace[2]);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 36);  //绘制立方体
	}


	private void drawLight() // 画一个点代表光的位置
	{
		final int pointMVPMatrixHandle = GLES20.glGetUniformLocation(mPointProgramHandle, "u_MVPMatrix");
        final int pointPositionHandle = GLES20.glGetAttribLocation(mPointProgramHandle, "a_Position");
        
		//传递位置
		GLES20.glVertexAttrib3f(pointPositionHandle, mLightPosInModelSpace[0], mLightPosInModelSpace[1], mLightPosInModelSpace[2]);

		//由于我们不使用缓冲对象，因此禁用此属性的顶点数组
        GLES20.glDisableVertexAttribArray(pointPositionHandle);  
		
		//把光线矩阵 乘进去.并进行投影变换
		Matrix.multiplyMM(mMVPMatrix, 0, mViewMatrix, 0, mLightModelMatrix, 0);
		Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mMVPMatrix, 0);
		GLES20.glUniformMatrix4fv(pointMVPMatrixHandle, 1, false, mMVPMatrix, 0);

		GLES20.glDrawArrays(GLES20.GL_POINTS, 0, 1);//画点
	}

	//编译着色器的辅助函数 参数:着色器类型,着色器源码  返回opengl 着色器句柄
	private int compileShader(final int shaderType, final String shaderSource) 
	{
		int shaderHandle = GLES20.glCreateShader(shaderType);
		if (shaderHandle != 0) 
		{
			GLES20.glShaderSource(shaderHandle, shaderSource);//进入着色器源码
			GLES20.glCompileShader(shaderHandle);             //编译着色器
			final int[] compileStatus = new int[1];           //获取编译器状态
			GLES20.glGetShaderiv(shaderHandle, GLES20.GL_COMPILE_STATUS, compileStatus, 0);
			if (compileStatus[0] == 0) //如果编译失败 删除着色器
			{
				Log.e(TAG, "Error compiling shader: " + GLES20.glGetShaderInfoLog(shaderHandle));
				GLES20.glDeleteShader(shaderHandle);
				shaderHandle = 0;
			}
		}
		if (shaderHandle == 0) { throw new RuntimeException("Error creating shader."); }
		return shaderHandle;
	}	

	//编译和链接程序的辅助函数。
	//参数:已编译的顶点着色器的OpenGL句柄,已编译的片段着色器的OpenGL句柄,需要绑定到程序的属性。返回opengl 着色器句柄
	private int createAndLinkProgram(final int vertexShaderHandle, final int fragmentShaderHandle, final String[] attributes) 
	{
		int programHandle = GLES20.glCreateProgram();
		if (programHandle != 0) 
		{
			GLES20.glAttachShader(programHandle, vertexShaderHandle); //将顶点着色器绑定到程序
			GLES20.glAttachShader(programHandle, fragmentShaderHandle);//将片段着色器绑定到程序。

			if (attributes != null)//绑定属性
			{
				final int size = attributes.length;
				for (int i = 0; i < size; i++)
				{
					GLES20.glBindAttribLocation(programHandle, i, attributes[i]);
				}						
			}

			GLES20.glLinkProgram(programHandle);//将两个着色器链接到一个程序中。
			final int[] linkStatus = new int[1]; //获取链接状态。
			GLES20.glGetProgramiv(programHandle, GLES20.GL_LINK_STATUS, linkStatus, 0);

			if (linkStatus[0] == 0) //链接失败删除程序
			{				
				Log.e(TAG, "Error compiling program: " + GLES20.glGetProgramInfoLog(programHandle));
				GLES20.glDeleteProgram(programHandle);
				programHandle = 0;
			}
		}
		
		if (programHandle == 0) { throw new RuntimeException("Error creating program."); }
		return programHandle;
	}
}
