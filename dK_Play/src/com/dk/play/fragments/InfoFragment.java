package com.dk.play.fragments;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RelativeLayout;

import com.dk.play.Licenses;
import com.dk.play.R;
import com.dk.play.WhatsNew;

public class InfoFragment extends Fragment implements OnClickListener{
	private RelativeLayout layout;
	private ImageButton newBnt;
	private Button licensesBnt;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		layout = (RelativeLayout)inflater.inflate(R.layout.info_fragment, container, false);
		
		newBnt = (ImageButton) layout.findViewById(R.id.new_bnt);
		newBnt.setOnClickListener(this);
		
		licensesBnt =  (Button) layout.findViewById(R.id.licenses);
		licensesBnt.setOnClickListener(this);
		
		return layout;
	}

	@Override
	public void onClick(View v) {
		int id = v.getId();
		if(id == R.id.new_bnt){
			Intent intent = new Intent(getActivity(), WhatsNew.class);
			startActivity(intent);
		}else if(id == R.id.licenses){
			Intent intent = new Intent(getActivity(), Licenses.class);
			startActivity(intent);
		}
	}
}
