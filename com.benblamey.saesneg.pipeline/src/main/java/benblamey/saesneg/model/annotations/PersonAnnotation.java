package com.benblamey.saesneg.model.annotations;

import com.benblamey.saesneg.model.LifeStory;
import com.restfb.types.CategorizedFacebookType;
import com.restfb.types.Event;
import com.restfb.types.NamedFacebookType;
import java.util.HashMap;

public class PersonAnnotation extends Annotation {


    public String Name;
    public String FacebookID;

    boolean _resolved; // can't be private because set from anon classes in static methods below.

    private PersonAnnotation() { // Only allow constructing through static methods.
    }

    public static PersonAnnotation From(final Event.Owner data, final String note) {

        return new PersonAnnotation() {
            {
                FacebookID = data.getId();
                Name = data.getName();
                Note = note;
                SourceDataKind = DataKind.Metadata;
                _resolved = true;
            }
        };

    }

    public static PersonAnnotation From(final CategorizedFacebookType data, final String note) {

        return new PersonAnnotation() {
            {
                FacebookID = data.getId();
                Name = data.getName();
                Note = note;
                SourceDataKind = DataKind.Metadata;
                _resolved = true;
            }
        };
    }

    public static PersonAnnotation From(final NamedFacebookType data, final String note) {
        return new PersonAnnotation() {
            {
                FacebookID = data.getId();
                Name = data.getName();
                Note = note;
                _resolved = true;
                SourceDataKind = DataKind.Metadata;
            }
        };
    }

    public static PersonAnnotation fromFreeText(String originalText, HashMap<String, String> FriendIDByName, String note) {
        PersonAnnotation personAtEvent = new PersonAnnotation();

        originalText = originalText.trim();

        if (originalText.endsWith("'s")) {
            originalText = originalText.substring(0, originalText.length() - 2);
        }

        personAtEvent.setOriginalText(originalText);
        personAtEvent.Name = originalText;
        personAtEvent.SourceDataKind = DataKind.Text;
        personAtEvent.FacebookID = FriendIDByName.get(originalText);
        personAtEvent._resolved = false;
        personAtEvent.Note = note;

        return personAtEvent;
    }

    public String getFacebookID() {
        return FacebookID;
    }

    public String getOfficialName() {
        return Name;
    }

    public void resolve(LifeStory ls) {
        if (!_resolved) {

            HashMap<String, String> friendIDByName = ls.FriendIDByName;

            FacebookID = friendIDByName.get(this._originalText);

            if (FacebookID == null) {

                // FB = "<first name> <surname>"
                // Otherwise, try to match the first name, start of first name,
                // or first name and initial.
                for (String indexName : friendIDByName.keySet()) {
                    if (indexName.startsWith(this._originalText)) {
                        this.FacebookID = friendIDByName.get(this._originalText);
                        Name = indexName;
                    }
                }

            } else {
                this.Name = this._originalText;
            }

            _resolved = true;
        }
    }

        @Override
    public String toString() {
        return "PersonAtEvent, "
                + "Orig:" + (_originalText == null ? "" : _originalText)
                + " Name:" + (Name == null ? "" : Name)
                + " FacebookID:" + (FacebookID == null ? "" : FacebookID)
                + ", Note: " + (Note == null ? "" : Note);
    }



}
