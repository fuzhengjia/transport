package edu.illinois.adsc.transport.coordinator;

import edu.illinois.adsc.transport.Config;
import edu.illinois.adsc.transport.ErrorCode;
import edu.illinois.adsc.transport.common.QueryType;
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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
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

    private Calendar currentTimeStamp;

    private SimpleDateFormat simpleDateFormat;


    public DRPCQueryResponser() throws TException {
        this("2015-4-24,05:00");
    }

    public DRPCQueryResponser(String time) throws TException {
        simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd,HH:mm");
        setTimeStamp(time);
    }

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
    public List<Long> query(String stationID, String timeStamp, long queryType) throws TException {
        Long queryId = queryIdGenerator.generateId();
        System.out.println("User submitted a query!");
        List<Long> ret = new Vector<Long>();
        try {
            pendingQueries.put(new Query(queryId, queryType,stationID, timeStamp));
            queryResults.put(queryId, new Result());
            System.out.println("Waiting fot the query result!");
            if(queryResults.get(queryId).sema.tryAcquire(2, TimeUnit.SECONDS)) {
                ret.add(queryResults.get(queryId).queryResult.result);
                ret.add(queryResults.get(queryId).queryResult.result2);

//                ret = queryResults.get(queryId).queryResult.result;
                queryResults.remove(queryId);
                return ret;
            }
            else {
                queryResults.remove(queryId);
                System.out.println("Timeout!");
                ret.add(ErrorCode.Timeout);
                ret.add(ErrorCode.Timeout);
                return ret;
            }
        }
        catch (InterruptedException e) {
            e.printStackTrace();
            ret.add(ErrorCode.Interrupted);
            ret.add(ErrorCode.Timeout);
            return ret;
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
            return new Query(-1,-1,"","");
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

//        simpleDateFormat.format(update.timeStamp).(currentTimeStamp.getTime())


        Calendar updateTime = Calendar.getInstance();
        try{
            updateTime.setTime(simpleDateFormat.parse(update.timeStamp));
            while (currentTimeStamp.before(updateTime)){
                Thread.sleep(1000);
            }
            pendingUpdateMatrix.add(update);
            pendingUpdateMatrixMutex.release();
            System.out.format("new update to location [%s] at time [%s]",update.getStationId(),update.getTimeStamp());
        } catch (ParseException e) {

        } catch (InterruptedException e ) {

        }



    }

    @Override
    public void pushUpdateForce(StationUpdate update) throws TException {
        pendingUpdateMatrix.add(update);
        pendingUpdateMatrixMutex.release();
        System.out.format("new update to location [%s] at time [%s]",update.getStationId(),update.getTimeStamp());
    }

    @Override
    public String getCurrentTime() throws TException {
        return simpleDateFormat.format(currentTimeStamp.getTime());
    }

    @Override
    public boolean setTimeStamp(String time) throws TException {
        try {
            currentTimeStamp = Calendar.getInstance();
            currentTimeStamp.setTime(simpleDateFormat.parse(time));
            System.out.println("The time is set to "+time);
        } catch (ParseException e ) {
            System.err.println("Illegal data format. The data format should be in the form of yyyy-MM-DD,hh:mm");
            return false;
        }
        return true;
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
        final String ip = Config.thriftIp;
        final int port = Config.thriftPort;

        TTransport transport = new TSocket(ip, port);

        try{
            transport.open();

            TProtocol protocol = new TBinaryProtocol(transport);

            QueryService.Client client = new QueryService.Client(protocol);

            long startTime = System.currentTimeMillis();

            List<Long> result = client.query(stationId, time, 0L);


            System.out.format("Station[%s]:\t %s\n", stationId, result.get(0));
            System.out.format("Delay: %5.5fms\n", (System.currentTimeMillis()-startTime)/(double)1000);
        }
        catch (TException e) {
            e.printStackTrace();
        }

    }

    public static void startServer(String timestamp) {
        try {
            DRPCQueryResponser queryResponseServer;
            if (timestamp != null) {
                queryResponseServer = new DRPCQueryResponser(timestamp);
            }
            else {
                queryResponseServer = new DRPCQueryResponser();
            }
            QueryService.Processor processor = new QueryService.Processor(queryResponseServer);

            TServerTransport serverTransport = new TServerSocket(Config.thriftPort);
            TServer server = new TThreadPoolServer(new TThreadPoolServer.Args(serverTransport).processor(processor));

            System.out.println("The coordinator daemon is serving...");
            server.serve();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
