package com.share2people.ringabell;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;




import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;
import com.ringabell.localdb.ContactLocalDB;
import com.ringabell.model.Contact;
import com.share2people.ringabell.R;
import com.share2people.ringabell.DisplayContactActivity.ContactAdapter;

import android.os.Bundle;
import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

public class ServerContactActivity extends Activity implements OnItemClickListener{

	List<Contact> rowItems;
	ListView mylistview;
	ContactsAdapter adapter;
	static ActionBar actionbar;
	EditText inputsearch;	 
	DisplayImageOptions options;
	ArrayList<Contact> sortedList;
	int textlength = 0;

	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_server_contact);

		//getActionBar().setTitle(getString(R.string.app_users));
		//getActionBar().setDisplayHomeAsUpEnabled(true);
		//getActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.group_main_action_bar)));

		actionbar = getActionBar();
		actionbar.setDisplayHomeAsUpEnabled(true);
		actionbar.setTitle(R.string.app_users);
		actionbar.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.group_main_action_bar)));

		actionbar.setSubtitle("0 selected");
		inputsearch = (EditText) findViewById(R.id.inputSearch);
		mylistview = (ListView) findViewById(R.id.list);


		rowItems = new ArrayList<Contact>();
		rowItems=new ContactLocalDB(getApplicationContext()).getAllContactListGroup();
		adapter = new ContactsAdapter(this, rowItems);
		mylistview.setAdapter(adapter);
		mylistview.setOnItemClickListener(this);


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
				for (int i = 0; i < rowItems.size(); i++)
				{
					if (textlength <= rowItems.get(i).getPhoneNo().length() || 
							textlength <= rowItems.get(i).getName().length())
					{

						if(rowItems.get(i).getPhoneNo().contains(inputsearch.getText().toString().trim()) || 
								rowItems.get(i).getName().toLowerCase().contains(inputsearch.getText().toString().toLowerCase().trim()))
						{
							sortedList.add(new Contact(rowItems.get(i).getId(), rowItems.get(i).getName(),
									rowItems.get(i).getPhoneNo(), rowItems.get(i).getOriginalPhoneNumber(),
									rowItems.get(i).getLookUp(), rowItems.get(i).getUrl(), rowItems.get(i).getUserstatus()));
						}
					}
				}

				mylistview.setAdapter(new ContactsAdapter(ServerContactActivity.this,sortedList));
			}
		});
	}	

	public static void initImageLoader(Context context) {
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


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_server_contact, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Take appropriate action for each action item click

		switch (item.getItemId()) {

		case R.id.menu_settings:
			// check for updates action        	
			List<Contact> selectedList=new ArrayList<Contact>();
			//Toast.makeText(getApplicationContext(),"Map="+ContactsAdapter.selectedContactMap.size() , Toast.LENGTH_SHORT).show();
			for (Entry<Integer, List<Contact>> ee : ContactsAdapter.selectedContactMap.entrySet()) {				
				//selectedList = ee.getValue();
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
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View view, int arg2, long arg3) {
		// TODO Auto-generated method stub
		CheckBox chk = (CheckBox) view.findViewById(R.id.chk);
		chk.performClick();
	}

	@SuppressLint("NewApi")
	static void updateActionBar() {
		int selectCount = ContactsAdapter.getItemSelectCount();
		actionbar.setSubtitle(selectCount + " selected");
	}
}
