package edu.illinois.adsc.transport;

import backtype.storm.Config;
import backtype.storm.LocalCluster;
import backtype.storm.StormSubmitter;
import backtype.storm.topology.TopologyBuilder;
import edu.illinois.adsc.transport.topology.DispatchSpout;
import edu.illinois.adsc.transport.topology.QueryBolt;
import edu.illinois.adsc.transport.topology.ResultBolt;

/**
 * Created by robert on 10/6/15.
 */
public class TopologySubmitter {
    public static void main(String [] args) throws Exception {

        final String thriftIp = "192.168.0.235";
        final int port = 20000;

        TopologyBuilder builder = new TopologyBuilder();

        builder.setSpout("dispatch",new DispatchSpout(thriftIp, port),2);

        builder.setBolt("query", new QueryBolt(), 8).shuffleGrouping("dispatch");

        builder.setBolt("result", new ResultBolt(thriftIp, port), 2).globalGrouping("query");

        Config conf = new Config();

        boolean local = false;

        if (local) {

            conf.setDebug(true);

            conf.setMaxTaskParallelism(3);

            LocalCluster cluster = new LocalCluster();
            cluster.submitTopology("query", conf, builder.createTopology());

            Thread.sleep(100000);

            cluster.shutdown();
        }
        else {
            conf.setNumWorkers(4);

            StormSubmitter.submitTopology("query", conf, builder.createTopology());
        }
    }
}
