package edu.illinois.adsc.transport.coordinator;

import edu.illinois.adsc.transport.Config;
import edu.illinois.adsc.transport.generated.QueryService;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;

/**
 * Created by robert on 10/14/15.
 */
public class CoordinatorClient {
    public QueryService.Client client;
    TProtocol protocol;
    TTransport transport;

    public boolean connect() {

        transport = new TSocket("localhost", Config.thriftPort);


        try{
            transport.open();

            protocol = new TBinaryProtocol(transport);

            client = new QueryService.Client(protocol);

        } catch (TTransportException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public void close() {
        transport.close();
    }
}
