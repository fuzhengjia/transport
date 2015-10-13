package edu.illinois.adsc.transport;

import backtype.storm.utils.DRPCClient;
import backtype.storm.utils.Utils;
import edu.illinois.adsc.transport.generated.*;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TThreadPoolServer;
import org.apache.thrift.transport.TServerSocket;
import org.apache.thrift.transport.TServerTransport;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by robert on 10/6/15.
 */
public class DRPCQueryResponser implements QueryService.Iface{

    class Result {
        public Result(){
            sema = new Semaphore(0);
        }
        Semaphore sema;
        QueryResult queryResult;
    }

    private Map<Long,Result> queryResults = new ConcurrentHashMap<Long, Result>();

    private BlockingQueue<Query> pendingQueries = new LinkedBlockingQueue<Query>();

    private QueryIdGenerator queryIdGenerator = new QueryIdGenerator();

    private BlockingQueue<StationUpdate> pendingUpdateMatrix = new LinkedBlockingQueue<StationUpdate>();

    private Semaphore pendingUpdateMatrixMutex = new Semaphore(0);


//    public static void main(String[] args) {
//
//        System.out.println("Note that this must be executed using #storm jar xxx.jar xxx command.");
//        try {
//            Map conf ;
//            conf = Utils.readDefaultConfig();
//
//            DRPCClient drpcClient = new DRPCClient(conf,"192.168.0.31",3772);
//            for(String s:args) {
//                System.out.format("%s--->%s\n",s,drpcClient.execute("exclamation",s));
//            }
//            drpcClient.close();
//        }
//        catch (Exception e) {
//            System.out.println("Error!");
//            e.printStackTrace();
//        }
//    }

    @Override
    public long getNumberOfPeople(String stationID, String timeStamp) throws TException {
        Long queryId = queryIdGenerator.generateId();
        long ret;
        System.out.println("User submitted a query!");

        try {
            pendingQueries.put(new Query(queryId, stationID, timeStamp));
            queryResults.put(queryId, new Result());
            System.out.println("Waiting fot the query result!");
            if(queryResults.get(queryId).sema.tryAcquire(2, TimeUnit.SECONDS)) {
                ret = queryResults.get(queryId).queryResult.result;
                queryResults.remove(queryId);
                return ret;
            }
            else {
                queryResults.remove(queryId);
                System.out.println("Timeout!");
                return ErrorCode.Timeout;
            }
        }
        catch (InterruptedException e) {
            e.printStackTrace();
            return ErrorCode.Interrupted;
        }
    }

    /**
     * This function is called by the spout of the topology to take an input query
     * for execution. When there is no queries in pendingQueries, the function
     * will be blocked until new queries are available.
     * @return the query
     * @throws TException
     */

    @Override
    public Query takeQuery() throws TException {
        System.out.println("topology tried to fetch a query!");
        try {
        Query query = pendingQueries.take();
        System.out.println("a query is return to the topology!");
        System.out.format("%d pending queries", pendingQueries.size());
        return query;
        }
        catch (InterruptedException e) {
            e.printStackTrace();
            return new Query(-1,"","");
        }
    }


    /**
     * This function is called by the final bolt in the topology, when the query
     * evaluation is finished.
     * This function puts the result in the hash table and updates the sema, such
     * that the thread waiting the result can resume to execution.
     * @param result query result computed by the topology.
     * @throws TException
     */
    @Override
    public void finishQuery(QueryResult result) throws TException {
        System.out.println("Topology returns query result!");
        Long queryId = result.query_id;
        if (queryResults.containsKey(queryId)){
            queryResults.get(queryId).queryResult = result;
            queryResults.get(queryId).sema.release();
        }
        else{
            System.out.format("Query [%d] is timeout!\n", queryId);
        }
    }

    @Override
    public StationUpdate fetchStateUpdate() throws TException {
        try {
            pendingUpdateMatrixMutex.acquire();
            return pendingUpdateMatrix.take();
        }
        catch (InterruptedException e) {
            e.printStackTrace();
            return null;
        }

    }

    @Override
    public void pushUpdate(StationUpdate update) throws TException {
        pendingUpdateMatrix.add(update);
        pendingUpdateMatrixMutex.release();
        System.out.format("new update to location [%s] at time [%s]",update.getStationId(),update.getTimeStamp());
    }

    private class QueryIdGenerator {
        AtomicLong id = new AtomicLong(0);
        long generateId(){
            return id.getAndIncrement();
        }

    }

    public static void main(String [] args) throws TException {
        String stationId = "";
        String time = "";
        if(args.length == 0){
            stationId = "34";
            time = "2015-4-10,14:33:23";
        }
        else{
            stationId = args[0];
            time = args[1];
        }
        final String ip = "192.168.0.235";
        final int port = Config.thriftPort;

        TTransport transport = new TSocket(ip, port);

        try{
            transport.open();

            TProtocol protocol = new TBinaryProtocol(transport);

            QueryService.Client client = new QueryService.Client(protocol);

            long startTime = System.currentTimeMillis();

            long result = client.getNumberOfPeople(stationId, time);


            System.out.format("Station[%s]:\t %s\n", stationId, result);
            System.out.format("Delay: %5.5fms\n", (System.currentTimeMillis()-startTime)/(double)1000);
        }
        catch (TException e) {
            e.printStackTrace();
        }

    }

    public static void startServer(String[] args) {
        System.out.println("Inside arguments:");
        for(String a: args) {
            System.out.println(a);
        }
        try {
            DRPCQueryResponser queryResponseServer = new DRPCQueryResponser();
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
