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
import soot.SootMethodRef;
import soot.Unit;
import soot.jimple.AssignStmt;
import soot.jimple.InvokeStmt;
import soot.jimple.Jimple;

public class RenderIntroduction extends BodyTransformer {

    @Override
    protected void internalTransform(Body b, String phaseName, Map options) {
        if (!(Helper.isConstructor(b)))
            return;

        Unit initStmt = findInitStmt(b);

        // add field
        SootField field = addField(b,
                "org.codehaus.groovy.grails.web.metaclass.RenderDynamicMethod",
                "__render");

        Jimple j = Jimple.v();
        RefType fieldType = (RefType) field.getType();
        SootClass fieldClass = fieldType.getSootClass();
        SootMethodRef ctorRef = fieldClass.getMethodByName("<init>").makeRef();
        Local thisLocal = b.getThisLocal();

        // prepare a local
        Local newLocal = j.newLocal("$_render", fieldType);
        b.getLocals().add(newLocal);

        // $_render = new RenderDynamicMethod.class
        AssignStmt newStmt = j.newAssignStmt(newLocal, j.newNewExpr(fieldType));
        // invokespecial RenderDynamicMethod.<init>()
        InvokeStmt invokeStmt = j.newInvokeStmt(j.newSpecialInvokeExpr(
                newLocal, ctorRef));
        // this._render = $_render
        AssignStmt assignStmt = j.newAssignStmt(j.newInstanceFieldRef(
                thisLocal, field.makeRef()), newLocal);

        PatchingChain<Unit> units = b.getUnits();
        units.insertAfter(newStmt, initStmt);
        units.insertAfter(invokeStmt, newStmt);
        units.insertAfter(assignStmt, invokeStmt);
    }

    private SootField addField(Body b, String fieldClass, String fieldName) {
        System.out.println("-- adField");
        SootClass declaringClass = b.getMethod().getDeclaringClass();
        SootClass sc = Scene.v().getSootClass(fieldClass);
        SootField f = new SootField(fieldName, sc.getType(), Modifier.PUBLIC);
        declaringClass.addField(f);
        return f;
    }

    private Unit findInitStmt(Body b) {
        PatchingChain<Unit> units = b.getUnits();
        Unit unit = units.getFirst();
        Unit initStmt = null;
        while (unit != null) {
            if (!(unit instanceof InvokeStmt)) {
                unit = units.getSuccOf(unit);
                continue;
            }
            InvokeStmt stmt = (InvokeStmt) unit;
            if (stmt.getInvokeExpr().getMethod().getName().equals("<init>")) {
                initStmt = unit;
                break;
            }
        }
        return initStmt;
    }

}
