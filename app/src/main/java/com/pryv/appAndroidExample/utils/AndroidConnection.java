package com.pryv.appAndroidExample.utils;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.pryv.Connection;
import com.pryv.Pryv;
import com.pryv.api.EventsCallback;
import com.pryv.api.Filter;
import com.pryv.api.StreamsCallback;
import com.pryv.api.database.DBinitCallback;
import com.pryv.api.model.Event;
import com.pryv.api.model.Stream;
import com.pryv.appAndroidExample.R;
import com.pryv.appAndroidExample.activities.LoginActivity;

import java.util.ArrayList;
import java.util.Map;

/**
 * Created by Thieb on 26.02.2016.
 */
public class AndroidConnection {
    private static Connection connection;
    private TextView progressView;
    private String currentMessage = "";
    private ArrayList <String> retrievedEvents;
    private Context context;
    private ListView eventsList;
    private Handler handler;

    public AndroidConnection (TextView progressView, ListView eventsList, Context context, Handler handler) {
        Pryv.deactivateCache();
        Pryv.deactivateSupervisor();
        this.handler = handler;
        connection = new Connection(LoginActivity.getUsername(), LoginActivity.getToken(), new DBinitCallback(){});
        this.progressView = progressView;
        this.progressView.setText("Connection to Pryv initialized!");
        this.context = context;
        this.eventsList = eventsList;
    }

    public void saveEvent(String streamId, String type, String content) {
        Event event = new Event();
        event.setStreamId(streamId);
        event.setType(type);
        event.setContent(content);
        connection.createEvent(event, new EventsCallback() {
            @Override
            public void onEventsRetrievalSuccess(Map<String, Event> map, Double aDouble) {

            }

            @Override
            public void onEventsRetrievalError(String s, Double aDouble) {

            }

            @Override
            public void onEventsSuccess(String s, Event event, Integer integer, Double aDouble) {
                Bundle b = new Bundle();
                b.putString("event", "id=" + event.getId() + ", streamID=" + event.getStreamId() + ", type=" + event.getType() + ", content=" + event.getContent() + "");
                Message msg = new Message();
                msg.setData(b);
                handler.sendMessage(msg);
            }

            @Override
            public void onEventsError(String s, Double aDouble) {

            }
        });
        //new SaveEventAsync().execute(event);
    }

    public void saveStream(String streamId, String streamName) {
        Stream stream = new Stream();
        stream.setId(streamId);
        stream.setName(streamName);
        connection.createStream(stream,streamsCallback);
    }

    public void retrieveEvents(String streamId, String type) {
        Filter filter = new Filter();
        filter.addStreamId(streamId);
        filter.addType(type);
        connection.getEvents(filter,eventsCallback);
    }

    private EventsCallback eventsCallback = new EventsCallback() {

            @Override
            public void onEventsRetrievalSuccess(Map<String, Event> events, Double serverTime) {
                Log.d("Pryv", "onEventsRetrievalSuccess");
                for(String e: events.keySet()) {
                    retrievedEvents.add(e);
                }
            }

            @Override
            public void onEventsRetrievalError(String errorMessage, Double serverTime) {
                Log.d("Pryv", "onEventsRetrievalError");
                currentMessage = errorMessage;
            }

            @Override
            public void onEventsSuccess(String successMessage, Event event, Integer stoppedId, Double serverTime) {
                Intent intent = new Intent("my-event");
                intent.putExtra("message", "data");
                LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
            }

            @Override
            public void onEventsError(String errorMessage, Double serverTime) {
                Log.d("Pryv", "onEventsError");
                currentMessage = errorMessage;
            }
    };

    private StreamsCallback streamsCallback = new StreamsCallback() {

            @Override
            public void onStreamsSuccess(String successMessage, Stream stream, Double serverTime) {
                Log.d("Pryv", "onStreamsSuccess");
            }

            @Override
            public void onStreamsRetrievalSuccess(Map<String, Stream> streams, Double serverTime) {
                Log.d("Pryv", "onStreamsRetrievalSuccess");
            }

            @Override
            public void onStreamsRetrievalError(String errorMessage, Double serverTime) {
                Log.d("Pryv", "onStreamsRetrievalError");
            }

            @Override
            public void onStreamError(String errorMessage, Double serverTime) {
                Log.d("Pryv", "onStreamError");
            }
    };

}