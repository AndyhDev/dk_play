package com.dk.play.util;

import java.util.List;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.dk.play.R;
import com.dk.play.adv.AdvRemoved;

public class CloudDeletedAdapter extends BaseAdapter{
	private Activity activity;
	private List<AdvRemoved>  list;
	private LayoutInflater layInf;
	
	public static final int INVALID_ID = -1;
	
	public CloudDeletedAdapter(Activity activity, List<AdvRemoved> list){
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
	public AdvRemoved getItem(int position) {
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
	
	@Override
	public View getView(int position, View view, ViewGroup parent) {
		if(list == null){
			return null;
		}
		CloudDeleteHolder viewHolder;
		AdvRemoved item = list.get(position);
		if(view == null){
			view = (LinearLayout)layInf.inflate(R.layout.cloud_delete_item, parent, false);
			viewHolder = new CloudDeleteHolder();
			
			viewHolder.title = (TextView)view.findViewById(R.id.song_title);
			viewHolder.artist = (TextView)view.findViewById(R.id.song_artist);
			
			view.setTag(viewHolder);
		}else{
			viewHolder = (CloudDeleteHolder)view.getTag();
			
		}
		viewHolder.title.setText(item.getTitle());
		viewHolder.artist.setText(item.getArtist());
		viewHolder.item = item;
		viewHolder.position = position;

		return view;
	}
	public List<AdvRemoved> getList() {
		return list;
	}
	public void setList(List<AdvRemoved> list) {
		this.list = list;
	}
	public Activity getActivity() {
		return activity;
	}
	
}
