package com.dk.play.util;

import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.widget.Toast;

import com.dk.play.R;
import com.dk.play.adv.AdvControl;
import com.dk.play.adv.AdvRemoved;
import com.dk.play.adv.AdvSQLiteDataSource;
import com.dk.play.database.SQLSong;
import com.dk.util.network.ApiCallListener;
import com.dk.util.network.dKApiCall;
import com.dk.util.network.dKSession;

public class CloudDeleteDlg {
	private Context context;
	private Activity activity;
	private CloudItem item;
	private String title;
	private String message;
	private AdvControl adv;
	private ProgressDialog dialog;
	private CloudDeleteDlgListener listener;
	
	public CloudDeleteDlg(Activity activity, CloudItem item){
		this.activity = activity;
		this.context = activity;
		this.item = item;
		adv = new AdvControl(context);
		title = context.getString(R.string.cloud_song_delete);
		message = item.getTitle();
		setUp();
	}

	private void setUp() {
		dialog = new ProgressDialog(context);;
		dialog.setTitle(title);
		dialog.setMessage(message);
		dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		dialog.setCancelable(false);
		dialog.setIndeterminate(true);
	}
	public void show(){
		dialog.show();
		Thread thread = new Thread(){
			@Override
			public void run() {
				dKSession session = adv.getSession();
				if(session != null){
					dKApiCall call = new dKApiCall(activity, session, "adv_delete_song");
					call.addParam("song_id", item.getId());
					call.setListener(new ApiCallListener() {						
						@Override
						public void onApiCallSuccess(JSONObject data) {
							String msg = data.optString("msg");
							if(msg.equals("deleted")){
								if(listener != null){
									listener.onSuccess(item);
								}
								addRemoved();
								Toast.makeText(context, R.string.cloud_delete_success, Toast.LENGTH_LONG).show();
							}else{
								Toast.makeText(context, R.string.cloud_delete_error, Toast.LENGTH_LONG).show();
							}
							dialog.dismiss();
						}
						@Override
						public void onApiCallError(int code) {
							if(code == 35){
								if(listener != null){
									listener.onSuccess(item);
								}
								addRemoved();
								Toast.makeText(context, R.string.cloud_delete_success, Toast.LENGTH_LONG).show();
							}else{
								Toast.makeText(context, R.string.cloud_delete_error, Toast.LENGTH_LONG).show();
							}
							dialog.dismiss();
						}
					});
					
					call.call();
				}else{
					dialog.dismiss();
				}
				
			}
		};
		thread.start();
	}
	private void addRemoved(){
		AdvSQLiteDataSource dataSource = new AdvSQLiteDataSource(context);
		dataSource.open();
		Long localId = -1l;
		SQLSong song = item.getSQLSong();
		if(song != null){
			localId = song.getId();
			if(dataSource.isRemoved(localId)){
				return;
			}
		}
		AdvRemoved rm = new AdvRemoved(-1l, localId, item.getId(), item.getTitle(), item.getArtist());
		
		dataSource.addAdvRemoved(rm);
		dataSource.close();
	}
	public CloudDeleteDlgListener getListener() {
		return listener;
	}

	public void setListener(CloudDeleteDlgListener listener) {
		this.listener = listener;
	}
	
}
