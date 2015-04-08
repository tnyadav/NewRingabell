package com.share2people.ringabell;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

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
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;

import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class BlockContacts extends Activity{

	ListView listView;
	TextView noContacts;
	BlockContactAdapter adapter;
	ArrayList<Contact> nonBlockContactList;
	private Context mContext;
	private ProgressDialog pDialog;
	ConnectionDetector cDetector;
	AlertDialogManager alert = new AlertDialogManager();
	//public static final String URL_BLOCK_USER="http://tech-sync.com/ringabell/insert_block_user.php";
	SharedPreferences sharedPref;
	boolean skipFlag=false;

	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_blockcontacts);

		getActionBar().setTitle(getString(R.string.app_users));
		getActionBar().setDisplayHomeAsUpEnabled(true);
		getActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.block_main_action_bar)));


		//ActionBar actionBar=getActionBar();
		//actionBar.setDisplayHomeAsUpEnabled(true);
		//actionBar.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.block_main_action_bar)));

		listView=(ListView) findViewById(R.id.listView);
		noContacts=(TextView) findViewById(R.id.noContacts);
		mContext=(Context) getApplicationContext();
		cDetector = new ConnectionDetector(getApplicationContext());		 

		sharedPref=getSharedPreferences("LAST_LOGIN", Activity.MODE_PRIVATE);
		skipFlag=sharedPref.getBoolean("SKIP", false);

		initImageLoader(getApplicationContext());
		if(new ContactLocalDB(getApplicationContext()).getAllNonBlockedContacts()>0){
			nonBlockContactList=new ContactLocalDB(getApplicationContext()).getAllNonBlockedContactList();
			adapter=new BlockContactAdapter(getApplicationContext(), nonBlockContactList);
			noContacts.setVisibility(View.GONE);
			listView.setVisibility(View.VISIBLE);
			listView.setAdapter(adapter);
		}

		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position,
					long id) {
				// TODO Auto-generated method stub
				if(skipFlag){
					//Toast.makeText(mContext, "Registration Required.", Toast.LENGTH_SHORT).show();
					showAlertDialog(BlockContacts.this,
							"Registration Required",
							"Please Register To Block A User.", false);
				}
				else{
					if(!cDetector.isConnectingToInternet()){
						alert.showAlertDialog(BlockContacts.this,
								"Internet Connection Error",
								"Please enable your Internet connection", false);
						return;
					}
					else{
						String uname = ((Contact) parent.getAdapter().getItem(position)).getName();
						String ustatus = ((Contact) parent.getAdapter().getItem(position)).getUserstatus();
						String upic = ((Contact) parent.getAdapter().getItem(position)).getUrl();

						String phoneNumber = ((Contact) parent.getAdapter().getItem(position)).getPhoneNo();
						new BlockUser().execute(phoneNumber, uname, ustatus,upic);
					}
				}
			}
		});
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
				BlockContacts.this.finish();
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
	
	private class BlockContactAdapter extends BaseAdapter  {

		Context context;
		List<Contact> rowItems;
		protected ImageLoader imageLoader;
		DisplayImageOptions options;
		private ImageLoadingListener animateFirstListener = new AnimateFirstDisplayListener();


		BlockContactAdapter(Context context, List<Contact> rowItems) {
			this.context = context;
			this.rowItems = rowItems;
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

		@Override
		public int getCount() {
			return rowItems.size();
		}

		@Override
		public Object getItem(int position) {
			return rowItems.get(position);
		}

		@Override
		public long getItemId(int position) {
			return rowItems.indexOf(getItem(position));
		}

		/* private view holder class */
		private class ViewHolder {
			ImageView profile_pic;
			TextView member_name;
			TextView status;

		}

		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {

			final ViewHolder holder;

			LayoutInflater mInflater = (LayoutInflater) context
					.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
			//if (convertView == null) {
				convertView = mInflater.inflate(R.layout.blockuser_layout, null);
				holder = new ViewHolder();

				holder.member_name = (TextView) convertView
						.findViewById(R.id.member_name);
				holder.profile_pic = (ImageView) convertView
						.findViewById(R.id.profile_pic);
				holder.status = (TextView) convertView.findViewById(R.id.status);


				// add or setting data's to listItem row
				final Contact row_pos = rowItems.get(position);

				// holder.profile_pic.setImageResource(row_pos.getLookUp());
				holder.member_name.setText(row_pos.getName());
				holder.status.setText(row_pos.getOriginalPhoneNumber());
				imageLoader.displayImage(row_pos.getUrl(), holder.profile_pic, options, animateFirstListener);	  


				convertView.setTag(holder);
			//}

			return convertView;
		}

	}
	private class BlockUser extends AsyncTask<String, Void, Void> {

		boolean error=false,resp=false;
		String username="";
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			pDialog = new ProgressDialog(BlockContacts.this);
			pDialog.setMessage("Please Wait...");
			pDialog.setCancelable(true);
			pDialog.show();

		}

		@Override
		protected Void doInBackground(String... arg) {
			String block_user=arg[0];
			String block_username=arg[1];
			String block_userstatus=arg[2];
			String block_userpic=arg[3];
			username=block_user;
			// Preparing post params

			ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("username", block_user));
			params.add(new BasicNameValuePair("owner", sharedPref.getString("USERNAME", null)));
			params.add(new BasicNameValuePair("status","0"));
			params.add(new BasicNameValuePair("block_username", block_username));
			params.add(new BasicNameValuePair("block_userpic", block_userpic));
			params.add(new BasicNameValuePair("block_userstatus",block_userstatus));


			ServiceHandler serviceClient = new ServiceHandler();
			try{
				String json = serviceClient.makeServiceCall(ServiceHandler.URL_BLOCK_USER, ServiceHandler.GET,params);

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
				Toast.makeText(mContext, "Something wrong, Please try later", Toast.LENGTH_SHORT).show();
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
						Toast toast=Toast.makeText(getApplicationContext(), "Contact Blocked", Toast.LENGTH_SHORT);
						toast.setGravity(Gravity.CENTER, 0, 0);
						toast.show();
						new ContactLocalDB(mContext).updateBlockUser(username);
						nonBlockContactList=new ContactLocalDB(getApplicationContext()).getAllNonBlockedContactList();
						adapter=new BlockContactAdapter(getApplicationContext(), nonBlockContactList);
						noContacts.setVisibility(View.GONE);
						listView.setVisibility(View.VISIBLE);
						listView.setAdapter(adapter);
						adapter.notifyDataSetChanged();

					}
					else{
						Toast toast=Toast.makeText(getApplicationContext(), "Sorry Contact Not Blocked, Try Again", Toast.LENGTH_SHORT);
						toast.setGravity(Gravity.CENTER, 0, 0);
						toast.show();
					}

					//finish();

				}
				else
					Toast.makeText(mContext, "Something wrong", Toast.LENGTH_SHORT).show();
			}
		}

	}
}

