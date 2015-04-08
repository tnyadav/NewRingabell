package com.share2people.ringabell;

import java.util.ArrayList;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;

import com.ringabell.localdb.GroupsLocalDB;
import com.ringabell.serverdb.ServiceHandler;
import com.ringabell.utils.ConnectionDetector;
import com.share2people.ringabell.R;

import android.os.AsyncTask;
import android.os.Bundle;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.drawable.ColorDrawable;

import android.util.Log;
import android.view.Menu;
import android.webkit.WebView;

public class AboutUsActivity extends Activity {
	private WebView webview;
	private String content = "";
	ConnectionDetector cDetector;


	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_aboutus);
		webview=(WebView) findViewById(R.id.webview);
		cDetector = new ConnectionDetector(getApplicationContext());		 


		getActionBar().setTitle(getString(R.string.about_us));
		getActionBar().setDisplayHomeAsUpEnabled(true);
		getActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.rating_action_bar)));

		String aboutus = "Ring-a-bell lets you set reminders in other person's calendar that rings off at the time specified by you and reminds the other person to complete the task.";
		String profile = "Share2people is driven by a group of tech savvy enthusiasts who believes in sharing great ideas. So if you think you got something cooking in your mind then do share with us. We would be happy to build it up for you.";
		String email = "share2people@outlook.com";
		String website = "www.share2people.com";
		String htmldata = "<html><body><p style='color : #e36055'>About Us</p><p>" + "Ring-a-bell lets you set reminders in other person's calendar that rings off at the time specified by you and reminds the other person to complete the task." +
				"</p><p style='color : #e36055'>Company Profile</p><p>" +  "Share2people is driven by a group of tech savvy enthusiasts who believes in sharing great ideas. So if you think you got something cooking in your mind then do share with us. We would be happy to build it up for you." +
				"</p><p style='color : #e36055'>Email Id : <span style='color:blue;text-decoration:underline'>" + "share2people@outlook.com"  +
				"</span></p><p style='color : #e36055'>Website : <a href ='http://www.share2people.com'>" + "www.share2people.com" +
				"</a></p></body></html>";
		

		if(cDetector.isConnectingToInternet()){
			new RetrieveAboutUs().execute();
		}
		else{
			webview.loadData(htmldata, "text/html", null);
			webview.getSettings().setBuiltInZoomControls(true);

		}
	}


	private class RetrieveAboutUs extends AsyncTask<Void, Void, Void> {

		boolean err=false;
		String members;
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}

		@Override
		protected Void doInBackground(Void... arg) {

			ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
			//params.add(new BasicNameValuePair("group_id", gid));


			ServiceHandler serviceClient = new ServiceHandler();
			try{
				String json = serviceClient.makeServiceCall(ServiceHandler.URL_RETRIEVE_ABOUTUS,
						ServiceHandler.GET, params);
				System.out.println("Retrieve About Us ="+json);
				if(!json.equals("error")){
					if (json != null && json.length()>3) {

						JSONArray array=new JSONArray(json);

						JSONObject obj=null;
						for(int i=0;i<array.length();i++){
							obj=array.getJSONObject(i);
							content = obj.getString("content");
						}
					}

					Log.d("Retrieve About Us Response: ", "> " + json);
				}
				else
					err=true;
			}
			catch(Exception e){
				e.printStackTrace();
				err=true;
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			if(!err){
				webview.loadData(content, "text/html", null);
				webview.getSettings().setBuiltInZoomControls(true);			}
			else{
				//Toast.makeText(mContext, "Something wrong", Toast.LENGTH_SHORT).show();
			}
			//}
		}
	}

}
