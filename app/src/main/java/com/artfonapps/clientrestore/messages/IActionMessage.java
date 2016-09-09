package com.artfonapps.clientrestore.messages;

/**
 * Created by Altirez on 07.09.2016.
 */
public interface IActionMessage {
    public void setOnPossitiveAction(Runnable action);
    public void setOnNegativeAction(Runnable action);
    public void setOnNeutralAction(Runnable action);
}
