package com.pryv.appAndroidExample;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.pryv.model.Event;

import java.util.ArrayList;
import java.util.List;

public class UINotifier {

    public final static int NOTIFICATION_TYPE_MESSAGE = 1;
    public final static int NOTIFICATION_TYPE_EVENTS = 2;
    private Handler notificationHandler;

    public UINotifier(Handler handler) {
        notificationHandler = handler;
    }

    /**
     * Notify MainActivity to update its progress dialog
     * @param notification: the notification message
     */
    public void notify(String notification) {
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
    public void notify(List<Event> events) {
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
}
