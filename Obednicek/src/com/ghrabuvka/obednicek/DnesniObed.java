package com.ghrabuvka.obednicek;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Binder;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

public class DnesniObed extends Service {
	  private NotificationManager nm;
	  private final IBinder mBinder = new MyBinder();
	  private String FILENAME_DATA = "Dny.dat", DATE_TODAY = "pref_todayDate";
	  private ArrayList<Den> Dny = new ArrayList<Den>();
	  SharedPreferences settings;
	  
	  @Override
	    public void onCreate() {

		  	settings = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		  	if (settings.getBoolean("pref_service", true))
		  	{
		  		String savedTime = settings.getString("pref_displayTime", "6:00");
		  		AlarmManager alarm = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
		  		Calendar cal = new GregorianCalendar();
		  		
		  		cal.setTimeInMillis(System.currentTimeMillis());
		  		String hour = savedTime.substring(0, savedTime.indexOf(":"));
		  		String minute = savedTime.substring(savedTime.indexOf(":") + 1, savedTime.length());
		    	cal.set(Calendar.HOUR_OF_DAY, Integer.valueOf(hour));
		    	cal.set(Calendar.MINUTE, Integer.valueOf(minute));
		    	cal.set(Calendar.SECOND, 0);
		    	cal.set(Calendar.MILLISECOND, 0);
		    	if (System.currentTimeMillis() > cal.getTimeInMillis()){
		        	cal.setTimeInMillis(cal.getTimeInMillis() + 24 * 60 * 60 * 1000);
		    	}
		    	Intent intent = new Intent(getApplicationContext(), DnesniObed.class);
		    	intent.putExtra("display", true);
		  		PendingIntent pintent = PendingIntent.getService(this, 0, intent, 0);
		  		alarm.cancel(pintent);
		  		alarm.setRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), 24 * 60 * 60 * 1000, pintent);
		  	}
	    }
	  
	  @Override
	  public int onStartCommand(Intent intent, int flags, int startId) {

		  File fileData = getApplicationContext().getFileStreamPath(FILENAME_DATA);
		  
		  if (fileData.exists() && settings.getBoolean("pref_service",true) && intent.getBooleanExtra("display", false))
		  {
			  	Log.i("file","found");
		    	FileInputStream fis = null;
		    	ObjectInputStream ois = null;
				try {
					fis = openFileInput(FILENAME_DATA);
					ois = new ObjectInputStream(fis);
					while (true)
					{
						Dny.add((Den)ois.readObject());
					}
				} catch (FileNotFoundException e) {
				} catch (IOException e) {	
				} catch (ClassNotFoundException e) {
				}
				
				finally 
				{
					try {
						ois.close();
						fis.close();
					} catch (IOException e) {
					}
				}
				int dnes = 0;
				if (getObed(getDnes()) != -1) // Pokud je dneska nìjaký obìd
				{	Log.i("before","timecheck");
					if (firstTimeToday()) // Pokud dnes nebyl obìd zobrazen
					{	
						try {
							dnes = getDnes();
							showNotification(Dny.get(dnes).Obedy.get(getObed(dnes)).Name);
						}
						catch (Exception e)
						{
							Toast.makeText(getApplicationContext(), "Služba obìdníèku spadla!", Toast.LENGTH_SHORT).show();
						}
					}
				}
		  }
				
	    return Service.START_NOT_STICKY;
	  }
	  
	  public boolean firstTimeToday()
	  {
		  String todayDate = settings.getString(DATE_TODAY, "unset");
		  if (!todayDate.equals("unset"))
		  {		
				if (todayDate.equals(Dny.get(getDnes()).Obedy.get(0).pStav1))
				{
					return false;
				}
				else
				{
					writeTodayDate();
					return true;
				}
		  }
		  else
		  {
			  writeTodayDate();
			  return true;
		  }
	  }
	  
	  public void writeTodayDate()
	  {
		  	Den tempDen = Dny.get(getDnes());
		  	Editor editor = settings.edit();
		  	editor.putString(DATE_TODAY, tempDen.Obedy.get(0).pStav1);
		  	editor.commit();
	  }
	  public int getDnes()
	  {
		  Calendar today = (GregorianCalendar) GregorianCalendar.getInstance();
		  for (int i = 0; i < Dny.size(); i++)
		  {
			  String tempObedDate = Dny.get(i).Obedy.get(0).pStav1;
			  if ( today.get(Calendar.DATE) == Integer.valueOf(tempObedDate.substring(6,8)) && today.get(Calendar.MONTH) + 1 == Integer.valueOf(tempObedDate.substring(4,6)) )
			  {
				  Log.i("mesic", Integer.toString(today.get(Calendar.MONTH)));
				  Log.i("den",Integer.toString(today.get(Calendar.DATE)));
				  return i;
			  }
		  }
		  return -1;
	  }
	  
	  public int getObed(int dnes)
	  {
		  if (dnes != -1)
		  {
			for (int i = 0; i < 2; i++)
		  	{
			  	if (Dny.get(dnes).Obedy.get(i).Stav.equals("P"))
			  	{
				  	return i;
			  	}
		  	}
		  	return -1;
		  }
		  else return -1;
	  }

	private void showNotification(String data)
	  {
		  	data = data.substring(data.indexOf(",") + 2, data.length());
		  	Notification notif = new Notification(R.drawable.statusicon, "Dnešní obìd", System.currentTimeMillis());
	        notif.flags |= Notification.FLAG_AUTO_CANCEL;
	        notif.number += 1;
	        Intent intent = new Intent(this, MainActivity.class);
	        PendingIntent pIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, 0);
	        notif.setLatestEventInfo(getApplicationContext(), "Dnešní obìd", data, pIntent);
	        nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
	        nm.notify(0, notif);
	  }
	  
	  @Override
	  public void onDestroy()
	  {
		  //Toast.makeText(getApplicationContext(), "Služba byla ukonèena. Znovu se zapne po restartu mobilu nebo aplikace.",Toast.LENGTH_LONG).show();
	  }
	  
	  public class MyBinder extends Binder {
	    DnesniObed getService() {
	      return DnesniObed.this;
	    }
	  }


	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}

	} 
