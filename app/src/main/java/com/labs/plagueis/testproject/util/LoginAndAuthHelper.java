package com.labs.plagueis.testproject.util;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;

import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.GooglePlayServicesAvailabilityException;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.plus.People;

import static com.labs.plagueis.testproject.util.LogUtils.LOGD;
import static com.labs.plagueis.testproject.util.LogUtils.LOGE;
import static com.labs.plagueis.testproject.util.LogUtils.LOGW;
import static com.labs.plagueis.testproject.util.LogUtils.makeLogTag;

import java.io.IOException;
import java.lang.ref.WeakReference;

/**
 * Created by plagueis on 25/02/15.
 */
public class LoginAndAuthHelper implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, ResultCallback<People.LoadPeopleResult> {

    // Request codes for the UIs that we show
    private static final int REQUEST_AUTHENTICATE = 100;
    private static final int REQUEST_RECOVER_FROM_AUTH_ERROR = 101;
    private static final int REQUEST_RECOVER_FROM_PLAY_SERVICES_ERROR = 102;
    private static final int REQUEST_PLAY_SERVICES_ERROR_DIALOG = 103;

    // Auth scopes we need
    public static final String AUTH_SCOPES[] = {
            Scopes.PLUS_LOGIN,
            Scopes.DRIVE_APPFOLDER,
            "https://www.googleapis.com/auth/plus.profile.emails.read"};

    static final String AUTH_TOKEN_TYPE;

    static {
        StringBuilder sb = new StringBuilder();
        sb.append("oauth2:");
        for (String scope : AUTH_SCOPES) {
            sb.append(scope);
            sb.append(" ");
        }
        AUTH_TOKEN_TYPE = sb.toString();
    }

    private static final String TAG = makeLogTag(LoginAndAuthHelper.class);

    Context mAppContext;

    // Controls whether or not we can show sign-in UI. Starts as true;
    // when sign-in *fails*, we will show the UI only once and set this flag to false.
    // After that, we don't attempt again in order not to annoy the user.
    private static boolean sCanShowSignInUi = true;
    private static boolean sCanShowAuthUi = true;

    // The Activity this object is bound to (we use a weak ref to avoid context leaks)
    WeakReference<Activity> mActivityRef;

    // Callbacks interface we invoke to notify the user of this class of useful events
    WeakReference<Callbacks> mCallbacksRef;

    // Name of the account to log in as (e.g. "foo@example.com")
    String mAccountName;

    // API client to interact with Google services
    private GoogleApiClient mGoogleApiClient;

    // Async task that fetches the token
    GetTokenTask mTokenTask = null;

    // Are we in the started state? Started state is between onStart and onStop.
    boolean mStarted = false;

    // True if we are currently showing UIs to resolve a connection error.
    boolean mResolving = false;


    public interface Callbacks {
        void onPlusInfoLoaded(String accountName);
        void onAuthSuccess(String accountName, boolean newlyAuthenticated);
        void onAuthFailure(String accountName);
    }

    @Override
    public void onConnected(Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    @Override
    public void onResult(People.LoadPeopleResult loadPeopleResult) {

    }

    private void reportAuthSuccess(boolean newlyAuthenticated) {
        LOGD(TAG, "Auth success for account " + mAccountName + ", newlyAuthenticated=" + newlyAuthenticated);
        Callbacks callbacks;
        if (null != (callbacks = mCallbacksRef.get())) {
            callbacks.onAuthSuccess(mAccountName, newlyAuthenticated);
        }
    }

    private void reportAuthFailure() {
        LOGD(TAG, "Auth FAILURE for account " + mAccountName);
        Callbacks callbacks;
        if (null != (callbacks = mCallbacksRef.get())) {
            callbacks.onAuthFailure(mAccountName);
        }
    }

    private Activity getActivity(String methodName) {
        Activity activity = mActivityRef.get();
        if (activity == null) {
            LOGD(TAG, "Helper lost Activity reference, ignoring (" + methodName + ")");
        }
        return activity;
    }

    private void showRecoveryDialog(int statusCode) {
        Activity activity = getActivity("showRecoveryDialog()");
        if (activity == null) {
            return;
        }

        if (sCanShowAuthUi) {
            sCanShowAuthUi = false;
            LOGD(TAG, "Showing recovery dialog for status code " + statusCode);
            final Dialog d = GooglePlayServicesUtil.getErrorDialog(
                    statusCode, activity, REQUEST_RECOVER_FROM_PLAY_SERVICES_ERROR);
            d.show();
        } else {
            LOGD(TAG, "Not showing Play Services recovery dialog because sCanShowSignInUi==false.");
            reportAuthFailure();
        }
    }

    private void showAuthRecoveryFlow(Intent intent) {
        Activity activity = getActivity("showAuthRecoveryFlow()");
        if (activity == null) {
            return;
        }

        if (sCanShowAuthUi) {
            sCanShowAuthUi = false;
            LOGD(TAG, "Starting auth recovery Intent.");
            activity.startActivityForResult(intent, REQUEST_RECOVER_FROM_AUTH_ERROR);
        } else {
            LOGD(TAG, "Not showing auth recovery flow because sCanShowSignInUi==false.");
            reportAuthFailure();
        }
    }

    /** Async task that obtains the auth token. */
    private class GetTokenTask extends AsyncTask<Void, Void, String> {
        public GetTokenTask() {}

        @Override
        protected String doInBackground(Void... params) {
            try {
                if (isCancelled()) {
                    LOGD(TAG, "doInBackground: task cancelled, so giving up on auth.");
                    return null;
                }

                LOGD(TAG, "Starting background auth for " + mAccountName);
                final String token = GoogleAuthUtil.getToken(mAppContext, mAccountName, AUTH_TOKEN_TYPE);

                // Save auth token.
                LOGD(TAG, "Saving token: " + (token == null ? "(null)" : "(length " +
                        token.length() + ")") + " for account "  + mAccountName);
                AccountUtils.setAuthToken(mAppContext, mAccountName, token);
                return token;
            } catch (GooglePlayServicesAvailabilityException e) {
                postShowRecoveryDialog(e.getConnectionStatusCode());
            } catch (UserRecoverableAuthException e) {
                postShowAuthRecoveryFlow(e.getIntent());
            } catch (IOException e) {
                LOGE(TAG, "IOException encountered: " + e.getMessage());
            } catch (GoogleAuthException e) {
                LOGE(TAG, "GoogleAuthException encountered: " + e.getMessage());
            } catch (RuntimeException e) {
                LOGE(TAG, "RuntimeException encountered: " + e.getMessage());
            }
            return null;
        }

        @Override
        protected void onPostExecute(String token) {
            super.onPostExecute(token);

            if (isCancelled()) {
                LOGD(TAG, "Task cancelled, so not reporting auth success.");
            } else if (!mStarted) {
                LOGD(TAG, "Activity not started, so not reporting auth success.");
            } else {
                LOGD(TAG, "GetTokenTask reporting auth success.");
                reportAuthSuccess(true);
            }
        }

        private void postShowRecoveryDialog(final int statusCode) {
            Activity activity = getActivity("postShowRecoveryDialog()");
            if (activity == null) {
                return;
            }

            if (isCancelled()) {
                LOGD(TAG, "Task cancelled, so not showing recovery dialog.");
                return;
            }

            LOGD(TAG, "Requesting display of recovery dialog for status code " + statusCode);
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (mStarted) {
                        showRecoveryDialog(statusCode);
                    } else {
                        LOGE(TAG, "Activity not started, so not showing recovery dialog.");
                    }
                }
            });
        }

        private void postShowAuthRecoveryFlow(final Intent intent) {
            Activity activity = getActivity("postShowAuthRecoveryFlow()");
            if (activity == null) {
                return;
            }

            if (isCancelled()) {
                LOGD(TAG, "Task cancelled, so not showing auth recovery flow.");
                return;
            }

            LOGD(TAG, "Requesting display of auth recovery flow.");
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (mStarted) {
                        showAuthRecoveryFlow(intent);
                    } else {
                        LOGE(TAG, "Activity not started, so not showing auth recovery flow.");
                    }
                }
            });
        }
    }
}
