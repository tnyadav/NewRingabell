package com.ringabell.model;

import java.io.Serializable;

public class Contact implements Serializable{
	private String id;
	private String name;
	private String phoneNo;
	private String originalPhoneNumber;
	private String lookUp;
	private String url;
	private String regID;
	private int deviceIdentification;
	private String userstatus;
	
	
	public Contact(String id,String name,String phone,String originalNumber,String photoUrl){
		super();
		this.id=id;
		this.name=name;
		this.phoneNo=phone;
		this.originalPhoneNumber=originalNumber;
		this.url=photoUrl;
	}

	public Contact(String id,String name,String phone,String originalNumber,String lookupkey,String photoUrl, String ustatus){
		super();
		this.id=id;
		this.name=name;
		this.phoneNo=phone;
		this.originalPhoneNumber=originalNumber;
		this.lookUp=lookupkey;
		this.url=photoUrl;
		this.userstatus=ustatus;

	}
	
	public Contact(String name,String phone){
		super();
		this.name=name;
		this.phoneNo=phone;
	}

	public Contact(String phone,String regId,String url,int deviceIdentify,String userstatus){
		super();

		this.url=url;
		this.phoneNo=phone;
		this.regID=regId;
		this.deviceIdentification=deviceIdentify;
		this.userstatus=userstatus;
	}

	public String getUserstatus() {
		return userstatus;
	}

	public void setUserstatus(String userstatus) {
		this.userstatus = userstatus;
	}

	public Contact(String name,String phone,String original){
		super();
		this.originalPhoneNumber=original;
		this.name=name;
		this.phoneNo=phone;
	}


	public String getRegID() {
		return regID;
	}

	public void setRegID(String regID) {
		this.regID = regID;
	}

	public int getDeviceIdentification() {
		return deviceIdentification;
	}

	public void setDeviceIdentification(int deviceIdentification) {
		this.deviceIdentification = deviceIdentification;
	}

	public Contact(String name){
		super();	
		this.name=name;
	}

	public String getLookUp() {
		return lookUp;
	}

	public void setLookUp(String lookUp) {
		this.lookUp = lookUp;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getOriginalPhoneNumber() {
		return originalPhoneNumber;
	}

	public void setOriginalPhoneNumber(String originalPhoneNumber) {
		this.originalPhoneNumber = originalPhoneNumber;
	}

	public String getPhoneNo() {
		return phoneNo;
	}

	public void setPhoneNo(String phoneNo) {
		this.phoneNo = phoneNo;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	@Override
	public String toString() {
		return "Contact [id=" + id + ", name=" + name + ", phoneNo=" + phoneNo
				+ ", originalPhoneNumber=" + originalPhoneNumber + ", lookUp="
				+ lookUp + ", url=" + url + ", regID=" + regID
				+ ", deviceIdentification=" + deviceIdentification
				+ ", userstatus=" + userstatus + "]";
	}

	public void setName(String name) {
		this.name = name;
	}
}
