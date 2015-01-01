package com.dk.play.util;

import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.dk.play.R;
import com.dk.play.database.SQLSong;
import com.dk.play.database.SQLSongList;

public class SongAdapter extends BaseAdapter {
	private SQLSongList songs;
	private static Map<String, Bitmap> coverCache = new HashMap<String, Bitmap>();
	private LayoutInflater songInf;
	private static Context context;
	private Integer markIndex;
	private long markId;
	
	final int INVALID_ID = -1;
    
	@SuppressWarnings("unused")
	private static final String TAG = "SongAdapter";
	
	public SongAdapter(Context c, SQLSongList songs){
		context = c;
		this.songs = songs;
		songInf = LayoutInflater.from(c);
	}
	
	public void setSongList(SQLSongList songs){
		this.songs = songs;
	}
	@Override
	public int getCount() {
		if(songs == null){
			return 0;
		}
		return songs.size();
	}

	@Override
	public SQLSong getItem(int position) {
		return songs.get(position);
	}

	@Override
	public long getItemId(int position) {
		if (position < 0 || position >= songs.size()) {
            return INVALID_ID;
        }
        SQLSong item = getItem(position);
        return item.getId();
	}
	public int getIndex(SQLSong song){
		return getIndex(song.getId());
	}
	public int getIndex(long id){
		if(songs != null){
			for(int i = 0; i < songs.size(); i++){
				if(songs.get(i).getId() == id){
					return i;
				}
			}
		}
		return 0;
	}
	private static Bitmap getCover(String path){
		return Image.decodeSampledBitmapFromPath(path, 80, 80);
	}
	public void cleanImageCache(){
		coverCache = new HashMap<String, Bitmap>();
	}
	private static class CoverTask extends AsyncTask<SongViewHolder, Void, Bitmap> {
		private int position;
	    private SongViewHolder holder;
	    
	    /*public CoverTask(int aPosition, SongViewHolder aHolder, Song aSong) {
	        position = aPosition;
	        holder = aHolder;
	        song = aSong;
	    }*/

	    @Override
	    protected Bitmap doInBackground(SongViewHolder... params){
	    	holder = params[0];
	    	position = holder.position;
	    	String path = holder.song.getCoverUri().getPath();
	    	if(coverCache.get(path) != null){
	    		return coverCache.get(path);
	    	}
	        Bitmap bmp = getCover(path);
	        coverCache.put(path, bmp);
			return bmp;
	    }

	    @Override
	    protected void onPostExecute(Bitmap result) {
	    	if (isCancelled()) {
	            return;
	        }
	        if(holder.position == position) {
	        	if(result == null){
					holder.cover.setImageBitmap(
							Image.decodeSampledBitmapFromResource(context.getResources(), R.drawable.default_cover, 80, 80));
				}else{
					holder.cover.setImageBitmap(result);
				}
	        }
	    }

	}
	@Override
	public View getView(int position, View view, ViewGroup parent) {
		SongViewHolder viewHolder;
		SQLSong curSong = songs.get(position);
		//Log.d(TAG, "position:" + position);
		//Log.d(TAG, "title:" + curSong.getTitle());
		if(view == null){
			view = (LinearLayout)songInf.inflate(R.layout.song, parent, false);
			viewHolder = new SongViewHolder();
			
			viewHolder.cover = (ImageView)view.findViewById(R.id.song_cover);
			viewHolder.title = (TextView)view.findViewById(R.id.song_title);
			viewHolder.artist = (TextView)view.findViewById(R.id.song_artist);
			//viewHolder.song = curSong;
			//viewHolder.position = position;
			
			view.setTag(viewHolder);
		}else{
			viewHolder = (SongViewHolder)view.getTag();
		}
		viewHolder.title.setText(curSong.getTitle());
		viewHolder.artist.setText(curSong.getArtist());
		viewHolder.song = curSong;
		viewHolder.position = position;
		
		String path = viewHolder.song.getCoverUri().getPath();
    	if(coverCache.get(path) != null){
    		Bitmap result = coverCache.get(path);
			viewHolder.cover.setImageBitmap(result);
			
    	}else{
    		new CoverTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR , viewHolder);
    	}
    	view.setBackgroundColor(0x00000000);
		if(markIndex != null){
			if(markId == curSong.getId()){
				view.setBackgroundColor(context.getResources().getColor(R.color.orange));
			}
		}
		return view;
	}

	public void markIndex(Integer index){
		markIndex = index;
		markId = getItemId(index);
	}

}