package com.share2people.ringabell;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
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

import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.GridView;
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
import com.ringabell.localdb.GroupsLocalDB;
import com.ringabell.localdb.ReminderLocalDB;
import com.ringabell.model.Group;
import com.ringabell.model.Reminder;
import com.share2people.ringabell.R;

@TargetApi(3)
public class CalendarScreen extends Activity implements OnClickListener {
	private static final String tag = "MyCalendarActivity";

	private LinearLayout calendarLayout;
	private TextView currentMonth, selectedDate;
	private Button done;
	private ImageView prevMonth, calendarHead;
	private ImageView nextMonth;
	private GridView calendarView;
	private GridCellAdapter adapter;
	private Calendar _calendar;
	@SuppressLint("NewApi")
	private int month, year;
	@SuppressWarnings("unused")
	@SuppressLint({ "NewApi", "NewApi", "NewApi", "NewApi" })
	private static final String dateTemplate = "MMMM yyyy";
	Date parsedDate =new Date();	


	Button addReminder,existingTemplate;
	TextView noRecordText,dateText;
	private ProgressDialog pDialog;
	private Context mContext;
	private SharedPreferences sharedPref;
	private ListView listView;
	LayoutInflater inflator;
	CustomAdapter adap;
	List<Reminder> reminderList;
	private final SimpleDateFormat dateFormatter = new SimpleDateFormat(
			"dd-MMM-yyyy");
	
	long mLastStopTime = 0;
	MediaPlayer mp2=new MediaPlayer() ;
	Chronometer chronometer;
	int play_flag=0;
	ImageView play, pause;
	private String remid="", outputFile="";


	/** Called when the activity is first created. */
	@SuppressLint("NewApi") @Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.my_calendar_view);

		getActionBar().setTitle(getString(R.string.calendar));
		getActionBar().setDisplayHomeAsUpEnabled(true);
		getActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.splash_action_bar)));


		_calendar = Calendar.getInstance(Locale.getDefault());
		month = _calendar.get(Calendar.MONTH) + 1;
		year = _calendar.get(Calendar.YEAR);
		Log.d(tag, "Calendar Instance:= " + "Month: " + month + " " + "Year: "
				+ year);

	
		selectedDate = (TextView) this
				.findViewById(R.id.selecteddateText);
		String curDate = new SimpleDateFormat("dd-MMMM-yyyy").format(new Date());
		selectedDate.setText(curDate);

		calendarLayout = (LinearLayout) this.findViewById(R.id.calButtonlayout);
		calendarHead = (ImageView) this.findViewById(R.id.calendarheader);
		prevMonth = (ImageView) this.findViewById(R.id.prevMonth);
		prevMonth.setOnClickListener(this);

		currentMonth = (TextView) this.findViewById(R.id.currentMonth);
		currentMonth.setText(DateFormat.format(dateTemplate,
				_calendar.getTime()));

		nextMonth = (ImageView) this.findViewById(R.id.nextMonth);
		nextMonth.setOnClickListener(this);

		calendarView = (GridView) this.findViewById(R.id.calendar);

		// Initialised
		adapter = new GridCellAdapter(getApplicationContext(),
				R.id.calendar_day_gridcell, month, year);
		adapter.notifyDataSetChanged();
		calendarView.setAdapter(adapter);

		mContext=(Context) getApplicationContext();
		noRecordText=(TextView) findViewById(R.id.cal_noReminder);
		listView=(ListView) findViewById(R.id.cal_rlist);
		calendarLayout.setVisibility(View.VISIBLE);
		calendarHead.setVisibility(View.VISIBLE);
		calendarView.setVisibility(View.VISIBLE);
		listView.setVisibility(View.GONE);
		noRecordText.setVisibility(View.GONE);
		
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
				
				LayoutInflater li = LayoutInflater.from(CalendarScreen.this);
				LinearLayout rinfo = (LinearLayout)li.inflate(R.layout.reminder_info, null);				
				AlertDialog.Builder builder = new AlertDialog.Builder(CalendarScreen.this);
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
							Toast.makeText(CalendarScreen.this, "File Not Exists.", Toast.LENGTH_SHORT).show();

							runOnUiThread(new Runnable() {
								public void run() {
									Toast.makeText(CalendarScreen.this, "File Not Exists.", Toast.LENGTH_SHORT).show();
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
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		//Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.receivers_done , menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Take appropriate action for each action item click

		switch (item.getItemId()) {

		case R.id.rece_done:
			Intent returnIntent = new Intent();
			returnIntent.putExtra("sDate",parsedDate.getTime());
			setResult(RESULT_OK,returnIntent);
			finish();
			return true;
			
		case R.id.action_calendar:
			// calendar View
			calendarLayout.setVisibility(View.VISIBLE);
			calendarHead.setVisibility(View.VISIBLE);
			calendarView.setVisibility(View.VISIBLE);
			listView.setVisibility(View.GONE);
			noRecordText.setVisibility(View.GONE);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	/**
	 * 
	 * @param month
	 * @param year
	 */
	private void setGridCellAdapterToDate(int month, int year) {
		adapter = new GridCellAdapter(getApplicationContext(),
				R.id.calendar_day_gridcell, month, year);
		_calendar.set(year, month - 1, _calendar.get(Calendar.DAY_OF_MONTH));
		currentMonth.setText(DateFormat.format(dateTemplate,
				_calendar.getTime()));
		adapter.notifyDataSetChanged();
		calendarView.setAdapter(adapter);
		listView.setVisibility(View.GONE);
	}

	@Override
	public void onClick(View v) {
		if (v == prevMonth) {
			if (month <= 1) {
				month = 12;
				year--;
			} else {
				month--;
			}
			Log.d(tag, "Setting Prev Month in GridCellAdapter: " + "Month: "
					+ month + " Year: " + year);
			setGridCellAdapterToDate(month, year);
		}
		if (v == nextMonth) {
			if (month > 11) {
				month = 1;
				year++;
			} else {
				month++;
			}
			Log.d(tag, "Setting Next Month in GridCellAdapter: " + "Month: "
					+ month + " Year: " + year);
			setGridCellAdapterToDate(month, year);
		}

	}

	@Override
	public void onDestroy() {
		Log.d(tag, "Destroying View ...");
		super.onDestroy();
	}

	// Inner Class
	public class GridCellAdapter extends BaseAdapter implements OnClickListener {
		private static final String tag = "GridCellAdapter";
		private final Context _context;

		private final List<String> list;
		private static final int DAY_OFFSET = 1;
		private final String[] weekdays = new String[] { "Sun", "Mon", "Tue",
				"Wed", "Thu", "Fri", "Sat" };
		private final String[] months = { "January", "February", "March",
				"April", "May", "June", "July", "August", "September",
				"October", "November", "December" };
		private final int[] daysOfMonth = { 31, 28, 31, 30, 31, 30, 31, 31, 30,
				31, 30, 31 };
		private int daysInMonth;
		private int currentDayOfMonth;
		private int currentWeekDay;
		private Button gridcell;
		private TextView num_events_per_day;
		private final HashMap<String, Integer> eventsPerMonthMap;
		private final SimpleDateFormat dateFormatter = new SimpleDateFormat(
				"dd-MMM-yyyy");

		// Days in Current Month
		public GridCellAdapter(Context context, int textViewResourceId,
				int month, int year) {
			super();
			this._context = context;
			this.list = new ArrayList<String>();
			Log.d(tag, "==> Passed in Date FOR Month: " + month + " "
					+ "Year: " + year);
			Calendar calendar = Calendar.getInstance();
			setCurrentDayOfMonth(calendar.get(Calendar.DAY_OF_MONTH));
			setCurrentWeekDay(calendar.get(Calendar.DAY_OF_WEEK));
			Log.d(tag, "New Calendar:= " + calendar.getTime().toString());
			Log.d(tag, "CurrentDayOfWeek :" + getCurrentWeekDay());
			Log.d(tag, "CurrentDayOfMonth :" + getCurrentDayOfMonth());

			// Print Month
			printMonth(month, year);

			// Find Number of Events
			eventsPerMonthMap = findNumberOfEventsPerMonth(year, month);
		}

		private String getMonthAsString(int i) {
			return months[i];
		}

		private String getWeekDayAsString(int i) {
			return weekdays[i];
		}

		private int getNumberOfDaysOfMonth(int i) {
			return daysOfMonth[i];
		}

		public String getItem(int position) {
			return list.get(position);
		}

		@Override
		public int getCount() {
			return list.size();
		}

		/**
		 * Prints Month
		 * 
		 * @param mm
		 * @param yy
		 */
		private void printMonth(int mm, int yy) {
			Log.d(tag, "==> printMonth: mm: " + mm + " " + "yy: " + yy);
			int trailingSpaces = 0;
			int daysInPrevMonth = 0;
			int prevMonth = 0;
			int prevYear = 0;
			int nextMonth = 0;
			int nextYear = 0;

			int currentMonth = mm - 1;
			String currentMonthName = getMonthAsString(currentMonth);
			daysInMonth = getNumberOfDaysOfMonth(currentMonth);

			Log.d(tag, "Current Month: " + " " + currentMonthName + " having "
					+ daysInMonth + " days.");

			GregorianCalendar cal = new GregorianCalendar(yy, currentMonth, 1);
			Log.d(tag, "Gregorian Calendar:= " + cal.getTime().toString());

			if (currentMonth == 11) {
				prevMonth = currentMonth - 1;
				daysInPrevMonth = getNumberOfDaysOfMonth(prevMonth);
				nextMonth = 0;
				prevYear = yy;
				nextYear = yy + 1;
				Log.d(tag, "*->PrevYear: " + prevYear + " PrevMonth:"
						+ prevMonth + " NextMonth: " + nextMonth
						+ " NextYear: " + nextYear);
			} else if (currentMonth == 0) {
				prevMonth = 11;
				prevYear = yy - 1;
				nextYear = yy;
				daysInPrevMonth = getNumberOfDaysOfMonth(prevMonth);
				nextMonth = 1;
				Log.d(tag, "**--> PrevYear: " + prevYear + " PrevMonth:"
						+ prevMonth + " NextMonth: " + nextMonth
						+ " NextYear: " + nextYear);
			} else {
				prevMonth = currentMonth - 1;
				nextMonth = currentMonth + 1;
				nextYear = yy;
				prevYear = yy;
				daysInPrevMonth = getNumberOfDaysOfMonth(prevMonth);
				Log.d(tag, "***---> PrevYear: " + prevYear + " PrevMonth:"
						+ prevMonth + " NextMonth: " + nextMonth
						+ " NextYear: " + nextYear);
			}

			int currentWeekDay = cal.get(Calendar.DAY_OF_WEEK) - 1;
			trailingSpaces = currentWeekDay;

			Log.d(tag, "Week Day:" + currentWeekDay + " is "
					+ getWeekDayAsString(currentWeekDay));
			Log.d(tag, "No. Trailing space to Add: " + trailingSpaces);
			Log.d(tag, "No. of Days in Previous Month: " + daysInPrevMonth);

			if (cal.isLeapYear(cal.get(Calendar.YEAR)))
				if (mm == 2)
					++daysInMonth;
				else if (mm == 3)
					++daysInPrevMonth;

			// Trailing Month days
			for (int i = 0; i < trailingSpaces; i++) {
				Log.d(tag,
						"PREV MONTH:= "
								+ prevMonth
								+ " => "
								+ getMonthAsString(prevMonth)
								+ " "
								+ String.valueOf((daysInPrevMonth
										- trailingSpaces + DAY_OFFSET)
										+ i));
				list.add(String
						.valueOf((daysInPrevMonth - trailingSpaces + DAY_OFFSET)
								+ i)
								+ "-GREY"
								+ "-"
								+ getMonthAsString(prevMonth)
								+ "-"
								+ prevYear);
			}

			Calendar c = Calendar.getInstance();
			String mnth = new SimpleDateFormat("MMMM").format(c.getTime());

			// Current Month Days
			for (int i = 1; i <= daysInMonth; i++) {
				Log.d(currentMonthName, String.valueOf(i) + " "
						+ getMonthAsString(currentMonth) + " " + yy);
				if (i == getCurrentDayOfMonth() && mnth.equalsIgnoreCase(getMonthAsString(currentMonth))) {
					list.add(String.valueOf(i) + "-BLUE" + "-"
							+ getMonthAsString(currentMonth) + "-" + yy);
				} else {
					list.add(String.valueOf(i) + "-WHITE" + "-"
							+ getMonthAsString(currentMonth) + "-" + yy);
				}
			}

			// Leading Month days
			for (int i = 0; i < list.size() % 7; i++) {
				Log.d(tag, "NEXT MONTH:= " + getMonthAsString(nextMonth));
				list.add(String.valueOf(i + 1) + "-GREY" + "-"
						+ getMonthAsString(nextMonth) + "-" + nextYear);
			}
		}

		/**
		 * NOTE: YOU NEED TO IMPLEMENT THIS PART Given the YEAR, MONTH, retrieve
		 * ALL entries from a SQLite database for that month. Iterate over the
		 * List of All entries, and get the dateCreated, which is converted into
		 * day.
		 * 
		 * @param year
		 * @param month
		 * @return
		 */
		private HashMap<String, Integer> findNumberOfEventsPerMonth(int year,
				int month) {
			HashMap<String, Integer> map = new HashMap<String, Integer>();
			return map;
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View row = convertView;
			if (row == null) {
				LayoutInflater inflater = (LayoutInflater) _context
						.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				row = inflater.inflate(R.layout.screen_gridcell, parent, false);
			}

			// Get a reference to the Day gridcell
			gridcell = (Button) row.findViewById(R.id.calendar_day_gridcell);
			gridcell.setOnClickListener(this);

			// ACCOUNT FOR SPACING
			Log.d(tag, "Current Day: " + getCurrentDayOfMonth());
			String[] day_color = list.get(position).split("-");
			String theday = day_color[0];
			String themonth = day_color[2];
			String theyear = day_color[3];
			if ((!eventsPerMonthMap.isEmpty()) && (eventsPerMonthMap != null)) {
				if (eventsPerMonthMap.containsKey(theday)) {
					num_events_per_day = (TextView) row
							.findViewById(R.id.num_events_per_day);
					Integer numEvents = (Integer) eventsPerMonthMap.get(theday);
					num_events_per_day.setText(numEvents.toString());
				}
			}

			// Set the Day GridCell
			gridcell.setText(theday);
			gridcell.setTag(theday + "-" + themonth + "-" + theyear);

			Log.d(tag, "Setting GridCell " + theday + "-" + themonth + "-"
					+ theyear);

			if (day_color[1].equals("GREY")) {
				gridcell.setTextColor(getResources()
						.getColor(R.color.lightgray));
			}
			if (day_color[1].equals("WHITE")) {
				String dateText = gridcell.getTag().toString();
				PrintCell(dateText);

				gridcell.setTextColor(getResources().getColor(
						R.color.black));
			}
			if (day_color[1].equals("BLUE")) {
				String dateText = gridcell.getTag().toString();
				PrintCell(dateText);
				gridcell.setTextColor(getResources().getColor(R.color.orrange));
			}
			return row;
		}

		@SuppressLint("NewApi")
		public void PrintCell(String dateText){
			Date dt = new Date();
			try {
				dt = dateFormatter.parse(dateText);
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			Calendar cal=Calendar.getInstance();
			cal.setTimeInMillis(dt.getTime());
			cal.set(cal.get(Calendar.YEAR),cal.get(Calendar.MONTH) ,cal.get(Calendar.DAY_OF_MONTH), 0, 0);
			long minVal=cal.getTimeInMillis();

			Calendar ccal=Calendar.getInstance();
			ccal.setTimeInMillis(dt.getTime());
			ccal.set(ccal.get(Calendar.YEAR),ccal.get(Calendar.MONTH) ,ccal.get(Calendar.DAY_OF_MONTH), 23, 59);

			long maxVal=ccal.getTimeInMillis();		
			if(new ReminderLocalDB(getApplicationContext()).getAllEvents(minVal, maxVal)>0){
				int sdk = android.os.Build.VERSION.SDK_INT;
				if(sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {
					gridcell.setBackgroundDrawable(getResources().getDrawable(R.drawable.cell_background)); 

				} else {
					gridcell.setBackground(getResources().getDrawable(R.drawable.cell_background)); 
				}
			}
		}

		@Override
		public void onClick(View view) {
			String date_month_year = (String) view.getTag();
			selectedDate.setText(date_month_year);
			Log.e("Selected date", date_month_year);
			showRemindersList(date_month_year);
			calendarLayout.setVisibility(View.GONE);
			calendarHead.setVisibility(View.GONE);
			calendarView.setVisibility(View.GONE);
		}

		public int getCurrentDayOfMonth() {
			return currentDayOfMonth;
		}

		private void setCurrentDayOfMonth(int currentDayOfMonth) {
			this.currentDayOfMonth = currentDayOfMonth;
		}

		public void setCurrentWeekDay(int currentWeekDay) {
			this.currentWeekDay = currentWeekDay;
		}


		public int getCurrentWeekDay() {
			return currentWeekDay;
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

	public void showRemindersList(String date_month_year) {
		try {
			Calendar cal=Calendar.getInstance();
			if(!date_month_year.equals("today")){			
				parsedDate = dateFormatter.parse(date_month_year);			
				cal.setTimeInMillis(parsedDate.getTime());
			}			
			cal.set(cal.get(Calendar.YEAR),cal.get(Calendar.MONTH) ,cal.get(Calendar.DAY_OF_MONTH), 0, 0);
			long minVal=cal.getTimeInMillis();

			Calendar ccal=Calendar.getInstance();
			if(!date_month_year.equals("today")){
				parsedDate = dateFormatter.parse(date_month_year);			
				ccal.setTimeInMillis(parsedDate.getTime());
			}
			ccal.set(ccal.get(Calendar.YEAR),ccal.get(Calendar.MONTH) ,ccal.get(Calendar.DAY_OF_MONTH), 23, 59);
			long maxVal=ccal.getTimeInMillis();

			if(new ReminderLocalDB(mContext).countRemindersByDate(minVal, maxVal)>0){
				reminderList=new ReminderLocalDB(mContext).getAllRemindersByDate(minVal, maxVal);
				adap=new CustomAdapter(mContext, R.layout.contact_reminder_layout, reminderList);
				noRecordText.setVisibility(View.GONE);
				listView.setVisibility(View.VISIBLE);
				listView.setAdapter(adap);
			}
			else{
				noRecordText.setVisibility(View.VISIBLE);
				listView.setVisibility(View.GONE);
				noRecordText.setText("No Reminders");
			}


		} catch (ParseException e) {
			e.printStackTrace();
		}		}

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
			TextView title,receiver,time,cname, location_name, responseText;
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
			viewHolder.cname=(TextView) convertView.findViewById(R.id.cdateText);

			viewHolder.title=(TextView) convertView.findViewById(R.id.cnameText);
			viewHolder.receiver=(TextView) convertView.findViewById(R.id.cphoneText);
			viewHolder.responseText=(TextView) convertView.findViewById(R.id.cResponse);


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

			viewHolder.title.setText(rem.getTitle());
			viewHolder.time.setText(timeStampToTime(rem.getRemTime()));
			viewHolder.receiver.setText(rem.getLocationName());	
			
			
			if(!rem.getResponseMsg().equals("")){
				viewHolder.responseText.setVisibility(View.VISIBLE);
			}
			else{
				viewHolder.responseText.setVisibility(View.GONE);
			}

			try{
				if(!rem.getVoiceFile().equals("TEXT")){
					viewHolder.audioImage.setVisibility(View.VISIBLE);
					viewHolder.audioImage.setImageResource(R.drawable.ic_action_volume_on);
				}
			}
			catch(Exception e){
				//viewHolder.visibleImage.setImageResource(R.drawable.ic_action_event);
			}

			try{
				imageLoader.displayImage(new ContactLocalDB(mContext).getContactPicUrl(rem.getContactNo()), viewHolder.profilePic, options, animateFirstListener);

				if(rem.getReminderType().equals("IN")){
					if(!rem.getGroupId().equals("0")){
						String gpname="";
						List<Group> gp = new GroupsLocalDB(getApplicationContext()).getGroupById(rem.getGroupId());
						for (Group g : gp) {
							gpname=g.getName();
						}
						viewHolder.cname.setText(gpname);
						imageLoader.displayImage(new GroupsLocalDB(mContext).getGroupPicUrl(rem.getGroupId()), viewHolder.profilePic, options, animateFirstListener);

					}
					else{
						imageLoader.displayImage(new ContactLocalDB(mContext).getContactPicUrl(rem.getSender()), viewHolder.profilePic, options, animateFirstListener);
						viewHolder.visibleImage.setVisibility(View.VISIBLE);
						viewHolder.visibleImage.setImageResource(R.drawable.ic_action_reminder_in);
						String uname= new ContactLocalDB(getApplicationContext()).getContactName(rem.getSender());
						viewHolder.cname.setText(uname);
					}
				}
				else{
					imageLoader.displayImage(new ContactLocalDB(mContext).getContactPicUrl(rem.getReceiver()), viewHolder.profilePic, options, animateFirstListener);
					viewHolder.visibleImage.setVisibility(View.VISIBLE);
					viewHolder.sendImage.setVisibility(View.VISIBLE);
					viewHolder.visibleImage.setImageResource(R.drawable.ic_action_reminder_out);
					viewHolder.sendImage.setImageResource(R.drawable.emoticon_smile);
					String uname= new ContactLocalDB(getApplicationContext()).getContactName(rem.getReceiver());
					if(uname.equals("")){
						viewHolder.cname.setText(rem.getReceiver());
					}
					else{
						viewHolder.cname.setText(uname);
					}
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

		SimpleDateFormat sf = new SimpleDateFormat("hh:mm a");
		String timeVal=(String) sf.format(date);
		return timeVal;
	}		

}

