package com.share2people.ringabell;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;



import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.ringabell.localdb.ContactLocalDB;
import com.ringabell.localdb.GroupsLocalDB;
import com.ringabell.model.Contact;
import com.ringabell.model.Group;
import com.ringabell.model.Reminder;
import com.ringabell.serverdb.ServiceHandler;
import com.ringabell.utils.AlertDialogManager;
import com.ringabell.utils.ConnectionDetector;
import com.share2people.ringabell.R;



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
import android.app.Service;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;

import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.QuickContactBadge;
import android.widget.TextView;
import android.widget.Toast;

public class AddGroupActivity extends Activity implements OnClickListener{

	private ImageView groupImage;
	private TextView groupText;
	//private AutoCompleteTextView groupEditText;
	public ImageView contactPickImage;
	public static final int IMAGE_PICK_CAMERA=1;
	public static final int IMAGE_PICK_GALLERY=2;
	public static final int CONTACT_PICK=3;
	public static final int REQUEST_CODE_CROP_IMAGE = 4;
	ListView mylistview;

	public static final int MEDIA_TYPE_IMAGE = 1;
	private static final String IMAGE_DIRECTORY_NAME = "RingBell";
	public static String TEMP_PHOTO_FILE_NAME = "temp_photo.jpg";
	private int serverResponseCode = 0;
	private Uri fileUri;
	private File  mFileTemp;
	private String imagepath="";
	EditText inputsearch,title,groupStatus;
	List<Contact> selectedContact;
	List<Contact> memberList;
	List<Group> myGroupMemberList;
	static ActionBar actionbar;
	private ProgressDialog pDialog,dialog;
	Context mContext;
	LayoutInflater inflator;
	CustomAdapter adapter;
	String groupNameText="",groupStatusText="",gid="", gname="", gstatus="", profilepic="";
	;
	SharedPreferences sharedPref;
	SharedPreferences.Editor editor;

	boolean skipFlag=false, editing=false;
	String user="", gowner="";
	ConnectionDetector cDetector;
	AlertDialogManager alert = new AlertDialogManager();
	ImageLoader imageLoader;

	DisplayImageOptions options;
	ImageLoadingListener animateFirstListener = new AnimateFirstDisplayListener();


	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_add_group);

		mContext=(Context) getApplicationContext();
		cDetector=new ConnectionDetector(mContext);


		sharedPref=getSharedPreferences("LAST_LOGIN", Activity.MODE_PRIVATE);
		skipFlag=sharedPref.getBoolean("SKIP", false);
		user=sharedPref.getString("USERNAME", "");
		editing=sharedPref.getBoolean("Editing", false);		
		TEMP_PHOTO_FILE_NAME=sharedPref.getString("USERNAME", null)+"_"+String.valueOf(System.currentTimeMillis())+".jpg";
		gid = sharedPref.getString("groupid", "");
		gname = sharedPref.getString("gpname", "");
		gstatus = sharedPref.getString("gpstatus", "");
		gowner = sharedPref.getString("gowner", "");

		if(editing){
			if(!gowner.equals(user)){
				getActionBar().setTitle("View Group");
			}
			else{
				getActionBar().setTitle("Edit Group");
			}
		}
		else{
			getActionBar().setTitle("Add Group");
		}
		getActionBar().setDisplayHomeAsUpEnabled(true);
		getActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.group_main_action_bar)));

		//image
		groupText=(TextView) findViewById(R.id.group_text);
		groupImage=(ImageView) findViewById(R.id.group_icon);
		contactPickImage=(ImageView) findViewById(R.id.contactPickImage);
		title=(EditText) findViewById(R.id.g_title_edit);
		groupStatus=(EditText) findViewById(R.id.g_status_edit);
		selectedContact=new ArrayList<Contact>();
		memberList=new ArrayList<Contact>();
		selectedContact.clear();
		memberList.clear();
		contactPickImage.setOnClickListener(this);		
		groupImage.setOnClickListener(this);		

		imageLoader = ImageLoader.getInstance();

		//options for image loading
		options = new DisplayImageOptions.Builder()
		.showImageOnLoading(R.drawable.groups_icon2)
		.showImageForEmptyUri(R.drawable.groups_icon2)
		.showImageOnFail(R.drawable.groups_icon2)
		.cacheInMemory(true)
		.cacheOnDisk(true)
		.considerExifParams(true)
		.displayer(new RoundedBitmapDisplayer(0))
		.build();

		if(editing){
			if(!gowner.equals(user)){				
				title.setEnabled(false);
				groupStatus.setEnabled(false);
				contactPickImage.setVisibility(View.GONE);
				groupImage.setOnClickListener(null);
				groupText.setText("Group Contacts");
			}
			title.setText(gname);
			groupStatus.setText(gstatus);
			mylistview=(ListView) findViewById(R.id.listView1);
			String imgpath=Environment.getExternalStorageDirectory().
					getAbsolutePath() + "/" +TEMP_PHOTO_FILE_NAME;
			Bitmap bitmap = BitmapFactory.decodeFile(imgpath);
			imageLoader.displayImage(new GroupsLocalDB(mContext).getGroupPicUrl(gid), groupImage, options, animateFirstListener);
			profilepic = new GroupsLocalDB(mContext).getGroupPicUrl(gid);

			//groupImage.setImageBitmap(bitmap);
			myGroupMemberList = new GroupsLocalDB(getApplicationContext()).getGroupMembers(gid);
			for(int i=0;i<myGroupMemberList.size();i++){
				String membername=new ContactLocalDB(getApplicationContext()).getContactName(myGroupMemberList.get(i).getGroupMember());
				memberList.add(new Contact(membername, myGroupMemberList.get(i).getGroupMember()));
			}
			selectedContact=memberList;
			adapter=new CustomAdapter(mContext, R.layout.reminder_layout, selectedContact);
			mylistview.setAdapter(adapter);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.activity_action_done, menu);		
		if(editing){
			if(!gowner.equals(user)){
				menu.clear();
			}
			else{
				menu.getItem(0).setTitle("UPDATE");
			}
		}
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Take appropriate action for each action item click

		switch (item.getItemId()) {
		case R.id.create:
			if(skipFlag){
				showAlertDialog(AddGroupActivity.this,
						"Registration Required",
						"Please Register To Add Group.", false);
			}
			else{
				if(!cDetector.isConnectingToInternet()){
					alert.showAlertDialog(AddGroupActivity.this,
							"Internet Connection Error",
							"Please Enable Your Internet Connection.", false);
				}
				else{
					groupNameText=title.getText().toString();
					groupStatusText=groupStatus.getText().toString();



					if(groupNameText.equals("") || groupNameText.equals(null)){
						Toast toast=Toast.makeText(getApplicationContext(), "Please Add Title", Toast.LENGTH_SHORT);
						toast.setGravity(Gravity.CENTER, 0, 0);
						toast.show();
					}					

					else if(selectedContact.size()<=0){
						Toast toast=Toast.makeText(getApplicationContext(), "Please Add at least 1 Contacts", Toast.LENGTH_SHORT);
						toast.setGravity(Gravity.CENTER, 0, 0);
						toast.show();

					}

					else{
						if (!imagepath.equals("") || !imagepath.equals(null)){
							new AsyncTask<Void, Void, Void>(){

								protected void onPreExecute() {
									dialog =ProgressDialog.show(AddGroupActivity.this, "", "updating");
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
									if(editing){
										new UpdateGroup().execute();
									}
									else{
										new AddGroup().execute();
									}
								}
							}.execute();
						}
						else{
							if(editing){
								new UpdateGroup().execute();
							}
							else{
								new AddGroup().execute();
							}
						}
					}
				}
				return true;
			}

		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();



	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		if(v.getId()==R.id.group_icon){
			showPickImageAlert();
		}
		else if(v.getId()==R.id.contactPickImage){
			Intent intent=new Intent(this,ServerContactActivity.class);
			startActivityForResult(intent,CONTACT_PICK);
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
				AddGroupActivity.this.finish();
			}
		});

		// Showing Alert Message
		alertDialog.show();
	}

	// Checking camera availability
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
		String state = Environment.getExternalStorageState();


		if (Environment.MEDIA_MOUNTED.equals(state)) {
			mFileTemp = new File(Environment.getExternalStorageDirectory(), TEMP_PHOTO_FILE_NAME);
		}
		else {
			mFileTemp = new File(getFilesDir(), TEMP_PHOTO_FILE_NAME);
		}
		final CharSequence[] options = { "Take Photo", "Choose from Gallery","Cancel" };
		AlertDialog.Builder builder = new AlertDialog.Builder(AddGroupActivity.this);
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
						//captureImage();
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
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (resultCode == RESULT_OK) {
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
			else if(requestCode == REQUEST_CODE_CROP_IMAGE){
				String path = data.getStringExtra(CropImage.IMAGE_PATH);
				if (path == null) {
					return;
				}
				imagepath=mFileTemp.getPath().toString();
				Bitmap bitmap = BitmapFactory.decodeFile(mFileTemp.getPath());
				groupImage.setImageBitmap(bitmap);
			}
			else if(requestCode == CONTACT_PICK){
				selectedContact=(ArrayList<Contact>) data.getSerializableExtra("LIST");
				updateUI();
			}
		}
		if(resultCode==RESULT_CANCELED){
		}
	}   

	private void updateUI(){
		mylistview=(ListView) findViewById(R.id.listView1);	
		adapter=new CustomAdapter(mContext, R.layout.reminder_layout, selectedContact);
		mylistview.setAdapter(adapter);
	}

	private class AddGroup extends AsyncTask<Void, Void, Void> {

		boolean err=false;
		String members;
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			pDialog = new ProgressDialog(AddGroupActivity.this);
			pDialog.setMessage("Please Wait...");
			pDialog.setCancelable(false);
			pDialog.show();
			List<String> memList=new ArrayList<String>();
			for(int i=0;i<selectedContact.size();i++){
				memList.add(selectedContact.get(i).getPhoneNo());
			}
			members=combinedString(memList);
			if(!imagepath.equals("") || !imagepath.equals(null))
				profilepic=ServiceHandler.SERVER_IMAGE_PATH+"/"+TEMP_PHOTO_FILE_NAME;
		}

		@Override
		protected Void doInBackground(Void... arg) {

			// Preparing post params
			ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("name", groupNameText));
			params.add(new BasicNameValuePair("owner", sharedPref.getString("USERNAME", null)));
			params.add(new BasicNameValuePair("members",members));
			params.add(new BasicNameValuePair("group_pic", profilepic));
			params.add(new BasicNameValuePair("status","0"));
			params.add(new BasicNameValuePair("group_status", groupStatusText));


			ServiceHandler serviceClient = new ServiceHandler();
			try{
				String json = serviceClient.makeServiceCall(ServiceHandler.URL_CREATE_GROUP,
						ServiceHandler.GET, params);
				System.out.println("Create Group ="+json);
				if(!json.equals("error")){
					Log.d("Create Group Response: ", "> " + json);
					Log.e("Response: ", "> " + json);
					System.out.println("New Group="+json+"length is="+json.length());

					try {
						JSONArray array=new JSONArray(json);
						JSONObject obj=null;
						for(int i=0;i<array.length();i++){
							obj=array.getJSONObject(i);
							gid=obj.getString("group_id");
							profilepic=obj.getString("group_pic");							
						}								
					} catch (JSONException e) {
						e.printStackTrace();
					}

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
					deleteFileFromSdCard(imagepath); 	
					Toast toast=Toast.makeText(getApplicationContext(), "Group Added", Toast.LENGTH_SHORT);
					toast.setGravity(Gravity.CENTER, 0, 0);
					toast.show();
					for(int j=0;j< selectedContact.size();j++){
						String gmember = selectedContact.get(j).getPhoneNo();
						new GroupsLocalDB(getApplicationContext()).insertGroupMembersLocal(gmember, gid);
						//Toast.makeText(getApplicationContext(), gm.toString(), Toast.LENGTH_SHORT).show();
					}
					new GroupsLocalDB(getApplicationContext()).insertGroupLocal(user, gid, groupNameText, gowner, profilepic, groupStatusText);
					
					finish();
				}
				else{
					//Toast.makeText(mContext, "Something wrong", Toast.LENGTH_SHORT).show();
				}
			}
		}
	}

	private class UpdateGroup extends AsyncTask<Void, Void, Void> {

		boolean err=false;
		String members;
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			pDialog = new ProgressDialog(AddGroupActivity.this);
			pDialog.setMessage("Please Wait...");
			pDialog.setCancelable(false);
			pDialog.show();
			List<String> memList=new ArrayList<String>();
			for(int i=0;i<selectedContact.size();i++){
				memList.add(selectedContact.get(i).getPhoneNo());
			}
			members=combinedString(memList);			
			if(!imagepath.equals("") || !imagepath.equals(null))
				profilepic=ServiceHandler.SERVER_IMAGE_PATH+"/"+TEMP_PHOTO_FILE_NAME;
		}

		@Override
		protected Void doInBackground(Void... arg) {

			ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("group_id", gid));
			params.add(new BasicNameValuePair("name", groupNameText));
			params.add(new BasicNameValuePair("owner", sharedPref.getString("USERNAME", null)));
			params.add(new BasicNameValuePair("members",members));
			params.add(new BasicNameValuePair("group_pic", profilepic));
			params.add(new BasicNameValuePair("status","0"));
			params.add(new BasicNameValuePair("group_status", groupStatusText));


			ServiceHandler serviceClient = new ServiceHandler();
			try{
				String json = serviceClient.makeServiceCall(ServiceHandler.URL_UPDATE_GROUP,
						ServiceHandler.GET, params);
				System.out.println("Update Group ="+json);
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
					deleteFileFromSdCard(imagepath); 	
					Toast toast=Toast.makeText(getApplicationContext(), "Group Updated", Toast.LENGTH_SHORT);
					toast.setGravity(Gravity.CENTER, 0, 0);
					toast.show();
					new GroupsLocalDB(getApplicationContext()).deleteMembers(gid);
					for(int j=0;j< selectedContact.size();j++){
						String gmember = selectedContact.get(j).getPhoneNo();
						new GroupsLocalDB(getApplicationContext()).insertGroupMembersLocal(gmember, gid);
					}
					//reminderList.add(new Reminder(title, number));
					new GroupsLocalDB(getApplicationContext()).updateGroupLocal(user, gid, groupNameText, gowner, profilepic, groupStatusText);
					finish();
				}
				else{
					//Toast.makeText(mContext, "Something wrong", Toast.LENGTH_SHORT).show();
				}
			}
		}
	}


	public static Bitmap getRoundedShape(Bitmap scaleBitmapImage,int width) {
		// TODO Auto-generated method stub
		int targetWidth = width;
		int targetHeight = width;
		Bitmap targetBitmap = Bitmap.createBitmap(targetWidth, 
				targetHeight,Bitmap.Config.ARGB_8888);

		Canvas canvas = new Canvas(targetBitmap);
		Path path = new Path();
		path.addCircle(((float) targetWidth - 1) / 2,
				((float) targetHeight - 1) / 2,
				(Math.min(((float) targetWidth), 
						((float) targetHeight)) / 2),
						Path.Direction.CCW);
		canvas.clipPath(path);
		Bitmap sourceBitmap = scaleBitmapImage;
		canvas.drawBitmap(sourceBitmap, 
				new Rect(0, 0, sourceBitmap.getWidth(),
						sourceBitmap.getHeight()), 
						new Rect(0, 0, targetWidth,
								targetHeight), null);
		return targetBitmap;
	}


	private void deleteFileFromSdCard(String path){
		File file= new File(path);
		if(file.exists())
		{
			file.delete();
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
	//
	private class CustomAdapter extends ArrayAdapter<Contact>{

		List<Contact> reminders=new ArrayList<Contact>();
		DisplayImageOptions option;


		public CustomAdapter(Context context,int resId,List <Contact> reminderList){
			super(context,resId,reminderList);
			this.reminders=reminderList;


			option = new DisplayImageOptions.Builder()
			.showImageOnLoading(R.drawable.ic_user)
			.showImageForEmptyUri(R.drawable.ic_user)
			.showImageOnFail(R.drawable.ic_user)
			.cacheInMemory(true)
			.cacheOnDisk(true)
			.considerExifParams(true)
			.displayer(new RoundedBitmapDisplayer(80))
			.build();

		}

		private class ViewHolder{
			TextView title,receiver;
			ImageView deleteImage/*visibleImage*/;
			LinearLayout callLayout;
			//RelativeLayout userProfile;
			QuickContactBadge profilePic;



		}
		public Contact getItem(int position) {
			return reminders.get(position);
		};



		ViewHolder viewHolder;
		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			inflator = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			//if(convertView==null){
			convertView=inflator.inflate(R.layout.group_member_layout, null);
			viewHolder=new ViewHolder();

			viewHolder.title=(TextView) convertView.findViewById(R.id.nameText);
			viewHolder.receiver=(TextView) convertView.findViewById(R.id.phoneText);
			viewHolder.deleteImage=(ImageView) convertView.findViewById(R.id.deleteImage);
			viewHolder.callLayout=(LinearLayout) convertView.findViewById(R.id.callLayout);
			viewHolder.profilePic=(QuickContactBadge) convertView.findViewById(R.id.icon);

			convertView.setTag(viewHolder);

			final Contact rem=reminders.get(position);

			if(!gowner.equals(user)){
				viewHolder.deleteImage.setVisibility(View.GONE);
			}

			if(rem.getPhoneNo().equals(user)){
				viewHolder.title.setText("Me");
				viewHolder.deleteImage.setVisibility(View.GONE);
			}

			else if(rem.getName().equals("")){
				viewHolder.title.setText(rem.getPhoneNo());
			}
			else{
				viewHolder.title.setText(rem.getName());

			}
			viewHolder.receiver.setText(rem.getPhoneNo());
			imageLoader.displayImage(rem.getUrl(), viewHolder.profilePic, option, animateFirstListener);

			viewHolder.callLayout.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					reminders.remove(position);
					adapter.notifyDataSetChanged();
				}
			});
			return convertView;
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
	public int uploadFile(String sourceFileUri) {     

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

			dialog.dismiss(); 

			Log.e("uploadFile", "Source File not exist :"+imagepath);

			runOnUiThread(new Runnable() {
				public void run() {
					//Toast.makeText(getApplicationContext(), "Image not found",Toast.LENGTH_SHORT).show();
				}
			}); 
			return 0;

		}
		else
		{
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
				//	                 conn.setRequestProperty("mobile","Hello");

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
							//Toast.makeText(getApplicationContext(), "File Upload Complete.", Toast.LENGTH_SHORT).show();
						}
					}); 
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
						Toast.makeText(getApplicationContext(), "MalformedURLException", Toast.LENGTH_SHORT).show();
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
		}
	}
}
