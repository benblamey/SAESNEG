package com.benblamey.evaluation.web.debug;

import com.benblamey.saesneg.ExperimentUserContext;
import com.benblamey.saesneg.experiments.Experiment;
import com.benblamey.saesneg.experiments.configs.TomcatExperimentSet;
import com.benblamey.saesneg.model.UserContext;
import com.mongodb.BasicDBObject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import socialworld.model.SocialWorldUser;

public class ProcessedLifeStoryCache {

    public static final String SVM_MODEL_PATH = "C://work//data//output//2014-03-14//svm.model";

    private static final Map<String, UserContext> _cache = new HashMap<>();

    public static synchronized UserContext getLifeStory(String facebookID) throws Exception {

        UserContext userContext = _cache.get(facebookID);

        if (userContext == null) {
            Experiment experiment = TomcatExperimentSet.getExperiment();
            experiment.Options.UserQuery = new BasicDBObject(SocialWorldUser.FACEBOOK_USER_ID, facebookID);
            List<ExperimentUserContext> run = experiment.run();
            ExperimentUserContext get = run.get(0);
            userContext = get.userContext;
            _cache.put(facebookID, userContext);
        }

        return userContext;
    }

}
