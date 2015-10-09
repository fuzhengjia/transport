package edu.illinois.adsc.transport.topology;

import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichBolt;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import edu.illinois.adsc.transport.ErrorCode;
import edu.illinois.adsc.transport.generated.Matrix;
import edu.illinois.adsc.transport.generated.Query;
import edu.illinois.adsc.transport.generated.StationUpdate;
import org.apache.thrift.TDeserializer;
import org.apache.thrift.TException;
import backtype.storm.tuple.Values;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by robert on 10/6/15.
 */
public class QueryBolt extends BaseRichBolt {

    private static final Logger logger = LoggerFactory.getLogger(QueryBolt.class);

    private OutputCollector outputCollector;

    private TDeserializer deserializer;

    private Map<String, Matrix> stationID2Matrix = new HashMap<String, Matrix>();

    @Override
    public void prepare(Map map, TopologyContext topologyContext, OutputCollector boltOutputCollector) {
        outputCollector = boltOutputCollector;
        deserializer = new TDeserializer();
    }

    @Override
    public void execute(Tuple tuple) {
        String streamID = tuple.getSourceStreamId();
        if(streamID.equals("query_stream")) {
            handleQuery(tuple);
        }
        else if (streamID.equals("update_stream")) {
            handleUpdate(tuple);
        }
        else {
            System.err.println("Unknown stream source");
        }

    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {
        outputFieldsDeclarer.declare(new Fields("id","result"));
    }

    private long predicate(int bias, int hour, int min) {
        final long base = 100 + bias;
        final long peak = 1000;
        final long peakTime = 1800;
        return (long)((1-Math.abs(hour*min-peakTime)/(double)peakTime) * peak + base);

    }

    private void handleQuery(Tuple tuple) {
        long queryId = tuple.getLong(0);
        Query query = new Query();
        long result;
        try{
            deserializer.deserialize(query,tuple.getBinary(1));

            Pattern p = Pattern.compile( ",([0-9]+):([0-9]+)" );
            Matcher m = p.matcher(query.getTimeStamp());
            if(!m.find()){
                System.err.println("failed to parse the input");
                System.err.format("name:%s, timeStamp:%s\n",query.getStationId(),query.getStationId());
                result = ErrorCode.InvalidateInput;
            }
            else{
                String hour = m.group(1);
                String min = m.group(2);

                result = predicate(Integer.parseInt(query.getStationId()),Integer.parseInt(hour), Integer.parseInt(min));
            }

            outputCollector.emit(new Values(query.query_id, result));

        }
        catch (TException e) {
            e.printStackTrace();
        }
    }

    void handleUpdate(Tuple tuple) {
        StationUpdate update = new StationUpdate();
        try {
            deserializer.deserialize(update,tuple.getBinary(1));
        } catch (TException e) {
            e.printStackTrace();
        }
        if(stationID2Matrix.containsKey(update.getStationId())) {
            stationID2Matrix.put(update.getStationId(),update.getUpdateMatrix());
        }
        else {
            stationID2Matrix.put(update.getStationId(),update.getUpdateMatrix());
        }
    }

}
