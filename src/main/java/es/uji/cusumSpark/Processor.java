package es.uji.cusumSpark;

import twitter4j.Status;

/**
 * Created by oscar on 4/07/14.
 */
public interface Processor {
    boolean processTweet(Status status);
    void stop();
}
