package edu.illinois.adsc.transport.topology;

import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichBolt;
import backtype.storm.tuple.Tuple;
import edu.illinois.adsc.transport.common.QueryType;
import edu.illinois.adsc.transport.generated.QueryResult;
import edu.illinois.adsc.transport.generated.QueryService;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;


import java.util.Map;

/**
 * This bolt is responsible for collecting the results and sending results
 * to Thrift Server
 * Created by robert on 10/6/15.
 */
public class ResultBolt extends BaseRichBolt {

    private String thriftServerIp;
    private int thriftServerPort;

    private TTransport transport;
    private QueryService.Client thriftClient;

    public ResultBolt(String ip, int port){
        thriftServerIp = ip;
        thriftServerPort = port;
    }

    @Override
    public void prepare(Map map, TopologyContext topologyContext, OutputCollector boltOutputCollector) {

        connectToThriftServer();
    }

    @Override
    public void execute(Tuple tuple) {
        long queryId = tuple.getLong(0);
        long type = tuple.getLong(1);
        long result = tuple.getLong(2);
        long result2 = tuple.getLong(3);
        QueryResult queryResult = new QueryResult(queryId, type, result,result2);
        try{
            thriftClient.finishQuery(queryResult);
        }
        catch (TException e) {
            e.printStackTrace();
            reconnectIfNecessary(1000);
        }
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {

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
