package com.auth.fingerprintauth;

import android.content.Context;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.support.v4.hardware.fingerprint.FingerprintManagerCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

public class FingerprintAuthenticationDialogFragment extends DialogFragment
        implements FingerprintUiHelper.Callback {


    private  FingerprintManagerCompat.CryptoObject mCryptoObject;
    private FingerprintUiHelper mFingerprintUiHelper;
    private MainActivity mActivity;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        setStyle(DialogFragment.STYLE_NORMAL, android.R.style.Theme_Material_Light_Dialog);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        getDialog().setTitle(getString(R.string.sign_in));
        View v = inflater.inflate(R.layout.fingerprint_dialog_container, container, false);
        Button mCancelButton = v.findViewById(R.id.cancel_button);
        mCancelButton.setOnClickListener(view -> dismiss());

        mFingerprintUiHelper = new FingerprintUiHelper(
                FingerprintManagerCompat.from(getActivity()),
                PreferenceManager.getDefaultSharedPreferences(getActivity()),
                v.findViewById(R.id.fingerprint_description),
                v.findViewById(R.id.fingerprint_icon),
                v.findViewById(R.id.fingerprint_status), this);
        mCancelButton.setText(R.string.cancel);
        if (!mFingerprintUiHelper.isFingerprintAuthAvailable() && getActivity() != null) {
            mActivity.isFingerprintAuthDisable();
        }
        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        mFingerprintUiHelper.startListening(mCryptoObject);
    }


    @Override
    public void onPause() {
        super.onPause();
        mFingerprintUiHelper.stopListening();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mActivity = (MainActivity) getActivity();
    }

    public void setCryptoObject( FingerprintManagerCompat.CryptoObject cryptoObject) {
        mCryptoObject = cryptoObject;
    }


    @Override
    public void onAuthenticated(FingerprintManagerCompat.AuthenticationResult result) {
        if (getActivity() != null) {
            mActivity.onAuthenticated(result);
        }
        dismiss();
    }

    @Override
    public void onNeedDeletePass() {
        if (getContext() != null) {
            Toast.makeText(getContext(), getString(R.string.fingerprint_exceeded_trying_count), Toast.LENGTH_SHORT).show();
        }
        if (getActivity() != null) {
            mActivity.onNeedDeletePass();
        }
        dismiss();
    }

    @Override
    public void onError() {

    }

}