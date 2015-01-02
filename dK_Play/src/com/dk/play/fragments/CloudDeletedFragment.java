package com.dk.play.fragments;

import java.util.List;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.dk.play.R;
import com.dk.play.adv.AdvRemoved;
import com.dk.play.adv.AdvSQLiteDataSource;
import com.dk.play.util.CloudDeletedAdapter;

public class CloudDeletedFragment extends Fragment implements OnItemClickListener{
	private static final String TAG = "CloudDeletedFragment";
	@SuppressWarnings("unused")
	private Context context;
	private ListView list;
	private CloudDeletedAdapter adapter;
	private AdvRemoved contextItem;
	private AdvSQLiteDataSource dataSource;
	
	private static final int MENU_RELEASE = 0;
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.list_layout, container, false);
		context = getActivity();
		
		dataSource = new AdvSQLiteDataSource(getActivity());
		dataSource.open();
		List<AdvRemoved> items = dataSource.getAllAdvRemoved();
		Log.d(TAG, "" + items.size());
		dataSource.close();
		adapter = new CloudDeletedAdapter(getActivity(), items);
		
		list = (ListView) rootView.findViewById(R.id.list_view);
		list.setAdapter(adapter);
		list.setOnItemClickListener(this);
		registerForContextMenu(list);
		
		return rootView;
	}
	private void readNew(){
		dataSource.open();
		List<AdvRemoved> items = dataSource.getAllAdvRemoved();
		dataSource.close();
		adapter.setList(items);
		adapter.notifyDataSetChanged();
	}
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		if (v.getId()==R.id.list_view) {
		    AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)menuInfo;
		    contextItem = adapter.getItem(info.position);
		    menu.setHeaderTitle(contextItem.getTitle());
		    menu.add(Menu.NONE, MENU_RELEASE, 0, getString(R.string.cloud_release));
		}
	}
	@Override
	public boolean onContextItemSelected(MenuItem item) {
	  if(contextItem != null){
		  int id = item.getItemId();
		  if(id == MENU_RELEASE){
			  dataSource.open();
			  dataSource.removeAdvRemoved(contextItem);
			  dataSource.close();
			  readNew();
		  }
	  }
	  return true;
	}
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		// TODO Auto-generated method stub
		
	}
}
