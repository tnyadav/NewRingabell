package com.share2people.ringabell;

import java.util.ArrayList;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import com.ringabell.serverdb.ServiceHandler;
import com.ringabell.utils.AlertDialogManager;
import com.ringabell.utils.ConnectionDetector;
import com.share2people.ringabell.R;

import android.os.AsyncTask;
import android.os.Bundle;
import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.drawable.ColorDrawable;

import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.Toast;
import android.widget.RatingBar.OnRatingBarChangeListener;

public class RatingActivity extends Activity implements OnClickListener{

	private RatingBar ratingBar;
	private Button submitButton,cancelButton;
	private EditText reviewEdit;
	private String reviewText="",ratingBarValue="";
	AlertDialogManager alert;
	ConnectionDetector cDetector;
	SharedPreferences sharePref;
	SharedPreferences.Editor editor;
	ProgressDialog pDialog;
	//private static final String URL_RATING="http://tech-sync.com/ringabell/insert_rating.php";
	private Context mContext;
	boolean isRating=false;
	boolean skipFlag=false;


	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_rating);

		sharePref=getSharedPreferences("LAST_LOGIN", Activity.MODE_PRIVATE);
		isRating=sharePref.getBoolean("IS_RATING", false);
		skipFlag=sharePref.getBoolean("SKIP", false);

		getActionBar().setTitle(getString(R.string.rating));
		getActionBar().setDisplayHomeAsUpEnabled(true);
		getActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.rating_action_bar)));


		//ActionBar actionBar=getActionBar();
		//actionBar.setDisplayHomeAsUpEnabled(true);
		//actionBar.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.rating_action_bar)));

		alert=new AlertDialogManager();
		mContext=(Context) getApplicationContext();

		ratingBar = (RatingBar) findViewById(R.id.ratingBar1);
		submitButton=(Button) findViewById(R.id.submitButton);
		cancelButton=(Button) findViewById(R.id.cancelButton);
		reviewEdit=(EditText) findViewById(R.id.reviewEditText);
		cDetector = new ConnectionDetector(getApplicationContext());

		submitButton.setOnClickListener(this);
		cancelButton.setOnClickListener(this);

		addListenerOnRatingBar();

	}

	public void addListenerOnRatingBar() {

		//ratingBar = (RatingBar) findViewById(R.id.ratingBar1);


		//if rating value is changed,
		//display the current rating value in the result (textview) automatically
		ratingBar.setOnRatingBarChangeListener(new OnRatingBarChangeListener() {
			public void onRatingChanged(RatingBar ratingBar, float rating,
					boolean fromUser) {

				ratingBarValue=	String.valueOf(rating);

			}
		});
	}

	@Override
	public void onClick(View view) {
		// TODO Auto-generated method stub
		switch(view.getId()){
		case R.id.submitButton:
			reviewText=reviewEdit.getText().toString();
			ratingBarValue=String.valueOf(ratingBar.getRating());

			if(skipFlag){
				//Toast.makeText(mContext, "Registration Required.", Toast.LENGTH_SHORT).show();
				showAlertDialog(RatingActivity.this,
						"Registration Required",
						"Please Register To Rate The App.", false);
			}
			else{

				if(reviewText.equals("")|| reviewText.equals(null)){
					Toast toast=Toast.makeText(getApplicationContext(), "Please Write Review", Toast.LENGTH_SHORT);
					toast.setGravity(Gravity.CENTER, 0, 0);
					toast.show();
				}
				else
					if(!cDetector.isConnectingToInternet()){
						alert.showAlertDialog(RatingActivity.this,
								"Internet Connection Error",
								"Please enable your Internet Connection to Rate App", false);
						return;
					}

					else{
						/*System.out.println("TExt::"+reviewText);
				System.out.println("Rating::"+String.valueOf(ratingBar.getRating()));
				Toast.makeText(getApplicationContext(), "Review::"+String.valueOf(ratingBar.getRating())+"Text::"+reviewText, Toast.LENGTH_SHORT).show();*/
						new UploadRateOnServer().execute(sharePref.getString("USERNAME", null),ratingBarValue,reviewText);
					}
			}

			break;
		case R.id.cancelButton:
			finish();
			break;
		}
	}
	
	public void showAlertDialog(Context context, String title, String message,
			Boolean status) {
		AlertDialog alertDialog = new AlertDialog.Builder(context).create();

		// Setting Dialog Title
		alertDialog.setTitle(title);

		// Setting Dialog Message
		alertDialog.setMessage(message);

		if(status != null)
			// Setting alert dialog icon
			alertDialog.setIcon((status) ? R.drawable.success : R.drawable.fail);

		// Setting OK Button
		alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				///RatingActivity.this.finish();
			}
		});

		// Showing Alert Message
		alertDialog.show();
	}
	
	private class UploadRateOnServer extends AsyncTask<String, Void, Void> {

		boolean err=false;

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			pDialog = new ProgressDialog(RatingActivity.this);
			pDialog.setMessage("Please Wait...");
			pDialog.setCancelable(false);
			pDialog.show();

		}

		@Override
		protected Void doInBackground(String... arg) {


			String user = arg[0];
			String rating=arg[1];
			String review=arg[2];

			// Preparing post params

			ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("username", user));
			params.add(new BasicNameValuePair("rating", rating));
			params.add(new BasicNameValuePair("review", review));


			ServiceHandler serviceClient = new ServiceHandler();
			try{
				String json = serviceClient.makeServiceCall(ServiceHandler.URL_RATING,
						ServiceHandler.GET, params);
				if(!json.equals("error")){

					Log.d("Insert Rating: ", "> " + json);
					System.out.println("Insert Rating is="+json);

				}
				else
					err=true;
			}
			catch(Exception e){
				e.printStackTrace();
				pDialog.dismiss();
				Toast.makeText(mContext, "Something wrong with network, Please try later", Toast.LENGTH_SHORT).show();
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
					editor=sharePref.edit();
					editor.putBoolean("IS_RATING", true);
					editor.commit();
					finish();
				}
				else
					Toast.makeText(mContext, "Something wrong", Toast.LENGTH_SHORT).show();
			}
		}

	}

}
