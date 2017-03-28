package com.learnopengles.android.lesson2;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ConfigurationInfo;
import android.opengl.GLSurfaceView;
import android.os.Bundle;

public class LessonTwoActivity extends Activity 
{
	private GLSurfaceView mGLSurfaceView;//持有一个GLSurfaceView引用
	@Override
	public void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		mGLSurfaceView = new GLSurfaceView(this);

		final ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
		final ConfigurationInfo configurationInfo = activityManager.getDeviceConfigurationInfo();
		final boolean supportsEs2 = configurationInfo.reqGlEsVersion >= 0x20000;

		if (supportsEs2)  {
			mGLSurfaceView.setEGLContextClientVersion(2);
			mGLSurfaceView.setRenderer(new LessonTwoRenderer());
		}  else  {  return; }

		setContentView(mGLSurfaceView);
	}

	@Override
	protected void onResume() 
	{
		// The activity must call the GL surface view's onResume() on activity onResume().
		super.onResume();
		mGLSurfaceView.onResume();
	}

	@Override
	protected void onPause() 
	{
		// The activity must call the GL surface view's onPause() on activity onPause().
		super.onPause();
		mGLSurfaceView.onPause();
	}	
}