/*
package com.benblamey.saesneg.phaseB;


import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.benblamey.saesneg.experiments.DatumsScope;
import com.benblamey.saesneg.experiments.PhaseBOptions;
import com.benblamey.saesneg.model.Event;
import com.benblamey.saesneg.model.LifeStory;
import com.benblamey.saesneg.model.datums.Datum;
import com.benblamey.saesneg.model.datums.DatumAlbum;
import com.benblamey.saesneg.phaseB.DatumSimilarityCalculator;
import com.benblamey.saesneg.phaseB.features.DatumPairSimilarity;

public class GreedyDatumClustering implements IDatumClustering {

	private LifeStory _ls;
	private IEdgeClassifier _edgeClassifier;
	private PhaseBOptions _phaseBoptions;

	public GreedyDatumClustering(LifeStory ls, PhaseBOptions options, IEdgeClassifier svmEdgeClassifier) {
		_ls = ls;
		_edgeClassifier = svmEdgeClassifier;
		_phaseBoptions = options;
	}

	/* (non-Javadoc)
	 * @see benblamey.saesneg.phaseB.clustering.IDatumClustering#clusterGroundTruthDatums(benblamey.saesneg.model.LifeStory)

	public List<Event> clusterGroundTruthDatums(LifeStory ls) {

		List<Datum> datums = new ArrayList<>();

		// Work out which datums we are sorting.
		if (_phaseBoptions.scope == DatumsScope.DatumsUsedInGroundTruth) {
			for (Event e : ls.EventsGolden) {
				datums.addAll(e.getDatums());
			}
		} else if (_phaseBoptions.scope == DatumsScope.AllDatums) {
			datums.addAll(ls.datums);
		} else {
			throw new IllegalArgumentException();
		}

		System.out.println("Starting Clustering of " + datums.size() + " datums.");

		// Make a list of all the pairs.
		List<DatumPairSimilarity> pairs = new ArrayList<DatumPairSimilarity>();
		for (Datum left : datums)
		{
			for (Datum right : datums) {
				if (left == right) {
					continue;
				}
				pairs.add(new DatumPairSimilarity(left, right));
			}
		}

		// Sets the classification results onto the pairs.
		this._edgeClassifier.computePairSimilarity(pairs);

		// Sort the pairs so that the most likely classifications appear first.
		Collections.sort(pairs, new Comparator<DatumPairSimilarity>() {
			@Override
			public int compare(DatumPairSimilarity o1, DatumPairSimilarity o2) {
				return -Double.compare(o1.getResult().probOfMostLikelyClass,
								o2.getResult().probOfMostLikelyClass);
			}
		});


		// Create a blank clustering.
		Map<Datum,Integer> events = new HashMap<>();
		for (Datum mfo : datums ) {
			events.put(mfo, -1);
		}


		 // TODO: think about clustering algorithms  -- post on stack exchange?




		for (DatumPairSimilarity pair : pairs) {
			Datum left = pair.getLeft();
			Datum right = pair.getRight();

			int leftEvent = events.get(left);
			int rightEvent = events.get(right);

			if (leftEvent < 0 && rightEvent < 0) {

			}
		}






		int nextGroup = 0;



		//boolean didWork;
//		do {
//			didWork = false;

			for (Datum mfo : datums) {
				Integer group = events.get(mfo);
				if (group < 0) {

					System.out.println("LHS: " + mfo);

					nextGroup++;
					events.put(mfo,nextGroup);


					boolean didWork;


					// Flood.
					do {

						didWork = false;

						for (Datum mfo2 : datums) {
							if (mfo2 == mfo) {
								continue;
							} else {

								//System.out.println("RHS: " + mfo2);

								int group2 = events.get(mfo2);
								if (group2 >= 0) {
									continue;
								} else {
									switch (binDatumSimilarity(mfo, mfo2)) {
										case SameEvent:
											events.put(mfo2, nextGroup);
											didWork= true;
											break;
										case DifferentEvent:

											break;
										case Neutral:

											break;
									}
								}
							}
						}

					} while (didWork);


					didWork= true;


				}

			}

//		} while (didWork);

		System.out.println("Finished Clustering.");


		if (events.containsValue(-1)) {
			throw new RuntimeException("Some datum was not assigned.");
		}

		Map<Integer, Event> eventsMap = new HashMap<>();
		for (Datum mfo : datums) {
			int eventID = events.get(mfo);
			Event event = eventsMap.get(eventID);
			if (event == null) {
				event = new Event();
				eventsMap.put(eventID, event);
			}
			event.getDatums().add(mfo);
		}


		List<Event> asList = new ArrayList<>(eventsMap.values());

		return asList;

	}

	private PairSimilarityResult binDatumSimilarity(Datum mfo, Datum mfo2) {
		DatumPairSimilarity runFESTIBUS = _ls.DatumSimilarityCalculator.runFESTIBUS(mfo, mfo2, _phaseBoptions);
		return _edgeClassifier.computePairSimilarity(runFESTIBUS);
	}



}
 */
