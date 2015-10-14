import edu.illinois.adsc.transport.ErrorCode;
import edu.illinois.adsc.transport.coordinator.CoordinatorClient;
import org.apache.thrift.TException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Created by robert on 10/14/15.
 */
public class SystemTime extends HttpServlet {
    @Override
    public void doGet(HttpServletRequest request,
                      HttpServletResponse response)
            throws IOException, ServletException
    {
        PrintWriter out = response.getWriter();

        CoordinatorClient coordinatorClient = new CoordinatorClient();
        if(!coordinatorClient.connect())
            out.println("Failed to connect to the coordinator! Please check the coordinator daemon is started");
        try {
            String time = coordinatorClient.client.getCurrentTime();
            out.print(time);
        }
        catch (TException e) {
            e.printStackTrace(out);
        }
    }
}
