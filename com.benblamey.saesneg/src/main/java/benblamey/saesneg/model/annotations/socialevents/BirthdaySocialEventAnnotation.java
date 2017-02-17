package benblamey.saesneg.model.annotations.socialevents;

import gate.Factory;
import gate.FeatureMap;
import java.util.List;

public class BirthdaySocialEventAnnotation extends SocialEventAnnotation {

    private Integer _age = -1; // negative means we don't know.
    private String _birthdayPerson = null;

    public BirthdaySocialEventAnnotation() {
        log();
    }

    public BirthdaySocialEventAnnotation(Number i) {
        _age = (Integer) i;
        if (_age == null) {
            throw new RuntimeException("_age is null.");
        }
        log();
    }

    public BirthdaySocialEventAnnotation(Number i, String who1) {
        _age = (Integer) i;
        if (_age == null) {
            throw new RuntimeException("_age is null.");
        }
        _birthdayPerson = who1;
        log();
    }

    private void log() {
        System.out.println("Created " + BirthdaySocialEventAnnotation.class.getName()
                + ", age = " + _age + ", person = " + _birthdayPerson);

    }

    @Override
    public String toString() {
        return BirthdaySocialEventAnnotation.class.getName() + ", age = " + _age + ", person = " + _birthdayPerson;
    }

    /*
	 * If <0 means we don't know.
     */
    public int getAge() {
        return _age;
    }

    public String getOwner() {
        return _birthdayPerson;
    }

    @Override
    public FeatureMap getFeaturesForGATE() {
        FeatureMap newFeatureMap = Factory.newFeatureMap();
        if (_age >= 0) {
            newFeatureMap.put("age", _age);
        }

        if (_birthdayPerson != null) {
            newFeatureMap.put("birthdayPerson", _birthdayPerson);
        }
        return newFeatureMap;
    }

    public static BirthdaySocialEventAnnotation merge(List<BirthdaySocialEventAnnotation> values) {
        BirthdaySocialEventAnnotation mergedBirthday = null;
        for (BirthdaySocialEventAnnotation birthday : values) {
            if (mergedBirthday == null) {
                mergedBirthday = birthday;
            }
            if (mergedBirthday._age < 0 && birthday._age > 0) {
                mergedBirthday._age = birthday._age;
            }
            if (mergedBirthday._birthdayPerson == null && birthday._birthdayPerson != null) {
                mergedBirthday._birthdayPerson = birthday._birthdayPerson;
            }
        }
        return mergedBirthday;
    }


}
