package com.pryv.appAndroidExample;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.pryv.Filter;
import com.pryv.Pryv;
import com.pryv.api.OnlineEventsAndStreamsManager;
import com.pryv.database.DBinitCallback;
import com.pryv.database.QueryGenerator;
import com.pryv.interfaces.EventsCallback;
import com.pryv.model.Event;
import com.pryv.model.Stream;
import com.pryv.utils.Logger;

import java.lang.ref.WeakReference;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.Set;

public class SQLiteDBHelper extends SQLiteOpenHelper {

    // JAVA
    private final String initDBerrorMessage = "Database initialization error: ";
    // the DB Connection
    private Connection dbConnection;
    // weak reference to Pryv's Connection
    private WeakReference<com.pryv.Connection> weakConnection;
    private Filter scope;
    private OnlineEventsAndStreamsManager api;
    private Double lastUpdate;
    private Logger logger = Logger.getInstance();
    private DBinitCallback initCallback;

    // ANDROID
    private static final int DATABASE_VERSION = 1;
    private static final String TABLE_EVENTS = "events";
    private static final String KEY_CONTENT = "content";
    private SQLiteDatabase db;

    /**
     * SQLiteDBHelper constructor. Creates and Connects to the SQLite database
     *
     * @param cacheFolderPath
     *          the path to the caching folder
     * @param weakConnection
     * @param initCallback
     *          callback to notify failure
     */
    // TODO: Track db creation exception => callback
    // TODO: Does rawQuery/execSQL require close()?
    public SQLiteDBHelper(Context context, Filter scope, String cacheFolderPath, OnlineEventsAndStreamsManager api,
                          WeakReference<com.pryv.Connection> weakConnection,
                          DBinitCallback initCallback) {
        super(context, cacheFolderPath + Pryv.DATABASE_NAME, null, DATABASE_VERSION);
        this.scope = scope;
        this.api = api;
        this.weakConnection = weakConnection;
        this.initCallback = initCallback;
        logger.log("SQLiteDBHelper: init DB in: " + cacheFolderPath + Pryv.DATABASE_NAME);
    }

    /**
     * Creates tables if required.
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        this.db = db;
        try {
            createEventsTable();
            createSteamsTable();
        } catch (SQLException e) {
            initCallback.onError(initDBerrorMessage + e.getMessage());
            e.printStackTrace();
        }
    }

    // Upgrading database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_EVENTS);
        // Create tables again
        onCreate(db);
    }

    /**
     * Set a scope of data stored in the cache.
     *
     * @param scope
     */
    public void setScope(Filter scope) {
        this.scope = scope;
    }

    /**
     * Create Events table in the SQLite database.
     *
     * @throws SQLException
     */
    private void createEventsTable() throws SQLException {
        String cmd = QueryGenerator.createEventsTable();
        logger.log("SQLiteDBHelper: createEventsTable: " + cmd);
        db.execSQL(cmd);
    }

    /**
     * Create Streams table in the SQLite database.
     *
     * @throws SQLException
     */
    private void createSteamsTable() throws SQLException {
        String cmd = QueryGenerator.createStreamsTable();
        logger.log("SQLiteDBHelper: createStreamsTable: " + cmd);
        db.execSQL(cmd);
    }

    // TODO: public void update(final UpdateCacheCallback updateCacheCallback) {...}

    /**
     * Inserts Event into the SQLite database.
     *
     * @param eventToCache
     *          the event to insert
     * @param eventsCallback
     *          callback to notify succeeventsCallback.onCacheError(e.getMessage());ss or failure
     */
    public void createEvent(final Event eventToCache, final EventsCallback eventsCallback) {
        new Thread() {
            @Override
            public void run() {
                try {
                    String cmd = QueryGenerator.insertOrReplaceEvent(eventToCache);
                    logger.log("SQLiteDBHelper: create event: " + cmd);
                    db.execSQL(cmd);

                    if (eventsCallback != null) {
                        eventsCallback.onCacheSuccess("SQLiteDBHelper: Event cached", eventToCache);
                    }
                }
                // TODO: catch db exception
                /* catch (SQLException e) {
                    if (eventsCallback != null) {
                        eventsCallback.onCacheError(e.getMessage());
                    }
                }*/
                catch (JsonProcessingException e) {
                    if (eventsCallback != null) {
                        eventsCallback.onCacheError(e.getMessage());
                    }
                }
            }
        }.start();
    }

    /**
     * update Event in the SQLite database
     *
     * @param eventToUpdate
     * @param cacheEventsCallback
     */
    public void updateEvent(final Event eventToUpdate, final EventsCallback cacheEventsCallback) {
        new Thread() {

            public void run() {
                try {
                    String cmd = QueryGenerator.updateEvent(eventToUpdate);
                    logger.log("SQLiteDBHelper: update event: " + cmd);
                    db.execSQL(cmd);

                    if (cacheEventsCallback != null) {
                        // TODO: Retrieve and print number of events updated: maybe change execSQL to rawSQL
                        cacheEventsCallback.onCacheSuccess("SQLiteDBHelper: Event(s) updated in cache", eventToUpdate);
                    }
                } catch (JsonProcessingException e) {
                    if (cacheEventsCallback != null) {
                        cacheEventsCallback.onCacheError(e.getMessage());
                    }
                }
            }
        }.start();
    }

    /**
     * Update Events in the SQLite database. used only when the cache receives
     * events from online.
     *
     * @param eventsToCache
     *          the events to insert in the cache
     * @param cacheEventsCallback
     *          callback to notify success or failure
     */
    public void updateOrCreateEvents(final Collection<Event> eventsToCache,
                                     final EventsCallback cacheEventsCallback) {
        new Thread() {
            @Override
            public void run() {

                for (Event event : eventsToCache) {
                    try {
                        String cmd = QueryGenerator.insertOrReplaceEvent(event);
                        logger.log("SQLiteDBHelper: update or create event : " + cmd);
                        db.execSQL(cmd);
                        logger.log("SQLiteDBHelper: inserted " + event.getClientId() + " into DB.");
                    }
                    // TODO: catch db exception
                    /* catch (SQLException e) {
                        cacheEventsCallback.onCacheError(e.getMessage());
                        e.printStackTrace();
                    }*/
                    catch (JsonProcessingException e) {
                        cacheEventsCallback.onCacheError(e.getMessage());
                        e.printStackTrace();
                    }
                }
                cacheEventsCallback.onCacheSuccess("SQLiteDBHelper: Events updated", null);

            }
        }.start();
    }

    // TODO: public void deleteEvent()

    // TODO: public void getEvents()

    // TODO: public void updateOrCreateStream()

    // TODO: public void updateOrCreateStreams()

    /**
     * gathers all descendants of Stream into allStreams
     *
     * @param childrenStreams
     *          a Set<Stream> into which all children are put
     * @param parentStream
     *          the stream whose children are gathered
     */
    private void retrieveAllChildren(Set<Stream> childrenStreams, Stream parentStream) {
        if (parentStream.getChildren() != null) {
            for (Stream childStream : parentStream.getChildren()) {
                childrenStreams.add(childStream);
                retrieveAllChildren(childrenStreams, childStream);
            }
        }
    }

    // TODO: public void deleteStream()

    // TODO: public void getStreams()

}