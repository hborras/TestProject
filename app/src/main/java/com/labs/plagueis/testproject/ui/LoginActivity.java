package com.labs.plagueis.testproject.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.util.Log;
import android.view.View;

import com.labs.plagueis.testproject.R;
import com.labs.plagueis.testproject.util.PrefUtils;


/**
 * Created by plagueis on 28/02/15.
 */
public class LoginActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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
}
