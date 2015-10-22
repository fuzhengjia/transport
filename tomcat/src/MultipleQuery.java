import edu.illinois.adsc.transport.Config;
import edu.illinois.adsc.transport.common.QueryType;
import edu.illinois.adsc.transport.generated.QueryService;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;
import org.json.JSONException;
import org.json.JSONObject;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Time;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by robert on 10/7/15.
 */
public class MultipleQuery extends HttpServlet {

    private QueryService.Client client;

    private PrintWriter out;

    private TTransport transport;

    @Override
    public void doGet(HttpServletRequest request,
                      HttpServletResponse response)
            throws IOException, ServletException
    {
        response.addHeader("Access-Control-Allow-Origin","*");
        out = response.getWriter();

        String stationName = request.getParameter("station");
        String time = request.getParameter("time");
        String delta = request.getParameter("delta");
        String type = request.getParameter("type");

        if(stationName==null || time ==null || delta == null || type == null){
            out.println("Illegal URL format.");
            out.print("it should be http://xxx/transport/multiplequery?type=1&station=22&time=2015-4-10,13:00&delta=3,18,31");
            return;
        }

        connectToThriftServer();

        JSONObject jsonObject = new JSONObject();

        String date ="";
        int hour = 0;
        int min = 0;


        Pattern p = Pattern.compile( "([0-9]+-[0-9]+-[0-9]+),([0-9]+):([0-9]+):([0-9]+)" );
        Matcher m = p.matcher(time);

        if(m.find()) {
            date = m.group(1);
            hour = Integer.parseInt(m.group(2));
            min = Integer.parseInt(m.group(3));
        }


        Pattern p2 = Pattern.compile( "([0-9|,])+" );
        Matcher m2 = p2.matcher(delta);

        String deltasSection = "";
        if(m2.find()) {
            deltasSection = m2.group();
        }



        String[] deltas = deltasSection.split(",");


        long query_type = Long.parseLong(type);

        if(!QueryType.validQueryTyep(query_type)) {
            out.print("illegal query type:"+query_type);
            return;
        }

        for(String point: deltas) {
            int d = Integer.parseInt(point);
            min = d + min;
            if(min>=60) {
                min = min%60;
                hour++;
            }
            try{
                String tmpTime = date+","+hour+":"+min;
                List<Long> result = client.query(stationName,tmpTime,query_type);

                convertResultToJson(jsonObject,query_type,result,d);
//                for(Long l: result) {
//                    jsonObject.put(Integer.toString(d), l);
//                }
            }
            catch (TException e) {
                transport.close();
                e.printStackTrace(out);
            }
            catch (JSONException e) {
                e.printStackTrace(out);
            }
        }

        out.print(jsonObject.toString());

    }
    boolean connectToThriftServer() {

        final String serverIP = Config.thriftIp;

        final int port = Config.thriftPort;

        transport = new TSocket(serverIP, port);
        try{
            transport.open();

            TProtocol protocol = new TBinaryProtocol(transport);

            client = new QueryService.Client(protocol);

            return true;
        }
        catch (TTransportException e) {
            out.println("Failed to connect to the QueryResponseServer. Please make sure the server is started and the port and address are "+serverIP+":"+port);
            return false;
        }
    }

    private void convertResultToJson(JSONObject jsonObject, long type, List<Long> values, int duration) throws JSONException{
        switch((int)type){
            case (int)QueryType.StationCrowd:
                jsonObject.put(Integer.toString(duration), "" + values.get(0));
                break;
            case (int)QueryType.StationWaiting:
                jsonObject.put(Integer.toString(duration), "" + values.get(0)+","+values.get(1));
                break;
            case (int)QueryType.TrainCrowd:
                jsonObject.put(Integer.toString(duration), "" + values.get(0)+","+values.get(1));
                break;
        }
    }

}
