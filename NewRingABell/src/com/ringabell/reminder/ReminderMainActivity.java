package com.ringabell.reminder;

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
import java.util.Map.Entry;
import java.util.Timer;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.ContentObserver;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.LayerDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;

import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.QuickContactBadge;
import android.widget.TextView;
import android.widget.Toast;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.PhoneNumberUtil.PhoneNumberFormat;
import com.google.i18n.phonenumbers.Phonenumber.PhoneNumber;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.nostra13.universalimageloader.utils.DiskCacheUtils;
import com.nostra13.universalimageloader.utils.MemoryCacheUtils;
import com.ringabell.localdb.ContactLocalDB;
import com.ringabell.localdb.GroupsLocalDB;
import com.ringabell.localdb.ReminderLocalDB;
import com.ringabell.localdb.TemplateLocalDB;
import com.ringabell.model.Contact;
import com.ringabell.model.Group;
import com.ringabell.model.Reminder;
import com.ringabell.serverdb.ServiceHandler;
import com.ringabell.service.AppSyncService;
import com.ringabell.service.GCMUpdateService;
import com.ringabell.service.GetReminderService;
import com.ringabell.user.ProfileActivity;
import com.ringabell.user.SettingsActivity;
import com.ringabell.utils.ConnectionDetector;
import com.ringabell.utils.Utils;
import com.share2people.ringabell.AboutUsActivity;
import com.share2people.ringabell.BlockUserActivity;
import com.share2people.ringabell.CalendarScreen;
import com.share2people.ringabell.MyGroupActivity;
import com.share2people.ringabell.R;
import com.share2people.ringabell.RatingActivity;
import com.share2people.ringabell.R.color;
import com.share2people.ringabell.R.drawable;
import com.share2people.ringabell.R.id;
import com.share2people.ringabell.R.layout;
import com.share2people.ringabell.R.menu;
import com.share2people.ringabell.R.string;

public class ReminderMainActivity extends Activity implements OnClickListener{
	//ImageView addImage,backImage;
	public static List<String> existingContactListInPhoneBookWithOnlyNumber;
	private static String phone;
	Button addReminder,existingTemplate;
	TextView noRecordText,dateText;
	Button cancelBtn;
	private ProgressDialog pDialog;
	private static Context mContext;
	private static SharedPreferences sharedPref;
	private ListView listView;
	LayoutInflater inflator;
	CustomAdapter adapter;
	List<Reminder> reminderList;
	ConnectionDetector cDetector;
	private List<Contact> contactList;
	static ArrayList<String> appContactListOnServerForCurrentUserWithAllInfo;
	public static List<Contact> existingContactListInPhoneBookWithAllInfo;
	public static ArrayList<Contact> NonAppcontactList;
	public static ArrayList<Group> AppGroupList;
	public static ArrayList<Contact> AppContactList;

	private List <String> idList;
	private List <String> bnumberList;
	HashMap<String, String> regIdMap;
	HashMap<String, Contact> contactMap;
	String selectedItem="";
	final int DATE_DIALOG_ID = 1;
	Calendar calendar;
	int day,month,year;

	String grp_user = "";
	static SharedPreferences.Editor editor;
	private int mNotificationsCount = 0;
	HashMap<String, ArrayList<Reminder>> reminderMap;
	boolean gcmFlag=false, skipFlag=false, sync_flag=false;
	boolean shouldExecuteOnResume;
	String gpname="";
	private Handler handler=new Handler();
	ContentObserver contentObserver=null;


	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_reminder_main);
		shouldExecuteOnResume = true;

		getActionBar().setTitle(getString(R.string.app_name));
		getActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.reminder_main_action_bar)));

		mContext=(Context) getApplicationContext();
		sharedPref =getSharedPreferences("LAST_LOGIN", Activity.MODE_PRIVATE);
		grp_user = sharedPref.getString("USERNAME", "");
		skipFlag = sharedPref.getBoolean("SKIP", false);
		sync_flag = sharedPref.getBoolean("ONETIME_SYNC", false);


		addReminder=(Button) findViewById(R.id.addImageButton);
		existingTemplate=(Button) findViewById(R.id.existingTemplateButton);
		noRecordText=(TextView) findViewById(R.id.noRecord);
		dateText=(TextView) findViewById(R.id.dateText);

		listView=(ListView) findViewById(R.id.list);
		addReminder.setOnClickListener(this);
		existingTemplate.setOnClickListener(this);
		cDetector=new ConnectionDetector(mContext);

		calendar=Calendar.getInstance();
		day = calendar.get(Calendar.DAY_OF_MONTH);
		month=calendar.get(Calendar.MONTH);
		year=calendar.get(Calendar.YEAR);

		if(new TemplateLocalDB(mContext).countTemplateSize()<=0)
			loadTemplateLocal();

		noRecordText.setVisibility(View.VISIBLE);		
		mContext=(Context) getApplicationContext();

		if(skipFlag){
			threadExistingContacts();
		}
		else{
			if(cDetector.isConnectingToInternet())
			{
				if(!sync_flag){
					GroupThread();
					new GetExistingContact().execute("0");
					//threadExistingContacts();			
				}
				else{
					if(!isServiceRunning()){
						AlarmManager alarm = (AlarmManager)getSystemService(ALARM_SERVICE);
						alarm.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis()+ (1000 * 30 ) , (1000 * 60 *1),
								PendingIntent.getService(this, 0, new Intent(this, AppSyncService.class), 0));
					}
					if(!isReminderServiceRunning()){
						AlarmManager alarm = (AlarmManager)getSystemService(ALARM_SERVICE);
						alarm.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis()+ (1000 * 30 ) , (1000 * 60 *15),
								PendingIntent.getService(this, 1, new Intent(this, GetReminderService.class), 0));
					}
				}
			}
			else{
				threadExistingContacts();
			}
		}
	}

	public void threadExistingContacts(){
		Thread thread=new Thread(new Runnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				try{
					existingContact();
				}
				catch(Exception e){
					e.printStackTrace();
				}
			}
		});
		thread.start();	
	}

	private boolean isServiceRunning() {
		ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
		for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
			if (AppSyncService.class.getName().equals(service.service.getClassName())) {
				return true;
			}
		}
		return false;
	}

	private boolean isReminderServiceRunning() {
		ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
		for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
			if (GetReminderService.class.getName().equals(service.service.getClassName())) {
				return true;
			}
		}
		return false;
	}

	public void GroupThread(){
		Thread t = new Thread(){
			public void run(){
				Looper.prepare();
				// Preparing get data params
				ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();

				params.add(new BasicNameValuePair("username", grp_user));

				ServiceHandler jsonParser = new ServiceHandler();
				try{
					String json = jsonParser.makeServiceCall(ServiceHandler.URL_RETRIEVE_GROUPS, ServiceHandler.GET,params);

					Log.v("Response: ", "> " + json);
					System.out.println("ARRAY IS="+json+"length is="+json.length());
					if(!json.equals("error")){

						if (json != null && json.length()>3) {
							//existingUser=true;
							//
							try {
								JSONArray array=new JSONArray(json);
								JSONObject obj=null;
								for(int i=0;i<array.length();i++){
									obj=array.getJSONObject(i);
									String guser=obj.getString("username");
									String gid=obj.getString("group_id");
									String gname=obj.getString("name");
									String gowner=obj.getString("owner");
									String gm=obj.getString("members");
									String gpic=obj.getString("group_pic");
									String gstatus=obj.getString("group_status");
									JSONArray member_array=new JSONArray(gm);
									if(!new GroupsLocalDB(getApplicationContext()).checkIfGrouptExists(gid)){
										for(int j=0;j< member_array.length();j++){
											String gmember = member_array.getString(j);
											new GroupsLocalDB(getApplicationContext()).insertGroupMembersLocal(gmember, gid);
											//Toast.makeText(getApplicationContext(), gm.toString(), Toast.LENGTH_SHORT).show();
										}
										new GroupsLocalDB(getApplicationContext()).insertGroupLocal(guser, gid, gname, gowner, gpic, gstatus);
									}
									else{
										new GroupsLocalDB(getApplicationContext()).deleteMembers(gid);
										for(int j=0;j< member_array.length();j++){
											String gmember = member_array.getString(j);
											new GroupsLocalDB(getApplicationContext()).insertGroupMembersLocal(gmember, gid);
										}
										//reminderList.add(new Reminder(title, number));
										new GroupsLocalDB(getApplicationContext()).updateGroupLocal(guser, gid, gname, gowner, gpic, gstatus);

									}
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
					//Toast.makeText(mContext, "Something wrong with network, Please try later", Toast.LENGTH_SHORT).show();
				}				
				Looper.loop();
			}
		};
		t.start();	
	}

	
	private void updateDateUI(long time){
		String convertedDate=null;
		Date date=new Date(time);
		DateFormat sf = new SimpleDateFormat("dd-MMM-yyyy");
		convertedDate=sf.format(date);
		dateText.setText(convertedDate.toString());

	}
	private String changeTimePattern(long timestamp){
		String convertedDate=null;
		Date date=new Date(timestamp);
		DateFormat sf = new SimpleDateFormat("dd::MMM::yyyy --hh:mm a");
		convertedDate=sf.format(date);
		return convertedDate;
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		
		sharedPref = getSharedPreferences("LAST_LOGIN", Activity.MODE_PRIVATE);

		if(shouldExecuteOnResume){
			updateListview();

			listView.setOnItemClickListener(new OnItemClickListener() {

				@Override
				public void onItemClick(AdapterView<?> arg0, View v, int arg2,
						long arg3) {
					// TODO Auto-generated method stub
					TextView c = (TextView) v.findViewById(R.id.nameText);
					String cname = c.getText().toString();
					String usernumber="";					
					usernumber=((Reminder) arg0.getAdapter().getItem(arg2)).getContactNo();

					Intent i =new Intent(ReminderMainActivity.this, ContactReminderScreen.class);
					i.putExtra("NUMBER",usernumber);
					i.putExtra("NAME",cname);
					i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					startActivity(i);				
				}			
			});

			if(skipFlag){
				//
			}
			else{
				if(cDetector.isConnectingToInternet())
				{
					if(sync_flag){
						//AppGroupList=new GroupsLocalDB(getApplicationContext()).getAllGroupList();
						//AppContactList=new ContactLocalDB(getApplicationContext()).getAllContactList();
						threadExistingContacts();
						//Intent AppSyncIntent=new Intent(getApplicationContext(),AppSyncService.class);
						//startService(AppSyncIntent);
						GroupThread();
						//new GetExistingContact().execute("0");						
					}

					//check gcm id flag
					gcmFlag=sharedPref.getBoolean("GCM_FLAG", false);

					//start Service for GCM update
					if(!gcmFlag){
						Intent serviceIntent=new Intent(this,GCMUpdateService.class);
						startService(serviceIntent);
					}
				}
				else{
					//threadExistingContacts();
					//existingContact();
				}
			}
		} else{
			shouldExecuteOnResume = true;
		}
	}


	private void updateListview(){

		new FetchCountTask().execute();
		Calendar cal=Calendar.getInstance();
		cal.set(cal.get(Calendar.YEAR),cal.get(Calendar.MONTH) ,cal.get(Calendar.DAY_OF_MONTH), 0, 0);
		long minVal=cal.getTimeInMillis();

		Calendar ccal=Calendar.getInstance();
		ccal.set(ccal.get(Calendar.YEAR),ccal.get(Calendar.MONTH) ,ccal.get(Calendar.DAY_OF_MONTH), 23, 59);
		long maxVal=ccal.getTimeInMillis();
		reminderMap = new HashMap<String, ArrayList<Reminder>>();
		updateDateUI(System.currentTimeMillis());

		System.out.println("Calendar Start="+changeTimePattern(minVal)+"Calendar End="+changeTimePattern(maxVal)+"current"+changeTimePattern(System.currentTimeMillis()));
		if(new ReminderLocalDB(mContext).getAllEvents(minVal, maxVal)>0){
			reminderList=new ReminderLocalDB(mContext).getReminderByDate(minVal, maxVal);
			adapter=new CustomAdapter(mContext, R.layout.reminder_layout, reminderList);
			noRecordText.setVisibility(View.GONE);
			listView.setVisibility(View.VISIBLE);
			listView.setAdapter(adapter);
			adapter.notifyDataSetChanged();
		}
		else{
			listView.setVisibility(View.GONE);
			noRecordText.setVisibility(View.VISIBLE);
			noRecordText.setText("No Reminders For Today");
		}	
	}

	private  void existingContact(){
		int totalContactCount =0;
		int phoneContactCount=0;
		existingContactListInPhoneBookWithOnlyNumber=new ArrayList<String>();
		existingContactListInPhoneBookWithAllInfo=new ArrayList<Contact>();
		NonAppcontactList=new ArrayList<Contact>();
		existingContactListInPhoneBookWithOnlyNumber.clear();
		appContactListOnServerForCurrentUserWithAllInfo=new ContactLocalDB(mContext).getAllContactNumber();

		ContentResolver cr = mContext.getContentResolver();
		Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI,
				null,null,null, "display_name asc");

		if (cur.getCount() > 0) {
			totalContactCount = cur.getCount();
			while (cur.moveToNext()) {
				String id = cur.getString(
						cur.getColumnIndex(ContactsContract.Contacts._ID));
				String name = cur.getString(
						cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
				// int  starred=cur.getInt(cur.getColumnIndex(ContactsContract.Contacts.STARRED));
				String lookup=cur.getString(cur.getColumnIndex(ContactsContract.Contacts.LOOKUP_KEY));

				String photoUrl;
		/*		if(!hasHoneycomb())	 
					photoUrl=cur.getString(cur.getColumnIndex(ContactsContract.Contacts._ID));
				else*/
					photoUrl=cur.getString(cur.getColumnIndex(ContactsContract.Contacts.PHOTO_THUMBNAIL_URI));


				if (Integer.parseInt(cur.getString(cur.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {
					Cursor pCur = cr.query( ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?", new String[] { id }, null);
					while (pCur.moveToNext()) {
						phone = pCur .getString(pCur .getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
						//System.out.println("phoneee "+ phone);
						//int stt= pCur.getInt(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.STARRED)); 						
						//if(phone.length()>=10){
							try{ 
								existingContactListInPhoneBookWithOnlyNumber.add(convertMobilePattern(phone));								
								existingContactListInPhoneBookWithAllInfo.add(new Contact(id, name,convertMobilePattern(phone),phone,lookup,photoUrl,""));
								NonAppcontactList=matchContact(existingContactListInPhoneBookWithAllInfo,appContactListOnServerForCurrentUserWithAllInfo);

							}catch(Exception e){
								e.printStackTrace();
							}
						//}
					}
					pCur.close();
				}
			}			
			//NonAppcontactList=matchContact(newContactList,phoneList);
			editor=sharedPref.edit();
			editor.putInt("phoneContactCount", phoneContactCount);
			editor.commit();
		}
		cur.close();
	}

	public static ArrayList<Contact> matchContact(List <Contact> cursorList,ArrayList<String> existList){
		ArrayList<Contact> temp=new ArrayList<Contact>();
		System.out.println("Cursor List Size"+cursorList.size()+"existList size="+existList.size());
		for(int i=0; i<cursorList.size();i++){

			if(!existList.contains(cursorList.get(i).getPhoneNo())){
				temp.add(new Contact(cursorList.get(i).getId(),cursorList.get(i).getName(), cursorList.get(i).
						getPhoneNo(),cursorList.get(i).getOriginalPhoneNumber(),cursorList.get(i).getLookUp(),cursorList.
						get(i).getUrl(),cursorList.get(i).getUserstatus()));
			}
		}
		System.out.println(" Cursor Temp List Size"+temp.size());
		return temp;
	}


	public static boolean hasHoneycomb() {
		return Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB;
	}

	public   String convertMobilePattern(String number)
	{    
		/*String out = number.replaceAll("[^0-9\\+]", "")        //remove all the non numbers (brackets dashes spaces etc.) except the + signs
				.replaceAll("(^[1-9].+)", "$1")         //if the number is starting with no zero and +, its a local number. prepend cc
				.replaceAll("(.)(\\++)(.)", "$1$3");         //if there are left out +'s in the middle by mistake, remove them
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

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.activity_reminder_main, menu);
		MenuItem item = menu.findItem(R.id.action_bell);

		LayerDrawable icon = (LayerDrawable) item.getIcon();
		if(!icon.equals(null)){
			// Update LayerDrawable's BadgeDrawable
			Utils.setBadgeCount(this, icon, mNotificationsCount);
		}
		return super.onCreateOptionsMenu(menu);
	}

	//badge icon code start
	@SuppressLint("NewApi")
	private void updateNotificationsBadge(int count) {
		mNotificationsCount = count;

		// force the ActionBar to relayout its MenuItems.
		// onCreateOptionsMenu(Menu) will be called again.
		invalidateOptionsMenu(); 	        
	}
	/*
	    Sample AsyncTask to fetch the notifications count
	 */
	class FetchCountTask extends AsyncTask<Void, Void, Integer> {

		@Override
		protected Integer doInBackground(Void... params) {
			// example count. This is where you'd 
			// query your data store for the actual count.
			int total_count=0, rem_count=0, response_count=0;
			rem_count=new ReminderLocalDB(mContext).getAllUnreadReminders();
			//response_count=new ReminderLocalDB(mContext).getAllResponseMsgs();
			//total_count=rem_count + response_count;
			return rem_count; 
		}

		@Override
		public void onPostExecute(Integer count) {
			updateNotificationsBadge(count);
		}
	}
	//badge icon code end
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Take appropriate action for each action item click

		switch (item.getItemId()) {
		case R.id.action_profile:
			// profile 
			Intent in =new Intent(ReminderMainActivity.this,ProfileActivity.class);
			startActivity(in);
			return true;

		case R.id.action_aboutus:
			// help
			Intent intent =new Intent(ReminderMainActivity.this,AboutUsActivity.class);
			startActivity(intent);
			return true;

		case R.id.action_bell:
			// previous reminder history
			Intent bellIntent =new Intent(ReminderMainActivity.this,NewReminder.class);
			startActivity(bellIntent);
			return true; 

		case R.id.action_calendar:
			// calendar View
			Intent i =new Intent(ReminderMainActivity.this,CalendarScreen.class);
			startActivityForResult(i, 1);
			//showDialog(DATE_DIALOG_ID);
			return true;

		case R.id.action_settings:
			// settings 
			Intent inte =new Intent(ReminderMainActivity.this,SettingsActivity.class);
			startActivity(inte);
			return true;
		case R.id.action_group:
			// group creation
			Intent groupIntent =new Intent(ReminderMainActivity.this,MyGroupActivity.class);
			startActivity(groupIntent);
			return true;
		case R.id.action_rating:
			// rating action
			Intent rateIntent =new Intent(ReminderMainActivity.this,RatingActivity.class);
			startActivity(rateIntent);
			return true;
		case R.id.action_block_user:
			// rating action
			Intent blockIntent =new Intent(ReminderMainActivity.this,BlockUserActivity.class);
			startActivity(blockIntent);
			return true;
		case R.id.action_invite_user:
			// invitation action
			Intent sharingIntent = new Intent(Intent.ACTION_SEND);
			sharingIntent.setType("text/plain");
			sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT,"Ring-A-Bell App Invitation:- Download App from  https://play.google.com/store/apps/details?id=com.share2people.ringabell");
			startActivity(Intent.createChooser(sharingIntent,"RingABell Invitation"));
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
		//return true;
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		// ContentResolver cr = getContentResolver();
		//  cr.unregisterContentObserver(contentObserver);
	}

	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		//	super.onBackPressed();
		Intent intent = new Intent();
		intent.setAction(Intent.ACTION_MAIN);
		intent.addCategory(Intent.CATEGORY_HOME);
		startActivity(intent);
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case DATE_DIALOG_ID:
			return new DatePickerDialog(this, mDateSetListener, year, month,
					day);
		}
		return null;
	}

	@Override
	protected void onPrepareDialog(int id, Dialog dialog) {
		switch (id) {
		case DATE_DIALOG_ID:
			((DatePickerDialog) dialog).updateDate(year, month, day);
			break;
		}
	}

	private DatePickerDialog.OnDateSetListener mDateSetListener = new DatePickerDialog.OnDateSetListener() {

		public void onDateSet(DatePicker view, int myear, int monthOfYear,
				int dayOfMonth) {

			/*	Calendar currentDate=Calendar.getInstance();
	    	currentDate.set(mYear, mMonth, mDay);*/

			Calendar selectedDate=Calendar.getInstance();
			//monthOfYear=monthOfYear+1;
			selectedDate.set(myear, monthOfYear, dayOfMonth);
			System.out.println("year::"+year+"month::"+month+"day::"+day+"local year"+myear+"local month"+monthOfYear+"local Day"+dayOfMonth);

			year = myear;
			month = monthOfYear;
			day = dayOfMonth;

			Date dd=new Date(selectedDate.getTimeInMillis());
			DateFormat ddf = new SimpleDateFormat("dd-MMM-yyyy");
			System.out.println("Invalid Date::"+ddf.format(dd));
			Calendar cal=Calendar.getInstance();
			cal.set(myear,monthOfYear ,dayOfMonth, 0, 0);
			long minVal=cal.getTimeInMillis();


			Calendar ccal=Calendar.getInstance();
			ccal.set(myear,monthOfYear ,dayOfMonth, 23, 59);

			long maxVal=ccal.getTimeInMillis();
			updateDateUI(selectedDate.getTimeInMillis());
			System.out.println("Calendar Start="+changeTimePattern(minVal)+"Calendar End="+changeTimePattern(maxVal)+"current"+changeTimePattern(System.currentTimeMillis()));
			if(new ReminderLocalDB(mContext).getAllEvents(minVal, maxVal)>0){
				reminderList=new ReminderLocalDB(mContext).getReminderByDate(minVal, maxVal);
				adapter=new CustomAdapter(mContext, R.layout.reminder_layout, reminderList);
				noRecordText.setVisibility(View.GONE);
				listView.setVisibility(View.VISIBLE);
				listView.setAdapter(adapter);
				adapter.notifyDataSetChanged();
			}
			else{
				listView.setVisibility(View.GONE);
				noRecordText.setVisibility(View.VISIBLE);
				noRecordText.setText("No Reminder");
			}			
		}
	};

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch(v.getId()){
		case R.id.addImageButton:
			if(!cDetector.isConnectingToInternet()){
				showAlertDialog(ReminderMainActivity.this, "No Internet Connection", "Do you want to continue?", false);
			}
			else{
				Intent intent=new Intent(ReminderMainActivity.this,AddReminderActivity.class);
				startActivity(intent);
			}
			
			break;
		case R.id.existingTemplateButton:
			launchExistingTemplates();
		}
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
				Intent intent=new Intent(ReminderMainActivity.this,AddReminderActivity.class);
				startActivity(intent);
				//finish();
			}
		});
		
		alertDialog.setNegativeButton("Cancel",
				new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {

			}
		});	

		// Showing Alert Message
		alertDialog.create().show();
	}



	private void loadTemplateLocal(){
		List<String> tempList=new ArrayList<String>();

		tempList.add("Call Me");
		tempList.add("Wake Up");
		tempList.add("Take Medicines");
		tempList.add("Bring Groceries	");
		tempList.add("Happy Birthday");
		tempList.add("Doctor's Appointment");
		tempList.add("Pay Bills");
		tempList.add("Dinner Date");
		tempList.add("Movie Time");
		tempList.add("Party Time");
		tempList.add("Time To Study");
		tempList.add("Pick Kids From School");
		tempList.add("Pick Me Up");
		tempList.add("Get Ready For Game");
		tempList.add("Get Laundry Done");

		for(int i=0;i<tempList.size();i++){
			new TemplateLocalDB(mContext).insertEventLocal(tempList.get(i));
		}
	}
	private void launchExistingTemplates(){
		ArrayList<String> tempList=new ArrayList<String>();
		tempList=new TemplateLocalDB(mContext).getAllTemplateList();
		final String arr[]=new String[tempList.size()];
		tempList.toArray(arr);
		selectedItem=arr[0];

		AlertDialog.Builder builder = new AlertDialog.Builder(ReminderMainActivity.this);

		// Set the dialog title
		builder.setTitle("Select Template")
		// specify the list array, the items to be selected by default (null for none),
		// and the listener through which to receive call backs when items are selected
		// again, R.array.choices were set in the resources res/values/strings.xml
		.setSingleChoiceItems(arr, 0, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				selectedItem=arr[arg1];
			}
		})

		// Set the action buttons
		.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int id) {
				// user clicked OK, so save the mSelectedItems results somewhere
				// or return them to the component that opened the dialog
				//Toast.makeText(getApplicationContext(), "Template Select"+selectedItem, Toast.LENGTH_SHORT).show();
				Intent intent=new Intent(getApplicationContext(),AddReminderActivity.class);
				intent.putExtra("TITLE", selectedItem);
				startActivity(intent);
			}
		})

		.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int id) {
				// removes the dialog from the screen
			}
		})
		.show();

	}
	//fetch data from database

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


	//fetch data from database
	private class GetExistingContact extends AsyncTask<String, Void, Void> {
		boolean error=false;	
		@Override
		protected void onPreExecute() {
			super.onPreExecute();

			contactList=new ArrayList<Contact>();
			idList=new ArrayList<String>();
			regIdMap=new HashMap<String, String>();
			contactMap=new HashMap<String, Contact>();
			regIdMap.clear();
			if(!sync_flag){
				pDialog = new ProgressDialog(ReminderMainActivity.this);
				pDialog.setTitle("Configuring........");
				pDialog.setMessage("Please Wait...");
				pDialog.setCancelable(false);
				pDialog.show();
			}
		}

		@Override
		protected Void doInBackground(String... arg) {


			// Preparing get data params
			ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
			String val="";
			try{
				existingContact();

				val=combinedString(existingContactListInPhoneBookWithOnlyNumber);
			}
			catch(Exception e){
				e.printStackTrace();
			}
			params.add(new BasicNameValuePair("username", val));
			params.add(new BasicNameValuePair("sender", sharedPref.getString("USERNAME", "")));

			ServiceHandler jsonParser = new ServiceHandler();

			String json = jsonParser.makeServiceCall(ServiceHandler.URL_RETRIEVE_FRIENDS, ServiceHandler.POST,params);
			if(!json.equals("error")){
				Log.e("Response: ", "> " + json);
				System.out.println("ALL Friends Are="+json+"length is="+json.length());

				if (json != null && json.length()>3) {

					try {
						JSONArray array=new JSONArray(json);
						JSONObject obj=null;
						for(int i=0;i<array.length();i++){
							obj=array.getJSONObject(i);
							String number=obj.getString("username");
							String regid=obj.getString("reg_id");
							String deviceid=obj.getString("device_id");
							String profilePic=obj.getString("profile_pic");
							String userstatus=obj.getString("user_status");
							String deviceIdentification=obj.getString("device_identification");

							System.out.println("numbeer "+ number);


							idList.add(number);
							//regIdMap.put(number, regid);
							contactMap.put(number, new Contact(number,regid,profilePic,Integer.parseInt(deviceIdentification),userstatus));
						}
					} catch (JSONException e) {
						e.printStackTrace();
					}
				} else {
					Log.e("JSON Data", "Didn't receive any data from server!");
				}
			}
			else
				error=true;
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);

			if(!error){
				try{
					List<Contact> mm=matchContact(existingContactListInPhoneBookWithAllInfo,idList);
					//store contact locally
					new ContactLocalDB(mContext).insertContactLocal("Me",grp_user,"Me",grp_user,"","");
					for(int k=0;k<mm.size();k++){
						if(!new ContactLocalDB(mContext).checkIfContactExists(mm.get(k).getPhoneNo())){
							new ContactLocalDB(mContext).insertContactLocal(mm.get(k).getId(),mm.get(k).getPhoneNo(), mm.get(k).getName(),mm.get(k).getOriginalPhoneNumber(),mm.get(k).getLookUp(),mm.get(k).getUrl());
						}
						else{
							//new ContactLocalDB(mContext).updateContact(mm.get(k).getId(),mm.get(k).getPhoneNo(), mm.get(k).getName(),mm.get(k).getOriginalPhoneNumber(),mm.get(k).getLookUp(),mm.get(k).getUrl());
						}
						new ContactLocalDB(mContext).updateContactFlag(mm.get(k).getPhoneNo());
					}
					new ContactLocalDB(mContext).updateContactFlag(grp_user);
					new ContactLocalDB(mContext).deleteContact("0");
					new ContactLocalDB(mContext).updateContactFlagAgain();
					//update regId Value
					/*for (Entry<String, String> ee : regIdMap.entrySet()) {
					//	new ContactLocalDB(mContext).updateRegID(ee.getKey(), ee.getValue());
					}*/
					for (Entry<String, Contact> eee : contactMap.entrySet()) {
						new ContactLocalDB(mContext).updateRegID(eee.getKey(), eee.getValue().getRegID(), eee.getValue().getUrl(),eee.getValue().getDeviceIdentification(),eee.getValue().getUserstatus());
						//new ContactLocalDB(mContext).updateRegID(eee.getKey(), eee.getValue());
					}
					new GetExistingBlockedContact().execute();
					editor=sharedPref.edit();
					editor.putBoolean("ONETIME_SYNC", true);
					editor.commit();
				}
				catch(NullPointerException nException){
					nException.printStackTrace();
				}
			}
			if(!sync_flag){
				if (pDialog.isShowing()){
					pDialog.dismiss();
				}
			}
		}
	}

	//fetch data from database
	private class GetExistingBlockedContact extends AsyncTask<String, Void, Void> {
		boolean error=false;	
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			bnumberList=new ArrayList<String>();
		}


		@Override
		protected Void doInBackground(String... arg) {

			// Preparing get data params
			ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
			String blockowner=grp_user;
			params.add(new BasicNameValuePair("blockowner", blockowner));
			ServiceHandler jsonParser = new ServiceHandler();

			String json = jsonParser.makeServiceCall(ServiceHandler.URL_RETRIEVE_BLOCK_CONTACTS, ServiceHandler.GET,params);
			if(!json.equals("error")){

				Log.e("Response: ", "> " + json);
				System.out.println("BLOCKED USERS Are="+json+"length is="+json.length());

				if (json != null && json.length()>3) {

					try {
						JSONArray array=new JSONArray(json);
						JSONObject obj=null;
						for(int i=0;i<array.length();i++){
							obj=array.getJSONObject(i);
							String bnumber=obj.getString("block_user");
							System.out.println("username=::"+bnumber);
							bnumberList.add(bnumber);
						}
					} catch (JSONException e) {
						e.printStackTrace();
					}

				} else {
					Log.e("JSON Data", "Didn't receive any data from server!");
				}
			}
			else
				error=true;
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			if(!error){
				try{
					for(int k=0;k<bnumberList.size();k++){
						if(new ContactLocalDB(mContext).checkIfContactExists(bnumberList.get(k))){
							new ContactLocalDB(mContext).updateBlockUser(bnumberList.get(k));
						}						
					}					
				}
				catch(NullPointerException nException){
					nException.printStackTrace();
				}
			}			
		}
	}


	public String combinedString(List<String> listUpdate){

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

	private List<Contact> matchContact(List <Contact> cursorList,List<String> existList){
		List<Contact> temp=new ArrayList<Contact>();

		for(int i=0; i<cursorList.size();i++){

			if(existList.contains(cursorList.get(i).getPhoneNo())){
				temp.add(new Contact(cursorList.get(i).getId(), cursorList.get(i).getName(), cursorList.get(i).getPhoneNo(),
						cursorList.get(i).getOriginalPhoneNumber(),cursorList.get(i).getLookUp(),
						cursorList.get(i).getUrl(), cursorList.get(i).getUserstatus()));
			}
		}
		return temp;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);

		shouldExecuteOnResume = false;
		if(requestCode==1){
			if(resultCode==RESULT_OK){

				updateListview();

				/*	
				long d = data.getLongExtra("sDate", -1);		
				Calendar cal=Calendar.getInstance();
				cal.setTimeInMillis(d);
				int mYear = cal.get(Calendar.YEAR);
				int mMonth = cal.get(Calendar.MONTH);
				int mDay = cal.get(Calendar.DAY_OF_MONTH);
				cal.set(mYear,mMonth ,mDay, 0, 0);

				long minVal=cal.getTimeInMillis();


				Calendar ccal=Calendar.getInstance();
				ccal.set(mYear,mMonth ,mDay, 23, 59);

				long maxVal=ccal.getTimeInMillis();
				updateDateUI(d);
				System.out.println("Calendar Start="+changeTimePattern(minVal)+"Calendar End="+changeTimePattern(maxVal)+"current"+changeTimePattern(System.currentTimeMillis()));
				if(new ReminderLocalDB(mContext).getAllEvents(minVal, maxVal)>0){
					//Toast.makeText(this, "dsd" ,Toast.LENGTH_LONG).show();					
					reminderList=new ReminderLocalDB(mContext).getReminderByDate(minVal, maxVal);
					adapter=new CustomAdapter(mContext, R.layout.reminder_layout, reminderList);
					noRecordText.setVisibility(View.GONE);
					listView.setVisibility(View.VISIBLE);
					listView.setAdapter(adapter);
					adapter.notifyDataSetChanged();
				}
				else{
					listView.setVisibility(View.GONE);
					noRecordText.setVisibility(View.VISIBLE);
					noRecordText.setText("No Reminder");
				}*/				

			}
			if(resultCode==RESULT_CANCELED){

			} 
		}
	}

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
			TextView title,receiver,time;
			ImageView sendImage, deliverImage,visibleImage;
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
			convertView=inflator.inflate(R.layout.reminder_layout, null);
			viewHolder=new ViewHolder();
			viewHolder.time=(TextView) convertView.findViewById(R.id.timeText);
			viewHolder.title=(TextView) convertView.findViewById(R.id.nameText);
			viewHolder.receiver=(TextView) convertView.findViewById(R.id.phoneText);

			viewHolder.visibleImage=(ImageView) convertView.findViewById(R.id.visibleIcon);
			viewHolder.sendImage=(ImageView) convertView.findViewById(R.id.sendIcon);
			viewHolder.deliverImage=(ImageView) convertView.findViewById(R.id.DeliverIcon);
			viewHolder.profilePic=(QuickContactBadge) convertView.findViewById(R.id.icon);
			convertView.setTag(viewHolder);
			//}
			//else 
			//viewHolder=(ViewHolder) convertView.getTag();


			final Reminder rem=reminders.get(position);
			//viewHolder.title.setText(rem.getTitle());
			viewHolder.time.setText(timeStampToTime(Long.parseLong(rem.getReceivingTime())));
			String user = sharedPref.getString("USERNAME", "");
			try{
				if(rem.getContactNo().equals(user)){
					viewHolder.title.setText("Me");
					//viewHolder.receiver.setText(rem.getTitle());
					imageLoader.displayImage(new ContactLocalDB(mContext).getContactPicUrl(rem.getContactNo()), viewHolder.profilePic, options, animateFirstListener);
				}
				else {
					List<Group> gp = new GroupsLocalDB(getApplicationContext()).getGroupById(rem.getContactNo());
					if (gp.isEmpty()){
						String uname= new ContactLocalDB(getApplicationContext()).getContactName(rem.getContactNo());
						if(uname.equals("")){
							viewHolder.title.setText(rem.getContactNo());
						}
						else{
							viewHolder.title.setText(uname);
							viewHolder.receiver.setText(rem.getContactNo());
						}
						//viewHolder.receiver.setText(rem.getTitle());
						imageLoader.displayImage(new ContactLocalDB(mContext).getContactPicUrl(rem.getContactNo()), viewHolder.profilePic, options, animateFirstListener);
					}
					else{
						for (Group g : gp) {
							gpname=g.getName();
						}
						viewHolder.title.setText(gpname);
						imageLoader.displayImage(new GroupsLocalDB(mContext).getGroupPicUrl(rem.getContactNo()), viewHolder.profilePic, options, animateFirstListener);
					}
				}
			}
			catch(Exception e){
				viewHolder.visibleImage.setImageResource(R.drawable.ic_action_event);
			}			
			return convertView;
		}
	}


	public class ContactObserver extends ContentObserver{
		public ContactObserver(Handler handler) {
			super(handler);
		}

		@SuppressLint("InlinedApi")
		@Override
		public void onChange(boolean selfChange) {
			super.onChange(selfChange);

			int totalContacts = 0;
			int totalPhoneContacts = 0;
			totalContacts = retrieveAllContactsCount();
			totalPhoneContacts = retrievePhoneContactsCount();

			if(totalPhoneContacts != sharedPref.getInt("phoneContactCount", 0)){
				//Toast.makeText(mContext, "contact added", Toast.LENGTH_SHORT).show();
				System.out.println("A");
				//Intent AppSyncIntent=new Intent(getApplicationContext(),AppSyncService.class);
				//startService(AppSyncIntent);
			}		

			editor=sharedPref.edit();
			editor.putInt("phoneContactCount", totalPhoneContacts);
			editor.commit();
			Toast.makeText(mContext, "Hello from service", Toast.LENGTH_SHORT).show();


			Log.e("","~~~~~~"+selfChange);
			System.out.println("CHANGE:"+selfChange);
			/*
			//int localContactCount = new ContactsLocalDB(mContext).getAllContacts();
			 int phoneBookCount = retrievePhoneBookContact();
	    System.out.println("Contacts Count :: "+phoneBookCount);
	       // long currentTimeStamp = System.currentTimeMillis();

	        // Contacts Cursor
	        Cursor cur = mContext.getContentResolver().query(
	                ContactsContract.Contacts.CONTENT_URI, null, null, null, null);

	        // Check which contact is added or updated
	        if (cur != null) {
	            while (cur.moveToNext()) {
	                // Get contact added/updated timestamp but CONTACT_LAST_UPDATED_TIMESTAMP 
	                //is added in API 18

	                String timeStampStr = cur
	                        .getString(cur.getColumnIndex(ContactsContract.Contacts.CONTACT_LAST_UPDATED_TIMESTAMP));

	                long timeStamp = Long.parseLong(timeStampStr);



	                // Check if any contact id added/updated with in 5 second
	                // you can reduce the time it take hardly 1 or 2 sec in 
	                // processing so i hape you will get the exact entry every time
//	                if (timeStamp > (currentTimeStamp - 5000)){
	                    // Get new/updated contact detail here
	                    String id = cur.getString(cur .getColumnIndex(ContactsContract.Contacts._ID));
	                    String contactName = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
	                    String ttime=cur.getString(cur.getColumnIndex(ContactsContract.Contacts.LAST_TIME_CONTACTED));
	                    long timeS=Long.parseLong(ttime);

	                    System.out.println("Contact Changed id:"+id+" and name is: "+contactName+"timestamp::"+convertTimeStamp(timeS));
	//            }
	        }
	        cur.close();
	    }*/
		}

		@Override
		public boolean deliverSelfNotifications() {
			return true;
		}
	}


	private int retrieveAllContactsCount(){

		int count=0;
		String projection[] =new String[] {ContactsContract.Contacts._ID};
		Cursor cur = mContext.getContentResolver().query(
				ContactsContract.Contacts.CONTENT_URI, projection, null, null, null);		     

		// Check which contact is added or updated
		if (cur != null) {
			count = cur.getCount();
		}		  
		return count;
	}
	private int retrievePhoneContactsCount(){

		int count=0;
		String id="",phoneNo="";

		Cursor cur = mContext.getContentResolver().query(
				ContactsContract.Contacts.CONTENT_URI, null, null, null, null);


		// Check which contact is added or updated
		if (cur != null) {
			while (cur.moveToNext()) {
				id = cur.getString(
						cur.getColumnIndex(ContactsContract.Contacts._ID));
				if (Integer.parseInt(cur.getString(cur.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {


					Cursor pCur = mContext.getContentResolver().query( ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?", new String[] { id }, null);
					while (pCur.moveToNext()) {

						phoneNo = pCur .getString(pCur .getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
						if(phoneNo.length()>=10)
							count++;
					}
				}
			}
		}
		return count;
	}

}
