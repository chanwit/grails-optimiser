package grails.soot.transformer;

import java.util.Iterator;
import java.util.Map;

import soot.Body;
import soot.BodyTransformer;
import soot.Local;
import soot.RefType;
import soot.Scene;
import soot.SootClass;
import soot.Unit;
import soot.jimple.AssignStmt;
import soot.jimple.InstanceFieldRef;
import soot.util.Chain;

public class ClosureDetector extends BodyTransformer {

    @Override
    protected void internalTransform(Body b, String phaseName, Map options) {

//		Extract this pattern
//    	$r2 = new TestController$_closure1
//    	specialinvoke $r2.<TestController$_closure1: void <init>(java.lang.Object,java.lang.Object)>(r0, r0)
//    	r0.<TestController: java.lang.Object index> = $r2

        if(b.getMethod().getName().equals("<init>")) {
            Unit u = b.getUnits().getFirst();
            while(u != null) {
                u = b.getUnits().getSuccOf(u);
                //System.out.println(u);
                if(u instanceof AssignStmt == false) continue;
                AssignStmt a = (AssignStmt)u;
                if(a.getLeftOp() instanceof InstanceFieldRef == false) continue;
                if(a.getRightOp() instanceof Local == false) continue;
                Local right = (Local) a.getRightOp();
                RefType refType = (RefType)right.getType();
                if(isClosure(refType)) {
                    InstanceFieldRef fr = (InstanceFieldRef)a.getLeftOp();
                    System.out.println(fr.getField().getName());
                }
            }
        }
    }

    private boolean isClosure(RefType refType) {
        SootClass closureClass = Scene.v().getSootClass("groovy.lang.Closure");
        SootClass sootClass = refType.getSootClass();
        while(sootClass.hasSuperclass()) {
            SootClass superclass = sootClass.getSuperclass();
            if(superclass.equals(closureClass)) return true;
        }
        return false;
    }

}
