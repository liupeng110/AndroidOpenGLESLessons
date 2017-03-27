package com.learnopengles.android.lesson1;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ConfigurationInfo;
import android.opengl.GLSurfaceView;
import android.os.Bundle;

public class LessonOneActivity extends Activity
{
	/** Hold a reference to our GLSurfaceView */
	private GLSurfaceView mGLSurfaceView;
	LessonOneRenderer mOneRenderer;
	@Override
	public void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);

		mGLSurfaceView = new GLSurfaceView(this);

		//检查是否支持opengl2.0
		final ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
		final ConfigurationInfo configurationInfo = activityManager.getDeviceConfigurationInfo();
		final boolean supportsEs2 = configurationInfo.reqGlEsVersion >= 0x20000;

		if (supportsEs2) //如果支持用2.0的上下文
		{

			mGLSurfaceView.setEGLContextClientVersion(2);
			mOneRenderer =new LessonOneRenderer();
			mGLSurfaceView.setRenderer(mOneRenderer);              //设置自定义渲染器
		} 
		else 
		{
			// This is where you could create an OpenGL ES 1.x compatible
			// renderer if you wanted to support both ES 1 and ES 2.
			return;
		}

		setContentView(mGLSurfaceView);
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