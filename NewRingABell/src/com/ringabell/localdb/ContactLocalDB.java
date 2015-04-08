package com.ringabell.localdb;

import java.util.ArrayList;
import java.util.List;

import com.ringabell.model.Contact;
import com.ringabell.model.Group;



import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;



public class ContactLocalDB {
	private Context context;
	private ContactDBHelper sdbHelper;
	private SQLiteDatabase sdb;

	private static final String DATABASE_NAME = "Contact.db";
	private static final int DATABASE_VERSION = 1;
	private static final String TABLE_NAME = "Contacts";
	
	private static final String FIELD_ID = "id";
	private static final String FIELD_USER_NUMBER = "mobileNumber";
	private static final String FIELD_ORIGINAL_NUMBER = "originalNumber";
	private static final String FIELD_USER_NAME = "name";
	private static final String FIELD_LOOKUP = "lookupKey";
	private static final String FIELD_PHOTOURL = "photoUrl";
	private static final String FIELD_GCM_ID = "gcmId";
	private static final String FIELD_BLOCK_USER = "isBlock";
	private static final String FIELD_FLAG = "contactFlag";
	private static final String FIELD_DEVICE_IDENTIFICATION = "deviceIdentification";
	private static final String FIELD_USER_STATUS = "userStatus";

	private static final String TABLE_BLOCK = "BlockedContacts";
	private static final String BID = "blkid";
	private static final String BLOCK_USER_ID = "block_user_id";
	private static final String BLOCK_USER = "block_user";
	private static final String BLOCK_OWNER = "block_owner";
	private static final String BLOCK_STATUS = "block_status";
	
	
	
	public ContactLocalDB(Context c) {
		context = c;
		sdbHelper = new ContactDBHelper(context);
	}

	private ContactLocalDB open() {
		sdb = sdbHelper.getWritableDatabase();
		return this;
	}

	private void close() {
		sdb.close();
	}
public int getAllContacts(){
	open();
	
	int totalUsers=0;
	
	Cursor cursor = sdb.rawQuery("SELECT "+FIELD_USER_NUMBER+" FROM " + TABLE_NAME +" ORDER BY "+FIELD_USER_NAME, null);
	cursor.moveToFirst();
	totalUsers=cursor.getCount();
	System.out.println("get total users "+ totalUsers);
	close();
	return totalUsers;

}
public int getAllBlockedContacts(){
	open();
	
	int totalBlockUsers=0;
	
	Cursor cursor = sdb.rawQuery("SELECT "+FIELD_USER_NUMBER+" FROM " + TABLE_NAME +" where "+FIELD_BLOCK_USER+" = 1 "+" ORDER BY "+FIELD_USER_NAME, null);
	cursor.moveToFirst();
	totalBlockUsers=cursor.getCount();
	System.out.println("get total blocked users "+ totalBlockUsers);
	close();
	return totalBlockUsers;

}
public int getAllNonBlockedContacts(){
	open();
	String own="Me";
	int totalNonBlockUsers=0;
	
	Cursor cursor = sdb.rawQuery("SELECT "+FIELD_USER_NUMBER+" FROM " + TABLE_NAME +" where "+FIELD_BLOCK_USER+" = 0 "+" AND "+FIELD_USER_NAME+" != '"+own+"'"+" ORDER BY "+FIELD_USER_NAME, null);
	cursor.moveToFirst();
	totalNonBlockUsers=cursor.getCount();
	System.out.println("get total non blocked users "+ totalNonBlockUsers);
	close();
	return totalNonBlockUsers;

}

public int getBlockedOwners(){
	open();
	
	int totalBlockOwners=0;
	
	Cursor cursor = sdb.rawQuery("SELECT * FROM " + TABLE_BLOCK , null);
	cursor.moveToFirst();
	totalBlockOwners=cursor.getCount();
	//System.out.println("get total blocked owners "+ totalBlockOwners);
	close();
	return totalBlockOwners;

}

public void insertBlockLocal(String buserid, String buser,String bowner,String bstatus) {
	open();
	
	ContentValues values = new ContentValues();
	
	values.put(BLOCK_USER_ID, buserid);
	values.put(BLOCK_USER, buser);
	values.put(BLOCK_OWNER, bowner);
	values.put(BLOCK_STATUS, bstatus);
	try {
		sdb.insertOrThrow(TABLE_BLOCK, null, values);
	} catch (Exception e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}	
	close();
}

public ArrayList<Contact> getAllContactList(){
	open();
	ArrayList <Contact> contactList=new ArrayList<Contact>();
	Cursor cursor = sdb.rawQuery("SELECT * FROM " + TABLE_NAME +" ORDER BY "+FIELD_USER_NAME, null);
	while (cursor.moveToNext()) {
		contactList.add(new Contact(cursor.getString(cursor.getColumnIndex(FIELD_ID))
				,cursor.getString(cursor.getColumnIndex(FIELD_USER_NAME))
				,cursor.getString(cursor.getColumnIndex(FIELD_USER_NUMBER))
				,cursor.getString(cursor.getColumnIndex(FIELD_ORIGINAL_NUMBER))
				,cursor.getString(cursor.getColumnIndex(FIELD_LOOKUP))
				,cursor.getString(cursor.getColumnIndex(FIELD_PHOTOURL))
				,cursor.getString(cursor.getColumnIndex(FIELD_USER_STATUS))));		
	}
	close();
	System.out.println(" App User List="+contactList);
	return contactList;
}
//get all contact list for group
public ArrayList<Contact> getAllContactListGroup(){
	open();
	String own="Me";
	ArrayList <Contact> contactList=new ArrayList<Contact>();
	Cursor cursor = sdb.rawQuery("SELECT * FROM " + TABLE_NAME +" where "+FIELD_USER_NAME+" != '"+own+"'"+" ORDER BY "+FIELD_USER_NAME, null);
	while (cursor.moveToNext()) {
		contactList.add(new Contact(cursor.getString(cursor.getColumnIndex(FIELD_ID))
				,cursor.getString(cursor.getColumnIndex(FIELD_USER_NAME))
				,cursor.getString(cursor.getColumnIndex(FIELD_USER_NUMBER))
				,cursor.getString(cursor.getColumnIndex(FIELD_ORIGINAL_NUMBER))
				,cursor.getString(cursor.getColumnIndex(FIELD_LOOKUP))
				,cursor.getString(cursor.getColumnIndex(FIELD_PHOTOURL))
				,cursor.getString(cursor.getColumnIndex(FIELD_USER_STATUS))));		
	}
	close();
	System.out.println(" App User List="+contactList);
	return contactList;
}
//retrieve non-blocked contacts
public ArrayList<Contact> getAllNonBlockedContactList(){
	
	open();
	String own="Me";
	ArrayList <Contact> contactList=new ArrayList<Contact>();
	Cursor cursor = sdb.rawQuery("SELECT * FROM " + TABLE_NAME +" where "+FIELD_BLOCK_USER+" = 0 "+" AND "+FIELD_USER_NAME+" != '"+own+"'"+" ORDER BY "+FIELD_USER_NAME, null);
	while (cursor.moveToNext()) {
		contactList.add(new Contact(cursor.getString(cursor.getColumnIndex(FIELD_ID))
				,cursor.getString(cursor.getColumnIndex(FIELD_USER_NAME))
				,cursor.getString(cursor.getColumnIndex(FIELD_USER_NUMBER))
				,cursor.getString(cursor.getColumnIndex(FIELD_ORIGINAL_NUMBER))
				,cursor.getString(cursor.getColumnIndex(FIELD_LOOKUP))
				,cursor.getString(cursor.getColumnIndex(FIELD_PHOTOURL))
				,cursor.getString(cursor.getColumnIndex(FIELD_USER_STATUS))));		
	}
	close();
	System.out.println("Non-Blocked User List="+contactList);
	return contactList;
}

//retrieve blocked contacts
public ArrayList<Contact> getAllBlockedContactList(){
	open();
	ArrayList <Contact> contactList=new ArrayList<Contact>();
	Cursor cursor = sdb.rawQuery("SELECT * FROM " + TABLE_NAME +" where "+FIELD_BLOCK_USER+" = 1 "+" ORDER BY "+FIELD_USER_NAME, null);
	while (cursor.moveToNext()) {
		contactList.add(new Contact(cursor.getString(cursor.getColumnIndex(FIELD_ID))
				,cursor.getString(cursor.getColumnIndex(FIELD_USER_NAME))
				,cursor.getString(cursor.getColumnIndex(FIELD_USER_NUMBER))
				,cursor.getString(cursor.getColumnIndex(FIELD_ORIGINAL_NUMBER))
				,cursor.getString(cursor.getColumnIndex(FIELD_LOOKUP))
				,cursor.getString(cursor.getColumnIndex(FIELD_PHOTOURL))
				,cursor.getString(cursor.getColumnIndex(FIELD_USER_STATUS))));		
	}
	close();
	System.out.println("Blocked User List="+contactList);
	return contactList;
}


public ArrayList<String> getAllContactNumber(){
	open();
	ArrayList <String> numberList=new ArrayList<String>();
	Cursor cursor = sdb.rawQuery("SELECT "+FIELD_USER_NUMBER +" FROM " + TABLE_NAME , null);
	while (cursor.moveToNext()) {
		numberList.add(cursor.getString(cursor.getColumnIndex(FIELD_USER_NUMBER)));		
	}
	close();
	//System.out.println("Number List="+numberList);
	return numberList;
}

public String getGcmRegisrationId(String user_id){
	open();
	String regId="";
	Cursor cursor = sdb.rawQuery("SELECT "+FIELD_GCM_ID +" FROM " + TABLE_NAME + " WHERE "
			+ FIELD_USER_NUMBER + "= '" +user_id  +"'", null);
	if (cursor.moveToNext()) {
		regId=cursor.getString(cursor.getColumnIndex(FIELD_GCM_ID));
		close();
		return regId;
	}
	close();
	System.out.println("Registration ID="+regId);
	return regId;
}

	public boolean checkIfContactExists(String userId) {
		open();
		Cursor cursor = sdb.rawQuery("SELECT * FROM " + TABLE_NAME + " WHERE "
				+ FIELD_USER_NUMBER + "= '" + userId  +"'", null);
		if (cursor.moveToNext()) {
			System.out.println("Contact Exist");
			close();
			return true;
		}
		System.out.println("Contact Not Exist");
		close();
		return false;
	}
	
	public boolean checkIfBlocked(String receiverno) {
		open();
		Cursor cursor = sdb.rawQuery("SELECT * FROM " + TABLE_BLOCK + " WHERE "
				+ BLOCK_OWNER + " = '" + receiverno + "'", null);
		if (cursor.moveToNext()) {
			System.out.println("Contact Exist");
			close();
			return true;
		}
		System.out.println("Contact Not Exist");
		close();
		return false;
	}
	
	public String getContactPicUrl(String username){
		open();
		String picUrl="";
		Cursor cursor = sdb.rawQuery("SELECT "+FIELD_PHOTOURL +" FROM " + TABLE_NAME + " WHERE "
				+ FIELD_USER_NUMBER + "= '" +username  +"'", null);
		if (cursor.moveToNext()) {
			picUrl=cursor.getString(cursor.getColumnIndex(FIELD_PHOTOURL));
			System.out.println("Pic Url="+picUrl);
			close();
			return picUrl;
		}
		close();
		System.out.println("Pic Url="+picUrl);
		return picUrl;
		
	}
	
	public String getContactName(String unumber){
		open();
		String uname="";
		Cursor cursor = sdb.rawQuery("SELECT "+FIELD_USER_NAME +" FROM " + TABLE_NAME + " WHERE "
				+ FIELD_USER_NUMBER + "= '" +unumber  +"'", null);
		if (cursor.moveToNext()) {
			uname=cursor.getString(cursor.getColumnIndex(FIELD_USER_NAME));
			close();
			return uname;
		}
		close();
		return uname;
		
	}
	
	public List<Contact> getContactInfo(String unumber){
		open();
		List <Contact> contactList=new ArrayList<Contact>();
		Cursor cursor = sdb.rawQuery("SELECT * FROM " + TABLE_NAME + " WHERE "
				+ FIELD_USER_NUMBER + "= '" +unumber  +"'", null);
		contactList.add(new Contact(cursor.getString(cursor.getColumnIndex(FIELD_ID))
				,cursor.getString(cursor.getColumnIndex(FIELD_USER_NAME))
				,cursor.getString(cursor.getColumnIndex(FIELD_USER_NUMBER))
				,cursor.getString(cursor.getColumnIndex(FIELD_ORIGINAL_NUMBER))
				,cursor.getString(cursor.getColumnIndex(FIELD_LOOKUP))
				,cursor.getString(cursor.getColumnIndex(FIELD_PHOTOURL))
				,cursor.getString(cursor.getColumnIndex(FIELD_USER_STATUS))));	
		close();
		return contactList;
		
	}
	
	public void insertContactLocal(String id,String userNumber, String userName,String original,String lookup,String photoUrl) {
		open();	
		ContentValues values = new ContentValues();		
		values.put(FIELD_ID, id);
		values.put(FIELD_USER_NUMBER, userNumber);
		values.put(FIELD_USER_NAME, userName);
		values.put(FIELD_ORIGINAL_NUMBER, original);
		values.put(FIELD_LOOKUP, lookup);
		values.put(FIELD_PHOTOURL, photoUrl);
		values.put(FIELD_GCM_ID,"");
		values.put(FIELD_BLOCK_USER, 0);
		values.put(FIELD_FLAG, "0");
		try {
			sdb.insertOrThrow(TABLE_NAME, null, values);
			System.out.println("Contact Inserted");

		} catch (Exception e) {
			// TODO Auto-generated catch block
			System.out.println("Contact Not Inserted");
			e.printStackTrace();
		}		
		close();
	}
	
	public void updateRegID(String username,String regId,String profilePic,int deviceIdentification, String uStatus){
		open();
		ContentValues values=new ContentValues();
		values.put(FIELD_GCM_ID, regId);
		values.put(FIELD_PHOTOURL, profilePic);
		values.put(FIELD_DEVICE_IDENTIFICATION, deviceIdentification);
		values.put(FIELD_USER_STATUS, uStatus);
		sdb.update(TABLE_NAME, values, FIELD_USER_NUMBER+" = ?", new String[]{username});
		close();
	}
	
	public void updateContact(String username,String regId,String uname, String profilePic,int deviceIdentification, String uStatus){
		open();
		ContentValues values=new ContentValues();
		values.put(FIELD_GCM_ID, regId);
		values.put(FIELD_USER_NAME, uname);
		values.put(FIELD_PHOTOURL, profilePic);
		values.put(FIELD_DEVICE_IDENTIFICATION, deviceIdentification);
		values.put(FIELD_USER_STATUS, uStatus);
		sdb.update(TABLE_NAME, values, FIELD_USER_NUMBER+" = ?", new String[]{username});
		close();
	}
	
	
	public void updateUnBlockUser(String username){
		open();
		ContentValues values=new ContentValues();
		values.put(FIELD_BLOCK_USER, 0);
		
		sdb.update(TABLE_NAME, values, FIELD_USER_NUMBER+" = ?", new String[]{username});
		close();
	}
	
	public void updateBlockUser(String username){
		open();
		ContentValues values=new ContentValues();
		values.put(FIELD_BLOCK_USER, 1);
		
		sdb.update(TABLE_NAME, values, FIELD_USER_NUMBER+" = ?", new String[]{username});
		close();
	}
	
	public void updateContactFlag(String username){
		open();
		ContentValues values=new ContentValues();
		values.put(FIELD_FLAG, "1");		
		sdb.update(TABLE_NAME, values, FIELD_USER_NUMBER+" = ?", new String[]{username});
		close();
	}
	
	public void updateContactName(String userno,String username){
		open();
		ContentValues values=new ContentValues();
		values.put(FIELD_USER_NAME, username);		
		sdb.update(TABLE_NAME, values, FIELD_USER_NUMBER+" = ?", new String[]{userno});
		close();
	}
	
	public void updateContactFlagAgain(){
		open();
		ContentValues values=new ContentValues();
		values.put(FIELD_FLAG, "0");		
		sdb.update(TABLE_NAME, values, FIELD_FLAG+" = ?", new String[]{"1"});
		close();
	}
	
	public void deleteContact(String flag){
		open();
		sdb.delete(TABLE_NAME, FIELD_FLAG+" = ?", new String[]{flag});
		close();
	}
	
	public void clearTable(){
		open();
		sdb.delete(TABLE_BLOCK, null, null);
		close();
	}
	private class ContactDBHelper extends SQLiteOpenHelper {
		public ContactDBHelper(Context c) {
			super(c, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			System.out.println("Database tables created");
			db.execSQL("CREATE TABLE " + TABLE_NAME + "( " + FIELD_ID + " TEXT PRIMARY KEY , " 
			+FIELD_USER_NUMBER + " TEXT, " +FIELD_USER_NAME + " TEXT, " +FIELD_ORIGINAL_NUMBER + " TEXT, " 
			+FIELD_LOOKUP + " TEXT , " +FIELD_PHOTOURL + " TEXT , " +FIELD_GCM_ID + " TEXT, " 
			+FIELD_BLOCK_USER + " INTEGER, " +FIELD_FLAG + " TEXT, " +FIELD_DEVICE_IDENTIFICATION + " INTEGER, " +FIELD_USER_STATUS + " TEXT);");

			db.execSQL("CREATE TABLE " + TABLE_BLOCK + "( " + BID + " INTEGER PRIMARY KEY , " 
					+ BLOCK_USER_ID + " TEXT, " +BLOCK_USER + " TEXT, " +BLOCK_OWNER + " TEXT, " 
							+BLOCK_STATUS + " TEXT);");
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
			db.execSQL("DROP TABLE IF EXISTS " + TABLE_BLOCK);
			onCreate(db);
		}

	}

}


