package com.labs.plagueis.testproject.ui;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.labs.plagueis.testproject.R;
import com.labs.plagueis.testproject.ui.widget.DrawShadowFrameLayout;

import static com.labs.plagueis.testproject.util.LogUtils.makeLogTag;

/**
 * Created by plagueis on 1/03/15.
 */
public class Activity2 extends BaseActivity {

    private static final String TAG = makeLogTag(Activity2.class);

    private int mMode = 0;

    private DrawShadowFrameLayout mDrawShadowFrameLayout;
    private View mButterBar;


    // time when the user last clicked "refresh" from the stale data butter bar
    private long mLastDataStaleUserActionTime = 0L;
    private int mHeaderColor = 0; // 0 means not customized

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_2);

        Toolbar toolbar = getActionBarToolbar();

        mButterBar = findViewById(R.id.butter_bar);
        mDrawShadowFrameLayout = (DrawShadowFrameLayout) findViewById(R.id.main_content);
        registerHideableHeaderView(mButterBar);
    }

    @Override
    public boolean canSwipeRefreshChildScrollUp() {
        return super.canSwipeRefreshChildScrollUp();
    }

    @Override
    protected int getSelfNavDrawerItem() {
        // we only have a nav drawer if we are in top-level Explore mode.
        return mMode == 0 ? NAVDRAWER_ITEM_ACTIVITY_2 : NAVDRAWER_ITEM_INVALID;
    }

}
