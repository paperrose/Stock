package com.artfonapps.clientrestore.network.requests;

import com.artfonapps.clientrestore.MainActivity;
import com.artfonapps.clientrestore.views.StartActivity;

import java.util.ArrayList;

/**
 * Created by paperrose on 31.03.2016.
 */
public class CookieStorage {

    public static MainActivity activity;
    public static StartActivity startActivity;

    private ArrayList<Object> arrayList;

    private static CookieStorage instance;

    private CookieStorage(){
        arrayList = new ArrayList<Object>();
    }

    public static CookieStorage getInstance(){
        if (instance == null){
            instance = new CookieStorage();
        }
        return instance;
    }

    public ArrayList<Object> getArrayList() {
        return arrayList;
    }

    @Override
    public String toString()
    {
        return getArrayList().toString();
    }
}
