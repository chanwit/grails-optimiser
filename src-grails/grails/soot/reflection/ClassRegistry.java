package grails.soot.reflection;

import soot.SootClass;

import java.util.HashMap;

/**
 * User: chanwit
 */
public class ClassRegistry extends HashMap<SootClass, ClassRegistryEntry> {

    private static ClassRegistry _instance = new ClassRegistry();

    public static ClassRegistry v() {
        return _instance;
    }

}
