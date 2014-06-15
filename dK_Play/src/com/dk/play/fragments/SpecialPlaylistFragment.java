package com.dk.play.fragments;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.dk.play.MusicEditActivity;
import com.dk.play.R;
import com.dk.play.SpecialPlaylist;
import com.dk.play.VideoEditActivity;
import com.dk.play.database.SQLPlaylist;
import com.dk.play.database.SQLSong;
import com.dk.play.database.SQLSongList;
import com.dk.play.database.SQLiteDataSource;
import com.dk.play.database.SQLiteHelper;
import com.dk.play.service.PlayService;
import com.dk.play.util.SongAdapter;
import com.dk.play.util.SongViewHolder;

public class SpecialPlaylistFragment extends Fragment {
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
	private SQLSong longClickSong;
	private ServiceReceiver serviceReceiver;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
		context = this.getActivity();

		Intent intent = getActivity().getIntent();
		if(intent.getAction() == SpecialPlaylist.PLAY){
			specialId = intent.getIntExtra(SpecialPlaylist.PLAYLIST_ID,	R.id.playlist_main);
		}
		layout = (RelativeLayout)inflater.inflate(R.layout.special_playlist_fragment, container, false);
		songView = (ListView)layout.findViewById(R.id.special_list);
		registerForContextMenu(songView);

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
		songView.setOnItemLongClickListener(new OnItemLongClickListener(){
			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
				SongViewHolder viewHolder = (SongViewHolder)view.getTag();
				longClickSong = viewHolder.song;
				return false;
			}});
		return layout;
	}
	
	private void refreshList(){
		readSongList();
		songAdt.cleanImageCache();
		songAdt.setSongList(playlist);
		songAdt.notifyDataSetChanged();
	}
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		if (v.getId() == R.id.special_list) {
			//AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)menuInfo;
			menu.setHeaderTitle(getString(R.string.choice));
			menu.add(0, SongListFragment.CONTEXT_DELETE, 0, getString(R.string.medium_delete));
			menu.add(0, SongListFragment.CONTEXT_EDIT, 1, getString(R.string.medium_edit));

			SQLiteDataSource datasource = new SQLiteDataSource(context);
			datasource.open();
			Map<String, Long> names = datasource.getPlaylistsNames();
			datasource.close();


			if(names.isEmpty()){
				menu.add(0, SongListFragment.CONTEXT_NEW_PLAYLIST, 2, getString(R.string.add_to_playlist));
			}else{
				SubMenu playlistMenu = menu.addSubMenu(0, SongListFragment.CONTEXT_SUB, 2, (getString(R.string.add_to_playlist)));
				Iterator<Entry<String, Long>> iterator = names.entrySet().iterator();
				int count = 0;
				while (iterator.hasNext()) {
					Entry<String, Long> entry =  iterator.next();
					String name = entry.getKey();

					playlistMenu.add(0, SongListFragment.CONTEXT_PLAYLIST, count, name);
					count++;
				}
			}
		}
	}
	@Override  
	public boolean onContextItemSelected(MenuItem item) {  
		if(item.getItemId() == SongListFragment.CONTEXT_DELETE){
			final SQLSong song = new SQLSong(longClickSong);
			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			builder.setTitle(getString(R.string.medium_delete));
			builder.setIcon(R.drawable.ic_launcher);
			builder.setMessage(getString(R.string.medium_delete_long));

			builder.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton){
					datasource = new SQLiteDataSource(getActivity());
					datasource.open();
					datasource.removeSQLSong(song);
					datasource.close();

					Intent readNew = new Intent(PlayService.READ_NEW);
					getActivity().sendBroadcast(readNew);
					dialog.dismiss();
				}
			});
			builder.setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					dialog.dismiss();
				}
			});
			AlertDialog alert = builder.create();
			alert.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
			alert.show();
		}else if(item.getItemId() == SongListFragment.CONTEXT_PLAYLIST){
			String playlistName = item.getTitle().toString();

			SQLiteDataSource datasource = new SQLiteDataSource(context);
			datasource.open();
			SQLPlaylist playlist = datasource.getSQLPlaylist(playlistName);
			if(playlist.isSQLSongIn(longClickSong)){
				new AlertDialog.Builder(getActivity())
				.setTitle(getString(R.string.medium_exists))
				.setMessage(getString(R.string.medium_is_in_list))
				.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) { 
						// continue with delete
					}
				})
				.setIcon(android.R.drawable.ic_dialog_alert)
				.show();
			}else{
				playlist.addSong(longClickSong);
				Toast.makeText(getActivity(), getString(R.string.playlist_added), Toast.LENGTH_SHORT).show();
			}
			datasource.updatePlaylist(playlist);
			datasource.close();
		}else if(item.getItemId() == SongListFragment.CONTEXT_NEW_PLAYLIST){
			Toast.makeText(getActivity(), getString(R.string.no_playlist), Toast.LENGTH_SHORT).show();
		}else if(item.getItemId() == SongListFragment.CONTEXT_EDIT){
			long id = longClickSong.getId();
			int type = longClickSong.getType();
			Intent intent;
			if(type == SQLSong.TYPE_MUSIC){
				intent = new Intent(context, MusicEditActivity.class);
			}else{
				intent = new Intent(context, VideoEditActivity.class);
			}
			intent.putExtra(VideoEditActivity.KEY_ID, id);
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
		}
		return true;  
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
		registerReceiver();
		refreshList();
	}
	@Override
	public void onPause() {
		super.onPause();
		if(service != null){
			context.unbindService(mConnection);
			service = null;
		}
		if(serviceReceiver != null){
			context.unregisterReceiver(serviceReceiver);
			serviceReceiver = null;
		}
	}
	@Override
	public void onStop() {
		super.onStop();
		if(service != null){
			context.unbindService(mConnection);
			service = null;
		}
		if(serviceReceiver != null){
			context.unregisterReceiver(serviceReceiver);
			serviceReceiver = null;
		}
	}
	private void registerReceiver(){
		if(serviceReceiver == null){
			serviceReceiver = new ServiceReceiver();

			IntentFilter intentFilter = new IntentFilter(PlayService.NEW_SONG);
			intentFilter.addAction(PlayService.READ_NEW);
			//intentFilter.addAction(PlayService.NEW_LOOP);
			context.registerReceiver(serviceReceiver, intentFilter);
		}
	}
	private class ServiceReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			if(intent.getAction().equals(PlayService.NEW_SONG)) {
				Log.d(TAG, "new song:" + intent.getLongExtra(PlayService.SONG_ID, 0));
			}else if(intent.getAction().equals(PlayService.READ_NEW)){
				Log.d(TAG, "READ_NEW");
				refreshList();
			}

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
