package com.auth.fingerprintauth.model;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface ApiRequestService {

    @POST("login_fingerscan")
    Call<ResponseBody> signUp(@Query("login") String login,
                              @Query("encrypted_fingerprint") String encrypted_fingerprint);

}
