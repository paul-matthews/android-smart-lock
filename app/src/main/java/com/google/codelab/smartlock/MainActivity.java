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
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.credentials.Credential;
import com.google.android.gms.auth.api.credentials.CredentialRequest;
import com.google.android.gms.auth.api.credentials.CredentialRequestResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = "MainActivity";
    private static final int RC_SAVE = 1;
    private static final int RC_READ = 3;
    private static final String IS_RESOLVING = "isResolving";
    private static final String SPLASH_TAG = "splash_fragment";
    private static final String SIGN_IN_TAG = "sign_in_fragment";
    public static final int DELAY_MILLIS = 3000;

    // Add mGoogleApiClient and mIsResolving fields here.
    private GoogleApiClient mGoogleApiClient;
    private boolean mIsResolving;
    private boolean mIsRequesting;
    private Fragment mCurrentFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (isFirstRun()) {
            setSplashState();
        } else {
            setSignInPromptState();
        }

        if (savedInstanceState != null) {
            mIsResolving = savedInstanceState.getBoolean(IS_RESOLVING);
        }

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .enableAutoManage(this, 0, this)
                .addApi(Auth.CREDENTIALS_API)
                .build();
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
        Log.d(TAG, "GoogleApiClient is connected.");
        // Request Credentials once connected. If credentials are retrieved the user will either be automatically
        // signed in or will be presented with credential options to be used by the application for sign in.

        // Check if currently resolving so additional modal dialogs are not launched.
        if (!mIsResolving) {
            // For the purposes of this code lab an artificial delay is introduced if the splash fragment is shown.
            // If the splash fragment is not being shown then credentials are immediately requested.
            if (isFirstRun()) {
                setSplashState();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        requestCredentials();
                    }
                }, DELAY_MILLIS);
            } else {
                setSignInDisabledState();
                requestCredentials();
            }
        }
    }

    @Override
    public void onConnectionSuspended(int cause) {
        Log.d(TAG, "GoogleApiClient is suspended with cause code: " + cause);
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d(TAG, "GoogleApiClient failed to connect: " + connectionResult);
    }

    /**
     * Request Credentials from the Credentials API.
     */
    private void requestCredentials() {
        mIsRequesting = true;

        CredentialRequest request = new CredentialRequest.Builder()
                .setSupportsPasswordLogin(true)
                .build();

        Auth.CredentialsApi.request(mGoogleApiClient, request).setResultCallback(
                new ResultCallback<CredentialRequestResult>() {
                    @Override
                    public void onResult(CredentialRequestResult credentialRequestResult) {
                        mIsRequesting = false;
                        Status status = credentialRequestResult.getStatus();
                        if (credentialRequestResult.getStatus().isSuccess()) {
                            // Successfully read the credential without any user interaction, this
                            // means there was only a single credential and the user has auto
                            // sign-in enabled.
                            Credential credential = credentialRequestResult.getCredential();
                            processRetrievedCredential(credential);
                        } else if (status.getStatusCode() == CommonStatusCodes.RESOLUTION_REQUIRED) {
                            setSignInPromptState();
                            // This is most likely the case where the user has multiple saved
                            // credentials and needs to pick one.
                            resolveResult(status, RC_READ);
                        } else if (status.getStatusCode() == CommonStatusCodes.SIGN_IN_REQUIRED) {
                            setSignInPromptState();
                            // This is most likely the case where the user does not currently
                            // have any saved credentials and thus needs to provide a username
                            // and password to sign in.
                            Log.d(TAG, "Sign in required");
                        } else {
                            Log.w(TAG, "Unrecognized status code: " + status.getStatusCode());
                            setSignInPromptState();
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
            goToContent();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult:" + requestCode + ":" + resultCode + ":" + data);

        if (requestCode == RC_READ) {
            if (resultCode == RESULT_OK) {
                Credential credential = data.getParcelableExtra(Credential.EXTRA_KEY);
                processRetrievedCredential(credential);
            } else {
                Log.e(TAG, "Credential Read: NOT OK");
                setSignInPromptState();
            }
        } else if (requestCode == RC_SAVE) {
            goToContent();
        }
        mIsResolving = false;
    }

    /**
     * Process a Credential object retrieved from a successful request.
     *  credential the Credential to process.
     */
    private void processRetrievedCredential(Credential credential) {
        if (CodelabUtil.isValidCredential(credential)) {
            CodelabUtil.setUser(credential);
            goToContent();
        } else {
            // This is likely due to the credential being changed outside of Smart Lock,
            // ie: away from Android or Chrome. The credential should be deleted and the
            // user allowed to enter a valid credential.
            Log.d(TAG, "Retrieved credential invalid, so delete retrieved credential.");
            Toast.makeText(this, "Retrieved credentials are invalid, so will be deleted.", Toast.LENGTH_LONG).show();
            deleteCredential(credential);
            requestCredentials();
            setSignInDisabledState();
        }
    }

    /**
     * Save valid credential, the validity of the credential is checked before this method is called.
     *
     * @param credential Credential to be saved, this is assumed to be a valid credential.
     */
    protected void saveCredential(final Credential credential) {
        // Credential is valid so save it.
        Auth.CredentialsApi.save(mGoogleApiClient, credential).setResultCallback(new ResultCallback<Status>() {
            @Override
            public void onResult(Status status) {
                if (status.isSuccess()) {
                    Log.d(TAG, "Credential saved");
                    CodelabUtil.setUser(credential);
                    goToContent();
                } else {
                    Log.d(TAG, "Attempt to save credential failed " + status.getStatusMessage() + " " + status.getStatusCode());
                    resolveResult(status, RC_SAVE);
                }
            }
        });
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

    private void setSplashState() {
        if (mCurrentFragment instanceof SplashFragment) {
            Log.d(TAG, "Splash already displaying.");
            return;
        }
        Log.d(TAG, "Set or replace Splash Fragment.");
        setOrReplaceFragment(new SplashFragment());
    }

    private void setSignInPromptState() {
        if (mCurrentFragment instanceof SignInFragment) {
            Log.d(TAG, "Sign-In already displaying - enabled.");
            ((SignInFragment) mCurrentFragment).setSignEnabled(true);
            return;
        }
        Log.d(TAG, "Set or replace Sign-In Fragment - enabled.");
        SignInFragment siFragment = new SignInFragment();
        siFragment.setSignEnabled(true);
        setOrReplaceFragment(siFragment);
    }

    private void setSignInDisabledState() {
        if (mCurrentFragment instanceof SignInFragment) {
            Log.d(TAG, "Sign-In already displaying - setting disabled.");
            ((SignInFragment) mCurrentFragment).setSignEnabled(false);
            return;
        }
        Log.d(TAG, "Set or replace Sign-In Fragment - disabled.");
        SignInFragment siFragment = new SignInFragment();
        siFragment.setSignEnabled(false);
        setOrReplaceFragment(siFragment);
    }

    private void setOrReplaceFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        if (mCurrentFragment != null) {
            Log.d(TAG, "Fragment already set.");
            transaction.replace(R.id.fragment_container, fragment);
        } else {
            Log.d(TAG, "Fragment not set.");
            transaction.add(R.id.fragment_container, fragment);
        }
        mCurrentFragment = fragment;
        transaction.commit();
    }

    /**
     * Start the Content Activity and finish this one.
     */
    protected void goToContent() {
        startActivity(new Intent(this, ContentActivity.class));
        finish();
    }

    /**
     * Check if the Splash Fragment is the currently selected Fragment.
     *
     * @return true if Splash Fragment is the current Fragment, false otherwise.
     */
    private boolean inSplashState() {
        return mCurrentFragment instanceof SplashFragment;
    }

    private boolean isFirstRun() {
        return getIntent().hasCategory(Intent.CATEGORY_LAUNCHER);
    }

    protected boolean isResolving() {
        return mIsResolving;
    }

    protected boolean isRequesting() {
        return mIsRequesting;
    }
}
