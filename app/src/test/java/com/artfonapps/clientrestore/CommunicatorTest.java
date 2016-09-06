package com.artfonapps.clientrestore;

import com.artfonapps.clientrestore.network.requests.Communicator;

import org.junit.Test;

import okhttp3.HttpUrl;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class CommunicatorTest {
    static final String TEST_PHONE_NUMBER = "+79602857432";
    @Test
    public void CommunicatorConstructTest() throws Exception {
        assertNotNull("Missing Communicator!", Communicator.INSTANCE);
    }
    @Test
    public void CommunicatorProductionUrlTest() throws Exception {
        HttpUrl hostUrl = Communicator.INSTANCE.getBaseUrl();
        assertNotNull("Missing host URI!", hostUrl);
        assertTrue("Host Url is not StockTrading host!", hostUrl.host().compareToIgnoreCase("stocktrading.log-os.ru") == 0);
    }
    //Интерфейс класса не очень поощряет unitTesting
    @Test
    public void CommunicatorJobCommunicateTest() throws Exception {


    }

}
