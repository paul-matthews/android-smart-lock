/**
 * Copyright Google Inc. All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.codelab.smartlock;

import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.credentials.Credential;
import com.google.android.gms.auth.api.credentials.CredentialRequest;
import com.google.android.gms.auth.api.credentials.CredentialRequestResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.arthurthompson.smartlockcodelab.R;

public class SignInActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = "SignInActivity";
    private static final int RC_READ = 3;
    private static final int RC_SAVE = 1;
    private static final String IS_RESOLVING = "isResolving";

    private TextInputLayout mUsernameTextInputLayout;
    private TextInputLayout mPasswordTextInputLayout;

    // Add mGoogleApiClient and mIsResolving fields here.
    private GoogleApiClient mGoogleApiClient;
    private boolean mIsResolving;
    // private Credential mCredential;

    private String mUsername;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState != null) {
            mIsResolving = savedInstanceState.getBoolean(IS_RESOLVING);
        }

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .enableAutoManage(this, 0, this)
                .addApi(Auth.CREDENTIALS_API)
                .build();

        mUsernameTextInputLayout = (TextInputLayout) findViewById(R.id.usernameTextInputLayout);
        mPasswordTextInputLayout = (TextInputLayout) findViewById(R.id.passwordTextInputLayout);

        Button signInButton = (Button) findViewById(R.id.signInButton);
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
                    // Credential is valid so save it.
                    Auth.CredentialsApi.save(mGoogleApiClient,
                            credential).setResultCallback(new ResultCallback<Status>() {
                        @Override
                        public void onResult(Status status) {
                            if (status.isSuccess()) {
                                Log.d(TAG, "Credential saved");
                                goToContent();
                            } else {
                                Log.d(TAG, "Attempt to save credential failed " + status.getStatusMessage() + " " + status.getStatusCode() );
                                resolveResult(status, RC_SAVE);
                            }
                        }
                    });
                } else {
                    Log.d(TAG, "Credentials are invalid. Username or password are incorrect.");
                }
            }
        });

        Button clearButton = (Button) findViewById(R.id.clearButton);
        clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mUsernameTextInputLayout.getEditText().setText("");
                mPasswordTextInputLayout.getEditText().setText("");
            }
        });
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        // Save the user's current game state
        savedInstanceState.putBoolean(IS_RESOLVING, mIsResolving);

        // Always call the superclass so it can save the view hierarchy state
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.d(TAG, "onConnected");

        if (!mIsResolving) {
            // Request Credentials once connected. If credentials are retrieved the user will either be automatically
            // signed in or will be presented with credential options to be used by the application for sign in.
            requestCredentials();
        }
    }

    @Override
    public void onConnectionSuspended(int cause) {
        Log.d(TAG, "onConnectionSuspended: " + cause);
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d(TAG, "onConnectionFailed: " + connectionResult);
    }

    /**
     * Request Credentials from the Credentials API.
     */
    private void requestCredentials() {
        CredentialRequest request = new CredentialRequest.Builder()
                .setSupportsPasswordLogin(true)
                .build();

        Auth.CredentialsApi.request(mGoogleApiClient, request).setResultCallback(
                new ResultCallback<CredentialRequestResult>() {
                    @Override
                    public void onResult(CredentialRequestResult credentialRequestResult) {
                        if (credentialRequestResult.getStatus().isSuccess()) {
                            // Successfully read the credential without any user interaction, this
                            // means there was only a single credential and the user has auto
                            // sign-in enabled.
                            Credential credential = credentialRequestResult.getCredential();
                            processRetrievedCredential(credential);
                        } else {
                            Status status = credentialRequestResult.getStatus();
                            if (status.getStatusCode() == CommonStatusCodes.SIGN_IN_REQUIRED) {
                                // This is most likely the case where the user does not currently
                                // have any saved credentials and thus needs to provide a username
                                // and password to sign in.
                                Log.d(TAG, "Sign in required");
                            } else if (status.getStatusCode() == CommonStatusCodes.RESOLUTION_REQUIRED) {
                                // This is most likely the case where the user has multiple saved
                                // credentials and needs to pick one.
                                resolveResult(status, RC_READ);
                            } else {
                                Log.w(TAG, "Unexpected status code: " + status.getStatusCode());
                            }
                        }
                    }
                }
        );
    }

    /**
     * Attempt to resolve a non-successful Status from an asynchronous request.
     * @param status the Status to resolve.
     * @param requestCode the request code to use when starting an Activity for result,
     *                    this will be passed back to onActivityResult.
     */
    private void resolveResult(Status status, int requestCode) {
        // We don't want to fire multiple resolutions at once since that can result
        // in stacked dialogs after rotation or another similar event.
        if (mIsResolving) {
            Log.w(TAG, "resolveResult: already resolving.");
            return;
        }

        Log.d(TAG, "Resolving: " + status);
        if (status.hasResolution()) {
            Log.d(TAG, "STATUS: RESOLVING");
            try {
                status.startResolutionForResult(this, requestCode);
                mIsResolving = true;
            } catch (IntentSender.SendIntentException e) {
                Log.e(TAG, "STATUS: Failed to send resolution.", e);
            }
        } else {
            Log.e(TAG, "STATUS: FAIL");
            if (requestCode == RC_SAVE) {
                goToContent();
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult:" + requestCode + ":" + resultCode + ":" + data);

        switch (requestCode) {
            case RC_READ:
                if (resultCode == RESULT_OK) {
                    Credential credential = data.getParcelableExtra(Credential.EXTRA_KEY);
                    processRetrievedCredential(credential);
                } else {
                    Log.e(TAG, "Credential Read: NOT OK");
                }
                break;
            case RC_SAVE:
                Log.d(TAG, "Result code: " + resultCode);
                if (resultCode == RESULT_OK) {
                    Log.d(TAG, "Credential Save: OK");
                } else {
                    Log.e(TAG, "Credential Save Failed");
                }
                goToContent();
                break;
        }
        mIsResolving = false;
    }

    /**
     * Process a Credential object retrieved from a successful request.
     *  credential the Credential to process.
     */
    private void processRetrievedCredential(Credential credential) {
        if (CodelabUtil.isValidCredential(credential)) {
            goToContent();
        } else {
            // This is likely due to the credential being changed outside of Smart Lock,
            // ie: away from Android or Chrome. The credential should be deleted and the
            // user allowed to enter a valid credential.
            Log.d(TAG, "Retrieved credential invalid, so delete retrieved credential.");
            deleteCredential(credential);
        }
    }

    private void goToContent() {
        Intent intent = new Intent(this, ContentActivity.class);
        // intent.putExtra("username", mCredential.getId());
        intent.putExtra("username", mUsername);
        startActivity(intent);
        // finish();
    }

    /**
     * Delete the provided credential.
     *
     * @param credential Credential to be deleted.
     */
    private void deleteCredential(Credential credential) {
        Auth.CredentialsApi.delete(mGoogleApiClient, credential).setResultCallback(new ResultCallback<Status>() {
            @Override
            public void onResult(Status status) {
                if (status.isSuccess()) {
                    Log.d(TAG, "Credential successfully deleted.");
                } else {
                    // This may be due to the credential not existing, possibly already deleted via another device/app.
                    Log.d(TAG, "Credential not deleted successfully.");
                }
            }
        });
    }

}
