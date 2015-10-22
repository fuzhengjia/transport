package edu.illinois.adsc.transport.coordinator;

import edu.illinois.adsc.transport.Config;
import edu.illinois.adsc.transport.generated.*;
import org.apache.thrift.TException;
import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TThreadPoolServer;
import org.apache.thrift.transport.TServerSocket;
import org.apache.thrift.transport.TServerTransport;

import java.util.HashMap;
import java.util.List;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by robert on 10/5/15.
 */
public class FakeQueryResponser implements QueryService.Iface {


    private static HashMap<String,Integer> fakeAnswer;
    static
    {
        fakeAnswer = new HashMap<String, Integer>();
        fakeAnswer.put("中山公园", 160);
        fakeAnswer.put("徐家汇", 200);
        fakeAnswer.put("宜山路", 97);
    }

    @Override
    public List<Long> query(String stationID, String timeStamp, long type) throws TException {

        Pattern p = Pattern.compile( ",([0-9]+):([0-9]+)" );
        Matcher m = p.matcher(timeStamp);
        List<Long> ret = new Vector<Long>();
        if(!m.find()){
            System.out.println("failed to parse the input");
            System.out.format("name:%s, timeStamp:%s\n",stationID,timeStamp);
            ret.add(-2L);
            return ret;
        }
        String hour = m.group(1);
        String min = m.group(2);

        return predicate(Integer.parseInt(stationID),Integer.parseInt(hour), Integer.parseInt(min));

//        if(fakeAnswer.containsKey(stationID)) {
//            return fakeAnswer.get(stationName)*Integer.parseInt(hour);
//        }

//        return -1;
    }

    @Override
    public Query takeQuery() throws TException {
        return null;
    }

    @Override
    public void finishQuery(QueryResult result) throws TException {

    }

    @Override
    public StationUpdate fetchStateUpdate() throws TException {
        StationUpdate ret = new StationUpdate();
        ret.stationId = "33";
        ret.updateMatrix = new Matrix();
        ret.updateMatrix.rows = 2;
        ret.updateMatrix.columns = 2;
        List<Double> matrix = new Vector<Double>();
        matrix.add(0.);
        matrix.add(1.0);
        matrix.add(2.);
        matrix.add(3.0);
        ret.updateMatrix.data=matrix;
        return ret;
    }

    @Override
    public void pushUpdate(StationUpdate update) throws TException {

    }

    @Override
    public void pushUpdateForce(StationUpdate update) throws TException {

    }

    @Override
    public String getCurrentTime() throws TException {
        return null;
    }

    @Override
    public boolean setTimeStamp(String time) throws TException {
        return false;
    }

    private List<Long> predicate(int bias, int hour, int min) {
        final long base = 100 + bias;
        final long peak = 1000;
        final long peakTime = 1800;
        List<Long> ret = new Vector<Long>();
        ret.add((long)((1-Math.abs(hour*min-peakTime)/(double)peakTime) * peak + base));
        return ret;

    }

    public static void startServer(String[] args) {
        System.out.println("Inside arguments:");
        for(String a: args) {
            System.out.println(a);
        }
        try {
            FakeQueryResponser queryResponseServer = new FakeQueryResponser();
            QueryService.Processor processor = new QueryService.Processor(queryResponseServer);

            TServerTransport serverTransport = new TServerSocket(Config.thriftPort);
            TServer server = new TThreadPoolServer(new TThreadPoolServer.Args(serverTransport).processor(processor));

            System.out.println("Starting the monitoring daemon...");
            server.serve();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main (String[] args) {
        System.out.println("Inside arguments:");
        for(String a: args) {
            System.out.println(a);
        }
        try {
            FakeQueryResponser queryResponseServer = new FakeQueryResponser();
            QueryService.Processor processor = new QueryService.Processor(queryResponseServer);

            TServerTransport serverTransport = new TServerSocket(Config.thriftPort);
            TServer server = new TThreadPoolServer(new TThreadPoolServer.Args(serverTransport).processor(processor));

            System.out.println("Starting the monitoring daemon...");
            server.serve();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

}
