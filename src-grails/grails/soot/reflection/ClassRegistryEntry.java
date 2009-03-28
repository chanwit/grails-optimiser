package grails.soot.reflection;

import soot.SootClass;

import java.util.HashMap;

/**
 * Created by IntelliJ IDEA.
 * User: chanwit
 * Date: 28 มี.ค. 2552
 * Time: 23:00:40
 * To change this template use File | Settings | File Templates.
 */
public class ClassRegistryEntry {

    private HashMap<String, SootClass> closures = new HashMap<String, SootClass>();

    public void addClosure(String closureName, SootClass sootClass) {
        this.closures.put(closureName, sootClass);
    }

}
