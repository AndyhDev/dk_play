package com.dk.play;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;

import com.dk.play.fragments.MusicEditFragment;
import com.dk.play.util.ActionBarImage;
import com.dk.play.util.LActivity;

public class MusicEditActivity extends LActivity {
	private MusicEditFragment frag;

	@SuppressWarnings("unused")
	private ActionBarImage actionBarImage;
	private boolean useBgImages = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		useBgImages = PreferenceManager.getDefaultSharedPreferences(this).getBoolean("use_bg_images", false);
		if(useBgImages){
			setTheme(R.style.AppTheme2);
		}
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_music_edit);

		if (savedInstanceState == null) {
			frag = new MusicEditFragment();
			getFragmentManager().beginTransaction().add(R.id.container, frag).commit();
		}
		actionBarImage = new ActionBarImage(this);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		if(useBgImages){
			getMenuInflater().inflate(R.menu.video_edit_l, menu);
		}else{
			getMenuInflater().inflate(R.menu.video_edit, menu);
		}
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == R.id.save) {
			frag.save();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
