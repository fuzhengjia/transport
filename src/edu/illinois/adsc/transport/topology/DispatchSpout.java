package edu.illinois.adsc.transport.topology;

import backtype.storm.spout.SpoutOutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichSpout;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;
import edu.illinois.adsc.transport.generated.Query;
import edu.illinois.adsc.transport.generated.QueryService;
import org.apache.thrift.TException;
import org.apache.thrift.TSerializer;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;

import java.util.Map;

/**
 * This class receives the user queries and dispatches queries to the subsequent bolts.
 */
public class DispatchSpout extends BaseRichSpout {

    private TTransport transport;
    private QueryService.Client thriftClient;

    private SpoutOutputCollector outputCollector;

    private String thriftServerIp;
    private int thriftServerPort;

    TSerializer serializer;


    public DispatchSpout(String ip, int port) {
        thriftServerIp = ip;
        thriftServerPort = port;
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {
        outputFieldsDeclarer.declare(new Fields("id","query"));
    }

    @Override
    public void open(Map map, TopologyContext topologyContext, SpoutOutputCollector spoutOutputCollector) {
        outputCollector = spoutOutputCollector;
        serializer = new TSerializer();
        connectToThriftServer();
    }

    @Override
    public void nextTuple() {
        try {
            Query query = thriftClient.takeQuery();
            if(query.getQuery_id()>=0)
                outputCollector.emit(new Values(query.query_id,serializer.serialize(query)));
            else{
                System.err.println("illegal query received!");
                Thread.sleep(100);
            }
        }
        catch (TException e) {
            e.printStackTrace();
            reconnectIfNecessary(1000);
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }


    }

    private boolean connectToThriftServer() {

        transport = new TSocket(thriftServerIp, thriftServerPort);
        try{
            transport.open();

            TProtocol protocol = new TBinaryProtocol(transport);

            thriftClient = new QueryService.Client(protocol);

            return true;

        }
        catch (TTransportException e) {
            e.printStackTrace();
            return false;
        }
    }

    private void reconnectIfNecessary(int delayInMillis) {

        try {
            Thread.sleep(delayInMillis);
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }

//        if(thriftClient == null || transport == null || !transport.isOpen()) {
            if (connectToThriftServer())
                System.out.println("ThriftServer is reconnected!");
            else
                System.out.println("Failed to reconnect to the ThriftServer!");
//        }
    }
}
