package com.dk.play.fragments;

import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.dk.play.AutoPlaylistActivity;
import com.dk.play.CurPlaylist;
import com.dk.play.DkPlay;
import com.dk.play.InfoActivity;
import com.dk.play.PlaylistActivity;
import com.dk.play.R;
import com.dk.play.SettingsActivity;
import com.dk.play.service.PlayService;
import com.dk.play.util.DeletePlaylistDlg;
import com.dk.play.util.NavAdapter;
import com.dk.play.util.NavItemViewHolder;
import com.dk.play.util.NewPlaylistDlg;

public class NavDrawerFragment extends Fragment{
	private static final String TAG = "NavDrawerFragment";
	private Context context;
	private RelativeLayout layout;
	private ListView list;
	private ServiceReceiver serviceReceiver;
	private NavAdapter adapter;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		context = this.getActivity();
		
		layout = (RelativeLayout)inflater.inflate(R.layout.nav_drawer_layout, container, false);
		list = (ListView) layout.findViewById(R.id.nav_drawer_list);
		
		adapter = new NavAdapter(this.getActivity());
		list.setAdapter(adapter);
		list.setLongClickable(true);
		list.setOnItemLongClickListener(new OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
				NavItemViewHolder holder = (NavItemViewHolder) view.getTag();
				if(holder.getItem().getId() == NavAdapter.ID_USER_PLAYLIST){
					DeletePlaylistDlg dlg = new DeletePlaylistDlg(getActivity(), holder.getItem().getTitle(), holder.getItem().getPlaylistId());
					dlg.show();
				}
				return false;
			}
		});
		list.setOnItemClickListener(new DrawerItemClickListener());
		
		return layout;
	}
	@Override
	public void onResume() {
		super.onResume();
		Log.d(TAG, "onResume");
		adapter.read();
		adapter.notifyDataSetChanged();
		registerReceiver();
	}
	@Override
	public void onPause() {
		super.onPause();
		if(serviceReceiver != null){
			context.unregisterReceiver(serviceReceiver);
			serviceReceiver = null;
		}
	}
	@Override
	public void onStop() {
		super.onStop();
		if(serviceReceiver != null){
			context.unregisterReceiver(serviceReceiver);
		}
	}
	public void close(){
		DrawerLayout d = (DrawerLayout)layout.getParent().getParent();
    	d.closeDrawers();
	}
	private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @SuppressWarnings("rawtypes")
		@Override
        public void onItemClick(AdapterView parent, View view, int position, long id) {
        	NavItemViewHolder viewHolder = (NavItemViewHolder)view.getTag();
        	if(viewHolder.item.getId() == NavAdapter.ID_MAIN){
        		Intent intent = new Intent(context, DkPlay.class);
        		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        		startActivity(intent);
        		close();
        	}else if(viewHolder.item.getId() == NavAdapter.ID_PLAYLIST){
        		Intent intent = new Intent(context, CurPlaylist.class);
        		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        		startActivity(intent);
        		close();
        	}else if(viewHolder.item.getId() == NavAdapter.ID_SETTINGS){
        		Intent intent = new Intent(context, SettingsActivity.class);
        		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        		startActivity(intent);
        		close();
        	}else if(viewHolder.item.getId() == NavAdapter.ID_AUTO_PLAYLISTS){
        		Intent intent = new Intent(context, AutoPlaylistActivity.class);
        		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        		startActivity(intent);
        		close();
        	}else if(viewHolder.item.getId() == NavAdapter.ID_INFO){
        		Intent intent = new Intent(context, InfoActivity.class);
        		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        		startActivity(intent);
        		close();
        	}else if(viewHolder.item.getId() == NavAdapter.ID_ADD_PLAYLIST){
        		close();
        		NewPlaylistDlg dlg = new NewPlaylistDlg(context);
        		dlg.show();
        	}else if(viewHolder.item.getId() == NavAdapter.ID_USER_PLAYLIST){
        		long playlistId = viewHolder.item.getPlaylistId();
        		Intent intent = new Intent(context, PlaylistActivity.class);
        		intent.setAction(PlaylistFragment.ACTION_PLAYLIST);
        		intent.putExtra(PlaylistFragment.PLAYLIST_ID, playlistId);
        		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        		startActivity(intent);
        		close();
        	}
        	
        }
    }
	private void registerReceiver(){
		if(serviceReceiver == null){
			serviceReceiver = new ServiceReceiver();

			IntentFilter intentFilter = new IntentFilter(PlayService.PLAYLIST_CREATED);
			intentFilter.addAction(PlayService.PLAYLIST_DELETED);
			this.getActivity().registerReceiver(serviceReceiver, intentFilter);
		}
	}
	private class ServiceReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			Log.d(TAG, "receive:" + intent.getAction());
			if(intent.getAction().equals(PlayService.PLAYLIST_CREATED)){
				adapter.read();
				adapter.notifyDataSetChanged();
			}else if(intent.getAction().equals(PlayService.PLAYLIST_DELETED)){
				adapter.read();
				adapter.notifyDataSetChanged();
			}

		}
	}

}