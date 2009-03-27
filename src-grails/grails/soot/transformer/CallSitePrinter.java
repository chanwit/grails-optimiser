package grails.soot.transformer;

import grails.soot.utils.CallsiteNameHolder;

import java.util.List;
import java.util.Map;

import soot.Body;
import soot.BodyTransformer;
import soot.Unit;
import soot.ValueBox;
import soot.jimple.AssignStmt;
import soot.jimple.IntConstant;
import soot.jimple.StaticInvokeExpr;

public class CallSitePrinter extends BodyTransformer {

    final private class Result {
        final Unit unit;
        final ValueBox box;

        public Result(Unit unit, ValueBox box) {
            super();
            this.unit = unit;
            this.box = box;
        }
    }

    @Override
    protected void internalTransform(Body b, String phaseName, Map options) {
        if(b.getMethod().getName().equals("doCall")) {
            Result callsiteVar = findCallSiteVar(b);
            if(callsiteVar.box == null) return;

            Result callSiteObject = findCallSiteObject(
                    callsiteVar.unit,
                    callsiteVar.box,
                    "render", b);
            if(callSiteObject.box == null) return;

            // TODO continue here
        }
    }

    private Result findCallSiteObject(
                    Unit start,
                    ValueBox callsiteVar,
                    String methodName, Body b) {

        String[] names = CallsiteNameHolder.v().get(b.getMethod().getDeclaringClass());

        Unit u = start;
        while(u != null){
            u = b.getUnits().getSuccOf(u);
            if(contains(u.getUseBoxes(), callsiteVar)) {
                int currentIndex = ((IntConstant)u.getUseBoxes().get(1).getValue()).value;
                if(names[currentIndex].equals(methodName)) {
                    return new Result(u, u.getDefBoxes().get(0));
                }
            }
        }
        return new Result(null, null);
    }

    private boolean contains(List<ValueBox> useBoxes, ValueBox callsiteVar) {
        for (ValueBox valueBox : useBoxes) {
            if(valueBox.getValue().equivTo(callsiteVar.getValue())) {
                return true;
            }
        }
        return false;
    }

    private Result findCallSiteVar(Body b) {
        for(Unit u : b.getUnits()) {
            if(!(u instanceof AssignStmt)) continue;

            AssignStmt a = (AssignStmt)u;
            if(!a.containsInvokeExpr()) continue;
            if(!(a.getInvokeExpr() instanceof StaticInvokeExpr)) continue;
            if(!(a.getInvokeExpr().getMethod().getName().equals("$getCallSiteArray"))) continue;
            return new Result(u, a.getLeftOpBox());
        }

        return new Result(null, null);
    }

}
