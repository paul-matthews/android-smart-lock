package com.google.codelab.smartlock;

import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.credentials.Credential;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;

/**
 * Created by arthurthompson on 10/16/15.
 */
public class SignInFragment extends Fragment {

    private static final String TAG = "SignInFragment";
    private TextInputLayout mUsernameTextInputLayout;
    private TextInputLayout mPasswordTextInputLayout;

    @Override
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup container, Bundle savedInstanceState) {
        View view = layoutInflater.inflate(R.layout.fragment_sign_in, container, false);
        mUsernameTextInputLayout = (TextInputLayout) view.findViewById(R.id.usernameTextInputLayout);
        mPasswordTextInputLayout = (TextInputLayout) view.findViewById(R.id.passwordTextInputLayout);

        Button signInButton = (Button) view.findViewById(R.id.signInButton);
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                String username = mUsernameTextInputLayout.getEditText().getText().toString();
                String password = mPasswordTextInputLayout.getEditText().getText().toString();

//                if (CodelabUtil.isValidCredential(username, password)) {
//                    mUsername = username;
//                    goToContent();
//                } else {
//                    Log.d(TAG, "Credentials are invalid. Username or password are incorrect.");
//                }
                Credential credential = new Credential.Builder(username)
                        .setPassword(password)
                        .build();
                if (CodelabUtil.isValidCredential(credential)) {
                    CodelabUtil.goToContent(view.getContext(), credential);
                } else {
                    Log.d(TAG, "Credentials are invalid. Username or password are incorrect.");
                    Toast.makeText(view.getContext(), "Credentials are invalid", Toast.LENGTH_LONG).show();
                }
            }
        });

        Button clearButton = (Button) view.findViewById(R.id.clearButton);
        clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mUsernameTextInputLayout.getEditText().setText("");
                mPasswordTextInputLayout.getEditText().setText("");
            }
        });

        return view;
    }



}
