package com.share2people.ringabell;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
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
import com.ringabell.model.Contact;
import com.ringabell.model.Reminder;
import com.ringabell.serverdb.ServiceHandler;
import com.ringabell.utils.AlertDialogManager;
import com.ringabell.utils.ConnectionDetector;
import com.share2people.ringabell.R;




import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;

import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.QuickContactBadge;
import android.widget.TextView;
import android.widget.Toast;

public class BlockUserActivity extends Activity {

	LayoutInflater inflator;
	CustomAdapter adapter;
	ArrayList<Contact> blockedContactList;
	TextView noContacts;
	ListView listView;
	ProgressDialog pDialog;
	SharedPreferences sharedPref;
	Context mContext;
	boolean skipFlag=false;
	ConnectionDetector cDetector;
	AlertDialogManager alert = new AlertDialogManager();

	//public static final String URL_UNBLOCK_USER="http://tech-sync.com/ringabell/delete_block_user.php";

	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_block_user);

		mContext=(Context) getApplicationContext();
		cDetector = new ConnectionDetector(getApplicationContext());		 
		noContacts = (TextView) findViewById(R.id.noContacts);
		listView = (ListView) findViewById(R.id.blockListView);
		sharedPref=getSharedPreferences("LAST_LOGIN", Activity.MODE_PRIVATE);
		skipFlag=sharedPref.getBoolean("SKIP", false);

		getActionBar().setTitle(getString(R.string.blocked_users));
		getActionBar().setDisplayHomeAsUpEnabled(true);


		//ActionBar actionBar=getActionBar();
		//actionBar.setDisplayHomeAsUpEnabled(true);
		//actionBar.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.block_main_action_bar)));

		initImageLoader(getApplicationContext());

		if(skipFlag){
			//Toast.makeText(mContext, "Registration Required.", Toast.LENGTH_SHORT).show();
			showAlertDialog(BlockUserActivity.this,
					"Registration Required",
					"Please Register To Use This Feature.", false);
		}


		blockedContactList=new ArrayList<Contact>();

		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position,
					long id) {
				// TODO Auto-generated method stub
				if(!cDetector.isConnectingToInternet()){
					alert.showAlertDialog(BlockUserActivity.this,
							"Internet Connection Error",
							"Please Enable Your Internet Connection.", false);
				}
				else{
					String number=((Contact) parent.getAdapter().getItem(position)).getPhoneNo();
					unBlockAlertDialog(number);
				}
			}
		});
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		try{
			if(new ContactLocalDB(getApplicationContext()).getAllBlockedContacts()>0){

				blockedContactList=new ContactLocalDB(getApplicationContext()).getAllBlockedContactList();
				noContacts.setVisibility(View.GONE);
				listView.setVisibility(View.VISIBLE);
				adapter=new CustomAdapter(getApplicationContext(), R.layout.contacts_list_layout, blockedContactList);
				listView.setAdapter(adapter);
			}
		}
		catch(Exception e){
			e.printStackTrace();
			noContacts.setVisibility(View.VISIBLE);
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
				BlockUserActivity.this.finish();
			}
		});

		// Showing Alert Message
		alertDialog.show();
	}

	public static void initImageLoader(Context context) {
		// This configuration tuning is custom. You can tune every option, you may tune some of them,
		// or you can create default configuration by
		//  ImageLoaderConfiguration.createDefault(this);
		// method.
		ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(context)
		.threadPriority(Thread.NORM_PRIORITY - 2)
		.denyCacheImageMultipleSizesInMemory()
		.diskCacheFileNameGenerator(new Md5FileNameGenerator())
		.diskCacheSize(50 * 1024 * 1024) // 50 Mb
		.tasksProcessingOrder(QueueProcessingType.LIFO)
		.writeDebugLogs() // Remove for release app
		.build();
		// Initialize ImageLoader with configuration.
		ImageLoader.getInstance().init(config);
	}
	public void unBlockAlertDialog(final String unBlockNumber) {


		final CharSequence[] items = { "UnBlock" };

		AlertDialog.Builder builder = new AlertDialog.Builder(BlockUserActivity.this);

		builder.setItems(items, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int item) {

				// will toast your selection
				//    showToast("Name: " + items[item]);
				if(items[item]=="UnBlock"){
					new UnBlockUser().execute(unBlockNumber);	
				}
				dialog.dismiss();
			}
		}).show();
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.activity_block_user, menu);
		return super.onCreateOptionsMenu(menu);
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Take appropriate action for each action item click

		switch (item.getItemId()) {

		case R.id.adduser:
			// rating action
			Intent rateIntent =new Intent(BlockUserActivity.this,BlockContacts.class);
			startActivity(rateIntent);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
		//return true; 
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
	private class CustomAdapter extends ArrayAdapter<Contact>{

		Context lContext;
		List<Contact> reminders=new ArrayList<Contact>();

		protected ImageLoader imageLoader;
		DisplayImageOptions options;
		private ImageLoadingListener animateFirstListener = new AnimateFirstDisplayListener();

		public CustomAdapter(Context context,int resId,List <Contact> reminderList){
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
			convertView=inflator.inflate(R.layout.contacts_list_layout, null);
			viewHolder=new ViewHolder();

			viewHolder.title=(TextView) convertView.findViewById(R.id.nameText);
			viewHolder.receiver=(TextView) convertView.findViewById(R.id.phoneText);
			viewHolder.profilePic=(QuickContactBadge) convertView.findViewById(R.id.icon);
			convertView.setTag(viewHolder);
			//}
			//else 
			//viewHolder=(ViewHolder) convertView.getTag();

			final Contact cont=reminders.get(position);

			viewHolder.title.setText(cont.getName());
			viewHolder.receiver.setText(cont.getPhoneNo());
			imageLoader.displayImage(cont.getUrl(), viewHolder.profilePic, options, animateFirstListener);

			return convertView;
		}
	}
	private class UnBlockUser extends AsyncTask<String, Void, Void> {

		boolean error=false,resp=false;
		String username="";
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			pDialog = new ProgressDialog(BlockUserActivity.this);
			pDialog.setMessage("Please Wait...");
			pDialog.setCancelable(true);
			pDialog.show();

		}

		@Override
		protected Void doInBackground(String... arg) {
			String block_user=arg[0];
			username=block_user;
			// Preparing post params

			ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("username", block_user));
			params.add(new BasicNameValuePair("owner", sharedPref.getString("USERNAME", null)));


			ServiceHandler serviceClient = new ServiceHandler();
			try{
				String json = serviceClient.makeServiceCall(ServiceHandler.URL_UNBLOCK_USER, ServiceHandler.GET,params);

				Log.v("Response: ", "> " + json);
				System.out.println("Existing User ARRAY IS="+json+"length is="+json.length());
				if(!json.equals("error")|| json!=null){


					try {
						JSONObject obj=new JSONObject(json);
						String response=obj.getString("succ");

						if(response.equals("1"))
							resp=true;
						else 
							resp=false;

					} catch (JSONException e) {
						e.printStackTrace();
					}       
				}
				else
					error=true;

			}catch(Exception e){
				e.printStackTrace();
				pDialog.dismiss();
				error=true;
				//Toast.makeText(mContext, "Something wrong, Please try later", Toast.LENGTH_SHORT).show();
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			if (pDialog.isShowing()){
				pDialog.dismiss();
				if(!error){
					if(resp){ 	 	
						/*Toast toast=Toast.makeText(getApplicationContext(), "Contact Blocked", Toast.LENGTH_SHORT);
	            toast.setGravity(Gravity.CENTER, 0, 0);
	    		toast.show();*/

						new ContactLocalDB(mContext).updateUnBlockUser(username);
						try{
							if(new ContactLocalDB(getApplicationContext()).getAllBlockedContacts()>0){

								blockedContactList=new ContactLocalDB(getApplicationContext()).getAllBlockedContactList();
								noContacts.setVisibility(View.GONE);
								listView.setVisibility(View.VISIBLE);
								adapter=new CustomAdapter(getApplicationContext(), R.layout.contacts_list_layout, blockedContactList);
								listView.setAdapter(adapter);
								adapter.notifyDataSetChanged();
							}
							else{
								noContacts.setVisibility(View.VISIBLE);
								listView.setVisibility(View.GONE);
							}
						}
						catch(Exception e){
							e.printStackTrace();
						}

					}
					else{
						//Toast toast=Toast.makeText(getApplicationContext(), "Sorry Contact Not Blocked, Try Again", Toast.LENGTH_SHORT);
						//toast.setGravity(Gravity.CENTER, 0, 0);
						//toast.show();
					}
					//finish();
				}
				else{
					//Toast.makeText(mContext, "Something wrong", Toast.LENGTH_SHORT).show();
				}
			}
		}

	}

}
