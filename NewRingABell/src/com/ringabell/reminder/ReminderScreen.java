package com.ringabell.reminder;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.KeyguardManager;
import android.app.KeyguardManager.KeyguardLock;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.PowerManager;
import android.os.SystemClock;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.telephony.PhoneStateListener;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
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
import com.ringabell.serverdb.ServiceHandler;
import com.ringabell.utils.ConnectionDetector;
import com.share2people.ringabell.R;

public class ReminderScreen extends Activity {

	MediaPlayer mp = null;
	MediaPlayer mp2 = new MediaPlayer();
	AlarmManager am;
	Vibrator vibrator;
	PendingIntent pendingIntent;
	TextView tv;
	ImageView play, pause, mic;
	Chronometer chromometer;
	TextView totalTime;
	SeekBar seek_bar;
	LinearLayout MLayout;
	String reminderid = "", sender = "", sendername = "", remtime = "",
			title = "", voice = "", location_name = "", gid = "";
	int pos = 0;
	String outputFile = "";
	KeyguardManager keyguardManager;
	KeyguardLock keyguardLock;
	long mLastStopTime = 0;
	private static final String RESPONSE_MESSAGE = "Done";
	private static PowerManager.WakeLock wakeLock;
	boolean vibrationMode = false, finish = false/*, callingMode = false*/;

	TextView titleText, internetText;
	EditText responseText;
	ProgressDialog pDialog;
	private ConnectionDetector cDetector;
	protected ImageLoader imageLoader;
	DisplayImageOptions options;
	private ImageLoadingListener animateFirstListener = new AnimateFirstDisplayListener();
	SharedPreferences preferences;
	String receiverMobileNumber = "", senderNumber = "";

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_reminder_screen);
		vibrator = (Vibrator) getApplicationContext().getSystemService(
				Context.VIBRATOR_SERVICE);
		getActionBar().setTitle(getString(R.string.app_name));
		getActionBar().setBackgroundDrawable(
				new ColorDrawable(getResources().getColor(
						R.color.display_contact_action_bar)));

		Button dsAlarm = (Button) findViewById(R.id.dismiss);
		Button snAlarm = (Button) findViewById(R.id.snooze);
		Button responseAlarm = (Button) findViewById(R.id.response);

		TextView sender_name = (TextView) findViewById(R.id.sender);
		TextView rtitle = (TextView) findViewById(R.id.rTitle);
		TextView rlocation = (TextView) findViewById(R.id.rLocation);
		ImageView sender_pic = (ImageView) findViewById(R.id.SenderPic);

		am = (AlarmManager) getSystemService(Activity.ALARM_SERVICE);

		cDetector = new ConnectionDetector(getApplicationContext());

		preferences = getSharedPreferences("LAST_LOGIN", Activity.MODE_PRIVATE);

		receiverMobileNumber = preferences.getString("USERNAME", "");

		sender = getIntent().getStringExtra("sender");
		title = getIntent().getStringExtra("rTitle");
		voice = getIntent().getStringExtra("voicefile");
		gid = getIntent().getStringExtra("gid");

		reminderid = getIntent().getStringExtra("rem_id");
		location_name = getIntent().getStringExtra("location");
		// pos = getIntent().getIntExtra("position",1);
		remtime = getIntent().getStringExtra("remTime");
		senderNumber = new ReminderLocalDB(getApplicationContext())
				.getReminderSender(reminderid);
		imageLoader = ImageLoader.getInstance();

		// options for image loading
		options = new DisplayImageOptions.Builder()
				.showImageOnLoading(R.drawable.ic_user)
				.showImageForEmptyUri(R.drawable.ic_user)
				.showImageOnFail(R.drawable.ic_user).cacheInMemory(true)
				.cacheOnDisk(true).considerExifParams(true)
				.displayer(new RoundedBitmapDisplayer(130)).build();

		if (!gid.equals("0")) {
			sender_name.setText(sender);
			imageLoader.displayImage(new GroupsLocalDB(getApplicationContext())
					.getGroupPicUrl(gid), sender_pic, options,
					animateFirstListener);
		} else {
			sendername = new ContactLocalDB(getApplicationContext())
					.getContactName(sender);
			sender_name.setText(sendername);
			imageLoader.displayImage(
					new ContactLocalDB(getApplicationContext())
							.getContactPicUrl(sender), sender_pic, options,
					animateFirstListener);
		}

		rtitle.setText(title);
		rlocation.setText(location_name);

		tv = (TextView) findViewById(R.id.digi_clock);
		chromometer = (Chronometer) findViewById(R.id.chrono);
		// totalTime = (TextView) findViewById(R.id.totalTime);
		// seek_bar = (SeekBar) findViewById(R.id.seekbar);
		play = (ImageView) findViewById(R.id.imgPlay);
		pause = (ImageView) findViewById(R.id.imgPause);
		mic = (ImageView) findViewById(R.id.rmic);
		MLayout = (LinearLayout) findViewById(R.id.mLayout);
		// totalTime.setVisibility(View.GONE);
		// seek_bar.setVisibility(View.GONE);
		play.setVisibility(View.GONE);

		SharedPreferences sharedPrefs = PreferenceManager
				.getDefaultSharedPreferences(this);
		vibrationMode = sharedPrefs.getBoolean("reminder_viberate", false);
		String snoozeInterval = sharedPrefs.getString("snooze_list", "15");
		pos = Integer.parseInt(snoozeInterval);

		if (!voice.equals("TEXT")) {
			mic.setVisibility(View.VISIBLE);
			chromometer.setVisibility(View.VISIBLE);
			MLayout.setVisibility(View.VISIBLE);

			outputFile = Environment.getExternalStorageDirectory()
					.getAbsolutePath() + "/RingABell/" + reminderid + ".3gp";

			try {
				mp2.setDataSource(outputFile);
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

			try {
				mp2.prepare();
				chromometer.setBase(SystemClock.elapsedRealtime());
				chromometer.start();
				mp2.start();
				int totalDuration = 0;
				totalDuration = mp2.getDuration();
				// totalDuration = outputFile.getAudioHeader().getTrackLength();
				Calendar timeCal = Calendar.getInstance();
				timeCal.setTimeInMillis(totalDuration);

				int min = timeCal.get(Calendar.MINUTE);
				int sec = timeCal.get(Calendar.SECOND);

				// Date date=new Date(totalDuration);
				// DateFormat sf = new SimpleDateFormat("mm:ss");
				// String timeVal=(String) sf.format(date);
				String timeVal = min + ":" + sec;

				// totalTime.setText(timeVal);
				// Toast.makeText(ReminderScreen.this, timeVal,
				// Toast.LENGTH_SHORT).show();
				mp2.setOnCompletionListener(new OnCompletionListener() {
					// When audio is done will change pause to play
					public void onCompletion(MediaPlayer mp) {
						chromometer.stop();
						// mp2.release();
						try {
							mp2.prepare();
						} catch (IllegalStateException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						chromometer.setBase(SystemClock.elapsedRealtime());
						chromometer.start();
						mp2.start();
					}
				});
			} catch (FileNotFoundException e) {
				// TODO: handle exception
				runOnUiThread(new Runnable() {
					public void run() {
						// Toast.makeText(ReminderScreen.this,
						// "Audio Not Exists.", Toast.LENGTH_SHORT).show();
					}
				});

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			chromometer.setVisibility(View.GONE);
			// seek_bar.setVisibility(View.GONE);
			MLayout.setVisibility(View.GONE);
			mp = MediaPlayer.create(getBaseContext(), getAlarmUri());

		}

		// Calendar timeCal=Calendar.getInstance();
		// timeCal.setTimeInMillis(System.currentTimeMillis());
		// long curTime = timeCal.getTimeInMillis();
		tv.setText(remtime);

		play.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				mp2.start();
				// on first start
				if (mLastStopTime == 0)
					chromometer.setBase(SystemClock.elapsedRealtime());
				// on resume after pause
				else {
					long intervalOnPause = (SystemClock.elapsedRealtime() - mLastStopTime);
					chromometer.setBase(chromometer.getBase() + intervalOnPause);
				}
				chromometer.start();
				play.setVisibility(View.GONE);
				pause.setVisibility(View.VISIBLE);
			}
		});

		pause.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				mp2.pause();
				chromometer.stop();
				mLastStopTime = SystemClock.elapsedRealtime();
				pause.setVisibility(View.GONE);
				play.setVisibility(View.VISIBLE);
			}
		});

		responseAlarm.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				responseAlert(cDetector.isConnectingToInternet());
			}
		});

		dsAlarm.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub

				if (!voice.equals("TEXT")) {
					mp2.stop();
					mp2.release();
				} else {
					mp.stop();
				}
				vibrator.cancel();
				finish = true;
				setMaxVolume();
				finish();
			}
		});

		snAlarm.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Random rand = new Random();

				// nextInt is normally exclusive of the top value,
				// so add 1 to make it inclusive
				int randomNum = rand.nextInt((900 - 100) + 100);
				long tstamp = System.currentTimeMillis() % 100000;
				String tmp = String.valueOf(tstamp) + String.valueOf(randomNum);
				int reqCode = Integer.parseInt(tmp);
				// Toast.makeText(ReminderScreen.this, String.valueOf(reqCode) ,
				// Toast.LENGTH_SHORT).show();

				Calendar cal = Calendar.getInstance();
				cal.add(Calendar.MINUTE, pos);
				Intent intent = new Intent(ReminderScreen.this,
						ReminderScreen.class);
				intent.putExtra("sender", sender);
				intent.putExtra("rTitle", title);
				intent.putExtra("rem_id", reminderid);
				intent.putExtra("voicefile", voice);
				intent.putExtra("location", location_name);
				intent.putExtra("gid", gid);
				intent.putExtra("remTime", remtime);

				// int req_code = System.currentTimeMillis();
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				pendingIntent = PendingIntent.getActivity(ReminderScreen.this,
						reqCode, intent, PendingIntent.FLAG_CANCEL_CURRENT);

				am.set(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(),
						pendingIntent);

				if (!voice.equals("TEXT")) {
					mp2.stop();
				} else {
					mp.stop();
				}
				// wakeLock.release();
				// keyguardLock.reenableKeyguard();
				vibrator.cancel();
				finish = true;
				setMaxVolume();
				finish();
			}
		});

		if (voice.equals("TEXT")) {
			playSound(this, getAlarmUri());
		}

		TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);

		PhoneStateListener callStateListener = new PhoneStateListener() {
			public void onCallStateChanged(int state, String incomingNumber) {
				if (state == TelephonyManager.CALL_STATE_RINGING) {
					// Riging
					if (!finish) {
						if (!voice.equals("TEXT")) {
							mp2.pause();
							chromometer.stop();
							mLastStopTime = SystemClock.elapsedRealtime();
							pause.setVisibility(View.GONE);
							play.setVisibility(View.VISIBLE);
						} else {
							mp.pause();
						}
						vibrator.cancel();
					
					}
				}
				if (state == TelephonyManager.CALL_STATE_OFFHOOK) {
					// Currently in A call
					if (!finish) {
						if (!voice.equals("TEXT")) {
							mp2.pause();
							chromometer.stop();
							mLastStopTime = SystemClock.elapsedRealtime();
							pause.setVisibility(View.GONE);
							play.setVisibility(View.VISIBLE);
						} else {
							mp.pause();
						}
						vibrator.cancel();
						
					}
				}

				if (state == TelephonyManager.CALL_STATE_IDLE) {
					//neither ringing nor in a call
					
						if (vibrationMode) {
							setMinVolume();
						} else {
							setMaxVolume();
						}
		
						if (!voice.equals("TEXT")) {
							mp2.start();
							long intervalOnPause = (SystemClock.elapsedRealtime() - mLastStopTime);
							chromometer.setBase(chromometer.getBase() + intervalOnPause);
							chromometer.start();
							play.setVisibility(View.GONE);
							pause.setVisibility(View.VISIBLE);
						} else {
							mp.start();
						}
						long[] pattern = { 0, 300, 200 };
						vibrator.vibrate(pattern, 1);
					
				}
			}
		};
		telephonyManager.listen(callStateListener,
				PhoneStateListener.LISTEN_CALL_STATE);
	}

	private void playSound(final Context context, Uri alert) {

		Thread background = new Thread(new Runnable() {
			public void run() {
				try {
					mp.start();
				} catch (Throwable t) {
					Log.i("Animation", "Thread  exception " + t);
				}
				mp.setOnCompletionListener(new OnCompletionListener() {
					// When audio is done will change pause to play
					public void onCompletion(MediaPlayer mpl) {
						// mp2.release();
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

	public void setMaxVolume() {
		AudioManager amanager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

		amanager.setStreamVolume(AudioManager.STREAM_MUSIC,
				amanager.getStreamMaxVolume(AudioManager.STREAM_MUSIC), 0);
	}

	public void setMinVolume() {

		AudioManager amanager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

		amanager.setStreamVolume(AudioManager.STREAM_MUSIC, 0, 0);
		long[] pattern = { 0, 300, 200 };
		vibrator.vibrate(pattern, 1);
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
	protected void onDestroy() {
		super.onDestroy();
		// mp2.stop();
		// mp2.release();

	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		if (!finish) {
			if (!voice.equals("TEXT")) {
				mp2.pause();
				chromometer.stop();
				mLastStopTime = SystemClock.elapsedRealtime();
				pause.setVisibility(View.GONE);
				play.setVisibility(View.VISIBLE);
			} else {
				mp.pause();
			}
			vibrator.cancel();
			//callingMode = true;
		}
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		/*if (!callingMode) {
			if (vibrationMode) {
				setMinVolume();
			} else {
				setMaxVolume();
			}
		} else {
			if (!voice.equals("TEXT")) {
				mp2.start();
				long intervalOnPause = (SystemClock.elapsedRealtime() - mLastStopTime);
				chromometer.setBase(chromometer.getBase() + intervalOnPause);
				chromometer.start();
				play.setVisibility(View.GONE);
				pause.setVisibility(View.VISIBLE);
			} else {
				mp.start();
			}
			long[] pattern = { 0, 300, 200 };
			vibrator.vibrate(pattern, 1);
		}*/
	}

	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
	}

	@SuppressLint("InflateParams")
	private void responseAlert(final boolean isInternet) {

		LayoutInflater li = LayoutInflater.from(ReminderScreen.this);
		LinearLayout layout = (LinearLayout) li.inflate(
				R.layout.response_alert_layout, null);

		AlertDialog.Builder builder = new AlertDialog.Builder(
				ReminderScreen.this);
		builder.setTitle("Reminder Response");
		builder.setView(layout);
		builder.setPositiveButton("Send", null);

		builder.setNegativeButton("Cancel", null);

		if (!isInternet)
			builder.setIcon(R.drawable.fail);
		builder.setCancelable(false);

		final AlertDialog dialog = builder.create();
		dialog.show();

		titleText = (TextView) layout.findViewById(R.id.titleText);
		internetText = (TextView) layout
				.findViewById(R.id.internetConnectionText);
		responseText = (EditText) layout.findViewById(R.id.responseEditText);

		titleText.setText(title);
		if (isInternet)
			internetText.setVisibility(View.GONE);

		responseText.setText(RESPONSE_MESSAGE);

		dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(
				new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						Boolean isCloseDialog = false;
						String message = responseText.getText().toString();
						if (message.equals("")) {
							Toast.makeText(getApplicationContext(),
									"Please type some text", Toast.LENGTH_SHORT)
									.show();
						} else {
							String respondMessage = message;
							if (isInternet)
								new ResponseTask().execute(reminderid,
										receiverMobileNumber, respondMessage);
							else
								sendMessage(senderNumber, respondMessage);
							isCloseDialog = true;
						}
						if (isCloseDialog)
							dialog.dismiss();
					}
				});
	}

	private void sendMessage(String mobileNumber, String responseMessage) {
		// Toast.makeText(getApplicationContext(), "Sms user",
		// Toast.LENGTH_SHORT).show();
		String message = receiverMobileNumber + " response for reminder "
				+ title + "is" + responseMessage;
		SmsManager smsManager = SmsManager.getDefault();
		smsManager.sendTextMessage(mobileNumber, null, message, null, null);
		Toast.makeText(getApplicationContext(), "Response Send via SMS",
				Toast.LENGTH_SHORT).show();
		if (!voice.equals("TEXT")) {
			mp2.stop();
			mp2.release();
		} else {
			mp.stop();
		}
		vibrator.cancel();
		finish = true;
		finish();

	}

	// Get an alarm sound. Try for an alarm. If none set, try notification,
	// Otherwise, ringtone.
	private Uri getAlarmUri() {

		SharedPreferences sharedPrefs = PreferenceManager
				.getDefaultSharedPreferences(this);

		boolean vibration = true;
		if (vibration) {
			// Vibrate for 500 milliseconds
			long[] pattern = { 0, 1000, 500 };
			vibrator.vibrate(pattern, 0);

			if (!vibrationMode) {
				Handler handler = new Handler();
				handler.postDelayed(new Runnable() {
					public void run() {
						vibrator.cancel();
					}
				}, 5000);
			}
		}

		Uri alert;
		String almRingtone = sharedPrefs.getString(
				"notifications_alarm_ringtone", null);
		if (almRingtone == null) {
			alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
			if (alert == null) {
				alert = RingtoneManager
						.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
				if (alert == null) {
					alert = RingtoneManager
							.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
				}
			}
		} else {
			alert = Uri.parse(almRingtone);
		}
		return alert;
	}

	private class ResponseTask extends AsyncTask<String, Void, Void> {

		boolean isNewCategoryCreated = false, err = false;

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			pDialog = new ProgressDialog(ReminderScreen.this);
			pDialog.setTitle("Response Sending");
			pDialog.setMessage("Please Wait...");
			pDialog.setCancelable(false);
			pDialog.show();
		}

		@Override
		protected Void doInBackground(String... arg) {

			String reminderId = arg[0];
			String receiverNumber = arg[1];
			String responseMessage = arg[2];
			// Preparing post params

			ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("reminder_id", reminderId));
			params.add(new BasicNameValuePair("receiver", receiverNumber));
			params.add(new BasicNameValuePair("reminder_msg", responseMessage));

			ServiceHandler serviceClient = new ServiceHandler();
			try {
				String json = serviceClient.makeServiceCall(
						ServiceHandler.URL_RESPONSE_REMINDER,
						ServiceHandler.GET, params);
				if (!json.equals("error")) {

					Log.d("Reminder Response: ", "> " + json);
					System.out.println("Reminder Response is=" + json);
					System.out.println("Reminder Response Param:: rem ID-:"
							+ reminderId + "receiver-:" + receiverNumber
							+ "reponse-:" + responseMessage);

				} else
					err = true;
			} catch (Exception e) {
				e.printStackTrace();
				pDialog.dismiss();
				runOnUiThread(new Runnable() {

					@Override
					public void run() {
						// TODO Auto-generated method stub
						Toast.makeText(getApplicationContext(), "Error",
								Toast.LENGTH_SHORT).show();
					}
				});
				err = true;
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			if (pDialog.isShowing()) {
				pDialog.dismiss();
				if (!err) {
					Toast.makeText(getApplicationContext(), "Response Send",
							Toast.LENGTH_SHORT).show();
					if (!voice.equals("TEXT")) {
						mp2.stop();
						mp2.release();
					} else {
						mp.stop();
					}
					vibrator.cancel();
					finish = true;
					setMaxVolume();
					finish();
				}
			}
		}

	}

	private static class AnimateFirstDisplayListener extends
			SimpleImageLoadingListener {

		static final List<String> displayedImages = Collections
				.synchronizedList(new LinkedList<String>());

		@Override
		public void onLoadingComplete(String imageUri, View view,
				Bitmap loadedImage) {
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

}
