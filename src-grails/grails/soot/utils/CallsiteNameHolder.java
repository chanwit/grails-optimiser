package grails.soot.utils;

import java.util.HashMap;

import soot.Body;
import soot.SootClass;

public class CallsiteNameHolder extends HashMap<SootClass, String[]> {

    private static final long serialVersionUID = -8665663912161719573L;

    private static CallsiteNameHolder _instance = new CallsiteNameHolder();

    public static CallsiteNameHolder v() {
        return _instance;
    }

    public static String[] getCallsiteNames(Body b) {
        return _instance.get(b.getMethod().getDeclaringClass());
    }

}
