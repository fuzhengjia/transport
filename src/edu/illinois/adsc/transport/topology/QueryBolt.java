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

    private Map<Long, Map<String, SortedMap<Calendar,List<Long>>>> type2StationID2Matrix = new HashMap<Long, Map<String, SortedMap<Calendar, List<Long>>>>();

//    private Map<String, SortedMap<Calendar,Long>> stationID2Matrix = new HashMap<String, SortedMap<Calendar,Long>>();

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
        outputFieldsDeclarer.declare(new Fields("id","query_type","result","result2"));
    }

    private List<Long> predicate(int bias, int hour, int min) {
        final long base = 50000 + bias;
        final long peak = 100000;
        final long peakTime = 1800;
        List<Long> ret = new Vector<Long>();
        ret.add((long)((1-Math.abs(hour*min-peakTime)/(double)peakTime) * peak + base));
        ret.add((long)((1-Math.abs(hour*min-peakTime)/(double)peakTime) * peak + base + new Random().nextInt(1000)));
        return ret;

    }

    private void handleQuery(Tuple tuple) {
        long queryId = tuple.getLong(0);
        Query query = new Query();
        List<Long> result = new Vector<Long>();
        try{
            deserializer.deserialize(query,tuple.getBinary(1));

            Pattern p = Pattern.compile( ",([0-9]+):([0-9]+)" );
            Matcher m = p.matcher(query.getTimeStamp());
            if(!m.find()){
                System.err.println("failed to parse the input");
                System.err.format("name:%s, timeStamp:%s\n",query.getStationId(),query.getStationId());
                result.add(ErrorCode.InvalidateInput);
            }
            else{
                String hour = m.group(1);
                String min = m.group(2);
                result = predicateBasedOnMatrix(query);
                if(result.get(0)<0)
                    result = predicate(Integer.parseInt(query.getStationId()),Integer.parseInt(hour), Integer.parseInt(min));
            }

            outputCollector.emit(new Values(query.query_id, query.query_type, result.get(0), result.get(1)));

        }
        catch (TException e) {
            e.printStackTrace();
        }
    }

    void handleUpdate(Tuple tuple) {
        String station = tuple.getString(0);
        Long queryType = tuple.getLong(1);
        String timeStamp = tuple.getString(2);
        Long value = tuple.getLong(3);
        Long value2 = tuple.getLong(4);

        System.out.format("handle update: %s, %s, %d\n\n", station, timeStamp, value);

        if(!type2StationID2Matrix.containsKey(queryType)) {
            type2StationID2Matrix.put(queryType, new HashMap<String, SortedMap<Calendar,List<Long>>>());
        }

        Map<String, SortedMap<Calendar,List<Long>>> targetStationID2Matrix = type2StationID2Matrix.get(queryType);


        if(!targetStationID2Matrix.containsKey(station)) {
            targetStationID2Matrix.put(station, new TreeMap<Calendar, List<Long>>());
        }

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd,HH:mm");
        Calendar cal = Calendar.getInstance();
        try{
            cal.setTime(simpleDateFormat.parse(timeStamp));
            List<Long> values = new Vector<Long>();
            values.add(value);
            values.add(value2);
            targetStationID2Matrix.get(station).put(cal, values);
            System.out.format("stationId2Matrix is updated: type:%d time:%s value:%d\n",queryType, timeStamp, value);
        } catch (ParseException e) {
            e.printStackTrace();
        }

    }

    private List<Long> predicateBasedOnMatrix(Query query) {
        String stationId = query.getStationId();
        Long queryType = query.getQuery_type();
        Map<String, SortedMap<Calendar,List<Long>>> targetStationID2Matrix = type2StationID2Matrix.get(queryType);
        List<Long> ret = new Vector<Long>();
        if(targetStationID2Matrix == null || !targetStationID2Matrix.containsKey(stationId)){
            System.out.println("stationID2Matrix does not contains the key "+stationId);
            ret.add(-1L);
            return ret;
        }
        SortedMap<Calendar, List<Long>> map = targetStationID2Matrix.get(stationId);

        String time = query.getTimeStamp();

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd,HH:mm");
        Calendar cal = Calendar.getInstance();
        try {
            cal.setTime(simpleDateFormat.parse(time));
        } catch (ParseException e) {
            e.printStackTrace();
            System.out.println("time parse fails!");
            ret.add(-1L);
            return ret;
        }

        SortedMap<Calendar, List<Long>> tailmap = map.tailMap(cal);
        if(tailmap.isEmpty()){
            System.out.println("tails is empty");
            ret.add(-1L);
            return ret;
        }
        List<Long> values = map.get(tailmap.firstKey());

        System.out.format("Predicate based on matrix: value1 :%d, value2: %s timestamp: %s\n", values.get(0), values.get(1), simpleDateFormat.format(tailmap.firstKey().getTime()));
//        System.out.println("Predicate based on matrix:" + value + "at timestamp:" + simpleDateFormat.format(tailmap.firstKey().getTime()));

        return values;


    }

//    private void generateMatrix() {
//
//        TreeMap<Calendar, Long> map = new TreeMap<Calendar, Long>();
//
//        Matrix matrix = new Matrix();
//        matrix.rows = 1;
//        matrix.columns = 1;
//        matrix.data = new Vector<Double>();
//        matrix.data.add(1024.);
//
//        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd,HH:mm");
//        Calendar cal = Calendar.getInstance();
//        try{
//            cal.setTime(simpleDateFormat.parse("2015-4-24,14:00"));
//        } catch (ParseException e) {
//            e.printStackTrace();
//            return;
//        }
//
//
//        map.put(cal,1024L);
//
//
//        stationID2Matrix.put("1", map);
//        System.out.println("generate matrix succeed!");
//    }

}
