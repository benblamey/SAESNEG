package benblamey.core;

import com.benblamey.core.ExceptionHandler;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class WebCache404 {

    private static final String Database = "jdbc:mysql://localhost/facebook_cache?useUnicode=true&characterEncoding=utf-8";
    private static final String Username = "root";
    private static final String Password = "";
    private static Connection _connection;

    static {
        // Clear out some stale entries from the cache (not too many so we don't slow down too much).
//        try {
//            Connection connection = databasecache.getDatabaseConnection();
//            Statement statement = connection.createStatement();
//            PreparedStatement prepareStatement = connection.prepareStatement("DELETE FROM `data` WHERE DATEDIFF(CURDATE(),`date`) >= ? LIMIT 1000");
//            prepareStatement.setInt(1, databasecache.CacheExpiryDays); // Parameter indices are 1-based.
//            prepareStatement.execute();
//            prepareStatement.close();
//        } catch (SQLException ex) {
//            Logger.getLogger(RDFFacebookRequestor.class.getName()).log(Level.SEVERE, null, ex);
//        }
    }

    public static Connection getDatabaseConnection() throws SQLException {
        if (_connection == null) {
            _connection = DriverManager.getConnection(Database, Username, Password);
            _connection.prepareStatement("SET CHARACTER SET utf8;").execute();
        }

        return _connection;
    }
    public static boolean is404(String url) {
        try {
            Connection connection = MySQLWebCache.getDatabaseConnection();
            PreparedStatement prepareStatement = connection.prepareStatement("SELECT * FROM `cache_404` WHERE (`url` = ?) LIMIT 1");
            prepareStatement.setString(1, url); // Parameter indices are 1-based.
            ResultSet executeQuery = prepareStatement.executeQuery();
            if (executeQuery.first()) {
                return true;
            }
            return false;
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }

    public static void put404(String url) {
        try {
            Connection connection = MySQLWebCache.getDatabaseConnection();
            PreparedStatement prepareStatement = connection.prepareStatement("INSERT INTO `cache_404` (`url`) VALUES (?)");
            prepareStatement.setString(1, url); // Parameter indices are 1-based.
            prepareStatement.execute();
        } catch (SQLException ex) {
            ExceptionHandler.handleException(ex);
        }
    }

    private WebCache404() {
    }
    
}
