package com.dk.play.util;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.LinearLayout.LayoutParams;

import com.dk.play.R;

public class CloudAdapter extends BaseAdapter{
	private Activity activity;
	private CloudList list;
	private LayoutInflater layInf;
	
	public static final int INVALID_ID = -1;
	
	public CloudAdapter(Activity activity, CloudList list){
		this.activity = activity;
		this.list = list;
		layInf = LayoutInflater.from(activity);
	}
	@Override
	public int getCount() {
		if(list != null){
			return list.size();
		}
		return 0;
	}

	@Override
	public CloudItem getItem(int position) {
		if(list != null){
			return list.get(position);
		}
		return null;
	}

	@Override
	public long getItemId(int position) {
		if (position < 0 || position >= list.size()) {
            return INVALID_ID;
        }
        return position;
	}
	
	public void expandItem(int position){
		CloudItem item = list.get(position);
		item.toggleExpand();
	}
	@Override
	public View getView(int position, View view, ViewGroup parent) {
		if(list == null){
			return null;
		}
		CloudViewHolder viewHolder;
		CloudItem item = list.get(position);
		if(view == null){
			view = (LinearLayout)layInf.inflate(R.layout.cloud_item, parent, false);
			viewHolder = new CloudViewHolder();
			
			viewHolder.img = (ImageView)view.findViewById(R.id.cloud_img);
			viewHolder.title = (TextView)view.findViewById(R.id.song_title);
			viewHolder.artist = (TextView)view.findViewById(R.id.song_artist);
			viewHolder.statusText = (TextView)view.findViewById(R.id.status_text);
			
			view.setTag(viewHolder);
		}else{
			viewHolder = (CloudViewHolder)view.getTag();
			
		}
		viewHolder.title.setText(item.getTitle());
		viewHolder.artist.setText(item.getArtist());
		viewHolder.item = item;
		viewHolder.position = position;
		viewHolder.onDevice = item.getOnDevice();
		viewHolder.expanded = item.getExpanded();
		
		if(viewHolder.onDevice == CloudItem.ON_DEVICE){
			viewHolder.img.setImageResource(R.drawable.green_point);
			viewHolder.statusText.setText(R.string.state_on_device);
		}else if(viewHolder.onDevice == CloudItem.ON_CACHE){
			viewHolder.img.setImageResource(R.drawable.orange_point);
			viewHolder.statusText.setText(R.string.state_on_cache);
		}else{
			viewHolder.img.setImageResource(R.drawable.red_point);
			viewHolder.statusText.setText(R.string.state_on_cloud);
		}
		
		View ex = view.findViewById(R.id.expand);
		
		if(viewHolder.expanded){
			ex.setVisibility(View.VISIBLE);
			LayoutParams params = (LayoutParams) ex.getLayoutParams();
			params.bottomMargin = 0;
		}else{
			if(ex.getVisibility() != View.GONE){
				ex.setVisibility(View.GONE);
				LayoutParams params = (LayoutParams) ex.getLayoutParams();
				params.bottomMargin = 0-ex.getHeight();
			}
		}
		return view;
	}
	public CloudList getList() {
		return list;
	}
	public void setList(CloudList list) {
		this.list = list;
	}
	public Activity getActivity() {
		return activity;
	}
	
}
