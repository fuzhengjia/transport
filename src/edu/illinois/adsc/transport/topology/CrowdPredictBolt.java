package edu.illinois.adsc.transport.topology;

import backtype.storm.task.ShellBolt;
import backtype.storm.topology.IRichBolt;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.tuple.Fields;

import java.util.Map;

/**
 * Created by robert on 10/12/15.
 */
public class CrowdPredictBolt extends ShellBolt implements IRichBolt {

    public CrowdPredictBolt() {
        super("python","processingbolt.py");
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {
        outputFieldsDeclarer.declareStream("update_stream",new Fields("station","query_type","time","value","value2"));
    }

    @Override
    public Map<String, Object> getComponentConfiguration() {
        return null;
    }
}
