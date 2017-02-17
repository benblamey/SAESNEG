package benblamey.saesneg.experiments.configs;

import benblamey.saesneg.experiments.Experiment;
import benblamey.saesneg.experiments.ExperimentOptions;
import benblamey.saesneg.experiments.ExperimentSet;
import benblamey.saesneg.phaseA.text.ProcessTextOptions;
import com.mongodb.BasicDBObject;
import socialworld.model.SocialWorldUser;

public class Minimal_ExperimentSet extends ExperimentSet {

    public static void main(String[] args) throws Exception {

        ExperimentSet set = new ExperimentSet();
        set.name = "Minimal";

        set.addExperiment(new Experiment(set) {
            {
                Name = "default";
                Options = new ExperimentOptions() {
                    {
                        UserQuery = new BasicDBObject() {
                            {
                                append("LIFE_STORY_INFOS.SUCCESS", true); // Select only users for whom we have a valid life story.
                                append("GROUND_TRUTH_EVENTS", new BasicDBObject() { // Where the user has created ground truth events.
                                    {
                                        append("$exists", true);
                                        append(SocialWorldUser.FACEBOOK_USER_ID, UserIDs.PARTICIPANT_1_USER_ID);
                                    }
                                });
                            }
                        };

                        // Skip both phases.
                        // Phase A options.
                        _gisTextOptions = null; // Don't run OSM search.
                        _textOptions = new ProcessTextOptions() {
                            {
                                // Skip text processing.
                                USE_CACHED_GATE_DOC = true;
                            }
                        };

                        // Skip phase B.
                        PhaseBOptions = null;
                    }
                };

            }
        });

        set.run();

    }
}
