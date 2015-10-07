package edu.illinois.adsc.transport;

import java.io.IOException;
import java.io.PrintStream;
import java.io.Writer;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by robert on 10/7/15.
 */
public class ErrorCode {

    public static Map<Long, String> errorCodeReason = new HashMap<Long, String>();

    static
    {
        errorCodeReason.put(-1L,"Query Interrupted");
        errorCodeReason.put(-2L,"Invalidate Input");
        errorCodeReason.put(-3L,"Query Timeout");
    }

    public static long Interrupted = 1;

    public static long InvalidateInput = 2;

    public static long Timeout = 3;

}
