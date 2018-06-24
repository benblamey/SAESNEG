package com.benblamey.nominatim;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Builds new tables to adapt the nominatim table schema for OpenStreetMap data
 * - in preparation for the search functionality.
 *
 * @author Ben Blamey ben@benblamey.com
 *
 */
public class BuildNewTablesMain {

    private static Connection _con;
    private static Statement createStatement;

    static {
        org.postgresql.Driver d = new org.postgresql.Driver();
        try {
            DriverManager.registerDriver(d);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) throws SQLException {

        _con = DriverManager.getConnection(
                OpenStreetMapConfig.JDBC_POSTGRESQL_NOMINATIM,
                OpenStreetMapConfig.JDBC_POSTGRESQL_NOMINATIM_USER,
                OpenStreetMapConfig.JDBC_POSTGRESQL_NOMINATIM_PASSWORD);

//		concatSearchName();
//		concatLocationAreaLarge();
        // CONCURRENTLY means don't lock.
        //runQuery(" CREATE INDEX CONCURRENTLY osm_is_index_place ON place (osm_id) ");
        //runQuery(" CREATE INDEX CONCURRENTLY osm_is_index_placex ON placex (osm_id) ");
    }

    private static void concatSearchName() throws SQLException {
        createStatement = _con.createStatement();

        runQuery("DROP TABLE IF EXISTS search_name_all");

        runQuery("CREATE TABLE search_name_all ( place_id bigint, search_rank integer, address_rank integer, name_vector integer[], centroid geometry(Geometry,4326)) ");

        int upperLimit = 250;
        for (int part_id = 1; part_id <= upperLimit; part_id++) {
            runQuery("insert into search_name_all select * from search_name_" + part_id);
        }

        runQuery(" CREATE INDEX idx_search_name_all_name_vector ON search_name_all USING gin (name_vector) ");
    }

    private static void concatLocationAreaLarge() throws SQLException {
        createStatement = _con.createStatement();

        runQuery("DROP TABLE IF EXISTS location_area_large_all");

        runQuery("CREATE TABLE location_area_large_all (partition integer,place_id bigint,country_code character varying(2),keywords integer[],rank_search integer NOT NULL,rank_address integer NOT NULL,isguess boolean,centroid geometry(Point,4326),geometry geometry(Geometry,4326)) ");

        int upperLimit = 250;
        for (int part_id = 1; part_id <= upperLimit; part_id++) {
            runQuery("insert into location_area_large_all select * from location_area_large_" + part_id);
        }

        runQuery(" CREATE INDEX idx_location_area_large_all_name_vector ON location_area_large_all USING gin (keywords) ");
    }

    private static void runQuery(String query) throws SQLException {
        System.out.println("Executing: " + query + "...");
        createStatement.execute(query);
        System.out.println("Done");
    }

}
