package com.ringabell.reminder;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.ringabell.localdb.ContactLocalDB;
import com.ringabell.localdb.GroupsLocalDB;
import com.ringabell.localdb.ReminderLocalDB;
import com.ringabell.model.Group;
import com.ringabell.model.Reminder;
import com.share2people.ringabell.R;
import com.share2people.ringabell.R.color;
import com.share2people.ringabell.R.drawable;
import com.share2people.ringabell.R.id;
import com.share2people.ringabell.R.layout;
import com.share2people.ringabell.R.string;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.media.MediaPlayer.OnCompletionListener;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.os.Vibrator;
import android.preference.PreferenceManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.QuickContactBadge;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class ReminderListScreen extends Activity {
	ListView listView;
	TextView noRecordText;
	List<Reminder> reminderList;
	CustomAdapter adapter;
	LayoutInflater inflator;
	AlarmManager am;
	PendingIntent pendingIntent;
	String timeString="";
    protected ImageLoader imageLoader;
	DisplayImageOptions options;
	private ImageLoadingListener animateFirstListener = new AnimateFirstDisplayListener();
	Vibrator vibrator;
	MediaPlayer mp=null;
	private String gid="";
	private boolean vibrationMode =false, finish=false, callingMode=false;
;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_reminder_list_screen);

		getActionBar().setTitle(getString(R.string.reminders_list));
		getActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.block_main_action_bar)));

		vibrator = (Vibrator) getApplicationContext().getSystemService(Context.VIBRATOR_SERVICE);		
		am = (AlarmManager)getSystemService(Activity.ALARM_SERVICE);
		
		noRecordText=(TextView) findViewById(R.id.noReminder);

		listView=(ListView) findViewById(R.id.remlist);
		timeString = getIntent().getStringExtra("rTimeString");
		reminderList=new ReminderLocalDB(getApplicationContext()).getReminderByTime(timeString);
		adapter=new CustomAdapter(getApplicationContext(), R.layout.contact_reminder_layout, reminderList);
		noRecordText.setVisibility(View.GONE);
		listView.setVisibility(View.VISIBLE);
		listView.setAdapter(adapter);
		
		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
		vibrationMode=sharedPrefs.getBoolean("reminder_viberate", false);
		
	
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
			mp = MediaPlayer.create(getBaseContext(), getAlarmUri());


		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int pos,
					long arg3) {
				
				mp.stop();
				vibrator.cancel();
				long rDate = ((Reminder) arg0.getAdapter().getItem(pos)).getRemDate();
				long rDelay = ((Reminder) arg0.getAdapter().getItem(pos)).getDelay();
				long rRecur = ((Reminder) arg0.getAdapter().getItem(pos)).getRecur();
				String rem_id = ((Reminder) arg0.getAdapter().getItem(pos)).getRemId();

				//date 
				Calendar dateCal = Calendar.getInstance();
				dateCal.setTimeInMillis(rDate);

				int dayOfMonth = dateCal.get(Calendar.DAY_OF_MONTH);
				int monthOfYear = dateCal.get(Calendar.MONTH);
				int Year = dateCal.get(Calendar.YEAR);					

				//time 
				String[] ar =timeString.split("_");
				Calendar combCal=Calendar.getInstance();
				combCal.set(Year, monthOfYear, dayOfMonth, Integer.parseInt(ar[0]), Integer.parseInt(ar[1]),0);
				combCal.getTimeInMillis();

				int reqCode=0;
				String sender = "";
				gid=((Reminder) arg0.getAdapter().getItem(pos)).getGroupId();
				if (!gid.equals("0")){
					List<Group> gp = new GroupsLocalDB(getApplicationContext()).getGroupById(gid);
					for (Group g : gp) {
						sender=g.getName();
					}
				}
				else{
					sender= ((Reminder) arg0.getAdapter().getItem(pos)).getSender();
				}
				
				Calendar timeCal=Calendar.getInstance();
				timeCal.setTimeInMillis(((Reminder) arg0.getAdapter().getItem(pos)).getRemTime());
				long rTime = timeCal.getTimeInMillis();
				String remtime = timeStampToTime(rTime);
				
				reqCode=((Reminder) arg0.getAdapter().getItem(pos)).getReqCode();
				Intent intent = new Intent(ReminderListScreen.this, ReminderScreen.class);
				intent.putExtra("sender",sender);
				intent.putExtra("rTitle", ((Reminder) arg0.getAdapter().getItem(pos)).getTitle());
				intent.putExtra("rem_id", ((Reminder) arg0.getAdapter().getItem(pos)).getRemId());
				intent.putExtra("voicefile", ((Reminder) arg0.getAdapter().getItem(pos)).getVoiceFile());
				intent.putExtra("location", ((Reminder) arg0.getAdapter().getItem(pos)).getLocationName());
				intent.putExtra("gid", gid);
				intent.putExtra("remTime", remtime);
				intent.putExtra("position", pos);

				//int req_code = System.currentTimeMillis();
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				pendingIntent = PendingIntent.getActivity(ReminderListScreen.this,
						reqCode, intent, 0);	
				am.setRepeating(AlarmManager.RTC_WAKEUP, combCal.getTimeInMillis()-rDelay,rRecur,
						pendingIntent);

				reminderList.remove(pos);
				adapter.notifyDataSetChanged();
				adapter.notifyDataSetInvalidated();
				new ReminderLocalDB(getApplicationContext()).updateTimeString(rem_id);
				if(reminderList.size()<1){
					finish=true;
					finish();
				}
			}
		});		
		playSound(this, getAlarmUri());

	}
	
	public static void initImageLoader(Context context) {
		ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(context)
		.threadPriority(Thread.NORM_PRIORITY - 2)
		.denyCacheImageMultipleSizesInMemory()
		.diskCacheFileNameGenerator(new Md5FileNameGenerator())
		.diskCacheSize(50 * 1024 * 1024) // 50 Mb
		.tasksProcessingOrder(QueueProcessingType.LIFO)
		.writeDebugLogs() // Remove for release app
		.build();
		// Initialize ImageLoader with configuration.
		ImageLoader.getInstance().init(config);
	}
	
	private void playSound(final Context context, Uri alert) {

		Thread background = new Thread(new Runnable() {
			public void run() {
				try {
					mp.start();
				} catch (Throwable t) {
					Log.i("Animation", "Thread  exception "+t);
				} 
				mp.setOnCompletionListener(new OnCompletionListener() {
					//When audio is done will change pause to play
					public void onCompletion(MediaPlayer mpl) {
						//mp2.release();
						try {
							mp.prepare();
						} catch (IllegalStateException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						mp.start();
					}
				});			
			}
		});
		background.start();
	}
	
	
	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		//super.onBackPressed();
	}
	
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		if(!finish){
			mp.pause();	
			vibrator.cancel();
			callingMode=true;
		}		
	}
	
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		if(!callingMode){
			if(vibrationMode){
				AudioManager amanager = 
						(AudioManager) getSystemService(Context.AUDIO_SERVICE);

				amanager.setStreamVolume(
						AudioManager.STREAM_MUSIC,
						0,
						0);	
				long[] pattern = {0, 300, 200};
				vibrator.vibrate(pattern,1);
			}
			else{
				AudioManager amanager = 
						(AudioManager) getSystemService(Context.AUDIO_SERVICE);

				amanager.setStreamVolume(
						AudioManager.STREAM_MUSIC,
						amanager.getStreamMaxVolume(AudioManager.STREAM_MUSIC),
						0);
			}
		}
		else{
			mp.start();	
			long[] pattern = {0, 300, 200};
			vibrator.vibrate(pattern,1);
		}		
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
	
	private String timeStampToTime(long timestamp){
		Date date=new Date(timestamp);
		DateFormat sf = new SimpleDateFormat("hh:mm a");
		String timeVal=(String) sf.format(date);
		return timeVal;
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

		public CustomAdapter(Context context,int resId,List <Reminder> reminderList){
			super(context,resId,reminderList);
			this.reminders=reminderList;			
		}

		private class ViewHolder{
			TextView title,sender,time,date, location_name, responseText;
			ImageView audioImage,sendImage, deliverImage,visibleImage;
			 QuickContactBadge profilePic;
		}
		public Reminder getItem(int position) {
			return reminders.get(position);
		};
		
		ViewHolder viewHolder;
		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			inflator = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView=inflator.inflate(R.layout.contact_reminder_layout, null);
			viewHolder=new ViewHolder();
			viewHolder.time=(TextView) convertView.findViewById(R.id.ctimeText);
			viewHolder.date=(TextView) convertView.findViewById(R.id.cdateText);
			viewHolder.title=(TextView) convertView.findViewById(R.id.cnameText);
			viewHolder.sender=(TextView) convertView.findViewById(R.id.cphoneText);

			viewHolder.responseText=(TextView) convertView.findViewById(R.id.cResponse);
			viewHolder.audioImage=(ImageView) convertView.findViewById(R.id.caudioIcon);
			viewHolder.visibleImage=(ImageView) convertView.findViewById(R.id.cvisibleIcon);
			viewHolder.sendImage=(ImageView) convertView.findViewById(R.id.csendIcon);
			viewHolder.deliverImage=(ImageView) convertView.findViewById(R.id.cDeliverIcon);
			viewHolder.profilePic=(QuickContactBadge) convertView.findViewById(R.id.cIcon);
			convertView.setTag(viewHolder);
			final Reminder rem=reminders.get(position);
			
			viewHolder.responseText.setVisibility(View.GONE);
			
			if(!gid.equals("0")){
				imageLoader.displayImage(new GroupsLocalDB(getApplicationContext()).getGroupPicUrl(gid),  viewHolder.profilePic, options, animateFirstListener);
			}
			else{
				imageLoader.displayImage(new ContactLocalDB(getApplicationContext()).getContactPicUrl(rem.getSender()), viewHolder.profilePic, options, animateFirstListener);
			}
			
			viewHolder.title.setText(rem.getTitle());
			viewHolder.sender.setText(rem.getSender());
			return convertView;
		}
	}
	
	private Uri getAlarmUri() {

		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);

		boolean vibration=sharedPrefs.getBoolean("notifications_new_message_vibrate", true);
		if(vibration){
			// Vibrate for 500 milliseconds
			long[] pattern = {0, 1000, 500};
			vibrator.vibrate(pattern,0);

			Handler handler = new Handler();
			handler.postDelayed(new Runnable() {
				public void run() {
					vibrator.cancel();
				}}, 5000);
		}

		Uri alert ;
		String almRingtone=sharedPrefs.getString("notifications_alarm_ringtone", null);
		if (almRingtone == null) {
			alert = RingtoneManager
					.getDefaultUri(RingtoneManager.TYPE_ALARM);
			if (alert == null) {
				alert = RingtoneManager
						
						.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
				if (alert == null) {
					alert = RingtoneManager
							.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
				}
			}
		}
		else{
			alert=Uri.parse(almRingtone);
		}
		return alert;
	}

}
