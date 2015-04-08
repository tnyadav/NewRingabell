package com.ringabell.localdb;

import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;



public class TemplateLocalDB {
	private Context context;
	private TemplateDBHelper sdbHelper;
	private SQLiteDatabase sdb;
//---------------
	private static final String DATABASE_NAME = "Template.db";
	private static final int DATABASE_VERSION = 1;
	private static final String TABLE_NAME = "Template";
	
	private static final String FIELD_ID = "id";
	private static final String FIELD_TEMPLATE = "template";
	/*private static final String FIELD_USER_NUMBER = "mobileNumber";*/
	/*private static final String FIELD_STAUS = "status";
	private static final String FIELD_START_DATE = "startDate";
	private static final String FIELD_END_DATE = "endDate";
	private static final String FIELD_DATE = "date";*/
	public TemplateLocalDB(Context c) {
		context = c;
		sdbHelper = new TemplateDBHelper(context);
	}

	private TemplateLocalDB open() {
		sdb = sdbHelper.getWritableDatabase();
		return this;
	}

	private void close() {
		sdb.close();
	}
	
	/*public long checkCalendarTime(String number,long timeStamp,String date){
		open();
		long startTime=0;
		Cursor cursor = sdb.rawQuery("SELECT * FROM " + TABLE_NAME + " WHERE "+ FIELD_USER_NUMBER + " = '" + number  +"'"+" AND "+FIELD_DATE+" = '"+ date +"'"+" AND "
				+FIELD_START_DATE+ " >= '" + timeStamp  +"'"+" ORDER BY "+FIELD_START_DATE +" LIMIT 1", null);
		
		if (cursor.moveToNext()) {
			startTime=cursor.getLong(cursor.getColumnIndex(FIELD_START_DATE));
			System.out.println("start time"+startTime);
			close();
			return startTime;
		}
		System.out.println("start time other"+startTime);
		close();
		return startTime;
	}*/
	/*public boolean checkCalendarEventTime(long timeStamp,String date){
		open();
		Cursor cursor = sdb.rawQuery("SELECT * FROM " + TABLE_NAME + " WHERE "+FIELD_DATE+" = '"+ date +"'"+" AND ("
				+ FIELD_END_DATE+ " >= '" + timeStamp  +"'"+" AND " + FIELD_START_DATE+ " <= '" + timeStamp  +"'"+" )"+" ORDER BY "+FIELD_START_DATE +" LIMIT 1", null);
		
		if (cursor.moveToNext()) {
			System.out.println("Event Counts"+cursor.getCount());
			close();
			return true;
		}
		System.out.println("Event Not Counts"+cursor.getCount());
		close();
		return false;
	}*/
	/*public long retrieveBusyCalendarTime(long timeStamp,String date){
		open();
		long endTime=0;
		Cursor cursor = sdb.rawQuery("SELECT * FROM " + TABLE_NAME + " WHERE "+FIELD_DATE+" = '"+ date +"'"+" AND ("
				+ FIELD_END_DATE+ " >= '" + timeStamp  +"'"+" AND " + FIELD_START_DATE+ " <= '" + timeStamp  +"'"+" )"+" ORDER BY "+FIELD_START_DATE +" LIMIT 1", null);
		
		if (cursor.moveToNext()) {
			endTime=cursor.getLong(cursor.getColumnIndex(FIELD_START_DATE));
			System.out.println("busy time"+endTime);
			close();
			return endTime;
		}
		System.out.println("busy time other"+endTime);
		close();
		return endTime;
	}*/
public int countTemplateSize(){
	open();
	
	int totalEvents=0;
	
	Cursor cursor = sdb.rawQuery("SELECT *FROM " + TABLE_NAME, null);
	cursor.moveToFirst();
	totalEvents=cursor.getCount();
	System.out.println("get total events "+ totalEvents);
	close();
	return totalEvents;

}

public ArrayList<String> getAllTemplateList(){
	open();
	ArrayList <String> templateList=new ArrayList<String>();
	Cursor cursor = sdb.rawQuery("SELECT "+FIELD_TEMPLATE+" FROM " + TABLE_NAME , null);
	while (cursor.moveToNext()) {
		templateList.add(cursor.getString(cursor.getColumnIndex(FIELD_TEMPLATE)));		
	}
	close();
	System.out.println("Template List="+templateList);
	return templateList;
}
/*public ArrayList<CalendarData> getEventById(String id){
	open();
	ArrayList <CalendarData> recordList=new ArrayList<CalendarData>();
	Cursor cursor = sdb.rawQuery("SELECT * FROM " + TABLE_NAME + " WHERE "
			+ FIELD_ID + " = '" + id  +"'", null);
	while (cursor.moveToNext()) {
		recordList.add(new CalendarData(cursor.getString(cursor.getColumnIndex(FIELD_ID)),
				cursor.getString(cursor.getColumnIndex(FIELD_USER_NUMBER)),
				cursor.getLong(cursor.getColumnIndex(FIELD_START_DATE)),
				cursor.getLong(cursor.getColumnIndex(FIELD_END_DATE)),
				cursor.getString(cursor.getColumnIndex(FIELD_STAUS))));		
	}
	close();
	System.out.println("Event List Only="+recordList);
	return recordList;
}*/
/*public void updateCalendarEvent(String id,String status,String sDate,String eDate,String date){
	open();
	ContentValues values=new ContentValues();
	values.put(FIELD_STAUS, status);
	values.put(FIELD_DATE, date);
	values.put(FIELD_START_DATE, Long.parseLong(sDate));
	values.put(FIELD_END_DATE, Long.parseLong(eDate));
	sdb.update(TABLE_NAME, values, FIELD_ID+" = ?", new String[]{id});
	close();
}
*/

	/*public boolean checkIfEventExists(String eventId) {
		open();
		Cursor cursor = sdb.rawQuery("SELECT * FROM " + TABLE_NAME + " WHERE "
				+ FIELD_ID + "= '" + eventId  +"'", null);
		if (cursor.moveToNext()) {
			System.out.println("Event Exist");
			close();
			return true;
		}
		System.out.println("Event Not Exist");
		close();
		return false;
	}*/
	public void insertEventLocal(String text) {
		open();
		
		ContentValues values = new ContentValues();
		
		
		values.put(FIELD_TEMPLATE, text);
	
		try {
			sdb.insertOrThrow(TABLE_NAME, null, values);
			System.out.println("TEMP="+text);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println("TEMP Exception");
		}
		
		close();
	}
	public void deleteEvent(String _id){
		open();
		sdb.delete(TABLE_NAME, FIELD_ID+" = ?", new String[]{_id});
		close();
	} 
	
	public void clear(){
		open();
		sdb.delete(TABLE_NAME, null, null);
		close();
	}
	private class TemplateDBHelper extends SQLiteOpenHelper {
		public TemplateDBHelper(Context c) {
			super(c, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			System.out.println("Database tables created");
			db.execSQL("CREATE TABLE " + TABLE_NAME + "( " + FIELD_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
			FIELD_TEMPLATE + " TEXT );");
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
			onCreate(db);
		}

	}

}
