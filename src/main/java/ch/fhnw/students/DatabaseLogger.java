package ch.fhnw.students;

import java.net.URL;
import java.sql.*;
import java.util.Map;

public class DatabaseLogger {


    private static final String DATABASE_URL = "jdbc:mysql://192.168.111.4:3306/db_group2";
    private static final Map<String, String> env = System.getenv();
    private static Connection connection;

    public static void main(String[] args) {

    }

    public static void createConnection() {
        try {
            connection = DriverManager.getConnection(DATABASE_URL, env.get("MYSQL_USER"), env.get("MYSQL_PASSWORD"));
            // SQL Code to create the table if it doesn't exist in the DB
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static void dbInit() throws SQLException {
        Statement statement = connection.createStatement();
        statement.execute("CREATE TABLE IF NOT EXISTS logging_group2_case2 (\n" +
                "\tlogging_id INT PRIMARY KEY auto_increment,\n" +
                "    order_id VARCHAR(255) NOT NUll,\n" +
                "    customer_id VARCHAR(255) NOT NUll,\n" +
                "    destination_country VARCHAR(255) NOT NUll,\n" +
                "    shipping_weight DOUBLE NOT NULL,\n" +
                "    delivery_type VARCHAR(255) NOT NULL\n" +
                ");");
    }

    public static void logging(String orderId, String customerId, String destination, double weight, String deliveryType) throws SQLException {
        PreparedStatement ps = connection.prepareStatement("INSERT INTO logging_group2_case2 (" +
                "order_id, customer_id, destination_country, shipping_weight, delivery_type) VALUES (" +
                "?,?,?,?,?)");

        ps.setString(1, orderId);
        ps.setString(2, customerId);
        ps.setString(3, destination);
        ps.setDouble(4, weight);
        ps.setString(5, deliveryType);

        ps.execute();
    }
}
