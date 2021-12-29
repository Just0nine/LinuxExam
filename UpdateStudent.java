import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.WebServlet;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import redis.clients.jedis.Jedis;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;

@WebServlet(urlPatterns = "/UpdateStudent")
public class UpdateStudent extends HttpServlet {
    static final String JDBC_DRIVER = "com.mysql.cj.jdbc.Driver";
    static final String URL = "jdbc:mysql://180.76.185.143/pages";
    static final String USER = "root";
    static final String PASS = "1234aBc@";
    static final String SQL_UPDATE_STUDENT = "UPDATE student SET name=?,age=? WHERE id=?";
 
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
      response.setContentType("application/json");
      response.setCharacterEncoding("UTF-8");

      Student req = getRequestBody(request);
      getServletContext().log(req.toString());
      PrintWriter out = response.getWriter();

      out.println(updateStudent(req));
      out.flush();
      out.close();
   }

   private Student getRequestBody(HttpServletRequest request) throws IOException {
      Student note = new Student();
      StringBuffer bodyJ = new StringBuffer();
      String line = null;
      BufferedReader reader = request.getReader();
      while ((line = reader.readLine()) != null)
         bodyJ.append(line);
      Gson gson = new Gson();
      note = gson.fromJson(bodyJ.toString(), new TypeToken<Student>() {
      }.getType());
      return note;
   }

   private int updateStudent(Student req) {
      Connection conn = null;
      PreparedStatement stmt = null;
      int retcode = -1;
      try {
         Class.forName(JDBC_DRIVER);
         conn = DriverManager.getConnection(URL, USER, PASS);
         stmt = conn.prepareStatement(SQL_UPDATE_STUDENT);

         stmt.setString(1, req.name);
         stmt.setInt(2, req.age);
	 stmt.setInt(3,req.id);
         int row = stmt.executeUpdate();
         if (row > 0)
            retcode = 1;

         stmt.close();
         conn.close();
      } catch (SQLException se) {
         se.printStackTrace();
      } catch (Exception e) {
         e.printStackTrace();
      } finally {
         try {
            if (stmt != null)
               stmt.close();
            if (conn != null)
               conn.close();
         } catch (SQLException se) {
            se.printStackTrace();
         }
      }
      return retcode;
   }
}

class Student {
    public int id;
    public String name;
    public int age;
}

