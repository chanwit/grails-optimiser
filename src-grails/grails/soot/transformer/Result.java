package grails.soot.transformer;

import soot.Unit;
import soot.ValueBox;

public class Result {

    public final Unit unit;
    public final ValueBox box;

    public Result(Unit unit, ValueBox box) {
        this.unit = unit;
        this.box = box;
    }

}