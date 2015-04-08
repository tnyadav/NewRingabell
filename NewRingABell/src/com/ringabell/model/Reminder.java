package com.ringabell.model;

public class Reminder {

	private int id;	
	private String sender;
	private String receiver;
	private String groupid;	
	private String title;
	private String rem_id;
	private long remdate;
	private long remtime;
	private String remtimeString;
	private long delay;
	private long recur;
	private int rem_status;
	private String location;
	private int reqcode;
	private String time_receiving;
	private String voice_file;
	private String rem_type;
	private String rem_delivered;
	private String contactNo;
	private String rem_response;


	public Reminder(){

	}


	public Reminder(String sender, String receiver, String groupid, String title, String rem_id, long remdate, long remtime, String remtimeString, long delay,
			long recur, int rem_status, String location, int reqcode, String time_receiving, String voice_file, String rem_type, String rem_delivered) {
		// TODO Auto-generated constructor stub
		this.sender=sender;
		this.receiver=receiver;
		this.groupid=groupid;
		this.title=title;
		this.rem_id=rem_id;
		this.remdate=remdate;
		this.remtime=remtime;
		this.remtimeString=remtimeString;
		this.delay=delay;
		this.recur=recur;
		this.rem_status = rem_status;
		this.location = location;
		this.reqcode=reqcode;
		this.time_receiving=time_receiving;
		this.voice_file=voice_file;
		this.rem_type=rem_type;
		this.rem_delivered=rem_delivered;
	}
	
	public Reminder(String sender, String receiver, String groupid, String title, String rem_id, long remdate, long remtime, String remtimeString, long delay,
			long recur, int rem_status, String location, int reqcode, String time_receiving, String voice_file, String rem_type, String rem_delivered, String response_msg) {
		// TODO Auto-generated constructor stub
		this.sender=sender;
		this.receiver=receiver;
		this.groupid=groupid;
		this.title=title;
		this.rem_id=rem_id;
		this.remdate=remdate;
		this.remtime=remtime;
		this.remtimeString=remtimeString;
		this.delay=delay;
		this.recur=recur;
		this.rem_status = rem_status;
		this.location = location;
		this.reqcode=reqcode;
		this.time_receiving=time_receiving;
		this.voice_file=voice_file;
		this.rem_type=rem_type;
		this.rem_delivered=rem_delivered;
		this.rem_response=response_msg;

	}
	
	public Reminder(String sender, String title, String rem_id, long remdate, long remtime) {
		// TODO Auto-generated constructor stub

		this.sender=sender;
		this.title=title;
		this.rem_id=rem_id;
		this.remdate=remdate;
		this.remtime=remtime;

	}
	public Reminder(String sender,String title) {
		// TODO Auto-generated constructor stub
		this.sender=sender;
		this.title=title;
	}

	public Reminder(String contactNo, long remdate, String receivetime) {
		// TODO Auto-generated constructor stub
		this.contactNo=contactNo;
		this.remdate=remdate;
		this.time_receiving=receivetime ;
	}

	public String getSender() {
		return sender;
	}
	public void setSender(String sender) {
		this.sender = sender;
	}

	public String getReceiver() {
		return receiver;
	}
	public void setReceiver(String receiver) {
		this.receiver = receiver;
	}


	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getRemId() {
		return rem_id;
	}


	public void setRemId(String rem_id) {
		this.rem_id = rem_id;
	}

	public long getRemDate() {
		return remdate;
	}


	public void setRemDate(long remdate) {
		this.remdate = remdate;
	}

	public long getRemTime() {
		return remtime;
	}


	public void setRemTime(long remtime) {
		this.remtime = remtime;
	}

	public String getRemTimeString(){
		return remtimeString;
	}

	public void setRemTimeString(String remtimeString){
		this.remtimeString=remtimeString;
	}

	public long getDelay() {
		return delay;
	}


	public void setDelay(long delay) {
		this.delay = delay;
	}

	public long getRecur() {
		return recur;
	}


	public void setRecur(long recur) {
		this.recur = recur;
	}

	public int getRemStatus() {
		return rem_status;
	}


	public void setRemStatus(int rem_status) {
		this.rem_status = rem_status;
	}

	public int getReqCode() {
		return reqcode;
	}


	public void setReqCode(int reqcode) {
		this.reqcode = reqcode;
	}

	public String getReceivingTime() {
		return time_receiving;
	}


	public void setReceivingTime(String time_receiving) {
		this.time_receiving = time_receiving;
	}

	public String getVoiceFile() {
		return voice_file;
	}


	public void setVoiceFile(String voice_file) {
		this.voice_file = voice_file;
	}

	public String getReminderType() {
		return rem_type;
	}


	public void setReminderType(String rem_type) {
		this.rem_type = rem_type;
	}

	public String getRemDeliverStaus() {
		return rem_delivered;
	}


	public void setRemDeliverStaus(String rem_delivered) {
		this.rem_delivered = rem_delivered;
	}

	public String getContactNo() {
		return contactNo;
	}


	public void setContactNo(String contactNo) {
		this.contactNo = contactNo;
	}

	public String getLocationName() {
		return location;
	}


	public void setLocationName(String location) {
		this.location = location;
	}

	public String getGroupId() {
		return groupid;
	}


	public void setGroupId(String groupid) {
		this.groupid = groupid;
	}

	public String getResponseMsg() {
		return rem_response;
	}


	@Override
	public String toString() {
		return "Reminder [id=" + id + ", sender=" + sender + ", receiver="
				+ receiver + ", groupid=" + groupid + ", title=" + title
				+ ", rem_id=" + rem_id + ", remdate=" + remdate + ", remtime="
				+ remtime + ", remtimeString=" + remtimeString + ", delay="
				+ delay + ", recur=" + recur + ", rem_status=" + rem_status
				+ ", location=" + location + ", reqcode=" + reqcode
				+ ", time_receiving=" + time_receiving + ", voice_file="
				+ voice_file + ", rem_type=" + rem_type + ", rem_delivered="
				+ rem_delivered + ", contactNo=" + contactNo
				+ ", rem_response=" + rem_response + "]";
	}


	public void setResponseMsg(String rem_response) {
		this.rem_response = rem_response;
	}
}
