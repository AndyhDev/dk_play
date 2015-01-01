package com.dk.play.util;

import java.io.File;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.preference.PreferenceManager;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;

import com.dk.play.App;
import com.dk.play.R;

public class ActionBarImage {
	private final String TAG = "ActionBArImage";
	
	private Activity activity;
	private SharedPreferences settings;
	private File imgFile;
	private String imgPath;
	private Bitmap bmp;
	private App app;
	
	public ActionBarImage(Activity activity){
		this.activity = activity;
		app = (App) activity.getApplication();
		
		imgFile = new File(activity.getFilesDir(), "actionbar.png");
		imgPath = imgFile.getAbsolutePath();
		Log.d("ActionBarImage", "PATH=" + imgPath);
		settings = PreferenceManager.getDefaultSharedPreferences(activity);
		settings.registerOnSharedPreferenceChangeListener(settingsListener);
		if(settings.getBoolean("use_bg_images", false)){
			setBgImage();
		}
	}
	SharedPreferences.OnSharedPreferenceChangeListener settingsListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
		public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
			if(prefs.getBoolean("use_bg_images", false)){
				setBgImage();
			}else{
				removeBgImage();
			}
		}
	};
	private void setBgImage(){
		Thread thread = new Thread(){
			@Override
			public void run() {
				TypedValue tv = new TypedValue();
				int actionBarHeight = 0;
			    if(activity.getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true)){
			        actionBarHeight = TypedValue.complexToDimensionPixelSize(tv.data, activity.getResources().getDisplayMetrics());
				}
			    Display display = activity.getWindowManager().getDefaultDisplay();
			    Point size = new Point();
			    display.getSize(size);
			    int width = size.x;
			    
			    if(bmp == null){
				    if(imgFile.exists()){
				    	bmp = app.getImage();
				    	if(bmp == null){
				    		Log.d(TAG, "LOAD2");
				    		bmp = Image.decodeSampledBitmapFromPath(imgPath, size.x, size.y);
				    		app.storeImage(bmp);
				    	}
				    }else{
				    	bmp = BitmapFactory.decodeResource(activity.getResources(), R.drawable.actionbar);
				    }
			    }
			    if(bmp == null){
			    	bmp = BitmapFactory.decodeResource(activity.getResources(), R.drawable.actionbar);
			    }
			    if(width > bmp.getWidth()){
			    	width = bmp.getWidth();
			    }
			    int height = actionBarHeight;
			    if(height > bmp.getHeight()){
			    	height = bmp.getHeight();
			    }
			    Bitmap bmp2 = Bitmap.createBitmap(bmp, 0, 0, width, height);
			    final Drawable bg = new BitmapDrawable(activity.getResources(), bmp2);
			    activity.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						activity.getActionBar().setBackgroundDrawable(bg);
					}
				});
			}
		};
		thread.start();
	}
	private void removeBgImage(){
		int[] android_styleable_ActionBar = { android.R.attr.background };

		TypedValue outValue = new TypedValue();
		activity.getTheme().resolveAttribute(android.R.attr.actionBarStyle, outValue, true);

		TypedArray abStyle = activity.getTheme().obtainStyledAttributes(outValue.resourceId, android_styleable_ActionBar);
		Drawable bg = abStyle.getDrawable(0);
		abStyle.recycle();
		activity.getActionBar().setBackgroundDrawable(bg);
	}
}
