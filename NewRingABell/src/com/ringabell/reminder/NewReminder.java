package com.ringabell.reminder;

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

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.QuickContactBadge;
import android.widget.TextView;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.ringabell.localdb.ContactLocalDB;
import com.ringabell.localdb.GroupsLocalDB;
import com.ringabell.localdb.ReminderLocalDB;
import com.ringabell.model.Contact;
import com.ringabell.model.Group;
import com.ringabell.model.Reminder;
import com.ringabell.serverdb.ServiceHandler;
import com.ringabell.utils.AlertDialogManager;
import com.ringabell.utils.ConnectionDetector;
import com.share2people.ringabell.R;
import com.share2people.ringabell.R.color;
import com.share2people.ringabell.R.drawable;
import com.share2people.ringabell.R.id;
import com.share2people.ringabell.R.layout;
import com.share2people.ringabell.R.string;

import de.timroes.swipetodismiss.SwipeDismissList;


public class NewReminder extends Activity {
	ListView rlist;
	TextView noRecordText;
	CustomAdapter adapter;
	private static Context mContext;
	ArrayList<Reminder> reminderList;
	AlarmManager am;
	PendingIntent pendingIntent;
	AlertDialogManager alert = new AlertDialogManager();
	String voice="";
	String sender="",  gid="";
	String gpname ="",response_msg="", reminderid="", lname="", reminder_accept="", rem_time="";;
	ConnectionDetector cDetector;
	private SharedPreferences sharedPref;
	private boolean response = false;


	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_new_reminder);
		rlist=(ListView) findViewById(R.id.rlistView);
		noRecordText = (TextView) findViewById(R.id.noReminders);
		mContext = getApplicationContext();
		cDetector=new ConnectionDetector(getApplicationContext());
		am = (AlarmManager)getSystemService(Activity.ALARM_SERVICE);
		sharedPref =getSharedPreferences("LAST_LOGIN", Activity.MODE_PRIVATE);

		getActionBar().setTitle(getString(R.string.pending_reminders));
		getActionBar().setDisplayHomeAsUpEnabled(true);
		getActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.new_reminder_action_bar)));

		initView();
	}

	public void initView(){
		reminderList = new ReminderLocalDB(getApplicationContext()).getNewReminderInfo();
		if(reminderList.size()>0){
			adapter=new CustomAdapter(mContext, R.layout.reminder_list, reminderList);
			noRecordText.setVisibility(View.GONE);
			rlist.setVisibility(View.VISIBLE);
			rlist.setAdapter(adapter);
			adapter.notifyDataSetChanged();
		}
		else{
			rlist.setVisibility(View.GONE);
			noRecordText.setVisibility(View.VISIBLE);
		}
	}
	
	
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		if(response){
			String msg="";
			new ReminderLocalDB(getApplicationContext()).updateReminderResponse(reminderid, msg);
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
	private class CustomAdapter extends ArrayAdapter<Reminder>{

		List<Reminder> reminders=new ArrayList<Reminder>();
		protected ImageLoader imageLoader;
		DisplayImageOptions options;
		private ImageLoadingListener animateFirstListener = new AnimateFirstDisplayListener();

		public CustomAdapter(Context context,int resId,List <Reminder> reminderList){
			super(context,resId,reminderList);
			this.reminders=reminderList;
			imageLoader = ImageLoader.getInstance();

			//options for image loading
			options = new DisplayImageOptions.Builder()
			.showImageOnLoading(R.drawable.ic_user)
			.showImageForEmptyUri(R.drawable.ic_user)
			.showImageOnFail(R.drawable.ic_user)
			.cacheInMemory(true)
			.cacheOnDisk(true)
			.considerExifParams(true)
			.displayer(new RoundedBitmapDisplayer(50))
			.build();
		}

		private class ViewHolder{
			TextView title,time,sender;
			ImageView rem_accept,rem_decline,rem_type;
			String rm="",sn=""; 
			QuickContactBadge profile_pic;
		}
		public Reminder getItem(int position) {
			return reminders.get(position);
		};

		ViewHolder viewHolder;
		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			LayoutInflater inflator = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			if(convertView==null){
				convertView=inflator.inflate(R.layout.reminder_list, null);
				viewHolder=new ViewHolder();
				viewHolder.title=(TextView) convertView.findViewById(R.id.remTitle);
				viewHolder.time=(TextView) convertView.findViewById(R.id.remTime);
				viewHolder.sender=(TextView) convertView.findViewById(R.id.remUser);
				viewHolder.rem_accept=(ImageView) convertView.findViewById(R.id.rm_accept);
				viewHolder.rem_decline=(ImageView) convertView.findViewById(R.id.rm_decline);
				viewHolder.rem_type=(ImageView) convertView.findViewById(R.id.remType);
				viewHolder.profile_pic=(QuickContactBadge) convertView.findViewById(R.id.icon);
				convertView.setTag(viewHolder);
			}
			else 
				viewHolder=(ViewHolder) convertView.getTag();

			final Reminder rem=reminders.get(position);
			/*			long rt = Long.parseLong(rem.getReceivingTime());
			Calendar rtCal=Calendar.getInstance();
			rtCal.setTimeInMillis(rt);
			int hr = rtCal.get(Calendar.HOUR_OF_DAY);
			int min = rtCal.get(Calendar.MINUTE);*/
			rem_time = timeStampToTime(rem.getRemTime());
			viewHolder.time.setText(timeStampToDateTime(rem.getRemTime()));
			gid = rem.getGroupId();
			sender = rem.getSender();
			reminderid= rem.getRemId();
			lname=rem.getLocationName();
			if(!rem.getResponseMsg().equals("")){
				viewHolder.rem_accept.setVisibility(View.GONE);
				viewHolder.rem_decline.setVisibility(View.GONE);
				viewHolder.title.setText(rem.getResponseMsg());
				viewHolder.sender.setText(rem.getReceiver());
				imageLoader.displayImage(new ContactLocalDB(mContext).getContactPicUrl(rem.getReceiver()), viewHolder.profile_pic, options, animateFirstListener);
			}
			else{
				viewHolder.rem_accept.setVisibility(View.VISIBLE);
				viewHolder.rem_decline.setVisibility(View.VISIBLE);
				viewHolder.title.setText(rem.getTitle());
				if (!gid.equals("0")){
					List<Group> gp = new GroupsLocalDB(getApplicationContext()).getGroupById(gid);
					for (Group g : gp) {
						gpname=g.getName();
					}
					viewHolder.sender.setText(gpname);
					imageLoader.displayImage(new GroupsLocalDB(getApplicationContext()).getGroupPicUrl(gid), viewHolder.profile_pic, options, animateFirstListener);
				}
				else{
					viewHolder.sender.setText(rem.getSender());
					imageLoader.displayImage(new ContactLocalDB(mContext).getContactPicUrl(rem.getSender()), viewHolder.profile_pic, options, animateFirstListener);
				}
				try{
					if(!rem.getVoiceFile().equalsIgnoreCase("TEXT")){
						viewHolder.rem_type.setVisibility(View.VISIBLE);
						viewHolder.rem_type.setImageResource(R.drawable.ic_action_volume_on);
					}
					//viewHolder.rem_type.setImageResource((rem.getVoiceFile().equalsIgnoreCase("TEXT"))?R.drawable.ic_action_event:R.drawable.ic_action_volume_on);
				}
				catch(Exception e){
					//viewHolder.rem_type.setImageResource(R.drawable.ic_action_event);
				}
			}

			viewHolder.rm = rem.getRemId();
			viewHolder.sn = rem.getSender();

			viewHolder.rem_accept.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					if(cDetector.isConnectingToInternet()){

						if(new ContactLocalDB(getApplicationContext()).checkIfContactExists(viewHolder.sn)){
							reminder_accept="1";
							new NotifySender().execute();
							//sendNotification(new ContactLocalDB(getApplicationContext()).getGcmRegisrationId(sender), "Accepted:R:" + reminderid + ":N:G:S:H:M");
						}
						else{
							//sendMessage(receivername);
							//Toast.makeText(getApplicationContext(), "sms", Toast.LENGTH_LONG).show();
						}

						new ReminderLocalDB(getApplicationContext()).updateReminderSataus(viewHolder.rm);

						List<Reminder> rems = new ReminderLocalDB(getApplicationContext()).getReminderInfo(viewHolder.rm);
						for (Reminder rm : rems) {
							//sender = rm.getSender();
							String rTitle = rm.getTitle();
							long rDate = rm.getRemDate();
							long rTime = rm.getRemTime();
							long rDelay = rm.getDelay();
							long rRecur = rm.getRecur();
							String rRtime = rm.getReceivingTime();
							voice = rm.getVoiceFile();

							//date 
							Calendar dateCal = Calendar.getInstance();
							dateCal.setTimeInMillis(rDate);

							int dayOfMonth = dateCal.get(Calendar.DAY_OF_MONTH);
							int monthOfYear = dateCal.get(Calendar.MONTH);
							int Year = dateCal.get(Calendar.YEAR);		


							//time 
							Calendar timeCal=Calendar.getInstance();
							timeCal.setTimeInMillis(rTime);

							int hr = timeCal.get(Calendar.HOUR_OF_DAY);
							int min = timeCal.get(Calendar.MINUTE);



							Calendar combCal=Calendar.getInstance();
							String timeString="";

							//if(hr.equals("none")){
							combCal.set(Year, monthOfYear, dayOfMonth, hr, min, 0);
							combCal.getTimeInMillis();
							timeString = String.valueOf(hr) + "_" + String.valueOf(min);
							//}
							//else{
							//combCal.set(Year, monthOfYear, dayOfMonth, Integer.parseInt(hr), Integer.parseInt(min),0);
							//combCal.getTimeInMillis();
							//timeString = String.valueOf(hr) + "_" + String.valueOf(min);
							//}

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
								Intent i = new Intent(NewReminder.this, ReminderListScreen.class);
								int tempReqCode=new ReminderLocalDB(getApplicationContext()).getReminderReqCode(timeString);
								i.putExtra("rTimeString", timeString);
								i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
								pendingIntent = PendingIntent.getActivity(NewReminder.this,
										tempReqCode, i, 0);	
								am.setRepeating(AlarmManager.RTC_WAKEUP, combCal.getTimeInMillis()-rDelay,rRecur,
										pendingIntent);

								Intent intent = new Intent(NewReminder.this, ReminderScreen.class);
								intent.putExtra("sender", "");
								intent.putExtra("rTitle", "");
								intent.putExtra("rem_id", "");
								intent.putExtra("voicefile", "");
								intent.putExtra("location", "");
								intent.putExtra("gid", "");
								intent.putExtra("remTime", "");
								intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
								pendingIntent = PendingIntent.getActivity(NewReminder.this, tempReqCode, intent, 0);
								pendingIntent.cancel();
							}
							else{						
								//Create a new PendingIntent and add it to the AlarmManager
								Intent intent = new Intent(NewReminder.this, ReminderScreen.class);
								if (gid.equals("0")){
									intent.putExtra("sender", sender);
								}
								else{
									intent.putExtra("sender", gpname);
								}
								intent.putExtra("rTitle", rTitle);
								intent.putExtra("rem_id", reminderid);
								intent.putExtra("voicefile", voice);
								intent.putExtra("location", lname);
								intent.putExtra("gid", gid);
								intent.putExtra("remTime", rem_time);
								intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
								pendingIntent = PendingIntent.getActivity(NewReminder.this,
										reqCode, intent, 0);	
								am.setRepeating(AlarmManager.RTC_WAKEUP, combCal.getTimeInMillis()-rDelay,rRecur,
										pendingIntent);
							}	
							new ReminderLocalDB(getApplicationContext()).updateReqCode(reminderid, reqCode); 
						}

						if(voice!="TEXT"){
							new Thread(new Runnable() {
								public void run() {

									try {
										File root = android.os.Environment.getExternalStorageDirectory();               

										File dir = new File (root.getAbsolutePath() + "/RingABell");
										if(dir.exists()==false) {
											dir.mkdirs();
										}

										String filename = viewHolder.rm + ".3gp";
										//String filename = "abc.3gp";

										//URL url = new URL("http://tech-sync.com/ringabell/recording/myrecording.3gp"); //you can write here any link
										URL url = new URL(voice); //you can write here any link
										File file = new File(dir, filename);

										long startTime = System.currentTimeMillis();
										Log.d("DownloadManager", "download begining");
										Log.d("DownloadManager", "download url:" + url);
										Log.d("DownloadManager", "downloaded file name:" + viewHolder.rm + ".3gp");

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
						initView();
						finish();
					}
					else{
						Toast.makeText(getApplicationContext(), "No Internet Connection", Toast.LENGTH_SHORT).show();

					}

				}
			});

			viewHolder.rem_decline.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					//Toast.makeText(getApplicationContext(), "Reminder Dieclined", Toast.LENGTH_SHORT).show();
					//new ReminderLocalDB(getApplicationContext()).updateReminderSataus(viewHolder.rm);
					if(cDetector.isConnectingToInternet()){

						if(new ContactLocalDB(getApplicationContext()).checkIfContactExists(viewHolder.sn)){
							reminder_accept="2";
							new NotifySender().execute();
							//sendNotification(new ContactLocalDB(getApplicationContext()).getGcmRegisrationId(sender), "Accepted:R:" + reminderid + ":N:G:S:H:M");
						}
						else{
							//sendMessage(receivername);
							//Toast.makeText(getApplicationContext(), "sms", Toast.LENGTH_LONG).show();
						}

						new ReminderLocalDB(getApplicationContext()).deleteReminderonDecline(viewHolder.rm);
						initView();
					}
					else{
						Toast.makeText(getApplicationContext(), "No Internet Connection", Toast.LENGTH_SHORT).show();
					}
				}
			});			
			return convertView;
		}
	}
	private String timeStampToTime(long timestamp){
		Date date=new Date(timestamp);

		DateFormat sf = new SimpleDateFormat("hh:mm a");
		String timeVal=(String) sf.format(date);
		return timeVal;
	}

	private String timeStampToDateTime(long timestamp){
		Date date=new Date(timestamp);

		DateFormat sf = new SimpleDateFormat("dd/MM/yyyy hh:mm a");
		String timeVal=(String) sf.format(date);
		return timeVal;
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
}
