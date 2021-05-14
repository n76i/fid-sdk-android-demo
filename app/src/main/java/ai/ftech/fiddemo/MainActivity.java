package ai.ftech.fiddemo;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import org.joda.time.format.DateTimeFormat;
import org.json.JSONObject;

import ai.ftech.fid.AuthState;
import ai.ftech.fid.factory.AuthStateManager;
import ai.ftech.fid.factory.Configuration;
import ai.ftech.fid.factory.FID;
import ai.ftech.fid.factory.FIDAuthStateChangeCallback;
import ai.ftech.fid.factory.FIDCallbackManager;
import ai.ftech.fid.factory.FIDCallbackType;
import ai.ftech.fid.factory.FIDUserChangeCallback;

public class MainActivity extends AppCompatActivity {
    private ClipboardManager clipboard;
    private AuthStateManager authStateManager;

    private TextView txtAccessToken;
    private TextView txtRefreshToken;
    private TextView txtIdToken;
    private TextView txtUserInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        FID.initialize(this, "fid-dev");

        clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        authStateManager = AuthStateManager.getInstance(this);

        txtAccessToken = findViewById(R.id.txtAccessToken);
        txtRefreshToken = findViewById(R.id.txtRefreshToken);
        txtIdToken = findViewById(R.id.txtIdToken);
        txtUserInfo = findViewById(R.id.txtUserInfo);

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

        findViewById(R.id.btnLogin).setOnClickListener(view -> FID.login(MainActivity.this));
        findViewById(R.id.btnUserInfo).setOnClickListener(view -> FID.fetchUser(MainActivity.this));
        findViewById(R.id.btnLogout).setOnClickListener(view -> FID.logout(MainActivity.this));
        findViewById(R.id.btnRefreshToken).setOnClickListener(view -> FID.refreshToken(MainActivity.this));


        findViewById(R.id.btnCopyAccessToken).setOnClickListener(view -> {
            ClipData clip = ClipData.newPlainText("Access Token", authStateManager.getCurrent().getAccessToken());
            clipboard.setPrimaryClip(clip);
            Toast.makeText(MainActivity.this, "copied", Toast.LENGTH_SHORT).show();
        });
        findViewById(R.id.btnCopyRefreshToken).setOnClickListener(view -> {
            ClipData clip = ClipData.newPlainText("Refresh Token", authStateManager.getCurrent().getRefreshToken());
            clipboard.setPrimaryClip(clip);
            Toast.makeText(MainActivity.this, "copied", Toast.LENGTH_SHORT).show();
        });
        findViewById(R.id.btnCopyIdToken).setOnClickListener(view -> {
            ClipData clip = ClipData.newPlainText("Id Token", authStateManager.getCurrent().getIdToken());
            clipboard.setPrimaryClip(clip);
            Toast.makeText(MainActivity.this, "copied", Toast.LENGTH_SHORT).show();
        });
        findViewById(R.id.btnUserInfoCopy).setOnClickListener(view -> {
            ClipData clip = ClipData.newPlainText("User Info", authStateManager.getCurrent().getIdToken());
            clipboard.setPrimaryClip(clip);
            Toast.makeText(MainActivity.this, "copied", Toast.LENGTH_SHORT).show();
        });

        if (authStateManager.getCurrent().isAuthorized()) {
            FID.login(this);
        }
    }

    private void loadText() {
        getSupportActionBar().setTitle("Client " + Configuration.getInstance(this).getClientId());
        AuthState authState = authStateManager.getCurrent();
        txtAccessToken.setText("Access Token: " + authState.getAccessToken());
        txtRefreshToken.setText("Refresh Token: " + authState.getRefreshToken());
        txtIdToken.setText("Id Token: " + authState.getIdToken());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        FIDCallbackManager.onActivityResult(requestCode, resultCode, data);
    }
}