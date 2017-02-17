/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package benblamey.saesneg;

import benblamey.saesneg.model.LifeStory;
import benblamey.saesneg.model.UserContext;
import benblamey.saesneg.serialization.LifeStoryJsonSerializer;
import benblamey.saesneg.serialization.LifeStoryXMLSerializer;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import java.io.File;
import java.util.TreeSet;
import socialworld.model.SocialWorldUser;

public class WhichLifeStoryToLoad {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {

        String fbID = "PARTICIPANT_1_FACEBOOK_ID";

        for (DBObject dbuser : Users.getUsers(new BasicDBObject("FACEBOOK_USER_ID", fbID))) {
            SocialWorldUser user = new SocialWorldUser(dbuser);
            UserContext uc = UserContext.fromSocialWorldUser(user);

//            for (LifeStoryInfo info : LifeStoryInfo.getLifeStoryInfos(user)) {
//                String filename = info.filename;
//                if (foo(filename, uc, user)) continue;
//            }
//            
            File dir = new File(LifeStoryXMLSerializer.getXMLDirectoryWithTrailingSlash());
            TreeSet<String> s = new TreeSet<>();
            for (File f : dir.listFiles()) {
                String[] split = (f.getName()).split("_");
                if (split.length != 3) {
                    continue;
                }
                String file_fbID = split[0];
                if (fbID.equals(file_fbID)) {
                    s.add(f.getName());
                }
            }
            
            for (String ss : s) {
                foo(ss, uc, user);
            }

        }

    }

    private static boolean foo(String filename, UserContext uc, SocialWorldUser user) {
        if (filename == null) {
            return true;
        }
        System.out.println("Trying file: " + filename);
        LifeStory ls;
        
        try {
            ls = LifeStoryXMLSerializer.DeserializeLifeStory(filename, uc);
        } catch (Exception e) {
            System.out.println("Exception: " + e);
            return false;
        }
        LifeStoryJsonSerializer.getGroundTruthEvents(ls, user);
        
        return false;
    }

}
