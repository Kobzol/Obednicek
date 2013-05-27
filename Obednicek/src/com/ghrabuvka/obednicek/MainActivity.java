package com.ghrabuvka.obednicek;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

@TargetApi(9)
public class MainActivity extends Activity {
	
	final String FILENAME_DATA = "OfflineJidelnicek.dat";
	SharedPreferences settings;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        settings = PreferenceManager.getDefaultSharedPreferences(this);
        setFields();
        startService();
        enableHttpResponseCache();
    }
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
    	super.onConfigurationChanged(newConfig);
    	String zarizeniText = ((EditText) findViewById(R.id.editText1)).getText().toString();
    	String usernameText = ((EditText) findViewById(R.id.editText2)).getText().toString();
    	String passwordText = ((EditText) findViewById(R.id.editText3)).getText().toString();
    	boolean isChecked = ((CheckBox) findViewById(R.id.checkBox1)).isChecked();
    	
    	setContentView(R.layout.activity_main);
    	
    	((EditText) findViewById(R.id.editText1)).setText(zarizeniText);
    	((EditText) findViewById(R.id.editText2)).setText(usernameText);
    	((EditText) findViewById(R.id.editText3)).setText(passwordText);
    	((CheckBox) findViewById(R.id.checkBox1)).setChecked(isChecked);
    	
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode == 0) {
            this.finish();
        }
    }
    
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        
    	if (item.getItemId() == R.id.menu_settings)
    	{
    		Intent settingsIntent = new Intent(this, PreferencesActivity.class);
			startActivity(settingsIntent);
    	}
    	else if (item.getItemId() == R.id.menu_help)
    	{
    		Intent helpIntent = new Intent(this, HelpActivity.class);
    		startActivity(helpIntent);
    	}
    	else if (item.getItemId() == R.id.menu_exit)
    	{
    		this.finish();
    	}
   
    	return super.onOptionsItemSelected(item);
    }
    
    /* public boolean isServiceRunning()
    {
    	ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (DnesniObed.class.getName().equals(service.service.getClassName())) {
            	return true;
            }
        }
        return false;
    }*/
    public void startService()
    {
    		startService(new Intent(this, DnesniObed.class));
    }
    public void stopService()
    {
    		stopService(new Intent(this, DnesniObed.class));
    }
    public void setFields()
    {
    		if (settings.getBoolean("login_remember", false) == true)
    		{
    			EditText zarizeni = (EditText) findViewById(R.id.editText1);
				EditText username = (EditText) findViewById(R.id.editText2);
				EditText password = (EditText) findViewById(R.id.editText3);
				zarizeni.setText(settings.getString("login_place", ""));
				username.setText(settings.getString("login_name", ""));
				password.setText(settings.getString("login_password", ""));
				CheckBox rememberCheckBox = (CheckBox) findViewById(R.id.checkBox1);
				rememberCheckBox.setChecked(true);
					try {
						tryLogin(null);
					} catch (IOException e) {
					}
    		}
    }
    private void enableHttpResponseCache() {
    	  try {
    	    long httpCacheSize = 1 * 1024 * 1024; // 1 MiB
    	    File httpCacheDir = new File(getCacheDir(), "http");
    	    Class.forName("android.net.http.HttpResponseCache")
    	         .getMethod("install", File.class, long.class)
    	         .invoke(null, httpCacheDir, httpCacheSize);
    	  } catch (Exception httpResponseCacheNotAvailable) {
    	  }
    	}
    public void tryLogin(View view) throws IOException
    {
    	ProgressBar pgBar = (ProgressBar) findViewById(R.id.progressBar1);
    	if (isOnline() && pgBar.getVisibility() == View.GONE)
    	{
    		pgBar.setVisibility(View.VISIBLE);
    		EditText zarizeni_text = (EditText) findViewById(R.id.editText1);
    		EditText username_text = (EditText) findViewById(R.id.editText2);
    		EditText password_text = (EditText) findViewById(R.id.editText3);
    		String zarizeni = zarizeni_text.getText().toString();
    		String username = username_text.getText().toString();
    		String password = password_text.getText().toString();
    		if (zarizeni.equals("") || username.equals("") || password.equals(""))
    		{
    			Toast.makeText(getApplicationContext(), "Vyplòte údaje", Toast.LENGTH_SHORT).show();
    			pgBar.setVisibility(View.GONE);
    		}
    		else 
    		{	
    			Editor editor = settings.edit();
    			CheckBox rememberCheckBox = (CheckBox) findViewById(R.id.checkBox1);
    			if (rememberCheckBox.isChecked())
    			{
    				editor.putBoolean("login_remember", true);
    				editor.putString("login_place", zarizeni);
    				editor.putString("login_name", username);
    				editor.putString("login_password", password);	
    			}
    			else 
    			{
    				editor.putBoolean("login_remember", false);
    				editor.putString("login_place", "");
    				editor.putString("login_name", "");
    				editor.putString("login_password", "");	 				
    			}
    			editor.commit();
    			if (username.contains(" "))
    			{ username = username.replace(" ", "+"); }
    			new LogIn().execute("http://www.strava.cz/foxisapi/foxisapi.dll/istravne.istravne.process?prihlaseniuzivatele&zarizeni=" + zarizeni + "&uzivatel="+  username + "&heslo=" + password);
    		}
    	}
    }
    
    public void displayOffline(View view)
    {
    	FileInputStream fi;
		try {
			fi = openFileInput(FILENAME_DATA);
			String data = new java.util.Scanner(fi,"windows-1250").useDelimiter("\\A").next();
			Intent jidelnicekIntent = new Intent(this, JidelnicekActivity.class);
			jidelnicekIntent.putExtra("DATA", data);
			startActivityForResult(jidelnicekIntent, 0);
			data = null;
		} catch (FileNotFoundException e) {
			Toast.makeText(getApplicationContext(), "Žádna offline data nejsou uložena.", Toast.LENGTH_SHORT).show();
		}	
    }
    public void afterLogin(String data)
    {
    	if (data != null)
    	{
    		ProgressBar pgBar = (ProgressBar) findViewById(R.id.progressBar1);
    		pgBar.setVisibility(View.GONE);
    		if (data.contains("Chybn") || data.contains("vypln"))
    		{	
    			Toast.makeText(getApplicationContext(), "Nelze se pøihlásit", Toast.LENGTH_SHORT).show(); 
    		}
    		else 
    		{
    			try { 
    				data = data.substring(data.indexOf("SID=") + 4, data.length());
        			String SID = data.substring(0, data.indexOf("&"));
        			Intent jidelnicekIntent = new Intent(this, JidelnicekActivity.class);
        			jidelnicekIntent.putExtra("SID", SID);
        			startActivityForResult(jidelnicekIntent, 0);	
    			}
    			catch (Exception e)
    			{
    				Toast.makeText(getApplicationContext(), "Server je zaneprázdnìn. Zkuste to pozdìji.", Toast.LENGTH_SHORT).show();
    			}
    		}
    	}
    }
    public boolean isOnline()
    {
    	Button offlineButton = (Button) findViewById(R.id.button2);
    	ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
    	if(connMgr.getActiveNetworkInfo() != null && connMgr.getActiveNetworkInfo().isConnectedOrConnecting())
    	{
    		offlineButton.setVisibility(View.GONE);
    		return true;
    	}
    	else {
    		displayWarning();
    		offlineButton.setVisibility(View.VISIBLE);
    		return false;
    	}
    }
    public void displayWarning()
    {
    	Toast.makeText(getApplicationContext(),"Nejste pøipojeni k internetu", Toast.LENGTH_SHORT).show();
    	
    }
    private class LogIn extends AsyncTask<String, Void, String> {
	 	
    	String data;
    	@Override
		protected String doInBackground(String... arg0) {
			
    		try {
				fetchPage(arg0[0]);
			} catch (IOException e) {
			}
    		
			return null;
		}
    	
    	@Override
    	protected void onPostExecute(String result)
    	{
    		super.onPostExecute(result);
			afterLogin(data);
			data = null;
    	}
        
        private void fetchPage(String urlData) throws IOException
        {
        	URL url = new URL(urlData);
    		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
    		conn.setRequestProperty("Content-Type", "text/html; charset=windows-1250");
    		try {
    				conn.setConnectTimeout(15000);
    				InputStream in = new BufferedInputStream(conn.getInputStream());
    				data = new java.util.Scanner(in,"windows-1250").useDelimiter("\\A").next();
    		}
    		finally { conn.disconnect(); }
        }
    
    }
  
}
	



