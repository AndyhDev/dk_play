package com.dk.play;

import android.app.Activity;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.dk.play.fragments.RemovedFragment;
import com.dk.play.util.NavDrawerFunc;

public class RemovedActivity extends Activity {
	private NavDrawerFunc navDrawerFunc;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_removed);
		navDrawerFunc = new NavDrawerFunc(this);
		
		if (savedInstanceState == null) {
			getFragmentManager().beginTransaction().add(R.id.container, new RemovedFragment()).commit();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		return true;
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
	public boolean onOptionsItemSelected(MenuItem item) {
		if(navDrawerFunc.processClick(item)){
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

}
