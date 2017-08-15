package com.learnopengles.android.lesson1;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ConfigurationInfo;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.learnopengles.android.R;

public class LessonOneActivity extends Activity
{

	private GLSurfaceView mGLSurfaceView;//持有一个引用
	LessonOneRenderer mOneRenderer;      //自定义着色器
    Button one,two;
	@Override public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		Log.i("opengl0","函数 onCreate");
//		mGLSurfaceView = new GLSurfaceView(this);

		//检查是否支持opengl2.0
		final ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
		final ConfigurationInfo configurationInfo = activityManager.getDeviceConfigurationInfo();
		final boolean supportsEs2 = configurationInfo.reqGlEsVersion >= 0x20000;
		setContentView(R.layout.layout);
		mGLSurfaceView = (GLSurfaceView) findViewById(R.id.gl_surface_view);//
         one = (Button)findViewById(R.id.btn_one);
		 two = (Button)findViewById(R.id.btn_two);

		if (supportsEs2) //如果支持用2.0的上下文
		{

			mGLSurfaceView.setEGLContextClientVersion(2);
			mOneRenderer =new LessonOneRenderer();
			mGLSurfaceView.setRenderer(mOneRenderer);              //设置自定义渲染器
			mGLSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);//设置渲染模式 更高效


		}
		else 
		{
			// This is where you could create an OpenGL ES 1.x compatible
			// renderer if you wanted to support both ES 1 and ES 2.
			return;
		}

//		setContentView(mGLSurfaceView);

		one.setOnClickListener(new View.OnClickListener() {
			@Override public void onClick(View v) {
				Log.i("test","点击one");
			}
		});

		two.setOnClickListener(new View.OnClickListener() {
			@Override public void onClick(View v) {
				Log.i("test","点击two");
				mOneRenderer.test();
			}
		});

	}

	@Override
	protected void onResume() 
	{
		super.onResume();
		mGLSurfaceView.onResume();//必须在activity的onResume()中调用GL surface view 的onResume()
	}

	@Override
	protected void onPause() 
	{
		super.onPause();
		mGLSurfaceView.onPause();
	}


}