import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
public class DbUtil {

    private static Connection connection = null;

    public static Connection getConnection() {
        if (connection != null)
            return connection;
        else {
            try {
                connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/UserDB","root", "");
            } catch(SQLException e)
            {
                System.out.println(e);
            }
            return connection;
        }

    }
}