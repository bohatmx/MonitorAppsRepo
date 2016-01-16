package com.boha.monitor.staffapp.activities;

import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class SignInActivityTest {

    private static final String TAG = "SignInActivityTest";
    SignInActivity mActivity;
    public SignInActivityTest() {
    }
    @Rule
    public ActivityTestRule<SignInActivity> mActivityRule = new ActivityTestRule(SignInActivity.class);

    @Before
    public void setUp() throws Exception {

        mActivity = mActivityRule.getActivity();
        Log.d(TAG, "setUp: " + mActivity);
    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void testOnCreate() throws Exception {

    }

    @Test
    public void testRegisterGCMDevice() throws Exception {

    }

    @Test
    public void testSendSignIn() throws Exception {

    }

    @Test
    public void testSetFields() throws Exception {
//        onView(withId(R.id.SI_pin))
//                .check(matches(withText("")));

//        onView(withId(R.id.SI_txtEmail))
//                .check(matches(withText("Select email account")))
//                .perform(click());

        //onView(withId(R.id.btnRed))
        //        .check(matches(withText("Sign In")));
    }

    @Test
    public void testGetEmail() throws Exception {

    }
}