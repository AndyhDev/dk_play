package com.dk.play;

import java.util.List;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.SearchView;
import android.widget.Toast;

import com.dk.play.database.SQLiteDataSource;
import com.dk.play.fragments.PlayerControlFragment;
import com.dk.play.fragments.SongListFragment;
import com.dk.play.util.NavDrawerFunc;
import com.dk.play.util.YesNoDlg;
import com.dk.play.util.YesNoDlgListener;

public class DkPlay extends Activity{
	private static final String TAG = "DkPlay";

	//private DrawerLayout drawer;
	//private ActionBarDrawerToggle drawerToggle;
	private SearchView searchView ;
	private MenuItem searchItem;
	private NavDrawerFunc navDrawerFunc;
	private SongListFragment frag1;
	private PlayerControlFragment frag2;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_dk_play);
		if(savedInstanceState == null){
			if(frag1 == null){
				frag1 = new SongListFragment();
				FragmentTransaction transaction1 = getFragmentManager().beginTransaction();
				transaction1.add(R.id.fragment1, frag1, "com.dk.play.fragments.SongListFragment");
				//transaction1.addToBackStack(null);
				transaction1.commit();
			}
			if(frag2 == null){
				frag2 = new PlayerControlFragment();
				FragmentTransaction transaction2 = getFragmentManager().beginTransaction();
				transaction2.add(R.id.fragment2, frag2, "com.dk.play.fragments.PlayerControlFragment");
				//transaction2.addToBackStack(null);
				transaction2.commit();
			}
		}else{
			FragmentManager fragmentManager = getFragmentManager();
			frag2 = (PlayerControlFragment) fragmentManager.findFragmentById(R.id.fragment2);
		}
		/*drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
		drawerToggle = new ActionBarDrawerToggle(this, drawer, R.drawable.ic_drawer, R.string.drawer_open, R.string.drawer_close);
		drawer.setDrawerListener(drawerToggle);
		getActionBar().setHomeButtonEnabled(true);
		getActionBar().setDisplayHomeAsUpEnabled(true);*/
		navDrawerFunc = new NavDrawerFunc(this);

		//Intent intent= new Intent(this, PlayService.class);
		//bindService(intent, mConnection, Context.BIND_AUTO_CREATE);

		if (isFirstLaunch()) {
			Intent firstLaunchIntent = new Intent(this, FirstLaunchActivity.class);
			firstLaunchIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivity(firstLaunchIntent);
			finish();
		}

	}

	@Override
	protected void onNewIntent(Intent intent) {
		Log.d(TAG, "onNewIntent:" + intent.getAction());
		if (Intent.ACTION_VIEW.equals(intent.getAction())){
			Uri uri = intent.getData();
			List<String> seg = uri.getPathSegments();
			if(seg.size() == 2){
				long id = Long.parseLong(seg.get(0));
				searchItem.collapseActionView();
				Log.d(TAG, "play=" + id);
				if(frag1 != null){
					frag1.playSongById(id);
					frag1.scrollToSong(id);
				}else{
					Log.e(TAG, "frag1 == null");
				}
				Log.d(TAG, "play=" + id);
			}
		}
	}
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(keyCode == KeyEvent.KEYCODE_BACK){
			if(frag2 != null){
				if(frag2.trySetOpenState(false)){
					return true;
				}
			}else{
				Log.d(TAG, "frag2 == null");
			}
		}       
		return super.onKeyDown(keyCode, event);
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
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.dk_play, menu);

		searchItem = menu.findItem(R.id.action_search_song);
		searchView = (SearchView) searchItem.getActionView();
		SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
		searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
		searchView.setIconifiedByDefault(false);
		
		int searchPlateId = getResources().getIdentifier("android:id/search_plate", null, null);
		View searchPlate = searchView.findViewById(searchPlateId);
		searchPlate.setBackgroundResource(R.drawable.searchview_underline);
		return true;
	}

	private boolean isFirstLaunch() {
		// Restore preferences
		SharedPreferences settings = getSharedPreferences("other", MODE_PRIVATE);
		boolean isFirstLaunch = settings.getBoolean("isFirstLaunch", true);
		return isFirstLaunch;
		//return true;
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if(navDrawerFunc.processClick(item)){
			return true;
		}
		Intent i;
		switch (item.getItemId()) {
		case R.id.action_search:
			i = new Intent(this, SearchActivity.class);
			startActivity(i);
			return true;
		case R.id.action_re_create:
			YesNoDlg dlg = new YesNoDlg(this, R.string.re_create_database, R.string.re_create_database_long);
			dlg.setListener(new YesNoDlgListener() {
				@Override
				public void onYes() {
					SQLiteDataSource data = new SQLiteDataSource(getApplicationContext());
					data.open();
					data.reCreateDatabase();
					data.close();
					Toast.makeText(getApplicationContext(), R.string.re_create_database_ok, Toast.LENGTH_LONG).show();
				}
				
				@Override
				public void onNo() {
					Toast.makeText(getApplicationContext(), R.string.action_canceled, Toast.LENGTH_LONG).show();
				}
			});
			dlg.show();
			
			return true;
		case R.id.action_removed_songs:
			i = new Intent(this, RemovedActivity.class);
			startActivity(i);
			return true;
		case R.id.action_settings:
			i = new Intent(this, SettingsActivity.class);
			startActivity(i);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}	
}
