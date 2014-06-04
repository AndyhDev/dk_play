package com.dk.play.util;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.dk.play.R;
import com.dk.play.database.SQLPlaylist;
import com.dk.play.database.SQLiteDataSource;
import com.dk.play.service.PlayService;

public class DeletePlaylistDlg implements OnClickListener{
	private Context context;
	private final Dialog dialog;
	private Button cancelButton;
	private Button deleteButton;
	private String playlistName;
	private long playlistId;
	
	public DeletePlaylistDlg(Context context, String playlistName, long playlistId){
		this.context = context;
		this.playlistName = playlistName;
		this.playlistId = playlistId;
		
		dialog = new Dialog(context);
		dialog.setContentView(R.layout.delete_playlist);
		dialog.setTitle(R.string.playlist_delete);
		
		TextView text = (TextView) dialog.findViewById(R.id.text);
		text.setText(context.getString(R.string.playlist_delete_long, this.playlistName));
		cancelButton = (Button) dialog.findViewById(R.id.cancel);
		cancelButton.setOnClickListener(this);

		deleteButton = (Button) dialog.findViewById(R.id.delete);
		deleteButton.setOnClickListener(this);

	}

	public void show(){
		dialog.show();
	}
	
	private void deletePlaylist(){
		dialog.dismiss();
		SQLiteDataSource datasource = new SQLiteDataSource(context);
		datasource.open();
		SQLPlaylist playlist = datasource.getSQLPlaylist(playlistId);
		datasource.removePlaylist(playlist);
		datasource.close();
		Intent intent = new Intent(PlayService.PLAYLIST_DELETED);
		context.sendBroadcast(intent);
		Toast.makeText(context, R.string.playlist_deleted, Toast.LENGTH_LONG).show();
			
	}
	@Override
	public void onClick(View v){
		if(v.getId() == R.id.cancel){
			dialog.dismiss();
		}else if(v.getId() == R.id.delete){
			deletePlaylist();
		}
	}
}
