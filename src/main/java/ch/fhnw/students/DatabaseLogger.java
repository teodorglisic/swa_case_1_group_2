package ch.fhnw.students;

import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;

public class DatabaseLogger {


    private static final String DATABASE_URL = "jdbc:mysql://192.168.111.4:3306/db_group2";
    private static final Map<String, String> env = System.getenv();

    public static void main(String[] args) {
        try (Connection connection = DriverManager.getConnection(DATABASE_URL, env.get("MYSQL_USER"), env.get("MYSQL_PASSWORD"));){
            Statement statement = connection.createStatement();


//          Code for testing the connection
//          statement.execute("CREATE TABLE IF NOT EXISTS test (" +
//                    "id INT PRIMARY KEY NOT NULL," +
//                    "client_name VARCHAR(255) NOT NULL)");

            // SQL Code to create the table if it doesn't exist in the DB
            dbInit(statement);
            connection.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }

    public static void dbInit(Statement statement) throws SQLException {
        statement.execute("CREATE TABLE IF NOT EXISTS logging_group2_case2 (\n" +
                "\tlogging_id INT PRIMARY KEY auto_increment,\n" +
                "    order_id VARCHAR(255) NOT NUll,\n" +
                "    customer_id VARCHAR(255) NOT NUll,\n" +
                "    destination_country VARCHAR(255) NOT NUll,\n" +
                "    shipping_weight DOUBLE NOT NULL,\n" +
                "    delivery_type VARCHAR(255) NOT NULL\n" +
                ");");
    }
}
