package com.pryv.appAndroidExample;

import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.LargeTest;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Created by Thieb on 30.03.2016.
 */
@RunWith(AndroidJUnit4.class)
public class LoginTest {
    private String userName;
    private String validPassword;
    private String invalidPassword;

    @Rule
    public ActivityTestRule<LoginActivity> mActivityRule = new ActivityTestRule<>(LoginActivity.class);

    @Before
    public void initCredentials() {
        userName = "tmodoux";
        validPassword = "pryv2016";
        invalidPassword = "password";
    }

    @Test
    public void invalidLogin() {

    }

    @Test
    public void validLogin() {

    }
}
