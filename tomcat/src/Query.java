


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
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class Query extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private static HashMap<String,Integer> fakeAnswer;
    static
    {
        fakeAnswer = new HashMap<String, Integer>();
        fakeAnswer.put("a", 44);
        fakeAnswer.put("b", 22);
        fakeAnswer.put("c", 15);
    }

    @Override
    public void doGet(HttpServletRequest request,
                      HttpServletResponse response)
            throws IOException, ServletException
    {
        response.addHeader("Access-Control-Allow-Origin","*");

        PrintWriter out = response.getWriter();

        String stationName = request.getParameter("station");
        String time = request.getParameter("time");
        String type = request.getParameter("type");

        if(stationName==null || time ==null || type ==null){
            out.println("Illegal URL format.");
            out.print("it should be http://xxx/transport/query?type=1&Station=arg1&time=2015-4-10,13:00");
            return;
        }

        JSONObject jsonObject = new JSONObject();

        final String serverIP = Config.thriftIp;

        final int port = Config.thriftPort;

        TTransport transport = new TSocket(serverIP, port);

        long query_type = Long.parseLong(type);
        if(!QueryType.validQueryTyep(query_type)) {
            out.format("Illegal query type: %d",query_type);
            return;
        }

        try{
            transport.open();

            TProtocol protocol = new TBinaryProtocol(transport);

            QueryService.Client client = new QueryService.Client(protocol);

            List<Long> result = client.query(stationName,time,query_type);

//            jsonObject.put("value",result);
            convertResultToJson(jsonObject,query_type,result);

            out.print(jsonObject.toString());

        }
        catch (TTransportException e) {
            out.println("Failed to connect to the QueryResponseServer. Please make sure the server is started and the port and address are "+serverIP+":"+port);
        }
        catch (TException e) {
            transport.close();
            e.printStackTrace(out);
        }
        catch (JSONException e) {
            e.printStackTrace(out);
        }

    }
    private void convertResultToJson(JSONObject jsonObject, long type, List<Long> values) throws JSONException{
        switch((int)type){
            case (int)QueryType.StationCrowd:
                jsonObject.put("values", "" + values.get(0));
                break;
            case (int)QueryType.StationWaiting:
                jsonObject.put("values", "" + values.get(0)+","+values.get(1));
                break;
            case (int)QueryType.TrainCrowd:
                jsonObject.put("values", "" + values.get(0)+","+values.get(1));
                break;
        }
    }
}
