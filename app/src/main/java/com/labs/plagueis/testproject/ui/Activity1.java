package com.labs.plagueis.testproject.ui;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.labs.plagueis.testproject.R;
import com.labs.plagueis.testproject.ui.widget.DrawShadowFrameLayout;

import static com.labs.plagueis.testproject.util.LogUtils.makeLogTag;

/**
 * Created by plagueis on 1/03/15.
 */
public class Activity1 extends BaseActivity{

    private static final String TAG = makeLogTag(Activity1.class);

    private int mMode = 0;

    private DrawShadowFrameLayout mDrawShadowFrameLayout;
    private View mButterBar;
    private Button mBtnTest;


    // time when the user last clicked "refresh" from the stale data butter bar
    private long mLastDataStaleUserActionTime = 0L;
    private int mHeaderColor = 0; // 0 means not customized

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_1);

        Toolbar toolbar = getActionBarToolbar();

        mButterBar = findViewById(R.id.butter_bar);
        mDrawShadowFrameLayout = (DrawShadowFrameLayout) findViewById(R.id.main_content);
        mBtnTest = (Button) findViewById(R.id.btn_test);

        mBtnTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(Activity1.this,"I told you, don't press the f**k**g button",Toast.LENGTH_SHORT).show();
            }
        });

        registerHideableHeaderView(mButterBar);
    }

    @Override
    public boolean canSwipeRefreshChildScrollUp() {
        return super.canSwipeRefreshChildScrollUp();
    }

    @Override
    protected int getSelfNavDrawerItem() {
        // we only have a nav drawer if we are in top-level Explore mode.
        return mMode == 0 ? NAVDRAWER_ITEM_ACTIVITY_1 : NAVDRAWER_ITEM_INVALID;
    }

}
