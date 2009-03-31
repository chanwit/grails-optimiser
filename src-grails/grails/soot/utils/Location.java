package grails.soot.utils;

import soot.Unit;
import soot.ValueBox;

public class Location {

    public final Unit unit;
    public final ValueBox box;

    public Location(Unit unit, ValueBox box) {
        this.unit = unit;
        this.box = box;
    }

}