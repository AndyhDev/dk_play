package com.dk.play.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.dk.play.R;

public class InfoFragment extends Fragment{
	private RelativeLayout layout;

	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		layout = (RelativeLayout)inflater.inflate(R.layout.info_fragment, container, false);
		
		return layout;
	}
}
