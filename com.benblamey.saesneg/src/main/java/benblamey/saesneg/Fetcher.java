package com.benblamey.saesneg;

import com.benblamey.saesneg.model.LifeStory;
import com.benblamey.saesneg.model.UserContext;
import com.benblamey.core.logging.Logger;
import com.benblamey.core.logging.LoggerLevel;
import com.restfb.types.NamedFacebookType;
import com.restfb.types.User;

public class Fetcher {

    /**
     * This is used from the daemon.
     *
     * @param user
     */
    public static void fetch(Logger logger, UserContext user) {
        /////////////// Get Data for "me" /////////////////

        LifeStory lifeStory = user.getLifeStory();

        logger.info("Fetching profile.");
        lifeStory._profile = user.fch._facebookClient.fetchObject("me", User.class);

        logger.info("Fetching friends.");
        lifeStory.Friends = user.fch.getConnectionWithPagination("me/friends", NamedFacebookType.class);
        // Build the friend indexes - incase other types want to use it during initial pre-processing.
        lifeStory.initFriends();

        logger.info("Fetching groups.");
        lifeStory.Groups = user.fch.getConnectionWithPagination("me/groups", com.restfb.types.Group.class);

        // Locations is now deprecated.
        //logger.info("Fetching locations.");
        //lifeStory.Locations = user.fch.GetConnectionWithPagination("me/locations", com.restfb.types.Location.class);

        logger.info("Fetching objects.");
        // Note: Photos automatically fetch their parent albums.
        user.fch.getObjects(user.getLifeStory().datums, "me");

        if (lifeStory.datums.isEmpty()) {
            logger.log(LoggerLevel.INFORMATION, "No Entries for " + user.getName());
        }

    }

}
