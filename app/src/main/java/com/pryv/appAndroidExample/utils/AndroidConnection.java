package com.pryv.appAndroidExample.utils;

import android.content.Context;
import android.os.AsyncTask;
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
import com.pryv.appAndroidExample.activities.MainActivity;

import java.util.List;
import java.util.Map;

/**
 * Created by Thieb on 26.02.2016.
 */
public class AndroidConnection {
    private static Connection connection;
    private TextView progressView;
    private Event eventToSave;
    private Stream streamToSave;
    private String streamMessage = "";
    private String eventMessage = "";
    private Filter filter;
    private Map<String, Event> retrievedEvents;
    private Context context;
    private ListView eventsList;

    public AndroidConnection (TextView progressView, ListView eventsList, Context context) {
        Pryv.deactivateCache();
        Pryv.deactivateSupervisor();
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
        eventToSave = event;
        new SaveEventAsync().execute();
    }

    public void saveStream(String streamId, String streamName) {
        if(!connection.getRootStreams().containsKey(streamId)) {
            Stream stream = new Stream();
            stream.setId(streamId);
            stream.setName(streamName);
            streamToSave = stream;
            new SaveStreamAsync().execute();
        }
    }

    public void getEvents(String streamId, String type) {
        retrievedEvents = null;
        filter = new Filter();
        filter.addStreamId(streamId);
        filter.addType(type);
        new RetrieveEventAsync().execute();
    }

    private class RetrieveEventAsync extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... arg0) {
            connection.getEvents(filter, eventsCallback);
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            if(retrievedEvents!=null) {
                String[] events = retrievedEvents.values().toArray(new String[retrievedEvents.values().size()]);
                ArrayAdapter<String> adapter = new ArrayAdapter(context, R.layout.list_item, events);
                eventsList.setAdapter(adapter);
            } else {
                progressView.setText("No event to retrieve!");
            }
        }

    }

    private class SaveEventAsync extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... arg0) {
            connection.createEvent(eventToSave, eventsCallback);
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            progressView.setText(eventMessage);
        }

    }

    private class SaveStreamAsync extends AsyncTask<Event, Void, Void> {

        @Override
        protected Void doInBackground(Event... events) {
            connection.createStream(streamToSave, streamsCallback);
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            progressView.setText(streamMessage);
        }

    }

    private EventsCallback eventsCallback = new EventsCallback() {

            @Override
            public void onEventsRetrievalSuccess(Map<String, Event> events, Double serverTime) {
                Log.d("Pryv", "onEventsRetrievalSuccess");
                eventMessage = "New events retrieved!";
                retrievedEvents = events;
            }

            @Override
            public void onEventsRetrievalError(String errorMessage, Double serverTime) {
                Log.d("Pryv", "onEventsRetrievalError");
                eventMessage = errorMessage;
            }

            @Override
            public void onEventsSuccess(String successMessage, Event event, Integer stoppedId, Double serverTime) {
                Log.d("Pryv", "onEventsSuccess");
                eventMessage = successMessage;
                getEvents(event.getStreamId(),event.getType());
            }

            @Override
            public void onEventsError(String errorMessage, Double serverTime) {
                Log.d("Pryv", "onEventsError");
                eventMessage = errorMessage;
            }
    };

    private StreamsCallback streamsCallback = new StreamsCallback() {

            @Override
            public void onStreamsSuccess(String successMessage, Stream stream, Double serverTime) {
                Log.d("Pryv", "onStreamsSuccess");
                streamMessage = successMessage;
            }

            @Override
            public void onStreamsRetrievalSuccess(Map<String, Stream> streams, Double serverTime) {
                Log.d("Pryv", "onStreamsRetrievalSuccess");
                streamMessage = "Stream retrieved!";
            }

            @Override
            public void onStreamsRetrievalError(String errorMessage, Double serverTime) {
                Log.d("Pryv", "onStreamsRetrievalError");
                streamMessage = errorMessage;
            }

            @Override
            public void onStreamError(String errorMessage, Double serverTime) {
                Log.d("Pryv", "onStreamError");
                streamMessage = errorMessage;
            }
    };
}