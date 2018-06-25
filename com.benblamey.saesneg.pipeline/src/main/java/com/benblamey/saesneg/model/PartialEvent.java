package com.benblamey.saesneg.model;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import java.util.ArrayList;
import java.util.List;

/**
 * A partially reconstructed event, where datums themselves are not actually
 * loaded from persisted state. Used in circumstances such as reporting, so that
 * the overhead of reading the XML can be avoided.
 *
 * @author Ben Blamey blamey.ben@gmail.com
 *
 */
public class PartialEvent {

    List<Long> _objects = new ArrayList<>();
    public String note = "";
    private String _userEditableName;

    /**
     * Reconstruct the event based on ground-truth JSON description.
     *
     * @param the JSON represention of the event.
     */
    public PartialEvent(BasicDBObject obj) { // Construct from JSON.
        _userEditableName = obj.getString("userEditableName");
        note = obj.getString("note");
        BasicDBList datumIDs = (com.mongodb.BasicDBList) obj.get("datums");
        if (datumIDs != null) {
            for (Object datumIDObj : datumIDs) {
                String datumID = ((BasicDBObject) datumIDObj).getString("id");

                // Trim the old global-ID prefix.
                if (datumID.startsWith("fb_")) {
                    datumID = datumID.substring(3);
                }

                _objects.add(Long.parseLong(datumID));
            }
        }

    }

    public List<Long> getDatumIDs() {
        return _objects;
    }

    public String getUserEditableName() {
        return _userEditableName;
    }

    public void setUserEditableName(String userEditableName) {
        _userEditableName = userEditableName;
    }

}
