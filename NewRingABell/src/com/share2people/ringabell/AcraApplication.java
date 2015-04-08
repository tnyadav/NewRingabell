package com.share2people.ringabell;

import org.acra.ACRA;
import org.acra.ReportField;
import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;
import org.acra.sender.HttpSender;

import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.share2people.ringabell.R;

import android.app.Application;
import android.content.Context;

@ReportsCrashes(
	    formUri = "https://ankitgupta90.cloudant.com/acra-techsync_fonecall/_design/acra-storage/_update/report",
	    reportType = HttpSender.Type.JSON,
	    httpMethod = HttpSender.Method.POST,
	    formUriBasicAuthLogin = "cencedimendurtedsingivel",
	    formUriBasicAuthPassword = "qtB1idIowpXMGeeuyecMwAIF",
	    formKey = "", // This is required for backward compatibility but not used
	    customReportContent = {
	            ReportField.APP_VERSION_CODE,
	            ReportField.APP_VERSION_NAME,
	            ReportField.ANDROID_VERSION,
	            ReportField.PACKAGE_NAME,
	            ReportField.REPORT_ID,
	            ReportField.BUILD,
	            ReportField.STACK_TRACE
	    },
	    mode = ReportingInteractionMode.TOAST,
	    resToastText = R.string.toast_crash
	)
//
public class AcraApplication extends Application{
@Override
public void onCreate() {
	// TODO Auto-generated method stub
	super.onCreate();
	ACRA.init(this);
	initImageLoader(getApplicationContext());
}
public static void initImageLoader(Context context) {
	// This configuration tuning is custom. You can tune every option, you may tune some of them,
	// or you can create default configuration by
	//  ImageLoaderConfiguration.createDefault(this);
	// method.
	ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(context)
			.threadPriority(Thread.NORM_PRIORITY - 2)
			.denyCacheImageMultipleSizesInMemory()
			.diskCacheFileNameGenerator(new Md5FileNameGenerator())
			.diskCacheSize(50 * 1024 * 1024) // 50 Mb
			.tasksProcessingOrder(QueueProcessingType.LIFO)
			.writeDebugLogs() // Remove for release app
			.build();
	// Initialize ImageLoader with configuration.
	ImageLoader.getInstance().init(config);
}
}
