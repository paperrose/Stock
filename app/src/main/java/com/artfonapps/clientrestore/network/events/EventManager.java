package com.artfonapps.clientrestore.network.events;

import android.content.Context;

import com.artfonapps.clientrestore.network.utils.BusProvider;

import java.util.LinkedList;
import java.util.Queue;

/**
 * Created by Altirez on 16.09.2016.
 */
public class EventManager {

    protected static EventManager _instance = new EventManager();
    protected Queue<BaseEvent> _events;
    protected Context _context;

    EventManager(){
        _events = new LinkedList<>();
    }

    public Context get_context() {
        return _context;
    }

    public void set_context(Context context) {
        _context = context;
    }

    public static EventManager get_instance() {
        return _instance;
    }


    public void addEvent(BaseEvent event){
        _events.add(event);
    }

    public void produceEvents(){
        if(!_events.isEmpty() && _context != null){
            BusProvider.getInstance().post(_events.poll());
            produceEvents();
        }
    }
}
