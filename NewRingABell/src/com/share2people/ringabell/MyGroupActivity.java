package com.share2people.ringabell;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import android.annotation.SuppressLint;
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
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
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
import com.ringabell.localdb.ReminderLocalDB;
import com.ringabell.model.Contact;
import com.ringabell.model.Group;
import com.ringabell.serverdb.ServiceHandler;
import com.ringabell.utils.AlertDialogManager;
import com.ringabell.utils.ConnectionDetector;
import com.share2people.ringabell.R;

import de.timroes.swipetodismiss.SwipeDismissList;

public class MyGroupActivity extends Activity implements OnClickListener {

	ListView listView;
	TextView noContacts;
	GroupContactAdapter adapter;
	ArrayList<Group> myGroupList;
	private Context mContext;
	private ProgressDialog pDialog;
	ConnectionDetector cDetector;
	AlertDialogManager alert = new AlertDialogManager();
	SharedPreferences sharedPref;
	SharedPreferences.Editor editor;
	private SwipeDismissList mSwipeList;


	boolean skipFlag=false;
	private String owner="", gid="", gpname="";
	private boolean owner_group=false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_my_group);		

		getActionBar().setTitle(getString(R.string.groups));
		getActionBar().setDisplayHomeAsUpEnabled(true);
		getActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.block_main_action_bar)));


		listView=(ListView) findViewById(R.id.grouplistView);
		noContacts=(TextView) findViewById(R.id.nogroupContacts);
		mContext=(Context) getApplicationContext();
		cDetector = new ConnectionDetector(getApplicationContext());		 

		sharedPref=getSharedPreferences("LAST_LOGIN", Activity.MODE_PRIVATE);
		skipFlag=sharedPref.getBoolean("SKIP", false);
		owner=sharedPref.getString("USERNAME", "");	
	}

	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		super.onBackPressed();		
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		if(owner_group){
			new GroupsLocalDB(getApplicationContext()).deleteGroup(gid);
			new GroupsLocalDB(getApplicationContext()).deleteMembers(gid);
			new DeleteGroup().execute();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.activity_add_group, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Take appropriate action for each action item click

		switch (item.getItemId()) {
		case R.id.action_add_group:
			editor=sharedPref.edit();
			editor.putBoolean("Editing", false);
			editor.commit();
			Intent groupIntent =new Intent(MyGroupActivity.this,AddGroupActivity.class);
			startActivity(groupIntent);
			return true;			

		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
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
				MyGroupActivity.this.finish();
			}
		});
		// Showing Alert Message
		alertDialog.show();
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

	@SuppressLint("ViewHolder")
	private class GroupContactAdapter extends BaseAdapter  {

		Context context;
		List<Group> rowItems;
		protected ImageLoader imageLoader;
		DisplayImageOptions options;
		private ImageLoadingListener animateFirstListener = new AnimateFirstDisplayListener();


		GroupContactAdapter(Context context, List<Group> rowItems) {
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
			ImageView profile_pic, group_owner;
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
			holder.group_owner = (ImageView) convertView
					.findViewById(R.id.owner);
			holder.profile_pic = (ImageView) convertView
					.findViewById(R.id.profile_pic);
			holder.status = (TextView) convertView.findViewById(R.id.status);

			// add or setting data's to listItem row
			final Group row_pos = rowItems.get(position);
			if(row_pos.getGroupOwner().equals(owner)){
				holder.group_owner.setVisibility(View.VISIBLE);
			}
			holder.member_name.setText(row_pos.getName());
			holder.status.setText(row_pos.getGroupStatus());
			imageLoader.displayImage(new GroupsLocalDB(mContext).getGroupPicUrl(row_pos.getGroupId()), holder.profile_pic, options, animateFirstListener);	  
			convertView.setTag(holder);
			return convertView;
		}
	}
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		
		if(new GroupsLocalDB(getApplicationContext()).countGroups()>0){
			myGroupList=new GroupsLocalDB(getApplicationContext()).getAllGroupList();
			adapter=new GroupContactAdapter(getApplicationContext(), myGroupList);
			noContacts.setVisibility(View.GONE);
			listView.setVisibility(View.VISIBLE);
			listView.setAdapter(adapter);
		}

		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position,
					long id) {
				// TODO Auto-generated method stub
				gid = ((Group) parent.getAdapter().getItem(position)).getGroupId();
				String gpname = ((Group) parent.getAdapter().getItem(position)).getName();
				String gstatus = ((Group) parent.getAdapter().getItem(position)).getGroupStatus();
				String gowner = ((Group) parent.getAdapter().getItem(position)).getGroupOwner();

				editor=sharedPref.edit();
				editor.putBoolean("Editing", true);
				editor.putString("groupid", gid);
				editor.putString("gpname", gpname);
				editor.putString("gpstatus", gstatus);
				editor.putString("gowner", gowner);
				editor.commit();
				Intent i = new Intent(MyGroupActivity.this, AddGroupActivity.class);
				i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(i);
			}
		});


		SwipeDismissList.UndoMode mode = SwipeDismissList.UndoMode.values()[0];		
		final ListView mListView=listView; 

		mSwipeList = new SwipeDismissList(
				// 1st parameter is the ListView you want to use
				mListView,					
				new SwipeDismissList.OnDismissCallback() {

					public SwipeDismissList.Undoable onDismiss(AbsListView listView, final int position) {

						gpname=myGroupList.get(position).getName();
						String gowner=myGroupList.get(position).getGroupOwner();
						gid=myGroupList.get(position).getGroupId();
						if(cDetector.isConnectingToInternet()){
						if(gowner.equals(owner)){
							owner_group=true;
							myGroupList.remove(position);
							adapter.notifyDataSetChanged();

						return new SwipeDismissList.Undoable() {

							@Override
							public String getTitle() {
								if(owner_group){
									return gpname + " Deleted";
								}
								else{
									return "You Cannot Delete This Group";
								}
							}
							
							@Override
							public void undo() {
								// Reinsert the item at its previous position.
								if(owner_group){
									myGroupList=new GroupsLocalDB(getApplicationContext()).getAllGroupList();
									adapter=new GroupContactAdapter(getApplicationContext(), myGroupList);
									mListView.setAdapter(adapter);
									owner_group=false;
								}								
							}

							@Override
							public void discard() {
								owner_group=false;
								new GroupsLocalDB(getApplicationContext()).deleteGroup(gid);
								new GroupsLocalDB(getApplicationContext()).deleteMembers(gid);
								new DeleteGroup().execute();
							}
						};
						}
						else{
							Toast.makeText(getApplicationContext(), "Sorry! you cannot delete this group ", Toast.LENGTH_LONG).show();
							return null;
						}
						}
						else{
							alert.showAlertDialog(MyGroupActivity.this,
									"Internet Connection Error",
									"Please Enable Your Internet Connection.", false);
							return null;
						}
					}				
				},
				// 3rd parameter needs to be the mode the list is generated.
				mode);
	}

	private class DeleteGroup extends AsyncTask<Void, Void, Void> {

		boolean err=false;
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}

		@Override
		protected Void doInBackground(Void... arg) {

			ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("group_id", gid));


			ServiceHandler serviceClient = new ServiceHandler();
			try{
				String json = serviceClient.makeServiceCall(ServiceHandler.URL_DELETE_GROUP,
						ServiceHandler.GET, params);
				System.out.println("Delete Group ="+json);
				if(!json.equals("error")){
					Log.d("Delete Group Response: ", "> " + json);
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
				new GroupsLocalDB(getApplicationContext()).deleteGroup(gid);
				new GroupsLocalDB(getApplicationContext()).deleteMembers(gid);
			}
			else{
				//Toast.makeText(mContext, "Something wrong", Toast.LENGTH_SHORT).show();
			}
			//}
		}
	}
}
