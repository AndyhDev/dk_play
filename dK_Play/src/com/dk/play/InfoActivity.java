package com.dk.play;

import android.app.ActionBar;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;

import com.dk.play.fragments.InfoFragment;
import com.dk.play.util.ActionBarImage;
import com.dk.play.util.LActivity;

public class InfoActivity extends LActivity {
	private InfoFragment frag1;

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
		setContentView(R.layout.activity_info);

		if (savedInstanceState == null) {
			frag1 = new InfoFragment();
			getFragmentManager().beginTransaction().add(R.id.container, frag1).commit();
		}
		ActionBar actionBar = getActionBar();

		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBarImage = new ActionBarImage(this);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
        case android.R.id.home:
            this.finish();
            return true;
        default:
            return super.onOptionsItemSelected(item);
    }
	}
	
}
