package com.auth.fingerprintauth;

import android.content.SharedPreferences;
import android.support.v4.hardware.fingerprint.FingerprintManagerCompat;
import android.support.v4.os.CancellationSignal;
import android.widget.ImageView;
import android.widget.TextView;

public class FingerprintUiHelper extends FingerprintManagerCompat.AuthenticationCallback {

    private static final String TRYING_COUNT = "trying_count";
    private static final long ERROR_TIMEOUT_MILLIS = 1600;
    private static final long SUCCESS_DELAY_MILLIS = 1300;

    private final FingerprintManagerCompat mFingerprintManager;
    private final SharedPreferences mSharedPreferences;
    private final TextView mDescriptionTextView;
    private final ImageView mIcon;
    private final TextView mErrorTextView;
    private final Callback mCallback;
    private CancellationSignal mCancellationSignal;

    private boolean mSelfCancelled;


    FingerprintUiHelper(FingerprintManagerCompat fingerprintManager, SharedPreferences sharedPreferences,
                        TextView descriptionTextView, ImageView icon, TextView errorTextView, Callback callback) {
        mFingerprintManager = fingerprintManager;
        mSharedPreferences = sharedPreferences;
        mDescriptionTextView = descriptionTextView;
        mIcon = icon;
        mErrorTextView = errorTextView;
        mCallback = callback;
    }

    public boolean isFingerprintAuthAvailable() {
        return mFingerprintManager.isHardwareDetected()
                && mFingerprintManager.hasEnrolledFingerprints();
    }

    public void startListening(FingerprintManagerCompat.CryptoObject cryptoObject) {
        if (!isFingerprintAuthAvailable()) {
            return;
        }
        mCancellationSignal = new CancellationSignal();
        mSelfCancelled = false;

        mFingerprintManager
                .authenticate(cryptoObject, 0, mCancellationSignal, this, null);
        mIcon.setImageResource(R.drawable.ic_fp_40px);
        setDescription();
    }

    private void setDescription() {
        int tryingCount = mSharedPreferences.getInt(TRYING_COUNT, 5);
        if (tryingCount < 5) {
            mDescriptionTextView.setText(mDescriptionTextView.getContext()
                    .getString(R.string.fingerprint_description_trying_count, tryingCount));
            if (tryingCount < 1) {
                mSharedPreferences.edit().putInt(TRYING_COUNT, 5).apply();
                mCallback.onNeedDeletePass();
            }
        }
    }

    public void stopListening() {
        if (mCancellationSignal != null) {
            mSelfCancelled = true;
            mCancellationSignal.cancel();
            mCancellationSignal = null;
        }
    }

    @Override
    public void onAuthenticationError(int errMsgId, CharSequence errString) {
        if (!mSelfCancelled) {
            showError(errString);
            mIcon.postDelayed(mCallback::onError, ERROR_TIMEOUT_MILLIS);
        }
    }

    @Override
    public void onAuthenticationHelp(int helpMsgId, CharSequence helpString) {
        showError(helpString);
    }

    @Override
    public void onAuthenticationFailed() {
        showError(mIcon.getResources().getString(
                R.string.fingerprint_not_recognized));

        int tryingCount = mSharedPreferences.getInt(TRYING_COUNT, 5);
        if (tryingCount <= 1) {
            mSharedPreferences.edit().putInt(TRYING_COUNT, 5).apply();
            mIcon.postDelayed(mCallback::onNeedDeletePass, ERROR_TIMEOUT_MILLIS);
        }
        mSharedPreferences.edit().putInt(TRYING_COUNT, tryingCount - 1).apply();
        setDescription();

    }

    @Override
    public void onAuthenticationSucceeded(final FingerprintManagerCompat.AuthenticationResult result) {
        mErrorTextView.removeCallbacks(mResetErrorTextRunnable);
        mIcon.setImageResource(R.drawable.ic_fingerprint_success);
        mErrorTextView.setTextColor(
                mErrorTextView.getResources().getColor(R.color.success_color, null));
        mErrorTextView.setText(
                mErrorTextView.getResources().getString(R.string.fingerprint_success));

        mSharedPreferences.edit().putInt(TRYING_COUNT, 5).apply();
        mIcon.postDelayed(() -> mCallback.onAuthenticated(result), SUCCESS_DELAY_MILLIS);
    }

    private void showError(CharSequence error) {
        mIcon.setImageResource(R.drawable.ic_fingerprint_error);
        mErrorTextView.setText(error);
        mErrorTextView.setTextColor(
                mErrorTextView.getResources().getColor(R.color.warning_color, null));
        mErrorTextView.removeCallbacks(mResetErrorTextRunnable);
        mErrorTextView.postDelayed(mResetErrorTextRunnable, ERROR_TIMEOUT_MILLIS);
    }

    private Runnable mResetErrorTextRunnable = new Runnable() {
        @Override
        public void run() {
            mErrorTextView.setTextColor(
                    mErrorTextView.getResources().getColor(R.color.hint_color, null));
            mErrorTextView.setText(
                    mErrorTextView.getResources().getString(R.string.fingerprint_hint));
            mIcon.setImageResource(R.drawable.ic_fp_40px);
        }
    };

    public interface Callback {

        void onAuthenticated(FingerprintManagerCompat.AuthenticationResult result);

        void onNeedDeletePass();

        void onError();
    }
}