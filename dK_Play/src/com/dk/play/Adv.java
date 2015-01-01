package com.dk.play;

import org.json.JSONObject;

import android.app.ActionBar;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.dk.account.LoginActivity;
import com.dk.play.adv.AdvControl;
import com.dk.play.util.ActionBarImage;
import com.dk.play.util.LActivity;
import com.dk.play.util.MD5FileCache;
import com.dk.style.dKColor;
import com.dk.ui.widgets.FolderSelector;
import com.dk.ui.widgets.SelectedFolderList;
import com.dk.util.FileSize;
import com.dk.util.network.ApiCallListener;
import com.dk.util.network.LoginListener;
import com.dk.util.network.dKApiCall;
import com.dk.util.network.dKLogin;
import com.dk.util.network.dKSession;

public class Adv extends LActivity implements OnClickListener {
	private static final String TAG = "Adv";

	private ActionBar bar;
	private ImageView accountState;
	private ProgressBar progressAccount;
	private MenuItem reloadBnt;
	private MenuItem newLoginBnt;
	private com.dk.ui.widgets.ProgressBar freeSpace;
	private TextView freeSpaceText;

	private int setLoginCode = 1003;

	private String user;
	private String passw;

	@SuppressWarnings("unused")
	private ActionBarImage actionBarImage;
	private boolean useBgImages = false;
	private AdvControl adv;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		useBgImages = PreferenceManager.getDefaultSharedPreferences(this).getBoolean("use_bg_images", false);
		if(useBgImages){
			setTheme(R.style.AppTheme2);
		}
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setProgressBarIndeterminateVisibility(true);

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_adv);

		adv = new AdvControl(this);

		bar = getActionBar();
		bar.setTitle(R.string.not_loged_in);

		SharedPreferences settings = adv.getSettings();
		settings.registerOnSharedPreferenceChangeListener(new OnSharedPreferenceChangeListener() {			
			@Override
			public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,String key) {
				setState();
			}
		});
		if(!settings.contains(AdvControl.userName)){
			Intent i = new Intent(this, LoginActivity.class);
			startActivityForResult(i, setLoginCode);
		}else{
			user = settings.getString(AdvControl.userName, "user");
			passw = settings.getString(AdvControl.password, "passw");
			setupUi();
		}
		actionBarImage = new ActionBarImage(this);
	}

	private void setupUi(){
		bar.setTitle(getString(R.string.adv) +":  " + user);

		accountState = (ImageView) findViewById(R.id.account_state);

		progressAccount = (ProgressBar) findViewById(R.id.progress_account);

		freeSpace = (com.dk.ui.widgets.ProgressBar) findViewById(R.id.free_space);
		freeSpaceText = (TextView) findViewById(R.id.space_text);

		actStates();
	}

	private Long getTimestamp(){
		return (Long) System.currentTimeMillis()/1000;
	}
	private void actStates(){
		SharedPreferences settings = adv.getSettings();
		long lastUpdate = Long.valueOf(settings.getLong(AdvControl.lastUpdate, 0L));
		long now = getTimestamp();
		if(now - lastUpdate > 60*60){
			runUpdate();
		}else{
			setState();
		}
	}
	private void setLoginState(){
		SharedPreferences settings = adv.getSettings();;
		boolean login= settings.getBoolean(AdvControl.loginOK, false);
		if(login){
			accountState.setImageResource(R.drawable.green_point);
		}else{
			accountState.setImageResource(R.drawable.red_point);
		}
		progressAccount.setVisibility(View.INVISIBLE);
	}
	private void setState(){
		SharedPreferences settings = adv.getSettings();
		boolean login= settings.getBoolean(AdvControl.loginOK, false);
		Log.d("login", "" + login);
		if(login){
			accountState.setImageResource(R.drawable.green_point);
		}else{
			accountState.setImageResource(R.drawable.red_point);
		}
		progressAccount.setVisibility(View.INVISIBLE);
		actSpace();
	}
	private void actSpace(){
		long total = adv.getTotalSpace();
		long inUse = adv.getSpaceInUse();

		int progress = (int) (100.0 / total * inUse);
		if(progress > 70){
			freeSpace.setFillColor(dKColor.RED);
		}else if(progress > 50){
			freeSpace.setFillColor(dKColor.ORANGE);
		}
		freeSpace.setProgress(progress);
		String sTotal = FileSize.humanReadableSize(total);
		String sInUse = FileSize.humanReadableSize(inUse);
		freeSpaceText.setText("Von " + sTotal + " sind " + sInUse + " benutzt");
	}
	private void runUpdate(){
		Log.d(TAG, "runUpdate()");
		progressAccount.setVisibility(View.VISIBLE);
		progressAccount.setIndeterminate(true);

		if(reloadBnt != null){
			reloadBnt.setEnabled(false);
			newLoginBnt.setEnabled(false);
		}
		Log.d(TAG, "runUpdate()1");
		dKLogin login = new dKLogin(this, user, passw);
		login.setListener(new LoginListener() {

			@Override
			public void onLoginSuccess(dKSession session) {
				Log.d(TAG, "runUpdate()2");
				Editor edit = adv.getSettings().edit();
				edit.putBoolean(AdvControl.loginOK, true);
				edit.putBoolean(AdvControl.activeKey, true);
				edit.commit();
				setLoginState();
				dKApiCall call2 = new dKApiCall(Adv.this, session, "adv_get_space_info");
				call2.setListener(new ApiCallListener() {
					@Override
					public void onApiCallSuccess(JSONObject data) {
						JSONObject quota = data.optJSONObject("quota");
						if(quota != null){
							Long total = quota.optLong("total");
							Long inUse = quota.optLong("in_use");
							if(total != null && inUse != null){
								adv.setTotalSpace(total);
								adv.setSpaceInUse(inUse);
								actSpace();
							}
						}
					}
					@Override
					public void onApiCallError(int code) {
						Log.d(TAG, "runUpdate()3:" + code);
					}
				});
				call2.call();
			}

			@Override
			public void onLoginError(int code) {
				Log.d(TAG, "runUpdate()4:" + code);
				Editor edit = adv.getSettings().edit();
				edit.putBoolean(AdvControl.loginOK, false);
				edit.putBoolean(AdvControl.activeKey, false);
				edit.commit();
				setState();
				if(reloadBnt != null){
					reloadBnt.setEnabled(true);
					newLoginBnt.setEnabled(true);
				}
			}
		});
		login.login();
		Editor edit = adv.getSettings().edit();
		edit.putLong(AdvControl.lastUpdate, getTimestamp());
		edit.commit();
	}
	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	@Override
	public void onClick(View v) {
		@SuppressWarnings("unused")
		int id = v.getId();
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		MenuInflater inflater = getMenuInflater();
		if(useBgImages){
			inflater.inflate(R.menu.adv_l, menu);
		}else{
			inflater.inflate(R.menu.adv, menu);
		}
		reloadBnt = menu.getItem(0);
		newLoginBnt = menu.getItem(1);
		return true;
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if(id == R.id.new_login){
			Intent i = new Intent(this, LoginActivity.class);
			startActivityForResult(i, setLoginCode);
		}else if(id == R.id.reload){
			runUpdate();
		}else if(id == R.id.disconnect){
			adv.disconnect();
			user = "";
			passw = "";
			runUpdate();
		}
		return super.onOptionsItemSelected(item);
	}
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		setupUi();
		if(requestCode == setLoginCode){
			try{
				user = data.getStringExtra(LoginActivity.DK_USERNAME);
				passw = data.getStringExtra(LoginActivity.DK_PASSWORD);
				Editor edit = adv.getSettings().edit();
				edit.putString(AdvControl.userName, user);
				edit.putString(AdvControl.password, passw);
				edit.commit();
				runUpdate();
				setupUi();
				genMd5();
			}catch(NullPointerException e){
				e.printStackTrace();
				user = "";
				passw = "";
				setupUi();
			}
		}else if(requestCode == 90){
			Log.d(TAG, "folder");
			SelectedFolderList list = data.getParcelableExtra(FolderSelector.selectedKey);
			Log.d(TAG, "size:" + list.size());
		}
	}

	private void genMd5() {
		final ProgressDialog progress = ProgressDialog.show(this, getString(R.string.data_act), getString(R.string.please_wait), true);

		new Thread(new Runnable() {
			@Override
			public void run(){
				MD5FileCache cache = new MD5FileCache();
				cache.genMd5Cache();
				
				runOnUiThread(new Runnable() {
					@Override
					public void run()
					{
						progress.dismiss();
					}
				});
			}
		}).start();
	}
}
