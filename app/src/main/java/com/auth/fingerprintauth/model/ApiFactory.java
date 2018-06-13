package com.auth.fingerprintauth.model;

import android.preference.PreferenceManager;
import android.support.annotation.NonNull;

import com.auth.fingerprintauth.FingerprintAuthApp;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static com.auth.fingerprintauth.LoginActivity.IP;

public class ApiFactory {
    private static final int SHORT_TIME_OUT = 30;

    @NonNull
    public static ApiRequestService getApiRequestService() {
        return getRetrofitDefault(getOkHttpClient(SHORT_TIME_OUT)).create(ApiRequestService.class);
    }


    public static OkHttpClient getOkHttpClient(int timeOut){
        return new OkHttpClient.Builder()
                .connectTimeout(timeOut, TimeUnit.SECONDS)
                .readTimeout(timeOut, TimeUnit.SECONDS)
                .writeTimeout(timeOut, TimeUnit.SECONDS)
                .build();
    }


    @NonNull
    private static Retrofit getRetrofitDefault(OkHttpClient okHttpClient) {
        String mainUrl = "http://" + PreferenceManager.getDefaultSharedPreferences(
                FingerprintAuthApp.getAppContext()).getString(IP,"") + ":3000/";
        return new Retrofit.Builder()
                .baseUrl(mainUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .client(okHttpClient)
                .build();
    }


}