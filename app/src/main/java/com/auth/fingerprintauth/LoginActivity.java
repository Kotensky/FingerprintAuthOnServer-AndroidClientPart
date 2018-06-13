package com.auth.fingerprintauth;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.widget.EditText;
import android.widget.Toast;

public class LoginActivity extends AppCompatActivity {


    public static final String IP = "ip";
    public static final String LOGIN = "login";
    public static final String ENCODED_PASS = "pass";

    private EditText mEditTextIp;
    private EditText mEditTextLogin;
    private EditText mEditTextPass;
    private SharedPreferences mPreferences;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        mEditTextIp = findViewById(R.id.ip_edt);
        mEditTextLogin = findViewById(R.id.login_edt);
        mEditTextPass = findViewById(R.id.pass_edt);

        findViewById(R.id.login_btn).setOnClickListener(view -> prepareLogin());
    }


    private void prepareLogin() {
        final String ip = mEditTextIp.getText().toString();
        final String login = mEditTextLogin.getText().toString();
        final String pass = mEditTextPass.getText().toString();
        if (isValidData(ip, login, pass)) {
            saveAuthData(ip, login, pass);
            finish();
            startActivity(new Intent(this, MainActivity.class));
        }
    }

    private boolean isValidData(String ip, String login, String pass) {
        if (ip.isEmpty()){
            Toast.makeText(this, getString(R.string.ip_empty_error), Toast.LENGTH_SHORT).show();
            return false;
        }
        if (login.isEmpty()){
            Toast.makeText(this, getString(R.string.login_empty_error), Toast.LENGTH_SHORT).show();
            return false;
        }
        if (pass.isEmpty()){
            Toast.makeText(this, getString(R.string.pass_empty_error), Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private void saveAuthData(String ip, String login, String pass) {
        if (FingerprintUtils.isSensorStateAt(FingerprintUtils.mSensorState.READY, this)) {
            String encodedPass = CryptoUtils.encode(pass);
            mPreferences.edit()
                    .putString(IP, ip)
                    .putString(LOGIN, login)
                    .putString(ENCODED_PASS, encodedPass)
                    .apply();
        }
    }
}
