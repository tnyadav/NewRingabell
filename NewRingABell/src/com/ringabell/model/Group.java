package com.ringabell.model;

import android.R.string;

public class Group {
	private int idd;	
	private String group_user;
	private String group_id;	
	private String gname;
	private String group_owner;
	private String group_member;
	private String group_pic;
	private String group_status;



	public Group(){

	}
	
	public Group(int id, String group_user, String group_id, String gname, String group_owner,String group_status) {
		// TODO Auto-generated constructor stub
		this.idd = id;
		this.group_user=group_user;
		this.group_id=group_id;
		this.gname=gname;
		this.group_owner=group_owner;
		this.group_status=group_status;
	}



	public Group(String group_user, String group_id, String gname, String group_owner, String group_pic, String group_status) {
		// TODO Auto-generated constructor stub
		this.group_user=group_user;
		this.group_id=group_id;
		this.gname=gname;
		this.group_owner=group_owner;
		this.group_pic=group_pic;
		this.group_status=group_status;
	}
	
	public Group(String group_member, String group_id) {
		// TODO Auto-generated constructor stub
		this.group_member =group_member;
		this.group_id=group_id;
	}
	
	public Group(String gname){
		this.gname= gname;
	}



	public String getGroupUser() {
		return group_user;
	}
	public void setGroupUser(String group_user) {
		this.group_user = group_user;
	}

	public String getGroupId() {
		return group_id;
	}
	public void setGroupId(String group_id) {
		this.group_id = group_id;
	}


	public String getName() {
		return gname;
	}
	
	public void setName(String gname) {
		this.gname = gname;		
	}

	
	public String getGroupOwner(){
		return group_owner;
	}
	
	public void setGroupOwner(String group_owner) {
		this.group_owner = group_owner;		
	}
	
	public String getGroupMember(){
		return group_member;		
	}
	public void setGroupMember(String grp_member){
		this.group_member=grp_member;
	}
	
	public String getGroupPic(){
		return group_pic;		
	}
	public void setGroupPic(String group_pic){
		this.group_pic=group_pic;
	}
	
	public String getGroupStatus(){
		return group_status;		
	}
	public void setGroupStatus(String group_status){
		this.group_status=group_status;
	}
	
	
}
