# fid-sdk-android

Tài liệu hướng dẫn tích hợp FID SDK cho Android

English version [see here](./README.md)

# Giới thiệu
FID SDK là thư viện để các app có thể tương tác với FID Platform. FID SDK bao gồm các chức năng chính như sau:
- Đăng nhập bằng tài khoản FID
- Hỗ trợ app lấy thông tin profile của user
- Hiện tại FID SDK hỗ trợ tất cả các thiệt bị cài đặt hệ điều hành Android 4.1 (API 16) trở lên.

## Hỗ trợ
Nếu trong quá trình tích hợp có thắc mắc gì bạn có thể:

- Liên hệ trực tiếp qua email với team SDK để được trợ giúp
- Tham khảo demo ở đây

## Bước 1
Liên hệ trực tiếp với ftech.ai để được cung cấp Client ID và các thông tin liên quan, sau đó tải [FID SDK](https://github.com/n76i/fid-sdk-android-demo/raw/main/fid-sdk/fid-sdk-release.aar)
## Bước 2: Thêm FID SDK vào dự án
Trong Android Studio:
- Chọn module app, nhấn chuột phải rồi chọn `New` -> `Module`
- Kéo xuống dưới chọn `Import .JAR/.AAR`-> `Next`
- Ở file name trỏ đường dẫn đến file SDK được tải ở bước 1
- Ở sub project name có thể đổi tên thành `fid-sdk`

Chú ý nếu bạn thấy đã import thành công nhưng không sử dụng được, có thể do SDK chưa được thêm vào `app`. Bạn có thể vào `File` -> `Project Structure` để thêm SDK vào module `app`
## Bước 3: Cấu hình thêm
### 1, Cấu hình AndroidManifest.xml
Trong `AndroidManifest.xml` thêm đoạn code sau ở trong `application`:
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

Trong `strings.xml` thêm 2 `string resources` này:
```xml
<string name="fid_app_id">6669642d646576</string>
<string name="fid_scheme">ai.ftech.id-dev</string>
```

Chú ý `App ID` và `Scheme` sẽ được cấp khi bạn đăng ký với Ftech
### 2, Cấu hình trong app/build.gradle
Trong `build.gradle` -> `android` -> `defaultConfigs` thêm đoạn code sau:
```
manifestPlaceholders = [
    'appAuthRedirectScheme': 'ai.ftech.id-dev'
]
```

Lưu ý `appAuthRedirectScheme` chính là `Scheme` được cấp ở trên
### 3, Cài thêm một số thư viện bổ sung
FID SDK được phát triển thêm từ `appauth-android`, với mục đích đơn giản hoá cách sử dụng nên cần bổ sung một số thư viện sau:
```
implementation "com.squareup.okio:okio:${project.okioVersion}"
implementation "joda-time:joda-time:${project.jodaVersion}"
```

Lưu ý ở đây chúng ta thấy `project.okioVersion` và `project.jodaVersion`, vì đảm bảo cả SDK và dự án của chúng ta đều dùng chung phiên bản nên sẽ cần quy định rõ, để cấu hình 2 giá trị này, chúng ta thêm đoạn code sau vào `build.gradle` của Project (không phải của module `app`):
```
project.ext {
    okioVersion = '2.10.0'
    jodaVersion = '2.10.10'
}
```
Hoàn tất, để kiểm tra mọi thứ đã hoạt động chúng ta đi đến hướng dẫn sử dụng

## Hướng dẫn sử dụng cơ bản
Ở hướng dẫn này sẽ cần import những class sau:
```java
import ai.ftech.fid.AuthState;
import ai.ftech.fid.factory.AuthStateManager;
import ai.ftech.fid.factory.FID;
import ai.ftech.fid.factory.FIDAuthStateChangeCallback;
import ai.ftech.fid.factory.FIDCallbackManager;
import ai.ftech.fid.factory.FIDCallbackType;
import ai.ftech.fid.factory.FIDUserChangeCallback;
```

Một số class cần được khởi tạo instance:
```java
AuthStateManager authStateManager = AuthStateManager.getInstance(this);
```

### 1, Khởi tạo
Bạn cần đảm bảo khởi tạo FID trước khi dùng những hàm khác của nó, có thể gọi ở `onCreate` của Activity
```java
FID.initialize(this);
```
Để chạy SDK trên môi trường sandbox, hãy thêm một tham số vào hàm khởi tạo như dưới đây:
```java
FID.initialize(this, "fid-dev");
```

Và để nhận lại kết quả sau khi đăng nhập từ WebView, cần thên đoạn sau vào phương thức `onActivityResult` của Activity:
```java
@Override
protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    FIDCallbackManager.onActivityResult(requestCode, resultCode, data);
}
```
### 2, Đăng ký các callback
Bạn cần đảm bảo đăng ký các callback trước khi thực hiện gọi `login` và các hàm khác:
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
            String expiredTime = String.format("Access token expires at: %s", DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss ZZ").print(expiresAt));
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
### 3, Đăng nhập FID
Gọi hàm sau để đăng nhập vào FID, kết quả sẽ được trả về ở callback được đăng ký ở trên (Có thể gọi hàm này nhiều lần mà không cần kiểm tra đã đăng nhập hay chưa):
```
FID.login(MainActivity.this);
```
### 4, Lấy thông tin User
Gọi hàm sau để lấy thông tin User, kết quả được trả về ở callback được đăng ký ở trên:
```
FID.fetchUser(MainActivity.this);
```
### 5, Làm mới Access Token
Access Token của FID chỉ live trong khoảng thời gian rất ngắn, nên cần làm mới. Gọi hàm sau để làm mới Access Token:
```
FID.refreshToken(MainActivity.this)
```
### 6, Đăng xuất FID
Gọi hàm sau để đăng xuất vào FID, kết quả sẽ được trả về ở callback được đăng ký ở trên (Có thể gọi hàm này nhiều lần mà không cần kiểm tra đã đăng nhập hay chưa):
```
FID.logout(MainActivity.this)
```
### 7, Kiểm tra đã đăng nhập
Nếu muốn bạn cũng có thể kiểm tra tình trạng đăng nhập của User bằng cách sau:
```java
if (authStateManager.getCurrent().isAuthorized()) {
   // do something
}
```
