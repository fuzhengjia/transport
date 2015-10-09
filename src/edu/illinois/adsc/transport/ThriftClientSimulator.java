package edu.illinois.adsc.transport;

import edu.illinois.adsc.transport.generated.Matrix;
import edu.illinois.adsc.transport.generated.QueryService;
import edu.illinois.adsc.transport.generated.StationUpdate;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;

import java.util.List;
import java.util.Random;
import java.util.Vector;

/**
 * Created by robert on 10/9/15.
 */
public class ThriftClientSimulator {

    QueryService.Client client;
    TProtocol protocol;
    TTransport transport;

    public ThriftClientSimulator() {

        transport = new TSocket("localhost", Config.thriftPort);


        try{
            transport.open();

            protocol = new TBinaryProtocol(transport);

            client = new QueryService.Client(protocol);

        } catch (TTransportException e) {
            e.printStackTrace();
        }

    }

    public void close() {
        transport.close();
    }

    public static void main(String[] args) throws TException {
        ThriftClientSimulator simulator = new ThriftClientSimulator();
        simulator.client.pushUpdate(simulator.generateStationUpdate());
        System.out.println("A new update matrix is generated and sent to the coordinator");
    }

    private StationUpdate generateStationUpdate() {
        StationUpdate ret = new StationUpdate();
        ret.stationId = Integer.toString(new Random().nextInt());
        ret.updateMatrix = new Matrix();
        ret.updateMatrix.rows = 2;
        ret.updateMatrix.columns = 2;
        List<List<Double>> matrix = new Vector<List<Double>>();
        matrix.add(new Vector<Double>());
        matrix.add(new Vector<Double>());
        matrix.get(0).add(0.);
        matrix.get(0).add(1.0);
        matrix.get(1).add(2.);
        matrix.get(1).add(3.0);
        ret.updateMatrix.data=matrix;
        return ret;
    }
}
