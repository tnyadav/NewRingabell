package com.ringabell.user;

import java.util.ArrayList;
import java.util.Random;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;

import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gcm.GCMRegistrar;
import com.ringabell.reminder.ReminderMainActivity;
import com.ringabell.serverdb.ServiceHandler;
import com.ringabell.utils.AlertDialogManager;
import com.ringabell.utils.ConnectionDetector;
import com.share2people.ringabell.R;
import com.share2people.ringabell.R.drawable;
import com.share2people.ringabell.R.id;
import com.share2people.ringabell.R.layout;


public class LoginActivity extends Activity implements OnClickListener,OnItemClickListener{

	//All Widgets
	private Button loginButton,skipButton;
	private EditText mobileNumber,etCode; 
	private TextView countryCodeEdit;
	private static ArrayList<Sample> array_sort,sortedList;
	int textlength=0;
	private AlertDialog myalertDialog=null;
	private ProgressDialog pDialog;

	// Class Instantiation
	ConnectionDetector cDetector;
	Context mContext;
	//Local Storage
	SharedPreferences sharedPref;
	SharedPreferences.Editor editor;

	//Variables
	private String code="", username="",registrationId="",deviceID,otp,countryCode="";

	private boolean isLogin=false,isValidation=false;
	//Alert dialog
	// Alert dialog manager
	AlertDialogManager alert = new AlertDialogManager();
	InputMethodManager imm;

	//GCM PROJECT ID
	public static final String SENDER_ID = "951290017600";


	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	
		cDetector = new ConnectionDetector(getApplicationContext());

			if(!cDetector.isConnectingToInternet()){
				showAlertDialog(LoginActivity.this,
						"Internet Connection Error",
						"Please enable your Internet connection", false);

				return;
			}
			setContentView(R.layout.activity_login);
			mContext=(Context) getApplicationContext();

			countryCodeEdit=(TextView) findViewById(R.id.countrycode);
			imm = (InputMethodManager)getSystemService(
					Context.INPUT_METHOD_SERVICE);

			loadConunryList();

			countryCodeEdit.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View arg0) {
					// TODO Auto-generated method stub
					//imm.hideSoftInputFromWindow(countryCodeEdit.getWindowToken(), 0);
					loadAlert();					
				}
			});


			// Make sure the manifest was properly set - comment out this line
			// while developing the app, then uncomment it when it's ready.
			GCMRegistrar.checkDevice(getApplicationContext());

			// Make sure the manifest was properly set - comment out this line
			// while developing the app, then uncomment it when it's ready.
			GCMRegistrar.checkManifest(getApplicationContext());



			// Get GCM registration id

			registrationId = GCMRegistrar.getRegistrationId(getApplicationContext());
			// Toast.makeText(getApplicationContext(), "RegId::="+registrationId, Toast.LENGTH_SHORT).show();
			if(registrationId.equals("") || registrationId.equals(null))
				registerGCMId();  	  	

			mobileNumber=(EditText) findViewById(R.id.username);
			etCode=(EditText) findViewById(R.id.code);
			loginButton=(Button) findViewById(R.id.loginButton);
			skipButton=(Button) findViewById(R.id.skipButton);
			loginButton.setOnClickListener(this);
			skipButton.setOnClickListener(this);
			TelephonyManager tm = (TelephonyManager)getSystemService(TELEPHONY_SERVICE); 
			deviceID=tm.getDeviceId();
		
	}

	@Override
	public void onItemClick(AdapterView arg0, View arg1, int position, long arg3) {

		myalertDialog.dismiss();
		String strName=((Sample) arg0.getAdapter().getItem(position)).getcName();
		//countryCode = code = ((Sample) arg0.getAdapter().getItem(position)).getcCode();
		countryCode = ((Sample) arg0.getAdapter().getItem(position)).getcCode();
		countryCodeEdit.setText(strName);
		etCode.setText(countryCode);
	}


	private void loadAlert(){
		AlertDialog.Builder myDialog = new AlertDialog.Builder(LoginActivity.this);
		final EditText editText = new EditText(LoginActivity.this);
		editText.setHint("Search Country");
		editText.setSingleLine(true);
		imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
		final ListView listview=new ListView(LoginActivity.this);

		sortedList=new ArrayList<Sample> ();

		LinearLayout layout = new LinearLayout(LoginActivity.this);
		layout.setOrientation(LinearLayout.VERTICAL);
		layout.addView(editText);	
		layout.addView(listview);
		myDialog.setView(layout);
		CustomAlertAdapter arrayAdapter=new CustomAlertAdapter(LoginActivity.this, array_sort);
		listview.setAdapter(arrayAdapter);
		listview.setOnItemClickListener(this);
		editText.addTextChangedListener(new TextWatcher()
		{
			public void afterTextChanged(Editable s){

			}
			public void beforeTextChanged(CharSequence s,
					int start, int count, int after){

			}
			public void onTextChanged(CharSequence s, int start, int before, int count)
			{
				editText.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
				textlength = editText.getText().length();
				sortedList.clear();
				for (int i = 0; i < array_sort.size(); i++)
				{
					if (textlength <= array_sort.get(i).getcName().length())
					{

						if(array_sort.get(i).getcName().toLowerCase().contains(editText.getText().toString().toLowerCase().trim()))
						{
							sortedList.add(new Sample(array_sort.get(i).getcName(),array_sort.get(i).getcCode()));
						}
					}
				}
				listview.setAdapter(new CustomAlertAdapter(LoginActivity.this, sortedList));
			}
		});
		myDialog.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
		myalertDialog=myDialog.show(); 
	}

	private class CustomAlertAdapter extends BaseAdapter{

		Context ctx=null;
		ArrayList<Sample> listarray=null;
		private LayoutInflater mInflater=null;
		public CustomAlertAdapter(Activity activty, ArrayList<Sample> list)
		{
			this.ctx=activty;
			mInflater = activty.getLayoutInflater();
			this.listarray=list;
		}
		@Override
		public int getCount() {

			return listarray.size();
		}

		@Override
		public Object getItem(int arg0) {
			return listarray.get(arg0);
		}

		@Override
		public long getItemId(int arg0) {
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup arg2) {       
			final ViewHolder holder;
			if (convertView == null ) {
				holder = new ViewHolder();
				convertView = mInflater.inflate(R.layout.row_layout, null);

				holder.countryname = (TextView) convertView.findViewById(R.id.textView2);
				holder.codename = (TextView) convertView.findViewById(R.id.code);
				convertView.setTag(holder);
			}
			else {
				holder = (ViewHolder) convertView.getTag();
			}

			Sample datavalue=listarray.get(position);
			holder.countryname.setText(datavalue.getcName());
			holder.codename.setText("("+datavalue.getcCode()+")");
			return convertView;
		}


	}
	private static class ViewHolder {
		TextView countryname,codename;
	}


	public void showAlertDialog(Context context, String title, String message,
			Boolean status) {
		AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
		
		alertDialog.setCancelable(false);

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
				LoginActivity.this.finish();
			}
		});
		// Showing Alert Message
		alertDialog.show();
	}
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		try{
			GCMRegistrar.onDestroy(getApplicationContext());
		}
		catch(Exception e){
			e.printStackTrace();
		}
		super.onDestroy();
	}
	private void registerGCMId(){
		GCMRegistrar.register(getApplicationContext(), SENDER_ID);
		GCMRegistrar.setRegisteredOnServer(getApplicationContext(), true);
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch(v.getId()){
		case R.id.loginButton:
			if(!cDetector.isConnectingToInternet()){
				alert.showAlertDialog(LoginActivity.this,
						"Internet Connection Error",
						"Please enable your Internet connection", false);
				return;
				
			}
			else{				 
				code = countryCodeEdit.getText().toString();
				username = countryCode + mobileNumber.getText().toString();
				if(code.equals(null) || code.equals("") ){

					alert.showAlertDialog(LoginActivity.this,
							"Login Error",
							"Please select the valid Country Code", false);
				}
				else if(username.equals(null) || username.equals("") ){

					alert.showAlertDialog(LoginActivity.this,
							"Login Error",
							"Please enter your valid mobile number", false);
				}
				else{
					editor=sharedPref.edit();
					editor.putBoolean("SKIP", false);
					editor.commit();
					new GetExistingUser().execute(username);
				}
			}
			break;
		case R.id.skipButton:
			Intent in=new Intent(LoginActivity.this,ReminderMainActivity.class);
			editor=sharedPref.edit();
			editor.putBoolean("SKIP", true);
			editor.commit();
			finish();
			startActivity(in);
			break;
		}
	}
	private class GetExistingUser extends AsyncTask<String, Void, Void> {

		boolean existingUser=false,error=false, oschange=false;
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			pDialog = new ProgressDialog(LoginActivity.this);
			pDialog.setTitle("Connecting");
			pDialog.setMessage("Please Wait...");
			pDialog.setCancelable(false);
			pDialog.show();
		}

		@Override
		protected Void doInBackground(String... arg) {
			String newUser = arg[0];

			// Preparing get data params
			ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("username", newUser));
			params.add(new BasicNameValuePair("reg_id", registrationId));
			params.add(new BasicNameValuePair("device_id", deviceID));
			params.add(new BasicNameValuePair("device_identity", "0"));
			params.add(new BasicNameValuePair("country_code", countryCode));

			ServiceHandler jsonParser = new ServiceHandler();
			try{
				String json = jsonParser.makeServiceCall(ServiceHandler.URL_EXISTING_USER, ServiceHandler.GET,params);

				Log.v("Response: ", "> " + json);
				System.out.println("Existing User ARRAY IS="+json+"length is="+json.length());
				if(!json.equals("error")|| json!=null){
					//existingUser=true;

					try {
						JSONObject obj=new JSONObject(json);
						String response=obj.getString("succ");
						//System.out.println("Array ="+number+"deviceId="+array.getString(1)+"date="+array.getString(2));
						if(response.equals("1")){
							existingUser=true;
						}
						else if(response.equals("3")){
							existingUser=false;	
						}
						else if(response.equals("2")){
							oschange=true;
							existingUser=true;
						}
						else if(response.equals("0")){
							error=true;
						}

					} catch (JSONException e) {
						e.printStackTrace();
					}       
				}

			}catch(Exception e){
				e.printStackTrace();
				pDialog.dismiss();
				error=true;
				runOnUiThread(new Runnable() {

					@Override
					public void run() {
						// TODO Auto-generated method stub
						//Toast.makeText(mContext, "Something wrong, Please try later", Toast.LENGTH_SHORT).show();		
					}
				});
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			if (pDialog.isShowing())
				pDialog.dismiss();
			if(!error){
				if (!existingUser) {
					
						runOnUiThread(new Runnable() {
							@Override
							public void run() {
								// fetching all categories
								numberValidationAlert();
							}
						});					
				}
				else{
					if(oschange){
						runOnUiThread(new Runnable() {
							@Override
							public void run() {
								// fetching all categories
								showOsChangeAlert();
							}
						});
					}
					else{
						new UpdateUser().execute(username);
					}
				}
			}
			else
				Toast.makeText(mContext, "Something wrong", Toast.LENGTH_SHORT).show();
		}

	}
	private class AddNewUser extends AsyncTask<String, Void, Void> {

		boolean isNewCategoryCreated = false,err=false;
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			pDialog = new ProgressDialog(LoginActivity.this);
			pDialog.setTitle("Registering");
			pDialog.setMessage("Please Wait...");
			pDialog.setCancelable(false);
			pDialog.show();

			if(registrationId.equals("")|| registrationId.equals(null)){
				registrationId = GCMRegistrar.getRegistrationId(LoginActivity.this);
				//	Toast.makeText(getApplicationContext(), "RegId="+registrationId, Toast.LENGTH_SHORT).show();
			}
		}

		@Override
		protected Void doInBackground(String... arg) {


			String newUser = arg[0];
			String deviceId = arg[1];
			// Preparing post params

			ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("username", newUser));
			params.add(new BasicNameValuePair("reg_id", registrationId));
			params.add(new BasicNameValuePair("device_id", deviceId));
			params.add(new BasicNameValuePair("device_identity", "0"));
			params.add(new BasicNameValuePair("country_code", countryCode));

			ServiceHandler serviceClient = new ServiceHandler();
			try{
				String json = serviceClient.makeServiceCall(ServiceHandler.URL_NEW_USER,
						ServiceHandler.GET, params);
				if(!json.equals("error")){

					Log.d("Create User Response: ", "> " + json);
					System.out.println("Insert User Response is="+json);
					System.out.println("User Saved Info:: username-:"+newUser+"regId-:"+registrationId+"deviceId-:"+deviceId);

				}
				else
					err=true;
			}
			catch(Exception e){
				e.printStackTrace();
				pDialog.dismiss();
				runOnUiThread(new Runnable() {

					@Override
					public void run() {
						// TODO Auto-generated method stub
						// Toast.makeText(mContext, "Something wrong, Please try later", Toast.LENGTH_SHORT).show();		
					}
				});
				err=true;
			}
			return null;
		}

		@Override 
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			if (pDialog.isShowing()){
				pDialog.dismiss();
				if(!err){					 
					saveSharedPrefCredentials();
				}
				else{
					//Toast.makeText(mContext, "Something wrong", Toast.LENGTH_SHORT).show();
				}
			}
		}

	}
	private class UpdateUser extends AsyncTask<String, Void, Void> {

		boolean err=false;

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			pDialog = new ProgressDialog(LoginActivity.this);

			pDialog.setMessage("Please Wait Updating..");
			pDialog.setCancelable(false);
			pDialog.show();

			if(registrationId.equals("")|| registrationId.equals(null)){
				registrationId = GCMRegistrar.getRegistrationId(LoginActivity.this);
				//	Toast.makeText(getApplicationContext(), "RegId="+registrationId, Toast.LENGTH_SHORT).show();
			}
		}

		@Override
		protected Void doInBackground(String... arg) {
			String newUser = arg[0];
			ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("username", newUser));
			params.add(new BasicNameValuePair("regid", registrationId));
			params.add(new BasicNameValuePair("device_identity", "0"));


			ServiceHandler serviceClient = new ServiceHandler();
			try{
				String json = serviceClient.makeServiceCall(ServiceHandler.URL_UPDATE_USER,
						ServiceHandler.GET, params);
				if(!json.equals("error")){
					Log.d("Create User Response: ", "> " + json);
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
			if (pDialog.isShowing()){
				pDialog.dismiss();
				if(!err){					
					Intent in=new Intent(LoginActivity.this,ReminderMainActivity.class);
					editor=sharedPref.edit();
					editor.putBoolean("IS_LOGIN", true);
					editor.putString("USERNAME", username);
					editor.putBoolean("IS_VALIDATION", false);
					editor.putString("COUNTRY_CODE", countryCode);
					editor.commit();
					finish();
					startActivity(in);
				}
				else{
					//Toast.makeText(mContext, "Something wrong", Toast.LENGTH_SHORT).show();
				}
			}
		}

	}
	
	public void showOsChangeAlert() {
		new AlertDialog.Builder(LoginActivity.this)
		.setTitle("OS Change Alert")
		.setMessage("As You Are About To Change The OS Platform, Your Account With The Previous Platfrom Will Be Deactivated.")

		.setPositiveButton("OK",
				new DialogInterface.OnClickListener() {

			public void onClick(DialogInterface dialog, int id) {
				//showToast("Thank you! You're awesome too!");
				try{
					new UpdateUser().execute(username);
				}
				catch(Exception e){
					e.printStackTrace();
				}
				dialog.cancel();
			}			
		}).show(); 
	}
	
	public void numberValidationAlert() {
		new AlertDialog.Builder(LoginActivity.this)
		.setTitle("OTP Alert")
		.setMessage("Are you sure for the number you have entered? We will send you one OTP for mobile number validation")

		.setPositiveButton("Done",
				new DialogInterface.OnClickListener() {

			public void onClick(DialogInterface dialog, int id) {
				//showToast("Thank you! You're awesome too!");
				try{
					new AddNewUser().execute(username,deviceID);
				}
				catch(Exception e){
					e.printStackTrace();
				}
				dialog.cancel();

			}
		})
		.setNegativeButton("Edit", new DialogInterface.OnClickListener() {

			public void onClick(DialogInterface dialog, int id) {
				//showToast("Mike is not awesome for you. :(");
				dialog.cancel();
			}
		}).show(); 
	}

	private void saveSharedPrefCredentials(){
		Intent in=new Intent(LoginActivity.this,NumberValidationActivity.class);
		editor=sharedPref.edit();
		editor.putString("COUNTRY_CODE", countryCode);
		editor.putBoolean("IS_VALIDATION", true);
		editor.putString("USERNAME", username);
		editor.commit();
		finish();
		startActivity(in);
	}

	class Sample{
		private String cName;
		private String cCode;

		public Sample(String name,String code){
			this.cName=name;
			this.cCode=code;
		}

		public String getcName() {
			return cName;
		}

		public void setcName(String cName) {
			this.cName = cName;
		}

		public String getcCode() {
			return cCode;
		}

		public void setcCode(String cCode) {
			this.cCode = cCode;
		} 

	}


	private void loadConunryList(){
		array_sort=new ArrayList<Sample> ();
		//country list
		array_sort.add(new Sample("Afghanistan", "+93"));
		array_sort.add(new Sample("Albania", "+355"));
		array_sort.add(new Sample("Algeria", "+213"));
		array_sort.add(new Sample("American Samoa", "+1(684)"));
		array_sort.add(new Sample("Andorra", "+376"));
		array_sort.add(new Sample("Angola", "+244"));
		array_sort.add(new Sample("Anguilla", "+1(264)"));
		array_sort.add(new Sample("Antigua", "+1(268)"));
		array_sort.add(new Sample("Argentina", "+54"));
		array_sort.add(new Sample("Armenia", "+374"));
		array_sort.add(new Sample("Aruba", "+297"));
		array_sort.add(new Sample("Ascension", "+247"));
		array_sort.add(new Sample("Australia", "+61"));
		array_sort.add(new Sample("Austria", "+43"));
		array_sort.add(new Sample("Azerbaijan", "+994"));
		array_sort.add(new Sample("Bahamas", "+1(242)"));
		array_sort.add(new Sample("Bahrain", "+973"));
		array_sort.add(new Sample("Bangladesh", "+880"));
		array_sort.add(new Sample("Barbados", "+1(246)"));
		array_sort.add(new Sample("Belarus", "+375"));
		array_sort.add(new Sample("Belgium", "+32"));
		array_sort.add(new Sample("Belize", "+501"));
		array_sort.add(new Sample("Benin", "+299"));
		array_sort.add(new Sample("Bermuda", "+1(441)"));
		array_sort.add(new Sample("Bhutan", "+975"));
		array_sort.add(new Sample("Bolivia", "+591"));
		array_sort.add(new Sample("Bonaire", "+599"));
		array_sort.add(new Sample("Botswana", "+267"));
		array_sort.add(new Sample("Brazil", "+55"));
		array_sort.add(new Sample("Brunei Darussalm", "+673"));
		array_sort.add(new Sample("Bulgaria", "+359"));
		array_sort.add(new Sample("Burkina Faso", "+226"));
		array_sort.add(new Sample("Burundi", "+257"));
		array_sort.add(new Sample("Cambodia", "+855"));
		array_sort.add(new Sample("Cameroon", "+237"));
		array_sort.add(new Sample("Canada", "+1"));
		array_sort.add(new Sample("Cape Verde", "+238"));
		array_sort.add(new Sample("Caribbean Nations", "+1"));
		array_sort.add(new Sample("Cayman Islands", "+1(345)"));
		array_sort.add(new Sample("Chad", "+235"));
		array_sort.add(new Sample("Chile", "+56"));
		array_sort.add(new Sample("China", "+86"));
		array_sort.add(new Sample("Colombia", "+57"));
		array_sort.add(new Sample("Comoros", "+269"));
		array_sort.add(new Sample("Congo", "+242"));
		array_sort.add(new Sample("Cook Islands", "+682"));
		array_sort.add(new Sample("Costa Rica", "+506"));
		array_sort.add(new Sample("Croatia", "+385"));
		array_sort.add(new Sample("Cuba", "+53"));
		array_sort.add(new Sample("Curaçao", "+57"));
		array_sort.add(new Sample("Cyprus", "+357"));
		array_sort.add(new Sample("Czech Republic", "+420"));
		array_sort.add(new Sample("Denmark", "+45"));
		array_sort.add(new Sample("Diego-Garcia", "+246"));
		array_sort.add(new Sample("Djibouti", "+253"));
		array_sort.add(new Sample("Dominica", "+1(767)"));
		array_sort.add(new Sample("East Timor", "+670"));
		array_sort.add(new Sample("Ecuador", "+593"));
		array_sort.add(new Sample("Egypt", "+20"));
		array_sort.add(new Sample("El Salvador", "+503"));
		array_sort.add(new Sample("Eritrea", "+291"));
		array_sort.add(new Sample("Estonia", "+372"));
		array_sort.add(new Sample("Ethiopia", "+251"));
		array_sort.add(new Sample("Faroe Islands", "+298"));
		array_sort.add(new Sample("Fiji", "+679"));
		array_sort.add(new Sample("Finland", "+358"));
		array_sort.add(new Sample("France", "+33"));
		array_sort.add(new Sample("French Antilles", "+590"));
		array_sort.add(new Sample("French Guiana", "+594"));
		array_sort.add(new Sample("Gabon", "+241"));
		array_sort.add(new Sample("Gambia", "+220"));
		array_sort.add(new Sample("Georgia", "+995"));
		array_sort.add(new Sample("Germany", "+94"));
		array_sort.add(new Sample("Ghana", "+233"));
		array_sort.add(new Sample("Gibraltar", "+350"));
		array_sort.add(new Sample("Greece", "+30"));
		array_sort.add(new Sample("Greenland", "+299"));
		array_sort.add(new Sample("Grenada", "+1(473)"));
		array_sort.add(new Sample("Guadeloupe ", "+590"));
		array_sort.add(new Sample("Guam", "+1(671)"));
		array_sort.add(new Sample("Guatemala", "+502"));
		array_sort.add(new Sample("Guinea", "+224"));
		array_sort.add(new Sample("Guinea-Bissau", "+245"));
		array_sort.add(new Sample("Guyana", "+592"));
		array_sort.add(new Sample("Haiti", "+509"));
		array_sort.add(new Sample("Honduras", "+504"));
		array_sort.add(new Sample("Hong Kong", "+852"));
		array_sort.add(new Sample("Hungary", "+36"));
		array_sort.add(new Sample("Iceland", "+354"));
		array_sort.add(new Sample("India", "+91"));
		array_sort.add(new Sample("Indonesia", "+62"));
		array_sort.add(new Sample("Inmarsat", "+870"));
		array_sort.add(new Sample("Iran", "+98"));
		array_sort.add(new Sample("Iraq", "+964"));
		array_sort.add(new Sample("Ireland", "+353"));
		array_sort.add(new Sample("Italy", "+39"));
		array_sort.add(new Sample("Japan", "+81"));
		array_sort.add(new Sample("Jordan", "+962"));
		array_sort.add(new Sample("Kazakhstan", "+7"));
		array_sort.add(new Sample("Kenya", "+254"));
		array_sort.add(new Sample("Liberia", "+231"));
		array_sort.add(new Sample("Libya", "+218"));
		array_sort.add(new Sample("Macao", "+853"));
		array_sort.add(new Sample("Malaysia", "+60"));
		array_sort.add(new Sample("Maldives", "+960"));
		array_sort.add(new Sample("Malta", "+356"));
		array_sort.add(new Sample("Mexico", "+52"));
		array_sort.add(new Sample("Mongolia", "+976"));
		array_sort.add(new Sample("Myanmar", "+95"));
		array_sort.add(new Sample("Nepal", "+977"));
		array_sort.add(new Sample("Netherlands", "+31"));
		array_sort.add(new Sample("New Zealand", "+64"));
		array_sort.add(new Sample("North Korea", "+850"));
		array_sort.add(new Sample("Norway", "+47"));
		array_sort.add(new Sample("Oman", "+968"));
		array_sort.add(new Sample("Pakistan", "+92"));
		array_sort.add(new Sample("Peru", "+51"));
		array_sort.add(new Sample("Philippines", "+63"));
		array_sort.add(new Sample("Poland", "+48"));
		array_sort.add(new Sample("Qatar", "+974"));
		array_sort.add(new Sample("Romania", "+40"));
		array_sort.add(new Sample("Russia", "+7"));
		array_sort.add(new Sample("Samoa", "+685"));
		array_sort.add(new Sample("San Marino", "+378"));
		array_sort.add(new Sample("Saudi Arabia", "+966"));
		array_sort.add(new Sample("Singapore", "+65"));
		array_sort.add(new Sample("Somalia", "+252"));
		array_sort.add(new Sample("South Africa", "+27"));
		array_sort.add(new Sample("South Korea", "+82"));
		array_sort.add(new Sample("South Sudan ", "+211"));
		array_sort.add(new Sample("Spain", "+34"));
		array_sort.add(new Sample("Shri Lanka", "+94"));
		array_sort.add(new Sample("Sudan", "+249"));
		array_sort.add(new Sample("Swaziland", "+268"));
		array_sort.add(new Sample("Switzerland", "+41"));
		array_sort.add(new Sample("Tajikistan", "+992"));  
		array_sort.add(new Sample("Thailand", "+66"));
		array_sort.add(new Sample("Tonga", "+676"));
		array_sort.add(new Sample("Turkey", "+90"));
		array_sort.add(new Sample("Uganda", "+256"));
		array_sort.add(new Sample("United Arab Emirates", "+971"));
		array_sort.add(new Sample("United Kingdom", "+44"));
		array_sort.add(new Sample("United States", "+1"));
		array_sort.add(new Sample("Vatican City", "+39, +379"));
		array_sort.add(new Sample("Yemen", "+967"));
		array_sort.add(new Sample("Zambia", "+260"));
		array_sort.add(new Sample("Zimbabwe", "+263"));
	}
}
