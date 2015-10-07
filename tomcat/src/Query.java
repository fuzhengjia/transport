


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
        PrintWriter out = response.getWriter();

        String stationName = request.getParameter("station");
        String time = request.getParameter("time");

        if(stationName==null && time ==null){
            out.println("Illegal URL format.");
            out.print("it should be http://xxx/transport/query?Station=arg1&time=2015-4-10,13:00");
            return;
        }

        JSONObject jsonObject = new JSONObject();

        final String serverIP = "192.168.0.235";

        final int port = 20000;

        TTransport transport = new TSocket(serverIP, port);
        try{
            transport.open();

            TProtocol protocol = new TBinaryProtocol(transport);

            QueryService.Client client = new QueryService.Client(protocol);

            double result = client.getNumberOfPeople(stationName,time);

            jsonObject.put("value",result);
            out.print(jsonObject.toString());

        }
        catch (TTransportException e) {
            out.println("Failed to connect to the QueryResponseServer. Please make sure the server is started and the port and address are "+serverIP+":"+port);
            return;
        }
        catch (TException e) {
            transport.close();
            e.printStackTrace(out);
        }
        catch (JSONException e) {
            e.printStackTrace(out);
        }

    }
}
