package com.dk.play;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.dk.play.fragments.MusicEditFragment;

public class MusicEditActivity extends Activity {
	private MusicEditFragment frag;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_music_edit);

		if (savedInstanceState == null) {
			frag = new MusicEditFragment();
			getFragmentManager().beginTransaction().add(R.id.container, frag).commit();
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.video_edit, menu);
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
