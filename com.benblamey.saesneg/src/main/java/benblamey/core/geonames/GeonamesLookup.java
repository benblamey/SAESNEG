package benblamey.core.geonames;

import com.mysql.jdbc.Driver;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Lookup places in a local GeoNames SQL database.
 *
 * @author Ben Blamey ben@benblamey.com
 *
 */
public class GeonamesLookup {

    private static final String DATABASE = "jdbc:mysql://localhost/geonames";//?useUnicode=true&characterEncoding=utf-8";
    private static final String USERNAME = "";
    private static final String PASSWORD = "";
    private static Connection _connection;

    public static Connection getDatabaseConnection() throws SQLException {
        if (_connection == null) {

            try {

                // The driver functionality is broken in Tomcat - you need to register manually.
                // See: "However, the implementation is fundamentally broken in all Java versions for a servlet container environment"
                // http://tomcat.apache.org/tomcat-7.0-doc/jndi-datasource-examples-howto.html#DriverManager,_the_service_provider_mechanism_and_memory_leaks
                com.mysql.jdbc.Driver d = new Driver();
                DriverManager.registerDriver(d);

            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            _connection = DriverManager.getConnection(DATABASE, USERNAME, PASSWORD);
            _connection.prepareStatement("SET CHARACTER SET utf8;").execute();
        }

        return _connection;
    }

//    /**
//     * Search for a token.
//     *
//     * @param token
//     * @return
//     */
//    public static GeoNamesPlace get(String token) {
//        try {
//            Connection connection = getDatabaseConnection();
//            PreparedStatement prepareStatement = connection
//                    .prepareStatement("SELECT * FROM gb WHERE name = ? limit 1");
//            prepareStatement.setString(1, token); // Parameter indices are
//            // 1-based.
//
//            ResultSet executeQuery = prepareStatement.executeQuery();
//            if (executeQuery.first()) {
//                GeoNamesPlace loc = new GeoNamesPlace(executeQuery);
//                
//                loc.originalText = token;
//                loc.geonames_id = executeQuery.getInt("geonameid");
//                return loc;
//            }
//            return null;
//        } catch (SQLException ex) {
//            ExceptionHandler.handleException(ex);
//        }
//        return null;
//    }

    private GeonamesLookup() {
    }

}
