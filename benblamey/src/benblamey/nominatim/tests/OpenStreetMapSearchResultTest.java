package benblamey.nominatim.tests;

import benblamey.nominatim.OpenStreetMapElementKind;
import benblamey.nominatim.OpenStreetMapSearch;
import benblamey.nominatim.OpenStreetMapSearchAlgorithmOptions;
import benblamey.nominatim.OpenStreetMapSearchResult;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import junit.framework.Assert;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * OSM search tests.
 *
 * @author Ben Blamey ben@benblamey.com
 *
 */
public class OpenStreetMapSearchResultTest {

    private OpenStreetMapSearch openStreetMapSearch;

    @Before
    public void setUp() throws Exception {
        openStreetMapSearch = new OpenStreetMapSearch(new OpenStreetMapSearchAlgorithmOptions());
    }

    @After
    public void tearDown() throws Exception {
        openStreetMapSearch.close();
    }

    @Test
    public void test() throws SQLException {

        int searchStringID = openStreetMapSearch.getSearchStringID("Wales");

        Assert.assertTrue(searchStringID > 0);

        Collection<OpenStreetMapSearchResult> searchForString = openStreetMapSearch.searchForString(searchStringID);

        Assert.assertTrue(searchForString.size() > 0);
    }

    @Test
    public void test_parent_relation() throws SQLException {

        List<Map<String, Object>> findParentRelations = openStreetMapSearch.findParentRelations(499029706L, OpenStreetMapElementKind.Node);

        "a".toString();

    }

}
