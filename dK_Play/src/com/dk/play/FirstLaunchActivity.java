package com.dk.play;

import android.app.Activity;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.dk.play.fragments.FirstLaunchFragment;

public class FirstLaunchActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_first_launch);

		if (savedInstanceState == null) {
			getFragmentManager().beginTransaction().add(R.id.container, new FirstLaunchFragment()).commit();
		}
		
		Editor settings = getSharedPreferences("other", MODE_PRIVATE).edit();
		settings.putBoolean("isFirstLaunch", false);
		settings.commit();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		return super.onOptionsItemSelected(item);
	}
}
