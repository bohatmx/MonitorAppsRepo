package com.boha.monitor.operations;

import android.test.ActivityInstrumentationTestCase2;
import android.test.suitebuilder.annotation.SmallTest;
import android.util.Log;

import java.util.List;

/**
 * Created by aubreyM on 15/01/27.
 */
public class RegistrationActivityTest extends ActivityInstrumentationTestCase2<RegistrationActivity> {
    /**
     * Creates an {@link android.test.ActivityInstrumentationTestCase2}.
     *
     * @param activityClass The activity to test. This must be a class in the instrumentation
     *                      targetPackage specified in the AndroidManifest.xml
     */
    public RegistrationActivityTest(Class<RegistrationActivity> activityClass) {
        super(activityClass);
        Log.w(LOG,"### RegistrationActivityTest constructor OK");
    }
    RegistrationActivity activity;
    @SmallTest
    public void testEmailAccountListFilled() {
        Log.d(LOG,"##### testEmailAccountListFilled");
        List<String> list = activity.emailAccountList;
        assertNotNull(list);
    }
    @Override
    protected void setUp() throws Exception {
        activity = getActivity();
        Log.i(LOG,"#### setUp OK...");
    }
    @Override
    protected void tearDown() throws Exception {

    }

    static final String LOG = RegistrationActivityTest.class.getSimpleName();
}
