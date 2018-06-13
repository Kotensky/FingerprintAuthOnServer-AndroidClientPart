package com.auth.fingerprintauth;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.hardware.fingerprint.FingerprintManagerCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.auth.fingerprintauth.model.ApiFactory;

import javax.crypto.Cipher;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.auth.fingerprintauth.LoginActivity.ENCODED_PASS;
import static com.auth.fingerprintauth.LoginActivity.IP;
import static com.auth.fingerprintauth.LoginActivity.LOGIN;

public class MainActivity extends AppCompatActivity {


    private FingerprintAuthenticationDialogFragment fingerprintDialogFragment;
    private SharedPreferences mPreferences;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        if (!mPreferences.contains(ENCODED_PASS)) {
            onNeedDeletePass();
            return;
        }

        setContentView(R.layout.activity_main);

        fingerprintDialogFragment = new FingerprintAuthenticationDialogFragment();

        ((TextView) findViewById(R.id.login_txt))
                .setText(getString(R.string.login_temp, mPreferences.getString(LOGIN, "Вас")));
        findViewById(R.id.fingerprint_btn).setOnClickListener(view -> showFingerprintDialog());
        new Handler().postDelayed(this::showFingerprintDialog, 300);

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_logout:
                onNeedDeletePass();
                break;
        }
        return super.onOptionsItemSelected(item);
    }



    private void showFingerprintDialog() {
        if (FingerprintUtils.isSensorStateAt(FingerprintUtils.mSensorState.READY, this)) {
            FingerprintManagerCompat.CryptoObject cryptoObject = CryptoUtils.getCryptoObject();
            if (cryptoObject != null) {
                if (getFragmentManager().findFragmentByTag("FingerprintAuthenticationDialogFragmentTag") == null) {
                    fingerprintDialogFragment.setCryptoObject(cryptoObject);
                    fingerprintDialogFragment.show(getSupportFragmentManager(), "FingerprintAuthenticationDialogFragmentTag");
                }

            } else {
                mPreferences.edit().remove(LOGIN).apply();
                mPreferences.edit().remove(ENCODED_PASS).apply();
                Toast.makeText(this, getString(R.string.new_fingerprint_enrolled_description), Toast.LENGTH_SHORT).show();
            }

        }
    }


    public void isFingerprintAuthDisable() {

    }


    public void onAuthenticated(FingerprintManagerCompat.AuthenticationResult result) {
        Cipher cipher = result.getCryptoObject().getCipher();
        String encodedFingerprint = mPreferences.getString(ENCODED_PASS, null);
//        String decodedFingerprint = CryptoUtils.decode(encodedPass, cipher);
        new Handler().postDelayed(() -> startAuthRequest(encodedFingerprint), 500);
    }

    private void startAuthRequest(String encodedFingerprint) {
        ApiFactory.getApiRequestService()
                .signUp(mPreferences.getString(LOGIN, null), encodedFingerprint)
                .enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        if (response.isSuccessful()) {
                            Toast.makeText(MainActivity.this, getString(R.string.sign_in_successful), Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(MainActivity.this, getString(R.string.request_error), Toast.LENGTH_SHORT).show();
                            onNeedDeletePass();
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        Log.e("TAG", call.request().toString());
                        Toast.makeText(MainActivity.this, "Помилка: " + t.getMessage(), Toast.LENGTH_SHORT).show();

                    }
                });

    }


    public void onNeedDeletePass() {
        mPreferences.edit().remove(IP).apply();
        mPreferences.edit().remove(LOGIN).apply();
        mPreferences.edit().remove(ENCODED_PASS).apply();

        finish();
        startActivity(new Intent(this, LoginActivity.class));
    }
}
