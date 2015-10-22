import edu.illinois.adsc.transport.ErrorCode;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Created by robert on 10/7/15.
 */
public class ErrorCodePrinter extends HttpServlet {
    @Override
    public void doGet(HttpServletRequest request,
                      HttpServletResponse response)
            throws IOException, ServletException
    {
        response.addHeader("Access-Control-Allow-Origin","*");

        PrintWriter out = response.getWriter();
        out.println("Error code:");
        for(Long key: ErrorCode.errorCodeReason.keySet()) {
            out.format("%d: %s\n", key, ErrorCode.errorCodeReason.get(key));
        }
    }
}
