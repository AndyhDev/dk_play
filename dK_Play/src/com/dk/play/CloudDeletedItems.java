package com.dk.play;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.preference.PreferenceManager;

import com.dk.play.fragments.CloudDeletedFragment;
import com.dk.play.util.ActionBarImage;
import com.dk.play.util.LActivity;

public class CloudDeletedItems extends LActivity {
	@SuppressWarnings("unused")
	private ActionBarImage actionBarImage;
	private boolean useBgImages = false;
	private CloudDeletedFragment frag1;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		useBgImages = PreferenceManager.getDefaultSharedPreferences(this).getBoolean("use_bg_images", false);
		if(useBgImages){
			setTheme(R.style.AppTheme2);
		}
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_cloud_deleted_items);
		
		if(savedInstanceState == null){
			if(frag1 == null){
				frag1 = new CloudDeletedFragment();
				FragmentTransaction transaction1 = getFragmentManager().beginTransaction();
				transaction1.add(R.id.container1, frag1, "com.dk.play.fragments.CloudDeletedFragment ");
				transaction1.commit();
			}
		}else{
			FragmentManager fragmentManager = getFragmentManager();
			frag1 = (CloudDeletedFragment) fragmentManager.findFragmentById(R.id.container1);
			
		}
		actionBarImage = new ActionBarImage(this);
	}
}
