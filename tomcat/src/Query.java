


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
        String location = request.getParameter("location");
        String time = request.getParameter("time");
        PrintWriter out = response.getWriter();

        if(location==null && time ==null){
            out.println("Illegal URL format.");
            out.print("it should be http://xxx/transport/query?location=arg1&time=arg2");
            return;
        }





        JSONObject jsonObject = new JSONObject();
        try {
            if(fakeAnswer.containsKey(location)) {
                jsonObject.put("value",fakeAnswer.get(location)*Integer.parseInt(time));
            }
        }
        catch (Exception e) {
            out.print("error!");
        }

        out.print(jsonObject.toString());
    }
}
