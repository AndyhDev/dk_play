package com.dk.play;

import android.app.ActionBar;
import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.dk.play.fragments.InfoFragment;

public class InfoActivity extends Activity {
	private InfoFragment frag1;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_info);

		if (savedInstanceState == null) {
			frag1 = new InfoFragment();
			getFragmentManager().beginTransaction().add(R.id.container, frag1).commit();
		}
		ActionBar actionBar = getActionBar();

		actionBar.setDisplayHomeAsUpEnabled(true);
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
