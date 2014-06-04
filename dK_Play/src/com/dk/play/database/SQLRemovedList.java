package com.dk.play.database;

import java.util.ArrayList;
import java.util.List;

public class SQLRemovedList {
	private List<SQLRemoved> removed = new ArrayList<SQLRemoved>();
	
	public SQLRemovedList(){
		
	}
	public ArrayList<String> getArrayList(){
		ArrayList<String> list = new ArrayList<String>();
		for(int i = 0; i < removed.size(); i++){
			list.add(removed.get(i).getPath());
		}
		return list;
	}
	public void add(SQLRemoved item){
		removed.add(item);
	}
	public int size(){
		return removed.size();
	}
	public SQLRemoved get(String path){
		SQLRemoved item1;
		for (int i = 0; i < removed.size(); i++) {
			item1 = removed.get(i);
			if(item1.getPath().equals(path)){
				return item1;
			}
		}
		return null;
	}
	public SQLRemoved get(int i){
		return removed.get(i);
	}
	public void remove(int i){
		removed.remove(i);
	}
	public void remove(SQLRemoved item){
		removed.remove(item);
	}
	public Boolean isIn(SQLRemoved item){
		SQLRemoved item1;
		for (int i = 0; i < removed.size(); i++) {
			item1 = removed.get(i);
			if(item1.getId() == item.getId()){
				return true;
			}
		}
		return false;
	}
	public Boolean isIn(String path){
		SQLRemoved item1;
		for (int i = 0; i < removed.size(); i++) {
			item1 = removed.get(i);
			if(item1.getPath().equals(path)){
				return true;
			}
		}
		return false;
	}
}
