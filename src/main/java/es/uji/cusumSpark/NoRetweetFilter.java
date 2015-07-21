package es.uji.cusumSpark;

import twitter4j.Status;

/**
 * Created by oscar on 8/07/14.
 */
public class NoRetweetFilter implements Filter {
    @Override
    public boolean filter(Status status) {
        if(!status.isRetweet() &&
                status.getInReplyToScreenName() == null &&
                status.getInReplyToStatusId() == -1 &&
                status.getInReplyToUserId() == -1)
            return true;
        else return false;
    }
}