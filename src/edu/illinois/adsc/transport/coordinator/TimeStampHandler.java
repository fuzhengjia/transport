package edu.illinois.adsc.transport.coordinator;

import org.apache.thrift.TException;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

/**
 * Created by robert on 10/14/15.
 */
public class TimeStampHandler extends CoordinatorClient {

    @Option(name = "--help", aliases = {"-h"}, usage = "help")
    private boolean _help;

    @Option(name = "--set", usage = "set timestamp")
    private String time;


    public static void main(String[] args) throws TException {
        TimeStampHandler timeStampHandler = new TimeStampHandler();
        CmdLineParser parser = new CmdLineParser(timeStampHandler);

        parser.setUsageWidth(80);

        try {
            parser.parseArgument(args);
        } catch (CmdLineException e) {
            System.err.println(e.getMessage());
            timeStampHandler._help = true;
        }

        if (timeStampHandler._help) {
            parser.printUsage(System.err);
            System.err.println();
            return;
        }

        if(!timeStampHandler.connect())
            return;
        if(timeStampHandler.time!=null) {
            if(timeStampHandler.client.setTimeStamp(timeStampHandler.time)) {
                System.out.println("Coordinator time is successfully updated!");
            }
            else {
                System.err.println("Fail to set the time");
            }
        }
        System.out.format("Current coordinator time is %s\n",timeStampHandler.client.getCurrentTime());
        timeStampHandler.close();
    }
}
