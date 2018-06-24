package com.benblamey.nominatim;

import com.benblamey.core.URLUtils;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Searches OpenStreetMap using the original Nominatim web/JSON API.
 *
 * @author Ben Blamey ben@benblamey.com
 */
public class LegacyNominatimOpenStreetMapSearch {

    public static void main(String[] args) {
        search("Mevagissey");
    }

    public static List<OpenStreetMapSearchResult> search(String search) {

        List<OpenStreetMapSearchResult> locs = new ArrayList<>();

        if (!search.equals("#")) {

            URL url;
            try {
                url = new URL("http://" + OpenStreetMapConfig.NOMINATIM_HOST + "/nominatim/search.php?q=" + search + "&format=jsonv2");
            } catch (MalformedURLException e) {
                // Static error.
                throw new RuntimeException(e);
            }

            String json = URLUtils.GetURL(url);
            if (json == null) {
                return locs;
            }

            try {

                JSONArray results = new JSONArray(json);

                for (int i = 0; i < results.length(); i++) {
                    JSONObject result;
                    result = (JSONObject) results.get(i);
                    OpenStreetMapSearchResult location = new OpenStreetMapSearchResult(result);
                    locs.add(location);
                }

            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        }

        return locs;
    }

}
