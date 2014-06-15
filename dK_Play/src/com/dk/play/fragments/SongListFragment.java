package com.dk.play.fragments;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.dk.play.MusicEditActivity;
import com.dk.play.R;
import com.dk.play.VideoEditActivity;
import com.dk.play.database.SQLPlaylist;
import com.dk.play.database.SQLSong;
import com.dk.play.database.SQLSongList;
import com.dk.play.database.SQLiteDataSource;
import com.dk.play.service.PlayService;
import com.dk.play.util.SongAdapter;
import com.dk.play.util.SongViewHolder;

public class SongListFragment extends Fragment {
	private static final String TAG = "SongListFragment";

	public static final int CONTEXT_DELETE = 1;
	public static final int CONTEXT_PLAYLIST = 2;
	public static final int  CONTEXT_SUB = 3;
	public static final int CONTEXT_NEW_PLAYLIST = 4;
	public static final int CONTEXT_EDIT = 5;
	
	private Long playId; //speichert temporär die song id bis die service verbindung wieder steht 
	private SQLSongList songList;
	private RelativeLayout layout;
	private ListView songView;
	private PlayService service;
	private SQLiteDataSource datasource;
	private ServiceReceiver serviceReceiver;
	private Context context;
	private SongAdapter songAdt;
	private SQLSong longClickSong;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		context = this.getActivity();
		layout = (RelativeLayout)inflater.inflate(R.layout.song_list, container, false);
		songView = (ListView)layout.findViewById(R.id.song_list);
		registerForContextMenu(songView);
		
		//bar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#ff8c00")));
		readSongList();
		songAdt = new SongAdapter(this.getActivity(), songList);
		songView.setAdapter(songAdt);
		songView.setOnItemClickListener(new OnItemClickListener(){
			@Override 
			public void onItemClick(AdapterView<?> arg0, View view,int position, long arg3){
				SongViewHolder viewHolder = (SongViewHolder)view.getTag();
				SQLSong song = viewHolder.song;
				if(service != null){
					if(service.isSongIn(song.getId())){
						service.setSongById(song.getId());
						service.processClick(song);
					}else{
						SQLSongList list = service.getSQLSongList();
						if(list.size() == 0){
							service.setMainList();
							if(service.isSongIn(song.getId())){
								service.setSongById(song.getId());
								service.processClick(song);
								return;
							}
						}
						showSongNotInListDlg(song);
					}
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
	private int getOffset(){
		int height = 80;
		for(int i = 0; i  <= songView.getLastVisiblePosition(); i++){
			if(songView.getChildAt(i)!= null){
				height = songView.getChildAt(i).getHeight();
			}
		}
		return (int)(songView.getHeight() - height) / 2;
	}
	public void scrollToSong(long id){
		int index = songAdt.getIndex(id);
		songView.smoothScrollToPositionFromTop(index, getOffset());
	}
	private void refreshList(){
		readSongList();
		songAdt.cleanImageCache();
		songAdt.setSongList(songList);
		songAdt.notifyDataSetChanged();
	}
	@Override
	public void onResume() {
		super.onResume();
		Log.d(TAG, "onResume");
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
		Log.d(TAG, "onPause");
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
	private void readSongList(){
		Log.d(TAG, "readSongList");
		datasource = new SQLiteDataSource(this.getActivity());
		datasource.open();
		songList = datasource.getSQLSongList();
		datasource.close();
	}
	public void songPicked(View view){
		SongViewHolder viewHolder = (SongViewHolder)view.getTag();
		SQLSong song = viewHolder.song;
		if(service != null){
			service.setSongById(song.getId());
			service.processClick(song);
		}

	}
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		if (v.getId() == R.id.song_list) {
			//AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)menuInfo;
			menu.setHeaderTitle(getString(R.string.choice));
			menu.add(0, CONTEXT_DELETE, 0, getString(R.string.medium_delete));
			menu.add(0, CONTEXT_EDIT, 1, getString(R.string.medium_edit));
			
			SQLiteDataSource datasource = new SQLiteDataSource(context);
			datasource.open();
			Map<String, Long> names = datasource.getPlaylistsNames();
			datasource.close();
			
			
			if(names.isEmpty()){
				menu.add(0, CONTEXT_NEW_PLAYLIST, 2, getString(R.string.add_to_playlist));
			}else{
				SubMenu playlistMenu = menu.addSubMenu(0, CONTEXT_SUB, 2, (getString(R.string.add_to_playlist)));
				Iterator<Entry<String, Long>> iterator = names.entrySet().iterator();
				int count = 0;
				while (iterator.hasNext()) {
					Entry<String, Long> entry =  iterator.next();
					String name = entry.getKey();
	
					playlistMenu.add(0, CONTEXT_PLAYLIST, count, name);
					count++;
				}
			}
		}
	}
	@Override  
	public boolean onContextItemSelected(MenuItem item) {  
		if(item.getItemId() == CONTEXT_DELETE){
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
		}else if(item.getItemId() == CONTEXT_PLAYLIST){
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
		}else if(item.getItemId() == CONTEXT_NEW_PLAYLIST){
			Toast.makeText(getActivity(), getString(R.string.no_playlist), Toast.LENGTH_SHORT).show();
		}else if(item.getItemId() == CONTEXT_EDIT){
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
	private ServiceConnection mConnection = new ServiceConnection() {

		public void onServiceConnected(ComponentName className, IBinder binder) {
			PlayService.MyBinder b = (PlayService.MyBinder) binder;
			service = b.getService();
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
			if(intent.getAction().equals(PlayService.NEW_SONG)) {
				Log.d(TAG, "new song:" + intent.getLongExtra(PlayService.SONG_ID, 0));
			}else if(intent.getAction().equals(PlayService.READ_NEW)){
				Log.d(TAG, "READ_NEW");
				refreshList();
			}

		}
	}
	private void showSongNotInListDlg(final SQLSong song){
		final Dialog dialog = new Dialog(context);
		dialog.setContentView(R.layout.dialog_song_not_in_list);
		dialog.setTitle(getString(R.string.medium_not_present));

		// set the custom dialog components - text, image and button
		TextView text = (TextView) dialog.findViewById(R.id.dlg_content);
		text.setText(getString(R.string.medium_not_in_list));
		ImageView image = (ImageView) dialog.findViewById(R.id.dlg_cover);
		image.setImageBitmap(song.getCoverBitmap(40));

		Button dialogButton = (Button) dialog.findViewById(R.id.dlg_cancel);
		// if button is clicked, close the custom dialog
		dialogButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				dialog.dismiss();
			}
		});
		Button dialogButton2 = (Button) dialog.findViewById(R.id.dlg_add);
		// if button is clicked, close the custom dialog
		dialogButton2.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if(service != null){
					service.addSong(service.getSongIndex(), song);
					service.setSong(song.getId(), true);
					service.processClick(song);
				}
				dialog.dismiss();
			}
		});

		dialog.show();
	}
}