package com.boha.monitor.staffapp.activities;

import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Created by aubreyM on 15/12/03.
 */
@RunWith(AndroidJUnit4.class)
public class StaffMainActivityTest {

    StaffMainActivity mainActivity;
    @Rule
    public ActivityTestRule<StaffMainActivity> mActivityRule = new ActivityTestRule(StaffMainActivity.class);

    @Before
    public void setUp() throws Exception {
        mainActivity = mActivityRule.getActivity();
    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void testFields() throws Exception {
        if (mainActivity.projectListFragment != null) {
            Log.e(TAG, "testFields: projectListFragment not null");
        }
        Log.d(TAG, "testFields: ");
    }

    private static final String TAG = "StaffMainActivityTest";
}