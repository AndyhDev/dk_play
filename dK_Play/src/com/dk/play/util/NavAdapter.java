package com.dk.play.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.dk.play.R;
import com.dk.play.adv.AdvControl;
import com.dk.play.database.SQLiteDataSource;

public class NavAdapter extends BaseAdapter {
	@SuppressLint("UseSparseArrays")
	private static Map<Integer, Bitmap> bmpCache = new HashMap<Integer, Bitmap>();
	private LayoutInflater layout;
	private static Context context;
	private List<NavItem> items = new ArrayList<NavItem>();
	
	public static final int ID_MAIN = 0;
	public static final int ID_PLAYLIST = 1;
	public static final int ID_AUTO_PLAYLISTS = 2;
	public static final int ID_SETTINGS = 3;
	public static final int ID_INFO = 4;
	public static final int ID_SPACER = 5;
	public static final int ID_ADD_PLAYLIST = 6;
	public static final int ID_PLAYLIST_TITLE = 7;
	public static final int ID_ADV = 8;
	
	public static final int ID_USER_PLAYLIST = 10;
	
	@SuppressWarnings("unused")
	private static final String TAG = "NavAdapter";
	private AdvControl adv;
	
	public NavAdapter(Context c){
		context = c;
		adv = new AdvControl(context);
		layout = LayoutInflater.from(c);
		read();
	}
	public void read(){
		items.clear();
		items.add(new NavItem(ID_MAIN, "Übersicht", R.drawable.home, true));
		items.add(new NavItem(ID_PLAYLIST, "Playlist", R.drawable.show_playlist, true));
		items.add(new NavItem(ID_AUTO_PLAYLISTS, "Auto Playlisten", R.drawable.playlist, true));
		items.add(new NavItem(ID_SETTINGS, "Einstellungen", R.drawable.settings, true));
		
		if(adv.getActive()){
			items.add(new NavItem(ID_ADV, "Cloud Songs", R.drawable.ic_action_cloud, true));
		}
		
		items.add(new NavItem(ID_INFO, "Info", R.drawable.info, true));
		items.add(new NavItem(ID_SPACER, "", R.drawable.info, false, R.layout.nav_spacer));
		items.add(new NavItem(ID_PLAYLIST_TITLE, "Playlisten:", R.drawable.info, false, R.layout.nav_center_text));
		
		readPlaylists();
		
		items.add(new NavItem(ID_ADD_PLAYLIST, "", R.drawable.add, true, R.layout.nav_center_img));
	}
	private void readPlaylists(){
		SQLiteDataSource datasource = new SQLiteDataSource(context);
		datasource.open();
		Map<String, Long> names = datasource.getPlaylistsNames();
		datasource.close();
		
		Iterator<Entry<String, Long>> iterator = names.entrySet().iterator();
		while (iterator.hasNext()) {
			Entry<String, Long> entry =  iterator.next();
			String name = entry.getKey();
			long id = entry.getValue();
			
			items.add(new NavItem(ID_USER_PLAYLIST, name, R.drawable.playlist, true, id));
		}
	 
		
	}
	@Override
	public int getCount() {
		return items.size();
	}

	@Override
	public Object getItem(int arg0) {
		return null;
	}

	@Override
	public long getItemId(int arg0) {
		return 0;
	}
	@Override
	public boolean isEnabled(int position) {
		NavItem item = items.get(position);
	    if(item.getClickable()){
	        return true;
	    }
	    return false;
	}
	private static class CoverTask extends AsyncTask<NavItemViewHolder, Void, Bitmap> {
		private int position;
	    private NavItemViewHolder holder;

	    @Override
	    protected Bitmap doInBackground(NavItemViewHolder... params){
	    	holder = params[0];
	    	position = holder.position;
	    	int img = holder.item.getImg();
	    	if(bmpCache.get(img) != null){
	    		return bmpCache.get(img);
	    	}
	        Bitmap bmp = Image.decodeSampledBitmapFromResource(context.getResources(), img, 40, 40);
	        bmpCache.put(img, bmp);
			return bmp;
	    }

	    @Override
	    protected void onPostExecute(Bitmap result) {
	    	if (isCancelled()) {
	            return;
	        }
	        if(holder.position == position) {
				holder.icon.setImageBitmap(result);
	        }
	    }

	}
	@Override
	public View getView(int position, View view, ViewGroup parent) {
		NavItemViewHolder viewHolder;
		NavItem curItem = items.get(position);
		
		view = (RelativeLayout)layout.inflate(curItem.getLayout(), parent, false);
		viewHolder = new NavItemViewHolder();
		
		viewHolder.icon = (ImageView)view.findViewById(R.id.nav_icon);
		viewHolder.title = (TextView)view.findViewById(R.id.nav_title);
		
		view.setTag(viewHolder);
		viewHolder.title.setText(curItem.getTitle());
		viewHolder.item = curItem;
		viewHolder.position = position;
		
		int img = viewHolder.item.getImg();
    	if(bmpCache.get(img) != null){
    		Bitmap result = bmpCache.get(img);
			viewHolder.icon.setImageBitmap(result);
			
    	}else{
    		new CoverTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR , viewHolder);
    	}
		
		return view;
	}

}