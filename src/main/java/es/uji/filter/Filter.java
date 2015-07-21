package es.uji.filter;

import twitter4j.Status;

/**
 * Created by oscar on 8/07/14.
 */
public interface Filter {
    boolean filter(Status status);
}
