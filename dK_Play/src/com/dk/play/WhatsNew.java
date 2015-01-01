package com.dk.play;

import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.preference.PreferenceManager;

import com.dk.play.util.ActionBarImage;
import com.dk.play.util.LActivity;

public class WhatsNew extends LActivity {

	@SuppressWarnings("unused")
	private ActionBarImage actionBarImage;
	private boolean useBgImages;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		useBgImages = PreferenceManager.getDefaultSharedPreferences(this).getBoolean("use_bg_images", false);
		if(useBgImages){
			setTheme(R.style.AppTheme2);
		}
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_whats_new);
		getActionBar().setTitle(R.string.whats_new);
		
		actionBarImage = new ActionBarImage(this);
		
		Editor settings = getSharedPreferences("other", MODE_PRIVATE).edit();
		try {
			PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
			int cur_version = pInfo.versionCode;
			settings.putInt("version", cur_version);
			settings.commit();
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		
	}
}
