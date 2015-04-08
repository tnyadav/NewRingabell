package com.ringabell.reminder;


import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.SystemClock;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.support.v4.app.FragmentActivity;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.fourmob.datetimepicker.date.DatePickerDialog;
import com.fourmob.datetimepicker.date.DatePickerDialog.OnDateSetListener;
import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.PhoneNumberUtil.PhoneNumberFormat;
import com.google.i18n.phonenumbers.Phonenumber.PhoneNumber;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.ringabell.localdb.ContactLocalDB;
import com.ringabell.localdb.GroupsLocalDB;
import com.ringabell.localdb.ReminderLocalDB;
import com.ringabell.localdb.TemplateLocalDB;
import com.ringabell.model.Contact;
import com.ringabell.model.Group;
import com.ringabell.serverdb.ServiceHandler;
import com.ringabell.utils.ConnectionDetector;
import com.share2people.ringabell.DisplayContactActivity;
import com.share2people.ringabell.R;
import com.share2people.ringabell.R.color;
import com.share2people.ringabell.R.drawable;
import com.share2people.ringabell.R.id;
import com.share2people.ringabell.R.layout;
import com.share2people.ringabell.R.string;
import com.sleepbot.datetimepicker.time.RadialPickerLayout;
import com.sleepbot.datetimepicker.time.TimePickerDialog;


public class AddReminderActivity extends FragmentActivity implements OnDateSetListener, TimePickerDialog.OnTimeSetListener {

	public static final String DATEPICKER_TAG = "datepicker";
	public static final String TIMEPICKER_TAG = "timepicker";
	public static final String AUDIO_DIRECTORY_NAME="RingABell";


	ConnectionDetector cDetector;
	private TextView messageText, recurrtv;
	private int serverResponseCode = 0;
	private ProgressDialog dialog = null;

	private String imagepath=null;	
	RadioGroup rbGroup;
	ImageView imgContact, remove_contact;
	TextView ch, more;
	ArrayList <String> contacts= new ArrayList<String>();
	ArrayList <String> contactsNo= new ArrayList<String>();

	//ArrayList<ChipsItem> arrContact = new ArrayList<ChipsItem>();
	public ArrayAdapter<String> adapter;

	AlarmManager am;
	PendingIntent pendingIntent;
	private MediaRecorder myAudioRecorder;
	private String outputFile = null;
	private ImageView start, play, stop;
	private int play_flag=0;
	private boolean isPlaying=false;
	SeekBar seek_bar;
	Chronometer myChronometer;
	Handler seekHandler = new Handler();
	static MediaPlayer mp2=new MediaPlayer() ;
	Handler handler = new Handler();
	Handler audio_limit_handler = new Handler();
	Runnable runnable;
	Runnable audio_limit_runnable;

	//Widgets
	LinearLayout locationLayout, delayLayout, tvdelayLayout, contactLayout, recurLayout, tvrecurLayout, scheduledTimeLayout, btnLayout, btnLayout2;
	ImageView contactPick,backImage, micButton;
	View rbView;
	EditText checkEdit;
	AutoCompleteTextView reminderTitleEdit,contactShow, textview;
	Button shareButton, shareBtn, cancelButton, cancelBtn ;
	EditText timeText,dateText,recurringText,delayText;
	private ProgressDialog pDialog;

	HashMap<String,Integer> timeMap;
	HashMap<String,Long> dateMap;
	HashMap<String,Long> recurringMap;
	HashMap<String,Long> delayMap;

	private static final int CONTACT_PICKER_RESULT = 1001;
	private static Context mContext;
	private SharedPreferences sharedPref;
	private Spinner dateSpin,timeSpin,recurringSpin,delaySpin;
	private String response="", hr="", min="",code="", sendername=null,receivername="",receiverno="",reminderID="", isMine="",reminderTitle="";
	private long reminderDate=-1,reminderTime=-1,recurringAlarm=-1,reminderDelay=-1;
	private File  mFileTemp;

	final int DATE_DIALOG_ID = 1;
	final int TIME_DIALOG_ID = 2;
	private int SEC = 60;
	private int MILLISEC = 1000;
	private int mYear;
	private int mMonth,cHour,cMinute;
	private int mDay,mMinute,mHour;
	private String month;
	private String dateOfBirth;
	String regId="",title="";
	boolean isUserExist = false, skipFlag = false;
	ArrayList<Contact> selectedContact;
	Geocoder geocoder;
	List<Address> coordinates;
	double longi = 0.0;
	double lat = 0.0;
	String filename = "";
	String voice_url = "TEXT";
	int contact_flag = 0;
	String gid="0";
	int recorded = 0;
	String number="";
	String AUDIO_FILENAME = String.valueOf(System.currentTimeMillis()) + "audio.3gp";

	protected ImageLoader imageLoader;
	DisplayImageOptions options;
	private ImageLoadingListener animateFirstListener = new AnimateFirstDisplayListener();

	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_add_reminder);
		sharedPref = getSharedPreferences("LAST_LOGIN", Activity.MODE_PRIVATE);
		geocoder = new Geocoder(this, Locale.getDefault());
		mContext=(Context) getApplicationContext();
		cDetector=new ConnectionDetector(mContext);
		code = sharedPref.getString("CODE", "");
		skipFlag = sharedPref.getBoolean("SKIP", false);

		ActionBar actionBar=getActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setTitle(getString(R.string.add_reminder));
		actionBar.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.add_reminder_action_bar)));
		am = (AlarmManager)getSystemService(Activity.ALARM_SERVICE);

		ch = (TextView) findViewById(R.id.receiverName);
		imgContact = (ImageView) findViewById(R.id.contactpic);
		more = (TextView) findViewById(R.id.more);
		remove_contact = (ImageView) findViewById(R.id.removeContact);
		imgContact.setVisibility(View.GONE);
		remove_contact.setVisibility(View.GONE);

		tvdelayLayout = (LinearLayout)
				findViewById(R.id.tvdelayLayout);
		delayLayout = (LinearLayout)
				findViewById(R.id.delayLayout);
		tvrecurLayout = (LinearLayout)
				findViewById(R.id.tvrecurrLayout);
		recurLayout = (LinearLayout)
				findViewById(R.id.recurringLayout);
		textview = (AutoCompleteTextView)
				findViewById(R.id.location);
		scheduledTimeLayout = (LinearLayout)
				findViewById(R.id.scheduledLayout);
		rbView = (View) findViewById(R.id.view2);

		rbGroup = (RadioGroup) findViewById(R.id.radioScheduled);
		btnLayout = (LinearLayout)
				findViewById(R.id.btnLayout);
		btnLayout2 = (LinearLayout)
				findViewById(R.id.buttonLayout2);
		contactLayout = (LinearLayout)
				findViewById(R.id.contactLayout);

		tvdelayLayout.setVisibility(View.GONE);
		delayLayout.setVisibility(View.GONE);
		tvrecurLayout.setVisibility(View.GONE);
		recurLayout.setVisibility(View.GONE);
		scheduledTimeLayout.setVisibility(View.GONE);
		rbGroup.setVisibility(View.GONE);

		adapter = new ArrayAdapter<String>(this,R.layout.list_item);
		adapter.setNotifyOnChange(true);
		textview.setAdapter(adapter);

		textview.addTextChangedListener(new TextWatcher() {

			public void onTextChanged(CharSequence s, int start, int before, int count) {
				if (count%3 == 1) {
					adapter.clear();
					if(cDetector.isConnectingToInternet())
					{
						GetPlaces task = new GetPlaces();
						//now pass the argument in the textview to the task
						task.execute(textview.getText().toString());
					}
				}
			}

			@Override
			public void afterTextChanged(Editable arg0) {
				// TODO Auto-generated method stub
			}

			@Override
			public void beforeTextChanged(CharSequence arg0, int arg1,
					int arg2, int arg3) {
				// TODO Auto-generated method stub
			}
		});

		more.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				tvdelayLayout.setVisibility(View.VISIBLE);
				delayLayout.setVisibility(View.VISIBLE);
				tvrecurLayout.setVisibility(View.VISIBLE);
				recurLayout.setVisibility(View.VISIBLE);
				scheduledTimeLayout.setVisibility(View.VISIBLE);
				rbGroup.setVisibility(View.VISIBLE);
				rbView.setVisibility(View.VISIBLE);
				more.setVisibility(View.GONE);
				btnLayout.setVisibility(View.VISIBLE);
				btnLayout2.setVisibility(View.GONE);
			}
		});


		//picking data from shared preference
		sendername=sharedPref.getString("USERNAME", null);

		title=getIntent().getStringExtra("TITLE");

		micButton=(ImageView) findViewById(R.id.micButton);
		contactPick=(ImageView) findViewById(R.id.contactPick);
		//backImage=(ImageView) findViewById(R.id.backImage);

		//date & Time Spinner
		dateSpin=(Spinner) findViewById(R.id.dateSpin);
		dateSpin.setVisibility(View.GONE);

		recurrtv=(TextView) findViewById(R.id.recurrtv);

		timeSpin=(Spinner) findViewById(R.id.timeSpin);
		timeSpin.setVisibility(View.GONE);

		recurringSpin=(Spinner) findViewById(R.id.recurringSpin);
		delaySpin=(Spinner) findViewById(R.id.delaySpin);


		dateText=(EditText) findViewById(R.id.dateSpinText);
		dateText.setInputType(InputType.TYPE_NULL);

		Date ddt=new Date(System.currentTimeMillis());
		DateFormat ddft = new SimpleDateFormat("dd-MMM-yyyy");
		dateText.setText(ddft.format(ddt));
		reminderDate=(System.currentTimeMillis());


		final Calendar calendar = Calendar.getInstance();

		final DatePickerDialog datePickerDialog = DatePickerDialog.newInstance(this, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
		final TimePickerDialog timePickerDialog = TimePickerDialog.newInstance(this, calendar.get(Calendar.HOUR_OF_DAY) ,calendar.get(Calendar.MINUTE), false, false);

		dateText.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				//showDialog(DATE_DIALOG_ID);
				// datePickerDialog.setVibrate(isVibrate());
				datePickerDialog.setYearRange(1985, 2028);
				// datePickerDialog.setCloseOnSingleTapDay(isCloseOnSingleTapDay());
				datePickerDialog.show(getSupportFragmentManager(), DATEPICKER_TAG);
			}
		});



		timeText=(EditText) findViewById(R.id.timeSpinText);
		timeText.setInputType(InputType.TYPE_NULL);

		Date dd=new Date(System.currentTimeMillis());
		DateFormat ddf = new SimpleDateFormat("hh:mm a");
		timeText.setText(ddf.format(dd));
		reminderTime=System.currentTimeMillis();

		timeText.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				timePickerDialog.show(getSupportFragmentManager(), TIMEPICKER_TAG);
			}
		});

		if (savedInstanceState != null) {
			DatePickerDialog dpd = (DatePickerDialog) getSupportFragmentManager().findFragmentByTag(DATEPICKER_TAG);
			if (dpd != null) {
				dpd.setOnDateSetListener(this);
			}

			TimePickerDialog tpd = (TimePickerDialog) getSupportFragmentManager().findFragmentByTag(TIMEPICKER_TAG);
			if (tpd != null) {
				tpd.setOnTimeSetListener(this);
			}
		}

		recurringText=(EditText) findViewById(R.id.recurringSpinText);
		recurringText.setVisibility(View.GONE);

		delayText=(EditText) findViewById(R.id.delaySpinText);
		delayText.setVisibility(View.GONE);

		//load calendar for date and time spinner
		//loadCalendarDate();
		//loadCalendarTime();
		loadRecurring();
		loadDelay();

		///load default calendar
		loadDefaultCalendar();

		//default delay
		reminderDelay=delayMap.get("On Time");
		//deafult recurring
		recurringAlarm=recurringMap.get("Once");

		//Toast.makeText(mContext, "date::"+showDateTime(reminderDate,reminderTime), Toast.LENGTH_SHORT).show();
		String dateArr[]=new String[]{"Today","Tomorrow","Pick A Date"};
		ArrayAdapter<String> dateAdp=new ArrayAdapter<String>(getApplicationContext(),R.layout.simple_dropdown_layout,dateArr);
		dateSpin.setAdapter(dateAdp);

		dateSpin.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO Auto-generated method stub
				String dateVal=parent.getItemAtPosition(position).toString();
				/*if(position==0){


				}*/
				if(position==2){
					dateSpin.setSelection(0);
					showDialog(DATE_DIALOG_ID);

				}
				else{
					//String dateVal=parent.getItemAtPosition(position).toString();
					//dateText.setText(dateVal);
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> adapter) {
				// TODO Auto-generated method stub

			}
		});
		String timeArr[]=new String[]{"Morning 9:00 am","Afternoon 12:00 pm","Evening 3:00 pm","Night 6:00 pm","Pick A Time"};
		ArrayAdapter<String> timeAdp=new ArrayAdapter<String>(getApplicationContext(),R.layout.simple_dropdown_layout,timeArr);
		timeSpin.setAdapter(timeAdp);
		timeSpin.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> adapter, View view,
					int position, long id) {
				// TODO Auto-generated method stub

				///String timeVal=adapter.getItemAtPosition(position).toString();
				//Toast.makeText(getApplicationContext(), "Time"+timeVal, Toast.LENGTH_SHORT).show();
				if(position==4){
					String timeVal=adapter.getItemAtPosition(0).toString();
					timeSpin.setSelection(0);
					timeText.setText(timeVal);
					showDialog(TIME_DIALOG_ID);
				}
				else{
					//String timeVal=adapter.getItemAtPosition(position).toString();
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				// TODO Auto-generated method stub
				//Toast.makeText(getApplicationContext(), "Time unselected", Toast.LENGTH_SHORT).show();
			}
		});

		String recurringArr[]=new String[]{"Once","5 min","10 min","15 min","30 min","Every Hour","Every Day","Every Week","Every Month"};
		ArrayAdapter<String> recurringAdp=new ArrayAdapter<String>(getApplicationContext(),R.layout.simple_dropdown_layout,recurringArr);
		recurringSpin.setAdapter(recurringAdp);
		recurringSpin.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> adapter, View view,
					int position, long id) {
				// TODO Auto-generated method stub

				String recurringVal=adapter.getItemAtPosition(position).toString();

				recurringText.setText(recurringVal);
				recurringAlarm=recurringMap.get(recurringVal);
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				// TODO Auto-generated method stub
				//Toast.makeText(getApplicationContext(), "recurring unselected", Toast.LENGTH_SHORT).show();
			}
		});
		String delayArr[]=new String[]{"On Time","5 min","10 min","15 min","30 min"};
		ArrayAdapter<String> delayAdp=new ArrayAdapter<String>(getApplicationContext(),R.layout.simple_dropdown_layout,delayArr);
		delaySpin.setAdapter(delayAdp);
		delaySpin.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> adapter, View view,
					int position, long id) {
				// TODO Auto-generated method stub

				String timeVal=adapter.getItemAtPosition(position).toString();
				delayText.setText(timeVal);
				reminderDelay=delayMap.get(timeVal);
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				// TODO Auto-generated method stub
				//	Toast.makeText(getApplicationContext(), "recurring unselected", Toast.LENGTH_SHORT).show();
			}
		});


		remove_contact.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				ch.setText("");
				imgContact.setVisibility(View.GONE);
				remove_contact.setVisibility(View.GONE);
				contactLayout.setEnabled(true);

			}
		});


		//contactShow=(AutoCompleteTextView) findViewById(R.id.receiverNumber);
		shareButton=(Button) findViewById(R.id.shareButton);
		shareBtn=(Button) findViewById(R.id.shareBtn);

		cancelButton=(Button) findViewById(R.id.cancelButton);
		cancelBtn=(Button) findViewById(R.id.cancelBtn);


		reminderTitleEdit=(AutoCompleteTextView) findViewById(R.id.reminderTitleEdit);

		List<String> tempList=new ArrayList<String>();
		tempList=new TemplateLocalDB(mContext).getAllTemplateList();

		ArrayAdapter<String> adapter = new ArrayAdapter<String> 
		(this,android.R.layout.simple_list_item_1,tempList);

		reminderTitleEdit.setAdapter(adapter);        
		reminderTitleEdit.setText(title);
		shareButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View view) {
				// TODO Auto-generated method stub
				shareReminder();
			}
		});

		shareBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				shareReminder();

			}
		});

		micButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View view) {
				if(skipFlag){
					//Toast.makeText(mContext, "Register To Send Reminder", Toast.LENGTH_SHORT).show();
					showAlertDialog(AddReminderActivity.this,
							"Registration Required",
							"Please Register To Send Audio Reminders To Your Friends.", false);
				}
				else{
					if(recorded == 0){
						LayoutInflater li = LayoutInflater.from(AddReminderActivity.this);
						LinearLayout voice = (LinearLayout)li.inflate(R.layout.activity_record_audio, null);				
						AlertDialog.Builder builder = new AlertDialog.Builder(AddReminderActivity.this);
						builder.setTitle("Recorder");
						builder.setView(voice);
						builder.setCancelable(false);


						builder.setPositiveButton("Done Recording",
								new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int whichButton) {
								micButton.setBackgroundResource(R.drawable.circle_background);
								recorded = 1;
								play_flag = 0;
								audio_limit_handler.removeCallbacks(audio_limit_runnable);
								voice_url = ServiceHandler.fileUrl + filename;
							}
						});	
						builder.setNegativeButton("Cancel",
								new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int whichButton) {
								if(play_flag == 1){
									myChronometer.stop();
									myAudioRecorder.stop();
									myAudioRecorder.release();
									myAudioRecorder  = null;
									audio_limit_handler.removeCallbacks(audio_limit_runnable);

								}
								play_flag=0;												
							}
						});	

						final AlertDialog dialog = builder.create();
						dialog.show();

						dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);

						start = (ImageView) voice.findViewById(R.id.imgStart);
						stop = (ImageView) voice.findViewById(R.id.imgStop);
						play = (ImageView) voice.findViewById(R.id.imgPlay);
						seek_bar = (SeekBar) voice.findViewById(R.id.seek_bar);
						myChronometer = (Chronometer) voice.findViewById(R.id.chronometer); 
						seek_bar.setVisibility(View.GONE);	

						stop.setEnabled(false);
						play.setEnabled(false);
						filename = AUDIO_FILENAME;
						//filename = "temp_audio.3gp";
						outputFile = Environment.getExternalStorageDirectory().
								getAbsolutePath() + "/" + filename;

						myAudioRecorder = new MediaRecorder();
						myAudioRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
						myAudioRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
						myAudioRecorder.setAudioEncoder(MediaRecorder.OutputFormat.AMR_NB);
						myAudioRecorder.setOutputFile(outputFile);

						start.setOnClickListener(new OnClickListener(){

							@Override
							public void onClick(View arg0) {
								// TODO Auto-generated method stub
								play_flag=1;
								try {
									myChronometer.setBase(SystemClock.elapsedRealtime());
									myChronometer.start();
									myAudioRecorder.prepare();
									myAudioRecorder.start();
									//voice_url = ServiceHandler.fileUrl + filename;

								} catch (IllegalStateException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								} catch (IOException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
								start.setImageResource(R.drawable.ic_action_start);
								start.setEnabled(false);
								stop.setEnabled(true);
								stop.setImageResource(R.drawable.stop_audio);

								//Handler handler = new Handler();
								//handler.removeCallbacks(myRunnable);
								
								audio_limit_runnable = new Runnable()
								{
									@Override
									public void run()
									{
										if(play_flag == 1){
											Toast.makeText(getApplicationContext(), "Limit Exceeded For Recording", Toast.LENGTH_LONG).show();
											dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
											myChronometer.stop();
											myAudioRecorder.stop();
											myAudioRecorder.release();
											myAudioRecorder = null;
											stop.setImageResource(R.drawable.ic_action_stop);
											stop.setEnabled(false);
											play.setEnabled(true);
											play.setImageResource(R.drawable.play_audio);
											play_flag=0;
										}
									}
								};								
								audio_limit_handler.postDelayed(audio_limit_runnable, SEC * MILLISEC );								
							}
						});

						stop.setOnClickListener(new OnClickListener(){
							@Override
							public void onClick(View arg0) {
								// TODO Auto-generated method stub
								dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
								//dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setEnabled(true);
								myChronometer.stop();
								myAudioRecorder.stop();
								myAudioRecorder.release();
								myAudioRecorder  = null;
								stop.setImageResource(R.drawable.ic_action_stop);
								stop.setEnabled(false);
								play.setEnabled(true);
								play.setImageResource(R.drawable.play_audio);
								play_flag=0;
							}
						});

						play.setOnClickListener(new OnClickListener(){

							@Override
							public void onClick(View view) {
								// TODO Auto-generated method stub
								//final MediaPlayer mp2=new MediaPlayer() ;
								if(isPlaying){
									dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
									dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setEnabled(true);
									play.setImageResource(R.drawable.play_audio);
									isPlaying=false;
									handler.removeCallbacks(runnable);
									mp2.stop();
									mp2.release();	
								}
								else{
									mp2=new MediaPlayer() ;
									seek_bar.setProgress(0);
									dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
									dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setEnabled(false);
									play.setImageResource(R.drawable.ic_action_stop);
									isPlaying=true;

									myChronometer.setVisibility(View.GONE);
									seek_bar.setVisibility(View.VISIBLE);

									outputFile = Environment.getExternalStorageDirectory().
											getAbsolutePath() + "/" + filename;

									try {
										mp2.setDataSource(outputFile);
									} catch (IOException e1) {
										// TODO Auto-generated catch block
										e1.printStackTrace();
									}

									try {
										mp2.prepare();
										mp2.start();
										mp2.setOnCompletionListener(new OnCompletionListener() {
											//When audio is done will change pause to play
											public void onCompletion(MediaPlayer mp) {
												seek_bar.setProgress(0);
												dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
												dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setEnabled(true);
												play.setImageResource(R.drawable.play_audio);
												isPlaying=false;
												handler.removeCallbacks(runnable);
												mp2.stop();
												mp2.release();									
											}
										});
									}
									catch (FileNotFoundException e) {
										// TODO: handle exception
										runOnUiThread(new Runnable() {
											public void run() {
												//Toast.makeText(AddReminderActivity.this, "Audio Not Exists.", Toast.LENGTH_SHORT).show();
											}
										});  

									}catch (IOException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}

									//final Handler handler = new Handler();

									runnable = new Runnable()
									{
										@Override
										public void run()
										{
											int currentPosition = mp2.getCurrentPosition() / 1000;
											int duration = mp2.getDuration() / 1000;
											int progress = (currentPosition * 100) / duration;
											seek_bar.setProgress(progress);
											//TextView txt = (TextView) findViewById(R.id.progress);
											// txt.setText(String.valueOf(progress) + "%");
											handler.postDelayed(this, 1000);
										}
									};
									handler.postDelayed(runnable, 1000);
								}
							}
						});		
					}

					if(recorded == 1){
						LayoutInflater li = LayoutInflater.from(AddReminderActivity.this);
						LinearLayout voice = (LinearLayout)li.inflate(R.layout.confirm_audio, null);				
						AlertDialog.Builder builder2 = new AlertDialog.Builder(AddReminderActivity.this);
						builder2.setTitle("Confirm");
						builder2.setView(voice);
						builder2.setCancelable(false);


						Button existingAudio = (Button) voice.findViewById(R.id.extAudio);
						Button newAudio = (Button) voice.findViewById(R.id.newAudio);
						builder2.setNegativeButton("Cancel",
								new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int whichButton) {
							}
						});	

						builder2.create().show();

						existingAudio.setOnClickListener(new OnClickListener() {
							@Override
							public void onClick(View v) {
								// TODO Auto-generated method stub

								LayoutInflater li = LayoutInflater.from(AddReminderActivity.this);
								LinearLayout voice = (LinearLayout)li.inflate(R.layout.activity_record_audio, null);				
								AlertDialog.Builder builder = new AlertDialog.Builder(AddReminderActivity.this);
								builder.setTitle("Recorded Audio");
								builder.setView(voice);
								builder.setCancelable(false);

								start = (ImageView) voice.findViewById(R.id.imgStart);
								stop = (ImageView) voice.findViewById(R.id.imgStop);
								play = (ImageView) voice.findViewById(R.id.imgPlay);
								seek_bar = (SeekBar) voice.findViewById(R.id.seek_bar);
								myChronometer = (Chronometer) voice.findViewById(R.id.chronometer); 
								myChronometer.setVisibility(View.GONE);
								start.setVisibility(View.GONE);
								stop.setVisibility(View.GONE);
								play.setImageResource(R.drawable.play_audio);


								builder.setNegativeButton("Cancel",
										new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog, int whichButton) {
									}
								});	

								final AlertDialog dialog = builder.create();
								dialog.show();
								//builder.create().show();

								play.setOnClickListener(new OnClickListener(){

									@Override
									public void onClick(View view) {
										// TODO Auto-generated method stub
										if(isPlaying){
											dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setEnabled(true);
											play.setImageResource(R.drawable.play_audio);
											isPlaying=false;
											handler.removeCallbacks(runnable);
											mp2.stop();
											mp2.release();	
										}
										else{
											mp2=new MediaPlayer() ;
											seek_bar.setProgress(0);
											dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setEnabled(false);
											play.setImageResource(R.drawable.ic_action_stop);
											isPlaying=true;
											dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setEnabled(false);
											seek_bar.setVisibility(View.VISIBLE);

											//final MediaPlayer mp2=new MediaPlayer() ;

											outputFile = Environment.getExternalStorageDirectory().
													getAbsolutePath() + "/" + filename;


											try {
												mp2.setDataSource(outputFile);
											} catch (IOException e1) {
												// TODO Auto-generated catch block
												e1.printStackTrace();
											}


											try {
												mp2.prepare();
												mp2.start();
												mp2.setOnCompletionListener(new OnCompletionListener() {
													//When audio is done will change pause to play
													public void onCompletion(MediaPlayer mp) {
														seek_bar.setProgress(0);
														dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setEnabled(true);
														play.setImageResource(R.drawable.play_audio);
														isPlaying=false;
														handler.removeCallbacks(runnable);
														mp2.stop();
														mp2.release();									
													}
												});
											}
											catch (FileNotFoundException e) {
												// TODO: handle exception
												runOnUiThread(new Runnable() {
													public void run() {
														//Toast.makeText(AddReminderActivity.this, "Audio Not Exists.", Toast.LENGTH_SHORT).show();
													}
												});  

											}catch (IOException e) {
												// TODO Auto-generated catch block
												e.printStackTrace();
											}

											//final Handler handler = new Handler();
											runnable = new Runnable()
											{
												@Override
												public void run()
												{
													int currentPosition = mp2.getCurrentPosition() / 1000;
													int duration = mp2.getDuration() / 1000;
													int progress = (currentPosition * 100) / duration;
													seek_bar.setProgress(progress);

													//TextView txt = (TextView) findViewById(R.id.progress);
													// txt.setText(String.valueOf(progress) + "%");
													handler.postDelayed(this, 1000);
												}
											};
											handler.postDelayed(runnable, 1000);
										}
									}
								});
							}
						});

						newAudio.setOnClickListener(new OnClickListener() {					

							@Override
							public void onClick(View v) {
								// TODO Auto-generated method stub

								LayoutInflater li = LayoutInflater.from(AddReminderActivity.this);
								LinearLayout voice = (LinearLayout)li.inflate(R.layout.activity_record_audio, null);				
								AlertDialog.Builder builder = new AlertDialog.Builder(AddReminderActivity.this);
								builder.setTitle("Recorder");
								builder.setView(voice);
								builder.setCancelable(false);


								builder.setPositiveButton("Done Recording",
										new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog, int whichButton) {
										micButton.setBackgroundResource(R.drawable.circle_background);
										recorded = 1;
										play_flag = 0;
										audio_limit_handler.removeCallbacks(audio_limit_runnable);
										voice_url = ServiceHandler.fileUrl + filename;
									}
								});	
								builder.setNegativeButton("Cancel",
										new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog, int whichButton) {
										if(play_flag == 1){
											myChronometer.stop();
											myAudioRecorder.stop();
											myAudioRecorder.release();
											myAudioRecorder  = null;
											audio_limit_handler.removeCallbacks(audio_limit_runnable);
										}
										play_flag=0;
									}
								});		

								final AlertDialog dialog = builder.create();
								dialog.show();

								dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false); 
								//dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setEnabled(false); 


								start = (ImageView) voice.findViewById(R.id.imgStart);
								stop = (ImageView) voice.findViewById(R.id.imgStop);
								play = (ImageView) voice.findViewById(R.id.imgPlay);
								seek_bar = (SeekBar) voice.findViewById(R.id.seek_bar);
								seek_bar.setVisibility(View.GONE);
								myChronometer = (Chronometer) voice.findViewById(R.id.chronometer); 	


								stop.setEnabled(false);
								play.setEnabled(false);
								filename = AUDIO_FILENAME;
								outputFile = Environment.getExternalStorageDirectory().
										getAbsolutePath() + "/" + filename;

								myAudioRecorder = new MediaRecorder();
								myAudioRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
								myAudioRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
								myAudioRecorder.setAudioEncoder(MediaRecorder.OutputFormat.AMR_NB);
								myAudioRecorder.setOutputFile(outputFile);

								start.setOnClickListener(new OnClickListener(){

									@Override
									public void onClick(View arg0) {
										// TODO Auto-generated method stub
										play_flag=1;
										try {
											myChronometer.setBase(SystemClock.elapsedRealtime());
											myChronometer.start();
											myAudioRecorder.prepare();
											myAudioRecorder.start();
											//voice_url="audio_upload";					        


										} catch (IllegalStateException e) {
											// TODO Auto-generated catch block
											e.printStackTrace();
										} catch (IOException e) {
											// TODO Auto-generated catch block
											e.printStackTrace();
										}
										start.setImageResource(R.drawable.ic_action_start);
										start.setEnabled(false);
										stop.setEnabled(true);
										stop.setImageResource(R.drawable.stop_audio);

										audio_limit_runnable = new Runnable()
										{
											@Override
											public void run()
											{
												if(play_flag == 1){
													Toast.makeText(getApplicationContext(), "Limit Exceeded For Recording", Toast.LENGTH_LONG).show();
													dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
													myChronometer.stop();
													myAudioRecorder.stop();
													myAudioRecorder.release();
													myAudioRecorder = null;
													stop.setImageResource(R.drawable.ic_action_stop);
													stop.setEnabled(false);
													play.setEnabled(true);
													play.setImageResource(R.drawable.play_audio);
													play_flag=0;
												}
											}
										};								
										audio_limit_handler.postDelayed(audio_limit_runnable, SEC * MILLISEC );
									}

								});

								stop.setOnClickListener(new OnClickListener(){

									@Override
									public void onClick(View arg0) {
										// TODO Auto-generated method stub
										dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true); 
										//dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setEnabled(true); 

										myChronometer.stop();
										myAudioRecorder.stop();
										myAudioRecorder.release();
										myAudioRecorder  = null;

										stop.setImageResource(R.drawable.ic_action_stop);
										stop.setEnabled(false);
										play.setEnabled(true);
										play.setImageResource(R.drawable.play_audio);
										play_flag=0;
									}
								});
								play.setOnClickListener(new OnClickListener(){

									@Override
									public void onClick(View view) {
										// TODO Auto-generated method stub

										if(isPlaying){
											dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
											dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setEnabled(true);
											play.setImageResource(R.drawable.play_audio);
											isPlaying=false;
											handler.removeCallbacks(runnable);
											mp2.stop();
											mp2.release();	
										}
										else{
											mp2=new MediaPlayer() ;
											seek_bar.setProgress(0);
											dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
											dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setEnabled(false);
											play.setImageResource(R.drawable.ic_action_stop);
											isPlaying=true;

											dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
											dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setEnabled(false);
											myChronometer.setVisibility(View.GONE);
											seek_bar.setVisibility(View.VISIBLE);

											//final MediaPlayer mp2=new MediaPlayer();
											outputFile = Environment.getExternalStorageDirectory().
													getAbsolutePath() + "/" + filename;

											try {
												mp2.setDataSource(outputFile);
											} catch (IOException e1) {
												// TODO Auto-generated catch block
												e1.printStackTrace();
											}

											try {
												mp2.prepare();
												mp2.start();
												mp2.setOnCompletionListener(new OnCompletionListener() {
													//When audio is done will change pause to play
													public void onCompletion(MediaPlayer mp) {
														seek_bar.setProgress(0);
														dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
														dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setEnabled(true);
														play.setImageResource(R.drawable.play_audio);
														isPlaying=false;
														handler.removeCallbacks(runnable);
														mp2.stop();
														mp2.release();									
													}
												});
											}
											catch (FileNotFoundException e) {
												// TODO: handle exception
												runOnUiThread(new Runnable() {
													public void run() {
														//Toast.makeText(AddReminderActivity.this, "Audio Not Exists.", Toast.LENGTH_SHORT).show();
													}
												});  

											}catch (IOException e) {
												// TODO Auto-generated catch block
												e.printStackTrace();
											}

											//final Handler handler = new Handler();

											runnable = new Runnable()
											{
												@Override
												public void run()
												{
													int currentPosition = mp2.getCurrentPosition() / 1000;
													int duration = mp2.getDuration() / 1000;
													int progress = (currentPosition * 100) / duration;
													seek_bar.setProgress(progress);
													//TextView txt = (TextView) findViewById(R.id.progress);
													// txt.setText(String.valueOf(progress) + "%");
													handler.postDelayed(this, 1000);
												}
											};
											handler.postDelayed(runnable, 1000);
										}
									}
								});		
							}
						});
					}				
				}
			}
		});


		contactLayout.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View view) {
				// TODO Auto-generated method stub
				Intent intent=new Intent(AddReminderActivity.this,DisplayContactActivity.class);
				startActivityForResult(intent,1);
			}
		});

		cancelButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				finish();
			}
		});

		cancelBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				finish();
			}
		});
	}



	public void shareReminder(){		

		if(skipFlag){
			//Toast.makeText(mContext, "Register To Send Reminder", Toast.LENGTH_SHORT).show();
			showAlertDialog(AddReminderActivity.this,
					"Registration Required",
					"Reminder Will Be Set To Yourself But Please Register To Send Reminders To Your Friends.", false);
		}
		else{
			int selectedId = rbGroup.getCheckedRadioButtonId();
			RadioButton rButton = (RadioButton) findViewById(selectedId);

			if(rButton.getText().equals("Friend's Time")){
				isMine="1";
				Calendar timeCal=Calendar.getInstance();
				timeCal.setTimeInMillis(reminderTime);

				hr = String.valueOf(timeCal.get(Calendar.HOUR_OF_DAY));
				min = String.valueOf(timeCal.get(Calendar.MINUTE));
			}
			else{
				isMine="0";
				hr="none";
				min="none";
			}

			if (filename.equals("")){
				filename="TEXT";
			}
			reminderID=sendername+"_"+String.valueOf(System.currentTimeMillis());

			receivername = ch.getText().toString();

			reminderTitle=reminderTitleEdit.getText().toString();

			//retrieve date value
			String dateTextValue=dateText.getText().toString();

			if(reminderTitle.equals("")|| reminderTitle.equals(null)){

				showToast("Please Fill Title");
			}

			else if(receivername.equals("")|| receivername.equals(null)){
				showToast("Please select one contact");
			}

			else if(!checkDateTimeValidation(reminderDate, reminderTime)){
				showToast("Please select valid date and time");
			}
			else{			

				if(contact_flag == 1){
					List <String> nol = new ArrayList<String>();
					List<Group> mList=new GroupsLocalDB(getApplicationContext()).getGroupMembers(gid);
					for (Group g : mList) {
						nol.add(convertMobilePattern(g.getGroupMember()));
					}					
					receivername = combinedNumberString(nol);
				}
				else{
					receivername = receiverno;							
				}

				if(cDetector.isConnectingToInternet())
				{
					if (voice_url!="TEXT"){
						new Thread(new Runnable() {
							public void run() {
								int r=UploadAudio(outputFile);
							}
						}).start();
					}

					if(receivername.indexOf(":")==-1){
						if(new ContactLocalDB(getApplicationContext()).checkIfBlocked(receivername)){
							showAlertDialog(AddReminderActivity.this,
									"Action Denied",
									"Recipient Has Blocked You", false);
						}
						else{
							if(new ContactLocalDB(getApplicationContext()).checkIfContactExists(receivername)){
								new AddReminder().execute();
							}
							else{
								number= code + receivername;
								//sendMessage(number);
								showAlertDialog(AddReminderActivity.this,
										"App User Required",
										"A Charged SMS Will Be Send To The Recipient..", false);
							}
						}
					}
					else{
						new AddReminder().execute();
					}							
				}
				else{
					showAlertDialog(AddReminderActivity.this,
							"No Internet Connection !",
							"Reminder Will Be Set To Yourself Only.", false);							
				}
			}
		}
	}


	private boolean copyFileFromOneDirectoryToAnother(File source,File destination){
		InputStream inStream = null;
		OutputStream outStream = null;

		try{

			System.out.println("source" + source);
			System.out.println("desti" + destination);



			inStream = new FileInputStream(source);
			outStream = new FileOutputStream(destination);

			byte[] buffer = new byte[1024];

			int length;
			//copy the file content in bytes 
			while ((length = inStream.read(buffer)) > 0){

				outStream.write(buffer, 0, length);

			}

			inStream.close();
			outStream.close();


			System.out.println("Audio File is copied successful!");

		}catch(IOException e){
			e.printStackTrace();
			return false;
		}
		catch(Exception e){
			e.printStackTrace();
			return false;
		}
		return true;
	}


	private void deleteFileFromSdCard(String path){
		File file= new File(path);
		if(file.exists())
		{
			file.delete();
		}
	}

	public void ReminderToMe(){
		String sender=sharedPref.getString("USERNAME", "");;
		String rTitle = reminderTitle;
		long rDate = reminderDate;
		long rTime = reminderTime;
		long rDelay = reminderDelay;
		long rRecur = recurringAlarm;
		String location = textview.getText().toString();
		String rtime_receiving = String.valueOf(System.currentTimeMillis());							


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
		combCal.set(Year, monthOfYear, dayOfMonth, hr, min, 0);
		combCal.getTimeInMillis();

		//Toast.makeText(getApplicationContext(), "TIME:: "+changeTimePattern(combCal.getTimeInMillis()), Toast.LENGTH_SHORT).show();

		Random rand = new Random();

		// nextInt is normally exclusive of the top value,
		// so add 1 to make it inclusive
		int randomNum = rand.nextInt((900 - 100) + 100);
		long tstamp = System.currentTimeMillis() % 100000 ;
		String tmp = String.valueOf(tstamp) + String.valueOf(randomNum);
		int reqCode = Integer.parseInt(tmp);


		//Create a new PendingIntent and add it to the AlarmManager
		Intent intent = new Intent(AddReminderActivity.this, ReminderScreen.class);
		intent.putExtra("sender", "Me");
		intent.putExtra("rTitle", reminderTitle);
		intent.putExtra("rem_id", reminderID);
		intent.putExtra("voicefile", "TEXT");
		intent.putExtra("location", location);
		intent.putExtra("gid", "0");

		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		pendingIntent = PendingIntent.getActivity(AddReminderActivity.this,
				reqCode, intent, PendingIntent.FLAG_CANCEL_CURRENT);

		am.setRepeating(AlarmManager.RTC_WAKEUP, combCal.getTimeInMillis()-rDelay,rRecur,
				pendingIntent);

		new ReminderLocalDB(getApplicationContext()).insertReminderLocal(sender, sender, gid, rTitle, reminderID, rDate, rTime,
				"", rDelay, rRecur, 1, location, reqCode, rtime_receiving, voice_url, "IN", "NONE", "0");

		new ReminderLocalDB(getApplicationContext()).insertRecentContacts(sender, rDate, rtime_receiving);
		finish();
	}

	public void showAlertDialog(Context context, final String title, String message,
			Boolean status) {

		AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);

		// Setting Dialog Title
		alertDialog.setTitle(title);

		// Setting Dialog Message
		alertDialog.setMessage(message);

		if(status != null)
			// Setting alert dialog icon
			alertDialog.setIcon((status) ? R.drawable.success : R.drawable.fail);

		// Setting OK Button
		alertDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				if(skipFlag || title.equals("No Internet Connection !")){
					ReminderToMe();
				}
				else if(title.equals("App User Required")){
					//Toast.makeText(getApplicationContext(), "Sms user", Toast.LENGTH_SHORT).show();

					//sendMessage(number);
					sendMessage(receivername);
				}
				//finish();
				reminderTitleEdit.setText("");
			}
		});
		if(title.equals("App User Required")){
			alertDialog.setNegativeButton("Cancel",
					new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {

				}
			});	
		}
		// Showing Alert Message
		alertDialog.create().show();
	}


	private boolean checkDateTimeValidation(long ddate,long dtime){

		//retrieve date 

		Calendar da=Calendar.getInstance();
		da.setTimeInMillis(ddate);
		int lDay = da.get(Calendar.DAY_OF_MONTH);
		int lMonth=da.get(Calendar.MONTH);
		int lYear=da.get(Calendar.YEAR);

		Calendar ta=Calendar.getInstance();
		ta.setTimeInMillis(dtime);
		int lHour=ta.get(Calendar.HOUR_OF_DAY);
		int lMins=ta.get(Calendar.MINUTE);


		Calendar ca=Calendar.getInstance();
		ca.set(lYear, lMonth, lDay, lHour, lMins);

		if(ca.getTimeInMillis()>=System.currentTimeMillis())
			return true;

		return false;
	}
	
	
	private String showDateTime(long ddate,long dtime){

		//retrieve date 
		Calendar da=Calendar.getInstance();
		da.setTimeInMillis(ddate);
		int lDay = da.get(Calendar.DAY_OF_MONTH);
		int lMonth=da.get(Calendar.MONTH);
		int lYear=da.get(Calendar.YEAR);

		Calendar ta=Calendar.getInstance();
		ta.setTimeInMillis(dtime);
		int lHour=ta.get(Calendar.HOUR_OF_DAY);
		int lMins=ta.get(Calendar.MINUTE);



		Calendar ca =Calendar.getInstance(); 
		ca.set(lYear, lMonth, lDay, lHour, lMins);
		String convertedDate=null;
		Date datett=new Date(ca.getTimeInMillis());
		DateFormat sf = new SimpleDateFormat("dd::MMM::yyyy --hh:mm a");
		convertedDate=sf.format(datett);
		return convertedDate;
	}
	
	
	private void showToast(String message){
		Toast t=Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT);
		t.setGravity(Gravity.CENTER, 0, 0);
		t.show();
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		/*if(!cDetector.isConnectingToInternet() || skipFlag){
			recurringSpin.setVisibility(View.GONE);
			recurrtv.setVisibility(View.GONE);
		}
		else{
			recurringSpin.setVisibility(View.VISIBLE);
			recurrtv.setVisibility(View.VISIBLE);
		}*/
	}

	private String changeTimePattern(long timestamp){
		String convertedDate=null;
		Date date=new Date(timestamp);
		DateFormat sf = new SimpleDateFormat("dd::MMM::yyyy --hh:mm a");
		convertedDate=sf.format(date);
		return convertedDate;
	}

	private long createTimeStamp(int hour){

		Calendar calen=Calendar.getInstance();
		calen.set(mYear, mMonth, mDay, hour, 0);
		return calen.getTimeInMillis();
	}

	private long reminderTimePattern(String timeTextValue){
		//String timeTextValue=timeText.getText().toString();
		if(timeTextValue.equals("Morning 9:00 am"))
			return createTimeStamp(timeMap.get(timeTextValue));

		else if(timeTextValue.equals("Afternoon 12:00 pm"))
			return createTimeStamp(timeMap.get(timeTextValue));

		else if(timeTextValue.equals("Evening 3:00 pm"))
			return createTimeStamp(timeMap.get(timeTextValue));

		else if(timeTextValue.equals("Night 6:00 pm"))
			return createTimeStamp(timeMap.get(timeTextValue));

		return 0;
	}
	private long reminderDatePattern(String dateTextValue){
		//String dateTextValue=dateText.getText().toString();

		if(dateTextValue.equals("Today"))
			return dateMap.get(dateTextValue);

		else if(dateTextValue.equals("Tomorrow"))
			return dateMap.get(dateTextValue);

		return 0;
	}
	private void loadCalendarTime(){
		timeMap=new HashMap<String, Integer>();
		timeMap.put("Morning 9:00 am", 9);
		timeMap.put("Afternoon 12:00 pm", 12);
		timeMap.put("Evening 3:00 pm", 15);
		timeMap.put("Night 6:00 pm", 18);
	}
	private void loadCalendarDate(){
		dateMap=new HashMap<String, Long>();
		dateMap.put("Today", System.currentTimeMillis());
		dateMap.put("Tomorrow", System.currentTimeMillis() + 24 * 60 *60 * 1000);
	}
	private void loadRecurring(){
		recurringMap=new HashMap<String, Long>();
		recurringMap.put("Once", 0L);
		recurringMap.put("5 min",  5 * 60 * 1000L);
		recurringMap.put("10 min", 10 * 60 * 1000L);
		recurringMap.put("15 min", 15 * 60 * 1000L);
		recurringMap.put("30 min", 30 * 60 * 1000L);
		recurringMap.put("Every Hour", 1 * 60 * 60 * 1000L);
		recurringMap.put("Every Day", 24 * 60 * 60 * 1000L);
		recurringMap.put("Every Week", 7 * 24 * 60 * 60 * 1000L);
		recurringMap.put("Every Month", 30 * 24 * 60 * 60 * 1000L);
	}
	
	
	private void loadDelay(){
		delayMap=new HashMap<String, Long>();
		delayMap.put("On Time",0L);
		delayMap.put("5 min",  5 * 60 * 1000L);
		delayMap.put("10 min", 10 * 60 * 1000L);
		delayMap.put("15 min", 15 * 60 * 1000L);
		delayMap.put("30 min", 30 * 60 * 1000L);
		delayMap.put("1 hour", 1 * 60 * 60 * 1000L);
	}


	private void loadDefaultCalendar() {

		Calendar calendar = Calendar.getInstance();
		mYear = calendar.get(Calendar.YEAR);
		mMonth = calendar.get(Calendar.MONTH);
		mDay = calendar.get(Calendar.DAY_OF_MONTH);
		cHour= mHour=calendar.get(Calendar.HOUR_OF_DAY);
		cMinute =mMinute=calendar.get(Calendar.MINUTE);
	}


	public void doLaunchContactPicker(View view) {
		Intent contactPickerIntent = new Intent(Intent.ACTION_PICK,
				Phone.CONTENT_URI);
		///contactPickerIntent.setType(Phone.CONTENT_TYPE);
		startActivityForResult(contactPickerIntent, CONTACT_PICKER_RESULT);
	}

	public  String convertMobilePattern(String number)
	{    
		/*String out = number.replaceAll("[^0-9\\+]", "")        //remove all the non numbers (brackets dashes spaces etc.) except the + signs
				.replaceAll("(^[1-9].+)", "$1")         //if the number is starting with no zero and +, its a local number. prepend cc
				.replaceAll("(.)(\\++)(.)", "$1$3");        //if there are left out +'s in the middle by mistake, remove them
				//.replaceAll("(^0{2}|^\\+)(.+)", "$2")       //make 00XXX... numbers and +XXXXX.. numbers into XXXX...
				//.replaceAll("^0([1-9])", "$1");
		//out=out.substring(out.length() - 10);
		//make 0XXXXXXX numbers into CCXXXXXXXX numbers
		return out;*/
		String converted = "";
		PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();

		try {
			TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
			String countryCode = tm.getSimCountryIso().toUpperCase();
			// phone must begin with '+'
			PhoneNumber numberProto = phoneUtil.parse(number, countryCode);
			
			converted = phoneUtil.format(numberProto, PhoneNumberFormat.E164);
			Log.e("converted number", converted);
		} catch (NumberParseException e) {
			System.err.println("NumberParseException was thrown: "
					+ e.toString());
		}
		return converted;
	}

	private void sendMessage(String mobileNumber){
		//Toast.makeText(getApplicationContext(), "Sms user", Toast.LENGTH_SHORT).show();
		String message=sendername+" send you reminder. To download this app, click here :"+ServiceHandler.NON_USER_MESSAGE;
		SmsManager smsManager = SmsManager.getDefault();
		smsManager.sendTextMessage(mobileNumber, null, message, null, null);
	}


	//insert data into database
	private class AddReminder extends AsyncTask<String, Void, Void> {

		boolean isNewCategoryCreated = false,err=false;
		@Override
		protected void onPreExecute() {
			super.onPreExecute();

			pDialog = new ProgressDialog(AddReminderActivity.this);
			pDialog.setTitle("Reminder Sending");
			pDialog.setMessage("Please Wait...");
			pDialog.setCancelable(false);
			pDialog.show();

			Handler handler = new Handler();
			handler.postDelayed(new Runnable() {
				public void run() {
					pDialog.dismiss();
					finish();
				}}, 2000);
		}

		@Override
		protected Void doInBackground(String... arg) {
			ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("sender_no", sendername));
			params.add(new BasicNameValuePair("receiver", receivername));
			params.add(new BasicNameValuePair("reminder_title",reminderTitle));
			params.add(new BasicNameValuePair("reminder_date", String.valueOf(reminderDate)));
			params.add(new BasicNameValuePair("reminder_time",String.valueOf(reminderTime)));
			params.add(new BasicNameValuePair("isMine", isMine));
			params.add(new BasicNameValuePair("hour", hr));
			params.add(new BasicNameValuePair("minute", min));
			params.add(new BasicNameValuePair("delay", String.valueOf(reminderDelay)));
			params.add(new BasicNameValuePair("recurring",String.valueOf(recurringAlarm)));
			params.add(new BasicNameValuePair("location", textview.getText().toString()));
			params.add(new BasicNameValuePair("voice_url", voice_url));
			params.add(new BasicNameValuePair("group_id", gid));
			params.add(new BasicNameValuePair("reminder_accept", "0"));

			ServiceHandler serviceClient = new ServiceHandler();
			try{
				String json = serviceClient.makeServiceCall(ServiceHandler.URL_ADD_REMINDER2,
						ServiceHandler.GET, params);
				System.out.println("Insert Reminder Response is="+json);
				if(!json.equals("error")){
					JSONObject obj=new JSONObject(json);
					response=obj.getString("succ");
					reminderID=obj.getString("reminder_id");
					Log.d("Create Reminder Response: ", "> " + json);
				}
				else
					err=true;
			}
			catch(Exception e){
				e.printStackTrace();
				pDialog.dismiss();
				//Toast.makeText(mContext, "Something wrong with network, Please try later", Toast.LENGTH_SHORT).show();
				err=true;
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			//if (pDialog.isShowing()){
			//pDialog.dismiss();

			if(!err){
				if(response.equals("1")){
					if(gid.equals("0")){
						String location = textview.getText().toString();

						if(!sendername.equals(receivername)){
							new ReminderLocalDB(getApplicationContext()).insertReminderLocal(sendername, receivername, gid, reminderTitle, reminderID, reminderDate,
									reminderTime,"", reminderDelay, recurringAlarm, 2,location, 0, "", filename, "OUT", "NONE", "0");
						}
						RecentContacts();
						if(!voice_url.equals("TEXT")){
							String state = Environment.getExternalStorageState();
							if (Environment.MEDIA_MOUNTED.equals(state)) {
								mFileTemp = new File(outputFile);
							}
							else {
								mFileTemp = new File(getFilesDir(), outputFile);
							}
							File audioFile = new File(
									Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator+AUDIO_DIRECTORY_NAME + File.separator + reminderID + ".3gp" );

							if(copyFileFromOneDirectoryToAnother(mFileTemp,audioFile)){
								deleteFileFromSdCard(outputFile);
							}
						}
					}
				}				
			}		
		}		
	}

	public void RecentContacts(){
		String rtime_sending = String.valueOf(System.currentTimeMillis());
		if (!gid.equals("0")){
			receivername=gid;					
		}
		new ReminderLocalDB(getApplicationContext()).insertRecentContacts(receivername, reminderDate, rtime_sending);
	}

	


	class GetPlaces extends AsyncTask<String, Void, ArrayList<String>>
	{

		@Override
		// three dots is java for an array of strings
		protected ArrayList<String> doInBackground(String... args)
		{

			Log.d("gottaGo", "doInBackground");
			String s = args[0];

			ArrayList<String> predictionsArr = new ArrayList<String>();

			try
			{
				URL googlePlaces = new URL(
						// URLEncoder.encode(url,"UTF-8");
						"https://maps.googleapis.com/maps/api/place/autocomplete/json?input="+
								URLEncoder.encode(s.toString(), "UTF-8") +"&types=geocode&language=en&sensor=true&key=" + ServiceHandler.BROWSER_API_KEY);
				URLConnection tc = googlePlaces.openConnection();
				BufferedReader in = new BufferedReader(new InputStreamReader(
						tc.getInputStream()));

				String line;
				StringBuffer sb = new StringBuffer();
				//take Google's legible JSON and turn it into one big string.
				while ((line = in.readLine()) != null) {
					sb.append(line);
				}
				//turn that string into a JSON object
				JSONObject predictions = new JSONObject(sb.toString());
				//now get the JSON array that's inside that object           
				JSONArray ja = new JSONArray(predictions.getString("predictions"));

				for (int i = 0; i < ja.length(); i++) {
					JSONObject jo = (JSONObject) ja.get(i);
					//add each entry to our array
					predictionsArr.add(jo.getString("description"));
				}
			} catch (IOException e)
			{

				Log.e("YourApp", "GetPlaces : doInBackground", e);

			} catch (JSONException e)
			{

				Log.e("YourApp", "GetPlaces : doInBackground", e);

			}

			return predictionsArr;

		}

		//then our post

		@Override
		protected void onPostExecute(ArrayList<String> result)
		{

			Log.d("YourApp", "onPostExecute : " + result.size());
			//update the adapter
			adapter = new ArrayAdapter<String>(getBaseContext(), R.layout.list_item);
			adapter.setNotifyOnChange(true);
			//attach the adapter to textview
			textview.setAdapter(adapter);

			longi=0;
			lat=0;

			String location_name = textview.getText().toString();		

			for (String string : result)



			{
				Log.d("YourApp", "onPostExecute : result = " + string);
				adapter.add(string);
				adapter.notifyDataSetChanged();
			}

			Log.d("YourApp", "onPostExecute : autoCompleteAdapter" + adapter.getCount());
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

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);

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


		if(requestCode==1){
			if(resultCode==RESULT_OK){
				//contactLayout.setOnClickListener(null);
				contactLayout.setEnabled(false);
				imgContact.setVisibility(View.VISIBLE);
				remove_contact.setVisibility(View.VISIBLE);

				selectedContact=new ArrayList<Contact>();
				selectedContact.clear();
				selectedContact=(ArrayList<Contact>) data.getSerializableExtra("LIST");
				//updateUI();
				List <String> nl = new ArrayList<String>();
				List <String> nol = new ArrayList<String>();
				//List <String> gl = new ArrayList<String>();

				String ph="",contName="";
				for(int i =0;i<selectedContact.size();i++){
					//Toast.makeText(this,selectedContact.get(i).getName(), Toast.LENGTH_LONG).show();
					nl.add(selectedContact.get(i).getName());
					nol.add(selectedContact.get(i).getPhoneNo());
					contName = selectedContact.get(i).getName();
					ph =selectedContact.get(i).getPhoneNo();
					gid =selectedContact.get(i).getOriginalPhoneNumber();
					//Toast.makeText(this, gid,Toast.LENGTH_LONG).show();					
				}
				ch.setText(contName);

				//ch.setText(combinedString(nl));
				//ch.setChips();
				if(ph.equals("")){
					contact_flag = 1;	
					imageLoader.displayImage(new GroupsLocalDB(mContext).getGroupPicUrl(gid), imgContact, options, animateFirstListener);
				}
				else{
					receiverno = combinedNumberString(nol);
					gid="0";
					imageLoader.displayImage(new ContactLocalDB(mContext).getContactPicUrl(ph), imgContact, options, animateFirstListener);
				}
			}

			if(resultCode==RESULT_CANCELED){

			} 
		}
	}
	public String combinedString(List<String> listUpdate){

		//int size=listUpdate.size();
		//int i=0;
		String ss="";
		Iterator itr=listUpdate.iterator();
		while(itr.hasNext())
		{
			String s=(String)itr.next();
			ss=ss.concat(s+",");
		}
		System.out.println("SSSSSS"+ss);
		return ss;
	}


	public String combinedNumberString(List<String> listUpdate){

		int size=listUpdate.size();
		int i=0;
		String ss="";
		Iterator itr=listUpdate.iterator();
		while(itr.hasNext())
		{
			if(i<size-1){ 	  
				String s=(String)itr.next();
				ss=ss.concat(s+":");
				i++;
			}
			else{
				String s=(String)itr.next();
				ss=ss.concat(s);
			}

		}
		System.out.println("SSSSSS"+ss);
		return ss;
	}


	public String getPhoneNumber(String name, Context context) {
		String ret = null;
		String selection = ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME+" = '" + name +"'";
		String[] projection = new String[] { ContactsContract.CommonDataKinds.Phone.NUMBER};
		Cursor c = context.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
				projection, selection, null, null);
		if (c.moveToFirst()) {
			ret = c.getString(0);
		}
		c.close();
		if(ret==null)
			ret = "Unsaved";
		return ret;
	}

	public int UploadAudio(String sourceFileUri) {


		String fileName = sourceFileUri;

		HttpURLConnection conn = null;
		DataOutputStream dos = null;  
		String lineEnd = "\r\n";
		String twoHyphens = "--";
		String boundary = "*****";
		int bytesRead, bytesAvailable, bufferSize;
		byte[] buffer;
		int maxBufferSize = 1 * 1024 * 1024; 
		File sourceFile = new File(sourceFileUri); 

		if (!sourceFile.isFile()) {

			//dialog.dismiss(); 

			Log.e("uploadFile", "Source File not exist :"+imagepath);

			runOnUiThread(new Runnable() {
				public void run() {
					//messageText.setText("Source File not exist :"+ imagepath);
				}
			});
			return 0;
		}
		else
		{
			try {
				// open a URL connection to the Servlet
				FileInputStream fileInputStream = new FileInputStream(sourceFile);
				URL url = new URL(ServiceHandler.upLoadServerUri);

				// Open a HTTP  connection to  the URL
				conn = (HttpURLConnection) url.openConnection(); 
				conn.setDoInput(true); // Allow Inputs
				conn.setDoOutput(true); // Allow Outputs
				conn.setUseCaches(false); // Don't use a Cached Copy
				conn.setRequestMethod("POST");
				conn.setRequestProperty("Connection", "Keep-Alive");
				conn.setRequestProperty("ENCTYPE", "multipart/form-data");
				conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
				conn.setRequestProperty("uploaded_file", fileName); 

				dos = new DataOutputStream(conn.getOutputStream());

				dos.writeBytes(twoHyphens + boundary + lineEnd); 
				dos.writeBytes("Content-Disposition: form-data; name=\"uploaded_file\";filename=\""
						+ fileName + "\"" + lineEnd);


				dos.writeBytes(lineEnd);

				// create a buffer of  maximum size
				bytesAvailable = fileInputStream.available(); 

				bufferSize = Math.min(bytesAvailable, maxBufferSize);
				buffer = new byte[bufferSize];

				// read file and write it into form...
				bytesRead = fileInputStream.read(buffer, 0, bufferSize);  

				while (bytesRead > 0) {

					dos.write(buffer, 0, bufferSize);
					bytesAvailable = fileInputStream.available();
					bufferSize = Math.min(bytesAvailable, maxBufferSize);
					bytesRead = fileInputStream.read(buffer, 0, bufferSize);   

				}
				// send multipart form data necesssary after file data...
				dos.writeBytes(lineEnd);
				dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

				// Responses from the server (code and message)
				serverResponseCode = conn.getResponseCode();
				String serverResponseMessage = conn.getResponseMessage();

				Log.i("uploadFile", "HTTP Response is : "
						+ serverResponseMessage + ": " + serverResponseCode);

				if(serverResponseCode == 200){

					runOnUiThread(new Runnable() {
						public void run() {
							String msg = "File Upload Completed.\n\n See uploaded file here : \n\n"
									+" F:/wamp/wamp/www/uploads";
							//messageText.setText(msg);
							//Toast.makeText(AddReminderActivity.this, "File Upload Complete.", Toast.LENGTH_SHORT).show();
						}
					});                
				}    

				//close the streams //
				fileInputStream.close();
				dos.flush();
				dos.close();

			} catch (MalformedURLException ex) {

				//dialog.dismiss();  
				ex.printStackTrace();

				runOnUiThread(new Runnable() {
					public void run() {
						//	messageText.setText("MalformedURLException Exception : check script url.");
						//	Toast.makeText(AddReminderActivity.this, "MalformedURLException", Toast.LENGTH_SHORT).show();
					}
				});

				Log.e("Upload file to server", "error: " + ex.getMessage(), ex);  
			} catch (Exception e) {

				//dialog.dismiss();  
				e.printStackTrace();

				runOnUiThread(new Runnable() {
					public void run() {
						//messageText.setText("Got Exception : see logcat ");
						//Toast.makeText(AddReminderActivity.this, "Got Exception : see logcat ", Toast.LENGTH_SHORT).show();
					}
				});
				Log.e("Upload file to server Exception", "Exception : "  + e.getMessage(), e);  
			}
			//dialog.dismiss();       
			return serverResponseCode; 

		} // End else block 
	}

	public static boolean hasHoneycomb() {
		return Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB;
	}


	@Override
	public void onTimeSet(RadialPickerLayout view, int hourOfDay, int minute) {
		// TODO Auto-generated method stub
		Calendar selectedTime=Calendar.getInstance();
		selectedTime.set(mYear, mMonth, mDay,hourOfDay,minute);
		if(selectedTime!=null){
			Date dd=new Date(selectedTime.getTimeInMillis());
			DateFormat ddf = new SimpleDateFormat("hh:mm a");
			timeText.setText(ddf.format(dd));
			reminderTime=selectedTime.getTimeInMillis();
		} 
	}


	@Override
	public void onDateSet(DatePickerDialog datePickerDialog, int year, int month, int day) {
		// TODO Auto-generated method stub
		Calendar selectedDate=Calendar.getInstance();
		selectedDate.set(year, month, day);
		Date dd=new Date(selectedDate.getTimeInMillis());
		DateFormat ddf = new SimpleDateFormat("dd-MMM-yyyy");
		dateText.setText(ddf.format(dd));
		reminderDate=selectedDate.getTimeInMillis();		
	}
}
