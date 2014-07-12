package com.dk.play;

import android.app.Activity;
import android.app.FragmentManager;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;

import com.dk.play.fragments.AutoPlaylistFragment;
import com.dk.play.fragments.PlayerControlFragment;
import com.dk.play.util.NavDrawerFunc;

public class AutoPlaylistActivity extends Activity {
	private NavDrawerFunc navDrawerFunc;
	private AutoPlaylistFragment frag1;
	private PlayerControlFragment frag2;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_auto_playlist);
		navDrawerFunc = new NavDrawerFunc(this);
		
		if (savedInstanceState == null) {
			frag1 = new AutoPlaylistFragment();
			getFragmentManager().beginTransaction().add(R.id.container, frag1).commit();
		}
		if (savedInstanceState == null) {
			frag2 = new PlayerControlFragment();
			getFragmentManager().beginTransaction().add(R.id.container2, frag2).commit();
		}else{
			FragmentManager fragmentManager = getFragmentManager();
			frag2 = (PlayerControlFragment) fragmentManager.findFragmentById(R.id.container2);
		}
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
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(keyCode == KeyEvent.KEYCODE_BACK){
			if(frag2 != null){
				if(frag2.trySetOpenState(false)){
					return true;
				}
			}
		}       
		return super.onKeyDown(keyCode, event);
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if(navDrawerFunc.processClick(item)){
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
