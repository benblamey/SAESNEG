/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.benblamey.core.facebook;

import com.benblamey.core.MySQLWebCache;
import com.restfb.DefaultWebRequestor;
import java.io.IOException;
import java.net.HttpURLConnection;

/**
 * A @see com.restfb.DefaultWebRequestor that requests text/turtle, and uses the
 * MySQL-based cache.
 *
 * @author ben
 */
public class RDFFacebookRequestor extends DefaultWebRequestor {

    public static final String s_turtleMime = "text/turtle";

    protected void customizeConnection(HttpURLConnection connection) {
        connection.addRequestProperty("accept", s_turtleMime);
    }

    @Override
    public Response executeGet(String url) throws IOException {
        String cacheResult = MySQLWebCache.internalGetMySQL(url, s_turtleMime);

        if (cacheResult != null) {
            return new Response(200, cacheResult);
        }

        Response response = super.executeGet(url);

        if (response.getStatusCode() == 200) {
            MySQLWebCache.Put(url, response.getBody(), s_turtleMime);
        }

        return response;
    }
}
