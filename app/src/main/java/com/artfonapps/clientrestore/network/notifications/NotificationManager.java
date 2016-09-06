package com.artfonapps.clientrestore.network.notifications;

import com.artfonapps.clientrestore.views.StartActivity;

import java.util.LinkedList;

/**
 * Created by Admin on 05.09.2016.
 */
 public class NotificationManager {
    private LinkedList<INotification> notifications = new LinkedList<>();

    public void setActivity(StartActivity activity) {
        this.activity = activity;
    }

    private StartActivity activity;
    private INotification _currentNotify;
    private final static NotificationManager _instance = new NotificationManager();

    public NotificationManager(){
    }

    public final static NotificationManager getInstance(){
        return _instance;
    }

    public void addNotify(INotification notify){

        notify.setDismiss( dialog -> {
            _currentNotify = null;
            this.showPlanedNotifies();
        });
        notifications.add(notify);
    }
    
    public void showPlanedNotifies(){
        if (_currentNotify != null)
            return;

        if (!notifications.isEmpty() && activity.isVisible()){
            _currentNotify = notifications.poll();
            _currentNotify.show();
        }
    }

    public void showNotify(){
        notifications.peek();
    }

}
