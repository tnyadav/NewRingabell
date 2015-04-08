package com.share2people.ringabell;


import android.os.Bundle;
import android.os.Handler;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;

import com.ringabell.reminder.ReminderMainActivity;
import com.ringabell.user.LoginActivity;
import com.ringabell.user.NumberValidationActivity;
import com.ringabell.utils.ConnectionDetector;
import com.share2people.ringabell.R;

public class SplashActivity extends Activity {	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_splash);		
        
         Handler handler = new Handler();
			handler.postDelayed(new Runnable() {
				public void run() {

					SharedPreferences sharedPref = getSharedPreferences("LAST_LOGIN", Activity.MODE_PRIVATE);
					
					boolean isValidation = sharedPref.getBoolean("IS_VALIDATION", false);
					boolean isLogin = sharedPref.getBoolean("IS_LOGIN", false);
						Intent intent;
					if(isValidation){
						intent=new Intent(SplashActivity.this,NumberValidationActivity.class);
					
					}

					else if(isLogin){

						intent=new Intent(SplashActivity.this,ReminderMainActivity.class);
					
					}else {
						intent = new Intent(SplashActivity.this,LoginActivity.class);
	                   
					}
					 finish();
	                    startActivity(intent);
					
				}}, 2000);
	}	
}
