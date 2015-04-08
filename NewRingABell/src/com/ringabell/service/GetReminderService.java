package com.ringabell.service;

import java.util.ArrayList;
import java.util.Calendar;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.ringabell.localdb.ReminderLocalDB;
import com.ringabell.reminder.NewReminder;
import com.ringabell.serverdb.ServiceHandler;
import com.ringabell.utils.ConnectionDetector;
import com.share2people.ringabell.R;
import com.share2people.ringabell.R.drawable;
import com.share2people.ringabell.R.string;

import android.app.Activity;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

public class GetReminderService extends IntentService {
	
	private SharedPreferences sharedPref;
	ConnectionDetector cDetector;
	private Context mContext;
	private String user = "", title = "",  sender = "", remid="", voice_url="",gid="", hr="", min="";
	
	public GetReminderService() {
		super(GetReminderService.class.getName());
		// TODO Auto-generated constructor stub
		
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		// TODO Auto-generated method stub
		mContext=(Context) getApplicationContext();
		sharedPref = getSharedPreferences("LAST_LOGIN", Activity.MODE_PRIVATE);
		System.out.println("MISSED REMINDERS");
		user = sharedPref.getString("USERNAME", "");
		getMissedReminders();
	}
	
	public void getMissedReminders(){
		
		ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("reciever", user));
		//params.add(new BasicNameValuePair("group_id", gid));

		ServiceHandler jsonParser = new ServiceHandler();
		try{
			
			int existingReminderCount=0;			
			String json = jsonParser.makeServiceCall(ServiceHandler.URL_RETRIEVE_MISSED_REMINDER, ServiceHandler.GET,params);
			Log.v("Mised Reminder Response: ", "> " + json);
			System.out.println("mISSED rEMINDER ARRAY IS="+json+"length is="+json.length());
			if(!json.equals("error")){
				if (json != null && json.length()>3) {
					//existingUser=true;
					//Toast.makeText(getApplicationContext(), gid.toString(), Toast.LENGTH_SHORT).show();
					try {
						JSONArray array=new JSONArray(json);
						JSONObject obj=null;
						for(int i=0;i<array.length();i++){
							obj=array.getJSONObject(i);
							sender=obj.getString("sender_no");
							String receiver=obj.getString("reciever_no");
							gid=obj.getString("group_id");
							title=obj.getString("reminder_title");
							remid= obj.getString("reminder_id");
							long remdate=obj.getLong("reminder_date");
							long remtime=obj.getLong("reminder_time");
							long delay=obj.getLong("delay");
							long recur=obj.getLong("recurring");
							voice_url =obj.getString("voice_url");
							String location =obj.getString("location");
							hr=obj.getString("hour");
							min=obj.getString("minute");
							int remstatus=0;
							int reqCode = 0;
							String rtime_receiving = String.valueOf(System.currentTimeMillis());
							String timeString="";
							if(hr.equalsIgnoreCase("none")){
								Calendar timeCal=Calendar.getInstance();
								timeCal.setTimeInMillis(remtime);

								int hour = timeCal.get(Calendar.HOUR_OF_DAY);
								int minute = timeCal.get(Calendar.MINUTE);

								timeString = String.valueOf(hour) + "_" + String.valueOf(minute);
							}
							else{
								timeString = String.valueOf(hr) + "_" + String.valueOf(min);
							}								
							if(!new ReminderLocalDB(getApplicationContext()).checkIfReminderExists(remid)){
								existingReminderCount++;
								System.out.println("RMID " + remid);
								new ReminderLocalDB(getApplicationContext()).insertReminderLocal(sender, receiver, gid, title, remid, remdate, remtime,
										timeString, delay, recur, remstatus, location, reqCode, rtime_receiving, voice_url, "IN", "NONE", "0");
							}								
						}
						if(existingReminderCount>0){
							generateNotification(getApplicationContext(), "You have some missed reminders." );
						}

					} catch (JSONException e) {
						e.printStackTrace();
					}

				} else {
					//existingUser=false;
					Log.e("JSON Data", "Didn't receive any data from server!");
				}
			}
			else{
				//
			}

		}catch(Exception e){
			e.printStackTrace();
			//pDialog.dismiss();
			//Toast.makeText(mContext, "Something wrong with network, Please try later", Toast.LENGTH_SHORT).show();
		}				
		
	}
	
	private static void generateNotification(Context context, String message) {
		int icon = R.drawable.ic_launcher;
		long when = System.currentTimeMillis();
		NotificationManager notificationManager = (NotificationManager)
				context.getSystemService(Context.NOTIFICATION_SERVICE);
		Notification notification = new Notification(icon, message, when);

		String title = context.getString(R.string.app_name);

		Intent notificationIntent = new Intent(context, NewReminder.class);
		// set intent so it does not start a new activity
		notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
				Intent.FLAG_ACTIVITY_SINGLE_TOP);
		PendingIntent intent =
				PendingIntent.getActivity(context, 0, notificationIntent, 0);
		notification.setLatestEventInfo(context, title, message, intent);
		notification.flags |= Notification.FLAG_AUTO_CANCEL;

		// Play default notification sound
		notification.defaults |= Notification.DEFAULT_SOUND;

		//notification.sound = Uri.parse("android.resource://" + context.getPackageName() + "your_sound_file_name.mp3");

		// Vibrate if vibrate is enabled
		notification.defaults |= Notification.DEFAULT_VIBRATE;
		notificationManager.notify(0, notification);
	}


}
