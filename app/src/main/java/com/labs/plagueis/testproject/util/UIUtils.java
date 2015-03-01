package com.labs.plagueis.testproject.util;

import android.os.Build;
import android.view.View;

import static com.labs.plagueis.testproject.util.LogUtils.makeLogTag;

/**
 * Created by plagueis on 1/03/15.
 */
public class UIUtils {

    private static final String TAG = makeLogTag(UIUtils.class);

    public static void setAccessibilityIgnore(View view) {
        view.setClickable(false);
        view.setFocusable(false);
        view.setContentDescription("");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            view.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
        }
    }
}
