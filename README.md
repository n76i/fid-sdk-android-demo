# fid-sdk-android

Tài liệu hướng dẫn tích hợp FID SDK cho Android

# Giới thiệu
FID SDK là bộ thư viện để các app có thể tương tác với FID Platform. FID SDK bao gồm các chức năng chính như sau:
- Đăng nhập bằng tài khoản FID
- Hỗ trợ app lấy thông tin profile của user
- Hiện tại FID SDK hỗ trợ tất cả các thiệt bị cài đặt hệ điều hành Android 4.1 (API 16) trở lên.

## Hỗ trợ
Nếu trong quá trình tích hợp có thắc mắc gì bạn có thể:

- Liên hệ trực tiếp qua email với team SDK để được trợ giúp
- Tham khảo demo ở đây

## Bước 1
Liên hệ trực tiếp với ftech.ai để được cung cấp Client ID và các thông tin liên quan, sau đó tải [FID SDK](https://github.com/n76i/fid-sdk-android-demo/blob/main/fid-sdk/fid-sdk-release.aar)
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
