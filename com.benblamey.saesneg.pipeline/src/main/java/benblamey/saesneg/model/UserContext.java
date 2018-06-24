package com.benblamey.saesneg.model;

import com.benblamey.saesneg.FacebookClientHelper;
import com.benblamey.saesneg.PipelineContext;
import com.mongodb.DBObject;
import com.restfb.exception.FacebookNetworkException;
import com.restfb.exception.FacebookOAuthException;
import com.restfb.types.User;
import java.io.File;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlID;
import socialworld.model.SocialWorldUser;

/**
 * The context for working with a user - holds instances of various utility and
 * helper classes, and contains the LifeStory. Behaviour of functions is
 * intended to change depended on which server is running, etc. Not intended to
 * be serialized.
 *
 * Mirror DAO is the SocialWorldUser
 */
public class UserContext {

    public static final String LIFE_STORY_FILES = "LIFE_STORY_FILES";

    @XmlID
    @XmlAttribute
    private String _name;
    private User _profile;
    public String FacebookAccessKey;
    private LifeStory _defaultLifeStory;

    public transient benblamey.saesneg.serialization.LifeStoryInfo lifeStoryInfo;
    public transient SocialWorldUser socialWorldUser;

    public transient FacebookClientHelper fch;

    public transient String ID;

    private UserContext() {
    } //Serialization Ctor.

    // Used in old RDF code.
    @Deprecated
    public UserContext(String name) {
        _name = name;
    }

    public static UserContext fromSocialWorldUser(SocialWorldUser user) {
        if (user == null) {
            throw new RuntimeException("user is null");
        }
       UserContext uc = FromSocialWorldUser(user._obj, null);
       uc.socialWorldUser = user;
       return uc;
    }

    public static UserContext FromSocialWorldUser(DBObject obj, LifeStory defaultLifeStory) {
        if (obj == null) {
            throw new RuntimeException("obj is null");
        }

        UserContext uc = new UserContext();
        String id = (String) obj.get(SocialWorldUser.FACEBOOK_USER_ID);
        uc._name = SocialWorldUser.getName(obj);
        uc.ID = (String) obj.get(SocialWorldUser.FACEBOOK_USER_ID);
        uc.FacebookAccessKey = (String) obj.get(SocialWorldUser.FACEBOOK_OAUTH_TOKEN);
        uc._defaultLifeStory = defaultLifeStory;
        uc.fch = new FacebookClientHelper(uc);
        try {
            uc._profile = (User) uc.fch._facebookClient.fetchObject("me", User.class);
        } catch (FacebookOAuthException e) {
            System.out.println("Skipping getting profile - access token invalid.");
        } catch (FacebookNetworkException e) {
            System.out.println("Skipping getting profile - offline.");
        }

        return uc;
    }

    public void postDeserializationFix() {
        if (this.fch == null) {
            this.fch = new FacebookClientHelper(this);
        }
    }

    public User getProfile() {
        return _profile;
    }

    public void setProfile(User profile) {
        _profile = profile;
        ID = profile.getId();
    }

    public LifeStory getLifeStory() {
        return _defaultLifeStory;
    }

    public void setDefaultLifeStory(LifeStory lifeStory) {
        _defaultLifeStory = lifeStory;
    }

    public String getName() {
        return _name;
    }

    public String getFileSystemSafeName() {
        return ID + "_" + _name.replaceAll("[^a-zA-Z0-9]", "");
    }

    public String getOutputDirectoryWithTrailingSlash() {
        new File(PipelineContext.getCurrentContext().getDataOutputDir()).mkdir();
        String directory = PipelineContext.getCurrentContext().getDataOutputDir() + _name + "\\";
        new File(directory).mkdir();
        return directory;
    }

    public String getOutputDirectoryWithTrailingSlash(SocialWorldUser user) {
        new File(PipelineContext.getCurrentContext().getDataOutputDir()).mkdir();
        String directory = PipelineContext.getCurrentContext().getDataOutputDir() + user.getPrettyName() + "\\";
        new File(directory).mkdir();
        return directory;
    }

    public static String getOutputDirectoryWithTrailingSlashForUser(String name) {
        new File(PipelineContext.getCurrentContext().getDataOutputDir()).mkdir();
        String directory = PipelineContext.getCurrentContext().getDataOutputDir() + name + "\\";
        new File(directory).mkdir();
        return directory;
    }

    public String getOutputDirectoryWithTrailingSlash(String subdir) {
        // Create parent directory
        getOutputDirectoryWithTrailingSlash();

        String directory = PipelineContext.getCurrentContext().getDataOutputDir() + _name + "\\" + subdir + "\\";
        new File(directory).mkdir();
        return directory;
    }

}
