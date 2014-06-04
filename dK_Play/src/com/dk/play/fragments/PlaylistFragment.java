package com.dk.play.fragments;

import com.dk.play.R;
import com.dk.play.database.SQLPlaylist;
import com.dk.play.database.SQLSong;
import com.dk.play.database.SQLSongList;
import com.dk.play.database.SQLiteDataSource;
import com.dk.play.service.PlayService;
import com.dk.play.util.DragListener;
import com.dk.play.util.DynamicListView;
import com.dk.play.util.SongAdapter;
import com.dk.play.util.SongViewHolder;
import com.dk.play.util.SwipeDismissListViewTouchListener;

import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.AdapterView.OnItemClickListener;

public class PlaylistFragment extends Fragment {
	public static final String ACTION_PLAYLIST = "action_playlist";
	public static final String PLAYLIST_ID = "playlist_id";

	private final String TAG = "PlaylistFragment";
	
	private Context context;
	private RelativeLayout layout;
	private SQLPlaylist playlist;
	private SQLSongList songlist;
	private DynamicListView songView;
	private SongAdapter songAdt;
	private PlayService service;
	private ServiceReceiver serviceReceiver;
	private long playlistId;
	private String playlistName;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		context = this.getActivity();
		layout = (RelativeLayout)inflater.inflate(R.layout.playlist_fragment, container, false);
		songView = (DynamicListView)layout.findViewById(R.id.list_view);
		
		Intent intent = getActivity().getIntent();
		String action = intent.getAction();
		if(action.equals(ACTION_PLAYLIST)){
			playlistId = intent.getLongExtra(PLAYLIST_ID, -1);
		}else{
			playlistId = -1;
		}
		
		//bar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#ff8c00")));

		songAdt = new SongAdapter(this.getActivity(), null);
		songView.setAdapter(songAdt);
		songView.setCheeseList(songlist);
		songView.setDragListener(dragListener);

		songView.setOnItemClickListener(new OnItemClickListener(){
			@Override 
			public void onItemClick(AdapterView<?> arg0, View view,int position, long arg3){
				SongViewHolder viewHolder = (SongViewHolder)view.getTag();
				SQLSong song = viewHolder.song;
				if(service != null){
					if(!service.getPlayListName().equals(playlistName) || service.getSQLSongList().size() != songlist.size()){
						service.setSQLSongList(songlist);
						Log.d(TAG, "songlist:" + songlist.size());
						service.setPlayListName(playlistName);
					}
					service.setSongById(song.getId());
					service.processClick(song);
				}
			}
		});
		SwipeDismissListViewTouchListener touchListener =
				new SwipeDismissListViewTouchListener(
						songView,
						new SwipeDismissListViewTouchListener.DismissCallbacks() {
							@Override
							public boolean canDismiss(int position) {
								return true;
							}

							@Override
							public void onDismiss(ListView listView, int[] reverseSortedPositions) {
								for (int position : reverseSortedPositions) {
									playlist.removeSQLSong(songAdt.getItem(position));
									savePlaylist();
									Log.d(TAG, "remove:" + position);
								}
							}
						});
		songView.setOnTouchListener(touchListener);
		// Setting this scroll listener is required to ensure that during ListView scrolling,
		// we don't look for swipes.
		songView.setOnScrollListener(touchListener.makeScrollListener());
		
		refreshSongList();
		
		return layout;
	}
	private void refreshSongList(){
		Log.d(TAG, "refreshList");
		readSongList();
		songAdt.setSongList(songlist);
		songView.setCheeseList(songlist);
		songAdt.notifyDataSetChanged();
		getActivity().getActionBar().setTitle(playlist.getName());
		if(service != null){
			service.setSQLSongList(songlist);
			service.setPlayListName(playlistName);
		}
		
	}
	private void readSongList(){
		if(playlistId == -1){
			playlist = new SQLPlaylist("None");
			playlistName = "None";
			songlist = new SQLSongList();
		}else{
			SQLiteDataSource datasource = new SQLiteDataSource(context);
			datasource.open();
			playlist = datasource.getSQLPlaylist(playlistId);
			datasource.close();
			if(playlist == null){
				playlist = new SQLPlaylist("None");
			}
			songlist = playlist.getSQLSongList(context);
			Log.d(TAG, "songlist:" + songlist.size());
			playlistName = playlist.getName();
		}
	}
	private void savePlaylist(){
		SQLiteDataSource datasource = new SQLiteDataSource(context);
		datasource.open();
		datasource.updatePlaylist(playlist);
		datasource.close();
		refreshSongList();
		
	}
	private DragListener dragListener = new DragListener(){

		@Override
		public void onStart(int pos) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onEnd(int from, int to) {
			Log.d(TAG, "onEnd(" + from + ", " + to + ")");
			if(service != null){
				service.moveSong(from, to);
			}
		}
		
	};
	
	@Override
	public void onResume() {
		super.onResume();
		if(service == null){
			Intent intent= new Intent(this.getActivity(), PlayService.class);
			context.startService(intent);
			context.bindService(intent, mConnection, Context.BIND_ABOVE_CLIENT);
		}
		registerReceiver();
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
			intentFilter.addAction(PlayService.NEW_PLAYLIST);
			//intentFilter.addAction(PlayService.NEW_LOOP);
			context.registerReceiver(serviceReceiver, intentFilter);
		}
	}
	private ServiceConnection mConnection = new ServiceConnection() {

		public void onServiceConnected(ComponentName className, IBinder binder) {
			PlayService.MyBinder b = (PlayService.MyBinder) binder;
			service = b.getService();
			if(service.getPlayListName().equals(playlistName) && service.getSQLSongList().size() != songlist.size()){
				service.setSQLSongList(songlist);
				service.setPlayListName(playlistName);
			}
		}

		public void onServiceDisconnected(ComponentName className) {
			service  = null;
		}
	};
	private class ServiceReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			if(intent.getAction().equals(PlayService.NEW_PLAYLIST)) {
				//refreshSongList();
			}else if(intent.getAction().equals(PlayService.NEW_SONG)) {
				//intent.getLongExtra(PlayService.SONG_INDEX, -1);
				//refreshSongList();
			}

		}
	}
}
