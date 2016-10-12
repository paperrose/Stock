package com.artfonapps.clientrestore.messages;

import android.content.Context;

import com.artfonapps.clientrestore.views.IShownToUser;

import java.util.LinkedList;

/**
 * Created by Altirez on 07.09.2016.
 */
public abstract class BaseMessenger  implements  IMessenger{

    protected LinkedList<IAlertMessage> messages;
    protected Context context;
    protected IAlertMessage current;


    protected BaseMessenger(final Context context){
        this.context = context;
        this.messages = new LinkedList<>();
    }

    protected boolean isShowing(){
        return context instanceof IShownToUser &&
                ((IShownToUser) context).isVisible();
    }

    protected boolean isNotification(IAlertMessage message){
        return message instanceof INotifyMessage;
    }

    protected IAlertMessage getLastMessage(){
        return messages.peekLast();
    }
    protected IAlertMessage getFirstMessage(){
        return messages.peekFirst();
    }

    @Override
    public void clearMessages() {
        messages.clear();
    }

    @Override
    public void showMessage(int id) {
        for (IAlertMessage message : this.messages ){
            if( message.getId() == id)
                showMessage(message);
        }
    }

    //TODO: Refactor
    @Override
    public void showMessage(IAlertMessage message) {
        //Запланировать если не показанно
        if (!isShowing()){
            if (!messages.contains(message)){
                messages.addFirst(message);
            }
            return;
        }
        if (current == message){
            return;
        }
        //Если кто-то уже демоснтрируется убираем его в начало очереди
        if (current != null){
            messages.addFirst(current);
        }

        //Выставляем текущим и убераем из очереди
        current = message;
        if (messages.contains(message)){
            messages.remove(message);
        }
        current.show();

    }

    public void setContext (final Context context){
        this.context = context;
    }

    @Override
    public void showMessages() {

        if (!isShowing()){
            IAlertMessage last = getLastMessage();
            if (last != null && isNotification(last)){
                ((INotifyMessage)last).Notify();
            }
            return;
        }

        if (!messages.isEmpty() && current == null ){
            current = messages.poll();
            current.show();
        }
    }

    @Override
    public void addMessage(IAlertMessage message) {
        messages.add(message);
    }

    @Override
    public Context getContext() {
        return this.context;
    }

    @Override
    public void onMessageDismiss() {
        current = null;
        showMessages();
    }

}
