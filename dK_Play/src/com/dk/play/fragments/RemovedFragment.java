package com.dk.play.fragments;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.dk.play.R;
import com.dk.play.database.SQLRemovedList;
import com.dk.play.database.SQLiteDataSource;
import com.dk.play.service.PlayService;
import com.dk.play.util.SearchPlayable;

public class RemovedFragment extends Fragment {
	private ListView removedList; 
	private StableArrayAdapter adapter;
	private ArrayList<String> list;
	private SQLiteDataSource datasource;
	private SQLRemovedList SQLList;
	private SearchPlayable search;
	private String curPath;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.removed_fragment, container, false);

		datasource = new SQLiteDataSource(this.getActivity());
		datasource.open();
		SQLList = datasource.getSQLRemovedList();
		list = SQLList.getArrayList();
		datasource.close();
		removedList = (ListView)rootView.findViewById(R.id.listView1);
		adapter = new StableArrayAdapter(this.getActivity(), android.R.layout.simple_list_item_1, list);
		removedList.setAdapter(adapter);
		registerForContextMenu(removedList);
		search = new SearchPlayable(this.getActivity());
		
		return rootView;
	}
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);

		if (v.getId() == R.id.listView1) {
		    ListView lv = (ListView) v;
		    AdapterView.AdapterContextMenuInfo acmi = (AdapterContextMenuInfo) menuInfo;
		    curPath = (String) lv.getItemAtPosition(acmi.position);
		    menu.setHeaderTitle("Auswahl:");
		    menu.add(0, 0, 0, "Wiederherstellen");
		    menu.add(0, 1, 1, "gelöscht lassen");
		}

	}
	@Override
	public boolean onContextItemSelected(MenuItem item){
	    if(item.getItemId() == 0){
	    	search.addSingelPath(curPath, true);
	    	Intent readNew = new Intent(PlayService.READ_NEW);
			getActivity().sendBroadcast(readNew);
			refreshList();
	    }else if(item.getItemId() == 1){
	    	return true;
	    }
	    return true;

	}
	private void refreshList(){
		datasource = new SQLiteDataSource(this.getActivity());
		datasource.open();
		SQLList = datasource.getSQLRemovedList();
		list = SQLList.getArrayList();
		datasource.close();
		adapter.update(list);
		adapter.notifyDataSetChanged();
	}
	private class StableArrayAdapter extends ArrayAdapter<String> {

		HashMap<String, Integer> mIdMap = new HashMap<String, Integer>();

		public StableArrayAdapter(Context context, int textViewResourceId, List<String> objects) {
			super(context, textViewResourceId, objects);
			for (int i = 0; i < objects.size(); ++i) {
				mIdMap.put(objects.get(i), i);
			}
		}
		public void update(List<String> objects){
			mIdMap = new HashMap<String, Integer>();
			clear();
			for (int i = 0; i < objects.size(); ++i) {
				mIdMap.put(objects.get(i), i);
				add(objects.get(i));
			}
		}
		@Override
		public long getItemId(int position) {
			String item = getItem(position);
			Log.d("POS", "position:" + position);
			return mIdMap.get(item);
		}

		@Override
		public boolean hasStableIds() {
			return true;
		}
	}
}