package com.dk.play.util;

import java.io.File;

import android.os.Environment;

public class Paths {
	public static File getCoverFile(String name){
		File dir = getCoverDir();
		return new File(dir, name + ".jpg");
	}
	public static String getCoverPath(String name){
		File dir = getCoverDir();
		return new File(dir, name + ".jpg").getAbsolutePath();
	}
	public static File getCoverDir(){
		//return new File(cordova.getActivity().getFilesDir(), "covers");
		File dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath(), "covers");
		if(!dir.exists()){
			dir.mkdirs();
		}
		return dir;
	}
	public static File getAdvSongDir(){
		File dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath(), "dkPlaySync");
		if(!dir.exists()){
			dir.mkdirs();
		}
		return dir;
	}
}
