package com.dk.play.database;

import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.util.Log;

public class SQLSongList {
	private List<SQLSong> songs = new ArrayList<SQLSong>();

	public SQLSongList(){

	}
	public void add(SQLSong item){
		songs.add(item);
	}
	public void add(int index, SQLSong item){
		songs.add(index, item);
	}
	public int size(){
		return songs.size();
	}
	public SQLSong get(int i){
		try {
			return songs.get(i);
		} catch (Exception e) {
			return null;
		}

	}
	public SQLSong getById(long id){
		SQLSong item;
		for (int i = 0; i < songs.size(); i++) {
			item = songs.get(i);
			if(item.getId() == id){
				return item;
			}
		}
		return null;
	}
	public int getIndex(SQLSong song){
		for(int i = 0; i < songs.size() ; i++){
			if(songs.get(i).getId() == song.getId()){
				return i;
			}
		}
		return -1;
	}
	public void remove(int i){
		songs.remove(i);
	}
	public void remove(SQLSong song){
		if(!songs.remove(song)){
			SQLSong item;
			for (int i = 0; i < songs.size(); i++) {
				item = songs.get(i);
				if(item.getId() == song.getId()){
					songs.remove(i);
					break;
				}
			}
		}
	}
	public Boolean isIn(SQLSong item){
		SQLSong item1;
		for (int i = 0; i < songs.size(); i++) {
			item1 = songs.get(i);
			if(item1.getId() == item.getId()){
				return true;
			}
		}
		return false;
	}
	public Boolean isIn(String path){
		SQLSong item1;
		for (int i = 0; i < songs.size(); i++) {
			item1 = songs.get(i);
			if(item1.getPath().equals(path)){
				return true;
			}
		}
		return false;
	}
	public void moveSong(int from, int to){
		if(from == to){
			return;
		}

		List<SQLSong> newSongs = new ArrayList<SQLSong>();
		if(songs == null){
			return;
		}
		Log.d("count", "onend3("+from+", "+to+")");
		SQLSong item = songs.get(from);
		SQLSong obj;
		int count = 0;
		for(int i = 0; i < songs.size(); i++) {
			Log.d("count", "count:"+count+" i:"+i);
			if(count == to){
				//Log.d("JSONArrayMove", "i='" + i + "', item='" + item + "'");
				Log.d("count", "add1");
				newSongs.add(item);
				count++;
			}
			if(i != from){
				obj = songs.get(i);
				//Log.d("JSONArrayMove", "i='" + i + "', item='" + obj + "'");
				Log.d("count", "add2");
				newSongs.add(obj);
				count++;
			};
		}
		songs = newSongs;
	}
	public void set(int index, SQLSong song) {
		songs.set(index, song);
	}
	public SQLSongList copy(){
		SQLSongList temp = new SQLSongList();
		List<SQLSong> tempSongs = new ArrayList<SQLSong>();
		for(int i = 0; i < songs.size(); i++){
			tempSongs.add(songs.get(i));
		}
		temp.songs = tempSongs;
		return temp;
	}
	public List<String> getArrayTitle() {
		List<String> listItems = new ArrayList<String>();
		for(int i = 0; i < songs.size(); i++){
			listItems.add(songs.get(i).getTitle());
		}
		return listItems;
	}
	public List<String> getArrayId() {
		List<String> listItems = new ArrayList<String>();
		for(int i = 0; i < songs.size(); i++){
			listItems.add(Long.toString(songs.get(i).getId()));
		}
		return listItems;
	}
	public CharSequence[] getCharSequenceTitle(){
		List<String> listItems = getArrayTitle();
		final CharSequence[] charSequenceItems = listItems.toArray(new CharSequence[listItems.size()]);
		return charSequenceItems;
	}
	public CharSequence[] getCharSequenceId(){
		List<String> listItems = getArrayId();
		final CharSequence[] charSequenceItems = listItems.toArray(new CharSequence[listItems.size()]);
		return charSequenceItems;
	}
	@SuppressLint("DefaultLocale")
	public SQLSongList search(String s) {
		SQLSongList matches = new SQLSongList();
		for(int i = 0; i < songs.size(); i++){
			if(songs.get(i).getTitle().toLowerCase().contains(s.toLowerCase())){
				matches.add(songs.get(i));
			}else if(songs.get(i).getArtist().toLowerCase().contains(s.toLowerCase())){
				matches.add(songs.get(i));
			}else if(songs.get(i).getAlbum().toLowerCase().contains(s.toLowerCase())){
				matches.add(songs.get(i));
			}else if(songs.get(i).getGenre().toLowerCase().contains(s.toLowerCase())){
				matches.add(songs.get(i));
			}
		}
		return matches;
	}
}
