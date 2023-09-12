# react-native-apps-insights

## Install 

The **recommended** way to install the latest version of react-native-apps-insights is by running the command below:

NPM Install
```bash
npm i @ashokdevatwal/react-native-apps-insights
```
Yarn Install
```bash
yarn add @ashokdevatwal/react-native-apps-insights
```

## Usages

```javascript
import AppsInsights from "@ashokdevatwal/react-native-apps-insights";

// Needed Only To Track Facebook App Install Campaign 
AppsInsights.config({
  installReferrerDecryptionKey : "2575590594a9cd809e5bfacf397f8c1ac730dbc38a3e137ecd1ab66591c8c3c9" 
});

// Install Track
 AppsInsights.getInstallReferrerInfoFromPlay()
 .then(installReferrerInfo => {
  // Handle the install referrer info here
  console.log('Install Referrer Info:', installReferrerInfo);
})
.catch(error => {
  // Handle any errors here
  console.error('Error:', error);
});
```

## If App Installed Direct From Google Play Store 

```json
{
	"googlePlayInstant": "false", // Information if your app's instant version (if you have one) was launched in past 7 days
	"installBeginTimestampSeconds": "2022-10-15 11:42:30",  // Timestamp of when app installation on device begun
	"installBeginTimestampServerSeconds": "2022-10-15 11:42:29", // Server timestamp of when app installation on device begun
	"installReferrer": "utm_source=google-play&utm_medium=organic", // Install referrer string value  
	"installVersion": "1.9.6", // Original app version which was installed
	"referrerClickTimestampSeconds": "1970-01-01 05:30:00", // Timestamp of when user clicked on URL which redirected him/her to Play Store to download your app
	"referrerClickTimestampServerSeconds": "1970-01-01 05:30:00", // Server timestamp of when user clicked on URL which redirected him/her to Play Store to download your app
	"utm_medium": "organic", 
	"utm_source": "google-play"
}
```

## If App Installed From Facebook App Install Campaign 

```json
{
  "account_id": "444444",
  "ad_id": "12345",
  "ad_objective_name": "APP_INSTALLS",
  "adgroup_id": "54321",
  "adgroup_name": "Adams Test Ad Group",
  "campaign_group_id": "654321",
  "campaign_group_name": "Adams Test Campaign Group",
  "campaign_id": "987654",
  "campaign_name": "Adams Test Campaign",
  "googlePlayInstant": "false",
  "installBeginTimestampSeconds": "2023-09-08 11:28:23",
  "installBeginTimestampServerSeconds": "2023-09-08 11:28:21",
  "installReferrer": "utm_source=google-play&utm_medium=organic",
  "installVersion": "3.0.8",
  "referrerClickTimestampSeconds": "1970-01-01 05:30:00",
  "referrerClickTimestampServerSeconds": "1970-01-01 05:30:00",
  "utm_campaign": "fb4a",
  "utm_source": "apps.facebook.com",
  "utm_content": "%7B%22app%22%3A0%2C%22t%22%3[...long Hash....]301%2C%22source%22%3Anull%7D" // encrypted campaign metadata
}
```