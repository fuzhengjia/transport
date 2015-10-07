package edu.illinois.adsc.transport;

/**
 * Created by robert on 10/6/15.
 */
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import java.util.ArrayList;
import java.util.List;

public class StartDaemon {

    @Option(name = "-f", usage = "use fake model")
    private boolean fackMode;

    @Option(name = "--help", aliases = {"-h"}, usage = "help")
    private boolean _help;

    @Argument
    List<String> restArguments = new ArrayList<String>();

    public static void main(String[] args) {

        StartDaemon startDaemon = new StartDaemon();
        CmdLineParser parser = new CmdLineParser(startDaemon);

        parser.setUsageWidth(80);

        try {
            parser.parseArgument(args);
        } catch (CmdLineException e) {
            System.err.println(e.getMessage());
            startDaemon._help = true;
        }

        if (startDaemon._help) {
            parser.printUsage(System.err);
            System.err.println();
            return;
        }
        if(startDaemon.fackMode){

            FakeQueryResponser.startServer(convertToStrings(startDaemon.restArguments));
        }
        else {
            DRPCQueryResponser.startServer(convertToStrings(startDaemon.restArguments));
        }
    }

    static private String[] convertToStrings(List<String> array) {
        String[] ret = new String[array.size()];
        for(int i=0; i<ret.length;i++) {
            ret[i] = array.get(i);
        }
        return ret;
    }
}
