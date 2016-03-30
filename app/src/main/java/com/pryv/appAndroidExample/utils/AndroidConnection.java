package com.pryv.appAndroidExample.utils;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.TextView;

import com.pryv.Connection;
import com.pryv.Pryv;
import com.pryv.api.EventsCallback;
import com.pryv.api.StreamsCallback;
import com.pryv.api.database.DBinitCallback;
import com.pryv.api.model.Event;
import com.pryv.api.model.Stream;
import com.pryv.appAndroidExample.activities.LoginActivity;

import java.util.Map;

/**
 * Created by Thieb on 26.02.2016.
 */
public class AndroidConnection {
    private static Connection connection = null;
    private static StreamsCallback streamsCallback = null;
    private static EventsCallback eventsCallback = null;
    private Context context;
    private TextView progressView;

    public AndroidConnection (Context context, TextView progressView) {
        Pryv.deactivateCache();
        Pryv.deactivateSupervisor();
        connection = new Connection(LoginActivity.getUsername(), LoginActivity.getToken(), new DBinitCallback(){});
        instanciateSCB();
        instanciateECB();
        this.context = context;
        this.progressView = progressView;
    }

    public static void saveEvent(Event event) {
        connection.createEvent(event, eventsCallback);
    }

    public static void saveStream(Stream stream) {
        connection.createStream(stream, streamsCallback);
    }

    private class SaveEventAsync extends AsyncTask<Event, Void, Void> {

        @Override
        protected Void doInBackground(Event... events) {
            //connection.createEvent(event, eventsCallback);
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {

        }

    }

    private class SaveStreamAsync extends AsyncTask<Event, Void, Void> {

        @Override
        protected Void doInBackground(Event... events) {
            for(Event e: events) {

            }
            //connection.createEvent(event, eventsCallback);
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {

        }

    }

    private static void instanciateECB() {
        eventsCallback = new EventsCallback() {

            @Override
            public void onEventsRetrievalSuccess(Map<String, Event> events, Double serverTime) {
                Log.d("Pryv", "onEventsRetrievalSuccess");
            }

            @Override
            public void onEventsRetrievalError(String errorMessage, Double serverTime) {
                Log.d("Pryv", "onEventsRetrievalError");
            }

            @Override
            public void onEventsSuccess(String successMessage, Event event, Integer stoppedId,
                                        Double serverTime) {
                Log.d("Pryv", "onEventsSuccess");
            }

            @Override
            public void onEventsError(String errorMessage, Double serverTime) {
                Log.d("Pryv", "onEventsError");
            }

        };
    }

    private static void instanciateSCB() {
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