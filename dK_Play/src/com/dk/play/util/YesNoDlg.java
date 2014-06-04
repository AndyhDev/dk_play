package com.dk.play.util;

import com.dk.play.R;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;

public class YesNoDlg implements OnClickListener {
	private Context context;
	private String title;
	private String message;
	private AlertDialog.Builder dialogBuilder;
	private YesNoDlgListener listener;
	
	public YesNoDlg(Context context, String title, String message){
		this.context = context;
		this.title = title;
		this.message = message;
		setUpBuilder();
	}
	
	public YesNoDlg(Context context, int title, int message){
		this.context = context;
		this.title = context.getString(title);
		this.message = context.getString(message);
		setUpBuilder();
	}

	private void setUpBuilder() {
		dialogBuilder = new AlertDialog.Builder(context);
		dialogBuilder.setTitle(title);
		dialogBuilder.setMessage(message);
		dialogBuilder.setCancelable(false);
		dialogBuilder.setPositiveButton(R.string.yes, this);
		dialogBuilder.setNegativeButton(R.string.no, this);
	}
	public void setListener(YesNoDlgListener listener){
		this.listener = listener;
	}
	public void show(){
		dialogBuilder.show();
	}

	@Override
	public void onClick(DialogInterface dialog, int which) {
		switch (which){
		case DialogInterface.BUTTON_POSITIVE:
			if(listener != null){
				listener.onYes();
			}
			break;

		case DialogInterface.BUTTON_NEGATIVE:
			if(listener != null){
				listener.onNo();
			}
			break;
		}
		dialog.dismiss();
	}
}
