package com.ringabell.service;

import java.io.IOException;
import java.util.ArrayList;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import org.json.JSONException;
import org.json.JSONObject;

import com.google.android.gcm.GCMRegistrar;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.ringabell.serverdb.ServiceHandler;
import com.ringabell.user.LoginActivity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

public class GCMUpdateService extends Service{

	//public static final String URL_CHECK_GCM_ID="http://tech-sync.com/ringabell/check_gcmid.php";
	//public static final String URL_UPDATE_GCM_ID="http://tech-sync.com/ringabell/update_gcmid.php";
	SharedPreferences sharedpref;
	SharedPreferences.Editor editor;
	String registrationId="";
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		sharedpref=getSharedPreferences("LAST_LOGIN", Activity.MODE_PRIVATE);

	}
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub
		//return super.onStartCommand(intent, flags, startId);

		new CheckGcmId().execute(sharedpref.getString("USERNAME", ""));
		//Toast.makeText(getApplicationContext(), "Service Invoke"+sharedpref.getString("USERNAME", ""), Toast.LENGTH_SHORT).show();
		return Service.START_STICKY;
	}

	private class CheckGcmId extends AsyncTask<String, Void, Void> {
		boolean existingUser=false,error=false;
		@Override
		protected void onPreExecute() {
			super.onPreExecute();

		}

		@Override
		protected Void doInBackground(String... arg) {
			String newUser = arg[0];

			// Preparing get data params
			ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("username", newUser));

			ServiceHandler jsonParser = new ServiceHandler();
			try{
				String json = jsonParser.makeServiceCall(ServiceHandler.URL_CHECK_GCM_ID, ServiceHandler.GET,params);

				Log.v("Check Gcm : ", "> " + json);
				System.out.println("Check Gcm ARRAY IS="+json+"length is="+json.length());
				if(!json.equals("error")|| json!=null){


					//existingUser=true;

					try {
						JSONObject obj=new JSONObject(json);
						String response=obj.getString("succ");

						if(response.equals("1"))
							existingUser=true;
						else if(response.equals("0"))
							existingUser=false;
						else if(response.equals("2"))
							error=true;

					} catch (JSONException e) {
						e.printStackTrace();
					}       
				}

			}catch(Exception e){
				e.printStackTrace();

				error=true;

			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			//  Toast.makeText(getApplicationContext(), "Service Finished", Toast.LENGTH_SHORT).show();
			// 
			if(!error){	       
				if(!existingUser)
					new UpdateGcmId().execute(sharedpref.getString("USERNAME", ""));
				else{
					editor=sharedpref.edit();
					editor.putBoolean("GCM_FLAG", true);
					editor.commit();
				}	                  	
			}
			else
				Toast.makeText(getApplicationContext(), "Something wrong", Toast.LENGTH_SHORT).show();
		}

	}
	private void retrieveGCMId(){

		//GCMRegistrar.checkDevice(getApplicationContext());

		// Make sure the manifest was properly set - comment out this line
		// while developing the app, then uncomment it when it's ready.
	/*	GCMRegistrar.checkManifest(getApplicationContext());
		GCMRegistrar.register(getApplicationContext(), LoginActivity.SENDER_ID);
		GCMRegistrar.setRegisteredOnServer(getApplicationContext(), true);
		
		registrationId = GCMRegistrar.getRegistrationId(getApplicationContext());*/

	/*	if(registrationId.equals("") || registrationId.equals(null))
			registerGCMId();  */
		
		GoogleCloudMessaging	gcm = GoogleCloudMessaging
				.getInstance(GCMUpdateService.this);
	
	try {
		String regid = gcm
				.register(LoginActivity.SENDER_ID);
	} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}

	}
	private void registerGCMId(){
		GCMRegistrar.register(getApplicationContext(), LoginActivity.SENDER_ID);
		GCMRegistrar.setRegisteredOnServer(getApplicationContext(), true);

	}
	private class UpdateGcmId extends AsyncTask<String, Void, Void> {
		boolean existingId=false,errors=false;
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			retrieveGCMId();
		}

		@Override
		protected Void doInBackground(String... arg) {
			String newUser = arg[0];
			GoogleCloudMessaging	gcm = GoogleCloudMessaging
					.getInstance(GCMUpdateService.this);
			String regid = null;
		try {
			 regid = gcm
					.register(LoginActivity.SENDER_ID);
			System.out.println("Update Gcm ARRAY IS=length is=");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
			// Preparing get data params
			ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("username", newUser));
			params.add(new BasicNameValuePair("regid", regid));

			ServiceHandler jsonParser = new ServiceHandler();
			try{
				String json = jsonParser.makeServiceCall(ServiceHandler.URL_UPDATE_GCM_ID, ServiceHandler.GET,params);

				Log.v("Update Gcm : ", "> " + json);
				System.out.println("Update Gcm ARRAY IS="+json+"length is="+json.length());
				if(!json.equals("error")|| json!=null){

					try {
						JSONObject obj=new JSONObject(json);
						String response=obj.getString("succ");

						if(response.equals("1"))
							existingId=true;
						else if(response.equals("0"))
							existingId=false;
						else if(response.equals("2"))
							errors=true;

					} catch (JSONException e) {
						e.printStackTrace();
					}       
				}

			}catch(Exception e){
				e.printStackTrace();

				errors=true;

			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			// Toast.makeText(getApplicationContext(), "Service Finished Updated", Toast.LENGTH_SHORT).show();
			if(!errors){
				if (existingId) {
					if(!registrationId.equals("")){
						editor=sharedpref.edit();
						editor.putBoolean("GCM_FLAG", true);
						editor.commit();
						//    	   Toast.makeText(getApplicationContext(), "Service Finished Commit"+registrationId, Toast.LENGTH_SHORT).show();

					}
				}
			}
			else
				Toast.makeText(getApplicationContext(), "Something wrong", Toast.LENGTH_SHORT).show();
		}

	}
}
