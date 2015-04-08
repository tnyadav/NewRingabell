package com.ringabell.service;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.ByteArrayBuffer;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.ringabell.localdb.ContactLocalDB;
import com.ringabell.localdb.GroupsLocalDB;
import com.ringabell.localdb.ReminderLocalDB;
import com.ringabell.model.Group;
import com.ringabell.model.Reminder;
import com.ringabell.reminder.ReminderListScreen;
import com.ringabell.reminder.ReminderScreen;
import com.ringabell.serverdb.ServiceHandler;
import com.ringabell.utils.ConnectionDetector;
import com.share2people.ringabell.R;
import com.share2people.ringabell.R.drawable;
import com.share2people.ringabell.R.id;
import com.share2people.ringabell.R.layout;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.KeyguardManager;
import android.app.KeyguardManager.KeyguardLock;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.location.LocationManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class AcceptNewReminderActivity extends Activity {

	Button btnaccept;
	Button btndecline;
	Button btnlater;
	TextView tvsender, tvreminder, tvremtime, tvremdate;
	ImageView senderPic, audioPic;
	String sender="",sendername="", reminder="", reminderid="", remTime="", voice_url="",voicefile="",gid="", hr="", min="", gpname="", reminder_accept="";
	AlarmManager am;
	Vibrator vibrator;
	PendingIntent pendingIntent;
	KeyguardManager keyguardManager; 
	KeyguardLock keyguardLock; 
	private static PowerManager.WakeLock wakeLock;
	protected ImageLoader imageLoader;
	DisplayImageOptions options;
	ConnectionDetector cDetector;
	private SharedPreferences sharedPref;
	private ImageLoadingListener animateFirstListener = new AnimateFirstDisplayListener();
	private static Context mContext;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_confirm_screen);
		mContext = getApplicationContext();
		sharedPref =getSharedPreferences("LAST_LOGIN", Activity.MODE_PRIVATE);
		
		 vibrator = (Vibrator) getApplicationContext().getSystemService(Context.VIBRATOR_SERVICE);
		cDetector=new ConnectionDetector(getApplicationContext());
		
		sender = getIntent().getStringExtra("sender");
		reminder = getIntent().getStringExtra("rem_title");
		reminderid = getIntent().getStringExtra("rem_id");
		voicefile=getIntent().getStringExtra("voice_file");
		gid=getIntent().getStringExtra("gid");
		hr=getIntent().getStringExtra("hr");
		min=getIntent().getStringExtra("min");

		btnaccept=(Button) findViewById(R.id.accept);
		btndecline=(Button) findViewById(R.id.decline);
		btnlater=(Button) findViewById(R.id.later);

		tvsender=(TextView) findViewById(R.id.sender_name);
		tvreminder=(TextView) findViewById(R.id.rem_title);
		tvremtime=(TextView) findViewById(R.id.remtime);
		tvremdate=(TextView) findViewById(R.id.remdate);
		senderPic=(ImageView) findViewById(R.id.sender_pic);
		audioPic=(ImageView) findViewById(R.id.audio_pic);
		imageLoader = ImageLoader.getInstance();		

		//options for image loading
		options = new DisplayImageOptions.Builder()
		.showImageOnLoading(R.drawable.ic_user)
		.showImageForEmptyUri(R.drawable.ic_user)
		.showImageOnFail(R.drawable.ic_user)
		.cacheInMemory(true)
		.cacheOnDisk(true)
		.considerExifParams(true)
		.displayer(new RoundedBitmapDisplayer(80))
		.build();		  


		if (!gid.equals("0")){
			List<Group> gp = new GroupsLocalDB(getApplicationContext()).getGroupById(gid);
			for (Group g : gp) {
				gpname=g.getName();
			}
			tvsender.setText(gpname);
			imageLoader.displayImage(new GroupsLocalDB(getApplicationContext()).getGroupPicUrl(gid), senderPic, options, animateFirstListener);
		}
		else{
			sendername=new ContactLocalDB(getApplicationContext()).getContactName(sender);
			tvsender.setText(sendername);
			imageLoader.displayImage(new ContactLocalDB(getApplicationContext()).getContactPicUrl(sender), senderPic, options, animateFirstListener);
		}

		List<Reminder> rem = new ReminderLocalDB(getApplicationContext()).getReminderInfo(reminderid);
		long rTime=0;
		long rDate=0;
		for (Reminder r : rem) {
			rDate = r.getRemDate();
			rTime = r.getRemTime();
		}
		
		tvreminder.setText(reminder);
		if(!voicefile.equals("TEXT")){
			audioPic.setVisibility(View.VISIBLE);
		}	

		tvremdate.setText(timeStampToDate(rDate));
		tvremtime.setText(timeStampToTime(rTime));
		remTime = timeStampToTime(rTime);

		am = (AlarmManager)getSystemService(Activity.ALARM_SERVICE);

		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		try {
			SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
			boolean vibration= true;
			if(vibration){
				 // Vibrate for 500 milliseconds
				 long[] pattern = {0, 300, 200};
				 vibrator.vibrate(pattern,0);
				 
				 Handler handler = new Handler();
					handler.postDelayed(new Runnable() {
						public void run() {
							vibrator.cancel();
						}}, 2000);
			}


			Uri notification;
			String notiRingtone=sharedPrefs.getString("notifications_new_message_ringtone", null);

			if(notiRingtone == null)
				notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
			else
				notification=Uri.parse(notiRingtone);

			Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notification);
			r.play();
		} catch (Exception e) {
			e.printStackTrace();
		}

		btnaccept.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View view) {
				// TODO Auto-generated method stub
				//wakeLock.release();
				vibrator.cancel();
				if(cDetector.isConnectingToInternet()){

					if(new ContactLocalDB(getApplicationContext()).checkIfContactExists(sender)){
						reminder_accept="1";
						new NotifySender().execute();
						//sendNotification(new ContactLocalDB(getApplicationContext()).getGcmRegisrationId(sender), "Accepted:R:" + reminderid + ":N:G:S:H:M");
					}
					else{
						//sendMessage(receivername);
						//Toast.makeText(getApplicationContext(), "sms", Toast.LENGTH_LONG).show();
					}

					new ReminderLocalDB(getApplicationContext()).updateReminderSataus(reminderid);

					if(!voicefile.equals("TEXT")){
						new Thread(new Runnable() {
							public void run() {

								try {
									File root = android.os.Environment.getExternalStorageDirectory();

									File dir = new File (root.getAbsolutePath() + "/RingABell");
									if(dir.exists()==false) {
										dir.mkdirs();
									}

									String filename = reminderid + ".3gp";

									//URL url = new URL("http://tech-sync.com/ringabell/recording/myrecording.3gp"); //you can write here any link
									URL url = new URL(voicefile); //you can write here any link
									File file = new File(dir, filename);

									long startTime = System.currentTimeMillis();
									Log.d("DownloadManager", "download begining");
									Log.d("DownloadManager", "download url:" + url);
									Log.d("DownloadManager", "downloaded file name:" + reminderid + ".3gp");

									//Open a connection to that URL. 
									URLConnection ucon = url.openConnection();


									//* Define InputStreams to read from the URLConnection.

									InputStream is = ucon.getInputStream();
									BufferedInputStream bis = new BufferedInputStream(is);

									//* Read bytes to the Buffer until there is nothing more to read(-1).

									ByteArrayBuffer baf = new ByteArrayBuffer(5000);
									int current = 0;
									while ((current = bis.read()) != -1) {
										baf.append((byte) current);
									}

									// Convert the Bytes read to a String. 
									FileOutputStream fos = new FileOutputStream(file);
									fos.write(baf.toByteArray());
									fos.flush();
									fos.close();
									Log.d("DownloadManager", "download ready in" + ((System.currentTimeMillis() - startTime) / 1000) + " sec");

								} catch (IOException e) {
									Log.d("DownloadManager", "Error: " + e);
								}	                                               
							}
						}).start();
					}				

					List<Reminder> rem = new ReminderLocalDB(getApplicationContext()).getReminderInfo(reminderid);
					for (Reminder rm : rem) {
						String rTitle = rm.getTitle();
						//Toast.makeText(getBaseContext(), rTitle  ,Toast.LENGTH_LONG).show();		
						long rDate = rm.getRemDate();
						long rTime = rm.getRemTime();
						long rDelay = rm.getDelay();
						long rRecur = rm.getRecur();
						String rRtime = rm.getReceivingTime();
						String lname=rm.getLocationName();

						//date 
						Calendar dateCal = Calendar.getInstance();
						dateCal.setTimeInMillis(rDate);

						int dayOfMonth = dateCal.get(Calendar.DAY_OF_MONTH);
						int monthOfYear = dateCal.get(Calendar.MONTH);
						int Year = dateCal.get(Calendar.YEAR);					

						//time 
						Calendar timeCal=Calendar.getInstance();
						timeCal.setTimeInMillis(rTime);

						int hour = timeCal.get(Calendar.HOUR_OF_DAY);
						int minute = timeCal.get(Calendar.MINUTE);
						
						Calendar combCal=Calendar.getInstance();
						String timeString="";

						if(hr.equals("none")){
							combCal.set(Year, monthOfYear, dayOfMonth, hour, minute,0);
							combCal.getTimeInMillis();
							timeString = String.valueOf(hour) + "_" + String.valueOf(minute);
						}
						else{
							combCal.set(Year, monthOfYear, dayOfMonth, Integer.parseInt(hr), Integer.parseInt(min),0);
							combCal.getTimeInMillis();
							timeString = String.valueOf(hr) + "_" + String.valueOf(min);
						}

						if (!gid.equals("0")){
							String senderno=gid;
							new ReminderLocalDB(getApplicationContext()).insertRecentContacts(senderno, rDate, rRtime);
						}
						else{
							new ReminderLocalDB(getApplicationContext()).insertRecentContacts(sender, rDate, rRtime);
						}

						int reqCode=0;
						Random rand = new Random();
						int randomNum = rand.nextInt((900 - 100) + 100);
						long tstamp = System.currentTimeMillis() % 100000 ;
						String tmp = String.valueOf(tstamp) + String.valueOf(randomNum);
						reqCode = Integer.parseInt(tmp);  

						int rcount=new ReminderLocalDB(getApplicationContext()).RemindersCount(timeString);
						if(rcount>1){
							Intent i = new Intent(AcceptNewReminderActivity.this, ReminderListScreen.class);
							int tempReqCode=new ReminderLocalDB(getApplicationContext()).getReminderReqCode(timeString);
							i.putExtra("rTimeString", timeString);
							i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
							pendingIntent = PendingIntent.getActivity(AcceptNewReminderActivity.this,
									tempReqCode, i, 0);	
							am.setRepeating(AlarmManager.RTC_WAKEUP, combCal.getTimeInMillis()-rDelay,rRecur,
									pendingIntent);


							Intent intent = new Intent(AcceptNewReminderActivity.this, ReminderScreen.class);
							intent.putExtra("sender", "");
							intent.putExtra("rTitle", "");
							intent.putExtra("rem_id", "");
							intent.putExtra("voicefile", "");
							intent.putExtra("location", "");
							intent.putExtra("gid", "");
							intent.putExtra("remTime", remTime);
							intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
							pendingIntent = PendingIntent.getActivity(AcceptNewReminderActivity.this, tempReqCode, intent, 0);
							pendingIntent.cancel();
						}
						else{						
							//Create a new PendingIntent and add it to the AlarmManager
							Intent intent = new Intent(AcceptNewReminderActivity.this, ReminderScreen.class);
							if (gid.equals("0")){
								intent.putExtra("sender", sender);
							}
							else{
								intent.putExtra("sender", gpname);
							}
							intent.putExtra("rTitle", rTitle);
							intent.putExtra("rem_id", reminderid);
							intent.putExtra("voicefile", voicefile);
							intent.putExtra("location", lname);
							intent.putExtra("gid", gid);
							intent.putExtra("remTime", remTime);
							intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
							pendingIntent = PendingIntent.getActivity(AcceptNewReminderActivity.this,
									reqCode, intent, 0);	
							am.setRepeating(AlarmManager.RTC_WAKEUP, combCal.getTimeInMillis()-rDelay,rRecur,
									pendingIntent);
						}	
						new ReminderLocalDB(getApplicationContext()).updateReqCode(reminderid, reqCode); 
					}
					finish();
					//keyguardLock.reenableKeyguard();
				}
				else{
					Toast.makeText(getApplicationContext(), "No Internet Connection", Toast.LENGTH_SHORT).show();
				}
			}
		});

		btndecline.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				//wakeLock.release();
				vibrator.cancel();
				//keyguardLock.reenableKeyguard();
				if(cDetector.isConnectingToInternet()){
					if(new ContactLocalDB(getApplicationContext()).checkIfContactExists(sender)){
						reminder_accept="2";
						new NotifySender().execute();
						//sendNotification(new ContactLocalDB(getApplicationContext()).getGcmRegisrationId(sender), "Accepted:R:" + reminderid + ":N:G:S:H:M");
					}
					else{
						//sendMessage(receivername);
						//Toast.makeText(getApplicationContext(), "sms", Toast.LENGTH_LONG).show();
					}

					new ReminderLocalDB(getApplicationContext()).deleteReminderonDecline(reminderid);
					finish();
				}
				else{
					Toast.makeText(getApplicationContext(), "No Internet Connection", Toast.LENGTH_LONG).show();
				}
			}
		});

		btnlater.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				//wakeLock.release();
				//keyguardLock.reenableKeyguard();
				vibrator.cancel();
				finish();				
			}
		});
	}
	
	@Override
	public void onAttachedToWindow() {
	        Window window = getWindow();

	        window.addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
	                | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
	                | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
	                | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
	                | WindowManager.LayoutParams.FLAG_FULLSCREEN);

	        super.onAttachedToWindow();
	    }
	
	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
	}
	
	
	
	private class NotifySender extends AsyncTask<Void, Void, Void>{

		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();			
		}
		
		
		@Override
		protected Void doInBackground(Void... Void) {
			// TODO Auto-generated method stub
			ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("sender_no", sender));
			params.add(new BasicNameValuePair("reminder_id", reminderid));
			params.add(new BasicNameValuePair("username", sharedPref.getString("USERNAME", "")));
			params.add(new BasicNameValuePair("reminder_accept", reminder_accept));

			ServiceHandler jsonParser = new ServiceHandler();
			try{
				String json = jsonParser.makeServiceCall(ServiceHandler.URL_UPDATE_REMINDER, ServiceHandler.GET,params);
				Log.v("Response: ", "> " + json);
				System.out.println("ARRAY IS="+json+"length is="+json.length());
				if(!json.equals("error")){
					if (json != null && json.length()>0) {
						//existingUser=true;
						//Toast.makeText(getApplicationContext(), json.toString(), Toast.LENGTH_SHORT).show();
						//Toast.makeText(getApplicationContext(), gid.toString(), Toast.LENGTH_SHORT).show();
						try {
							//JSONArray array=new JSONArray(json);
							//JSONObject obj=null;							
						} catch (Exception e) {
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
			
		}
	}

	private static class AnimateFirstDisplayListener extends SimpleImageLoadingListener {

		static final List<String> displayedImages = Collections.synchronizedList(new LinkedList<String>());

		@Override
		public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
			if (loadedImage != null) {
				ImageView imageView = (ImageView) view;
				boolean firstDisplay = !displayedImages.contains(imageUri);
				if (firstDisplay) {
					FadeInBitmapDisplayer.animate(imageView, 500);
					displayedImages.add(imageUri);
				}
			}
		}
	}

	private String timeStampToTime(long timestamp){
		Date date=new Date(timestamp);

		DateFormat sf = new SimpleDateFormat("hh:mm a");
		String timeVal=(String) sf.format(date);
		return timeVal;
	}
	private String timeStampToDate(long timestamp){
		Date date=new Date(timestamp);

		DateFormat sf = new SimpleDateFormat("dd-MMM-yyyy");
		String timeVal=(String) sf.format(date);
		return timeVal;
	}	

	

}
