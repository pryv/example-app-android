package com.pryv.appAndroidExample;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private TextView progressView;
    private ListView notesList;
    private EditText noteText;

    private AndroidConnection connection;

    private static final String NOTE_STREAM_ID = "sampleNotes";
    private static final String NOTE_STREAM_NAME = "Notes";
    private static final String NOTE_EVENT_TYPE = "note/txt";

    private Handler noteCreationHandler;
    private Handler noteRetrievalHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setHandlers();

        progressView = (TextView) findViewById(R.id.progress);
        noteText = (EditText) findViewById(R.id.note);
        notesList = (ListView) findViewById(R.id.notes_list);

        // Initiate the connection to Pryv, providing handlers which will update UI
        connection = new AndroidConnection(noteCreationHandler, noteRetrievalHandler);

        // Initiate a "Notes" stream containing notes if not already created
        connection.saveStream(NOTE_STREAM_ID, NOTE_STREAM_NAME);
    }

    @Override
    /**
     * Override back button action with logout alert dialog
     * If click yes, disconnect the user by resetting credentials,
     * empty activities stack and starting login activity
     */
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setTitle("Log out")
                .setMessage("You are about to log out. Continue?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        LoginActivity.resetCredentials();
                        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                    }
                })
                .setNegativeButton("No", null)
                .show();
    }

    /**
     * Called when "Create a new note" button is clicked
     * Ask for the creation of a new note
     */
    public void addNote(View v) {
        String text = noteText.getText().toString();
        if(!text.isEmpty()) {
            if(text.length()>20) {
                progressView.setText("Please make your note shorter (20 characters max)!");
            } else {
                noteText.setText("");
                connection.saveEvent(NOTE_STREAM_ID,NOTE_EVENT_TYPE,text);
            }
        } else {
            progressView.setText("Your note must contain some text!");
        }
    }

    /**
     * Called when "Retrieve notes" button is clicked
     * Ask for the retrieval of all stored notes
     */
    public void retrieveNotes(View v) {
        connection.retrieveEvents(NOTE_STREAM_ID, NOTE_EVENT_TYPE);
    }

    private void setHandlers() {
        /**
         * Handler updating the progress dialog with success/error message when event creation is finish
         */
        noteCreationHandler = new Handler() {
            public void handleMessage(Message msg) {
                Bundle b = msg.getData();
                progressView.setText(b.getString("notification"));
            }
        };

        /**
         * Handler updating the notes list when events retrieval is finish
         */
        noteRetrievalHandler = new Handler() {
            public void handleMessage(Message msg) {
                Bundle b = msg.getData();
                ArrayList<String> retrievedEvents = b.getStringArrayList("events");
                ArrayAdapter<String> adapter = new ArrayAdapter(MainActivity.this, R.layout.list_item, retrievedEvents);
                notesList.setAdapter(adapter);
                progressView.setText("All notes retrieved!");
            }
        };
    }

}