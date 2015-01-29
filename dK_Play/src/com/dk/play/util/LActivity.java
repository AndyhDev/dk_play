package com.dk.play.util;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Window;

import com.dk.play.R;

public class LActivity extends Activity{
	
	private Window window;
	private int statusBarColor = -1;
	private int navBarColor = -1;
	private boolean statusBarColoring = true;
	private boolean navBarColoring = true;
	private boolean statusBarSameColor = true;
	private boolean navBarSameColor = true;
	
	private int orange;
	private int defaultColor;
	
	private ProgressDialog progressDlg;
	private SharedPreferences pref;
	
	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		if(android.os.Build.VERSION.SDK_INT >= 21){
			orange = getResources().getColor(R.color.orange_dark);
			defaultColor = Resources.getSystem().getColor(android.R.color.background_dark);
			window = getWindow();
			
			pref = PreferenceManager.getDefaultSharedPreferences(this);
			pref.registerOnSharedPreferenceChangeListener(listener);
			
			readSettings();
			setColors();
		}
	}
	private OnSharedPreferenceChangeListener listener = new OnSharedPreferenceChangeListener() {
		
		@Override
		public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
			readSettings();
			setColors();
		}
	};
	
	public void showProgress(){
		showProgress(getString(R.string.please_wait));
	}
	public void showProgress(int resId){
		showProgress(getString(resId));
	}
	public void showProgress(String msg){
		progressDlg = new ProgressDialog(this);
		progressDlg.setTitle("");
		progressDlg.setMessage(msg);
		progressDlg.setCancelable(false);
		progressDlg.setIndeterminate(true);
		progressDlg.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		progressDlg.show();
	}
	public void hideProgress(){
		if(progressDlg != null){
			progressDlg.dismiss();
		}
	}
	private void readSettings() {
		statusBarColoring = pref.getBoolean("status_bar_coloring", true);
		navBarColoring = pref.getBoolean("nav_bar_coloring", true);
		
		statusBarSameColor = pref.getBoolean("status_bar_same_color", true);
		navBarSameColor = pref.getBoolean("nav_bar_same_color", true);
		
		if(statusBarSameColor){
			statusBarColor = orange;
		}else{
			statusBarColor = pref.getInt("status_bar_color", orange);
		}
		if(navBarSameColor){
			navBarColor = orange;
		}else{
			navBarColor = pref.getInt("nav_bar_color", orange);
		}
	}
	@SuppressLint("NewApi")
	private void setColors(){
		if(android.os.Build.VERSION.SDK_INT >= 21){
			if(statusBarColoring && statusBarColor != -1){
				window.setStatusBarColor(statusBarColor);
			}else{
				window.setStatusBarColor(defaultColor);
			}
			if(navBarColoring && navBarColor != -1){
				window.setNavigationBarColor(navBarColor);
			}else{
				window.setNavigationBarColor(defaultColor);
			}
		}
	}
	
}
