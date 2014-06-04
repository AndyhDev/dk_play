package com.dk.play.fragments;

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
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.dk.play.R;
import com.dk.play.database.SQLSong;
import com.dk.play.database.SQLSongList;
import com.dk.play.service.PlayService;
import com.dk.play.util.DragListener;
import com.dk.play.util.DynamicListView;
import com.dk.play.util.SongAdapter;
import com.dk.play.util.SongViewHolder;
import com.dk.play.util.SwipeDismissListViewTouchListener;

public class CurPlaylistFragment extends Fragment {
	private static final String TAG = "CurPlaylistFragment";
	
	private Long playId; //speichert temporär die song id bis die service verbindung wieder steht 
	private RelativeLayout layout;
	private PlayService service;
	private ServiceReceiver serviceReceiver;
	private Context context;
	private SQLSongList playlist;
	private DynamicListView songView;
	private SongAdapter songAdt;
	private Integer curIndex;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		context = this.getActivity();
		layout = (RelativeLayout)inflater.inflate(R.layout.playlist, container, false);
		songView = (DynamicListView)layout.findViewById(R.id.list_view);

		//bar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#ff8c00")));
		readSongList();
		songAdt = new SongAdapter(this.getActivity(), playlist);
		songView.setAdapter(songAdt);
		songView.setCheeseList(playlist);
		songView.setDragListener(dragListener);

		songView.setOnItemClickListener(new OnItemClickListener(){
			@Override 
			public void onItemClick(AdapterView<?> arg0, View view,int position, long arg3){
				SongViewHolder viewHolder = (SongViewHolder)view.getTag();
				SQLSong song = viewHolder.song;
				if(service != null){
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
									//songAdt.remove(songAdt.getItem(position));
									if(service != null){
										service.removeSong(position);
									}
									Log.d(TAG, "remove:" + position);
								}
							}
						});
		songView.setOnTouchListener(touchListener);
		// Setting this scroll listener is required to ensure that during ListView scrolling,
		// we don't look for swipes.
		songView.setOnScrollListener(touchListener.makeScrollListener());
		
		return layout;
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
	public void playSongById(long id){
		Log.d(TAG, "1");
		if(service != null){
			Log.d(TAG, "2");
			if(service.isSongIn(id)){
				Log.d(TAG, "3");
				service.setSongById(id);
			}else{
				Log.d(TAG, "4");
				SQLSongList list = service.getSQLSongList();
				if(list.size() == 0){
					Log.d(TAG, "5");
					service.setMainList();
					if(service.isSongIn(id)){
						service.setSongById(id);
						return;
					}
				}
			}
		}else{
			playId = id;
		}
	}
	@SuppressWarnings("unused")
	private int getVisibleItems(){
		int count = 0;
		for(int i = 0; i  <= songView.getLastVisiblePosition(); i++){
			if(songView.getChildAt(i)!= null){
				count++;
			}
		}
		return count;
	}
	private int getOffset(){
		int height = 80;
		for(int i = 0; i  <= songView.getLastVisiblePosition(); i++){
			if(songView.getChildAt(i)!= null){
				height = songView.getChildAt(i).getHeight();
			}
		}
		return (int)(songView.getHeight() - height) / 2;
	}
	private void refreshList(){
		Log.d(TAG, "refreshList");
		readSongList();
		songAdt.setSongList(playlist);
		songView.setCheeseList(playlist);
		if(curIndex != null){
			songAdt.markIndex(curIndex);
		}
		songAdt.notifyDataSetChanged();
		if(curIndex != null){
			songView.post( new Runnable() {
				@Override
				public void run() {
					songView.smoothScrollToPositionFromTop(curIndex, getOffset());
					songAdt.notifyDataSetChanged();
				}
			});
		}
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
	private void readSongList(){
		if(service != null){
			playlist = service.getSQLSongList();
			curIndex = service.getSongIndex();
		}
	}
	public void songPicked(View view){
		SongViewHolder viewHolder = (SongViewHolder)view.getTag();
		SQLSong song = viewHolder.song;
		if(service != null){
			service.setSongById(song.getId());
			service.processClick(song);
		}

	}
	private ServiceConnection mConnection = new ServiceConnection() {

		public void onServiceConnected(ComponentName className, IBinder binder) {
			PlayService.MyBinder b = (PlayService.MyBinder) binder;
			service = b.getService();
			refreshList();
			if(playId != null){
				playSongById(playId);
				playId = null;
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
				refreshList();
			}else if(intent.getAction().equals(PlayService.NEW_SONG)) {
				//intent.getLongExtra(PlayService.SONG_INDEX, -1);
				refreshList();
			}

		}
	}
	public SQLSongList getPlaylist() {
		return playlist;
	}
	
}