package com.pryv.appAndroidExample;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

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
 * Utility class that handles all communications with Pryv by:
 * initiating connection, setting up callbacks, creating/retrieving events/streams
 * and notifying UI through handlers
 */
public class AndroidConnection {
    private Connection connection;
    private Handler notificationHandler;
    private EventsCallback eventsCallback;
    private GetEventsCallback getEventsCallback;
    private StreamsCallback streamsCallback;
    private GetStreamsCallback getStreamsCallback;

    public static AndroidConnection singleton;

    public final static int NOTIFICATION_TYPE_MESSAGE = 1;
    public final static int NOTIFICATION_TYPE_EVENTS = 2;

    public static AndroidConnection getSharedInstance() {
        if(singleton==null) {
            singleton = new AndroidConnection();
        }
        return singleton;
    }

    public void setConnection (String username, String token) {
        setCallbacks();
        // Initiate new connection to Pryv with connected account
        connection = new Connection(username, token, LoginActivity.DOMAIN, false, new DBinitCallback());
    }

    public void setNotifications(Handler handler) {
        notificationHandler = handler;
    }

    /**
     * Create a new event on Pryv
     * @param streamId: id of the stream containing the new event
     * @param type: type of the new event
     * @param content: content of the new event
     */
    public void createEvent(String streamId, String type, String content) {
        if(checkLogin()) {
            connection.events.create(new Event(streamId, null, type, content), eventsCallback);
        }
    }

    /**
     * Create a new stream on Pryv
     * @param streamId: id of the new stream
     * @param streamName: name of the new stream
     */
    public Stream createStream(String streamId, String streamName) {
        Stream stream = null;
        if(checkLogin()) {
            stream = new Stream(streamId,streamName);
            connection.streams.create(stream, streamsCallback);
        }
        return stream;
    }

    /**
     * Get all events from Pryv according to some filter
     * @param stream: filter events by stream
     */
    public void getEvents(Stream stream) {
        if(checkLogin()) {
            Filter filter = new Filter();
            filter.addStream(stream);
            connection.events.get(filter, getEventsCallback);
        }
    }

    /**
     * Get all streams from Pryv according to some filter
     * @param parent: filter streams by stream parent
     */
    public void getStreams(Stream parent) {
        if(checkLogin()) {
            Filter filter = new Filter();
            filter.addStream(parent);
            connection.streams.get(filter, getStreamsCallback);
        }
    }

    private boolean checkLogin() {
        if(connection == null) {
            notifyUI("Need login!");
            Log.e("Pryv", "Need login!");
        }
        return (connection != null);
    }

    /**
     * Notify MainActivity to update its progress dialog
     * @param notification: the notification message
     */
    private void notifyUI(String notification) {
        if(notificationHandler!=null) {
            Bundle b = new Bundle();
            b.putInt("type", NOTIFICATION_TYPE_MESSAGE);
            b.putString("content", notification);
            Message msg = new Message();
            msg.setData(b);
            notificationHandler.sendMessage(msg);
        }
    }

    /**
     * Notify the MainActivity to update its events list
     * @param events: the list of events retrieved
     */
    private void notifyUI(List<Event> events) {
        if(notificationHandler!=null) {
            Bundle b = new Bundle();
            b.putInt("type", NOTIFICATION_TYPE_EVENTS);
            ArrayList<String> retrievedEvents = new ArrayList<>();
            for(Event e: events) {
                retrievedEvents.add((String)e.getContent());
            }
            b.putStringArrayList("content", retrievedEvents);
            Message msg = new Message();
            msg.setData(b);
            notificationHandler.sendMessage(msg);
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
                notifyUI(s);
                Log.i("Pryv", s);
            }

            @Override
            public void onApiError(String s, Double aDouble) {
                notifyUI(s);
                Log.e("Pryv", s);
            }

            @Override
            public void onCacheSuccess(String s, Event event) {
                notifyUI(s);
                Log.i("Pryv", s);
            }

            @Override
            public void onCacheError(String s) {
                notifyUI(s);
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
                notifyUI(list);
                Log.i("Pryv", list.size() + " events retrieved from cache.");
            }

            @Override
            public void onCacheError(String s) {
                notifyUI(s);
                Log.e("Pryv", s);
            }

            @Override
            public void apiCallback(List<Event> list, Map<String, Double> map, Double aDouble) {
                notifyUI(list);
                Log.i("Pryv", list.size() + " events retrieved from API.");
            }

            @Override
            public void onApiError(String s, Double aDouble) {
                notifyUI(s);
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