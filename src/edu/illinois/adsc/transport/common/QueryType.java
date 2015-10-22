package edu.illinois.adsc.transport.common;

/**
 * Created by robert on 10/22/15.
 */
public class QueryType {
    public static final long StationCrowd = 0;
    public static final long StationWaiting = 1;
    public static final long TrainCrowd = 2;
    public static boolean validQueryTyep(long type) {
        return type>=0 && type<=2;
    }
}
