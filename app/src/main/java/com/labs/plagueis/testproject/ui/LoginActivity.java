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
import android.util.Log;
import android.view.View;

import com.google.android.gms.auth.GoogleAuthUtil;
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
        SharedPreferences.OnSharedPreferenceChangeListener{

    private static final String TAG = makeLogTag(LoginActivity.class);

    // the LoginAndAuthHelper handles signing in to Google Play Services and OAuth
    private LoginAndAuthHelper mLoginAndAuthHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        PrefUtils.init(this);

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        sp.registerOnSharedPreferenceChangeListener(this);

        setContentView(R.layout.login_layout);

        findViewById(R.id.button_accept).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PrefUtils.markUserSignedIn(LoginActivity.this);
                Intent intent = new Intent(LoginActivity.this, Activity1.class);
                startActivity(intent);
                finish();
            }
        });

        findViewById(R.id.button_decline).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    /**
     * Returns the default account on the device. We use the rule that the first account
     * should be the default. It's arbitrary, but the alternative would be showing an account
     * chooser popup which wouldn't be a smooth first experience with the app. Since the user
     * can easily switch the account with the nav drawer, we opted for this implementation.
     */
    private String getDefaultAccount() {
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
        return accounts[0].name;
    }

    private void complainMustHaveGoogleAccount() {
        LOGD(TAG, "Complaining about missing Google account.");
        /*new AlertDialog.Builder(this)
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
                .show();*/
    }

    private void startLoginProcess() {
        LOGD(TAG, "Starting login process.");
        if (!AccountUtils.hasActiveAccount(this)) {
            LOGD(TAG, "No active account, attempting to pick a default.");
            String defaultAccount = getDefaultAccount();
            if (defaultAccount == null) {
                LOGE(TAG, "Failed to pick default account (no accounts). Failing.");
                complainMustHaveGoogleAccount();
                return;
            }
            LOGD(TAG, "Default to: " + defaultAccount);
            AccountUtils.setActiveAccount(this, defaultAccount);
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
}
