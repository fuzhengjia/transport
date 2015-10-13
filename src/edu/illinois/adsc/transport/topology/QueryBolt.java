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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
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

    private Map<String, SortedMap<Calendar,Long>> stationID2Matrix = new HashMap<String, SortedMap<Calendar,Long>>();

    @Override
    public void prepare(Map map, TopologyContext topologyContext, OutputCollector boltOutputCollector) {
        outputCollector = boltOutputCollector;
        deserializer = new TDeserializer();

//        generateMatrix();
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
                result = predicateBasedOnMatrix(query);
                if(result<0)
                    result = predicate(Integer.parseInt(query.getStationId()),Integer.parseInt(hour), Integer.parseInt(min));
            }

            outputCollector.emit(new Values(query.query_id, result));

        }
        catch (TException e) {
            e.printStackTrace();
        }
    }

    void handleUpdate(Tuple tuple) {
        String station = tuple.getString(0);
        String timeStamp = tuple.getString(1);
        Long value = tuple.getDouble(2).longValue();

        if(!stationID2Matrix.containsKey(station)) {
            stationID2Matrix.put(station, new TreeMap<Calendar, Long>());
        }

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd,HH:mm");
        Calendar cal = Calendar.getInstance();
        try{
            cal.setTime(simpleDateFormat.parse(timeStamp));

            stationID2Matrix.get(station).put(cal, value);
            System.out.println("stationID2Matrix is updated: "+timeStamp+","+value);
        } catch (ParseException e) {
            e.printStackTrace();
        }

    }

    private long predicateBasedOnMatrix(Query query) {
        String stationId = query.getStationId();
        if(!stationID2Matrix.containsKey(stationId)){
            System.out.println("stationID2Matrix does not contains the key"+stationId);
            return -1;
        }
        SortedMap<Calendar, Long> map = stationID2Matrix.get(stationId);

        String time = query.getTimeStamp();

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd,HH:mm");
        Calendar cal = Calendar.getInstance();
        try {
            cal.setTime(simpleDateFormat.parse(time));
        } catch (ParseException e) {
            e.printStackTrace();
            System.out.println("time parse fails!");
            return -1;
        }

        SortedMap<Calendar, Long> tailmap = map.tailMap(cal);
        if(tailmap.isEmpty()){
            System.out.println("tails is empty");
            return -1;
        }
        Long value = map.get(tailmap.firstKey());
        System.out.println("Predicate based on matrix:"+value);
        return value;


    }

    private void generateMatrix() {

        TreeMap<Calendar, Long> map = new TreeMap<Calendar, Long>();

        Matrix matrix = new Matrix();
        matrix.rows = 1;
        matrix.columns = 1;
        matrix.data = new Vector<Double>();
        matrix.data.add(1024.);

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd,HH:mm");
        Calendar cal = Calendar.getInstance();
        try{
            cal.setTime(simpleDateFormat.parse("2015-4-24,14:00"));
        } catch (ParseException e) {
            e.printStackTrace();
            return;
        }


        map.put(cal,1024L);


        stationID2Matrix.put("1", map);
        System.out.println("generate matrix succeed!");
    }

}
