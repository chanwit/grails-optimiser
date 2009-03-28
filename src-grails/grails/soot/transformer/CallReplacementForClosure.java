package grails.soot.transformer;

import grails.soot.utils.CallsiteNameHolder;

import java.util.Map;

import soot.Body;
import soot.BodyTransformer;
import soot.Unit;
import soot.ValueBox;
import soot.jimple.AssignStmt;
import soot.jimple.IntConstant;
import soot.jimple.StaticInvokeExpr;

public class CallReplacementForClosure extends BodyTransformer {
    private static final String DOCALL_METHOD_NAME = "doCall";

    private static final Result EMPTY_RESULT = new Result(null, null);

    @Override
    protected void internalTransform(Body b, String phaseName, Map options) {

        // check only if doCall object, for closure
        if (Helper.hasMethodName(b, DOCALL_METHOD_NAME)) {
            Result callsiteVar = findCallSiteVar(b);
            if (callsiteVar.box == null) return;

            Result callSiteObject = findCallSiteObject(
                    callsiteVar.unit,
                    callsiteVar.box,
                    "render", b);

            if (callSiteObject.box == null) return;

            // TODO continue here
        }
    }

    private Result findCallSiteObject(Unit start, ValueBox callsiteVar, String methodName, Body b) {

        String[] names = CallsiteNameHolder.v().get(b.getMethod().getDeclaringClass());

        Unit u = start;
        while (true) {
            u = b.getUnits().getSuccOf(u);
            if (u == null) break;

            if (Helper.listContainsBox(u.getUseBoxes(), callsiteVar)) {
                int currentIndex = ((IntConstant) u.getUseBoxes().get(1).getValue()).value;
                if (names[currentIndex].equals(methodName)) {
                    return new Result(u, u.getDefBoxes().get(0));
                }
            }
        }

        // if cannot find any call site, return null
        return EMPTY_RESULT;
    }

    private Result findCallSiteVar(Body b) {
        for (Unit u : b.getUnits()) {

            if (!(u instanceof AssignStmt)) continue;

            AssignStmt a = (AssignStmt) u;
            if (!a.containsInvokeExpr()) continue;
            if (!(a.getInvokeExpr() instanceof StaticInvokeExpr)) continue;
            if (!(a.getInvokeExpr().getMethod().getName().equals("$getCallSiteArray"))) continue;
            return new Result(u, a.getLeftOpBox());
        }

        // if cannot find any call site, return null
        return EMPTY_RESULT;
    }

}
