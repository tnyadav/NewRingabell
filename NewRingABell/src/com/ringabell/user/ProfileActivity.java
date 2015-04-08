package com.ringabell.user;



import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.Externalizable;
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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.ByteArrayBuffer;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


import com.ringabell.serverdb.ServiceHandler;
import com.ringabell.utils.ConnectionDetector;
import com.share2people.ringabell.InternalStorageContentProvider;
import com.share2people.ringabell.R;
import com.share2people.ringabell.R.color;
import com.share2people.ringabell.R.drawable;
import com.share2people.ringabell.R.id;
import com.share2people.ringabell.R.layout;
import com.share2people.ringabell.R.menu;
import com.share2people.ringabell.R.string;


import eu.janmuller.android.simplecropimage.CropImage;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;

import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;


public class ProfileActivity extends Activity implements OnClickListener{

	private ImageView imageWrite,nameWrite,profileImage;
	private TextView numberTextView;
	private EditText nameTextView,userstatusTextView;
	static ActionBar actionbar;
	private static final int NAME_REQUEST_CODE = 100;
	public static final int IMAGE_PICK_CAMERA = 200;
	public static final int IMAGE_PICK_GALLERY = 300;
	public static final int REQUEST_CODE_CROP_IMAGE = 400;
	String name="";
	public static  String TEMP_PHOTO_FILE_NAME = "temp_photo.jpg";
	public String PIC_FILE_NAME = String.valueOf(System.currentTimeMillis()) + ".jpg";

	public static final String IMAGE_DIRECTORY_NAME="RingABell"+File.separator+"ProfilePicture";

	String localImagePath = "1";
	private String imagepath="",userstatus="",number="";
	boolean skipFlag=false;

	private File  mFileTemp;
	private ProgressDialog pDialog,dialog;
	private SharedPreferences sharedPref;
	SharedPreferences.Editor editor;
	private Context mContext;
	private int serverResponseCode = 0;
	ConnectionDetector cDetector;
	ProgressBar pBar;

	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_profile);


		getActionBar().setTitle(getString(R.string.profile));
		getActionBar().setDisplayHomeAsUpEnabled(true);
		getActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.profile_main_action_bar)));


		mContext=(Context) getApplicationContext();
		sharedPref=getSharedPreferences("LAST_LOGIN", Activity.MODE_PRIVATE);
		number=sharedPref.getString("USERNAME", null);
		skipFlag=sharedPref.getBoolean("SKIP", false);
		name=sharedPref.getString("PROFILE_NAME", getString(R.string.username_text));
		localImagePath=sharedPref.getString("PROFILE_IMAGE_PATH","1");
		userstatus=sharedPref.getString("PROFILE_STATUS", getString(R.string.status_text));

		//imageWrite=(ImageView) findViewById(R.id.imageWrite);
		//	nameWrite=(ImageView) findViewById(R.id.profileWrite);
		profileImage=(ImageView) findViewById(R.id.profileimage);
		nameTextView=(EditText) findViewById(R.id.tname1);
		userstatusTextView=(EditText) findViewById(R.id.statustext);
		numberTextView=(TextView) findViewById(R.id.numbertext);

		cDetector = new ConnectionDetector(getApplicationContext());

		profileImage.setImageResource(R.drawable.single_user);

		numberTextView.setText(number);
		if(!name.equals(null) || !name.equals(""))
			nameTextView.setText(name);
		if(!userstatus.equals(null)||!userstatus.equals(""))
			userstatusTextView.setText(userstatus);
		if(localImagePath!="1"){
			File sourceFile = new File(localImagePath); 
			if (!sourceFile.isFile()) {
				profileImage.setImageResource(R.drawable.single_user);
				new RetrieveUserProfile().execute();
			}
			else{
				Bitmap bitmap = BitmapFactory.decodeFile(localImagePath);
				profileImage.setImageBitmap(bitmap);
			}			
		}
		else{
			profileImage.setImageResource(R.drawable.single_user);
			if(!skipFlag){
				if(cDetector.isConnectingToInternet()){
					new RetrieveUserProfile().execute();
				}
			}
		}	

		profileImage.setOnClickListener(this);
	}
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.activity_profile, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Take appropriate action for each action item click

		switch (item.getItemId()) {
		case R.id.create:

			if(skipFlag){
				//Toast.makeText(mContext, "Registration Required.", Toast.LENGTH_SHORT).show();
				showAlertDialog(ProfileActivity.this,
						"Registration Required",
						"Please Register To Create Your Profile.", false);
			}
			else{
				if(!cDetector.isConnectingToInternet()){
					Toast.makeText(mContext, "Please Check Your Internet Connection", Toast.LENGTH_SHORT).show();
				}
				else{
					if (!imagepath.equals("") || !imagepath.equals(null)){
						new AsyncTask<Void, Void, Void>(){

							protected void onPreExecute() {
								dialog =ProgressDialog.show(ProfileActivity.this, "", "Uploading Pic");
								dialog.setCancelable(true);
							};
							@Override
							protected Void doInBackground(Void... params) {
								// TODO Auto-generated method stub
								uploadFile(imagepath);
								return null;
							}
							@Override
							protected void onPostExecute(Void result) {
								// TODO Auto-generated method stub
								super.onPostExecute(result);
								new UpdateUserProfile().execute();
							}
						}.execute();
					}
					else{
						new UpdateUserProfile().execute();
					}
				}
				return true;
			}

		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch(v.getId()){
		case R.id.profileimage:
			showPickImageAlert();
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
				//ProfileActivity.this.finish();
			}
		});

		// Showing Alert Message
		alertDialog.show();
	}


	private void updateProfileUi(String name, String status){
		nameTextView.setText(name);
		userstatusTextView.setText(status);
	}

	private File getOutputMediaFile() {

		// External sdcard location
		File mediaStorageDir = new File(
				Environment.getExternalStorageDirectory() + File.separator+IMAGE_DIRECTORY_NAME);

		// Create the storage directory if it does not exist
		if (!mediaStorageDir.exists()) {
			if (!mediaStorageDir.mkdirs()) {
				Log.d(IMAGE_DIRECTORY_NAME, "Oops! Failed create "
						+ IMAGE_DIRECTORY_NAME + " directory");
				return null;
			}
		}

		// Create a media file name
		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss",
				Locale.getDefault()).format(new Date());
		File mediaFile;

		//mediaFile = new File(mediaStorageDir.getPath() + File.separator
		//+ sharedPref.getString("USERNAME", null)+ ".jpg");

		mediaFile = new File(mediaStorageDir.getPath() + File.separator
				+ PIC_FILE_NAME);

		return mediaFile;
	}

	private class UpdateUserProfile extends AsyncTask<Void, Void, Void> {

		boolean err=false;
		String profilepic="";
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			pDialog = new ProgressDialog(ProfileActivity.this);
			pDialog.setMessage("Updating Profile.....");
			pDialog.setCancelable(false);
			pDialog.show();
			name=nameTextView.getText().toString().trim();
			userstatus=userstatusTextView.getText().toString().trim();


			if(!imagepath.equals("") || !imagepath.equals(null))
				profilepic=ServiceHandler.SERVER_IMAGE_PATH+"/"+PIC_FILE_NAME;
			//profilepic=ServiceHandler.SERVER_IMAGE_PATH+"/"+sharedPref.getString("USERNAME", null)+"12345.jpg";			
		}

		@Override
		protected Void doInBackground(Void... arg) {		

			ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("name", name));
			params.add(new BasicNameValuePair("username", sharedPref.getString("USERNAME", null)));
			params.add(new BasicNameValuePair("ustatus",userstatus));
			params.add(new BasicNameValuePair("profilepic",profilepic));
			params.add(new BasicNameValuePair("status","0"));

			ServiceHandler serviceClient = new ServiceHandler();
			try{
				String json = serviceClient.makeServiceCall(ServiceHandler.URL_UPDATE_PROFILE,
						ServiceHandler.GET, params);
				//  System.out.println("Create Group ="+json);
				if(!json.equals("error")){
					Log.d("Create Group Response: ", "> " + json);
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

					editor=sharedPref.edit();
					editor.putString("PROFILE_NAME", name);
					editor.putString("PROFILE_IMAGE_PATH", getOutputMediaFile().getPath().toString());
					//editor.putString("PROFILE_IMAGE_PATH", Environment.getExternalStorageDirectory().
					//getAbsolutePath() + "/RingABell/" + sharedPref.getString("USERNAME", null)+".jpg");
					editor.putString("PROFILE_STATUS", userstatus);
					editor.commit();



					if(copyFileFromOneDirectoryToAnother(mFileTemp,getOutputMediaFile()))

						deleteFileFromSdCard(imagepath);
					Toast toast=Toast.makeText(getApplicationContext(), "Profile Updated ", Toast.LENGTH_SHORT);
					toast.setGravity(Gravity.CENTER, 0, 0);
					toast.show();
					finish();
				}
				else{
					//Toast.makeText(mContext, "Something wrong", Toast.LENGTH_SHORT).show();
				}
			}
		}

	}  

	private void takePicture() {

		Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

		try {
			Uri mImageCaptureUri = null;
			String state = Environment.getExternalStorageState();
			if (Environment.MEDIA_MOUNTED.equals(state)) {
				mImageCaptureUri = Uri.fromFile(mFileTemp);
			}
			else {				
				mImageCaptureUri = InternalStorageContentProvider.CONTENT_URI;
			}	
			intent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, mImageCaptureUri);
			intent.putExtra("return-data", true);
			startActivityForResult(intent, IMAGE_PICK_CAMERA);
		} catch (ActivityNotFoundException e) {
			Log.d("TAG", "cannot take picture", e);
		}
	}

	private void deleteFileFromSdCard(String path){
		File file= new File(path);
		if(file.exists())
		{
			file.delete();
		}
	}

	private boolean copyFileFromOneDirectoryToAnother(File source,File destination){
		InputStream inStream = null;
		OutputStream outStream = null;

		try{

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


			System.out.println("File is copied successful!");

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

	private void openGallery() {

		Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
		photoPickerIntent.setType("image/*");
		startActivityForResult(photoPickerIntent, IMAGE_PICK_GALLERY);
	}

	private void startCropImage() {

		Intent intent = new Intent(this, CropImage.class);
		intent.putExtra(CropImage.IMAGE_PATH, mFileTemp.getPath());
		intent.putExtra(CropImage.SCALE, true);

		intent.putExtra(CropImage.ASPECT_X, 3);
		intent.putExtra(CropImage.ASPECT_Y, 2);

		startActivityForResult(intent, REQUEST_CODE_CROP_IMAGE);
	}
	public static void copyStream(InputStream input, OutputStream output)
			throws IOException {

		byte[] buffer = new byte[1024];
		int bytesRead;
		while ((bytesRead = input.read(buffer)) != -1) {
			output.write(buffer, 0, bytesRead);
		}
	}
	/**
	 * Checking device has camera hardware or not
	 * */
	@SuppressLint("NewApi")
	private boolean isDeviceSupportCamera() {
		if (getApplicationContext().getPackageManager().hasSystemFeature(
				PackageManager.FEATURE_CAMERA)) {
			// this device has a camera
			return true;
		} else {
			// no camera on this device
			return false;
		}
	}

	private void showPickImageAlert(){
		//TEMP_PHOTO_FILE_NAME=sharedPref.getString("USERNAME", null)+".jpg";
		TEMP_PHOTO_FILE_NAME = PIC_FILE_NAME;
		String state = Environment.getExternalStorageState();
		if (Environment.MEDIA_MOUNTED.equals(state)) {
			mFileTemp = new File(Environment.getExternalStorageDirectory(), TEMP_PHOTO_FILE_NAME);
		}
		else {
			mFileTemp = new File(getFilesDir(), TEMP_PHOTO_FILE_NAME);
		}
		final CharSequence[] options = { "Take Photo", "Choose from Gallery","Cancel" };
		AlertDialog.Builder builder = new AlertDialog.Builder(ProfileActivity.this);
		builder.setTitle("Add Photo!");
		builder.setItems(options, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int item) {
				if (options[item].equals("Take Photo"))
				{
					if (!isDeviceSupportCamera()) {
						Toast.makeText(getApplicationContext(),
								"Sorry! Your device doesn't support camera",
								Toast.LENGTH_LONG).show();
						// will close the app if the device does't have camera
						finish();
					}
					else{
						takePicture();
					}
				}
				else if (options[item].equals("Choose from Gallery"))
				{
					openGallery();
				}
				else if (options[item].equals("Cancel")) {
					dialog.dismiss();
				}
			}
		});
		builder.show();
	}

	private class RetrieveUserProfile extends AsyncTask<Void, Void, Void> {

		boolean err=false,activeUser=false;
		String pName="",pUserStatus="",pProfilePic="";

		@Override
		protected void onPreExecute() {
			super.onPreExecute();			
		}

		@Override
		protected Void doInBackground(Void... arg) {

			ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();

			params.add(new BasicNameValuePair("username", sharedPref.getString("USERNAME", null)));


			ServiceHandler serviceClient = new ServiceHandler();
			try{
				String json = serviceClient.makeServiceCall(ServiceHandler.URL_RETRIEVE_PROFILE,
						ServiceHandler.GET, params);
				//  System.out.println("Create Group ="+json);
				if(!json.equals("error")){
					Log.d("Retrieve User Response: ", "> " + json);
					if(!json.equals(null)){
						JSONArray arr=new JSONArray(json);
						JSONObject obj=null;
						for(int i=0;i<arr.length();i++){
							obj=arr.getJSONObject(i);
							pName= obj.getString("name");
							pProfilePic=obj.getString("profile_pic");
							pUserStatus=obj.getString("user_status");
							activeUser=true;
						}
					}
				}

				else
					err=true;
			}
			catch(Exception e){
				e.printStackTrace();

				//Toast.makeText(mContext, "Something wrong with network, Please try later", Toast.LENGTH_SHORT).show();
				err=true;
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			//if (pDialog.isShowing()){
			//	            pDialog.dismiss();
			if(!err && activeUser){

				editor=sharedPref.edit();

				if(!pName.equals("") || !pName.equals(null))
					editor.putString("PROFILE_NAME", pName);

				if(!pUserStatus.equals("")|| !pUserStatus.equals(null))
					editor.putString("PROFILE_STATUS", pUserStatus);

				if(!pProfilePic.equals("")|| !pProfilePic.equals(null)){
					downloadProfilePic(pProfilePic);
					editor.putString("PROFILE_IMAGE_PATH", getOutputMediaFile().getPath().toString());
					editor.putString("PHOTO_FILENAME", pProfilePic);					
				}
				editor.commit();
				showUI();
			}
			else{
				//Toast.makeText(mContext, "Something wrong", Toast.LENGTH_SHORT).show();
			}
		}
		//   }

	} 
	private void showUI(){

		name=sharedPref.getString("PROFILE_NAME", getString(R.string.username_text));
		localImagePath=sharedPref.getString("PROFILE_IMAGE_PATH","1");
		userstatus=sharedPref.getString("PROFILE_STATUS", getString(R.string.status_text));

		if(!name.equals(null) || !name.equals(""))
			nameTextView.setText(name);
		if(!userstatus.equals(null)||!userstatus.equals(""))
			userstatusTextView.setText(userstatus);	
	}


	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (resultCode == RESULT_OK) {
			if (requestCode == NAME_REQUEST_CODE) {
				name=data.getStringExtra("NAME");
				userstatus=data.getStringExtra("STATUS");
				updateProfileUi(name,userstatus);
			}
			else if(requestCode == REQUEST_CODE_CROP_IMAGE){

				String path = data.getStringExtra(CropImage.IMAGE_PATH);
				if (path == null) {
					return;
				}
				imagepath=mFileTemp.getPath().toString();
				Bitmap bitmap = BitmapFactory.decodeFile(mFileTemp.getPath());

				profileImage.setImageBitmap(bitmap);
			}
			if (requestCode == IMAGE_PICK_CAMERA) {

				startCropImage();
			} else if (requestCode == IMAGE_PICK_GALLERY) {


				try {

					InputStream inputStream = getContentResolver().openInputStream(data.getData());
					FileOutputStream fileOutputStream = new FileOutputStream(mFileTemp);
					copyStream(inputStream, fileOutputStream);
					fileOutputStream.close();
					inputStream.close();

					startCropImage();

				} catch (Exception e) {

					Log.e("TAG", "Error while creating temp file", e);
				}

			}
		}
		if(resultCode==RESULT_CANCELED){

		}
	}


	private void downloadProfilePic(final String picUrl){


		new AsyncTask<Void, Void, Void>(){

			protected void onPreExecute() {

				pBar=(ProgressBar) findViewById(R.id.imageProgress);
				pBar.setVisibility(View.VISIBLE);

			};

			@Override
			protected Void doInBackground(Void... params) {
				// TODO Auto-generated method stub
				try {
					File mediaStorageDir = new File(
							Environment.getExternalStorageDirectory() + File.separator+IMAGE_DIRECTORY_NAME);

					// Create the storage directory if it does not exist
					if (!mediaStorageDir.exists()) {
						mediaStorageDir.mkdirs();
						if (!mediaStorageDir.mkdirs()) {
							Log.d(IMAGE_DIRECTORY_NAME, "Oops! Failed create "
									+ IMAGE_DIRECTORY_NAME + " directory");

						}
					}
					//File root = android.os.Environment.getExternalStorageDirectory(); 
					//File dir = new File (root.getAbsolutePath() + "/RingABell");

					//String filename = sharedPref.getString("USERNAME", null) + ".jpg";
					String filename = PIC_FILE_NAME;


					URL url = new URL(picUrl); //you can write here any link
					File file = new File(mediaStorageDir, filename);

					long startTime = System.currentTimeMillis();
					Log.d("DownloadManager", "download begining");
					Log.d("DownloadManager", "download url:" + url);
					Log.d("DownloadManager", "downloaded file name:" + filename);

					//Open a connection to that URL. 
					URLConnection ucon = url.openConnection();


					//* Define InputStreams to read from the URLConnection.

					InputStream is = ucon.getInputStream();
					BufferedInputStream bis = new BufferedInputStream(is);


					//* Read bytes to the Buffer until there is nothing more to read(-1).

					ByteArrayBuffer baf = new ByteArrayBuffer(5000);
					int current = 0;
					while ((current = bis.read()) != -1) {
						baf.append((byte) current);
					}

					// Convert the Bytes read to a String. 
					FileOutputStream fos = new FileOutputStream(file);
					fos.write(baf.toByteArray());
					fos.flush();
					fos.close();
					runOnUiThread(new Runnable() {

						@Override
						public void run() {
							// TODO Auto-generated method stub
							pBar.setVisibility(View.GONE);

						}
					});
					Log.d("DownloadManager", "download ready in" + ((System.currentTimeMillis() - startTime) / 1000) + " sec");
				}
				catch (IOException e) {
					Log.d("DownloadManager", "Error: " + e);
					runOnUiThread(new Runnable() {

						@Override
						public void run() {
							// TODO Auto-generated method stub
							pBar.setVisibility(View.GONE);
						}
					});

				}
				return null;
			}
			protected void onPostExecute(Void result) {

				try{					
					File sourceFile = new File(getOutputMediaFile().getPath().toString()); 
					if (!sourceFile.isFile()) {
						profileImage.setImageResource(R.drawable.single_user);
					}
					else{
						Bitmap bitmap = BitmapFactory.decodeFile(getOutputMediaFile().getPath().toString());
						profileImage.setImageBitmap(bitmap);
					}					
				}
				catch(Exception e){
					e.printStackTrace();
				}
			}
		}.execute();
	}	

	public int uploadFile(String sourceFileUri) {
		String fileName = sourceFileUri;
		// String renameFileName;
		HttpURLConnection conn = null;
		DataOutputStream dos = null;  
		String lineEnd = "\r\n";
		String twoHyphens = "--";
		String boundary = "*****";
		int bytesRead, bytesAvailable, bufferSize;
		byte[] buffer;
		int maxBufferSize = 1 * 1024 * 1024; 
		File sourceFile = new File(sourceFileUri); 

		//    renameFileName=renameFile(fileName);
		if (!sourceFile.isFile()) {

			dialog.dismiss(); 

			Log.e("uploadFile", "Source File not exist :"+imagepath);

			runOnUiThread(new Runnable() {
				public void run() {
					//Toast.makeText(getApplicationContext(), "Image not found",Toast.LENGTH_SHORT).show();
				}
			}); 
			return 0;
		}
		else		{
			try { 
				// open a URL connection to the Servlet
				FileInputStream fileInputStream = new FileInputStream(sourceFile);
				URL url = new URL(ServiceHandler.URL_IMAGE_UPLOAD);

				// Open a HTTP  connection to  the URL
				conn = (HttpURLConnection) url.openConnection(); 
				conn.setDoInput(true); // Allow Inputs
				conn.setDoOutput(true); // Allow Outputs
				conn.setUseCaches(false); // Don't use a Cached Copy
				conn.setRequestMethod("POST");
				conn.setRequestProperty("Connection", "Keep-Alive");
				conn.setRequestProperty("ENCTYPE", "multipart/form-data");
				conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
				conn.setRequestProperty("uploaded_file",fileName); 
				//                 conn.setRequestProperty("mobile","Hello");

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

				System.out.println("Image URL"+serverResponseMessage);
				Log.i("uploadFile", "HTTP Response is : "
						+ serverResponseMessage + ": " + serverResponseCode);

				if(serverResponseCode == 200){
					InputStream in = conn.getInputStream();

					byte data[] = new byte[1024];
					int counter = -1;
					String jsonString = "";
					while( (counter = in.read(data)) != -1){
						jsonString += new String(data, 0, counter);
					}
					Log.d("Debug", " JSON String: " + jsonString);
					System.out.println("IMAGE RESPONSE"+jsonString);
					
					editor=sharedPref.edit();
					editor.putString("PHOTO_FILENAME", ServiceHandler.SERVER_IMAGE_PATH+"/"+PIC_FILE_NAME);					
					editor.commit();
				} 
				//close the streams //
				fileInputStream.close();
				dos.flush();
				dos.close();

			} catch (MalformedURLException ex) {

				dialog.dismiss();  
				ex.printStackTrace();

				runOnUiThread(new Runnable() {
					public void run() {
						//  messageText.setText("MalformedURLException Exception : check script url.");
						Toast.makeText(getApplicationContext(), "Failed To Update Profile", Toast.LENGTH_SHORT).show();
					}
				});
				Log.e("Upload file to server", "error: " + ex.getMessage(), ex);  

			} catch (Exception e) {

				dialog.dismiss();  
				e.printStackTrace();

				runOnUiThread(new Runnable() {
					public void run() {
						//messageText.setText("Got Exception : see logcat ");
						Toast.makeText(getApplicationContext(), "Got Exception : see logcat ", Toast.LENGTH_SHORT).show();
					}
				});
				Log.e("Upload file to server Exception", "Exception : "  + e.getMessage(), e);  
			}
			dialog.dismiss();       
			return 1; 

		} // End else block 
	}
}

