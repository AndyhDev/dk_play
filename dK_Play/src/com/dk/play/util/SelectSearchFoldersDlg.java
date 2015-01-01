package com.dk.play.util;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

import com.dk.play.R;
import com.dk.ui.widgets.FolderSelector;
import com.dk.ui.widgets.FolderSelectorDlg;
import com.dk.ui.widgets.SelectedFolderList;
import com.dk.ui.widgets.onOkListener;

public class SelectSearchFoldersDlg extends Dialog implements OnCheckedChangeListener, android.view.View.OnClickListener{
	private Activity activity;
	private CheckBox normal;
	private CheckBox all;
	private CheckBox custom;
	private Button ok;
	private SelectedFolderList selected = new SelectedFolderList();
	private FolderSelectorDlg dlg;
	
	public static final int requestCode = 12367;
	
	private onOkListener listener;
	
	public SelectSearchFoldersDlg(Activity activity) {
		super(activity);
		this.activity = activity;
	}
	
	@Override
	  protected void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    requestWindowFeature(Window.FEATURE_NO_TITLE);
	    setContentView(R.layout.search_folder_dlg);
	    
	    setCancelable(false);
	    
	    normal = (CheckBox) findViewById(R.id.normal);
	    all = (CheckBox) findViewById(R.id.all);
	    custom = (CheckBox) findViewById(R.id.custom);
	    
	    normal.setOnCheckedChangeListener(this);
	    all.setOnCheckedChangeListener(this);
	    custom.setOnCheckedChangeListener(this);
	    
	    ok = (Button) findViewById(R.id.ok);
	    ok.setOnClickListener(this);
	  }

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		int id = buttonView.getId();
		if(id == R.id.custom && isChecked){
			Bitmap bmp = Image.decodeSampledBitmapFromResource(getContext().getResources(), R.drawable.ic_launcher, 100, 100);
			dlg = new FolderSelectorDlg(getContext(), selected, bmp);
			dlg.setListener(new onOkListener() {
				@Override
				public void onOk(SelectedFolderList list) {
					selected = list;
				}
			});
			dlg.show();
		}
	}

	public void processResult(Intent data){
		SelectedFolderList sel = data.getParcelableExtra(FolderSelector.selectedKey);
		if(sel != null){
			selected = sel;
		}
	}
	@Override
	public void onClick(View v) {
		if(v.getId() == R.id.ok){
			if(!normal.isChecked() && !all.isChecked() && !custom.isChecked()){
				showNoSelectError();
				return;
			}
			
			SelectedFolderList list = new SelectedFolderList();

			if(custom.isChecked()){
				if(selected.size() == 0){
					showNoSelectError();
					return;
				}else{
					list.addList(selected);
				}
			}
			if(normal.isChecked()){
				list.add(Environment.getExternalStorageDirectory());
			}
			if(all.isChecked()){
				list.add("/");
			}
			list.writeToSettings(SelectedFolderList.folderKey, activity.getSharedPreferences("other", Context.MODE_PRIVATE));
			if(listener != null){
				listener.onOk(list);
			}
			dismiss();
						
		}
	}

	private void showNoSelectError() {
		AlertDialog.Builder alertDlg = new AlertDialog.Builder(getContext());
		alertDlg.setTitle(R.string.no_choice);
		alertDlg.setMessage(R.string.no_choice_msg);
		alertDlg.setNeutralButton(R.string.ok, new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
		alertDlg.show();
		
	}

	public void setListener(onOkListener listener) {
		this.listener = listener;
	}
	
}
