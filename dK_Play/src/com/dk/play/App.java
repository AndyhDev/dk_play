package com.dk.play;

import android.app.Application;
import android.content.Context;
import android.content.res.Resources;

public class App extends Application {
	private static Context context;

	public static Resources getResourcesStatic() {
		return context.getResources();
	}
	public static Context getContextStatic() {
		return context;
	}

	@SuppressWarnings("static-access")
	@Override
	public void onCreate() {
		super.onCreate();

		this.context = getApplicationContext();
	}
} 