package com.pryv.appAndroidExample;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.pryv.Connection;
import com.pryv.Pryv;
import com.pryv.api.EventsCallback;
import com.pryv.api.Filter;
import com.pryv.api.StreamsCallback;
import com.pryv.api.database.DBinitCallback;
import com.pryv.api.model.Event;
import com.pryv.api.model.Stream;

import java.util.ArrayList;
import java.util.Map;

/**
 * Created by Thieb on 26.02.2016.
 */
public class AndroidConnection {
    private static Connection connection;
    private Handler creationHandler;
    private Handler retrievalHandler;
    private EventsCallback eventsCallback;
    private StreamsCallback streamsCallback;

    public AndroidConnection (Handler creationHandler, Handler retrievalHandler) {
        Pryv.deactivateCache();
        Pryv.deactivateSupervisor();

        this.creationHandler = creationHandler;
        this.retrievalHandler = retrievalHandler;
        setCallbacks();

        // Initiate new connection to Pryv with connected account
        connection = new Connection(LoginActivity.getUsername(), LoginActivity.getToken(), new DBinitCallback(){});
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
        connection.createEvent(event, eventsCallback);
    }

    /**
     * Save a new stream to Pryv
     * @param streamId: id of the new stream
     * @param streamName: name of the new stream
     */
    public void saveStream(String streamId, String streamName) {
        Stream stream = new Stream();
        stream.setId(streamId);
        stream.setName(streamName);
        connection.createStream(stream, streamsCallback);
    }

    /**
     * Retrieve all events from Pryv according to some filter
     * @param streamId: filter events by stream id
     * @param type: filter events by type
     */
    public void retrieveEvents(String streamId, String type) {
        Filter filter = new Filter();
        filter.addStreamId(streamId);
        filter.addType(type);
        connection.getEvents(filter, eventsCallback);
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
     * @param events: the list of events retrieveds
     */
    private void notifyUI(Map<String, Event> events) {
        Bundle b = new Bundle();
        ArrayList<String> retrievedEvents = new ArrayList<>();
        retrievedEvents.addAll(events.keySet());
        b.putStringArrayList("events", retrievedEvents);
        Message msg = new Message();
        msg.setData(b);
        retrievalHandler.sendMessage(msg);
    }

    /**
     * Initiate custom callbacks
     */
    private void setCallbacks() {

        //Called when action related to events complete
        eventsCallback = new EventsCallback() {

            @Override
            public void onEventsRetrievalSuccess(Map<String, Event> events, Double serverTime) {
                Log.d("Pryv", "onEventsRetrievalSuccess");
                notifyUI(events);
            }

            @Override
            public void onEventsRetrievalError(String errorMessage, Double serverTime) {
                Log.d("Pryv", "onEventsRetrievalError");
                notifyUI(errorMessage);
            }

            @Override
            public void onEventsSuccess(String successMessage, Event event, Integer stoppedId, Double serverTime) {
                Log.d("Pryv", successMessage);
                notifyUI("New event created: id=" + event.getId() + ", streamID=" + event.getStreamId() + ", type=" + event.getType() + ", content=" + event.getContent());
            }

            @Override
            public void onEventsError(String errorMessage, Double serverTime) {
                Log.d("Pryv", "onEventsError");
                notifyUI(errorMessage);
            }
        };

        //Called when action related to streams complete
        streamsCallback = new StreamsCallback() {

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

}