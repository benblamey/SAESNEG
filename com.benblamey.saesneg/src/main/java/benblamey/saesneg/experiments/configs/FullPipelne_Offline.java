/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.benblamey.saesneg.experiments.configs;

import com.benblamey.saesneg.PipelineContext;
import com.benblamey.saesneg.experiments.Experiment;
import com.benblamey.saesneg.experiments.ExperimentOptions;
import com.benblamey.saesneg.experiments.ExperimentSet;
import com.benblamey.saesneg.experiments.LibSVMGoldActions;
import com.benblamey.saesneg.experiments.LifeStorySelectionStrategy;
import com.benblamey.saesneg.experiments.PhaseBOptions;
import com.benblamey.saesneg.phaseA.text.ProcessTextOptions;

public class FullPipelne_Offline {

        public static void main(String[] args) throws Exception {

        ExperimentSet set = new ExperimentSet();
        set.name = "ThesisOffline_Experiment_Set";

        set.addExperiment(new Experiment(set) {
            {
                Name = "ThesisOffline_Experiment_Set";

                Options = new ExperimentOptions() {
                    {
                        UserQuery = PipelineContext.getCurrentContext().getUserQuery(); // Don't change here

                        lifeStorySelectStrategy = LifeStorySelectionStrategy.UseGroundTruthMatching;

                        // Phase A options.
                        //_gisTextOptions = new OpenStreetMapSearchAlgorithmOptions(); // need to fix IP address issues with VM.

                        _textOptions = new ProcessTextOptions() {
                        };
                        runPhaseAMetadata = true;

                        geocodeMetadata = false;

                        PhaseBOptions = new PhaseBOptions() {
                            {
                                LibSVMGoldAction = LibSVMGoldActions.CrossRun_Individual;
                                //EdgeClassifier = EdgeClassifierMethod.SVM;
                            }
                        };


                    }
                };

            }
        });

        set.run();

    }
}
