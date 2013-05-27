package com.ghrabuvka.obednicek;

import java.io.BufferedInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import com.ghrabuvka.obednicek.R;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

@TargetApi(9)
public class JidelnicekActivity extends Activity {

	public ArrayList<Den> Dny = new ArrayList<Den>();
	public ArrayList<Container> containerArray = new ArrayList<Container>();
	public MyAdapter adapter;
	public String SID, DATA, konto, FILENAME_DNY = "Dny.dat", FILENAME_DATA = "OfflineJidelnicek.dat";
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_jidelnicek);
        
        Intent intent = getIntent();
        SID = intent.getStringExtra("SID");
        DATA = intent.getStringExtra("DATA");
        
        setResult(0);
        if (SID != null)
        {
        	i (isOnline() && (!SID.equals("") && SID.length() == 10))
        	{
        		downloadData(null);
        	}
        	else 
        	{
        		logout();
        	}
        }
        else if (DATA != null)
        {
        	buildDate(DATA);
        	RelativeLayout relativeLayout = (RelativeLayout) findViewById(R.id.relative1);
        	relativeLayout.setVisibility(View.GONE);
        	DATA = null;
        }
    }
    
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
    	super.onConfigurationChanged(newConfig);
    	setContentView(R.layout.activity_jidelnicek);
    	displayContent();
    }
        
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_jidelnicek, menu);
        return true;
    }
   
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        
    	
    	if (item.getItemId() == R.id.menu_settings)
    	{
    		Intent settingsIntent = new Intent(this, PreferencesActivity.class);
			startActivity(settingsIntent);
    	}
    	else if (item.getItemId() == R.id.menu_logout)
    	{
    		logout();
    	}
    	else if (item.getItemId() == R.id.menu_help)
    	{
    		Intent helpIntent = new Intent(this, HelpActivity.class);
    		startActivity(helpIntent);
    		setResult(1);
    	}
    	else if (item.getItemId() == R.id.menu_exit)
    	{
    		setResult(0);
    		this.finish();
    	}
           
    	return super.onOptionsItemSelected(item);      
    }
    
    public void saveLocalData(String data)
    {   	
    	new DataSaver().execute(data);
    }
    public void logout()
    {
    	setResult(1);
    	this.finish();
    }
    public void sendData(View view)
    {
    	if (isOnline())
    	{
    		String dataObedu = "";
    		for (int i = 0; i < Dny.size(); i++)
    		{
    			for (int u = 0; u < 2; u++)
    			{
    				dataObedu += Dny.get(i).Obedy.get(u).Stav;
    			}
    		}
    		new FetchData().execute("upload","http://www.strava.cz/foxisapi/foxisapi.dll/istravne.istravne.process?Zprac1Objednavky&SID="+ SID +"&stavy="+ dataObedu +"&nastaveni=");
    	}
    }
    public void downloadData(View view)
    {
    	ProgressBar progressBarLoading = (ProgressBar) findViewById(R.id.progressBar1);
    	
    	if (isOnline())
    	{
    		ListView listObedu = (ListView) findViewById(R.id.listView1); 
    		listObedu.setVisibility(View.GONE);
        	progressBarLoading.setVisibility(View.VISIBLE);
        	new FetchData().execute("download", "http://www.strava.cz/foxisapi/foxisapi.dll/istravne.istravne.process?formobjednavky&SID=" + SID);	
    	}
    }
    public void buildDate(String data)
    {
    	if (data != null)
    	{
    		String entry;
    		String[] properties;
    		String lookup = "nadpisden";													
    		Obed tempObed;
    		Den tempDen;
    		ArrayList<String> poleObedu = new ArrayList<String>();    	
    		int pocetPridanychObedu;
    		containerArray.clear();
    		Dny.clear();
    		konto = new StringBuffer(data.substring(data.indexOf("Nedoplatek:") + 11, data.indexOf("Nedoplatek:") + 35)).reverse().toString();
			konto = konto.substring(konto.indexOf("/<") + 2, konto.indexOf("/<") + 9);
			konto = new StringBuffer(konto).reverse().toString();
    	
			for (int i = 0; i < getCount(data, 0); i++)
        	{	
    			pocetPridanychObedu = 0;
            	data = new String(data.substring(data.indexOf(lookup) + lookup.length() + 1, data.length())); 	
            	entry = new String(data.substring(0, data.indexOf("<")));
            	tempDen = new Den(i, entry);
            	properties = new String[6];
            
            	poleObedu.add(entry);
            	containerArray.add(new Container(containerArray.size(), Dny.size(), 2, false));
            	
            	for (int u = 0; u < 2; u++)
            	{
            		tempObed = new Obed(i + u, tempDen);
            		data = new String(data.substring(data.indexOf("VALUE"), data.length()));
            		tempObed.Stav = new String(data.substring(7, 8));
            		data = new String(data.substring(data.indexOf("MouseOut"), data.length()));	
            		data = new String(data.substring(data.indexOf(">") + 1, data.length()));
            		tempObed.Name = new String(data.substring(0, data.indexOf("<")));
                
                	if (!tempObed.Name.contains("Obìd"))
                	{
                		poleObedu.add(tempObed.Name);
                		pocetPridanychObedu++;
                		containerArray.add(new Container(containerArray.size(), Dny.size(), u, true));
                	}
                	for (int o = 0; o < 6; o++)
                	{
                		data = new String(data.substring(data.indexOf("VALUE") + 7, data.length()));
                		properties[o] = new String(data.substring(0, data.indexOf("\"")));
                	}
            		tempObed.pStav1 = properties[0];
            		tempObed.Objednano = properties[1];
            		tempObed.pStav2 = properties[2];
            		tempObed.omezObj = properties[3];
            		tempObed.konecObj = properties[4];
            		tempObed.konecCod = properties[5];
            		tempDen.addObed(tempObed);
            	}
            	Dny.add(tempDen);
            	if (pocetPridanychObedu == 0)
            	{
            		poleObedu.remove(poleObedu.size() - 1);
            		containerArray.remove(containerArray.size() - 1);
            	}
        	}
			data = null;
			displayContent();
    	}
    }
    
    public void displayContent()
    {
    	TextView kontoText = (TextView) findViewById(R.id.textView1);
		kontoText.setText("Konto: " + konto + " Kè");
    	ListView listObedu = (ListView) findViewById(R.id.listView1);   	
		adapter = new MyAdapter(this,R.id.obedText, containerArray);
		listObedu.setOnItemClickListener(new OnItemClickListener() {
		  	@Override
		  	public void onItemClick(AdapterView<?> parent, View view,
				  int position, long id) {

			  if (isOnline())
				{
				  if (containerArray.get(position).isValid)
					{
					  	int obedIndex = containerArray.get(position).obedID;					
					  	int denIndex = containerArray.get(position).denID;
					
						Obed obed = Dny.get(denIndex).Obedy.get(obedIndex);
						if ( (!obed.pStav2.equals("O") && !obed.pStav2.equals("N")) || (obed.konecObj.equals("A") && obed.konecCod.equals("N") && obed.Objednano.equals("N")) || (obed.konecCod.equals("A"))) {
							displayWarning(1);
							return; }
						if (obed.omezObj.contains("B")) {
							displayWarning(1);
							return; }
						if (obed.omezObj.contains("C") && obed.Objednano.equals("A")) {
							displayWarning(1);
							return; }
						if ( (obed.omezObj.contains("D") || obed.omezObj.contains("F")) && !obed.Objednano.equals("A")) {
							displayWarning(1);
							return; }    					   					
						if (obed.Stav.equals("O"))
						{
							Dny.get(denIndex).zmenStav(true, obedIndex);
						}
						else 
						{
							Dny.get(denIndex).zmenStav(false, obedIndex);
						}
						adapter.notifyDataSetChanged();
					}
				}
		  	}
			});
		listObedu.setAdapter(adapter);
		listObedu.setVisibility(View.VISIBLE);
		ProgressBar progressBarLoading = (ProgressBar) findViewById(R.id.progressBar1);
		progressBarLoading.setVisibility(View.GONE);
		if (konto != null)
		{
			if (konto.contains("java"))
			{
				logout();
			}
		}
		else logout();
    }
    public boolean isOnline()
    {
    	ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
    	if(connMgr.getActiveNetworkInfo() != null && connMgr.getActiveNetworkInfo().isConnectedOrConnecting())
    	{
    		return true;
    	}
    	else {
    		displayWarning(0);
    		return false;
    	}
    }
    private int getCount(String data, int count)			
    {
        String lookup = "nadpisden";				
        if (data != null)
        {
        	if (data.contains(lookup))						
        	{
            	data = data.substring(data.indexOf(lookup) + lookup.length());
            	return getCount(data, count + 1);
        	}
        	else 
        	{
            	return count;
        	}
        }
        else return 0;
    }
    public void displayWarning(int error)
    {
    	if (error == 0)
    	{
    		Toast.makeText(getApplicationContext(),"Nejste pøipojeni k internetu", Toast.LENGTH_SHORT).show();
    	}
    	else if (error == 1)
    	{
    		Toast.makeText(getApplicationContext(), "Zmìny tohoto jídla již nejsou možné.", Toast.LENGTH_SHORT).show();
    	}
    }
    
    private class FetchData extends AsyncTask<String, Void, String> {
    	 	
    	String data, action;
    	@Override
		protected String doInBackground(String... args) {
			
    		try {
    			action = args[0];
    			if (action.equals("upload"))
    			{
    				fetchPage(args[1]);
    			}
    			else if (action.equals("download"))
    			{
    				fetchPage(args[1]);
    			}
			} catch (IOException e) {
			}
    		
			return null;
		}
    	
    	@Override
    	protected void onPostExecute(String result)
    	{
    		super.onPostExecute(result);
    			if (action.equals("upload"))
    			{
    				downloadData(null);
    				Toast.makeText(getApplicationContext(), "Zmìny byly uloženy.", Toast.LENGTH_SHORT).show();
    			}
    			else if (action.equals("download"))		
    			{	
    				buildDate(data);
    				saveLocalData(data);
    				data = null;
    			}
    	}
        private void fetchPage(String urlData) throws IOException
        {
        	if (isOnline())
        	{
        		URL url = new URL(urlData);
        		HttpURLConnection conn = (HttpURLConnection) url.openConnection();				// Pøipojení
    			conn.setRequestProperty("Content-Type", "text/html; charset=windows-1250"); 	// Nastavení charsetu
    			try {
    					InputStream in = new BufferedInputStream(conn.getInputStream());		// Uložení do InputStreamu
    					data = new java.util.Scanner(in,"windows-1250").useDelimiter("\\A").next();	// Hack na pøevedení do stringu
    			}
    			finally { conn.disconnect(); }
        	}
        }
    
    }
    
    private class MyAdapter extends ArrayAdapter<Container>
    {
    	ArrayList<Container> containerList = new ArrayList<Container>();
    	
		public MyAdapter(Context context, int textViewResourceId,
				ArrayList<Container> objects) {
			super(context, textViewResourceId, objects);
			this.containerList = objects;
		}	
		@Override
		public View getView(int position, View convertView, ViewGroup parent)
		{
			View v = convertView;
			
			if (v == null)
			{
				LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				v = inflater.inflate(R.layout.listview_layout_1, null);
			}
			
			Container tempContainer = containerList.get(position);
			
			if (tempContainer != null)
			{
				TextView obedText = (TextView) v.findViewById(R.id.obedText);
				ImageView orderImage = (ImageView) v.findViewById(R.id.imageView1);
				
				if (obedText != null)
				{
					if (tempContainer.isValid == true)
					{
						Obed tempObed = Dny.get(tempContainer.denID).Obedy.get(tempContainer.obedID);
						
						obedText.setText(tempObed.Name);
						if (tempObed.Stav.equals("P"))
						{
							obedText.setTypeface(null,Typeface.BOLD);
							orderImage.setVisibility(View.VISIBLE);
						}
						else 
						{
							obedText.setTypeface(null, Typeface.NORMAL);
							orderImage.setVisibility(View.INVISIBLE);
						}
						obedText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
					}
					else 
					{
						String dateName = Dny.get(tempContainer.denID).Name;
						StringBuilder dateSpaceInsert = new StringBuilder(dateName);
						dateSpaceInsert.insert(dateName.indexOf(".") + 1, " ");
						obedText.setTypeface(null, Typeface.ITALIC);
						obedText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 21);
						obedText.setText(dateName);
						LayoutParams lp = new LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
						lp.setMargins(0, 15, 0, 0);
						obedText.setLayoutParams(lp);
						orderImage.setVisibility(View.GONE);
					}
				}
			}
			
			return v;
		}

    }

    private class DataSaver extends AsyncTask<String, Void, String>
    {
    	@Override
		protected String doInBackground(String... args) {
			
    		saveData(args[0]);
			return null;
		}
    	
    	@Override
    	protected void onPostExecute(String result)
    	{
    		super.onPostExecute(result);
    	}
        private void saveData(String data)
        {
        	FileOutputStream fos;
    		try {
    			fos = openFileOutput(FILENAME_DNY, Context.MODE_PRIVATE);
    			ObjectOutputStream oos = new ObjectOutputStream(fos);
    			for (int i = 0; i < Dny.size(); i++)
    			{
    				oos.writeObject(Dny.get(i));
    			}
    			oos.close();
    			fos.close();
    			
    			fos = openFileOutput(FILENAME_DATA, Context.MODE_PRIVATE);
    			fos.write(data.getBytes("windows-1250"));
    			fos.close();
    		} catch (FileNotFoundException e) {
    			Log.i("error",e.getMessage());
    		} catch (IOException e) {
    			Log.i("error",e.getMessage());
    		}
    		data = null;
        }
    }
}
	



