package com.dk.play.util;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.Notification.Builder;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.RemoteViews;

import com.dk.play.R;
import com.dk.play.adv.AdvControl;
import com.dk.play.adv.AdvReceiver;
import com.dk.play.adv.AdvSQLiteDataSource;
import com.dk.play.adv.AdvSong;
import com.dk.play.database.SQLSong;
import com.dk.play.database.SQLSongList;
import com.dk.play.database.SQLiteDataSource;
import com.dk.play.service.AdvService;
import com.dk.util.CancelThread;
import com.dk.util.Hash;
import com.dk.util.network.ApiCallListener;
import com.dk.util.network.dKApiCall;
import com.dk.util.network.dKSession;

public class AdvActLibrary2 implements AdvUploadSongListener {
	private static final String TAG = "AdvActLibrary";
	
	//private AdvService service;
	private Context context;
	private dKSession session;
	private ConnectivityManager iNet;
	private boolean active = false;
	private SQLiteDataSource dataSource;
	private AdvSQLiteDataSource advDataSource;
	private SQLSongList allSongs;
	private SharedPreferences settings;
	private SharedPreferences sOther;
	private int updateInterval;
	private boolean uploadOnlyOnWifi;
	private AdvControl adv;
	private String notTrack = Paths.getAdvSongDir().getAbsolutePath();
	private boolean skip = true;
	private boolean syncMode = false;
	private MD5FileCache md5Cache;
	private AdvUploadSong2 upload;
	private AdvService service;
	private static final int notifyId = 23;
	@SuppressLint("UseSparseArrays")
	private Map<Long, Bitmap> coverCache = new HashMap<Long, Bitmap>();
	
	public static final String lastIndex = "last_index";
	private CancelThread worker;
	
	private UploadCancel remoteControlReceiver;
	
	public AdvActLibrary2(Context context, AdvService service){
		//this.service = service;
	    this.context = context;
	    this.service = service;
	    
		adv = new AdvControl(context);
		iNet = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
		dataSource = new SQLiteDataSource(context);
		advDataSource = new AdvSQLiteDataSource(context);
		settings = PreferenceManager.getDefaultSharedPreferences(context);
		settings.registerOnSharedPreferenceChangeListener(settingsListener);
		
		sOther = context.getSharedPreferences("other", Context.MODE_PRIVATE);
		
		updateInterval = Integer.valueOf(settings.getString("adv_update_interval", "120"));
		uploadOnlyOnWifi = settings.getBoolean("update_only_on_wifi", true);
		
		md5Cache = new MD5FileCache();
		
	}
	private void makeNotify(SQLSong song, int progress) {
		Bitmap icon;
		if(coverCache.containsKey(song.getId())){
			icon = coverCache.get(song.getId());
		}else{
			File cover = new File(song.getCoverUri().getPath());
			if(cover.exists()){
				icon = Image.decodeSquareBitmapFromPath(cover.getAbsolutePath(), 200);
			}else{
				icon = Image.decodeSampledBitmapFromResource(context.getResources(), R.drawable.default_cover, 200, 200);
			}
			coverCache.put(song.getId(), icon);
		}
		Intent cancelIntent = new Intent(context, AdvReceiver.class);
		cancelIntent.setAction(UploadCancel.ACTION_CANCEL);
		PendingIntent pCancelIntent = PendingIntent.getBroadcast(context, 0, cancelIntent, PendingIntent.FLAG_UPDATE_CURRENT);

		Builder builder;
		RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.adv_upload_notify);
		remoteViews.setOnClickPendingIntent(R.id.cancel, pCancelIntent);
		remoteViews.setImageViewBitmap(R.id.cover, icon);
		remoteViews.setTextViewText(R.id.title, song.getTitle());
		if(progress == -1){
			remoteViews.setProgressBar(R.id.progress, 0, 0, true);
		}else{
			remoteViews.setProgressBar(R.id.progress, 100, progress, false);
		}

		builder  = new Notification.Builder(context);
		builder.setContent(remoteViews); 
		builder.setSmallIcon(R.drawable.ic_upload);

		@SuppressWarnings("deprecation")
		Notification n = builder.getNotification();
		service.startForeground(notifyId, n);
	}
	SharedPreferences.OnSharedPreferenceChangeListener settingsListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
		public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
			updateInterval = Integer.valueOf(prefs.getString("adv_update_interval", "120"));
			uploadOnlyOnWifi = prefs.getBoolean("update_only_on_wifi", true);
		}
	};
	private void run(){
		if(syncMode){
			session = adv.getSession();
			if(session != null){
				actLibrary();
			}
		}else{
			worker = new CancelThread(){
				@Override
				public void run() {
					remoteControlReceiver = new UploadCancel();

					IntentFilter intentFilter = new IntentFilter(UploadCancel.REMOTE_CONTROL);
					context.registerReceiver(remoteControlReceiver, intentFilter);
					
					session = adv.getSession();
					if(session != null){
						actLibrary();
					}
				}
			};
			
			worker.start();
		}
	}
	
	public class UploadCancel extends BroadcastReceiver{
		private static final String TAG = "UploadCancel";

		public static final String REMOTE_CONTROL = "ADV_CONTROL";
		public static final String ACTION = "ACTION";
		public static final String ACTION_CANCEL = "action_cancel";
		
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getStringExtra(ACTION);
			Log.d(TAG, "action=" + action);
			if(action.equals(ACTION_CANCEL)){
				cancel();
			}
		}
	}
	
	private void actLibrary(){
		dataSource.open();
		allSongs = dataSource.getSQLSongList();
		dataSource.close();
		
		int index = sOther.getInt(lastIndex, 0);
		if(index >= allSongs.size()-1){
			index = 0;
		}
		advDataSource.open();
		SQLSong song;
		for(int i = index; i < allSongs.size(); i++){
			Log.d(TAG, "canceled:" + worker.getCanceled());
			if(worker.getCanceled()){
				break;
			}
			
			song = allSongs.get(i);
			Log.d(TAG, "action4 = " + song.getTitle());
			makeNotify(song, -1);
			if(canUpload(song)){
				Log.d(TAG, "action upload");
				upload(song);
			}else{
				Log.d(TAG, "action no upload");
			}
		}
		advDataSource.close();
		
		Editor edit = adv.getSettings().edit();
		edit.putLong(AdvControl.lastUpdate, System.currentTimeMillis()/1000);
		edit.commit();
		
		service.stopForeground(true);

	}
	private void upload(final SQLSong song){
		dKApiCall call = new dKApiCall(session, "adv_get_song_state");
		upload = new AdvUploadSong2(song, session);
		
		final String serverId = md5Cache.getMd5(song.getPath());
		
		call.addParam(AdvUploadSong2.songId, serverId);
		call.addParam(AdvUploadSong2.songTime, String.valueOf(song.getTime()));
		call.addParam(AdvUploadSong2.songFile, md5Cache.getMd5(song.getPath()));
		if(song.getCover().equals("no")){
			call.addParam(AdvUploadSong2.songCover, Hash.getMd5("no"));
		}else{
			call.addParam(AdvUploadSong2.songCover, md5Cache.getMd5(song.getCoverUri().getPath()));
		}
		call.addParam(AdvUploadSong2.songAttr, upload.getAttrMD5());
		call.setSyncModus(true);
		call.setListener(new ApiCallListener() {
			@Override
			public void onApiCallSuccess(JSONObject data) {
				String action = data.optString("action");
				if(action != null){
					Log.d(TAG, "action = " + action);
					Log.d(TAG, "action2 = " + song.getTitle());
					Log.d(TAG, "action3 = " + md5Cache.getMd5(song.getPath()));
					if(action.equals("full")){
						//upload.setUploadParts(true, true, true);
						runUpload(true, true, true, song, serverId);
					}else if(action.equals("part")){
						boolean upFile = false;
						boolean upCover = false;
						boolean upAttr = false;
						JSONObject parts = data.optJSONObject("parts");
						if(parts != null){
							if(parts.optInt("file") == 1){
								upFile = true;
							}	
							if(parts.optInt("cover") == 1){
								upCover = true;
							}
							if(parts.optInt("attr") == 1){
								upAttr = true;
							}
							
							runUpload(upFile, upCover, upAttr, song, serverId);
						}
					}
				}
			}
			
			@Override
			public void onApiCallError(int code) {
			}
		});
		call.call();
	}
	protected void runUpload(boolean upFile, boolean upCover, boolean upAttr, SQLSong song, String serverId) {
		Log.d(TAG, "upload 1");
		upload.setListener(this);
		Log.d(TAG, "upload 2");
		int resp = upload.run(upFile, upCover, upAttr);
		Log.d(TAG, "upload 3");
		Log.d(TAG, "upload 4" + resp);
		if(resp == AdvUploadSong2.NO_ERROR){
			updateAdvSong(song, serverId, AdvSong.STATE_OK);
		}
	}
	private boolean canUpload(SQLSong song){
		if(!checkAdv()){
			return false;
		}
		if(advDataSource.isRemoved(song.getId())){
			return false;
		}
		AdvSong advSong = advDataSource.getAdvSongFromSQLSong(song);
		if(advSong != null){
			long time = ((System.currentTimeMillis()/1000) - advSong.getLastCheck())/60;
			if(time < updateInterval && skip){
				return false;
			}
		}
		if(song.getPath().startsWith(notTrack)){
			return false;
		}
		return true;
	}
	public void updateAdvSong(SQLSong song, String serverId, int state){
		AdvSong advSong = advDataSource.getAdvSongFromSQLSong(song.getId());
		if(advSong == null){
			advSong = new AdvSong(serverId, song.getId(), state, System.currentTimeMillis()/1000);
			advDataSource.addAdvSong(advSong);
		}else{
			advSong.setLastCheck(System.currentTimeMillis()/1000);
			advDataSource.updateAdvSong(advSong);
		}
		
	}
	private boolean checkAdv(){
		SharedPreferences settings = adv.getSettings();
		NetworkInfo info = iNet.getActiveNetworkInfo();
		if(info == null){
			return false;
		}
		if(!uploadOnlyOnWifi){
			if(info.isConnected()){
				return settings.getBoolean(AdvControl.activeKey, true);
			}
		}
		int type = info.getType();
		if(type == ConnectivityManager.TYPE_WIFI){
			if(info.isConnected()){
				return settings.getBoolean(AdvControl.activeKey, true);
			}
		}
		return false;
	}
	public boolean isActive(){
		return active;
	}
	public void start(){
		if(!active){
			skip = true;
			active = true;
			run();
		}
	}
	public void startByPass(){
		if(!active){
			skip = false;
			active = true;
			run();
		}
	}
	
	public boolean getSkip() {
		return skip;
	}
	public void setSkip(boolean skip) {
		this.skip = skip;
	}
	public boolean getSyncMode() {
		return syncMode;
	}
	public void setSyncMode(boolean syncMode) {
		this.syncMode = syncMode;
	}
	@Override
	public void onProgress(int progress) {
		makeNotify(upload.getSQLSong(), progress);
	}
	public void cancel() {
		if(upload != null){
			upload.cancel();	
		}else{
			Log.d(TAG, "upload == null");
		}
		if(worker != null){
			Log.d(TAG, "canceled2:" + worker.getCanceled());
			worker.cancel();
			Log.d(TAG, "canceled2:" + worker.getCanceled());
		}else{
			Log.d(TAG, "worker == null");
		}
	}
	
}
