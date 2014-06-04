package com.dk.play.util;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

import com.dk.play.R;
import com.dk.play.database.SQLPlaylist;
import com.dk.play.database.SQLiteDataSource;
import com.dk.play.service.PlayService;

public class NewPlaylistDlg implements OnClickListener{
	private Context context;
	private final Dialog dialog;
	private EditText name;
	private Button cancelButton;
	private Button createButton;

	public NewPlaylistDlg(Context context){
		this.context = context;
		dialog = new Dialog(context);
		dialog.setContentView(R.layout.new_playlist);
		dialog.setTitle("Neue Playlist erstellen");

		name = (EditText) dialog.findViewById(R.id.playlist_name);
		name.setOnEditorActionListener(new OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				createPlaylist();
				return false;
			}
		});
		name.addTextChangedListener(new TextWatcher(){
	        public void afterTextChanged(Editable s) {}
	        public void beforeTextChanged(CharSequence s, int start, int count, int after){}
	        public void onTextChanged(CharSequence s, int start, int before, int count){
	        	name.setError(null);
	        }
	    }); 
		cancelButton = (Button) dialog.findViewById(R.id.cancel);
		cancelButton.setOnClickListener(this);

		createButton = (Button) dialog.findViewById(R.id.create);
		createButton.setOnClickListener(this);

	}

	public void show(){
		dialog.show();
	}
	
	private void createPlaylist(){
		String plName = name.getText().toString();
		if(plName.length() == 0){
			Animation shake = AnimationUtils.loadAnimation(context, R.anim.shake);
			name.startAnimation(shake);
			name.setError(context.getResources().getString(R.string.playlist_no_name));
			return;
		}
		SQLiteDataSource datasource = new SQLiteDataSource(context);
		datasource.open();
		if(datasource.isPlaylist(plName)){
			datasource.close();
			Animation shake = AnimationUtils.loadAnimation(context, R.anim.shake);
			name.startAnimation(shake);
			name.setError(context.getResources().getString(R.string.playlist_exists));
		}else{
			dialog.dismiss();
			SQLPlaylist playlist = new SQLPlaylist(plName);
			datasource.addPlaylist(playlist);
			datasource.close();
			Toast.makeText(context, R.string.playlist_created, Toast.LENGTH_LONG).show();
			
			Intent intent = new Intent(PlayService.PLAYLIST_CREATED);
			context.sendBroadcast(intent);
		}
	}
	@Override
	public void onClick(View v){
		if(v.getId() == R.id.cancel){
			dialog.dismiss();
		}else if(v.getId() == R.id.create){
			createPlaylist();
		}
	}
}
