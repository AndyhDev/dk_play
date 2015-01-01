package com.dk.play.fragments;

import java.io.File;

import android.app.Activity;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnLayoutChangeListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.dk.play.App;
import com.dk.play.AutoPlaylistActivity;
import com.dk.play.CloudSongs;
import com.dk.play.CurPlaylist;
import com.dk.play.DkPlay;
import com.dk.play.InfoActivity;
import com.dk.play.PlaylistActivity;
import com.dk.play.R;
import com.dk.play.SettingsActivity;
import com.dk.play.service.PlayService;
import com.dk.play.util.DeletePlaylistDlg;
import com.dk.play.util.Image;
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
	private SharedPreferences settings;
	private boolean useBgImage = false;
	private int oldWidth = 0;
	private int oldHeight = 0;
	private Bitmap saveBmp;
	private File imgFile;
	private String imgPath;
	private App app;
	
	private final String BmpKey = "bmp_key";
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		if(savedInstanceState != null){
			if(savedInstanceState.containsKey(BmpKey)){
				saveBmp = savedInstanceState.getParcelable(BmpKey);
			}
		}
		context = this.getActivity();
		
		app = (App)getActivity().getApplication();
		
		imgFile = new File(context.getFilesDir(), "actionbar.png");
		imgPath = imgFile.getAbsolutePath();
		
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
		
		settings = PreferenceManager.getDefaultSharedPreferences(this.getActivity());
		settings.registerOnSharedPreferenceChangeListener(settingsListener);
		useBgImage = settings.getBoolean("use_bg_images", false);

		
		layout.addOnLayoutChangeListener(new OnLayoutChangeListener() {
			@Override
			public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
				if(useBgImage){
					setBgImage();
				}
			}
		});
		return layout;
	}
	
	SharedPreferences.OnSharedPreferenceChangeListener settingsListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
		public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
			useBgImage = prefs.getBoolean("use_bg_images", false);
			if(prefs.getBoolean("use_bg_images", false)){
				setBgImage();
			}else{
				removeBgImage();
			}
		}
	};
	
	private void setBgImage(){
		if(NavDrawerFragment.this.getActivity() == null){
			return;
		}
		Thread thread = new Thread() {
			@Override
			public void run() {
				TypedValue tv = new TypedValue();
				int actionBarHeight = 0;
			    if(getActivity().getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true)){
			        actionBarHeight = TypedValue.complexToDimensionPixelSize(tv.data,getResources().getDisplayMetrics());
				}
				Display display = getActivity().getWindowManager().getDefaultDisplay();
			    Point size = new Point();
			    display.getSize(size);
			    int height = size.y;
			    if(saveBmp == null){
			    	if(imgFile.exists()){
			    		saveBmp = app.getImage();
			    		if(saveBmp == null){
			    			Log.d(TAG, "LOAD IMAGE");
			    			saveBmp = Image.decodeSampledBitmapFromPath(imgPath, size.x, size.y);
			    			app.storeImage(saveBmp);
			    		}
			    		

			    	}else{
			    		saveBmp = BitmapFactory.decodeResource(getResources(), R.drawable.actionbar);
			    	}
			    	
			    }
			    if(saveBmp == null){
			    	saveBmp = BitmapFactory.decodeResource(getResources(), R.drawable.actionbar);
			    }
			    if(height > saveBmp.getHeight()){
			    	height = saveBmp.getHeight() - actionBarHeight;
			    }else{
			    	height = height - actionBarHeight;
			    }
			    int width = layout.getWidth();
			    if(width > saveBmp.getWidth()){
			    	width = saveBmp.getWidth();
			    }
			    if(oldHeight != height || oldWidth != width){
				    Bitmap bmp2 = Bitmap.createBitmap(saveBmp, 0, actionBarHeight, width, height);
				    final Drawable bg = new BitmapDrawable(getResources(), bmp2);
				    NavDrawerFragment.this.getActivity().runOnUiThread(new Runnable() {
						@SuppressWarnings("deprecation")
						@Override
						public void run() {
							layout.setBackgroundDrawable(bg);
						}
					});
				    oldHeight = height;
				    oldWidth = width;
			    }
			}
		};
		thread.start();
		
	}
	@SuppressWarnings("deprecation")
	private void removeBgImage(){
		if(NavDrawerFragment.this.getActivity() == null){
			return;
		}
		Drawable bg = new ColorDrawable(getResources().getColor(R.color.red));
	    if(layout != null){
	    	layout.setBackgroundDrawable(bg);
	    }
	}
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		if(useBgImage && layout.getWidth() != 0){
			setBgImage();
		}else{
			removeBgImage();
		}
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
        	}else if(viewHolder.item.getId() == NavAdapter.ID_ADV){
        		Intent intent = new Intent(context, CloudSongs.class);
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
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putParcelable(BmpKey, saveBmp);
	}
	
}