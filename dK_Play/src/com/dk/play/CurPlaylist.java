package com.dk.play;

import java.util.List;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.SearchView;
import android.widget.SearchView.OnQueryTextListener;
import android.widget.SearchView.OnSuggestionListener;

import com.dk.play.database.SQLSong;
import com.dk.play.database.SQLSongList;
import com.dk.play.fragments.CurPlaylistFragment;
import com.dk.play.fragments.PlayerControlFragment;
import com.dk.play.util.NavDrawerFunc;
import com.dk.play.util.SaveCurrentPlaylistDlg;
import com.dk.play.util.SearchAdapter;

public class CurPlaylist extends Activity implements OnQueryTextListener, OnSuggestionListener {
	private static final String TAG = "CurPlaylist";

	private NavDrawerFunc navDrawerFunc;
	private CurPlaylistFragment frag1;
	private PlayerControlFragment frag2;

	private SearchView searchView ;
	private MenuItem searchItem;
	private SearchAdapter searchAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_cur_playlist);
		navDrawerFunc = new NavDrawerFunc(this);
		if(savedInstanceState == null){
			if(frag1 == null){
				frag1 = new CurPlaylistFragment();
				FragmentTransaction transaction1 = getFragmentManager().beginTransaction();
				transaction1.add(R.id.fragment1, frag1, "com.dk.play.fragments.CurPlaylistFragment");
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
		String[] columnNames = {"_id", "title", "artist", "cover"};
		MatrixCursor cursor = new MatrixCursor(columnNames);

		String[] from = {"title", "artist"}; 
		int[] to = {R.id.item_title, R.id.item_artist};
		//searchAdapter = new SimpleCursorAdapter(this, R.layout.search_list_item, cursor, from, to);
		searchAdapter = new SearchAdapter(this, R.layout.search_list_item, cursor, new SQLSongList(), from, to);

	}
	@Override
	protected void onNewIntent(Intent intent) {
		Log.d(TAG, "onNewIntent:" + intent.getAction());
		if (Intent.ACTION_VIEW.equals(intent.getAction())){
			Uri uri = intent.getData();
			List<String> seg = uri.getPathSegments();
			if(seg.size() == 2){
				long id = Long.parseLong(seg.get(0));
				Log.d(TAG, "play=" + id);
				frag1.playSongById(id);
				Log.d(TAG, "play=" + id);
				searchItem.collapseActionView();
			}
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.cur_playlist, menu);

		searchItem = menu.findItem(R.id.action_search_song);
		searchView = (SearchView) searchItem.getActionView();
		//SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
		/*searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
		searchView.setIconifiedByDefault(false);*/
		searchView.setOnQueryTextListener(this);
		searchView.setSuggestionsAdapter(searchAdapter);
		searchView.setOnSuggestionListener(this);
		
		int searchPlateId = getResources().getIdentifier("android:id/search_plate", null, null);
		View searchPlate = searchView.findViewById(searchPlateId);
		searchPlate.setBackgroundResource(R.drawable.searchview_underline);
		
		return true;
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
	public boolean onOptionsItemSelected(MenuItem item) {
		if(navDrawerFunc.processClick(item)){
			return true;
		}
		if(item.getItemId() == R.id.save){
			SaveCurrentPlaylistDlg dlg = new SaveCurrentPlaylistDlg(this, frag1.getPlaylist().toPlaylist());
			dlg.show();
		}
		return super.onOptionsItemSelected(item);
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
	@Override
	public boolean onQueryTextSubmit(String query) {
		Log.d(TAG, "query=" + query);
		return false;
	}
	@Override
	public boolean onQueryTextChange(String newText) {
		Log.d(TAG, "newText=" + newText);

		SQLSongList list = frag1.getPlaylist();
		SQLSongList matches = list.search(newText);
		Log.d(TAG, "matches=" + matches.size());

		String[] columnNames = {"_id", "title", "artist", "cover"};
		MatrixCursor cursor = new MatrixCursor(columnNames);
		for(int i = 0; i < matches.size(); i++){
			SQLSong song = matches.get(i);
			cursor.addRow(new String[]{String.valueOf(song.getId()), song.getTitle(), song.getArtist(), song.getCover()});
		}
		searchAdapter.changeSongList(matches);
		searchAdapter.changeCursor(cursor);
		searchAdapter.notifyDataSetChanged();
		return true;
	}
	@Override
	public boolean onSuggestionSelect(int position) {
		return false;
	}
	@Override
	public boolean onSuggestionClick(int position) {
		SQLSong song = searchAdapter.getSongList().get(position);
		frag1.playSongById(song.getId());
		searchItem.collapseActionView();
		return true;
	}
}
