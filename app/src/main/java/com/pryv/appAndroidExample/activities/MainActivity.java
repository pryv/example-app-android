package com.pryv.appAndroidExample.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.pryv.appAndroidExample.R;
import com.pryv.appAndroidExample.utils.AndroidConnection;

public class MainActivity extends AppCompatActivity {

    private TextView progressView;
    private EditText noteText;
    private AndroidConnection connection;
    private static final String NOTE_STREAM_ID = "sampleNotes";
    private static final String NOTE_STREAM_NAME = "Notes";
    private static final String NOTE_EVENT_TYPE = "note/txt";
    private ListView notesList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        progressView = (TextView) findViewById(R.id.progress);
        noteText = (EditText) findViewById(R.id.note);
        notesList = (ListView) findViewById(R.id.notes_list);

        connection = new AndroidConnection(progressView, notesList, this);
        connection.saveStream(NOTE_STREAM_ID, NOTE_STREAM_NAME);
        connection.retrieveEvents(NOTE_STREAM_ID, NOTE_EVENT_TYPE);
    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setTitle("Log out")
                .setMessage("You are about to log out. Continue?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        LoginActivity.resetCreditentials();
                        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                    }
                })
                .setNegativeButton("No", null)
                .show();
    }

    public void addNote(View v) {
        String text = noteText.getText().toString();
        if(!text.isEmpty()) {
            if(text.length()>20) {
                progressView.setText("Please make your note shorter (20 characters max)!");
            } else {
                noteText.setText("");
                connection.saveEvent(NOTE_STREAM_ID, NOTE_EVENT_TYPE, text);
            }
        } else {
            progressView.setText("Your note must contain some text!");
        }
    }

}