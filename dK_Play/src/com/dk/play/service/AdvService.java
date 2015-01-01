package com.dk.play.service;

import org.json.JSONObject;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;

import com.dk.play.adv.AdvControl;
import com.dk.play.util.AdvActLibrary2;
import com.dk.util.network.ApiCallListener;
import com.dk.util.network.dKApiCall;
import com.dk.util.network.dKSession;

public class AdvService extends Service {
	private static final String TAG = "AdvService";
	
	private final IBinder mBinder = new MyBinder();
	private Handler stateUpdateHandler = new Handler();
	private Handler libraryUpdateHandler = new Handler();
	private ConnectivityManager iNet;
	private SharedPreferences settings;
	public boolean uploadOnlyOnWifi = true;
	public boolean deleteFromSync = false;
	public boolean deleteFromDevice = false;
	public boolean showNotify = true;
	public int updateInterval = 120;
	private AdvControl adv;
	
	private volatile AdvActLibrary2 advActLibrary;
	private boolean started = false;
	
	public static final int notifyId = 20;
	public static final int maxUploadSize = 100*1024*1024;
	public static final String ACTION_BYPASS = "action_by_pass";
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		settings = PreferenceManager.getDefaultSharedPreferences(this);
		settings.registerOnSharedPreferenceChangeListener(settingsListener);
		
		adv = new AdvControl(getApplicationContext());
		uploadOnlyOnWifi = settings.getBoolean("update_only_on_wifi", true);
		deleteFromSync = settings.getBoolean("delete_from_sync", false);
		deleteFromDevice = settings.getBoolean("delete_from_deive", false);
		showNotify = settings.getBoolean("adv_show_notify", true);
		updateInterval = Integer.valueOf(settings.getString("adv_update_interval", "120"));
		
		iNet = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
		
		setUpdateLoop();
		
		if(intent != null){
			if(intent.getAction() != null){
				if(intent.getAction().equals(BootBroadcastReceiver.SERVICE_ACTION)){
					runUpdate();
				}else if(intent.getAction().equals(ACTION_BYPASS)){
					actLibraryByPass();
				}
			}
		}
		if(!started){
			Log.d(TAG, "START");
			advActLibrary = new AdvActLibrary2(this, this);
			
			actLibrary();
		}else{
			started = true;
		}
		
	    return Service.START_STICKY;
	}
	private void setUpdateLoop() {
		stateUpdateHandler.postDelayed(new Runnable(){
			@Override
			public void run() {
				runUpdate();
				setUpdateLoop();
			}
		}, 120*60*1000);
	}
	SharedPreferences.OnSharedPreferenceChangeListener settingsListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
		public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
			uploadOnlyOnWifi = settings.getBoolean("update_only_on_wifi", true);
			deleteFromSync = settings.getBoolean("delete_from_sync", false);
			deleteFromDevice = settings.getBoolean("delete_from_deive", false);
			showNotify = settings.getBoolean("adv_show_notify", true);
			updateInterval = Integer.valueOf(prefs.getString("adv_update_interval", "120"));
			if(!showNotify){
				stopForeground(true);
			}
		}
	};
	
	private void actLibrary(){
		SharedPreferences settings = adv.getSettings();
		long lastCheck = settings.getLong(AdvControl.lastUpdate, 0);
		long time = ((System.currentTimeMillis()/1000) - lastCheck)/60;
		if(time < updateInterval){
			return;
		}
		Log.d(TAG, "actLibrary()");
		if(!advActLibrary.isActive()){
			advActLibrary.start();
			Log.d(TAG, "actLibrary().start");
		}
		libraryUpdateHandler.postDelayed(new Runnable() {
			@Override
			public void run() {
				actLibrary();
			}
		}, (updateInterval*10000) / 3);
	}
	public void actLibraryByPass(){
		Log.d(TAG, "actLibrary()");
		if(!advActLibrary.isActive()){
			advActLibrary.startByPass();
			Log.d(TAG, "actLibrary().start");
		}
	}
	
	private Long getTimestamp(){
		return (Long) System.currentTimeMillis()/1000;
	}
	private void runUpdate(){
		NetworkInfo info = iNet.getActiveNetworkInfo();
		if(info == null){
			return;
		}
		if(!info.isConnected()){
			return;
		}
		Thread t = new Thread(){
			@Override
			public void run() {
				dKSession session = adv.getSession();
				if(session == null){
					return;
				}
				
				dKApiCall call2 = new dKApiCall(session, "adv_get_space_info");
				call2.setListener(new ApiCallListener() {
					@Override
					public void onApiCallSuccess(JSONObject data) {
						JSONObject quota = data.optJSONObject("quota");
						if(quota != null){
							Long total = quota.optLong("total");
							Long inUse = quota.optLong("in_use");
							if(total != null && inUse != null){
								adv.setTotalSpace(total);
								adv.setSpaceInUse(inUse);
							}
						}
					}
					@Override
					public void onApiCallError(int code) {
					}
				});
				call2.call();
				
				Editor edit = adv.getSettings().edit();
				edit.putLong(AdvControl.lastUpdate, getTimestamp());
				edit.commit();
			}
		};
		t.start();
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}
	public class MyBinder extends Binder {
	    AdvService getService() {
	      return AdvService.this;
	    }
	  }
}
