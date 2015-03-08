package com.labs.plagueis.testproject.ui;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.common.SignInButton;
import com.labs.plagueis.testproject.R;
import com.labs.plagueis.testproject.util.AccountUtils;
import com.labs.plagueis.testproject.util.LoginAndAuthHelper;
import com.labs.plagueis.testproject.util.PrefUtils;

import static com.labs.plagueis.testproject.util.LogUtils.makeLogTag;
import static com.labs.plagueis.testproject.util.LogUtils.LOGD;
import static com.labs.plagueis.testproject.util.LogUtils.LOGI;
import static com.labs.plagueis.testproject.util.LogUtils.LOGE;
import static com.labs.plagueis.testproject.util.LogUtils.LOGW;


/**
 * Created by plagueis on 28/02/15.
 */
public class LoginActivity extends Activity implements LoginAndAuthHelper.Callbacks,
        SharedPreferences.OnSharedPreferenceChangeListener, View.OnClickListener{

    private static final String TAG = makeLogTag(LoginActivity.class);

    // the LoginAndAuthHelper handles signing in to Google Play Services and OAuth
    private LoginAndAuthHelper mLoginAndAuthHelper;

    private SignInButton btnSignIn;
    private Button btnSignOut, btnRevokeAccess;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        PrefUtils.init(this);

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        sp.registerOnSharedPreferenceChangeListener(this);

        setContentView(R.layout.login_layout);

        btnSignIn = (SignInButton) findViewById(R.id.btn_sign_in);
        btnSignOut = (Button) findViewById(R.id.btn_sign_out);
        btnRevokeAccess = (Button) findViewById(R.id.btn_revoke_access);

        btnSignIn.setOnClickListener(this);
        btnSignOut.setOnClickListener(this);
        btnRevokeAccess.setOnClickListener(this);
    }

    /**
     * Returns the default account on the device. We use the rule that the first account
     * should be the default. It's arbitrary, but the alternative would be showing an account
     * chooser popup which wouldn't be a smooth first experience with the app. Since the user
     * can easily switch the account with the nav drawer, we opted for this implementation.
     */
    private Account[] getDefaultAccount() {
        // Choose first account on device.
        LOGD(TAG, "Choosing default account (first account on device)");
        AccountManager am = AccountManager.get(this);
        Account[] accounts = am.getAccountsByType(GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE);
        if (accounts.length == 0) {
            // No Google accounts on device.
            LOGW(TAG, "No Google accounts on device; not setting default account.");
            return null;
        }

        LOGD(TAG, "Default account is: " + accounts[0].name);
        return accounts;
    }

    private void complainMustHaveGoogleAccount() {
        LOGD(TAG, "Complaining about missing Google account.");
        new AlertDialog.Builder(this)
                .setTitle(R.string.google_account_required_title)
                .setMessage(R.string.google_account_required_message)
                .setPositiveButton(R.string.add_account, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        promptAddAccount();
                    }
                })
                .setNegativeButton(R.string.not_now, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                })
                .show();
    }

    private void promptAddAccount() {
        Intent intent = new Intent(Settings.ACTION_ADD_ACCOUNT);
        intent.putExtra(Settings.EXTRA_ACCOUNT_TYPES, new String[]{"com.google"});
        startActivity(intent);
        finish();
    }

    private void startLoginProcess() {
        LOGD(TAG, "Starting login process.");
        if (!AccountUtils.hasActiveAccount(this)) {
            LOGD(TAG, "No active account, attempting to pick one in selector.");
            Account[] defaultAccount = getDefaultAccount();
            if (defaultAccount == null) {
                LOGE(TAG, "Failed to pick default account (no accounts). Failing.");
                complainMustHaveGoogleAccount();
                return;
            }
            LOGD(TAG, "Creating and starting new Helper with no account");
            mLoginAndAuthHelper = new LoginAndAuthHelper(this, this, "");
            mLoginAndAuthHelper.start();

            /*LOGD(TAG, "Default to: " + defaultAccount);
            AccountUtils.setActiveAccount(this, defaultAccount);*/
        }

        if (!AccountUtils.hasActiveAccount(this)) {
            LOGD(TAG, "Can't proceed with login -- no account chosen.");
            return;
        } else {
            LOGD(TAG, "Chosen account: " + AccountUtils.getActiveAccountName(this));
        }

        String accountName = AccountUtils.getActiveAccountName(this);
        LOGD(TAG, "Chosen account: " + AccountUtils.getActiveAccountName(this));

        if (mLoginAndAuthHelper != null && mLoginAndAuthHelper.getAccountName().equals(accountName)) {
            LOGD(TAG, "Helper already set up; simply starting it.");
            mLoginAndAuthHelper.start();
            return;
        }

        LOGD(TAG, "Starting login process with account " + accountName);

        if (mLoginAndAuthHelper != null) {
            LOGD(TAG, "Tearing down old Helper, was " + mLoginAndAuthHelper.getAccountName());
            if (mLoginAndAuthHelper.isStarted()) {
                LOGD(TAG, "Stopping old Helper");
                mLoginAndAuthHelper.stop();
            }
            mLoginAndAuthHelper = null;
        }

        LOGD(TAG, "Creating and starting new Helper with account: " + accountName);
        mLoginAndAuthHelper = new LoginAndAuthHelper(this, this, accountName);
        mLoginAndAuthHelper.start();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (mLoginAndAuthHelper == null || !mLoginAndAuthHelper.onActivityResult(requestCode,
                resultCode, data)) {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onStop() {
        LOGD(TAG, "onStop");
        super.onStop();
        if (mLoginAndAuthHelper != null) {
            mLoginAndAuthHelper.stop();
        }
    }

    @Override
    public void onPlusInfoLoaded(String accountName) {
        LOGI(TAG,"PLus info loaded with account name: " + accountName);
        PrefUtils.markUserSignedIn(LoginActivity.this);
        Intent intent = new Intent(LoginActivity.this, Activity1.class);
        startActivity(intent);
        finish();
        //updateUI(true);
    }

    @Override
    public void onAuthSuccess(String accountName, boolean newlyAuthenticated) {
        LOGI(TAG,"Auth Success with account name" + accountName);
    }

    @Override
    public void onAuthFailure(String accountName) {
        LOGI(TAG,"Auth failure with account name: " + accountName);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_sign_in:
                // Signin button clicked
                signInWithGplus();
                break;
            case R.id.btn_sign_out:
                // Signout button clicked
                signOutFromGplus();
                break;
            case R.id.btn_revoke_access:
                // Revoke access button clicked
                revokeGplusAccess();
        }
    }

    /**
     * Updating the UI, showing/hiding buttons and profile layout
     * */
    private void updateUI(boolean isSignedIn) {
        if (isSignedIn) {
            btnSignIn.setVisibility(View.GONE);
            btnSignOut.setVisibility(View.VISIBLE);
            btnRevokeAccess.setVisibility(View.VISIBLE);
        } else {
            btnSignIn.setVisibility(View.VISIBLE);
            btnSignOut.setVisibility(View.GONE);
            btnRevokeAccess.setVisibility(View.GONE);
        }
    }

    /**
     * Sign-in into google
     * */
    private void signInWithGplus() {
        startLoginProcess();
    }

    /**
     * Sign-out from google
     * */
    private void signOutFromGplus() {
        mLoginAndAuthHelper.stop();
        updateUI(false);
        /*if (mGoogleApiClient.isConnected()) {
            Plus.AccountApi.clearDefaultAccount(mGoogleApiClient);
            mGoogleApiClient.disconnect();
            mGoogleApiClient.connect();

        }*/
    }

    /**
     * Revoking access from google
     * */
    private void revokeGplusAccess() {
        mLoginAndAuthHelper.revokeGplusAccess();
        updateUI(false);
    }
}
