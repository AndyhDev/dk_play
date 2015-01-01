package com.dk.play.adv;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import com.dk.util.network.LoginListener;
import com.dk.util.network.dKLogin;
import com.dk.util.network.dKSession;

public class AdvControl{
	@SuppressWarnings("unused")
	private final String TAG = "Adv";
	
	@SuppressWarnings("unused")
	private Context context;
	private SharedPreferences settings;
	
	public static final String settingsName = "adv_settings";
	public static final String infoKey = "info";
	public static final String activeKey = "active";
	public static final String lastUpdate = "last_update";
	
	public static final String userName = "user_name";
	public static final String password = "password";
	public static final String loginOK = "login_ok";
	
	public static final String consumeKey = "consume";
	public static final String consumeSpaceKey = "consume_space";
	public static final String totalSpaceKey = "total_space";
	public static final String spaceInUseKey = "space_in_use";
	
	public static final long OneDay = 24 * 60 * 60 * 1000;
	public static final long DateAlert = OneDay * 14;
	
	public static final String curSkuKey = "cur_sku";
	
	private dKSession tmpSession;
	
	public AdvControl(Context context){
		this.context = context;
		settings = context.getSharedPreferences(settingsName, Context.MODE_PRIVATE);
	}
	
	public SharedPreferences getSettings(){
		return settings;
	}
	
	public boolean getActive(){
		return settings.getBoolean(activeKey, false);
	}

	public void disconnect() {
		Editor edit = settings.edit();
		edit.putBoolean(activeKey, false);
		edit.putBoolean(loginOK, false);
		edit.putString(password, "");
		edit.putString(userName, "");
		edit.commit();
	}
	public dKSession getSession(){
		tmpSession = null;
		dKLogin login = new dKLogin(settings.getString(userName, ""), settings.getString(password, ""));
		login.setSyncModus(true);
		login.setListener(new LoginListener() {
			
			@Override
			public void onLoginSuccess(dKSession session) {
				setLoginOK();
				tmpSession = session;
			}
			
			@Override
			public void onLoginError(int code) {
			}
		});
		login.login();
		return tmpSession;
	}
	public void setLoginOK(){
		Editor edit = getSettings().edit();
		edit.putBoolean(AdvControl.loginOK, true);
		edit.putBoolean(AdvControl.activeKey, true);
		edit.commit();
	}
	public long getTotalSpace(){
		return settings.getLong(totalSpaceKey, 0l);
	}
	public void setTotalSpace(long space){
		Editor edit = settings.edit();
		edit.putLong(totalSpaceKey, space);
		edit.commit();
	}	
	public long getSpaceInUse(){
		return settings.getLong(spaceInUseKey, 0l);
	}
	public void setSpaceInUse(long space){
		Editor edit = settings.edit();
		edit.putLong(spaceInUseKey, space);
		edit.commit();
	}
	public boolean isAccount(){
		return settings.getBoolean(loginOK, false);
	}
	public void setCurSku(String sku){
		Editor edit = settings.edit();
		edit.putString(curSkuKey, sku);
		edit.commit();
	}	

}
