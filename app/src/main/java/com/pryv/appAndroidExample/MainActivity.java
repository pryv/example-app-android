package com.pryv.appAndroidExample;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.pryv.Connection;
import com.pryv.Filter;
import com.pryv.database.DBinitCallback;
import com.pryv.interfaces.EventsCallback;
import com.pryv.interfaces.GetEventsCallback;
import com.pryv.interfaces.GetStreamsCallback;
import com.pryv.interfaces.StreamsCallback;
import com.pryv.model.Event;
import com.pryv.model.Stream;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Main screen allowing to create note events to your Pryv
 * and retrieve all of them as a list of events
 */
public class MainActivity extends AppCompatActivity {

    private TextView progressView;
    private ListView notesList;
    private EditText noteText;
    private Button login;

    private Credentials credentials;
    private static final int LOGIN_REQUEST = 1;

    private static final String NOTE_STREAM_ID = "sampleNotes";
    private static final String NOTE_STREAM_NAME = "Notes";
    private static final String NOTE_EVENT_TYPE = "note/txt";
    private Stream noteStream;

    public static final String TOO_LONG_ERROR = "Please make your note shorter (20 characters max)!";
    public static final String TOO_SHORT_ERROR = "Your note must contain some text!";
    public static final String NOTES_RETRIEVED_MESSAGE = "All notes retrieved!";

    private BaseAdapter adapter;
    private ArrayList<String> retrievedEvents;

    private Connection connection;
    private EventsCallback eventsCallback;
    private GetEventsCallback getEventsCallback;
    private StreamsCallback streamsCallback;
    private GetStreamsCallback getStreamsCallback;
    private UINotifier notifier;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        progressView = (TextView) findViewById(R.id.progress);
        noteText = (EditText) findViewById(R.id.note);
        notesList = (ListView) findViewById(R.id.notesList);
        login = (Button) findViewById(R.id.login);

        retrievedEvents = new ArrayList<>();
        adapter = new ArrayAdapter(this, R.layout.list_item, retrievedEvents);
        notesList.setAdapter(adapter);

        credentials = new Credentials(this);
        if(credentials.hasCredentials()) {
            initPryvConnection();
            setLogoutView();
        } else {
            setLoginView();
        }
    }

    /**
     * Called when "Create a new note" button is clicked
     * Ask for the creation of a new note
     */
    public void addNote(View v) {
        if(credentials.hasCredentials()) {
            String text = noteText.getText().toString();
            if(!text.isEmpty()) {
                if(text.length()>20) {
                    progressView.setText(TOO_LONG_ERROR);
                } else {
                    noteText.setText("");
                    connection.events.create(new Event(noteStream.getId(), null, NOTE_EVENT_TYPE, text), eventsCallback);
                }
            } else {
                progressView.setText(TOO_SHORT_ERROR);
            }
        } else {
            startLogin();
        }
    }

    /**
     * Called when "Retrieve notes" button is clicked
     * Ask for the retrieval of all stored notes
     */
    public void retrieveNotes(View v) {
        if(credentials.hasCredentials()) {
            Filter filter = new Filter();
            filter.addStream(noteStream);
            connection.events.get(filter, getEventsCallback);
        } else {
            startLogin();
        }
    }

    private void startLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivityForResult(intent, LOGIN_REQUEST);
    }

    private void initPryvConnection() {
        setCallbacks();
        // Initiate new connection to Pryv with connected account
        connection = new Connection(this, credentials.getUsername(), credentials.getToken(), LoginActivity.DOMAIN, true, new DBinitCallback());
        notifier = new UINotifier(notificationHandler);
        // Initiate a "Notes" stream containing notes if not already created
        noteStream = new Stream(NOTE_STREAM_ID, NOTE_STREAM_NAME);
        connection.streams.create(noteStream, streamsCallback);
    }

    private final Handler notificationHandler = new Handler() {
        public void handleMessage(Message msg) {
            Bundle b = msg.getData();

            switch(b.getInt("type")) {
                case UINotifier.NOTIFICATION_TYPE_MESSAGE:
                    progressView.setText(b.getString("content"));
                    break;
                case UINotifier.NOTIFICATION_TYPE_EVENTS:
                    retrievedEvents.clear();
                    retrievedEvents.addAll(b.getStringArrayList("content"));
                    adapter.notifyDataSetChanged();
                    progressView.setText(NOTES_RETRIEVED_MESSAGE);
                    break;
            }
        }
    };

    private void setLoginView() {
        progressView.setText("Hello guest!");
        retrievedEvents.clear();
        adapter.notifyDataSetChanged();
        login.setText("Login");
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startLogin();
            }
        });
    }

    private void setLogoutView() {
        progressView.setText("Hello " + credentials.getUsername() + "!");
        login.setText("Logout");
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                credentials.resetCredentials();
                setLoginView();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == LOGIN_REQUEST && resultCode == RESULT_OK) {
            setLogoutView();
            initPryvConnection();
        }
    }

    /**
     * Initiate custom callbacks
     */
    private void setCallbacks() {

        //Called when actions related to events creation/modification complete
        eventsCallback = new EventsCallback() {

            @Override
            public void onApiSuccess(String s, Event event, String s1, Double aDouble) {
                notifier.notify(s);
                Log.i("Pryv", s);
            }

            @Override
            public void onApiError(String s, Double aDouble) {
                notifier.notify(s);
                Log.e("Pryv", s);
            }

            @Override
            public void onCacheSuccess(String s, Event event) {
                notifier.notify(s);
                Log.i("Pryv", s);
            }

            @Override
            public void onCacheError(String s) {
                notifier.notify(s);
                Log.e("Pryv", s);
            }
        };

        //Called when actions related to streams creation/modification complete
        streamsCallback = new StreamsCallback() {

            @Override
            public void onApiSuccess(String s, Stream stream, Double aDouble) {
                Log.i("Pryv", s);
            }

            @Override
            public void onApiError(String s, Double aDouble) {
                Log.e("Pryv", s);
            }

            @Override
            public void onCacheSuccess(String s, Stream stream) {
                Log.i("Pryv", s);
            }

            @Override
            public void onCacheError(String s) {
                Log.e("Pryv", s);
            }

        };

        //Called when actions related to events retrieval complete
        getEventsCallback = new GetEventsCallback() {
            @Override
            public void cacheCallback(List<Event> list, Map<String, Double> map) {
                notifier.notify(list);
                Log.i("Pryv", list.size() + " events retrieved from cache.");
            }

            @Override
            public void onCacheError(String s) {
                notifier.notify(s);
                Log.e("Pryv", s);
            }

            @Override
            public void apiCallback(List<Event> list, Map<String, Double> map, Double aDouble) {
                notifier.notify(list);
                Log.i("Pryv", list.size() + " events retrieved from API.");
            }

            @Override
            public void onApiError(String s, Double aDouble) {
                notifier.notify(s);
                Log.e("Pryv", s);
            }
        };

        //Called when actions related to streams retrieval complete
        getStreamsCallback = new GetStreamsCallback() {

            @Override
            public void cacheCallback(Map<String, Stream> map, Map<String, Double> map1) {
                Log.i("Pryv", map.size() + " streams retrieved from cache.");
            }

            @Override
            public void onCacheError(String s) {
                Log.e("Pryv", s);
            }

            @Override
            public void apiCallback(Map<String, Stream> map, Map<String, Double> map1, Double aDouble) {
                Log.i("Pryv", map.size() + " streams retrieved from API.");
            }

            @Override
            public void onApiError(String s, Double aDouble) {
                Log.e("Pryv", s);
            }
        };

    }

}