package com.pryv.appAndroidExample;

import android.support.test.espresso.intent.rule.IntentsTestRule;
import android.support.test.espresso.web.webdriver.DriverAtoms;
import android.support.test.espresso.web.webdriver.Locator;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.widget.ListView;
import android.widget.TextView;

import com.jayway.awaitility.Awaitility;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.Callable;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
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
public class MainTest {
    private TextView notification;
    private ListView notes;

    @Rule
    public ActivityTestRule<MainActivity> mainActivityRule = new ActivityTestRule(MainActivity.class);

    @Before
    public void initViews() {
        new Credentials(mainActivityRule.getActivity().getApplicationContext()).setCredentials("apptest", "apptest");
        notification = (TextView) mainActivityRule.getActivity().findViewById(R.id.progress);
        notes = (ListView) mainActivityRule.getActivity().findViewById(R.id.notesList);
    }

    @Test
    public void addNoteAndRetrieve() {
        onView(withId(R.id.retrieveNote)).perform(click());
        Awaitility.await().until(isShowingMessage(mainActivityRule.getActivity().NOTES_RETRIEVED_MESSAGE));
        int n = notes.getAdapter().getCount();
        onView(withId(R.id.note))
                .perform(typeText("This is a test"), closeSoftKeyboard());
        onView(withId(R.id.addNote)).perform(click());
        Awaitility.await().until(isShowingMessage("event created"));
        onView(withId(R.id.retrieveNote)).perform(click());
        Awaitility.await().until(isShowingMessage(mainActivityRule.getActivity().NOTES_RETRIEVED_MESSAGE));
        assertTrue(notes.getAdapter().getCount() == n+1);
    }

    @Test
    public void addVoidNote() {
        onView(withId(R.id.addNote)).perform(click());
        Awaitility.await().until(isShowingMessage(mainActivityRule.getActivity().TOO_SHORT_ERROR));
    }

    @Test
    public void addBigNote() {
        onView(withId(R.id.note))
                .perform(typeText("This is a big big big test"), closeSoftKeyboard());
        onView(withId(R.id.addNote)).perform(click());
        Awaitility.await().until(isShowingMessage(mainActivityRule.getActivity().TOO_LONG_ERROR));
    }

    private Callable<Boolean> isShowingMessage(final String message) {
        return new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                return (notification.getText().toString().contains(message));
            }
        };
    }

}
