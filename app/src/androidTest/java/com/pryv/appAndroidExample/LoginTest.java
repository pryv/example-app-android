package com.pryv.appAndroidExample;

import android.content.ComponentName;
import android.support.test.espresso.intent.rule.IntentsTestRule;
import android.support.test.espresso.web.webdriver.DriverAtoms;
import android.support.test.espresso.web.webdriver.Locator;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.jayway.awaitility.Awaitility;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.Callable;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static android.support.test.espresso.intent.Intents.intended;
import static android.support.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static android.support.test.espresso.web.sugar.Web.onWebView;
import static android.support.test.espresso.web.webdriver.DriverAtoms.clearElement;
import static android.support.test.espresso.web.webdriver.DriverAtoms.findElement;
import static android.support.test.espresso.web.webdriver.DriverAtoms.webClick;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

/**
 * Created by Thieb on 30.03.2016.
 */
@RunWith(AndroidJUnit4.class)
public class LoginTest {
    private String userName;
    private String validPassword;
    private String invalidPassword;
    private Credentials credentials;

    @Rule
    public ActivityTestRule<LoginActivity> loginActivityRule = new ActivityTestRule(LoginActivity.class);

    @Before
    public void initCredentials() {
        userName = "apptest";
        invalidPassword = "password";
        validPassword = "apptest";
        credentials = new Credentials(loginActivityRule.getActivity().getApplicationContext());
    }

    @Test
    public void invalidLogin() {
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

        assertTrue(credentials.getToken() == null);
        assertTrue(credentials.getUsername() == null);

    }

    @Test
    public void validLogin() {
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
                .perform(DriverAtoms.webKeys(validPassword))
                        // Find the submit button
                .withElement(findElement(Locator.ID, "login-form-loginButton"))
                        // Simulate a click via javascript
                .perform(webClick());

        Awaitility.await().until(hasCredentials());
        assertFalse(credentials.getToken() == null);
        assertFalse(credentials.getUsername() == null);
        assertTrue(credentials.getUsername().equals(userName));
    }

    private Callable<Boolean> hasCredentials() {
        return new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                return (credentials.getUsername() != null && credentials.getToken() != null);
            }
        };
    }

}
