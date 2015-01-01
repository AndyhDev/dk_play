package com.dk.play.util;

import java.io.File;
import java.util.Iterator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;

import com.dk.play.App;
import com.dk.play.database.SQLSong;
import com.dk.play.database.SQLSongList;
import com.dk.play.database.SQLiteDataSource;
import com.dk.util.Hash;
import com.dk.util.IO;

public class MD5FileCache {
	private Context context = App.getContextStatic();
	private JSONObject data; //FORMAT: {filename: [modifyTime, md5]}
	private File cache = new File(context.getCacheDir(), "md5");
	
	public MD5FileCache(){
		try {
			String content = IO.readToString(cache);
			if(content != null){
				data = new JSONObject(content);
			}else{
				data = new JSONObject();
			}
		} catch (JSONException e) {
			e.printStackTrace();
			data = new JSONObject();
		}
	}
	public void genMd5Cache(){
		Context context = App.getContextStatic();
		SQLiteDataSource ds = new SQLiteDataSource(context);
		ds.open();
		SQLSongList songs = ds.getSQLSongList();
		ds.close();
		for(int i = 0; i < songs.size(); i++){
			SQLSong song = songs.get(i);
			getMd5(song.getPath());
		}
	}
	public String getMd5(String path){
		return getMd5(new File(path));
	}
	public String getMd5(File f){
		if(data != null){
			JSONArray elm = data.optJSONArray(f.getName());
			if(elm != null){
				long mTime = f.lastModified();
				long lTime = elm.optLong(0, -1l);
				if(mTime != 0 && lTime != -1l){
					if(mTime == lTime){
						return elm.optString(1);
					}
				}
			}
		}
		String md5 = Hash.getMd5FromFile(f);
		
		if(data != null){ 
			JSONArray array = new JSONArray();
			array.put(f.lastModified());
			array.put(md5);
			array.put(f.getPath());
			try {
				data.put(f.getName(), array);
			} catch (JSONException e) {
				e.printStackTrace();
			}
			save();
		}
		
		return md5;
	}
	public String getPathFromMd5(String md5){
		if(data != null){
			Iterator<?> keys = data.keys();

	        while(keys.hasNext()){
	            String name = (String)keys.next();
	            JSONArray array = data.optJSONArray(name);
	            if(array != null){
	            	if(array.optString(1).equals(md5)){
	            		return array.optString(2);
	            	}
	            }
	        }
		}
		return  null;
	}
	private void save(){
		if(data != null){
			IO.write(data.toString(), cache);
		}
	}
	public boolean clearCache(){
		return cache.delete();
	}
}
