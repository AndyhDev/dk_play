package com.dk.play;

import android.app.Dialog;
import android.app.FragmentManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.dk.play.fragments.SearchFragment;
import com.dk.play.util.ActionBarImage;
import com.dk.play.util.LActivity;
import com.dk.play.util.SelectSearchFoldersDlg;

public class SearchActivity extends LActivity {
	@SuppressWarnings("unused")
	private ActionBarImage actionBarImage;
	private boolean useBgImages = false;
	private Dialog dlg;
	private SearchFragment frag;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		useBgImages = PreferenceManager.getDefaultSharedPreferences(this).getBoolean("use_bg_images", false);
		if(useBgImages){
			setTheme(R.style.AppTheme2);
		}
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_search);

		if (savedInstanceState == null) {
			frag = new SearchFragment();
			getFragmentManager().beginTransaction().add(R.id.container, frag).commit();
		}else{
			FragmentManager fragmentManager = getFragmentManager();
			frag = (SearchFragment) fragmentManager.findFragmentById(R.id.container);
		}
		actionBarImage = new ActionBarImage(this);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		MenuInflater inflater = getMenuInflater();
		if(useBgImages){
			inflater.inflate(R.menu.search_l, menu);
		}else{
			inflater.inflate(R.menu.search, menu);
		}

		return true;

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == R.id.folder_select) {
			dlg = new SelectSearchFoldersDlg(this);
			dlg.show();
		}else if(id == R.id.start_search){
			frag.start();
		}
		return super.onOptionsItemSelected(item);
	}
}
