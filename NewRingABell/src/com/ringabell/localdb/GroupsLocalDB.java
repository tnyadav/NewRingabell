package com.ringabell.localdb;



import java.util.ArrayList;

import com.ringabell.model.Group;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class GroupsLocalDB {
	
	private Context context;
	private GroupsDBHelper sdbHelper;
	private SQLiteDatabase sdb;
	
	private static final String DATABASE_NAME = "Group.db";
	private static final int DATABASE_VERSION = 1;
	private static final String TABLE_GROUP = "Groups";
	private static final String IDD = "idd";
	private static final String GROUP_USER = "group_user";
	private static final String GROUP_ID = "group_id";
	private static final String NAME = "name";
	private static final String GROUP_OWNER = "owner";
	private static final String GROUP_PIC = "group_pic";
	private static final String GROUP_STATUS = "group_status";
	
	private static final String TABLE_GROUPMEMBER = "GroupMembers";
	private static final String ID = "id";
	private static final String GROUP_MEMBER = "group_member";
	private static final String GROUPID = "groupid";
	public GroupsLocalDB(Context c) {
		context = c;
		sdbHelper = new GroupsDBHelper(context);
	}

	private GroupsLocalDB open() {
		sdb = sdbHelper.getWritableDatabase();
		return this;
	}

	private void close() {
		sdb.close();
	}
	
	public int countGroups(){
		open();
		int totalValues=0;
		String selectQuery = "SELECT  * FROM " + TABLE_GROUP ;
		Cursor cursor = sdb.rawQuery(selectQuery, null);
		totalValues=cursor.getCount();
		close();
		return totalValues;
	}
	
	public int countMyGroups(String group_owner){
		open();
		int totalValues=0;
		String selectQuery = "SELECT * FROM " + TABLE_GROUP + " WHERE "+ GROUP_OWNER + " = '" + group_owner +"'";
 		Cursor cursor = sdb.rawQuery(selectQuery, null);
		totalValues=cursor.getCount();
		close();
		return totalValues;
	}
	
	public ArrayList<Group> getAllGroupList(){
		open();
		ArrayList <Group> groupList=new ArrayList<Group>();
		Cursor cursor = sdb.rawQuery("SELECT * FROM " + TABLE_GROUP , null);
		while (cursor.moveToNext()) {
			groupList.add(new Group(cursor.getInt(cursor.getColumnIndex(IDD))
					,cursor.getString(cursor.getColumnIndex(GROUP_USER))
					,cursor.getString(cursor.getColumnIndex(GROUP_ID))
					,cursor.getString(cursor.getColumnIndex(NAME))
					,cursor.getString(cursor.getColumnIndex(GROUP_OWNER))
					,cursor.getString(cursor.getColumnIndex(GROUP_STATUS))));
		}
		close();
		System.out.println("My Group  List="+groupList);
		return groupList;
	}
	
	public ArrayList<Group> getGroupById(String groupid){
		open();
		ArrayList <Group> groupList=new ArrayList<Group>();
		Cursor cursor = sdb.rawQuery("SELECT * FROM " + TABLE_GROUP + " WHERE "+ GROUP_ID + " = '" + groupid +"'" , null);
		while (cursor.moveToNext()) {
			groupList.add(new Group(cursor.getInt(cursor.getColumnIndex(IDD))
					,cursor.getString(cursor.getColumnIndex(GROUP_USER))
					,cursor.getString(cursor.getColumnIndex(GROUP_ID))
					,cursor.getString(cursor.getColumnIndex(NAME))
					,cursor.getString(cursor.getColumnIndex(GROUP_OWNER))
					,cursor.getString(cursor.getColumnIndex(GROUP_STATUS))));
		}
		close();
		System.out.println("User List="+groupList);
		return groupList;
	}
	
	public ArrayList<Group> getMyGroups(String group_owner){
		open();
		ArrayList <Group> mygroupList=new ArrayList<Group>();
		Cursor cursor = sdb.rawQuery("SELECT * FROM " + TABLE_GROUP + " WHERE "+ GROUP_OWNER + " = '" + group_owner +"'" , null);
		while (cursor.moveToNext()) {
			mygroupList.add(new Group(cursor.getInt(cursor.getColumnIndex(IDD))
					,cursor.getString(cursor.getColumnIndex(GROUP_USER))
					,cursor.getString(cursor.getColumnIndex(GROUP_ID))
					,cursor.getString(cursor.getColumnIndex(NAME))
					,cursor.getString(cursor.getColumnIndex(GROUP_OWNER))
					,cursor.getString(cursor.getColumnIndex(GROUP_STATUS))));
		}
		close();
		return mygroupList;
	}
	
	public ArrayList<Group> getGroupMembers(String group_id){
		open();
		ArrayList <Group> memberList=new ArrayList<Group>();
		Cursor cursor = sdb.rawQuery("SELECT * FROM " + TABLE_GROUPMEMBER + " WHERE "+ GROUPID + " = '" + group_id +"'" , null);
		while (cursor.moveToNext()) {
			memberList.add(new Group(cursor.getString((cursor.getColumnIndex(GROUP_MEMBER))),
					cursor.getString(cursor.getColumnIndex(GROUPID))));
		}
		close();
		//System.out.println("User List="+memberList);
		return memberList;
	}
	
	public void insertGroupLocal(String guser, String gid,String gname,String gowner, String group_pic, String group_status) {
		open();
		
		ContentValues values = new ContentValues();
		
		values.put(GROUP_USER, guser);
		values.put(GROUP_ID, gid);
		values.put(NAME, gname);
		values.put(GROUP_OWNER, gowner);
		values.put(GROUP_PIC, group_pic);
		values.put(GROUP_STATUS, group_status);
		try {
			sdb.insertOrThrow(TABLE_GROUP, null, values);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		close();
	}
	
	public void updateGroupLocal(String guser, String gid,String gname,String gowner, String group_pic, String group_status) {
		open();
		ContentValues values=new ContentValues();
		values.put(GROUP_USER, guser);
		values.put(NAME, gname);
		values.put(GROUP_OWNER, gowner);
		values.put(GROUP_PIC, group_pic);
		values.put(GROUP_STATUS, group_status);		
		sdb.update(TABLE_GROUP, values, GROUP_ID+" = ?", new String[]{gid});
		System.out.println(group_status);
		System.out.println(gname);
		close();
	}
	
	public boolean checkIfGrouptExists(String groupId) {
		open();
		Cursor cursor = sdb.rawQuery("SELECT * FROM " + TABLE_GROUP + " WHERE "
				+ GROUP_ID + "= '" + groupId  +"'", null);
		if (cursor.moveToNext()) {
			System.out.println("Group Exists");
			close();
			return true;
		}
		System.out.println("Group Not Exist");
		close();
		return false;
	}
	
	
	public void insertGroupMembersLocal(String gmember, String grp_id) {
		open();
		
		ContentValues values = new ContentValues();
		
		values.put(GROUP_MEMBER, gmember);
		values.put(GROUPID, grp_id);
		try {
			sdb.insertOrThrow(TABLE_GROUPMEMBER, null, values);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		close();
	}	
	
	public String getGroupPicUrl(String groupId){
		open();
		String gpicUrl="";
		Cursor cursor = sdb.rawQuery("SELECT "+GROUP_PIC +" FROM " + TABLE_GROUP + " WHERE "
				+ GROUP_ID + "= '" +groupId  +"'", null);
		if (cursor.moveToNext()) {
			gpicUrl=cursor.getString(cursor.getColumnIndex(GROUP_PIC));
			System.out.println("Pic Url="+gpicUrl);
			close();
			return gpicUrl;
		}
		close();
		System.out.println("Pic Url="+gpicUrl);
		return gpicUrl;
		
	}
	
	public void deleteGroup(String gid){
		open();
		sdb.delete(TABLE_GROUP, GROUP_ID+" = ?", new String[]{gid});
		close();		
	}
	
	public void deleteMembers(String gid){
		open();
		sdb.delete(TABLE_GROUPMEMBER, GROUPID+" = ?", new String[]{gid});
		close();		
	}
	
	
	private class GroupsDBHelper extends SQLiteOpenHelper {
		
		public GroupsDBHelper(Context c) {
			super(c, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			System.out.println("Database tables created");
			

			db.execSQL("CREATE TABLE " + TABLE_GROUP + "( " + IDD + " INTEGER PRIMARY KEY , " 
					+ GROUP_USER + " TEXT, " +GROUP_ID + " TEXT, " +NAME + " TEXT, " 
							+GROUP_OWNER + " TEXT, " +GROUP_PIC+ " TEXT, "+GROUP_STATUS + " TEXT);");
			
			db.execSQL("CREATE TABLE " + TABLE_GROUPMEMBER + "( " + ID + " INTEGER PRIMARY KEY , " 
					+ GROUP_MEMBER + " TEXT, " +GROUPID + " TEXT);");
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			
			db.execSQL("DROP TABLE IF EXISTS " + TABLE_GROUP);
			db.execSQL("DROP TABLE IF EXISTS " + TABLE_GROUPMEMBER);
			onCreate(db);
		}

	}

}
