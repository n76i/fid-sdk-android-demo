# fid-sdk-android v1.0

FID SDK integration documentation for Android

Phiên bản tiếng Việt [xem ở đây](./README-vi.md)

# Introduce
FID SDK is a library for apps to interact with FID Platform. FID SDK includes the following main functions:
- Sign in with your FID account
- Support to get profile information of the user
- Currently, FID SDK supports all devices running Android 4.1 (API 16) and above.

## Support
If during the integration have any questions you may have:

- Contact directly via email with the SDK team for assistance
- Refer to the demo here

## Step 1
Contact [ftech.ai](https://ftech.ai/) directly for Client ID and related information, then download [FID SDK](https://github.com/n76i/fid-sdk-android-demo/raw/main/fid-sdk/fid-sdk-release.aar)
## Step 2: Add the FID SDK to the project
In Android Studio:
- Select module app, right click then select `New` -> `Module`
- Scroll down to select `Import .JAR/.AAR`-> `Next`
- In the file name, point to the path to the SDK file loaded in step 1
- In the sub project name you can change the name to `fid-sdk`

Note that if you find that the import was successful but not usable, it is possible that the SDK has not been added to `app`. You can go to `File` ->` Project Structure` to add the SDK to the `app` module
## Step 3: Additional configuration
### 1, Configuration AndroidManifest.xml
In `AndroidManifest.xml` add the following code in` application`:
```java
<activity android:name="ai.ftech.fid.RedirectUriReceiverActivity">
    <intent-filter>
        <action android:name="android.intent.action.VIEW" />

        <category android:name="android.intent.category.DEFAULT" />
        <category android:name="android.intent.category.BROWSABLE" />

        <data android:scheme="@string/fid_scheme" />
    </intent-filter>
</activity>
```

In the `strings.xml` add these 2 `string resources`:
```xml
<string name="fid_app_id">6669642d646576</string>
<string name="fid_scheme">ai.ftech.id-dev</string>
```

Note `App ID` and `Scheme` will be issued when you register with Ftech
### 2, Configuration in app/build.gradle
In `build.gradle` -> `android` -> `defaultConfigs` add the following code:
```
manifestPlaceholders = [
    'appAuthRedirectScheme': 'ai.ftech.id-dev'
]
```

Note `appAuthRedirectScheme` is the same `Scheme` provided above
### 3, Install some additional libraries
FID SDK is developed from `appauth-android`, with the aim of simplifying the use should be added some libraries follows:
```
implementation "com.squareup.okio:okio:${project.okioVersion}"
implementation "joda-time:joda-time:${project.jodaVersion}"
implementation "androidx.browser:browser:1.3.0"
implementation 'org.greenrobot:eventbus:3.2.0'
```

Note that here we see `project.okioVersion` and `project.jodaVersion`, because both our SDK and our project are using the same version, we need to specify, to configure these 2 values, We add the following code to the Project's `build.gradle` (not the `app` module):
```
project.ext {
    okioVersion = '2.10.0'
    jodaVersion = '2.10.10'
}
```
Finished, to check everything is working we go to the user guide

## Basic instructions for use
For this tutorial, we will need to import the following classes:
```java
import ai.ftech.fid.AuthState;
import ai.ftech.fid.factory.AuthStateManager;
import ai.ftech.fid.factory.FID;
import ai.ftech.fid.factory.FIDAuthStateChangeCallback;
import ai.ftech.fid.factory.FIDCallbackManager;
import ai.ftech.fid.factory.FIDCallbackType;
import ai.ftech.fid.factory.FIDUserChangeCallback;
```

Some classes need to initialize the instance:
```java
AuthStateManager authStateManager = AuthStateManager.getInstance(this);
```

### 1, Initialization
You need to make sure to initialize the FID before using its other functions, which can be called on the Activity's `onCreate`.
```java
FID.initialize(this);
```
To run the SDK on a sandbox environment, add a parameter to the constructor as shown below
```java
FID.initialize(this, "fid-dev");
```

And to get the results back after logging in from the WebView, you need to add the following to the Activity's `onActivityResult` method:
```java
@Override
protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    FIDCallbackManager.onActivityResult(requestCode, resultCode, data);
}
```
### 2, Register callbacks
Make sure to register the callbacks before making `login` calls and other functions:
```java
FIDCallbackManager.registerCallback(FIDCallbackType.USER_CHANGE, new FIDUserChangeCallback() {
    @Override
    public void onUserChange(@Nullable JSONObject user, @Nullable Exception e) {
        if (user != null) {
            Log.e("FID", user.toString());
            txtUserInfo.setText("User Info: " + user.toString());
        } else {
            txtUserInfo.setText("User Info: ");
        }

        if (e != null) {
            Log.e("FID", e.toString());
        }
    }
});

FIDCallbackManager.registerCallback(FIDCallbackType.AUTH_STATE_CHANGE, new FIDAuthStateChangeCallback() {
    @Override
    public void onAuthStateChange(@Nullable AuthState authState, @Nullable Exception e) {
        loadText();
        if (authState != null && authState.isAuthorized()) {
            txtAccessToken.setText("Access Token: " + authState.getAccessToken());
            Long expiresAt = authState.getAccessTokenExpirationTime();
            if (authState.getRefreshToken() != null) {
                Log.e("FID", "Refresh Token returned");
            }
            if (authState.getAccessToken() != null) {
                Log.e("FID", "Access Token returned");
            }
            String expiredTime = String.format("Access token expires at: %s", 
                DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss ZZ").print(expiresAt));
            Log.e("FID", expiredTime);
            FID.fetchUser(MainActivity.this);
        } else {
            Log.e("FID", "isAuthorized false or authState null");
            txtUserInfo.setText("User Info: ");
        }
        if (e != null) {
            Log.e("FID", e.toString());
        }
    }
});
```
### 3, Login FID
Call the following function to log into the FID, the results will be returned in the above registered callback (This function can be called multiple times without checking whether or not logged in):
```
FID.login(MainActivity.this);
```
### 4, Get User information
Call the following function to get the User information, the result is returned in the above registered callback:
```
FID.fetchUser(MainActivity.this);
```
### 5, Refresh the Access Token
FID's Access Token is only live for a very short time, so it needs to be refreshed. Call the following function to refresh the Access Token:
```
FID.refreshToken(MainActivity.this)
```
### 6, Logout of FID
Call the following function to log out of the FID, the results will be returned in the above registered callback (This function can be called multiple times without checking whether to log in or not):
```
FID.logout(MainActivity.this)
```
### 7, Check signed in
If you want you can also check the login status of the User by the following:
```java
if (authStateManager.getCurrent().isAuthorized()) {
   // do something
}
```

## Related documents
FID is built according to openid standard, full documentation can be viewed [here](https://openid.net/specs/openid-connect-core-1_0.html#Authenticates)

Some basic information that can be a quick reference to develop your app

### 1, User information
This specification defines a set of standard Claims. They can be requested to be returned either in the UserInfo Response.

| Member | Type  | Description  |
| ------- | --- | --- |
| sub | string | Subject - Global user ID, ie: "sub": "1000" |
| name | string | Display name, ie: "preferred_username": "Bạch Ngọc Sơn" |
| preferred_username | string | Field username, ie: "name": "sonbn" |
| picture | string | Primary profile picture url, ie: "picture": "https://a.com/b.jpg" |
| email | string | Primary email, used by local login, ie: "sonbn@ftech.ai" |
| email_verified | bool | Primary email verification status |
| phone_number | string | Primary phone number, used by local login. "+84"-formated, ie: "phone_number": "+8453458875". This field is also used for SMS OTP |
| phone_number_verified | bool | Primary phone number verification status |
| extra_info | object | Example: "extra_info": {"emails":["a@a.com","b@b.com"],"names":["A","B","C"],"pictures":[]} |
