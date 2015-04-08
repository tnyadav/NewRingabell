package com.ringabell.service;



import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.PowerManager;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.ringabell.localdb.ContactLocalDB;
import com.ringabell.localdb.ReminderLocalDB;
import com.ringabell.model.Reminder;
import com.ringabell.reminder.ReminderMainActivity;
import com.ringabell.serverdb.ServiceHandler;
import com.share2people.ringabell.R;


public class GcmIntentService extends IntentService {

	public GcmIntentService() {
		super("GcmIntentService");
		// TODO Auto-generated constructor stub
	}


	//private static final String URL_RETRIEVE_REMINDER="http://tech-sync.com/ringabell/retrievereminderbyid2.php";
	private ProgressDialog pDialog;
	List<Reminder> reminderList;
	String  message ="",title = "",  sender = "", remid="", receiv="",voice_url="",gid="", hr="", min="";
	private static Context mContext;
	private static PowerManager.WakeLock wakeLock;


	private static final String SENDER_ID="951290017600";
	private static final String TAG = "GCMIntentService";
	private SharedPreferences sharedPref;
	private boolean existingReminder=false;

	
/*	public GCMIntentService() {
		super(SENDER_ID);
	}*/

	/**
	 * Method called on device registered
	 **/
	/*@Override
	protected void onRegistered(Context context, String registrationId) {
		Log.i(TAG, "Device registered: regId = " + registrationId);
	}

	*//**
	 * Method called on device un registred
	 * *//*
	@Override
	protected void onUnregistered(Context context, String registrationId) {
		Log.i(TAG, "Device unregistered");
	}*/

	/**
	 * Method called on Receiving a new message
	 * */
/*	@Override
	protected void onMessage(Context context, Intent intent) {
		sharedPref = getSharedPreferences("LAST_LOGIN", Activity.MODE_PRIVATE);

		message = intent.getExtras().getString("message");
		String result = intent.getExtras().getString("result");
		remid=message;		

		// notifies user
		String msg="",receivername="";
		String receiverno=new ReminderLocalDB(getApplicationContext()).getReceiver(remid);
		if(receiverno.equals("")){
			receivername="Me";
		}
		else{
			receivername=new ContactLocalDB(getApplicationContext()).getContactName(receiverno);
		}

		if(result.equalsIgnoreCase("accepted")){
			if(receiverno.indexOf(":")== -1){
				msg=receivername + " Has accepted your reminder";
			}
			else{
				msg="Your Group Reminder Has been accepted";
			}
			generateNotification(context, msg);
			new ReminderLocalDB(getApplicationContext()).updateReminderDeliverStatus(remid);
		}
		else if(result.equalsIgnoreCase("declined")){
			if(receiverno.indexOf(":")== -1){
				msg=receivername + " has declined your Reminder";
			}
			else{
				msg="Your Group Reminder has been declined";
			}
			generateNotification(context, msg);
			new ReminderLocalDB(getApplicationContext()).updateReminderDeliverStatus2(remid);
		}
		else if(result.equalsIgnoreCase("reminder response")){
			String response_msg = intent.getExtras().getString("response");
			msg=receivername + " has responded on your reminder.";				
			generateNotification(context, msg);
			new ReminderLocalDB(getApplicationContext()).updateReminderResponse(remid, response_msg);
		}
		else if(result.equalsIgnoreCase("reminder")){
			new SaveReminder().execute();			
		}
	}*/

/*	@Override
	protected void onDeletedMessages(Context context, int total) {
		Log.i(TAG, "Received deleted messages notification");
	}

	@Override
	public void onError(Context context, String errorId) {
		Log.i(TAG, "Received error: " + errorId);
		// displayMessage(context, getString(R.string.gcm_error, errorId));
	}

	@Override
	protected boolean onRecoverableError(Context context, String errorId) {
		// log message
		Log.i(TAG, "Received recoverable error: " + errorId);
		return super.onRecoverableError(context, errorId);
	}*/
	public void privacyPolicyAlert() {

		new AlertDialog.Builder(this)
		.setTitle("Privacy Policy")
		.setMessage("Message")
		.setPositiveButton("Cancel", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				dialog.cancel();
			}
		}).show();
	}

	private static void generateNotification(Context context, String message) {
		int icon = R.drawable.ic_launcher;
		long when = System.currentTimeMillis();
		NotificationManager notificationManager = (NotificationManager)
				context.getSystemService(Context.NOTIFICATION_SERVICE);
		Notification notification = new Notification(icon, message, when);

		String title = context.getString(R.string.app_name);

		Intent notificationIntent = new Intent(context, ReminderMainActivity.class);
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


	private class SaveReminder extends AsyncTask<Void, Void, Void>{

		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();			
		}


		@Override
		protected Void doInBackground(Void... Void) {
			// TODO Auto-generated method stub
			ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("reminder_id", remid));
			params.add(new BasicNameValuePair("receiver", sharedPref.getString("USERNAME", "")));
			//params.add(new BasicNameValuePair("group_id", gid));

			ServiceHandler jsonParser = new ServiceHandler();
			try{

				String json = jsonParser.makeServiceCall(ServiceHandler.URL_RETRIEVE_REMINDER, ServiceHandler.GET,params);
				Log.v("Response: ", "> " + json);
				System.out.println("ARRAY IS="+json+"length is="+json.length());
				if(!json.equals("error")){
					if (json != null && json.length()>0) {
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
								String rem_id=remid;
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
									System.out.println("zzzzzz = "+sender);
									System.out.println("zzzzzz = "+receiver);

									new ReminderLocalDB(getApplicationContext()).insertReminderLocal(sender, receiver, gid, title, rem_id, remdate, remtime,
											timeString, delay, recur, remstatus, location, reqCode, rtime_receiving, voice_url, "IN", "NONE", "0");
								}
								else{
									existingReminder=true;
								}
							}

							//String number=array.getString(0);
							//for(int i=0;i<array.length();i++){
							//reminderList.add(new Reminder(array.getString(i), "9898989898"));
							//}	
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
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);

			if(!existingReminder){
				Intent i = new Intent(getApplicationContext(), AcceptNewReminderActivity.class);
				i.putExtra("sender", sender);
				i.putExtra("gid", gid);
				i.putExtra("rem_title", title);
				i.putExtra("rem_id", remid);
				i.putExtra("voice_file",voice_url);
				i.putExtra("hr",hr);
				i.putExtra("min",min);
				i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(i);	
			}
		}
	}


	@Override
	protected void onHandleIntent(Intent intent) {
		 Bundle extras = intent.getExtras();
	        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
	        // The getMessageType() intent parameter must be the intent you received
	        // in your BroadcastReceiver.
	        String messageType = gcm.getMessageType(intent);
	      
			if (messageType != null && !extras.isEmpty()) {
	     
	         	if (GoogleCloudMessaging.
	                    MESSAGE_TYPE_MESSAGE.equals(messageType)) {
	         		sharedPref = getSharedPreferences("LAST_LOGIN", Activity.MODE_PRIVATE);

	        		message = intent.getExtras().getString("message");
	        		String result = intent.getExtras().getString("result");
	        		remid=message;		

	        		// notifies user
	        		String msg="",receivername="";
	        		String receiverno=new ReminderLocalDB(getApplicationContext()).getReceiver(remid);
	        		if(receiverno.equals("")){
	        			receivername="Me";
	        		}
	        		else{
	        			receivername=new ContactLocalDB(getApplicationContext()).getContactName(receiverno);
	        		}

	        		if(result.equalsIgnoreCase("accepted")){
	        			if(receiverno.indexOf(":")== -1){
	        				msg=receivername + " Has accepted your reminder";
	        			}
	        			else{
	        				msg="Your Group Reminder Has been accepted";
	        			}
	        			generateNotification(this, msg);
	        			new ReminderLocalDB(getApplicationContext()).updateReminderDeliverStatus(remid);
	        		}
	        		else if(result.equalsIgnoreCase("declined")){
	        			if(receiverno.indexOf(":")== -1){
	        				msg=receivername + " has declined your Reminder";
	        			}
	        			else{
	        				msg="Your Group Reminder has been declined";
	        			}
	        			generateNotification(this, msg);
	        			new ReminderLocalDB(getApplicationContext()).updateReminderDeliverStatus2(remid);
	        		}
	        		else if(result.equalsIgnoreCase("reminder response")){
	        			String response_msg = intent.getExtras().getString("response");
	        			msg=receivername + " has responded on your reminder.";				
	        			generateNotification(this, msg);
	        			new ReminderLocalDB(getApplicationContext()).updateReminderResponse(remid, response_msg);
	        		}
	        		else if(result.equalsIgnoreCase("reminder")){
	        			new SaveReminder().execute();			
	        		}	
	            	
	              
	            }
	        }
	
		 GcmBroadcastReceiver.completeWakefulIntent(intent);
	}	
}
