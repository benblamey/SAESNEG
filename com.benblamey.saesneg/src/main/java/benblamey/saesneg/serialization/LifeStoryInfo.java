package com.benblamey.saesneg.serialization;

import com.benblamey.core.DateUtil;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.joda.time.DateTime;
import socialworld.model.SocialWorldUser;

/**
 * Describes a life story serialized as XML on disk.
 *
 * @author Ben
 */
public class LifeStoryInfo {

    public static String LIFE_STORY_INFOS = "LIFE_STORY_INFOS";

    public static LifeStoryInfo guessFromFilename(String filename) {
        LifeStoryInfo lifeStoryInfo = new LifeStoryInfo();
        String createdUnixTime = filename.split("\\.")[0].split("_")[2];
        lifeStoryInfo.created = DateUtil.DateTimeFromUnixTime(Integer.parseInt(
                // We have the file name, with extension (no path).
                createdUnixTime
        ));
        lifeStoryInfo.note = "FAKE life story info!";
        lifeStoryInfo.success = true;
        lifeStoryInfo.filename = filename;
        return lifeStoryInfo;
    }

    public static List<LifeStoryInfo> getLifeStoryInfos(DBObject user) {

        ArrayList<LifeStoryInfo> infos = new ArrayList<LifeStoryInfo>();

        BasicDBList dbList = (BasicDBList) user.get(LIFE_STORY_INFOS);
        if (dbList != null) {
            for (Object infoObj : dbList) {
                infos.add(LifeStoryInfo.FromBasicDBObject((BasicDBObject) infoObj));
            }
        }

        return infos;
    }

    public static List<LifeStoryInfo> getLifeStoryInfos(SocialWorldUser user) {

        ArrayList<LifeStoryInfo> infos = new ArrayList<LifeStoryInfo>();

        BasicDBList dbList = (BasicDBList) user.getValue(LIFE_STORY_INFOS);
        if (dbList != null) {
            for (Object infoObj : dbList) {
                infos.add(LifeStoryInfo.FromBasicDBObject((BasicDBObject) infoObj));
            }
        }

        return infos;
    }

    /**
     * Get the latest life story descriptor. Not guaranteed to be valid.
     */
    public static LifeStoryInfo getLatestLifeStory(
            SocialWorldUser user) throws IOException {
        List<LifeStoryInfo> lifeStoryInfos = getLifeStoryInfos(user);
        if (lifeStoryInfos.size() == 0) {
            return null;
        }
        LifeStoryInfo latestInfo = null;

        for (LifeStoryInfo info : lifeStoryInfos) {
            latestInfo = ((latestInfo == null) || info.created.isAfter(latestInfo.created)) ? info : latestInfo;
        }
        return latestInfo;
    }

    /**
     * Get the latest life story descriptor. Not guaranteed to be valid.
     */
    public static LifeStoryInfo getLatestGoodLifeStory(
            SocialWorldUser user) {
        List<LifeStoryInfo> lifeStoryInfos = getLifeStoryInfos(user);
        if (lifeStoryInfos.size() == 0) {
            return null;
        }
        LifeStoryInfo latestInfo = null;

        for (LifeStoryInfo info : lifeStoryInfos) {
            if (!info.success) {
                continue;
            }
            latestInfo = ((latestInfo == null) || info.created.isAfter(latestInfo.created)) ? info : latestInfo;
        }
        return latestInfo;
    }

    public static LifeStoryInfo getLifeStoryWithCreatedTimestamp(DateTime created, SocialWorldUser user) {
        LifeStoryInfo goldInfo = null;
        List<LifeStoryInfo> lifeStoryInfos = LifeStoryInfo.getLifeStoryInfos(user);
        for (LifeStoryInfo info : lifeStoryInfos) {
            if (info.created.equals(created)) {
                goldInfo = info;
                break;
            }
        }
        return goldInfo;
    }

    public static LifeStoryInfo FromBasicDBObject(BasicDBObject obj) {
        LifeStoryInfo info = new LifeStoryInfo();
        info.created = DateUtil.DateTimeFromUnixTime(obj.getInt("CREATED"));
        info.filename = obj.getString("FILENAME");
        info.note = obj.getString("NOTE");
        info.source = obj.getString("SOURCE");
        info.success = obj.getBoolean("SUCCESS");
        info.version = obj.getInt("VERSION", 0);
        return info;
    }

    public boolean success;
    public String filename;
    public DateTime created;
    public String source;
    public String note;
    public Integer version;

    public BasicDBObject ToDBObject() {
        BasicDBObject obj = new BasicDBObject();
        obj.put("CREATED", DateUtil.DateTimeToUnixTime(created));
        obj.put("CREATED_PRETTY", created.toString());
        obj.put("FILENAME", filename);
        obj.put("NOTE", note);
        obj.put("SOURCE", source);
        obj.put("SUCCESS", success);
        obj.put("VERSION", version);
        return obj;
    }

    public String getSummary() {

        String summary = "";

        summary += "Success: " + success;
        summary += "\n";

        summary += "Filename: " + filename;
        summary += "\n";

        summary += "Created: " + created;
        summary += "\n";

        summary += "Source: " + source;
        summary += "\n";

        summary += "Version: " + version;
        summary += "\n";

        summary += "Note: " + note;
        summary += "\n";

        return summary;
    }
}
