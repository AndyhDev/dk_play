package com.dk.play;

import java.io.File;

import com.dk.play.util.Image;
import com.dk.play.util.PRNGFixes;

import android.app.Application;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;

public class App extends Application {
	private static final String TAG = "App";
	
	private static Context context;
	private Bitmap bmp;
	
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
		Log.d(TAG, "onCreate");
		PRNGFixes.apply();
		
		this.context = getApplicationContext();
		
		File imgFile = new File(context.getFilesDir(), "actionbar.png");
		String imgPath = imgFile.getAbsolutePath();
		
		Display display = ((WindowManager)context.getSystemService(WINDOW_SERVICE)).getDefaultDisplay();
	    Point size = new Point();
	    display.getSize(size);
	    
	    storeImage(Image.decodeSampledBitmapFromPath(imgPath, size.x, size.y));
	}
	
	public void storeImage(Bitmap img){
		this.bmp = img;
	}
	public Bitmap getImage(){
		return this.bmp;
	}
} 