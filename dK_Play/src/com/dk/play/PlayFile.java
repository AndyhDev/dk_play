package com.dk.play;

import java.io.File;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.MenuItem;

import com.dk.play.database.SQLSong;
import com.dk.play.database.SQLiteDataSource;
import com.dk.play.fragments.PlayerControlFragment;
import com.dk.play.service.PlayService;
import com.dk.play.util.ActionBarImage;
import com.dk.play.util.LActivity;
import com.dk.play.util.NavDrawerFunc;
import com.dk.play.util.SearchPlayable;

public class PlayFile extends LActivity {
	private static final String TAG = "PlayFile";
	
	@SuppressWarnings("unused")
	private ActionBarImage actionBarImage;
	private boolean useBgImages = false;
	private PlayService service;
	private SQLSong waitSong = null;
	private PlayerControlFragment frag2;
	private NavDrawerFunc navDrawerFunc;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		useBgImages = PreferenceManager.getDefaultSharedPreferences(this).getBoolean("use_bg_images", false);
		if(useBgImages){
			setTheme(R.style.AppTheme2);
		}
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_play_file);
		navDrawerFunc = new NavDrawerFunc(this);
		if(savedInstanceState == null){
			if(frag2 == null){
				frag2 = new PlayerControlFragment();
				frag2.setExpandable(false);
				frag2.setPrevOpenState(true);
				FragmentTransaction transaction2 = getFragmentManager().beginTransaction();
				transaction2.add(R.id.fragment2, frag2, "com.dk.play.fragments.PlayerControlFragment");
				transaction2.commit();
			}
		}else{
			FragmentManager fragmentManager = getFragmentManager();
			frag2 = (PlayerControlFragment) fragmentManager.findFragmentById(R.id.fragment2);
			frag2.setExpandable(false);
			frag2.setPrevOpenState(true);
		}
		
		Intent intent = getIntent();
		processIntent(intent);
		
		actionBarImage = new ActionBarImage(this);
	}

	@Override
	protected void onNewIntent(Intent intent) {
		processIntent(intent);
	}

	private void processIntent(Intent intent) {
		Log.d(TAG, "progressIntent");
		if(intent != null){
			Uri uri = intent.getData();
			if(uri != null){
				String path = uri.getPath();
				Log.d(TAG, "path: " + path);
				File f = new File(path);
				if(f.exists()){
					SQLiteDataSource dataSource = new SQLiteDataSource(this);
					dataSource.open();
					SQLSong song = dataSource.getSQLSong(path);
					dataSource.close();
					if(song != null){
						Log.d(TAG, "song != null");
						if(service != null){
							service.setSongBySQLSong(song, true);
						}else{
							waitSong = song;
						}
					}else{
						Log.d(TAG, "song == null");
						SearchPlayable search = new SearchPlayable(this);
						song = search.getSQLSong(path);
						if(song != null){
							Log.d(TAG, "song != null 2");
							if(service != null){
								service.setSongBySQLSong(song, true);
							}else{
								waitSong = song;
							}
						}
					}
				}
			}
			
		}
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if(navDrawerFunc.processClick(item)){
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		navDrawerFunc.onPostCreate();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		navDrawerFunc.onConfigurationChanged(newConfig);
	}
	@Override
	public void onResume() {
		super.onResume();
		Log.d(TAG, "onResume");
		if(service == null){
			Intent intent= new Intent(this, PlayService.class);
			startService(intent);
			bindService(intent, mConnection, Context.BIND_ABOVE_CLIENT);
		}
	}
	@Override
	public void onPause() {
		super.onPause();
		if(service != null){
			unbindService(mConnection);
			service = null;
		}
	}
	private ServiceConnection mConnection = new ServiceConnection() {

		public void onServiceConnected(ComponentName className, IBinder binder) {
			PlayService.MyBinder b = (PlayService.MyBinder) binder;
			service = b.getService();
			if(waitSong != null){
				service.setSongBySQLSong(waitSong, true);
				waitSong = null;
			}
		}

		public void onServiceDisconnected(ComponentName className) {
			service  = null;
		}
	};
}
