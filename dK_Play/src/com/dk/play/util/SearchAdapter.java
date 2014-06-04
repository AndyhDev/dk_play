package com.dk.play.util;

import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.dk.play.R;
import com.dk.play.database.SQLSong;
import com.dk.play.database.SQLSongList;

public class SearchAdapter extends SimpleCursorAdapter { 
	private static Context context; 
	private int layout; 
	private LayoutInflater inflater;
	private static Map<String, Bitmap> coverCache = new HashMap<String, Bitmap>();
	private static int size;
	private SQLSongList songs;
	
	@SuppressWarnings({ "deprecation", "static-access" })
	public SearchAdapter(Context context, int layout, Cursor c, SQLSongList songs, String[] from, int[] to) { 
		super(context, layout, c, from, to); 

		this.context = context; 
		this.layout = layout; 
		inflater = LayoutInflater.from(context);
		size = getPx(70);
		this.songs = songs;
	} 
	
	private int getPx(int dpi){
		Resources r = context.getResources();
		return (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dpi, r.getDisplayMetrics());
	}
	private static Bitmap getCover(String path){
		path = Paths.getCoverPath(path);
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
	    	String path = holder.song.getCover();
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
							Image.decodeSampledBitmapFromResource(context.getResources(), R.drawable.default_cover, size, size));
				}else{
					holder.cover.setImageBitmap(result);
				}
	        }
	    }

	}
	public View getView(int position, View view, ViewGroup parent){
		SongViewHolder viewHolder;
		SQLSong curSong = songs.get(position);
		//Log.d(TAG, "position:" + position);
		//Log.d(TAG, "title:" + curSong.getTitle());
		if(view == null){
			view = (RelativeLayout)inflater.inflate(layout, parent, false);
			viewHolder = new SongViewHolder();
			
			viewHolder.cover = (ImageView)view.findViewById(R.id.item_cover);
			viewHolder.title = (TextView)view.findViewById(R.id.item_title);
			viewHolder.artist = (TextView)view.findViewById(R.id.item_artist);
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
		
		String path = viewHolder.song.getCover();
    	if(coverCache.get(path) != null){
    		Bitmap result = coverCache.get(path);
			viewHolder.cover.setImageBitmap(result);
			
    	}else{
    		new CoverTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR , viewHolder);
    	}
		return view;
		/*Cursor cursor = getCursor(); 

		View v = inflater.inflate(layout, parent, false); 

		TextView title = (TextView) v.findViewById(R.id.item_title); 
		TextView artist = (TextView) v.findViewById(R.id.item_artist); 
		ImageView cover = (ImageView) v.findViewById(R.id.item_cover); 
		
		title.setText(cursor.getString(1));
		artist.setText(cursor.getString(2));
		Bitmap bmp = Image.decodeSampledBitmapFromPath(Paths.getCoverPath(cursor.getString(3)), size, size);
		cover.setImageBitmap(bmp);
		return v; */
	}

	public void changeSongList(SQLSongList matches) {
		songs = matches;
	}

	public SQLSongList getSongList() {
		return songs;
	}
	
} 
