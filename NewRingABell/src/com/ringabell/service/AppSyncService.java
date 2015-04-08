package com.ringabell.service;


import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.Map.Entry;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.PhoneNumberUtil.PhoneNumberFormat;
import com.google.i18n.phonenumbers.Phonenumber.PhoneNumber;
import com.ringabell.localdb.ContactLocalDB;
import com.ringabell.localdb.GroupsLocalDB;
import com.ringabell.model.Contact;
import com.ringabell.model.Reminder;
import com.ringabell.serverdb.ServiceHandler;
import com.ringabell.utils.ConnectionDetector;

import android.app.Activity;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Build;
import android.os.IBinder;
import android.os.Looper;
import android.provider.ContactsContract;
import android.telephony.PhoneNumberUtils;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.ListView;
import android.widget.Toast;


public class AppSyncService extends IntentService {


	boolean skipFlag=false;
	private SharedPreferences sharedPref;

	LayoutInflater inflator;
	List<Reminder> reminderList;
	ConnectionDetector cDetector;
	private List<Contact> contactList;
//	private static ArrayList<String> phoneList;
	private static List<Contact> existingContactListInPhoneBookWithAllInfo;
//	private static List<Contact> NonAppcontactList;
	private List <String> idList;
	private List <String> bnumberList;
	HashMap<String, String> regIdMap;
	HashMap<String, Contact> contactMap;
	String selectedItem="";
	final int DATE_DIALOG_ID = 1;
	Calendar calendar;
	int day,month,year;
	Timer timer;
	private static List<String> existingContactListInPhoneBookWithOnlyNumber;
	private static String phone;
	private static Context mContext;
	String grp_user = "";
	String filename = "";


	public AppSyncService() {
		super(AppSyncService.class.getName());
		// TODO Auto-generated constructor stub
	}
	
	@Override
	protected void onHandleIntent(Intent intent) {
		// TODO Auto-generated method stub
		mContext=(Context) getApplicationContext();
		sharedPref = getSharedPreferences("LAST_LOGIN", Activity.MODE_PRIVATE);
		System.out.println("APP USERS SYNCING");
		grp_user = sharedPref.getString("USERNAME", "");
		filename = sharedPref.getString("PHOTO_FILENAME", "");

		getAppUsers();
	}
	
	public void getAppUsers(){
		
		boolean error=false;
		contactList=new ArrayList<Contact>();
		idList=new ArrayList<String>();
		regIdMap=new HashMap<String, String>();
		contactMap=new HashMap<String, Contact>();
		regIdMap.clear();
		
		ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
		String val="";
		try{
			existingContact();
			val=combinedString(existingContactListInPhoneBookWithOnlyNumber);
			System.out.println("listtt  :  " + existingContactListInPhoneBookWithOnlyNumber);

		}
		catch(Exception e){
			e.printStackTrace();
		}
		params.add(new BasicNameValuePair("username", val));
		params.add(new BasicNameValuePair("sender", sharedPref.getString("USERNAME", "")));


		ServiceHandler jsonParser = new ServiceHandler();

		String json = jsonParser.makeServiceCall(ServiceHandler.URL_RETRIEVE_FRIENDS, ServiceHandler.POST,params);
		if(!json.equals("error")){

			//Log.e("Response: ", "> " + json);
			//System.out.println("contacts length is in service="+json.length());

			if (json != null && json.length()>3) {
				try {
					JSONArray array=new JSONArray(json);
					JSONObject obj=null;
					
					for(int i=0;i<array.length();i++){
						obj=array.getJSONObject(i);
						String number=obj.getString("username");
						String regid=obj.getString("reg_id");
						String deviceid=obj.getString("device_id");
						String profilePic=obj.getString("profile_pic");
						String userstatus=obj.getString("user_status");
						String deviceIdentification=obj.getString("device_identification");
						idList.add(number);
						contactMap.put(number, new Contact(number,regid,profilePic,Integer.parseInt(deviceIdentification),userstatus));
						System.out.println("numberrr  :  " + number);
					}
					String myPicUrl= filename;
					contactMap.put(grp_user, new Contact(grp_user,"",myPicUrl,0,""));
					System.out.println("numberrr  :  " + grp_user);

				} catch (JSONException e) {
					e.printStackTrace();
				}
			} else {
				Log.e("JSON Data service 3", "Didn't receive contact data from server!");
			}
		}
		else
			error=true;
		
		if(!error){
			try{
				System.out.println("sizeee " + existingContactListInPhoneBookWithAllInfo.size());
				System.out.println("sizeee " + idList.size());
				List<Contact> mm=matchContact(existingContactListInPhoneBookWithAllInfo,idList);
				//store contact locally
				System.out.println("sizeee " + mm.size());
				for(int k=0;k<mm.size();k++){
					System.out.println("inseteddd");
					new ContactLocalDB(mContext).updateContactName(mm.get(k).getPhoneNo(),mm.get(k).getName());
					if(!new ContactLocalDB(getBaseContext()).checkIfContactExists(mm.get(k).getPhoneNo())){
						new ContactLocalDB(getBaseContext()).insertContactLocal(mm.get(k).getId(),mm.get(k).getPhoneNo(), mm.get(k).getName(),mm.get(k).getOriginalPhoneNumber(),mm.get(k).getLookUp(),mm.get(k).getUrl());
					}
					new ContactLocalDB(mContext).updateContactFlag(mm.get(k).getPhoneNo());
				}
				new ContactLocalDB(mContext).updateContactFlag(grp_user);
				new ContactLocalDB(getBaseContext()).deleteContact("0");
				new ContactLocalDB(getBaseContext()).updateContactFlagAgain();

				for (Entry<String, Contact> eee : contactMap.entrySet()) {
					//String contactName= new ContactLocalDB(getBaseContext()).getContactName(eee.getValue().getPhoneNo());
					new ContactLocalDB(getBaseContext()).updateRegID(eee.getKey(), eee.getValue().getRegID(), eee.getValue().getUrl(),eee.getValue().getDeviceIdentification(),eee.getValue().getUserstatus());
					System.out.println("Updated");
				}
				//System.out.println("APPP Contacts: "+new ContactLocalDB(getApplicationContext()).getAllContactList().size());
			}
			catch(NullPointerException nException){
				nException.printStackTrace();
			}
		}		
	}

	//fetch data from database
	private class GetExistingBlockedContact extends AsyncTask<String, Void, Void> {
		boolean error=false;	
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			bnumberList=new ArrayList<String>();
		}

		@Override
		protected Void doInBackground(String... arg) {

			// Preparing get data params
			ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
			String blockowner=grp_user;
			params.add(new BasicNameValuePair("blockowner", blockowner));
			ServiceHandler jsonParser = new ServiceHandler();

			String json = jsonParser.makeServiceCall(ServiceHandler.URL_RETRIEVE_BLOCK_CONTACTS, ServiceHandler.GET,params);
			if(!json.equals("error")){

				Log.e("Response: ", "> " + json);
				System.out.println("BLOCKED USERS length is="+json.length());

				if (json != null && json.length()>3) {
					try {
						JSONArray array=new JSONArray(json);
						JSONObject obj=null;
						for(int i=0;i<array.length();i++){
							obj=array.getJSONObject(i);
							String bnumber=obj.getString("block_user");
							//System.out.println("username=::"+bnumber);
							bnumberList.add(bnumber);
						}
					} catch (JSONException e) {
						e.printStackTrace();
					}

				} else {
					Log.e("JSON Data service", "Didn't receive any data from server!");
				}
			}
			else
				error=true;

			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			if(!error){
				try{
					for(int k=0;k<bnumberList.size();k++){
						if(new ContactLocalDB(mContext).checkIfContactExists(bnumberList.get(k))){
							new ContactLocalDB(mContext).updateBlockUser(bnumberList.get(k));
						}						
					}					
				}
				catch(NullPointerException nException){
					nException.printStackTrace();
				}
			}			
		}
	}

	private List<Contact> matchContact(List <Contact> cursorList,List<String> existList){
		List<Contact> temp=new ArrayList<Contact>();
		

		for(int i=0; i<cursorList.size();i++){

			if(existList.contains(cursorList.get(i).getPhoneNo())){
				temp.add(new Contact(cursorList.get(i).getId(), cursorList.get(i).getName(), cursorList.get(i).getPhoneNo(),
						cursorList.get(i).getOriginalPhoneNumber(),cursorList.get(i).getLookUp(),
						cursorList.get(i).getUrl(), cursorList.get(i).getUserstatus()));
			}
		}

		return temp;
	}

	public static ArrayList<Contact> matchContact(List <Contact> cursorList,ArrayList<String> existList){
		ArrayList<Contact> temp=new ArrayList<Contact>();
		//System.out.println("Cursor List Size"+cursorList.size()+"existList size="+existList.size());
		for(int i=0; i<cursorList.size();i++){

			if(!existList.contains(cursorList.get(i).getPhoneNo())){
				temp.add(new Contact(cursorList.get(i).getId(),cursorList.get(i).getName(), cursorList.get(i).
						getPhoneNo(),cursorList.get(i).getOriginalPhoneNumber(),cursorList.get(i).getLookUp(),cursorList.
						get(i).getUrl(),cursorList.get(i).getUserstatus()));
			}
		}
		//System.out.println(" Cursor Temp List Size"+temp.size());
		return temp;
	}

	private  void existingContact(){
		existingContactListInPhoneBookWithOnlyNumber=new ArrayList<String>();
		existingContactListInPhoneBookWithAllInfo=new ArrayList<Contact>();

		existingContactListInPhoneBookWithOnlyNumber.clear();
		ContentResolver cr = mContext.getContentResolver();
		Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI,
				null,null,null, "display_name asc");

		if (cur.getCount() > 0) {
			while (cur.moveToNext()) {
				String id = cur.getString(
						cur.getColumnIndex(ContactsContract.Contacts._ID));
				String name = cur.getString(
						cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
				String lookup=cur.getString(cur.getColumnIndex(ContactsContract.Contacts.LOOKUP_KEY));

				String photoUrl;
				if(!hasHoneycomb())	 
					photoUrl=cur.getString(cur.getColumnIndex(ContactsContract.Contacts._ID));
				else
					photoUrl=cur.getString(cur.getColumnIndex(ContactsContract.Contacts.PHOTO_THUMBNAIL_URI));


				if (Integer.parseInt(cur.getString(cur.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {
					Cursor pCur = cr.query( ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?", new String[] { id }, null);
					while (pCur.moveToNext()) {
						phone = pCur .getString(pCur .getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
						//int stt= pCur.getInt(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.STARRED)); 
						//if(phone.length()>=10){
							try{
								existingContactListInPhoneBookWithOnlyNumber.add(convertMobilePattern(phone));
								existingContactListInPhoneBookWithAllInfo.add(new Contact(id, name,convertMobilePattern(phone),phone,lookup,photoUrl,""));
								/*phoneList=new ContactLocalDB(mContext).getAllContactNumber();
								NonAppcontactList=matchContact(newContactList,phoneList);*/

							}catch(Exception e){
								e.printStackTrace();
							}
						//}
					}
					pCur.close();
				}
			}
		}
		cur.close();
	}

	public  String convertMobilePattern(String number)
	{    
		/*String out = number.replaceAll("[^0-9\\+]", "")        //remove all the non numbers (brackets dashes spaces etc.) except the + signs
				.replaceAll("(^[1-9].+)", "$1")         //if the number is starting with no zero and +, its a local number. prepend cc
				.replaceAll("(.)(\\++)(.)", "$1$3");         //if there are left out +'s in the middle by mistake, remove them
				//.replaceAll("(^0{2}|^\\+)(.+)", "$2")       //make 00XXX... numbers and +XXXXX.. numbers into XXXX...
				//.replaceAll("^0([1-9])", "$1");
		//out=out.substring(out.length() - 10);
		//make 0XXXXXXX numbers into CCXXXXXXXX numbers
		Log.e("converted number", out);
		PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
	
		try {
		    // phone must begin with '+'
		    PhoneNumber numberProto = phoneUtil.parse(number, "");
		   String converted=phoneUtil.format(numberProto, PhoneNumberFormat.INTERNATIONAL);
			Log.e("converted number", converted);
		} catch (NumberParseException e) {
		    System.err.println("NumberParseException was thrown: " + e.toString());
		}
		return out;*/
		String converted = "";
		PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();

		try {
			TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
			String countryCode = tm.getSimCountryIso().toUpperCase();
			// phone must begin with '+'
			PhoneNumber numberProto = phoneUtil.parse(number, countryCode);
			
			converted = phoneUtil.format(numberProto, PhoneNumberFormat.E164);
			Log.e("converted number", converted);
		} catch (NumberParseException e) {
			System.err.println("NumberParseException was thrown: "
					+ e.toString());
		}
		return converted;

	}

	public static boolean hasHoneycomb() {
		return Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB;
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
		//System.out.println("SSSSSS"+ss);
		return ss;
	}

}
