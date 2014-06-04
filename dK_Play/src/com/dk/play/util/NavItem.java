package com.dk.play.util;

import com.dk.play.R;


public class NavItem {
	private String title;
	private int img;
	private Boolean clickable;
	private int layout = R.layout.nav_item;
	private int id;
	private long playlistId = -1;
	
	public NavItem(){
		
	}
	public NavItem(int id, String title, int img, Boolean clickable){
		this.setId(id);
		this.title = title;
		this.img = img;
		this.clickable = clickable;
	}
	public NavItem(int id, String title, int img, Boolean clickable, long playlistId){
		this.setId(id);
		this.title = title;
		this.img = img;
		this.clickable = clickable;
		this.playlistId = playlistId;
	}
	public NavItem(int id, String title, int img, Boolean clickable, int layout){
		this.setId(id);
		this.title = title;
		this.img = img;
		this.clickable = clickable;
		this.layout = layout;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public int getImg() {
		return img;
	}
	public void setImg(int img) {
		this.img = img;
	}
	public Boolean getClickable() {
		return clickable;
	}
	public void setClickable(Boolean clickable) {
		this.clickable = clickable;
	}
	public int getLayout() {
		return layout;
	}
	public void setLayout(int layout) {
		this.layout = layout;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public long getPlaylistId() {
		return playlistId;
	}
	public void setPlaylistId(long playlistId) {
		this.playlistId = playlistId;
	}
	
}
