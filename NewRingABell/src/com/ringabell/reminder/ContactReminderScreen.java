package com.ringabell.reminder;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Timer;

import android.R.string;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
import com.ringabell.localdb.ReminderLocalDB;
import com.ringabell.model.Contact;
import com.ringabell.model.Reminder;
import com.ringabell.utils.ConnectionDetector;
import com.share2people.ringabell.R;
import com.share2people.ringabell.R.color;
import com.share2people.ringabell.R.drawable;
import com.share2people.ringabell.R.id;
import com.share2people.ringabell.R.layout;

import de.timroes.swipetodismiss.SwipeDismissList;

public class ContactReminderScreen extends Activity {
	Button addReminder,existingTemplate;
	TextView noRecordText,dateText;
	ImageView play, pause;
	Button cancelBtn;
	private ProgressDialog pDialog;
	private Context mContext;
	private SharedPreferences sharedPref;
	ListView listView;
	LayoutInflater inflator;
	CustomAdapter adapter;
	List<Reminder> reminderList;
	ConnectionDetector cDetector;
	private List<Contact> contactList;
	private List <String> idList;
	HashMap<String, String> regIdMap;
	HashMap<String, Contact> contactMap;
	String selectedItem="", outputFile="";
	final int DATE_DIALOG_ID = 1;
	Calendar calendar;
	int day,month,year;
	Timer timer;
	String grp_user = "",usernumber="", selected_item="",contactName="", remid="";
	int rcode=1;
	SharedPreferences.Editor editor;
	private SwipeDismissList mSwipeList;
	long mLastStopTime = 0;
	MediaPlayer mp2=new MediaPlayer() ;
	Chronometer chronometer;
	int play_flag=0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.contact_reminder_screen);

		contactName = getIntent().getStringExtra("NAME");

		getActionBar().setTitle(contactName);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		getActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.block_main_action_bar)));

		mContext=(Context) getApplicationContext();
		noRecordText=(TextView) findViewById(R.id.noReminder);
		listView=(ListView) findViewById(R.id.rlist);
		cDetector=new ConnectionDetector(mContext);
		usernumber = getIntent().getStringExtra("NUMBER");
		if(new ReminderLocalDB(mContext).getAllEventsByContact()>0){
			reminderList=new ReminderLocalDB(mContext).getReminderByContact(usernumber);
			adapter=new CustomAdapter(mContext, R.layout.contact_reminder_layout, reminderList);
			noRecordText.setVisibility(View.GONE);
			listView.setVisibility(View.VISIBLE);
			listView.setAdapter(adapter);
		}
		else{
			noRecordText.setVisibility(View.VISIBLE);
			noRecordText.setText("No Reminders");
		}
		
		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View v, int arg2,
					long arg3) {
				// TODO Auto-generated method stub
				
				String title="", response="", voicefile="";					
				title=((Reminder) arg0.getAdapter().getItem(arg2)).getTitle();
				response=((Reminder) arg0.getAdapter().getItem(arg2)).getResponseMsg();

				remid=((Reminder) arg0.getAdapter().getItem(arg2)).getRemId();
				voicefile=((Reminder) arg0.getAdapter().getItem(arg2)).getVoiceFile();
				
				LayoutInflater li = LayoutInflater.from(ContactReminderScreen.this);
				LinearLayout rinfo = (LinearLayout)li.inflate(R.layout.reminder_info, null);				
				AlertDialog.Builder builder = new AlertDialog.Builder(ContactReminderScreen.this);
				builder.setTitle("Details");
				builder.setView(rinfo);
				builder.setCancelable(false);

				TextView textValue = (TextView) rinfo.findViewById(R.id.textValue);
				textValue.setText(title);
				
				TextView responseValue = (TextView) rinfo.findViewById(R.id.responseValue);
				responseValue.setText(response);
				
				chronometer = (Chronometer) rinfo.findViewById(R.id.chronometer);
				play = (ImageView) rinfo.findViewById(R.id.aPlay);
				pause = (ImageView) rinfo.findViewById(R.id.aPause);
				play.setVisibility(View.GONE);

				if (!voicefile.equals("TEXT")){
					chronometer.setVisibility(View.VISIBLE);
					play.setVisibility(View.VISIBLE);					
				}
				
				play.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						play_flag=1;
						mp2 = new MediaPlayer() ;
						
						outputFile = Environment.getExternalStorageDirectory().
								getAbsolutePath() + "/RingABell/" + remid + ".3gp";
						
						try {
							mp2.setDataSource(outputFile);
						}
						catch (FileNotFoundException e) {
							// TODO: handle exception
							Toast.makeText(ContactReminderScreen.this, "File Not Exists.", Toast.LENGTH_SHORT).show();

							runOnUiThread(new Runnable() {
								public void run() {
									Toast.makeText(ContactReminderScreen.this, "File Not Exists.", Toast.LENGTH_SHORT).show();
								}
							});  

						}catch (IOException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
						
						mp2.setOnCompletionListener(new OnCompletionListener() {
							//When audio is done will change pause to play
							public void onCompletion(MediaPlayer mp) {
								pause.setVisibility(View.GONE);
								play.setVisibility(View.VISIBLE);
								chronometer.stop();	
								chronometer.setBase( SystemClock.elapsedRealtime() );

							}
						});		
						try {
							mp2.prepare();
							mp2.start();
							
							// on first start
							if ( mLastStopTime == 0 )
								chronometer.setBase( SystemClock.elapsedRealtime() );
							// on resume after pause
							else
							{
								long intervalOnPause = (SystemClock.elapsedRealtime() - mLastStopTime);
								chronometer.setBase( chronometer.getBase() + intervalOnPause );
							}
							chronometer.start();
							play.setVisibility(View.GONE);
							pause.setVisibility(View.VISIBLE);
						}
						
						catch (IllegalStateException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}					
					}
				});

				pause.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						mp2.pause();
						chronometer.stop();
						mLastStopTime = SystemClock.elapsedRealtime();
						pause.setVisibility(View.GONE);
						play.setVisibility(View.VISIBLE);
					}
				});
				
				builder.setPositiveButton("OK",
						new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						if(play_flag==1){
							mp2.stop();
							mp2.release();
							chronometer.stop();
							 mLastStopTime= 0;
							play_flag=0;
						}						
					}
				});				

				final AlertDialog dialog = builder.create();
				dialog.show();
								
			}			
		});

		SwipeDismissList.UndoMode mode = SwipeDismissList.UndoMode.values()[0];		
		final ListView mListView=listView; 


		// Create a new SwipeDismissList from the activities listview.
		mSwipeList = new SwipeDismissList(
				// 1st parameter is the ListView you want to use
				mListView,					
				new SwipeDismissList.OnDismissCallback() {

					public SwipeDismissList.Undoable onDismiss(AbsListView listView, final int position) {

						// Get item that should be deleted from the adapter.
						rcode=adapter.getItem(position).getReqCode();
						new ReminderLocalDB(getApplicationContext()).updateRemDelete(adapter.getItem(position).getRemId());
						selected_item = adapter.getItem(position).getTitle();
						adapter.remove(adapter.getItem(position));	
						adapter.notifyDataSetChanged();

						return new SwipeDismissList.Undoable() {
							
							@Override
							public String getTitle() {
								return selected_item + " deleted";
							}

							@Override
							public void undo() {
								// Reinsert the item at its previous position.
								reminderList=new ReminderLocalDB(mContext).getReminderByContact(usernumber);
								adapter=new CustomAdapter(mContext, R.layout.contact_reminder_layout, reminderList);
								mListView.setAdapter(adapter);
								new ReminderLocalDB(getApplicationContext()).updateRemDeleteAgain(adapter.getItem(position).getRemId());
								rcode=1;
							}

							@Override
							public void discard() {
								// Just write a log message (use logcat to see the effect)
								deleteReminder();
								rcode=1;
							}
						};
					}				
				},
				// 3rd parameter needs to be the mode the list is generated.
				mode);		
	}


	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		super.onBackPressed();		
	}
	
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		if(rcode != 1){
			deleteReminder();
		}
		if(reminderList.size()==0){
			new ReminderLocalDB(getApplicationContext()).deleteRecentContact(usernumber);
		}
	}
	
	
	public void deleteReminder(){
		Intent intent = new Intent(ContactReminderScreen.this, ReminderScreen.class);
		intent.putExtra("sender", "");
		intent.putExtra("rTitle", "");
		intent.putExtra("rem_id", "");
		intent.putExtra("voicefile", "");
		intent.putExtra("location", "");
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		PendingIntent pendingIntent = PendingIntent.getActivity(ContactReminderScreen.this, rcode, intent, 0);
		pendingIntent.cancel();
		new ReminderLocalDB(mContext).deleteReminder("1");
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

	@SuppressLint("ViewHolder")
	private class CustomAdapter extends ArrayAdapter<Reminder>{

		Context lContext;
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
			.showImageOnLoading(R.drawable.ic_contact_picture_holo_light)
			.showImageForEmptyUri(R.drawable.ic_contact_picture_holo_light)
			.showImageOnFail(R.drawable.ic_contact_picture_holo_light)
			.cacheInMemory(true)
			.cacheOnDisk(true)
			.considerExifParams(true)
			.displayer(new RoundedBitmapDisplayer(20))
			.build();
		}

		private class ViewHolder{
			TextView title,receiver,time,date, location_name, responseText;
			ImageView audioImage,sendImage, deliverImage,visibleImage;
			//LinearLayout callLayout;
			//RelativeLayout userProfile;
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
			//if(convertView==null){
			convertView=inflator.inflate(R.layout.contact_reminder_layout, null);
			viewHolder=new ViewHolder();
			viewHolder.time=(TextView) convertView.findViewById(R.id.ctimeText);
			viewHolder.date=(TextView) convertView.findViewById(R.id.cdateText);
			viewHolder.title=(TextView) convertView.findViewById(R.id.cnameText);
			viewHolder.receiver=(TextView) convertView.findViewById(R.id.cphoneText);
			viewHolder.responseText=(TextView) convertView.findViewById(R.id.cResponse);
			//viewHolder.response_msg=(TextView) convertView.findViewById(R.id.responseMsg);
			//viewHolder.acknowledgeText=(TextView) convertView.findViewById(R.id.acknowledgeMsg);


			viewHolder.audioImage=(ImageView) convertView.findViewById(R.id.caudioIcon);
			viewHolder.visibleImage=(ImageView) convertView.findViewById(R.id.cvisibleIcon);
			viewHolder.sendImage=(ImageView) convertView.findViewById(R.id.csendIcon);
			viewHolder.deliverImage=(ImageView) convertView.findViewById(R.id.cDeliverIcon);
			viewHolder.profilePic=(QuickContactBadge) convertView.findViewById(R.id.cIcon);
			convertView.setTag(viewHolder);
			
			//}
			//else 
			//viewHolder=(ViewHolder) convertView.getTskyag();

			final Reminder rem=reminders.get(position);
			viewHolder.profilePic.setVisibility(View.GONE);
			viewHolder.title.setText(rem.getTitle());

			viewHolder.time.setText(timeStampToTime(rem.getRemTime()));
			viewHolder.date.setText(timeStampToDate(rem.getRemDate()));

			//	viewHolder.time.setText(timeStampToDate(rem.getRemDate()) + " - " +timeStampToTime(rem.getRemTime()));
			if(!(adapter.getItem(position).getGroupId()).equals("0")){
				viewHolder.receiver.setText(rem.getSender());
			}
			else{
				viewHolder.receiver.setText(rem.getLocationName());			
			}

			try{
				
				if(!rem.getResponseMsg().equals("")){
					viewHolder.responseText.setVisibility(View.VISIBLE);
				}
				else{
					viewHolder.responseText.setVisibility(View.GONE);
				}
				
				if(!rem.getVoiceFile().equals("TEXT")){
					viewHolder.audioImage.setVisibility(View.VISIBLE);
					viewHolder.audioImage.setImageResource(R.drawable.ic_action_volume_on);
				}
			}
			catch(Exception e){
				//viewHolder.visibleImage.setImageResource(R.drawable.ic_action_event);
			}

			try{
				if(rem.getReminderType().equals("IN")){
					//imageLoader.displayImage(new ContactLocalDB(mContext).getContactPicUrl(rem.getSender()), viewHolder.profilePic, options, animateFirstListener);
					viewHolder.visibleImage.setVisibility(View.VISIBLE);
					viewHolder.visibleImage.setImageResource(R.drawable.ic_action_reminder_in);
				}
				else{
					//imageLoader.displayImage(new ContactLocalDB(mContext).getContactPicUrl(rem.getReceiver()), viewHolder.profilePic, options, animateFirstListener);
					viewHolder.visibleImage.setVisibility(View.VISIBLE);
					viewHolder.sendImage.setVisibility(View.VISIBLE);
					viewHolder.visibleImage.setImageResource(R.drawable.ic_action_reminder_out);
					viewHolder.sendImage.setImageResource(R.drawable.emoticon_smile);
					
					
					if(rem.getRemDeliverStaus().equals("YES")){
						viewHolder.deliverImage.setVisibility(View.VISIBLE);
						viewHolder.deliverImage.setImageResource(R.drawable.emoticon_smile);
					}
					else if(rem.getRemDeliverStaus().equals("DECLINE")){
						viewHolder.deliverImage.setVisibility(View.VISIBLE);
						viewHolder.deliverImage.setImageResource(R.drawable.emoticon_neutral);
					}
					else{
						viewHolder.deliverImage.setVisibility(View.VISIBLE);
						viewHolder.deliverImage.setImageResource(R.drawable.emoticon_unhappy);
					}
				}
			}
			catch(Exception e){
				//viewHolder.visibleImage.setImageResource(R.drawable.ic_action_event);
			}			
			return convertView;
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

		DateFormat sf = new SimpleDateFormat("dd-MMM-yy");
		String timeVal=(String) sf.format(date);
		return timeVal;
	}	
}
