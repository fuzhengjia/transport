package edu.illinois.adsc.transport;

import backtype.storm.Config;
import backtype.storm.LocalCluster;
import backtype.storm.StormSubmitter;
import backtype.storm.topology.TopologyBuilder;
import backtype.storm.tuple.Fields;
import edu.illinois.adsc.transport.topology.*;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

/**
 * Created by robert on 10/6/15.
 */
public class TopologySubmitter {

    @Option(name = "--local-mode", aliases = {"-l"}, usage = "submit topology locally")
    private  boolean local_mode;

    @Option(name = "--help", aliases = {"-h"}, usage = "help")
    private boolean _help;


    public static void main(String [] args) throws Exception {



        TopologySubmitter submitter = new TopologySubmitter();
        CmdLineParser parser = new CmdLineParser(submitter);

        parser.setUsageWidth(80);

        try {
            parser.parseArgument(args);
        } catch (CmdLineException e) {
            System.err.println(e.getMessage());
            submitter._help = true;
        }

        if (submitter._help) {
            parser.printUsage(System.err);
            System.err.println();
            return;
        }



        final String thriftIp = "192.168.0.235";
        final int port = edu.illinois.adsc.transport.Config.thriftPort;

        TopologyBuilder builder = new TopologyBuilder();

        builder.setSpout("query_dispatch", new QueryDispatchSpout(thriftIp, port), 1);

        builder.setSpout("update_dispatch", new UpdateDispatchSpout(thriftIp, port),1);

        builder.setBolt("predict_bolt", new CrowdPredictBolt(),1).fieldsGrouping("update_dispatch","update_stream", new Fields("station"));

        builder.setBolt("query", new QueryBolt(), 8).fieldsGrouping("query_dispatch", "query_stream",new Fields("station"))
                                                    .fieldsGrouping("predict_bolt","update_stream", new Fields("station"));

        builder.setBolt("result", new ResultBolt(thriftIp, port), 2).shuffleGrouping("query");

        Config conf = new Config();

        boolean local = false;

        if (submitter.local_mode) {

            conf.setDebug(true);

            conf.setMaxTaskParallelism(3);

            LocalCluster cluster = new LocalCluster();
            cluster.submitTopology("query", conf, builder.createTopology());


            while(true)
                Thread.sleep(100000);

//            cluster.shutdown();
        }
        else {
            conf.setNumWorkers(4);

            StormSubmitter.submitTopology("query", conf, builder.createTopology());
        }
    }
}
