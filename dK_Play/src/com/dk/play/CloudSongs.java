package com.dk.play;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.Window;
import android.widget.SearchView;
import android.widget.SearchView.OnQueryTextListener;
import android.widget.SearchView.OnSuggestionListener;

import com.dk.play.fragments.CloudSongsFragment;
import com.dk.play.fragments.PlayerControlFragment;
import com.dk.play.service.AdvService;
import com.dk.play.util.ActionBarImage;
import com.dk.play.util.CloudAdapter;
import com.dk.play.util.CloudList;
import com.dk.play.util.LActivity;
import com.dk.play.util.NavDrawerFunc;
import com.dk.util.TextSearchAdapter;

public class CloudSongs extends LActivity {
	@SuppressWarnings("unused")
	private static final String TAG = "CloudSongs";

	private NavDrawerFunc navDrawerFunc;
	private CloudSongsFragment frag1;
	private PlayerControlFragment frag2;

	@SuppressWarnings("unused")
	private ActionBarImage actionBarImage;
	private boolean useBgImages = false;

	private SearchView search;

	private TextSearchAdapter searchAdaper;

	protected CloudList cList;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		useBgImages = PreferenceManager.getDefaultSharedPreferences(this).getBoolean("use_bg_images", false);
		if(useBgImages){
			setTheme(R.style.AppTheme2);
		}
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_cloud_songs);
		navDrawerFunc = new NavDrawerFunc(this);
		if(savedInstanceState == null){
			if(frag1 == null){
				frag1 = new CloudSongsFragment();
				FragmentTransaction transaction1 = getFragmentManager().beginTransaction();
				transaction1.add(R.id.fragment1, frag1, "com.dk.play.fragments.CoudSongsFragment");
				transaction1.commit();
			}
			if(frag2 == null){
				frag2 = new PlayerControlFragment();
				FragmentTransaction transaction2 = getFragmentManager().beginTransaction();
				transaction2.add(R.id.fragment2, frag2, "com.dk.play.fragments.PlayerControlFragment");
				transaction2.commit();
			}
		}else{
			FragmentManager fragmentManager = getFragmentManager();
			frag1 = (CloudSongsFragment) fragmentManager.findFragmentById(R.id.fragment1);

			frag2 = (PlayerControlFragment) fragmentManager.findFragmentById(R.id.fragment2);
		}
		actionBarImage = new ActionBarImage(this);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		if(useBgImages){
			inflater.inflate(R.menu.cloud_songs_l, menu);
		}else{
			inflater.inflate(R.menu.cloud_songs, menu);
		}
		SearchManager manager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);

		search = (SearchView) menu.findItem(R.id.action_search_song).getActionView();
		final MenuItem searchItem = menu.findItem(R.id.action_search_song);

		search.setSearchableInfo(manager.getSearchableInfo(getComponentName()));
		
		searchItem.setOnMenuItemClickListener(new OnMenuItemClickListener() {
			
			@Override
			public boolean onMenuItemClick(MenuItem item) {
				if(frag1 != null){
					CloudAdapter adt = frag1.getAdapter();
					cList = adt.getList();
					searchAdaper = new TextSearchAdapter.Builder(CloudSongs.this, cList.getNames()).build();
					search.setSuggestionsAdapter(searchAdaper);
				}
				return false;
			}
		});
		search.setOnQueryTextListener(new OnQueryTextListener() { 

			@Override 
			public boolean onQueryTextChange(String query) {
				if(searchAdaper != null){
					searchAdaper.setQuery(query);
				}
				return true; 
			}

			@Override
			public boolean onQueryTextSubmit(String query) {
				searchItem.collapseActionView();
				return true;
			} 

		});
		search.setOnSuggestionListener(new OnSuggestionListener() {
			
			@Override
			public boolean onSuggestionSelect(int position) {
				return false;
			}
			
			@Override
			public boolean onSuggestionClick(int position) {
				if(frag1 != null){
					frag1.activateItem(searchAdaper.getOrginalPosition(position));
				}
				searchItem.collapseActionView();
				return true;
			}
		});
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if(navDrawerFunc.processClick(item)){
			return true;
		}
		int id = item.getItemId();
		if (id == R.id.refresh) {
			if(frag1 != null){
				frag1.readCloud();
			}
		}else if(id == R.id.deleted_items){
			Intent intent = new Intent(this, CloudDeletedItems.class);
			startActivity(intent);
		}else if(id == R.id.cloud_sync_songs){
			Intent advIntent = new Intent(this, AdvService.class); 
			advIntent.setAction(AdvService.ACTION_BYPASS);
			startService(advIntent);
		}else if(id == R.id.adv){
			startAdv();
		}
		return super.onOptionsItemSelected(item);
	}
	public void startAdv(){
		Intent i = new Intent(this, Adv.class);
		startActivity(i);
	}
	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		navDrawerFunc.onPostCreate();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		navDrawerFunc.onConfigurationChanged(newConfig);
	}
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(keyCode == KeyEvent.KEYCODE_BACK){
			if(frag2 != null){
				if(frag2.trySetOpenState(false)){
					return true;
				}
			}
		}       
		return super.onKeyDown(keyCode, event);
	}
}
