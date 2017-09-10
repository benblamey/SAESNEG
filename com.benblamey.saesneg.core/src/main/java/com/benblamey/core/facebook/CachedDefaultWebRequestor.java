package com.benblamey.core.facebook;

import com.benblamey.core.MySQLWebCache;
import com.restfb.DefaultWebRequestor;
import java.io.IOException;
import java.net.HttpURLConnection;

/**
 * A @see com.restfb.DefaultWebRequestor , which uses the MySQL-based cache.
 * cache.
 */
public class CachedDefaultWebRequestor extends DefaultWebRequestor {

    private static final String s_jsonMime = "application/json";

    protected void customizeConnection(HttpURLConnection connection) {
        connection.addRequestProperty("accept", s_jsonMime);
    }

    @Override
    public Response executeGet(String url) throws IOException {

        String cacheResult = MySQLWebCache.internalGetMySQL(url, s_jsonMime);

        if (cacheResult != null) {
            return new Response(200, cacheResult);
        }

        System.err.println("CachedDefaultWebRequestor - Doing web request: " + url);

        Response response = super.executeGet(url);

        //if (response.getStatusCode() == 200) {
        MySQLWebCache.Put(url, response.getBody(), s_jsonMime);
        // }

        return response;
    }

    @Override
    public Response executePost(String url, String parameters) throws IOException {
        System.out.println(" CachedDefaultWebRequestor - executing post");
        return super.executePost(url, parameters);
    }
}
