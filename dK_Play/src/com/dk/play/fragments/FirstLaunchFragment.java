package com.dk.play.fragments;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.dk.play.DkPlay;
import com.dk.play.R;
import com.dk.play.database.SQLSong;
import com.dk.play.util.SearchListener;
import com.dk.play.util.SearchPlayable;
import com.dk.play.util.SelectSearchFoldersDlg;
import com.dk.ui.widgets.SelectedFolderList;
import com.dk.ui.widgets.onOkListener;

public class FirstLaunchFragment extends Fragment implements OnClickListener {
	private SearchPlayable search;
	@SuppressWarnings("unused")
	private Context context;
	private RelativeLayout layout;
	private ProgressBar spinner;
	private TextView foundText;
	private Button nextBnt;
	private int foundCount = 0;
	private SelectSearchFoldersDlg dlg;
	private WakeLock lock;
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		PowerManager pm = (PowerManager) getActivity().getSystemService(Context.POWER_SERVICE);
		lock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "My Tag");
		lock.acquire();
		context = this.getActivity();
		layout = (RelativeLayout)inflater.inflate(R.layout.first_launch_fragment, container, false);
		foundText = (TextView) layout.findViewById(R.id.found);
		nextBnt = (Button)layout.findViewById(R.id.next);
		nextBnt.setOnClickListener(this);
		
		spinner = (ProgressBar)layout.findViewById(R.id.progressBar1);
		spinner.getIndeterminateDrawable().setColorFilter(getResources().getColor(R.color.dark_red), android.graphics.PorterDuff.Mode.SRC_IN);
		

		dlg = new SelectSearchFoldersDlg(getActivity());
		dlg.setListener(new onOkListener() {
			@Override
			public void onOk(SelectedFolderList list) {
				startSearch();
			}
		});
		dlg.show();

		return layout;
	}
	private void startSearch(){
		search = new SearchPlayable(this.getActivity());
		search.setSearchListener(listener);
		actFoundCount();
		
		search.search();
	}
	private void actFoundCount(){
		foundText.setText(getResources().getString(R.string.found_first_launch, foundCount));
		
	}
	private SearchListener listener = new SearchListener() {

		@Override
		public void onStart() {
		}

		@Override
		public void onFound(SQLSong song) {
			foundCount++;
			actFoundCount();
		}

		@Override
		public void onEnd(){
			spinner.setIndeterminate(false);
			foundText.setText(getResources().getString(R.string.found_end_first_launch, foundCount));
			nextBnt.setEnabled(true);
		}
	};

	@Override
	public void onClick(View v) {
		if(v.getId() == R.id.next){
			lock.release();
			Intent intent = new Intent(this.getActivity(), DkPlay.class);
			startActivity(intent);
			this.getActivity().finish();
		}
	}
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if(requestCode == SelectSearchFoldersDlg.requestCode){
			dlg.processResult(data);
		}
	}
	
}
