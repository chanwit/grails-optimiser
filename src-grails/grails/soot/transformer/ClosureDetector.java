package grails.soot.transformer;

import java.util.Map;

import soot.*;
import soot.jimple.AssignStmt;
import soot.jimple.InstanceFieldRef;
import grails.soot.utils.Helper;
import grails.soot.reflection.ClassRegistry;
import grails.soot.reflection.ClassRegistryEntry;

public class ClosureDetector extends BodyTransformer {

    @Override
    protected void internalTransform(Body b, String phaseName, Map options) {

        ClassRegistry cr = ClassRegistry.v();        

        //      Extract this pattern
        //      ====================
        //      $r2 = new TestController$_closure1
        //      specialinvoke $r2.<TestController$_closure1: void <init>(java.lang.Object,java.lang.Object)>(r0, r0)
        //      r0.<TestController: java.lang.Object index> = $r2

        if (Helper.isConstructor(b)) {
            Unit u = b.getUnits().getFirst();
            while (u != null) {
                u = b.getUnits().getSuccOf(u);
                if (!(u instanceof AssignStmt)) continue;
                AssignStmt a = (AssignStmt) u;
                if (!(a.getLeftOp() instanceof InstanceFieldRef)) continue;
                if (!(a.getRightOp() instanceof Local)) continue;
                Local right = (Local) a.getRightOp();
                
                RefType refType = (RefType) right.getType();
                if (Helper.isClosureType(refType)) {
                    InstanceFieldRef fr = (InstanceFieldRef) a.getLeftOp();
                    String actionName = fr.getField().getName();

                    SootClass declaringClass = b.getMethod().getDeclaringClass();
                    if (cr.containsKey(declaringClass)) {
                        ClassRegistryEntry cre = cr.get(declaringClass);
                        cre.addClosure(actionName, refType.getSootClass());
                    } else {
                        ClassRegistryEntry cre = new ClassRegistryEntry();
                        cre.addClosure(actionName, refType.getSootClass());
                        cr.put(declaringClass, cre);
                    }

                }
            }
        }
    }

}
