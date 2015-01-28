package com.dk.play.fragments;

import java.io.File;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Fragment;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
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
import android.widget.TextView;
import android.widget.Toast;

import com.dk.play.R;
import com.dk.play.adv.AdvControl;
import com.dk.play.database.SQLSong;
import com.dk.play.database.SQLSongList;
import com.dk.play.service.DownloadService;
import com.dk.play.service.PlayService;
import com.dk.play.util.CloudAdapter;
import com.dk.play.util.CloudDeleteDlg;
import com.dk.play.util.CloudDeleteDlgListener;
import com.dk.play.util.CloudItem;
import com.dk.play.util.CloudList;
import com.dk.play.util.DownloadSongDlg;
import com.dk.play.util.DownloadSongDlgListener;
import com.dk.play.util.ExpandAnimation;
import com.dk.play.util.YesNoDlg;
import com.dk.play.util.YesNoDlgListener;
import com.dk.style.dKColor;
import com.dk.ui.widgets.ProgressBar;
import com.dk.util.FileSize;
import com.dk.util.IO;
import com.dk.util.JSON;
import com.dk.util.network.ApiCallListener;
import com.dk.util.network.dKApiCall;
import com.dk.util.network.dKSession;

public class CloudSongsFragment extends Fragment implements OnItemClickListener{
	private final String TAG = "CloudSongsFragment";
	private AdvControl adv;
	private ListView list;
	private ProgressBar freeSpace;
	private TextView text;
	private CloudAdapter adapter;
	private PlayService service;
	private Context context;
	private JSONObject cloudData;
	private CloudItem contextItem = null;
	
	private final int MENU_DELETE_CACHE = 1;
	private final int MENU_DELETE_CLOUD = 2;
	private final int MENU_STORE = 3;
	private final int MENU_PLAY = 4;
	
	public CloudSongsFragment() {
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_cloud_songs, container, false);
		context = getActivity();
		
		if(savedInstanceState != null){
			if(savedInstanceState.containsKey("data")){
				try {
					cloudData = new JSONObject(savedInstanceState.getString("data"));
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		}
		
		adv = new AdvControl(getActivity());
		list = (ListView) rootView.findViewById(R.id.list);
		adapter = new CloudAdapter(this.getActivity(), null);
		list.setAdapter(adapter);
		list.setOnItemClickListener(this);
		registerForContextMenu(list);
		
		freeSpace = (ProgressBar) rootView.findViewById(R.id.free_space);
		text = (TextView) rootView.findViewById(R.id.space_text);
		if(cloudData != null){
			processData();
		}else{
			if(!loadData()){
				Log.d(TAG, "readCloud 1");
				readCloud();
			}
		}
		return rootView;
	}
	
	public CloudAdapter getAdapter(){
		return adapter;
	}
	
	public void scrollTo(int index){
		list.setSelection(index);
	}
	public void activateItem(int index){
		list.setSelection(index);
		View expand = getViewByPosition(index).findViewById(R.id.expand);
		ExpandAnimation expandAni = new ExpandAnimation(expand, 500);
		expand.startAnimation(expandAni);
		adapter.expandItem(index);
		
	}
	public View getViewByPosition(int pos) {
	    final int firstListItemPosition = list.getFirstVisiblePosition();
	    final int lastListItemPosition = firstListItemPosition + list.getChildCount() - 1;

	    if (pos < firstListItemPosition || pos > lastListItemPosition ) {
	        return list.getAdapter().getView(pos, null, list);
	    } else {
	        final int childIndex = pos - firstListItemPosition;
	        return list.getChildAt(childIndex);
	    }
	}
	public void readCloud(){
		getActivity().setProgressBarIndeterminateVisibility(true);
		Thread thread = new Thread(){
			@Override
			public void run() {
				dKSession session = adv.getSession();
				dKApiCall down = new dKApiCall(getActivity(), session, "get_cloud");
				down.setListener(new ApiCallListener() {
					@Override
					public void onApiCallSuccess(JSONObject data) {
						cloudData = data;
						saveSpace();
						saveData();
						processData();
						getActivity().setProgressBarIndeterminateVisibility(false);
					}
					
					@Override
					public void onApiCallError(int code) {
						Toast.makeText(getActivity(), R.string.cloud_error1, Toast.LENGTH_LONG).show();
						getActivity().setProgressBarIndeterminateVisibility(false);
					}
				});
				down.call();
			}
			
		};
		thread.start();
	}
	private void saveData(){
		if(cloudData != null){
			File f = new File(context.getFilesDir(), "cloudData");
			IO.write(cloudData.toString(), f);
			f.setLastModified(System.currentTimeMillis());
		}
	}
	private boolean loadData(){
		File f = new File(context.getFilesDir(), "cloudData");
		if(!f.exists()){
			Log.d(TAG, "readCloud 2");
			return false;
		}
		if(System.currentTimeMillis() - f.lastModified() > 720000){
			Log.d(TAG, "readCloud 3 " + (System.currentTimeMillis() - f.lastModified()));
			return false;
		}
		String data = IO.readToString(f);
		if(data != null){
			try {
				cloudData = new JSONObject(data);
				processData();
				return true;
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		Log.d(TAG, "readCloud 4");
		return false;
	}
	private void saveSpace(){
		JSONObject quota = cloudData.optJSONObject("quota");
		if(quota != null){
			Long total = quota.optLong("total");
			Long inUse = quota.optLong("in_use");
			if(total != null && inUse != null){
				Log.d(TAG, "total:" + total);
				Log.d(TAG, "inUse:" + inUse);
				adv.setTotalSpace(total);
				adv.setSpaceInUse(inUse);
			}
		}
	}
	private void processData(){
		if(cloudData == null){
			return;
		}
		JSONObject quota = cloudData.optJSONObject("quota");
		if(quota != null){
			Long total = quota.optLong("total");
			Long inUse = quota.optLong("in_use");
			if(total != null && inUse != null){
				Log.d(TAG, "total:" + total);
				Log.d(TAG, "inUse:" + inUse);
				int progress = (int) (100.0 / total * inUse);
				if(progress > 70){
					freeSpace.setFillColor(dKColor.RED);
				}else if(progress > 50){
					freeSpace.setFillColor(dKColor.ORANGE);
				}
				freeSpace.setProgress(progress);
				String sTotal = FileSize.humanReadableSize(total);
				String sInUse = FileSize.humanReadableSize(inUse);
				text.setText("Von " + sTotal + " sind " + sInUse + " benutzt");
			}
		}
		JSONArray files = cloudData.optJSONArray("files");
		if(files != null){
			CloudList list = new CloudList(files);
			adapter.setList(list);
			adapter.notifyDataSetChanged();
		}
	}
	public void removeItem(CloudItem item){
		if(cloudData != null){
			JSONArray files = cloudData.optJSONArray("files");
			if(files == null){
				return;
			}
			for(int i = 0; i < files.length(); i++){
				try {
					JSONObject obj = files.getJSONObject(i);
					String id = obj.getString("id");
					if(id.equals(item.getId())){
						files = JSON.RemoveJSONArray(files, i);
						cloudData.put("files", files);
						CloudList list = new CloudList(files);
						adapter.setList(list);
						adapter.notifyDataSetChanged();
						saveData();
						break;
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		}
	}
	@Override
	public void onResume() {
		super.onResume();
		if(service == null){
			Intent intent= new Intent(this.getActivity(), PlayService.class);
			context.startService(intent);
			context.bindService(intent, mConnection, Context.BIND_ABOVE_CLIENT);
		}
	}
	@Override
	public void onPause() {
		super.onPause();
		if(service != null){
			context.unbindService(mConnection);
			service = null;
		}
	}
	@Override
	public void onStop() {
		super.onStop();
		if(service != null){
			context.unbindService(mConnection);
			service = null;
		}
	}
	private ServiceConnection mConnection = new ServiceConnection() {

		public void onServiceConnected(ComponentName className, IBinder binder) {
			PlayService.MyBinder b = (PlayService.MyBinder) binder;
			service = b.getService();
		}

		public void onServiceDisconnected(ComponentName className) {
			service  = null;
		}
	};
	private SQLSong checkCache(CloudItem item){
		File cover = new File(context.getCacheDir(), item.getId() + ".cover");
		File file = new File(context.getCacheDir(), item.getId() + ".file");
		File attr_file = new File(context.getCacheDir(), item.getId() + ".attr");
		if(cover.exists() && file.exists() && attr_file.exists()){
			try {
				JSONObject attr = new JSONObject(IO.readToString(attr_file));
				int id = -1;
				String title = attr.getString("title");
	    		String artist = attr.getString("artist");
	    		String album = attr.getString("album");
	    		String genre = attr.getString("genre");
	    		String coverA;
	    		if(cover.length() < 10){
	    			coverA = "no";
	    		}else{
	    			coverA = cover.getAbsolutePath();
	    		}
	    		Log.d("cover", coverA);
	    		int rating = attr.getInt("rating");
	    		int type = attr.getInt("type");
	    		SQLSong song = new SQLSong(id, title, artist, album, genre, coverA, rating, 0, file.getAbsolutePath(), 0, type, 0);
	    		song.setDontSQL(true);
	    		if(!coverA.equals("no")){
	    			Log.d("cover", "usepath");
	    			song.setCoverUsePath(true);
	    		}
	    		return song;
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		
		return null;
	}
	private void requestDownload(final CloudItem item){
		SQLSong song = checkCache(item);
		if(song != null){
			if(service != null){
				service.setSong(song, true);
			}
			return;
		}
		YesNoDlg dlg = new YesNoDlg(context, R.string.play_cloud_song, R.string.play_cloud_song_msg);
		dlg.setListener(new YesNoDlgListener() {
			@Override
			public void onYes() {
				startDownloadSong(item);
			}
			
			@Override
			public void onNo() {
			}
		});
		dlg.show();
	}
	private void requestCloudDelete(final CloudItem item){
		YesNoDlg dlg = new YesNoDlg(context, R.string.delete_cloud, R.string.delete_cloud_msg);
		dlg.setListener(new YesNoDlgListener() {
			@Override
			public void onYes() {
				deleteCloud(item);
			}
			
			@Override
			public void onNo() {
			}
		});
		dlg.show();
	}
	private void deleteCloud(CloudItem item){
		CloudDeleteDlg dlg = new CloudDeleteDlg(getActivity(), item);
		dlg.setListener(new CloudDeleteDlgListener() {
			
			@Override
			public void onSuccess(CloudItem item) {
				removeItem(item);
			}
		});
		dlg.show();
	}
	private void startDownloadSong(CloudItem item){
		DownloadSongDlg dlg = new DownloadSongDlg(getActivity(), item);
		dlg.setListener(new DownloadSongDlgListener() {	
			@Override
			public void onSong(SQLSong song) {
				processData();
				if(service != null){
					service.setSong(song, true);
				}
			}
		});
		dlg.show();
	}
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		View expand = view.findViewById(R.id.expand);
		ExpandAnimation expandAni = new ExpandAnimation(expand, 500);
		expand.startAnimation(expandAni);
		adapter.expandItem(position);
		
		/*CloudItem item = adapter.getItem(position);
		SQLSong song = item.getSQLSong();
		if(song == null){
			requestDownload(item);
			return;
		}
		if(service != null){
			SQLSongList playlist = new SQLSongList();
			playlist.add(song);
			service.setSQLSongList(playlist);
			service.setSongById(song.getId());
		}*/
	}
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		if (v.getId()==R.id.list) {
		    AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)menuInfo;
		    contextItem = adapter.getItem(info.position);
		    menu.setHeaderTitle(contextItem.getTitle());
		    if(contextItem.getOnDevice() == CloudItem.ON_CACHE){
		    	menu.add(Menu.NONE, MENU_DELETE_CACHE, 0, getString(R.string.deleteCache));
		    	menu.add(Menu.NONE, MENU_DELETE_CLOUD, 1, getString(R.string.deleteCloud));
		    	menu.add(Menu.NONE, MENU_STORE, 2, getString(R.string.storeDevice));
		    	menu.add(Menu.NONE, MENU_PLAY, 3, getString(R.string.listen));
		    }else if(contextItem.getOnDevice() == CloudItem.NOT_ON_DEVICE){
		    	menu.add(Menu.NONE, MENU_DELETE_CLOUD, 0, getString(R.string.deleteCloud));
		    	menu.add(Menu.NONE, MENU_STORE, 1, getString(R.string.storeDevice));
		    	menu.add(Menu.NONE, MENU_PLAY, 2, getString(R.string.listen));
		    }else{
		    	menu.add(Menu.NONE, MENU_DELETE_CLOUD, 0, getString(R.string.deleteCloud));
		    	menu.add(Menu.NONE, MENU_PLAY, 1, getString(R.string.listen));
		    }
		}
	}
	@Override
	public boolean onContextItemSelected(MenuItem item) {
	  if(contextItem != null){
		  int id = item.getItemId();
		  if(id == MENU_DELETE_CACHE){
			  CloudList.deleteCache(contextItem);
			  processData();
		  }else if(id == MENU_DELETE_CLOUD){
			  requestCloudDelete(contextItem);
		  }else if(id == MENU_STORE){
			  Log.d(TAG, "store");
			  Intent downIntent = new Intent(getActivity(), DownloadService.class);
			  downIntent.putExtra(DownloadService.KEY_ID, contextItem.getId());
		      getActivity().startService(downIntent);
		  }else if(id == MENU_PLAY){
			  SQLSong song = contextItem.getSQLSong();
			  if(song == null){
				  requestDownload(contextItem);
			  }else{
				  if(service != null){
					  SQLSongList playlist = new SQLSongList();
					  playlist.add(song);
					  service.setSQLSongList(playlist);
					  service.setSongById(song.getId());
				  }
			  }
		  }
	  }
	  return true;
	}
	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putString("data", cloudData.toString());
		super.onSaveInstanceState(outState);
	}
	
}
