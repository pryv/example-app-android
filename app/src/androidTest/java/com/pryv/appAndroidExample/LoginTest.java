package com.pryv.appAndroidExample;

import android.content.Intent;
import android.support.test.espresso.web.webdriver.DriverAtoms;
import android.support.test.espresso.web.webdriver.Locator;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.web.assertion.WebViewAssertions.webMatches;
import static android.support.test.espresso.web.sugar.Web.onWebView;
import static android.support.test.espresso.web.webdriver.DriverAtoms.clearElement;
import static android.support.test.espresso.web.webdriver.DriverAtoms.findElement;
import static android.support.test.espresso.web.webdriver.DriverAtoms.getText;
import static android.support.test.espresso.web.webdriver.DriverAtoms.webClick;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Created by Thieb on 30.03.2016.
 */
@RunWith(AndroidJUnit4.class)
public class LoginTest {
    private String userName;
    private String validPassword;
    private String invalidPassword;

    @Rule
    public ActivityTestRule<LoginActivity> mActivityRule = new ActivityTestRule<LoginActivity>(LoginActivity.class, false, false) {
        @Override
        protected void afterActivityLaunched() {
            // Enable JS!
            onWebView().forceJavascriptEnabled();
        }
    };

    @Before
    public void initCredentials() {
        userName = "tmodoux";
        invalidPassword = "password";
    }

    @Test
    public void invalidLogin() {
        // Lazily launch the Activity with a custom start Intent per test
        mActivityRule.launchActivity(new Intent());

        onWebView()
                // Find the input element by ID for username
                .withElement(findElement(Locator.ID, "loginUsernameOrEmail"))
                        // Clear previous input
                .perform(clearElement())
                        // Enter text into the input element
                .perform(DriverAtoms.webKeys(userName))
                        // Same for password
                .withElement(findElement(Locator.ID, "loginPassword"))
                        // Clear previous input
                .perform(clearElement())
                        // Enter text into the input element
                .perform(DriverAtoms.webKeys(invalidPassword))
                        // Find the submit button
                .withElement(findElement(Locator.ID, "login-form-loginButton"))
                        // Simulate a click via javascript
                .perform(webClick());

        assertTrue(mActivityRule.getActivity().getUsername() == null);
        assertTrue(mActivityRule.getActivity().getToken() == null);

    }

}
