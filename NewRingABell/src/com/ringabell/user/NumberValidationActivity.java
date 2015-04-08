package com.ringabell.user;

import java.util.ArrayList;
import java.util.Random;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;import org.json.JSONObject;

import com.ringabell.reminder.ReminderMainActivity;
import com.ringabell.serverdb.ServiceHandler;
import com.ringabell.utils.AlertDialogManager;
import com.ringabell.utils.ConnectionDetector;
import com.share2people.ringabell.R;
import com.share2people.ringabell.R.color;
import com.share2people.ringabell.R.id;
import com.share2people.ringabell.R.layout;
import com.share2people.ringabell.R.string;

import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;

import android.telephony.SmsManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class NumberValidationActivity extends Activity implements OnClickListener{
	
	EditText otpEditText,mobileEditText;
	Button nextButton,resendButton;
	String otpNumber="",username="",existOtp="",otp="",code="";
	SharedPreferences sharedPreferences;
	SharedPreferences.Editor editor;
	ConnectionDetector cDetector;
	private ProgressDialog pDialog;
	AlertDialogManager alert = new AlertDialogManager();
	private Context mContext;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_number_validation);	
		
		getActionBar().setTitle(getString(R.string.app_name));
		getActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.login_action_bar)));

	
		mContext=(Context) getApplicationContext();
		otpEditText=(EditText) findViewById(R.id.otpEdit);
		mobileEditText=(EditText) findViewById(R.id.mobileNumberEdit);		
		
		resendButton=(Button) findViewById(R.id.resendButton);
		nextButton=(Button) findViewById(R.id.nextButton);
		
		resendButton.setOnClickListener(this);
		nextButton.setOnClickListener(this);
		
		cDetector = new ConnectionDetector(getApplicationContext());
				
	}
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		sharedPreferences=getSharedPreferences("LAST_LOGIN", Activity.MODE_PRIVATE);		
		username=sharedPreferences.getString("USERNAME", null);
		code = sharedPreferences.getString("COUNTRY_CODE", "");	
		mobileEditText.setText(username);
	}
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		
		if(v.getId()==R.id.nextButton){
			otpNumber=otpEditText.getText().toString().trim();
			if(otpNumber.equals("")|| otpNumber==null){
				
				Toast.makeText(getApplicationContext(), "Please Enter Valid OTP", Toast.LENGTH_SHORT).show();				
			}
			else {
				new VerifyUser().execute(otpNumber);

				
			}
		}
		if(v.getId()==R.id.resendButton){
			if(!cDetector.isConnectingToInternet()){
				alert.showAlertDialog(NumberValidationActivity.this,
						"Internet Connection Error",
						"Please enable your Internet connection", false);
				return;
			}	
			else
				numberValidationAlert();
		}
		
	}
	public void numberValidationAlert() {
	    new AlertDialog.Builder(NumberValidationActivity.this)
	            .setTitle("OTP Alert")
	            .setMessage("Do u want to resend code?")
	            
	            .setPositiveButton("Yes",
	                    new DialogInterface.OnClickListener() {
	                
	                        public void onClick(DialogInterface dialog, int id) {
	                            //showToast("Thank you! You're awesome too!");
	                        	try{
	                        	//sendMessage(username);
	                        	//	 number = code + username;
	            				//	 message = generateOtp();
	            					 new SendMessage().execute();
	                        	
	                        	}
	                        	catch(Exception e){
	                        		e.printStackTrace();
	                        	}
	                            dialog.cancel();
	                        }
	                    })
	            .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
	                
	                public void onClick(DialogInterface dialog, int id) {
	                    //showToast("Mike is not awesome for you. :(");
	                    dialog.cancel();
	                }
	            }).show();
	}
	 private void sendMessage(String mobileNumber){
		
		 String message="This is One Time Password "+generateOtp()+" sent by RingABell smartphone reminder app";
			SmsManager smsManager = SmsManager.getDefault();
			smsManager.sendTextMessage(mobileNumber, null, message, null, null);
	 }
	 private String generateOtp(){
		//generate random User Id
            char[] chars = "1234567890".toCharArray();
            StringBuilder sb = new StringBuilder();
            Random random = new Random();
            for (int i = 0; i < 4; i++) {
                char c = chars[random.nextInt(chars.length)];
                sb.append(c);
            }
            String result = sb.toString();
            otp=result;
            return result;	            
	 }
	
	 
	//Sending msg to non-app user
			private class SendMessage extends AsyncTask<Void, Void, Void> {

				boolean err=false,responseFlag=false;

				@Override
				protected void onPreExecute() {
					super.onPreExecute();
					 pDialog = new ProgressDialog(NumberValidationActivity.this);
					 pDialog.setTitle("Sending");
					 pDialog.setMessage("Please Wait...");
					 pDialog.setCancelable(false);
					 pDialog.show();
				}

				@Override
				protected Void doInBackground(Void... arg) {

					// Preparing post params

					ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();

					params.add(new BasicNameValuePair("username", username));
					params.add(new BasicNameValuePair("country_code", sharedPreferences.getString("COUNTRY_CODE", "")));
					params.add(new BasicNameValuePair("device_identity", "0"));
					

					ServiceHandler serviceClient = new ServiceHandler();
					try{
						String json = serviceClient.makeServiceCall(ServiceHandler.URL_SEND_MESSAGE,
								ServiceHandler.GET, params);
						if(!json.equals("error")){
							//Log.d("Create User Response: ", "> " + json);
							// System.out.println("Insert User Response is="+json);

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
					if (pDialog.isShowing())
						 pDialog.dismiss();
					 if(err){
						 //Toast.makeText(mContext, "Something wrong", Toast.LENGTH_SHORT).show(); 
					 }						
				}
			}

			private class VerifyUser extends AsyncTask<String, Void, Void> {

				boolean err=false,responseMsg=false;

				@Override
				protected void onPreExecute() {
					super.onPreExecute();
					 pDialog = new ProgressDialog(NumberValidationActivity.this);
					 pDialog.setTitle("Verifying User");
					 pDialog.setMessage("Please Wait...");
					 pDialog.setCancelable(false);
					 pDialog.show();
				}

				@Override
				protected Void doInBackground(String... arg) {

					String code= arg[0];
					// Preparing post params

					ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();

					params.add(new BasicNameValuePair("username", username));
					params.add(new BasicNameValuePair("country_code", sharedPreferences.getString("COUNTRY_CODE", "")));
					params.add(new BasicNameValuePair("verification_code",code));
					

					ServiceHandler serviceClient = new ServiceHandler();
					try{
						String json = serviceClient.makeServiceCall(ServiceHandler.URL_VERIFY_USER,
								ServiceHandler.GET, params);
						if(!json.equals("error")){
							
							JSONObject obj=new JSONObject(json);
							 String response=obj.getString("succ");
							 
							 if(response.equals("1"))
								 responseMsg=true;
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
					 if (pDialog.isShowing())
						 pDialog.dismiss();
					 if(!err){
						 if (!responseMsg) {
							 Toast.makeText(mContext, "Code Not Matched", Toast.LENGTH_SHORT).show();	 
						 }
						 else{
							 Intent intent=new Intent(NumberValidationActivity.this,ReminderMainActivity.class);
								Toast.makeText(getApplicationContext(), "You have successfully registered", Toast.LENGTH_SHORT).show();
								 editor=sharedPreferences.edit();
								 editor.putBoolean("IS_LOGIN", true);
								 editor.putString("USERNAME", username);
								 editor.putBoolean("IS_VALIDATION", false);
								 // editor.putString("CODE", code);
								 editor.commit();
								finish();
								startActivity(intent);
						 }
						 
					 }
					 else{
						 //Toast.makeText(mContext, "Something wrong", Toast.LENGTH_SHORT).show();
					 }
				}

			}


}
