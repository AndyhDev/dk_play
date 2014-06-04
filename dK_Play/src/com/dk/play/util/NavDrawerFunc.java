package com.dk.play.util;

import android.app.Activity;
import android.content.res.Configuration;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.view.MenuItem;

import com.dk.play.R;

public class NavDrawerFunc {
	@SuppressWarnings("unused")
	private Activity activity;
	private DrawerLayout drawer;
	private ActionBarDrawerToggle drawerToggle;
	
	public NavDrawerFunc(Activity activity){
		this.activity = activity;
		drawer = (DrawerLayout) activity.findViewById(R.id.drawer_layout);
		drawerToggle = new ActionBarDrawerToggle(activity, drawer, R.drawable.ic_drawer, R.string.drawer_open, R.string.drawer_close);
		drawer.setDrawerListener(drawerToggle);
		
		activity.getActionBar().setHomeButtonEnabled(true);
		activity.getActionBar().setDisplayHomeAsUpEnabled(true);
	}
	
	public Boolean processClick(MenuItem item){
		if(drawerToggle.onOptionsItemSelected(item)){
			return true;
		}
		return false;
	}
	public void onPostCreate(){
		drawerToggle.syncState();
	}
	public void onConfigurationChanged(Configuration newConfig){
		drawerToggle.onConfigurationChanged(newConfig);
	}
}
