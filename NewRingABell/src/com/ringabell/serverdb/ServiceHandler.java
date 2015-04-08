package com.ringabell.serverdb;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
 
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
 
import android.util.Log;

 
public class ServiceHandler {
	
	//Live url
	public static final String BASE_URL= "http://ringabell.share2people.com/ringabell/";
	
	// testing url
	//public static final String BASE_URL= "http://ringabell-testing.share2people.com/code/";
	
	public static final String URL_SEND_MESSAGE = BASE_URL +"send_verification_message.php";
	public static final String URL_VERIFY_USER = BASE_URL +"verify_user.php";
	
	public static final String URL_ADD_REMINDER2= BASE_URL + "insertreminder2.php";
	public static final String URL_UPDATE_REMINDER = BASE_URL + "update_reminder.php";
	
	public static final String upLoadServerUri = BASE_URL + "recording_file.php";
	
	public static final String fileUrl = "https://s3-us-west-2.amazonaws.com/ringabell/recording/";

	
	public static final String URL_CREATE_GROUP= BASE_URL + "insert_reminder_group.php";
	
	public static final String URL_UPDATE_GROUP= BASE_URL + "update_group.php";

	public static final String URL_DELETE_GROUP= BASE_URL + "delete_group.php";
	
	public static final String URL_BLOCK_USER= BASE_URL + "insert_block_user.php";
	
	public static final String URL_UNBLOCK_USER= BASE_URL + "delete_block_user.php";
	
	public static final String URL_RETRIEVE_REMINDER= BASE_URL + "retrievereminderbyid2.php";

	public static final String URL_CHECK_GCM_ID= BASE_URL + "check_gcmid.php";
	public static final String URL_UPDATE_GCM_ID= BASE_URL + "update_gcmid.php";
	
	public static final String URL_NEW_USER = BASE_URL + "insert_user.php";
	public static final String URL_EXISTING_USER = BASE_URL + "check_user.php";
	public static final String URL_UPDATE_USER = BASE_URL + "update_user.php";	
	
	public static final String URL_UPDATE_PROFILE = BASE_URL + "update_user_profile.php";
	public static final String URL_RETRIEVE_PROFILE = BASE_URL + "retrive_user.php";

	public static final String URL_IMAGE_UPLOAD = BASE_URL + "pic.php";
	public static final String SERVER_IMAGE_PATH = "https://s3-us-west-2.amazonaws.com/ringabell/picture";

	
	public static final String URL_RATING= BASE_URL + "insert_rating.php";
	
	public static final String URL_RETRIEVE_FRIENDS = BASE_URL +"retrievefriends.php";
	public static final String URL_RETRIEVE_GROUPS = BASE_URL +"retrieve_usergroup.php";
	public static final String URL_RETRIEVE_BLOCK_USER = BASE_URL + "retrieve_block_owner.php";
	public static final String URL_RETRIEVE_BLOCK_CONTACTS = BASE_URL + "retrieve_block_user.php";	
	
	public static final String URL_RETRIEVE_ABOUTUS = BASE_URL + "retrieve_aboutus.php";
	public static final String URL_RESPONSE_REMINDER = BASE_URL + "response_reminder.php";	
	public static final String URL_RETRIEVE_MISSED_REMINDER = BASE_URL + "fetch_pending_reminder.php";	


	public static final String BROWSER_API_KEY = "AIzaSyDnEsN5FgEc577970JvQBtcGCeCgS76JxU";	
	public static final String NON_USER_MESSAGE = "http://www.share2people.com";	

 
    static InputStream is = null;
    static String response = null;
    public final static int GET = 1;
    public final static int POST = 2;
 
    public ServiceHandler() {    }
 
    /**
     * Making service call
     * @url - url to make request
     * @method - http request method
     * */
    public String makeServiceCall(String url, int method) {
    	
        return this.makeServiceCall(url, method,null);
    }
 
    /**
     * Making service call
     * @url - url to make request
     * @method - http request method
     * @params - http request params
     * */
    public String makeServiceCall(String url, int method,
            ArrayList<NameValuePair> params) {
        try {
            // http client
            DefaultHttpClient httpClient = new DefaultHttpClient();
            HttpEntity httpEntity = null;
            HttpResponse httpResponse = null;
             String error="Error";
            // Checking http request method type
            if (method == POST) {
                HttpPost httpPost = new HttpPost(url);
                // adding post params
                if (params != null) {
                    httpPost.setEntity(new UrlEncodedFormEntity(params));
                }
 
                httpResponse = httpClient.execute(httpPost);
 
            } else if (method == GET) {
                // appending params to url
                if (params != null) {
                    String paramString = URLEncodedUtils
                            .format(params, "utf-8");
                    url += "?" + paramString;
                }
                HttpGet httpGet = new HttpGet(url);
 
                httpResponse = httpClient.execute(httpGet);
             //   System.out.println("HTTP RESPONSE CODE***"+httpResponse.getStatusLine().getStatusCode());
                if(httpResponse.getStatusLine().getStatusCode()!=HttpStatus.SC_OK){
                	
                System.out.println("HTTP RESPONSE CODE***"+httpResponse.getStatusLine().getStatusCode());
                //return error;
                }
                else{
                	System.out.println("HTTP RESPONSE CODE"+httpResponse.getStatusLine().getStatusCode());
                }
            }
            httpEntity = httpResponse.getEntity();
            is = httpEntity.getContent();
 
        } catch(UnknownHostException un){
        	System.out.println("HTTP Response Unknownhost exception**");
        	un.printStackTrace();
        	response="error";
        } 
        catch (UnsupportedEncodingException e) {
            
            System.out.println("HTTP Response UnsupportedEncodingException**");
            response="error";
            e.printStackTrace();
        } catch (ClientProtocolException e) {
        	System.out.println("HTTP Response ClientProtocolException**");
        	response="error";
            e.printStackTrace();
         } catch (IOException e) {
        	 System.out.println("HTTP Response IOException**");
        	 response="error";
            e.printStackTrace();
            //return error;
        }
       
 
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    is, "UTF-8"), 8);
            StringBuilder sb = new StringBuilder();
            String line = null;
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
            is.close();
            response = sb.toString();
            System.out.println("response is="+response);
        } catch (Exception e) {
            Log.e("Buffer Error", "Error: " + e.toString());
            System.out.println("HTTP Response Buffer Error**");
        }
 
        return response;
 
    }
}