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

@WebServlet(urlPatterns = "/GetStudentById")
public class GetStudentById extends HttpServlet {
    static final String JDBC_DRIVER = "com.mysql.cj.jdbc.Driver";
    static final String DB_URL = "jdbc:mysql://180.76.185.143/pages";
    static final String USER = "root";
    static final String PASS = "1234aBc@";
    static final String SQL_QURERY_STUDENT_BY_ID = "SELECT id, name, age FROM student WHERE id=?";
    static final String REDIS_URL = "127.0.0.1";

    static Connection conn = null;
    static Jedis jedis = null;

    public void init() {
        try {
            Class.forName(JDBC_DRIVER);
            conn = DriverManager.getConnection(DB_URL, USER, PASS);
            jedis = new Jedis(REDIS_URL);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void destroy() {
        try {
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        getServletContext().log(request.getParameter("id"));

        String json = jedis.get(request.getParameter("id"));

        if (json != null) {
            Student stu = getStudent(Integer.parseInt(request.getParameter("id")));

            Gson gson = new Gson();
            json = gson.toJson(stu, new TypeToken<Student>() {
            }.getType());

            jedis.set(request.getParameter("id"), json);
            out.println(json);

        } else {
            out.println(0);
        }
        out.flush();
        out.close();
    }

    public Student getStudent(int id) {
        Student stu = new Student();
        PreparedStatement stmt = null;
        try {
            stmt = conn.prepareStatement(SQL_QURERY_STUDENT_BY_ID);
            stmt.setInt(1, id);

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                stu.id = rs.getInt("id");
                stu.name = rs.getString("name");
		stu.age = rs.getInt("age");
            }

            rs.close();
            stmt.close();
        } catch (SQLException se) {
            se.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (stmt != null)
                    stmt.close();
            } catch (SQLException se) {
                se.printStackTrace();
            }
        }

        return stu;

    }

}

class Student {
    public int id;
    public String name;
    public int age;
}

