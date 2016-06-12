package benblamey.saesneg.phaseA.image;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.WriteConcern;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

public class ImageMetadataCache {

    private static DBCollection _collection;
    
    private static Map<String,Object> _cache = new HashMap<String, Object>();

    public ImageMetadataCache() {
        _collection = _client.getDB("BenTimeline2").getCollection(ImageMetadataCache.class.getCanonicalName());
    }
    
    private static final MongoClient _client;
    
    static {
        try {
            _client = new MongoClient("localhost");
        } catch (UnknownHostException e) {
            throw new RuntimeException("localhost not found?!");
        }
        _client.setWriteConcern(WriteConcern.SAFE);
    }

    private final static String IMAGE_ID = "image_id"; // URL or filesystem path.

    public void put(String image_id, String metadata_key, Object metadata_value) {

        _cache.put(image_id + metadata_key, metadata_value);
        
        BasicDBObject query = new BasicDBObject(IMAGE_ID, image_id);
        DBObject user = _collection.findOne(query);

        if (user == null) {
            query.append(metadata_key, metadata_value);
            _collection.insert(query);
        } else {
            _collection.update(query, new BasicDBObject("$set",
                    new BasicDBObject(metadata_key, metadata_value)));
        }
    }

    public Object get(String image_id, String metadata_key) {

        Object result;
        
        result = _cache.get(image_id + metadata_key);
        if (result != null) {
            return result;
        }
        
        BasicDBObject query = new BasicDBObject(IMAGE_ID, image_id);
        DBObject user = _collection.findOne(query);

        if (user == null) {
            return null;
        }

        result = user.get(metadata_key);

        return result;
    }
}
