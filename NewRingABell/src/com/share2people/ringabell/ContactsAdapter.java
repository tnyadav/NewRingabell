package com.share2people.ringabell;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;


import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.ringabell.model.Contact;
import com.share2people.ringabell.R;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.TextView;

public class ContactsAdapter extends BaseAdapter  {

	Context context;
	List<Contact> rowItems;
	static Map<Integer, List<Contact>> selectedContactMap=new HashMap<Integer, List<Contact>>();
	private ImageLoadingListener animateFirstListener = new AnimateFirstDisplayListener();
	protected ImageLoader imageLoader;
	//List<Contact> selectedContact=new ArrayList<Contact>(); 
	boolean[] itemChecked;
	DisplayImageOptions options;
	static int selectcount = 0;
	//static String imgUrl="http://tech-sync.com/ringabell/picture/8826327669.jpg"; 

	ContactsAdapter(Context context, List<Contact> rowItems) {
		this.context = context;
		this.rowItems = rowItems;
		itemChecked = new boolean[rowItems.size()];
		selectcount=0;
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
		CheckBox chk;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {

		final ViewHolder holder;

		LayoutInflater mInflater = (LayoutInflater) context
				.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
		//if (convertView == null) {
			convertView = mInflater.inflate(R.layout.listview_layout, null);
			holder = new ViewHolder();

			holder.member_name = (TextView) convertView
					.findViewById(R.id.member_name);
			holder.profile_pic = (ImageView) convertView
					.findViewById(R.id.profile_pic);
			holder.status = (TextView) convertView.findViewById(R.id.status);
			holder.chk = (CheckBox) convertView.findViewById(R.id.chk);

			// add or setting data's to listItem row
			final Contact row_pos = rowItems.get(position);

			// holder.profile_pic.setImageResource(row_pos.getLookUp());
			holder.member_name.setText(row_pos.getName());
			System.out.println("PPPP::"+row_pos.getUrl());
			holder.status.setText(row_pos.getOriginalPhoneNumber());
			holder.chk.setChecked(false);
			imageLoader.displayImage(row_pos.getUrl(), holder.profile_pic, options, animateFirstListener);
			if (itemChecked[position])
				holder.chk.setChecked(true);
			else
				holder.chk.setChecked(false);

			/*holder.chk.setOnCheckedChangeListener(new OnCheckedChangeListener() {

	@Override
	public void onCheckedChanged(CompoundButton arg0, boolean isChecked) {
		// TODO Auto-generated method stub
		if (isChecked) {
		      itemChecked[position] = true;
		      selectcount++;
		      List<Contact> tempList=new ArrayList<Contact>();
		      tempList.add(new Contact(row_pos.getId(), row_pos.getName(), row_pos.getPhoneNo(), row_pos.getOriginalPhoneNumber()));
		      selectedContactMap.put(position, tempList);

		     } else {
		      itemChecked[position] = false;
		      selectcount--;
		      selectedContactMap.remove(position);
		     }
		     ServerContactActivity.updateActionBar();

	}
});
			 */ holder.chk.setOnClickListener(new OnClickListener() {

				 @Override
				 public void onClick(View v) {

					 if (holder.chk.isChecked()) {
						 itemChecked[position] = true;
						 selectcount++;
						 List<Contact> tempList=new ArrayList<Contact>();
						 tempList.add(new Contact(row_pos.getId(), row_pos.getName(), row_pos.getPhoneNo(), row_pos.getOriginalPhoneNumber(),row_pos.getUrl()));
						 selectedContactMap.put(position, tempList);

					 } else {
						 itemChecked[position] = false;
						 selectcount--;
						 selectedContactMap.remove(position);
					 }
					 ServerContactActivity.updateActionBar();

				 }
			 });

			 convertView.setTag(holder);
		//}

		return convertView;
	}

	static int getItemSelectCount() {

		return selectcount;
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
}