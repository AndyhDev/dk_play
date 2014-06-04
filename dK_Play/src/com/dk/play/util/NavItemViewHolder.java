package com.dk.play.util;

import android.widget.ImageView;
import android.widget.TextView;

public class NavItemViewHolder {
	public TextView title;
	public ImageView icon;
	public int position;
	public NavItem item;
	
	public TextView getTitle() {
		return title;
	}
	public void setTitle(TextView title) {
		this.title = title;
	}
	public ImageView getIcon() {
		return icon;
	}
	public void setIcon(ImageView icon) {
		this.icon = icon;
	}
	public int getPosition() {
		return position;
	}
	public void setPosition(int position) {
		this.position = position;
	}
	public NavItem getItem() {
		return item;
	}
	public void setItem(NavItem item) {
		this.item = item;
	}
	
	
}
