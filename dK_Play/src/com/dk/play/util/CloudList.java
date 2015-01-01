package com.dk.play.util;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.util.Log;

import com.dk.play.App;
import com.dk.play.adv.AdvSQLiteDataSource;
import com.dk.play.adv.AdvSong;

public class CloudList {
	private static final String TAG = "CloudList";
	
	private ArrayList<CloudItem> items = new ArrayList<CloudItem>();
	
	public CloudList(JSONArray files){
		AdvSQLiteDataSource dataSource = new AdvSQLiteDataSource(App.getContextStatic());
		dataSource.open();
		JSONObject item;
		JSONObject attr;
		CloudItem tmp;
		
		String[] tmp2 = Paths.getAdvSongDir().list();
		ArrayList<String> names = new ArrayList<String>(Arrays.asList(tmp2));
		
		for(int i = 0; i < files.length(); i++){
			try {
				item = files.getJSONObject(i);
				attr = item.getJSONObject("attr");
				String id = item.getString("id");
				String title = attr.getString("title");
				String artist = attr.getString("artist");
				String genre = attr.getString("genre");
				Integer rating = attr.getInt("rating");
				int onDevice = CloudItem.NOT_ON_DEVICE;
				Log.d(TAG, "onDevice1:" + onDevice);
				AdvSong advSong = dataSource.getAdvSongFromServerId(id);
				if(checkCache(id)){
					Log.d(TAG, "onDevice2:" + onDevice);
					onDevice = CloudItem.ON_CACHE;
				}
				if(advSong != null){
					Log.d(TAG, "onDevice3:" + onDevice);
					if(advSong.valid()){
						Log.d(TAG, "onDevice4:" + onDevice);
						onDevice = CloudItem.ON_DEVICE;
					}
				}
				if(onDevice != CloudItem.ON_DEVICE){
					Log.d(TAG, "onDevice5:" + onDevice);
					if(names.contains(id + ".file")){
						Log.d(TAG, "onDevice6:" + onDevice);
						onDevice = CloudItem.ON_DEVICE;
					}
				}
				Log.d(TAG, "onDevice7:" + onDevice);
				tmp = new CloudItem(id, title, artist, rating, genre, onDevice);
				items.add(tmp);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		dataSource.close();
	}
	public static boolean checkCache(String id){
		Context context = App.getContextStatic();
		File cover = new File(context.getCacheDir(), id + ".cover");
		File file = new File(context.getCacheDir(), id + ".file");
		File attr_file = new File(context.getCacheDir(), id + ".attr");
		if(cover.exists() && file.exists() && attr_file.exists()){
			return true;
		}
		return false;
	}
	public static boolean deleteCache(CloudItem item){
		return deleteCache(item.getId());
	}
	public static boolean deleteCache(String id){
		Context context = App.getContextStatic();
		File cover = new File(context.getCacheDir(), id + ".cover");
		File file = new File(context.getCacheDir(), id + ".file");
		File attr_file = new File(context.getCacheDir(), id + ".attr");
		if(cover.exists()){
			cover.delete();
		}
		if(file.exists()){
			file.delete();
		}
		if(attr_file.exists()){
			attr_file.delete();
		}
		return true;
	}
	public int size(){
		return items.size();
	}
	public CloudItem get(int index){
		return items.get(index);
	}
	public void remove(int index){
		items.remove(index);
	}
	public void remove(CloudItem item){
		for(int i = 0; i < items.size(); i++){
			if(items.get(i).getId().equals(item.getId())){
				items.remove(i);
				break;
			}
		}
	}
}
