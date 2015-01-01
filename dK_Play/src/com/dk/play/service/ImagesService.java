package com.dk.play.service;

import java.io.FileOutputStream;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Binder;
import android.os.IBinder;
import android.view.Display;
import android.view.WindowManager;

import com.dk.util.network.DownloadImage;
import com.dk.util.network.DownloadImageListener;
import com.dk.util.network.Request;
import com.dk.util.network.RequestListener;

public class ImagesService extends Service {
	@SuppressWarnings("unused")
	private static final String TAG = "ImagesService";
	private final IBinder mBinder = new MyBinder();
	
	private ConnectivityManager iNet;
	private int updateInterval = 24*60*60*1000;
	private SharedPreferences settings;
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		//Log.d(TAG, "START");
		iNet = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
		
		settings = getSharedPreferences("other", Context.MODE_PRIVATE);
		long lastUpdate = settings.getLong("images_last_update", 0);
		if(System.currentTimeMillis() - updateInterval > lastUpdate){
			//Log.d(TAG, "1");
			runUpdate();
		}
	    return Service.START_NOT_STICKY;
	}
	private void runUpdate(){
		NetworkInfo info = iNet.getActiveNetworkInfo();
		if(info == null){
			return;
		}
		int type = info.getType();
		if(type == ConnectivityManager.TYPE_WIFI){
			if(!info.isConnected()){
				return;
			}
		}
		Request request = new Request("https://dk-force.de/data/dkplay/actionbarImageId");
		request.setListener(new RequestListener() {
			@Override
			public void onRequestError() {
				//Log.d(TAG, "image response error");
			}
			
			@Override
			public void onRequestEnd(String msg) {
				//Log.d(TAG, "image response end: " + msg);
				//Log.d(TAG, "int" + isInteger(msg.trim()));
				msg = msg.trim();
				if(isInteger(msg)){
					//Log.d(TAG, "image response end2");
					final int id = Integer.parseInt(msg);
					int cur = settings.getInt("action_bar_image_id", 0);
					if(id > cur){
						//Log.d(TAG, "image response end3");
						
						WindowManager window = (WindowManager) getSystemService(Context.WINDOW_SERVICE); 
					    Display display = window.getDefaultDisplay();
					    Point size = new Point();
					    display.getSize(size);
					    String dwSize = "low";
					    if(size.x + size.y > 3300){
					    	dwSize = "hight";
					    }else if(size.x + size.y > 2500){
					    	dwSize = "middle";
					    }
					    //Log.d(TAG, "Size: " + dwSize);
						DownloadImage download = new DownloadImage("https://dk-force.de/data/dkplay/actionbar_" + dwSize + ".jpg");
						download.setListener(new DownloadImageListener() {
							
							@Override
							public void onDownloadImageProgress(int progress) {
							}
							
							@Override
							public void onDownloadImageError() {
								//Log.d(TAG, "image download error");
							}
							@Override
							public void onDownloadImageEnd(Bitmap bitmap) {
								try {
								    FileOutputStream fos = openFileOutput("actionbar.png", Context.MODE_PRIVATE);
								    bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
								    fos.close();
								    Editor edit = settings.edit();
								    edit.putLong("images_last_update", System.currentTimeMillis());
								    edit.putInt("action_bar_image_id", id);
								    edit.commit();
								    //Log.d(TAG, "image downloaded");
							    } catch (Exception e) {
							    }
							}
						});
						download.start();
					}
				}
			}
		});
		request.start();
	}
	public boolean isInteger(String s) {
	    try { 
	        Integer.parseInt(s);
	        return true;
	    } catch(NumberFormatException e) { 
	        return false; 
	    }
	}
	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}
	public class MyBinder extends Binder {
	    ImagesService getService() {
	      return ImagesService.this;
	    }
	  }

}
