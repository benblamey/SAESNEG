package benblamey.saesneg.phaseB.strategies;

import benblamey.saesneg.experiments.PhaseBOptions;
import benblamey.saesneg.model.datums.Datum;
import benblamey.saesneg.model.datums.DatumAlbum;
import benblamey.saesneg.model.datums.DatumCheckin;
import benblamey.saesneg.model.datums.DatumEvent;
import benblamey.saesneg.model.datums.DatumLink;
import benblamey.saesneg.model.datums.DatumPhoto;
import benblamey.saesneg.model.datums.DatumStatusMessage;
import benblamey.saesneg.phaseB.DatumPairSimilarity;
import benblamey.saesneg.phaseB.DatumSimilarityEvidence;
import benblamey.saesneg.phaseB.FestibusFeatures;
import java.util.ArrayList;
import java.util.List;

public class KindStrategy extends Strategy {

    @Override
    public void addEvidenceToPair(DatumPairSimilarity pair, PhaseBOptions options) {

        Datum left = pair.getLeft();
        Datum right = pair.getRight();

        pair.addEvidence(createEvidence(left, right));

    }

    private static List<Class> s_kinds;
    private static List<String> s_enumNames;

    static {
        s_kinds = new ArrayList<>();
        s_enumNames = new ArrayList<>();

        s_kinds.add(DatumAlbum.class);
        s_kinds.add(DatumCheckin.class);
        s_kinds.add(DatumEvent.class);
        s_kinds.add(DatumLink.class); // ***Do not remove this***. ////////
        s_kinds.add(DatumPhoto.class);
        s_kinds.add(DatumStatusMessage.class);
    }

    private static DatumSimilarityEvidence createEvidence(Datum left, Datum right) {

        int aIndex = s_kinds.indexOf(left.getClass());
        int bIndex = s_kinds.indexOf(right.getClass());

        // Canonically order the types.
        if (aIndex > bIndex) {
            int c = aIndex;
            aIndex = bIndex;
            bIndex = c;
        }

        String enumName = "Kind_" + toEnumNamePart(s_kinds.get(aIndex))
                + "_" + toEnumNamePart(s_kinds.get(bIndex));
        FestibusFeatures enumValue = FestibusFeatures.valueOf(enumName);

        return new DatumSimilarityEvidence(enumValue, 1.0);
    }

    private static String toEnumNamePart(Class clazz) {
        String[] nameParts = clazz.getName().split("\\.");
        String className = nameParts[nameParts.length - 1];
        className = className.substring("Datum".length());
        return className;
    }

}
