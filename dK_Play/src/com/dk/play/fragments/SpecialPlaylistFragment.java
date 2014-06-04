package com.dk.play.fragments;

import android.app.Fragment;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.IBinder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.dk.play.R;
import com.dk.play.SpecialPlaylist;
import com.dk.play.database.SQLSong;
import com.dk.play.database.SQLSongList;
import com.dk.play.database.SQLiteDataSource;
import com.dk.play.database.SQLiteHelper;
import com.dk.play.service.PlayService;
import com.dk.play.util.SongAdapter;
import com.dk.play.util.SongViewHolder;

public class SpecialPlaylistFragment extends Fragment {
	@SuppressWarnings("unused")
	private static final String TAG = "SpecialPlaylistFragment";

	private RelativeLayout layout;
	private PlayService service;
	private Context context;
	private SQLSongList playlist;
	private ListView songView;
	private SongAdapter songAdt;
	private int specialId;
	private String playlistName;
	private SQLiteDataSource datasource;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
		context = this.getActivity();

		Intent intent = getActivity().getIntent();
		if(intent.getAction() == SpecialPlaylist.PLAY){
			specialId = intent.getIntExtra(SpecialPlaylist.PLAYLIST_ID,	R.id.playlist_main);
		}
		layout = (RelativeLayout)inflater.inflate(R.layout.special_playlist_fragment, container, false);
		songView = (ListView)layout.findViewById(R.id.special_list);

		readSongList();
		songAdt = new SongAdapter(this.getActivity(), playlist);
		songView.setAdapter(songAdt);

		songView.setOnItemClickListener(new OnItemClickListener(){
			@Override 
			public void onItemClick(AdapterView<?> arg0, View view,int position, long arg3){
				SongViewHolder viewHolder = (SongViewHolder)view.getTag();
				SQLSong song = viewHolder.song;
				if(service != null){
					if(!service.getPlayListName().equals(playlistName)){
						service.setSQLSongList(playlist);
						service.setPlayListName(playlistName);
					}
					service.setSongById(song.getId());
					service.processClick(song);
				}
			}
		});

		return layout;
	}

	private void readSongList() {
		datasource = new SQLiteDataSource(context);
		datasource.open();
		Cursor cursor = null;
		String limit = "25";

		if(specialId == R.id.playlist_main){
			playlist = datasource.getSQLSongList();
			playlistName = "main";
			datasource.close();
			return;

		}else if(specialId == R.id.playlist_most){
			playlistName = "most";
			SQLiteDatabase database = datasource.getDatabase();
			String order = SQLiteHelper.COLUMN_PLAY_COUNT + " DESC";
			cursor = database.query(SQLiteHelper.TABLE_SONGS, datasource.allSongsColumns, null, null, null, null, order, limit);

		}else if(specialId == R.id.playlist_date){
			playlistName = "date";
			SQLiteDatabase database = datasource.getDatabase();
			String order = SQLiteHelper.COLUMN_TIME + " DESC";
			cursor = database.query(SQLiteHelper.TABLE_SONGS, datasource.allSongsColumns, null, null, null, null, order, limit);

		}else if(specialId == R.id.playlist_click){
			playlistName = "click";
			SQLiteDatabase database = datasource.getDatabase();
			String order = SQLiteHelper.COLUMN_CLICK + " DESC";
			cursor = database.query(SQLiteHelper.TABLE_SONGS, datasource.allSongsColumns, null, null, null, null, order, limit);

		}else if(specialId == R.id.playlist_r5){
			playlistName = "r5";
			SQLiteDatabase database = datasource.getDatabase();
			cursor = database.query(SQLiteHelper.TABLE_SONGS, datasource.allSongsColumns, SQLiteHelper.COLUMN_RATING + "=?", new String[]{"5"}, null, null, null, limit);

		}else if(specialId == R.id.playlist_r4){
			playlistName = "r4";
			SQLiteDatabase database = datasource.getDatabase();
			cursor = database.query(SQLiteHelper.TABLE_SONGS, datasource.allSongsColumns, SQLiteHelper.COLUMN_RATING + "=?", new String[]{"4"}, null, null, null, limit);

		}else if(specialId == R.id.playlist_r3){
			playlistName = "r3";
			SQLiteDatabase database = datasource.getDatabase();
			cursor = database.query(SQLiteHelper.TABLE_SONGS, datasource.allSongsColumns, SQLiteHelper.COLUMN_RATING + "=?", new String[]{"3"}, null, null, null, limit);

		}else if(specialId == R.id.playlist_r2){
			playlistName = "r2";
			SQLiteDatabase database = datasource.getDatabase();
			cursor = database.query(SQLiteHelper.TABLE_SONGS, datasource.allSongsColumns, SQLiteHelper.COLUMN_RATING + "=?", new String[]{"2"}, null, null, null, limit);

		}else if(specialId == R.id.playlist_r1){
			playlistName = "r1";
			SQLiteDatabase database = datasource.getDatabase();
			cursor = database.query(SQLiteHelper.TABLE_SONGS, datasource.allSongsColumns, SQLiteHelper.COLUMN_RATING + "=?", new String[]{"1"}, null, null, null, limit);
		}
		if(cursor != null){
			SQLSongList list = new SQLSongList();
			cursor.moveToFirst();
			while (!cursor.isAfterLast()) {
				SQLSong song = new SQLSong(cursor);
				list.add(song);
				cursor.moveToNext();
			}
			cursor.close();
			playlist = list;
		}
		datasource.close();
	}
	@Override
	public void onResume() {
		super.onResume();
		if(service == null){
			Intent intent= new Intent(this.getActivity(), PlayService.class);
			context.startService(intent);
			context.bindService(intent, mConnection, Context.BIND_ABOVE_CLIENT);
		}
	}
	@Override
	public void onPause() {
		super.onPause();
		if(service != null){
			context.unbindService(mConnection);
			service = null;
		}
	}
	@Override
	public void onStop() {
		super.onStop();
		if(service != null){
			context.unbindService(mConnection);
			service = null;
		}
	}
	private ServiceConnection mConnection = new ServiceConnection() {

		public void onServiceConnected(ComponentName className, IBinder binder) {
			PlayService.MyBinder b = (PlayService.MyBinder) binder;
			service = b.getService();
		}

		public void onServiceDisconnected(ComponentName className) {
			service  = null;
		}
	};
}
