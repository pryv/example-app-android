package com.pryv.appAndroidExample;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.pryv.Connection;
import com.pryv.Filter;
import com.pryv.appAndroidExample.activities.LoginActivity;
import com.pryv.database.DBinitCallback;
import com.pryv.interfaces.EventsCallback;
import com.pryv.interfaces.GetEventsCallback;
import com.pryv.interfaces.StreamsCallback;
import com.pryv.model.Event;
import com.pryv.model.Stream;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Utility class that handles all communications with Pryv by:
 * initiating connection, setting up callbacks, creating/retrieving events/streams
 * and notifying UI through handlers
 */
public class AndroidConnection {
    private Connection connection;
    private Handler creationHandler;
    private Handler retrievalHandler;
    private EventsCallback eventsCallback;
    private GetEventsCallback getEventsCallback;
    private StreamsCallback streamsCallback;

    public AndroidConnection (String username, String token, Handler creationHandler, Handler retrievalHandler) {

        this.creationHandler = creationHandler;
        this.retrievalHandler = retrievalHandler;

        setCallbacks();

        // Initiate new connection to Pryv with connected account
        connection = new Connection(username, token, LoginActivity.DOMAIN, new DBinitCallback());
        connection.setupCacheScope(new Filter());
    }

    /**
     * Save a new event to Pryv
     * @param streamId: id of the stream containing the new event
     * @param type: type of the new event
     * @param content: content of the new event
     */
    public void saveEvent(String streamId, String type, String content) {
        Event event = new Event();
        event.setStreamId(streamId);
        event.setType(type);
        event.setContent(content);
        connection.events.create(new Event(streamId, null, type, content), eventsCallback);
    }

    /**
     * Save a new stream to Pryv
     * @param streamId: id of the new stream
     * @param streamName: name of the new stream
     */
    public Stream saveStream(String streamId, String streamName) {
        Stream stream = new Stream(streamId,streamName);
        connection.streams.create(stream, streamsCallback);
        return stream;
    }

    /**
     * Retrieve all events from Pryv according to some filter
     * @param stream: filter events by stream
     * @param type: filter events by type
     */
    public void retrieveEvents(Stream stream, String type) {
        // TODO: DO NOT LIKE PASSING STREAM FROM MAINACTIVITY, GETSTREAMBYID?
        Filter filter = new Filter();
        filter.addStream(stream);
        connection.events.get(filter, getEventsCallback);
    }

    /**
     * Notify MainActivity to update its progress dialog
     * @param notification: the notification message
     */
    private void notifyUI(String notification) {
        Bundle b = new Bundle();
        b.putString("notification", notification);
        Message msg = new Message();
        msg.setData(b);
        creationHandler.sendMessage(msg);
    }

    /**
     * Notify the MainActivity to update its events list
     * @param events: the list of events retrieved
     */
    private void notifyUI(List<Event> events) {
        Bundle b = new Bundle();
        ArrayList<String> retrievedEvents = new ArrayList<>();
        for(Event e: events) {
            retrievedEvents.add((String)e.getContent());
        }
        b.putStringArrayList("events", retrievedEvents);
        Message msg = new Message();
        msg.setData(b);
        retrievalHandler.sendMessage(msg);
    }

    /**
     * Initiate custom callbacks
     */
    private void setCallbacks() {

        //Called when action related to events creation/modification complete
        eventsCallback = new EventsCallback() {

            @Override
            public void onApiSuccess(String s, Event event, String s1, Double aDouble) {
                notifyUI(s);
            }

            @Override
            public void onApiError(String s, Double aDouble) {
                notifyUI(s);
            }

            @Override
            public void onCacheSuccess(String s, Event event) {
                notifyUI(s);
            }

            @Override
            public void onCacheError(String s) {
                notifyUI(s);
            }
        };

        //Called when action related to streams complete
        streamsCallback = new StreamsCallback() {

            @Override
            public void onApiSuccess(String s, Stream stream, Double aDouble) {
                Log.d("Pryv",s);
            }

            @Override
            public void onApiError(String s, Double aDouble) {
                Log.d("Pryv", s);
            }

            @Override
            public void onCacheSuccess(String s, Stream stream) {
                Log.d("Pryv", s);
            }

            @Override
            public void onCacheError(String s) {
                Log.d("Pryv", s);
            }

        };

        //Called when action related to events retrieval
        getEventsCallback = new GetEventsCallback() {
            @Override
            public void cacheCallback(List<Event> list, Map<String, Double> map) {
                notifyUI(list);
            }

            @Override
            public void onCacheError(String s) {
                notifyUI(s);
            }

            @Override
            public void apiCallback(List<Event> list, Map<String, Double> map, Double aDouble) {
                notifyUI(list);
            }

            @Override
            public void onApiError(String s, Double aDouble) {
                notifyUI(s);
            }
        };

    }

}