package grails.soot.transformer;

import soot.Body;
import soot.RefType;
import soot.SootClass;
import soot.Scene;

/**
 * User: chanwit
 */
public class Helper {

    static boolean isConstructor(Body b) {
        return b.getMethod().getName().equals("<init>");
    }

    static boolean isClosureType(RefType refType) {
        SootClass closureClass = Scene.v().getSootClass("groovy.lang.Closure");
        SootClass sootClass = refType.getSootClass();
        while(sootClass.hasSuperclass()) {
            SootClass superclass = sootClass.getSuperclass();
            if(superclass.equals(closureClass)) return true;
        }
        return false;
    }

    static boolean hasMethodName(Body b, String name) {
        return b.getMethod().getName().equals(name);
    }
}
