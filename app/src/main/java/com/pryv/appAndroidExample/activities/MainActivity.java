package com.pryv.appAndroidExample.activities;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.pryv.Connection;
import com.pryv.Pryv;
import com.pryv.api.EventsCallback;
import com.pryv.api.database.DBinitCallback;
import com.pryv.api.model.Event;
import com.pryv.appAndroidExample.R;
import com.pryv.appAndroidExample.utils.AndroidConnection;

import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private TextView progressView;
    private EditText noteText;
    private AndroidConnection connection;
    private static final String NOTE_STREAM_ID = "sampleNotes";
    private static final String NOTE_STREAM_NAME = "Notes";
    private static final String NOTE_EVENT_TYPE = "note/txt";
    private ListView notesList;

    private Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setHandler();

        progressView = (TextView) findViewById(R.id.progress);
        noteText = (EditText) findViewById(R.id.note);
        notesList = (ListView) findViewById(R.id.notes_list);

        connection = new AndroidConnection(progressView, notesList, this, handler);
        connection.saveStream(NOTE_STREAM_ID, NOTE_STREAM_NAME);
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
                LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver, new IntentFilter("my-event"));
                noteText.setText("");
                connection.saveEvent(NOTE_STREAM_ID,NOTE_EVENT_TYPE,text);
            }
        } else {
            progressView.setText("Your note must contain some text!");
        }
    }

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String message = intent.getStringExtra("message");
            Toast.makeText(MainActivity.this,message,Toast.LENGTH_SHORT).show();
        }
    };

    public void retrieveNotes(View v) {
        connection.retrieveEvents(NOTE_STREAM_ID, NOTE_EVENT_TYPE);
    }

    private void setHandler() {
        handler = new Handler() {

            public void handleMessage(Message msg) {
                Bundle b = msg.getData();
                noteText.setText("myEvent:" + b.get("event"));
            }
        };
    }
    
    private void updateList() {
        //ArrayAdapter<String> adapter = new ArrayAdapter(context, R.layout.list_item, retrievedEvents);
        //eventsList.setAdapter(adapter);
    }
}