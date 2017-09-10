package com.benblamey.core;

import com.benblamey.core.ExceptionHandler;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Utility methods for the MySQL database that backs the HTTP cache.
 *
 * @author ben
 */
public class MySQLWebCache {

    private static final String Database = "jdbc:mysql://localhost/facebook_cache?useUnicode=true&characterEncoding=utf-8";
    private static final String Username = "root";
    private static final String Password = "";
    public static final Integer CacheExpiryDays = 10;
    private static Connection _connection;

    static {
        try {
            Class.forName("com.mysql.jdbc.Driver").newInstance();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }

        //Class.forName("com.mysql.jdbc.Driver").newInstance();
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
            _connection = DriverManager.getConnection(MySQLWebCache.Database, MySQLWebCache.Username, MySQLWebCache.Password);
            _connection.prepareStatement("SET CHARACTER SET utf8;").execute();
        }

        return _connection;
    }

    public static String get(String urlstr, String mimeType) throws IOException {
        String content = internalGetMySQL(urlstr, mimeType);
        if (content == null) {

            StringBuffer buff = new StringBuffer();
            URL url = new URL(urlstr);
            BufferedReader br = new BufferedReader(new InputStreamReader(url.openConnection().getInputStream()));
            int c;
            while ((c = br.read()) != -1) {
                buff.append((char) c);
            }
            br.close();
            content = buff.toString();

            Put(urlstr, content, mimeType);
        }
        return content;
    }

    public static String internalGetMySQL(String url, String mimeType) {
        try {
            Connection connection = MySQLWebCache.getDatabaseConnection();
            PreparedStatement prepareStatement = connection.prepareStatement("SELECT `data` FROM `data` WHERE (`url` = ? AND DATEDIFF(CURDATE(),`date`) < ? AND `mime_type` = ?) LIMIT 1");
            prepareStatement.setString(1, url); // Parameter indices are 1-based.
            prepareStatement.setInt(2, MySQLWebCache.CacheExpiryDays);
            prepareStatement.setString(3, mimeType);
            ResultSet executeQuery = prepareStatement.executeQuery();
            if (executeQuery.first()) {
                String body = executeQuery.getString("data");
                return body;
            }
            return null;
        } catch (SQLException ex) {
            ExceptionHandler.handleException(ex);
        }
        return null;
    }

    public static void Put(String url, String body, String mimeType) {
        try {
            Connection connection = MySQLWebCache.getDatabaseConnection();

            PreparedStatement prepareStatement = connection.prepareStatement("INSERT INTO `data` (`url`,`date`,`data`,`mime_type`) VALUES (?,CURDATE(),?,?)");
            prepareStatement.setString(1, url); // Parameter indices are 1-based.
            prepareStatement.setString(2, body);
            prepareStatement.setString(3, mimeType);
            prepareStatement.execute();
        } catch (SQLException ex) {
            ExceptionHandler.handleException(ex);
        }
    }

    private MySQLWebCache() {
    }
}
