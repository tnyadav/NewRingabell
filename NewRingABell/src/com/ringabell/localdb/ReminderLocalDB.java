package com.ringabell.localdb;

import java.util.ArrayList;

import com.ringabell.model.Reminder;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;



public class ReminderLocalDB {
	private Context context;
	private ReminderDBHelper sdbHelper;
	private SQLiteDatabase sdb;
	//---------------
	private static final String DATABASE_NAME = "Reminder.db";
	private static final int DATABASE_VERSION = 1;
	private static final String TABLE_NAME = "Reminder";

	private static final String FIELD_ID = "id";
	private static final String FIELD_SENDER = "senderno";
	private static final String FIELD_RECEIVER = "receiverno";
	private static final String FIELD_GROUPID = "GroupId";
	private static final String FIELD_REM_TITLE = "reminder_title";
	private static final String FIELD_REM_ID = "rem_id";
	private static final String FIELD_REM_DATE = "rem_date";
	private static final String FIELD_TIME = "rem_time";
	private static final String FIELD_TIME_STRING = "rem_time_string";
	private static final String FIELD_DELAY = "delay";
	private static final String FIELD_RECUR = "recuring";
	private static final String FIELD_REMSTATUS = "reminder_status";
	private static final String FIELD_LOCATION = "location";
	private static final String FIELD_REQCODE = "requestCode";
	private static final String FIELD_TIME_RECEIVING = "receive_time";
	private static final String FIELD_FILE = "voicefile";
	private static final String FIELD_REM_TYPE = "reminder_type";
	private static final String FIELD_REM_DELIVERED = "reminder_delivered";
	private static final String FIELD_REM_DELETE = "reminder_deleted";
	private static final String FIELD_REM_RESPONSE = "reminder_response";


	
	private static final String TABLE_RECENTCONTACTS = "RecentContacts";
	private static final String RC_ID = "cont_id";
	private static final String RC_NO = "ContactNo";
	private static final String RC_REMDATE = "RemDate";
	private static final String RC_TIME = "receivingTime";



	public ReminderLocalDB(Context c) {
		context = c;
		sdbHelper = new ReminderDBHelper(context);
	}

	private ReminderLocalDB open() {
		sdb = sdbHelper.getWritableDatabase();
		return this;
	}

	private void close() {
		sdb.close();		
	}
	
	public boolean checkIfReminderExists(String remId) {
		open();
		Cursor cursor = sdb.rawQuery("SELECT * FROM " + TABLE_NAME + " WHERE "
				+ FIELD_REM_ID + "= '" + remId  +"'", null);
		if (cursor.moveToNext()) {
			System.out.println("Reminder Exist");
			close();
			return true;
		}
		System.out.println("Reminder Not Exist");
		close();
		return false;
	}

	public int getAllEvents(long startDate,long endDate){
		open();

		int totalEvents=0;

		Cursor cursor = sdb.rawQuery("SELECT * FROM " + TABLE_NAME+" where "+FIELD_REM_DATE+" BETWEEN "+startDate+ " AND "+endDate , null);
		cursor.moveToFirst();
		totalEvents=cursor.getCount();
		System.out.println("get total events "+ totalEvents);
		close();
		return totalEvents;

	}
	public int getAllEventsByContact(){
		open();

		int totalEvents=0;

		Cursor cursor = sdb.rawQuery("SELECT * FROM " + TABLE_NAME, null);
		cursor.moveToFirst();
		totalEvents=cursor.getCount();
		System.out.println("get total events "+ totalEvents);
		close();
		return totalEvents;

	}
	
	
	public int countRemindersByDate(long startDate,long endDate){
		open();
		int totalEvents=0;
		Cursor cursor = sdb.rawQuery("select * from Reminder where "+FIELD_REM_DATE+" BETWEEN "+startDate+ " AND "+endDate + " ORDER BY id DESC" , null);
		cursor.moveToFirst();
		totalEvents=cursor.getCount();
		System.out.println("get total events "+ totalEvents);
		close();
		return totalEvents;
	}

	
	public int getAllUnreadReminders(){
		open();
		int totalReminders=0;
		Cursor cursor = sdb.rawQuery("SELECT * FROM " + TABLE_NAME+" where "+FIELD_REMSTATUS+" = 0" , null);
		cursor.moveToFirst();
		totalReminders=cursor.getCount();
		System.out.println("get total reminders "+ totalReminders);
		close();
		return totalReminders;
	}
	
	public int getAllResponseMsgs(){
		open();
		int totalResponseMsgs=0;
		Cursor cursor = sdb.rawQuery("SELECT * FROM " + TABLE_NAME+" where "+FIELD_REM_RESPONSE+" != ''" , null);
		cursor.moveToFirst();
		totalResponseMsgs=cursor.getCount();
		close();
		return totalResponseMsgs;
	}
	
	public String getReminderSender(String remid){
		open();
		String senderno="";
		Cursor cursor = sdb.rawQuery("SELECT "+FIELD_SENDER+" FROM " + TABLE_NAME + " WHERE "
				+ FIELD_REM_ID + " = '" + remid  +"'", null);
		while (cursor.moveToNext()) {
			senderno=cursor.getString(cursor.getColumnIndex(FIELD_SENDER));	
		}
		close();
		return senderno;		
	}

	public ArrayList<Reminder> getReminderInfo(String rem_id){
		open();
		ArrayList <Reminder> recordList=new ArrayList<Reminder>();
		Cursor cursor = sdb.rawQuery("SELECT * FROM " + TABLE_NAME + " WHERE "
				+ FIELD_REM_ID + " = '" + rem_id  +"'" , null);
		while (cursor.moveToNext()) {
			recordList.add(new Reminder(cursor.getString(cursor.getColumnIndex(FIELD_SENDER)),
					cursor.getString(cursor.getColumnIndex(FIELD_RECEIVER)),
					cursor.getString(cursor.getColumnIndex(FIELD_GROUPID)),
					cursor.getString(cursor.getColumnIndex(FIELD_REM_TITLE)),
					cursor.getString(cursor.getColumnIndex(FIELD_REM_ID)),
					cursor.getLong(cursor.getColumnIndex(FIELD_REM_DATE)),
					cursor.getLong(cursor.getColumnIndex(FIELD_TIME)),
					cursor.getString(cursor.getColumnIndex(FIELD_TIME_STRING)),
					cursor.getLong(cursor.getColumnIndex(FIELD_DELAY)),
					cursor.getLong(cursor.getColumnIndex(FIELD_RECUR)),
					cursor.getInt(cursor.getColumnIndex(FIELD_REMSTATUS)),
					cursor.getString(cursor.getColumnIndex(FIELD_LOCATION)),
					cursor.getInt(cursor.getColumnIndex(FIELD_REQCODE)),
					cursor.getString(cursor.getColumnIndex(FIELD_TIME_RECEIVING)),
					cursor.getString(cursor.getColumnIndex(FIELD_FILE)),
					cursor.getString(cursor.getColumnIndex(FIELD_REM_TYPE)),
					cursor.getString(cursor.getColumnIndex(FIELD_REM_DELIVERED))
					));		

		}
		close();
		System.out.println("Events List="+recordList);
		return recordList;
	}
	
	
	public ArrayList<Reminder> getNewReminderInfo(){
		open();
		ArrayList <Reminder> nrecordList=new ArrayList<Reminder>();
		Cursor cursor = sdb.rawQuery("SELECT * FROM " + TABLE_NAME + " WHERE "
				+ FIELD_REMSTATUS + " = 0 " , null);
		while (cursor.moveToNext()) {
			nrecordList.add(new Reminder(cursor.getString(cursor.getColumnIndex(FIELD_SENDER)),
					cursor.getString(cursor.getColumnIndex(FIELD_RECEIVER)),
					cursor.getString(cursor.getColumnIndex(FIELD_GROUPID)),
					cursor.getString(cursor.getColumnIndex(FIELD_REM_TITLE)),
					cursor.getString(cursor.getColumnIndex(FIELD_REM_ID)),
					cursor.getLong(cursor.getColumnIndex(FIELD_REM_DATE)),
					cursor.getLong(cursor.getColumnIndex(FIELD_TIME)),
					cursor.getString(cursor.getColumnIndex(FIELD_TIME_STRING)),
					cursor.getLong(cursor.getColumnIndex(FIELD_DELAY)),
					cursor.getLong(cursor.getColumnIndex(FIELD_RECUR)),
					cursor.getInt(cursor.getColumnIndex(FIELD_REMSTATUS)),
					cursor.getString(cursor.getColumnIndex(FIELD_LOCATION)),
					cursor.getInt(cursor.getColumnIndex(FIELD_REQCODE)),
					cursor.getString(cursor.getColumnIndex(FIELD_TIME_RECEIVING)),
					cursor.getString(cursor.getColumnIndex(FIELD_FILE)),
					cursor.getString(cursor.getColumnIndex(FIELD_REM_TYPE)),
					cursor.getString(cursor.getColumnIndex(FIELD_REM_DELIVERED)),
					cursor.getString(cursor.getColumnIndex(FIELD_REM_RESPONSE))
					));		

		}
		close();
		System.out.println("Events List="+nrecordList);
		return nrecordList;
	}	
	
	public ArrayList<Reminder> getReminderByDate(long startDate,long endDate){
		open();
		ArrayList <Reminder> recordList=new ArrayList<Reminder>();
		//Cursor cursor = sdb.rawQuery("SELECT senderno, COUNT(*) cnt FROM (SELECT senderno FROM " + TABLE_NAME+ " UNION ALL SELECT receiverno FROM Reminder) where "+
				//FIELD_REM_DATE+" BETWEEN "+startDate+ " AND "+endDate +" AND ("+ FIELD_REMSTATUS + " = 1" + " OR "+ FIELD_REMSTATUS + " = 2) GROUP BY senderno ORDER BY cnt DESC", null);
		
		Cursor cursor = sdb.rawQuery("select * from RecentContacts where "+RC_REMDATE+" BETWEEN "+startDate+ " AND "+endDate+ " GROUP BY ContactNo ORDER BY cont_id DESC" , null);

		
		//Cursor cursor = sdb.rawQuery("SELECT * FROM " + TABLE_NAME+" where "+FIELD_REM_DATE+" BETWEEN "+startDate+ " AND "+endDate +" AND ("+ FIELD_REMSTATUS + " = 1" + " OR "+ FIELD_REMSTATUS + " = 2)", null);
		while (cursor.moveToNext()) {
			recordList.add(new Reminder(
					cursor.getString(cursor.getColumnIndex(RC_NO)),
					cursor.getLong(cursor.getColumnIndex(RC_REMDATE)),					
					cursor.getString(cursor.getColumnIndex(RC_TIME))
					));	
		}
		close();
		System.out.println("Events List="+recordList);
		return recordList;		
	}
	
	public ArrayList<Reminder> getReminderByContact(String usernumber){
		open();
ArrayList <Reminder> recordList=new ArrayList<Reminder>();
		Cursor cursor = sdb.rawQuery("select * from Reminder where "+ FIELD_SENDER + " ='"+usernumber +
				"' OR "+ FIELD_RECEIVER + " = '"+usernumber + "' OR " + FIELD_GROUPID + " = '" + usernumber + "' ORDER BY " + FIELD_REM_DATE  + " DESC ", null);

//Cursor cursor = sdb.rawQuery("select * from "+TABLE_NAME+" where "+FIELD_SENDER+" ='"+usernumber+"' or "+FIELD_RECEIVER+" ='"+usernumber+"' or "+FIELD_GROUPID+" ='"+usernumber+"' order by "+FIELD_REM_DATE+" DESC", null);


/*sdb.query(false, TABLE_NAME, new String[]{FIELD_SENDER,
		FIELD_RECEIVER,
		FIELD_GROUPID,
		FIELD_REM_TITLE,
		FIELD_REM_ID,
		FIELD_REM_DATE,
		FIELD_TIME,
		FIELD_TIME_STRING,
		FIELD_DELAY,
		FIELD_RECUR,
		FIELD_REMSTATUS,
		FIELD_LOCATION,
		FIELD_REQCODE,
		FIELD_TIME_RECEIVING,
		FIELD_FILE,
		FIELD_REM_TYPE,
		FIELD_REM_DELIVERED,
		FIELD_REM_RESPONSE
		},
		, selectionArgs, groupBy, having, orderBy, limit, cancellationSignal);*/
		//Cursor cursor = sdb.rawQuery("select * from Reminder where senderno ='"+usernumber+"' or receiverno ='"+usernumber+"' or GroupId ='"+usernumber+"' order by rem_date DESC", null);
		
		
		while (cursor.moveToNext()) {
			recordList.add(new Reminder(
					cursor.getString(cursor.getColumnIndex(FIELD_SENDER)),
					cursor.getString(cursor.getColumnIndex(FIELD_RECEIVER)),
					cursor.getString(cursor.getColumnIndex(FIELD_GROUPID)),
					cursor.getString(cursor.getColumnIndex(FIELD_REM_TITLE)),
					cursor.getString(cursor.getColumnIndex(FIELD_REM_ID)),
					cursor.getLong(cursor.getColumnIndex(FIELD_REM_DATE)),
					cursor.getLong(cursor.getColumnIndex(FIELD_TIME)),
					cursor.getString(cursor.getColumnIndex(FIELD_TIME_STRING)),
					cursor.getLong(cursor.getColumnIndex(FIELD_DELAY)),
					cursor.getLong(cursor.getColumnIndex(FIELD_RECUR)),
					cursor.getInt(cursor.getColumnIndex(FIELD_REMSTATUS)),
					cursor.getString(cursor.getColumnIndex(FIELD_LOCATION)),
					cursor.getInt(cursor.getColumnIndex(FIELD_REQCODE)),
					cursor.getString(cursor.getColumnIndex(FIELD_TIME_RECEIVING)),
					cursor.getString(cursor.getColumnIndex(FIELD_FILE)),
					cursor.getString(cursor.getColumnIndex(FIELD_REM_TYPE)),
					cursor.getString(cursor.getColumnIndex(FIELD_REM_DELIVERED)),
					cursor.getString(cursor.getColumnIndex(FIELD_REM_RESPONSE))));	
		}
		close();
		System.out.println("Eventss List="+recordList);
		return recordList;		
	}
	
	
	public ArrayList<Reminder> getReminderByTime(String timeString){
		open();
		ArrayList <Reminder> recordList=new ArrayList<Reminder>();
		Cursor cursor = sdb.rawQuery("SELECT * FROM " + TABLE_NAME +" where "+ FIELD_TIME_STRING + " = '" + timeString + "'", null);
		while (cursor.moveToNext()) {
			recordList.add(new Reminder(
					cursor.getString(cursor.getColumnIndex(FIELD_SENDER)),
					cursor.getString(cursor.getColumnIndex(FIELD_RECEIVER)),
					cursor.getString(cursor.getColumnIndex(FIELD_GROUPID)),
					cursor.getString(cursor.getColumnIndex(FIELD_REM_TITLE)),
					cursor.getString(cursor.getColumnIndex(FIELD_REM_ID)),
					cursor.getLong(cursor.getColumnIndex(FIELD_REM_DATE)),
					cursor.getLong(cursor.getColumnIndex(FIELD_TIME)),
					cursor.getString(cursor.getColumnIndex(FIELD_TIME_STRING)),
					cursor.getLong(cursor.getColumnIndex(FIELD_DELAY)),
					cursor.getLong(cursor.getColumnIndex(FIELD_RECUR)),
					cursor.getInt(cursor.getColumnIndex(FIELD_REMSTATUS)),
					cursor.getString(cursor.getColumnIndex(FIELD_LOCATION)),
					cursor.getInt(cursor.getColumnIndex(FIELD_REQCODE)),
					cursor.getString(cursor.getColumnIndex(FIELD_TIME_RECEIVING)),
					cursor.getString(cursor.getColumnIndex(FIELD_FILE)),
					cursor.getString(cursor.getColumnIndex(FIELD_REM_TYPE)),
					cursor.getString(cursor.getColumnIndex(FIELD_REM_DELIVERED))));	
		}
		close();
		return recordList;			
	}
	
	
	public ArrayList<Reminder> getAllRemindersByDate(long startDate,long endDate){
		open();
		ArrayList <Reminder> recordList=new ArrayList<Reminder>();
		Cursor cursor = sdb.rawQuery("select * from Reminder where "+FIELD_REM_DATE+" BETWEEN "+startDate+ " AND "+endDate + " ORDER BY id DESC" , null);
		while (cursor.moveToNext()) {
			recordList.add(new Reminder(
					cursor.getString(cursor.getColumnIndex(FIELD_SENDER)),
					cursor.getString(cursor.getColumnIndex(FIELD_RECEIVER)),
					cursor.getString(cursor.getColumnIndex(FIELD_GROUPID)),
					cursor.getString(cursor.getColumnIndex(FIELD_REM_TITLE)),
					cursor.getString(cursor.getColumnIndex(FIELD_REM_ID)),
					cursor.getLong(cursor.getColumnIndex(FIELD_REM_DATE)),
					cursor.getLong(cursor.getColumnIndex(FIELD_TIME)),
					cursor.getString(cursor.getColumnIndex(FIELD_TIME_STRING)),
					cursor.getLong(cursor.getColumnIndex(FIELD_DELAY)),
					cursor.getLong(cursor.getColumnIndex(FIELD_RECUR)),
					cursor.getInt(cursor.getColumnIndex(FIELD_REMSTATUS)),
					cursor.getString(cursor.getColumnIndex(FIELD_LOCATION)),
					cursor.getInt(cursor.getColumnIndex(FIELD_REQCODE)),
					cursor.getString(cursor.getColumnIndex(FIELD_TIME_RECEIVING)),
					cursor.getString(cursor.getColumnIndex(FIELD_FILE)),
					cursor.getString(cursor.getColumnIndex(FIELD_REM_TYPE)),
					cursor.getString(cursor.getColumnIndex(FIELD_REM_DELIVERED)),
					cursor.getString(cursor.getColumnIndex(FIELD_REM_RESPONSE))));	
		}
		close();
		System.out.println("Events List="+recordList);
		return recordList;
		
	}
	
	public void insertReminderLocal(String sender, String receiver, String groupid, String title, String rem_id, long remdate, long remtime, String remTimeString, long delay,
			long recur, int remstatus, String location, int reqcode, String rtime_receiving, String vfile, String remType, String remDelivered, String remDeleted) {
		open();

		ContentValues values = new ContentValues();

		//values.put(FIELD_ID, id);
		//values.put(FIELD_DATE, date);
		values.put(FIELD_SENDER, sender);
		values.put(FIELD_RECEIVER, receiver);
		values.put(FIELD_GROUPID, groupid);
		values.put(FIELD_REM_TITLE, title);
		values.put(FIELD_REM_ID, rem_id);
		values.put(FIELD_REM_DATE, remdate);
		values.put(FIELD_TIME, remtime);
		values.put(FIELD_TIME_STRING, remTimeString);
		values.put(FIELD_DELAY, delay);
		values.put(FIELD_RECUR, recur);
		values.put(FIELD_REMSTATUS, remstatus);
		values.put(FIELD_LOCATION, location);
		values.put(FIELD_REQCODE, reqcode);
		values.put(FIELD_TIME_RECEIVING, rtime_receiving);
		values.put(FIELD_FILE, vfile);
		values.put(FIELD_REM_TYPE, remType);
		values.put(FIELD_REM_DELIVERED, remDelivered);
		values.put(FIELD_REM_DELETE, remDeleted);
		values.put(FIELD_REM_RESPONSE, "");


		try {
			sdb.insertOrThrow(TABLE_NAME, null, values);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		close();
	}
	
	public void insertRecentContacts(String contactNo, long remDate, String receiveTime) {
		open();

		ContentValues values = new ContentValues();
		values.put(RC_NO, contactNo);
		values.put(RC_REMDATE, remDate);
		values.put(RC_TIME, receiveTime);

		try {
			sdb.insertOrThrow(TABLE_RECENTCONTACTS, null, values);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		close();
	}
	
	public int RemindersCount(String timeString) {
		open();
		int reminderCount=0;
		Cursor cursor = sdb.rawQuery("SELECT * FROM " + TABLE_NAME + " WHERE "
				+ FIELD_TIME_STRING + "= '" + timeString  +"'", null);
		reminderCount=cursor.getCount();
		close();
		return reminderCount;
	}
	
	public String getReceiver(String remid){
		open();
		String receiverno="";
		Cursor cursor = sdb.rawQuery("SELECT * FROM " + TABLE_NAME + " WHERE "
				+ FIELD_REM_ID + " = '" + remid  +"'", null);
		while (cursor.moveToNext()) {
			receiverno=cursor.getString(cursor.getColumnIndex(FIELD_RECEIVER));	
		}
		close();
		return receiverno;		
	}
	
	public int getReminderReqCode(String timeString){
		open();
		int reqcode=0;
		Cursor cursor = sdb.rawQuery("SELECT * FROM " + TABLE_NAME + " WHERE "
				+ FIELD_TIME_STRING + " = '" + timeString  +"' ORDER BY id LIMIT 1 ", null);
		while (cursor.moveToNext()) {
			reqcode=cursor.getInt(cursor.getColumnIndex(FIELD_REQCODE));	
		}
		close();
		return reqcode;
		
	}

	public void updateReminderSataus(String remid){
		open();
		ContentValues values=new ContentValues();
		values.put(FIELD_REMSTATUS, 1);
		sdb.update(TABLE_NAME, values, FIELD_REM_ID+" = ?", new String[]{remid});
		close();
	}
	
	public void updateReminderDeliverStatus(String remid){
		open();
		ContentValues values=new ContentValues();
		values.put(FIELD_REM_DELIVERED, "YES");
		sdb.update(TABLE_NAME, values, FIELD_REM_ID+" = ?", new String[]{remid});
		close();
	}
	
	public void updateReminderDeliverStatus2(String remid){
		open();
		ContentValues values=new ContentValues();
		values.put(FIELD_REM_DELIVERED, "DECLINE");
		sdb.update(TABLE_NAME, values, FIELD_REM_ID+" = ?", new String[]{remid});
		close();
	}
	
	public void updateReminderResponse(String remid, String response_msg){
		open();
		ContentValues values=new ContentValues();
		values.put(FIELD_REM_RESPONSE, response_msg);
		sdb.update(TABLE_NAME, values, FIELD_REM_ID+" = ?", new String[]{remid});
		System.out.println("Events List="+ response_msg);

		close();
	}
	
	public void updateReqCode(String remid, int reqcode){
		open();
		ContentValues values=new ContentValues();
		values.put(FIELD_REQCODE, reqcode);
		sdb.update(TABLE_NAME, values, FIELD_REM_ID+" = ?", new String[]{remid});
		close();
	}
	
	public void updateTimeString(String remid){
		open();
		ContentValues values=new ContentValues();
		values.put(FIELD_TIME_STRING, "NONE");
		sdb.update(TABLE_NAME, values, FIELD_REM_ID+" = ?", new String[]{remid});
		close();
	}

	public void deleteReminderonDecline(String rem_id){
		open();
		sdb.delete(TABLE_NAME, FIELD_REM_ID+" = ?", new String[]{rem_id});
		close();		
	}
	
	public void deleteReminderonResponse(String rem_id){
		open();
		sdb.delete(TABLE_NAME, FIELD_REM_ID+" = ?", new String[]{rem_id});
		close();		
	}
	
	public void deleteReminder(String deleted){
		open();
		sdb.delete(TABLE_NAME, FIELD_REM_DELETE+" = ?", new String[]{deleted});
		close();		
	}	
	
	public void deleteRecentContact(String contactNo){
		open();
		sdb.delete(TABLE_RECENTCONTACTS, RC_NO+" = ?", new String[]{contactNo});
		close();		
	}	
	
	public void updateRemDelete(String remid){
		open();
		ContentValues values=new ContentValues();
		values.put(FIELD_REM_DELETE, 1);
		sdb.update(TABLE_NAME, values, FIELD_REM_ID+" = ?", new String[]{remid});
		close();
	}
	
	public void updateRemDeleteAgain(String remid){
		open();
		ContentValues values=new ContentValues();
		values.put(FIELD_REM_DELETE, 0);
		sdb.update(TABLE_NAME, values, FIELD_REM_ID+" = ?", new String[]{remid});
		close();
	}

	
	public void clear(){
		open();
		sdb.delete(TABLE_NAME, null, null);
		close();
	}
	
	private class ReminderDBHelper extends SQLiteOpenHelper {
		public ReminderDBHelper(Context c) {
			super(c, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			System.out.println("Database tables created");
			db.execSQL("CREATE TABLE " + TABLE_NAME + "( " + FIELD_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
					FIELD_SENDER + " TEXT , " +	FIELD_RECEIVER + " TEXT , " + FIELD_GROUPID + " TEXT, " +
					FIELD_REM_TITLE + " TEXT, " +FIELD_REM_ID + " TEXT , " + FIELD_REM_DATE + " INTEGER , " +
					FIELD_TIME + " INTEGER , " + FIELD_TIME_STRING + " TEXT, " +FIELD_DELAY + " INTEGER , " +FIELD_RECUR + " INTEGER, " +FIELD_REMSTATUS +
					" INTEGER, " +FIELD_LOCATION +	" TEXT, " +FIELD_REQCODE + " INTEGER, " + FIELD_TIME_RECEIVING +
					" TEXT, " +FIELD_FILE + " TEXT, " + FIELD_REM_TYPE + " TEXT, " + FIELD_REM_DELIVERED + " TEXT, " +
							 FIELD_REM_DELETE + " TEXT, " + FIELD_REM_RESPONSE + " TEXT);");
			
			db.execSQL("CREATE TABLE " + TABLE_RECENTCONTACTS + "( " + RC_ID + " INTEGER PRIMARY KEY AUTOINCREMENT , " 
					+ RC_NO + " TEXT, " +RC_REMDATE + " INTEGER, " + RC_TIME + " TEXT);");
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
			db.execSQL("DROP TABLE IF EXISTS " + TABLE_RECENTCONTACTS);
			onCreate(db);			
		}

	}

}


