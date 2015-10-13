import edu.illinois.adsc.transport.Config;
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
        out = response.getWriter();

        String stationName = request.getParameter("station");
        String time = request.getParameter("time");
        String delta = request.getParameter("delta");

        if(stationName==null || time ==null || delta == null){
            out.println("Illegal URL format.");
            out.print("it should be http://xxx/transport/multiplequery?station=22&time=2015-4-10,13:00&delta=3,18,31");
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



        for(String point: deltas) {
            int d = Integer.parseInt(point);
            min = d + min;
            if(min>=60) {
                min = min%60;
                hour++;
            }
            try{
                String tmpTime = date+","+hour+":"+min;
                long result = client.getNumberOfPeople(stationName,tmpTime);
                jsonObject.put(Integer.toString(d), result);
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

        final String serverIP = "192.168.0.235";

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
}
