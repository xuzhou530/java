package com.pubnub.api.endpoints;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.github.tomakehurst.wiremock.verification.LoggedRequest;
import com.jayway.awaitility.Awaitility;
import com.pubnub.api.callbacks.TimeCallback;
import com.pubnub.api.core.PubnubException;
import com.pubnub.api.core.models.consumer_facing.PNErrorStatus;
import com.pubnub.api.core.models.consumer_facing.PNTimeResult;
import org.junit.Before;
import org.junit.Rule;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TimeEndpointTest extends EndpointTest {
    private Time partialTime;

    @Rule
    public WireMockRule wireMockRule = new WireMockRule();

    @Before
    public void beforeEach() throws IOException {
        partialTime = this.createPubNubInstance(8080).time().build();
    }


    @org.junit.Test
    public void testSyncSuccess() throws IOException, PubnubException, InterruptedException {
        stubFor(get(urlPathEqualTo("/time/0"))
                .willReturn(aResponse().withBody("[14593046077243110]")));

        PNTimeResult response = partialTime.sync();
        assertTrue(response.getTimetoken().equals(14593046077243110L));

        List<LoggedRequest> requests = findAll(getRequestedFor(urlMatching("/.*")));
        assertEquals(1, requests.size());

    }

    @org.junit.Test(expected=PubnubException.class)
    public void testSyncBrokenWithString() throws IOException, PubnubException {
        stubFor(get(urlPathEqualTo("/time/0"))
                .willReturn(aResponse().withBody("[abc]")));
        partialTime.sync();
    }

    @org.junit.Test(expected=PubnubException.class)
    public void testSyncBrokenWithoutJSON() throws IOException, PubnubException {
        stubFor(get(urlPathEqualTo("/time/0"))
                .willReturn(aResponse().withBody("zimp")));
        partialTime.sync();
    }

    @org.junit.Test(expected=PubnubException.class)
    public void testSyncBrokenWithout200() throws IOException, PubnubException {
        stubFor(get(urlPathEqualTo("/time/0"))
                .willReturn(aResponse().withBody("[14593046077243110]").withStatus(404)));
        PNTimeResult response = partialTime.sync();
        assertEquals(response.getTimetoken(), "14593046077243110");
    }

    @org.junit.Test
    public void testAsyncSuccess() throws IOException, PubnubException {
        stubFor(get(urlPathEqualTo("/time/0"))
                .willReturn(aResponse().withBody("[14593046077243110]")));
        final AtomicInteger atomic = new AtomicInteger(0);
        partialTime.async(new TimeCallback(){

            @Override
            public void onResponse(PNTimeResult result, PNErrorStatus status) {
                assertTrue(result.getTimetoken().equals(14593046077243110L));
                atomic.incrementAndGet();
            }
        });

        Awaitility.await().atMost(5, TimeUnit.SECONDS)
                .untilAtomic(atomic, org.hamcrest.core.IsEqual.equalTo(1));
    }

    @org.junit.Test
    public void testAsyncBrokenWithString() throws IOException, PubnubException {
        stubFor(get(urlPathEqualTo("/time/0"))
                .willReturn(aResponse().withBody("[abc]")));
        final AtomicInteger atomic = new AtomicInteger(0);
        partialTime.async(new TimeCallback(){

            @Override
            public void onResponse(PNTimeResult result, PNErrorStatus status) {
                if (status != null) {
                    atomic.incrementAndGet();
                }
            }
        });

        Awaitility.await().atMost(5, TimeUnit.SECONDS)
                .untilAtomic(atomic, org.hamcrest.core.IsEqual.equalTo(1));

    }

    @org.junit.Test
    public void testAsyncBrokenWithoutJSON() throws IOException, PubnubException {
        stubFor(get(urlPathEqualTo("/time/0"))
                .willReturn(aResponse().withBody("zimp")));
        final AtomicInteger atomic = new AtomicInteger(0);
        partialTime.async(new TimeCallback(){

            @Override
            public void onResponse(PNTimeResult result, PNErrorStatus status) {
                if (status != null) {
                    atomic.incrementAndGet();
                }
            }
        });

        Awaitility.await().atMost(5, TimeUnit.SECONDS)
                .untilAtomic(atomic, org.hamcrest.core.IsEqual.equalTo(1));

    }

    @org.junit.Test
    public void testAsyncBrokenWithout200() throws IOException, PubnubException {
        stubFor(get(urlPathEqualTo("/time/0"))
                .willReturn(aResponse().withBody("[14593046077243110]").withStatus(404)));
        final AtomicInteger atomic = new AtomicInteger(0);
        partialTime.async(new TimeCallback(){

            @Override
            public void onResponse(PNTimeResult result, PNErrorStatus status) {
                if (status != null) {
                    atomic.incrementAndGet();
                }
            }

        });

        Awaitility.await().atMost(5, TimeUnit.SECONDS)
                .untilAtomic(atomic, org.hamcrest.core.IsEqual.equalTo(1));

    }


}