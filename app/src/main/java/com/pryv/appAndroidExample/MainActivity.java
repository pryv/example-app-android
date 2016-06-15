package com.pryv.appAndroidExample;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.pryv.model.Stream;

import java.util.ArrayList;

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

    private SQLiteDBHelper db;

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

        db = new SQLiteDBHelper(this);
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
                    AndroidConnection.getSharedInstance().createEvent(noteStream.getId(), NOTE_EVENT_TYPE, text);
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
            AndroidConnection.getSharedInstance().getEvents(noteStream);
        } else {
            startLogin();
        }
    }

    private void startLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivityForResult(intent, LOGIN_REQUEST);
    }

    private void initPryvConnection() {
        // Initiate the connection to Pryv, providing handler which will update UI
        AndroidConnection.getSharedInstance().setConnection(credentials.getUsername(), credentials.getToken(), this);
        AndroidConnection.getSharedInstance().setNotifications(notificationHandler);
        // Initiate a "Notes" stream containing notes if not already created
        noteStream = AndroidConnection.getSharedInstance().createStream(NOTE_STREAM_ID, NOTE_STREAM_NAME);
    }

    private final Handler notificationHandler = new Handler() {
        public void handleMessage(Message msg) {
            Bundle b = msg.getData();

            switch(b.getInt("type")) {
                case AndroidConnection.NOTIFICATION_TYPE_MESSAGE:
                    progressView.setText(b.getString("content"));
                    break;
                case AndroidConnection.NOTIFICATION_TYPE_EVENTS:
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

}