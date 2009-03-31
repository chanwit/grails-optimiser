package grails.soot.transformer;

import grails.soot.utils.Helper;

import java.util.Map;

import soot.Body;
import soot.BodyTransformer;
import soot.Local;
import soot.Modifier;
import soot.PatchingChain;
import soot.RefType;
import soot.Scene;
import soot.SootClass;
import soot.SootField;
import soot.Type;
import soot.Unit;
import soot.jimple.AssignStmt;
import soot.jimple.InvokeStmt;
import soot.jimple.Jimple;
import soot.jimple.SpecialInvokeExpr;

public class RenderIntroduction extends BodyTransformer {

    @Override
    protected void internalTransform(Body b, String phaseName, Map options) {
        if(Helper.isConstructor(b)) {
            Unit initStmt = findInitStmt(b);

            // add field
            SootField field = addField(b, "org.codehaus.groovy.grails.web.metaclass.RenderDynamicMethod", "__render");
            RefType refType = (RefType)field.getType();
            SootClass fieldClass = refType.getSootClass();

            
            Jimple j = Jimple.v();

            // prepare local
            Local newLocal = j.newLocal("$_render", refType);
            b.getLocals().add(newLocal);
            AssignStmt newObjectToLocalStmt = j.newAssignStmt(
                    newLocal,
                    j.newNewExpr(refType)
            );
            InvokeStmt invokeToInitLocalStmt = j.newInvokeStmt(j.newSpecialInvokeExpr(
                    newLocal,
                    fieldClass.getMethodByName("<init>").makeRef()
            ));
            AssignStmt assignLocalToFieldStmt = j.newAssignStmt(
                    j.newInstanceFieldRef(b.getThisLocal(), field.makeRef()),
                    newLocal
            );

            b.getUnits().insertAfter(newObjectToLocalStmt, initStmt);
            b.getUnits().insertAfter(invokeToInitLocalStmt, newObjectToLocalStmt);
            b.getUnits().insertAfter(assignLocalToFieldStmt, invokeToInitLocalStmt);
        }
    }

    private SootField addField(Body b, String fieldClass, String fieldName) {
        SootClass declaringClass = b.getMethod().getDeclaringClass();
        SootClass sc = Scene.v().getSootClass(fieldClass);
        SootField f = new SootField(fieldName, sc.getType(), Modifier.PUBLIC);
        declaringClass.addField(f);
        return f;
    }

    private Unit findInitStmt(Body b) {
        PatchingChain<Unit> units = b.getUnits();
        Unit unit = units.getFirst();
        Unit initStmt=null;
        while(unit != null) {
            if(!(unit instanceof InvokeStmt)) {
                unit = units.getSuccOf(unit);
                continue;
            }
            InvokeStmt stmt = (InvokeStmt)unit;
            if(stmt.getInvokeExpr().getMethod().getName().equals("<init>")) {
                initStmt = unit;
                break;
            }
        }
        return initStmt;
    }

}
