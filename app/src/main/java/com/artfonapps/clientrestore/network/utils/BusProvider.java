package com.artfonapps.clientrestore.network.utils;

import com.squareup.otto.Bus;
import com.squareup.otto.ThreadEnforcer;

/**
 * Created by paperrose on 20.07.2016.
 */
public class BusProvider {
    private static final Bus BUS = new Bus(ThreadEnforcer.ANY);

    public static Bus getInstance(){
        return BUS;
    }

    public BusProvider(){}
}
