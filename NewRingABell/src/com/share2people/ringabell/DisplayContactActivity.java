package com.share2people.ringabell;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.provider.ContactsContract;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.QuickContactBadge;
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
import com.ringabell.model.Contact;
import com.ringabell.model.Group;
import com.ringabell.reminder.ReminderMainActivity;
import com.ringabell.serverdb.ServiceHandler;
import com.ringabell.utils.ConnectionDetector;
import com.share2people.ringabell.R;

public class DisplayContactActivity extends Activity implements OnItemClickListener{

	ListView list;
	ArrayList<Contact> serverContactList,localContactList,combinedContactList;
	ArrayList<Group> GroupList;
	static ArrayList<String> phoneList;
	LayoutInflater inflator;
	//private static List<String> existingContactList;
	//private static ArrayList<Contact> newContactList;
	//private ArrayList<String> contactArray;
	ArrayList<Contact> sortedList;
	private static Context mContext;
	private static String phone;
	private LayoutInflater vi;
	
	Button doneButton;
	HashMap<Integer, List<Contact>>selectedContactMap;
	List<Contact> rowItems;
	ListView mylistview;
	ContactAdapter adapter;
	static ActionBar actionbar;
	EditText inputsearch;
	int selectcount = 0, textlength = 0;
	ProgressBar pb;
	private static SharedPreferences sharedPref;
	ConnectionDetector cDetector;


	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_display_contact);

		getActionBar().setTitle(getString(R.string.contacts));
		getActionBar().setDisplayHomeAsUpEnabled(true);
		getActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.display_contact_action_bar)));

		mContext = getApplicationContext();
		inputsearch = (EditText) findViewById(R.id.searchEditText);
		mylistview = (ListView) findViewById(R.id.listView);
		pb=(ProgressBar) findViewById(R.id.pbDefault);
		sharedPref =getSharedPreferences("LAST_LOGIN", Activity.MODE_PRIVATE);
		cDetector=new ConnectionDetector(mContext);

		inputsearch.addTextChangedListener(new TextWatcher() {

			public void afterTextChanged(Editable s){

			}

			public void beforeTextChanged(CharSequence s, int start, int count, int after){

			}

			public void onTextChanged(CharSequence s, int start, int before, int count)
			{
				sortedList=new ArrayList<Contact>();
				inputsearch.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
				textlength = inputsearch.getText().length();
				sortedList.clear();
				if(textlength==0){ 
					mylistview.setAdapter(new ContactAdapter(DisplayContactActivity.this,combinedContactList));
				}
				else{					
					for (int i = 0; i < combinedContactList.size(); i++){
						if (textlength <= combinedContactList.get(i).getPhoneNo().length() || 
								textlength <= combinedContactList.get(i).getName().length()){

							if(combinedContactList.get(i).getPhoneNo().contains(inputsearch.getText().toString().trim()) || 
									combinedContactList.get(i).getName().toLowerCase().contains(inputsearch.getText().toString().toLowerCase().trim())){

								sortedList.add(new Contact(combinedContactList.get(i).getId(), combinedContactList.get(i).getName(),
										combinedContactList.get(i).getPhoneNo(), combinedContactList.get(i).getOriginalPhoneNumber(),
										combinedContactList.get(i).getLookUp(), combinedContactList.get(i).getUrl(), combinedContactList.get(i).getUserstatus()));
							}
						}
					}
					for(int i=0;i<sortedList.size();i++)
					{
						if(sortedList.get(i).getName() == "GROUPS" || sortedList.get(i).getName() == "PEOPLE ON RING-A-BELL" || sortedList.get(i).getName() == "NON-APP USERS")
						{
							sortedList.remove(i);
						}
					}
					mylistview.setAdapter(new ContactAdapter(DisplayContactActivity.this,sortedList));
				}
			}
		});

	//	contactArray=new ArrayList<String>();
		//server contact list nobile number
		phoneList=new ContactLocalDB(mContext).getAllContactNumber();
		
		
		mylistview.setOnItemClickListener(this);
		setupUI(findViewById(R.id.contLayout));

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		return true;
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Take appropriate action for each action item click

		switch (item.getItemId()) {

		case R.id.rece_done:
			List<Contact> selectedList=new ArrayList<Contact>();
			//Toast.makeText(getApplicationContext(),"Map="+selectedContactMap.size() , Toast.LENGTH_SHORT).show();
			for (Entry<Integer, List<Contact>> ee : selectedContactMap.entrySet()) {
				selectedList.addAll(ee.getValue());
			}
			
			Intent returnIntent = new Intent();
			returnIntent.putExtra("LIST",(ArrayList<Contact>) selectedList);
			setResult(RESULT_OK,returnIntent);
			finish();


			return true;
		default:
			return super.onOptionsItemSelected(item);
		}

		//return true;
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View view, int arg2, long arg3) {
		// TODO Auto-generated method stub
		TextView name = (TextView) view.findViewById(R.id.contact_no);
		name.performClick();
	}
	@SuppressLint("NewApi")
	public void updateActionBar() {
		int selectCount = new ContactAdapter().getItemSelectCount();
		actionbar.setSubtitle(selectCount + " selected");
	}

	public void setupUI(View view) {

		//Set up touch listener for non-text box views to hide keyboard.
		if(!(view instanceof EditText)) {

			view.setOnTouchListener(new OnTouchListener() {

				@Override
				public boolean onTouch(View v, MotionEvent event) {
					// TODO Auto-generated method stub
					hideSoftKeyboard(DisplayContactActivity.this);
					return false;
				}
			});
		}

		//If a layout container, iterate over children and seed recursion.
		if (view instanceof ViewGroup) {

			for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
				View innerView = ((ViewGroup) view).getChildAt(i);
				setupUI(innerView);
			}
		}
	}

	public static void hideSoftKeyboard(Activity activity) {
		InputMethodManager inputMethodManager = (InputMethodManager)  activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
		inputMethodManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
	}


	public static boolean hasHoneycomb() {
		return Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB;
	}

	public ArrayList<Contact> matchContact(List <Contact> cursorList,ArrayList<String> existList){
		ArrayList<Contact> temp=new ArrayList<Contact>();
		System.out.println("Cursor List Size"+cursorList.size()+"existList size="+existList.size());
		for(int i=0; i<cursorList.size();i++){

			if(!existList.contains(cursorList.get(i).getPhoneNo())){
				temp.add(new Contact(cursorList.get(i).getId(),cursorList.get(i).getName(), cursorList.get(i).
						getPhoneNo(),cursorList.get(i).getOriginalPhoneNumber(),cursorList.get(i).getLookUp(),cursorList.
						get(i).getUrl(),cursorList.get(i).getUserstatus()));
			}
		}
		System.out.println(" Cursor Temp List Size"+temp.size());

		return temp;
	}
	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		super.onBackPressed();
		//finish();
	}

	private class LoadContact extends AsyncTask<Void, Void, Void>{

		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();
			pb.setVisibility(View.VISIBLE);
			combinedContactList=new ArrayList<Contact>();

		}
		@Override
		protected Void doInBackground(Void... params) {
			// TODO Auto-generated method stub

			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);


			//System.out.println("Contact id="+idList);

			//contactListother=SplashScreen.newContactList;
			//System.out.println("EXISTING LIST SIZE"+contactListother.size());

			//groups
			GroupList=new GroupsLocalDB(getApplicationContext()).getAllGroupList();
			//GroupList=(ArrayList<Group>) ReminderMainActivity.AppGroupList;

			if (!GroupList.isEmpty()){
				combinedContactList.add(new Contact("GROUPS","G","G"));
				for (Group gp : GroupList) {
					String gname= gp.getName();
					String grp_id= gp.getGroupId();	
					combinedContactList.add(new Contact(gname,"", grp_id));				
				}
			}

			//app user
			serverContactList=new ContactLocalDB(getApplicationContext()).getAllContactList();
			//serverContactList=ReminderMainActivity.AppContactList;
			System.out.println("APPP Contacts: "+serverContactList.size());
			//serverContactList=(ArrayList<Contact>) ReminderMainActivity.AppContactList;
			if (!serverContactList.isEmpty()){
				serverContactList.add(0,new Contact("PEOPLE ON RING-A-BELL","A","A"));
			}

			//non app user
			localContactList=(ArrayList<Contact>) ReminderMainActivity.NonAppcontactList;
			if(localContactList.get(0).getName().equals("NON-APP USERS")){
				localContactList.remove(0); 
			}
			if (!localContactList.isEmpty()){
				localContactList.add(0,new Contact("NON-APP USERS", "NA","NA"));
			}
			combinedContactList.addAll(serverContactList);
			combinedContactList.addAll(localContactList);
			adapter = new ContactAdapter(getApplicationContext(), combinedContactList);
			mylistview.setAdapter(adapter);
			adapter.notifyDataSetChanged();
			pb.setVisibility(View.GONE);
		}
	}
	

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		Thread th = new Thread(){
			public void run(){
				Looper.prepare();
				// Preparing get data params
				ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
				params.add(new BasicNameValuePair("blockuser", sharedPref.getString("USERNAME", null)));
				ServiceHandler jsonParser = new ServiceHandler();
				try{
					String json = jsonParser.makeServiceCall(ServiceHandler.URL_RETRIEVE_BLOCK_USER, ServiceHandler.GET,params);
					//String json = jsonParser.makeServiceCall("http://tech-sync.com/ringabell/retrieve_block_owner.php?blockuser=9795268946", ServiceHandler.GET,params);
					Log.v("Response: ", "> " + json);
					System.out.println("ARRAY IS="+json+"length is="+json.length());
					if(!json.equals("error")){
						if (json != null && json.length()>3) {
							try {
								JSONArray array=new JSONArray(json);
								JSONObject obj=null;
								for(int i=0;i<array.length();i++){
									obj=array.getJSONObject(i);
									String bid=obj.getString("block_user_id");
									String buser=obj.getString("block_user");
									String bowner=obj.getString("block_owner");
									String bstatus=obj.getString("status");

									if(new ContactLocalDB(getApplicationContext()).getBlockedOwners()>0){
										new ContactLocalDB(mContext).clearTable();
										new ContactLocalDB(getApplicationContext()).insertBlockLocal(bid, buser, bowner, bstatus);
									}
									else{
										new ContactLocalDB(getApplicationContext()).insertBlockLocal(bid, buser, bowner, bstatus);
									}
								}

							} catch (JSONException e) {
								e.printStackTrace();
							}
						} else {
							//existingUser=false;
							new ContactLocalDB(mContext).clearTable();
							Log.e("JSON Data", "Didn't receive any data from server!");
						}
					}
					else{
						//
					}

				}catch(Exception e){
					e.printStackTrace();
					//Toast.makeText(mContext, "Something wrong with network, Please try later", Toast.LENGTH_SHORT).show();
				}				
				Looper.loop();
			}
		};
		if(cDetector.isConnectingToInternet()){
			th.start();
		}
		new LoadContact().execute();
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

	public class ContactAdapter extends BaseAdapter  {

		Context context;
		//List<Contact> rowItems;
		//Map<Integer, List<Contact>> selectedContactMap=new HashMap<Integer, List<Contact>>();
		List<Contact> rowItems=new ArrayList<Contact>(); 
		boolean[] itemChecked;

		protected ImageLoader imageLoader;
		DisplayImageOptions options;
		private ImageLoadingListener animateFirstListener = new AnimateFirstDisplayListener();


		int groupcount = 1;

		public ContactAdapter() {
			// TODO Auto-generated constructor stub
		}

		ContactAdapter(Context context, List<Contact> rowItems) {
			this.context = context;
			this.rowItems = rowItems;

			itemChecked = new boolean[rowItems.size()];
			selectedContactMap = new HashMap<Integer, List<Contact>>();
			selectcount=0;
			groupcount=1;

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
			ImageView profile_pics;
			QuickContactBadge profile_pic;
			TextView member_name;
			TextView contactNo;
			CheckBox chk;
		}

		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {

			final ViewHolder holder;

			LayoutInflater mInflater = (LayoutInflater) context
					.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
			//if (convertView == null) {
			convertView = mInflater.inflate(R.layout.contact_list_item, null);
			holder = new ViewHolder();

			holder.member_name = (TextView) convertView
					.findViewById(R.id.contact_name);
			holder.profile_pic = (QuickContactBadge) convertView
					.findViewById(R.id.profilepic);
			holder.contactNo = (TextView) convertView.findViewById(R.id.contact_no);
			holder.chk = (CheckBox) convertView.findViewById(R.id.chkbox);

			holder.chk.setVisibility(View.GONE);

			// add or setting data's to listItem row
			final Contact row_pos = rowItems.get(position);
			System.out.println("kj " +row_pos.getName());
			if((row_pos.getName().equals("PEOPLE ON RING-A-BELL")) || row_pos.getName().equals("NON-APP USERS") || row_pos.getName().equals("GROUPS")){
				convertView = mInflater.inflate(R.layout.list_item_section, null);

				convertView.setOnClickListener(null);				
				convertView.setOnLongClickListener(null);
				convertView.setLongClickable(false);

				final TextView sectionView = (TextView) convertView.findViewById(R.id.sectionView);
				sectionView.setText(row_pos.getName());
				if(row_pos.getName() == "GROUPS"){
					final ImageView pic = (ImageView) convertView.findViewById(R.id.pic);
					pic.setImageResource(R.drawable.group_icon);
				}
				//Toast.makeText(mContext, "g" , Toast.LENGTH_LONG).show();
			}
			else{
				// holder.profile_pic.setImageResource(row_pos.getLookUp());
				holder.member_name.setText(row_pos.getName());
				holder.contactNo.setText(row_pos.getPhoneNo());

				if(row_pos.getPhoneNo().equals("")){
					imageLoader.displayImage(new GroupsLocalDB(mContext).getGroupPicUrl(row_pos.getOriginalPhoneNumber()), holder.profile_pic , options, animateFirstListener);
					/* try {
							holder.profile_pic.setImageURI(Uri.parse(row_pos.getUrl()));
						} catch (Exception e) {
							
							holder.profile_pic.setImageResource(R.drawable.ic_user);
							e.printStackTrace();
						}*/
				}
				else{		
					if (serverContactList.size()+1<position) {
						 try {
								holder.profile_pic.setImageURI(Uri.parse(row_pos.getUrl()));
							} catch (Exception e) {
								
								holder.profile_pic.setImageResource(R.drawable.ic_user);
								e.printStackTrace();
							}	
					}else {
						imageLoader.displayImage(new ContactLocalDB(mContext).getContactPicUrl(row_pos.getPhoneNo()), holder.profile_pic , options, animateFirstListener);
						
					}
					
				}

				holder.contactNo.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						List<Contact> tempList=new ArrayList<Contact>();
						tempList.add(new Contact(row_pos.getName(), row_pos.getPhoneNo(), row_pos.getOriginalPhoneNumber() ));
						selectedContactMap.put(position, tempList);
						List<Contact> selectedList=new ArrayList<Contact>();
						for (Entry<Integer, List<Contact>> ee : selectedContactMap.entrySet()) {
							selectedList.addAll(ee.getValue());
						}
						Intent returnIntent = new Intent();
						returnIntent.putExtra("LIST",(ArrayList<Contact>) selectedList);
						setResult(RESULT_OK,returnIntent);
						finish();
					}
				});
			}

			convertView.setTag(holder);
			return convertView;
		}

		int getItemSelectCount() {

			return selectcount;
		}
	}
}
