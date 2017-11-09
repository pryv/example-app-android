package com.pryv.appAndroidExample;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.pryv.Connection;
import com.pryv.model.Event;
import com.pryv.model.Filter;
import com.pryv.model.Stream;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
            final String text = noteText.getText().toString();
            if(!text.isEmpty()) {
                if(text.length()>20) {
                    progressView.setText(TOO_LONG_ERROR);
                } else {
                    noteText.setText("");
                    new Thread() {
                        public void run() {
                            try {
                                Event newEvent = new Event()
                                        .setStreamId(noteStream.getId())
                                        .setType(NOTE_EVENT_TYPE)
                                        .setContent(text);
                                newEvent = connection.events.create(newEvent);
                                updateStatusText("New event created with id: " + newEvent.getId());
                            } catch (IOException e) {
                                updateStatusText(e.toString());
                            }
                        }
                    }.start();
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
            new Thread() {
                public void run() {
                    try {
                        Filter filter = new Filter().addStream(noteStream);
                        List<Event> retrievedEvents = connection.events.get(filter);
                        updateEventsList(retrievedEvents);
                    } catch (IOException e) {
                        updateStatusText(e.toString());
                    }
                }
            }.start();
        } else {
            startLogin();
        }
    }

    private void startLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivityForResult(intent, LOGIN_REQUEST);
    }

    private void initPryvConnection() {
        // Initiate new connection to Pryv with connected account
        connection = new Connection(credentials.getUsername(), credentials.getToken(), LoginActivity.DOMAIN);
        // Initiate a "Notes" stream containing notes if not already created
        noteStream = new Stream()
            .setId(NOTE_STREAM_ID)
            .setName(NOTE_STREAM_NAME);

        new Thread() {
            public void run() {
                try {
                    Stream createdStream = connection.streams.create(noteStream);
                    updateStatusText("Stream created with id: " + createdStream.getId());
                } catch (IOException e) {
                    updateStatusText(e.toString());
                }
            }
        }.start();
    }

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

    private void updateEventsList(final List<Event> events) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ArrayList<String> eventsContent = new ArrayList<>();
                for(Event e: events) {
                    eventsContent.add(""+e.getContent());
                }
                retrievedEvents.clear();
                retrievedEvents.addAll(eventsContent);
                adapter.notifyDataSetChanged();
                progressView.setText(NOTES_RETRIEVED_MESSAGE);
            }
        });
    }

    private void updateStatusText(final String text) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progressView.setText(text);
            }
        });
    }

}