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
    private Handler notificationHandler;
    private EventsCallback eventsCallback;
    private GetEventsCallback getEventsCallback;
    private StreamsCallback streamsCallback;

    public static AndroidConnection singleton;

    public final static int NOTIFICATION_TYPE_MESSAGE = 1;
    public final static int NOTIFICATION_TYPE_EVENTS = 2;

    public static AndroidConnection sharedInstance() {
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
     * Save a new event to Pryv
     * @param streamId: id of the stream containing the new event
     * @param type: type of the new event
     * @param content: content of the new event
     */
    public void saveEvent(String streamId, String type, String content) {
        if(connection==null) {
            notifyUI("Need login!");
            Log.e("Pryv","Need login!");
        } else {
            connection.events.create(new Event(streamId, null, type, content), eventsCallback);
        }
    }

    /**
     * Save a new stream to Pryv
     * @param streamId: id of the new stream
     * @param streamName: name of the new stream
     */
    public Stream saveStream(String streamId, String streamName) {
        Stream stream = null;
        if(connection==null) {
            notifyUI("Need login!");
            Log.e("Pryv", "Need login!");
        } else {
            stream = new Stream(streamId,streamName);
            connection.streams.create(stream, streamsCallback);
        }
        return stream;
    }

    /**
     * Retrieve all events from Pryv according to some filter
     * @param stream: filter events by stream
     */
    public void retrieveEvents(Stream stream) {
        if(connection == null) {
            notifyUI("Need login!");
            Log.e("Pryv", "Need login!");
        } else {
            Filter filter = new Filter();
            filter.addStream(stream);
            connection.events.get(filter, getEventsCallback);
        }
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

        //Called when action related to events creation/modification complete
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

        //Called when action related to streams complete
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

        //Called when action related to events retrieval
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

    }

}