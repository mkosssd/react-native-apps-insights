package com.ashokdevatwal;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.ReadableMapKeySetIterator;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Promise;

import com.facebook.react.modules.core.DeviceEventManagerModule;

import java.util.HashMap;
import java.util.Map;
import java.util.Iterator;
import java.util.Date;
import java.text.SimpleDateFormat;

import javax.annotation.Nullable;

import org.json.JSONObject;
import org.json.JSONException;

import android.os.RemoteException;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

// Install Referrer
import com.android.installreferrer.api.InstallReferrerClient;
import com.android.installreferrer.api.InstallReferrerStateListener;
import com.android.installreferrer.api.ReferrerDetails;

import com.ashokdevatwal.appsinsights.FacebookAppInstallCampaign;

public class AppsInsightsModule extends ReactContextBaseJavaModule {

	private static final String TAG = "AppsInsights";

	private FacebookAppInstallCampaign facebookAppInstallCampaign = new FacebookAppInstallCampaign();
		
	// create the map to store install referrer details
    WritableMap installReferrerInfo = Arguments.createMap();

	// variable for install referrer client.
  	InstallReferrerClient referrerClient = null;

	private static ReactApplicationContext reactContext;

	public AppsInsightsModule(ReactApplicationContext context) {
	    super(context);
	    reactContext = context;
	}

	@Override
	public String getName() {
		return "AppsInsights";
	}

    @ReactMethod
    public void config(ReadableMap config) {

    	if ( config.hasKey("installReferrerDecryptionKey") ) {
            String installReferrerDecryptionKey = config.getString("installReferrerDecryptionKey");
            facebookAppInstallCampaign.setInstallReferrerDecryptionKey( installReferrerDecryptionKey );
        }
    }

    @ReactMethod
    public void getInstallReferrerInfoFromPlay( Promise promise ) {

        try {
        	SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(reactContext);
        	boolean hasRunBefore = preferences.getBoolean("hasRunBefore", false);
        	
        	if (!hasRunBefore) {

	            referrerClient = InstallReferrerClient.newBuilder(getReactApplicationContext()).build();
	            referrerClient.startConnection(new InstallReferrerStateListener() {
	                @Override
	                public void onInstallReferrerSetupFinished(int responseCode) {
	                    switch (responseCode) {
	                        case InstallReferrerClient.InstallReferrerResponse.OK: {
	                            try {
	                                ReferrerDetails response = referrerClient.getInstallReferrer();
	                                
	                                if (response != null) {

	                                	installReferrerInfo = parseInstallReferrerInfo( response );

	                                    // Set a flag to indicate that the code has run
						                SharedPreferences.Editor editor = preferences.edit();
						                editor.putString("installReferrerInfo", installReferrerInfo.toString());
						                editor.putBoolean("hasRunBefore", true);
						                editor.apply();
						                
						                promise.resolve(installReferrerInfo);
	                                } else {
	                                	promise.reject("DATA_NOT_FOUND", "Install referrer info not recived.");
	                                }
	                            } catch (RemoteException ex) {
	                            	promise.reject("ERROR_CODE", ex.getMessage());
	                            }
	                            break;
	                        }
	                        case InstallReferrerClient.InstallReferrerResponse.FEATURE_NOT_SUPPORTED: {
	                        	promise.reject("FEATURE_NOT_SUPPORTED", "FEATURE_NOT_SUPPORTED");
	                            break;
	                        }
	                        case InstallReferrerClient.InstallReferrerResponse.SERVICE_UNAVAILABLE: {
	                        	promise.reject("SERVICE_UNAVAILABLE", "SERVICE_UNAVAILABLE");
	                            break;
	                        }
	                        case InstallReferrerClient.InstallReferrerResponse.DEVELOPER_ERROR: {
	                        	promise.reject("DEVELOPER_ERROR", "DEVELOPER_ERROR");
	                            break;
	                        }
	                        case InstallReferrerClient.InstallReferrerResponse.SERVICE_DISCONNECTED: {
	                        	promise.reject("SERVICE_DISCONNECTED", "SERVICE_DISCONNECTED");
	                            break;
	                        }
	                    }
	                }

	                @Override
	                public void onInstallReferrerServiceDisconnected() {
	                    // no need to handle this
	                }
	            });
			} else {

				// If it's not the first run, retrieve the stored install referrer info
            	String storedInstallReferrerInfo = preferences.getString("installReferrerInfo", null);

            	if (storedInstallReferrerInfo != null) {

            		try {
	                    // Parse the stored JSON string to a WritableMap
	                    JSONObject jsonObject = new JSONObject(storedInstallReferrerInfo);
	                    WritableMap installReferrerInfo = convertJsonObjectToWritableMap( (JSONObject) jsonObject.get("NativeMap") );
		                    
	                    // Resolve the Promise with the stored install referrer info
	                    promise.resolve(installReferrerInfo);
	                } catch (JSONException e) {
	                    // Handle JSON parsing errors
	                    promise.reject("JSON_PARSING_ERROR", e.getMessage());
	                }
	            } else {
	                // Handle the case where stored data is missing
	                promise.reject("DATA_NOT_FOUND", "Install referrer info not found in storage.");
	            }
            }
        } catch (Throwable ex) {
        	promise.reject("ERROR_CODE", ex.getMessage());
        }
    }
 
	@ReactMethod
    public void addListener(String eventName) {
    	// Keep: Required for RN built in Event Emitter Calls.
    }

    @ReactMethod
    public void removeListeners(Integer count) {
		// Keep: Required for RN built in Event Emitter Calls.
    }

    private void sendEvent(ReactContext reactContext, String eventName, @Nullable WritableMap params) {
        reactContext
            .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
            .emit(eventName, params);
    }

	/** 
	 **  Module Core Methods
	 ***************************************/ 
    
    private WritableMap parseInstallReferrerInfo(ReferrerDetails response) {
    	String installReferrer = null;
        long referrerClickTimestampSeconds = 0L;
        long installBeginTimestampSeconds = 0L;
        long referrerClickTimestampServerSeconds = 0L;
        long installBeginTimestampServerSeconds = 0L;
        String installVersion = null;
        boolean googlePlayInstant = false;
 
 		WritableMap parsedInstallReferrerInfo = Arguments.createMap();

        installReferrer = response.getInstallReferrer();
        referrerClickTimestampSeconds = response.getReferrerClickTimestampSeconds();
        installBeginTimestampSeconds = response.getInstallBeginTimestampSeconds();
        referrerClickTimestampServerSeconds = response.getReferrerClickTimestampServerSeconds();
        installBeginTimestampServerSeconds = response.getInstallBeginTimestampServerSeconds();
        installVersion = response.getInstallVersion();
        googlePlayInstant = response.getGooglePlayInstantParam();        
  
        parsedInstallReferrerInfo.putString("installReferrer", installReferrer);
        parsedInstallReferrerInfo.putString("referrerClickTimestampSeconds", timestampToDatetime( referrerClickTimestampSeconds, "yyyy-MM-dd HH:mm:ss") );
        parsedInstallReferrerInfo.putString("installBeginTimestampSeconds", timestampToDatetime( installBeginTimestampSeconds, "yyyy-MM-dd HH:mm:ss" ) );
        parsedInstallReferrerInfo.putString("referrerClickTimestampServerSeconds", timestampToDatetime( referrerClickTimestampServerSeconds, "yyyy-MM-dd HH:mm:ss") );
        parsedInstallReferrerInfo.putString("installBeginTimestampServerSeconds", timestampToDatetime( installBeginTimestampServerSeconds, "yyyy-MM-dd HH:mm:ss") );
        parsedInstallReferrerInfo.putString("installVersion", installVersion);
        parsedInstallReferrerInfo.putString("googlePlayInstant", Boolean.toString(googlePlayInstant));
	                                    
        // Append URL Parameters
        parseUrlParams( installReferrer, parsedInstallReferrerInfo );

        if( parsedInstallReferrerInfo.hasKey("utm_content") ) {
        	HashMap<String, Object> parsedInstallReferrerInfoHashMap = writableMapToHashMap( parsedInstallReferrerInfo );
        	parsedInstallReferrerInfoHashMap = facebookAppInstallCampaign.campaignMetaData( parsedInstallReferrerInfoHashMap );

			parsedInstallReferrerInfo = Arguments.createMap();

        	// Iterate through the HashMap and add key-value pairs to the WritableMap
			for (Map.Entry<String, Object> entry : parsedInstallReferrerInfoHashMap.entrySet()) {
			    String key = entry.getKey();
			    Object value = entry.getValue();

			    if (value instanceof String) {
			        parsedInstallReferrerInfo.putString(key, (String) value);
			    } else if (value instanceof Integer) {
			        parsedInstallReferrerInfo.putInt(key, (Integer) value);
			    } else if (value instanceof Double) {
			        parsedInstallReferrerInfo.putDouble(key, (Double) value);
			    } else if (value instanceof Boolean) {
			        parsedInstallReferrerInfo.putBoolean(key, (Boolean) value);
			    } else if (value == null) {
			        parsedInstallReferrerInfo.putNull(key);
			    }
			    // You can add more cases for other types if needed
			}
        }
		
		if( parsedInstallReferrerInfo.hasKey("campaign_name") ) {
			parsedInstallReferrerInfo.putString("utm_campaign",  parsedInstallReferrerInfo.getString("campaign_name") );
		}

        return parsedInstallReferrerInfo;
    }

    private static void parseUrlParams( String url,  WritableMap writableMap) {
        String[] params = url.split("&");
        
        for (String param : params) {
            String[] keyValue = param.split("=");
            if (keyValue.length == 2) {
                writableMap.putString(keyValue[0], keyValue[1]);
            }
        }
    }

    private static String timestampToDatetime(long timestampInSeconds, String format) {
        // Convert the timestamp to milliseconds
        long timestampMillis = timestampInSeconds * 1000;

        // Create a SimpleDateFormat for the desired datetime format
        SimpleDateFormat sdf = new SimpleDateFormat(format);

        // Convert the timestamp to a Date
        Date datetime = new Date(timestampMillis);

        // Format the Date as a string
        return sdf.format(datetime);
    }

	private WritableMap convertJsonObjectToWritableMap(JSONObject jsonObject) throws JSONException {
        WritableMap writableMap = Arguments.createMap();
        
        // Iterate through the keys of the JSONObject
        Iterator<String> iterator = jsonObject.keys();
        while (iterator.hasNext()) {
            String key = iterator.next();
            Object value = jsonObject.get(key);
            
            // Check the type of the value and add it to the WritableMap
            if (value instanceof String) {
                writableMap.putString(key, (String) value);
            } else if (value instanceof Integer) {
                writableMap.putInt(key, (Integer) value);
            } else if (value instanceof Double) {
                writableMap.putDouble(key, (Double) value);
            } else if (value instanceof Boolean) {
                writableMap.putBoolean(key, (Boolean) value);
            } else if (value instanceof JSONObject) {
                // If it's a nested JSONObject, recursively convert it to a WritableMap
                writableMap.putMap(key, convertJsonObjectToWritableMap((JSONObject) value));
            } else {
                // Handle other data types as needed
            }
        }
        
        return writableMap;
    }

    private void mergeJsonObjectToWritableMap(JSONObject jsonObject, WritableMap writableMap) throws JSONException {
        
        // Iterate through the keys of the JSONObject
        Iterator<String> iterator = jsonObject.keys();
        while (iterator.hasNext()) {
            String key = iterator.next();
            Object value = jsonObject.get(key);
            
            // Check the type of the value and add it to the WritableMap
            if (value instanceof String) {
                writableMap.putString(key, (String) value);
            } else if (value instanceof Integer) {
                writableMap.putInt(key, (Integer) value);
            } else if (value instanceof Double) {
                writableMap.putDouble(key, (Double) value);
            } else if (value instanceof Boolean) {
                writableMap.putBoolean(key, (Boolean) value);
            } else if (value instanceof JSONObject) {
                // If it's a nested JSONObject, recursively convert it to a WritableMap
                writableMap.putMap(key, convertJsonObjectToWritableMap((JSONObject) value));
            } else {
                // Handle other data types as needed
            }
        }
    }

    public HashMap<String, Object> writableMapToHashMap(ReadableMap readableMap) {
	    HashMap<String, Object> hashMap = new HashMap<>();

	    ReadableMapKeySetIterator iterator = readableMap.keySetIterator();
	    while (iterator.hasNextKey()) {
	        String key = iterator.nextKey();
	        hashMap.put(key, readableMap.getString(key)); // You can use other methods like getDouble, getInt, etc. depending on your use case.
	    }

	    return hashMap;
	}
}
