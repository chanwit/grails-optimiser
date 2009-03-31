package grails.soot.transformer;

import soot.BodyTransformer;
import soot.Body;
import soot.Unit;
import soot.ValueBox;
import soot.jimple.IntConstant;
import soot.jimple.AssignStmt;
import soot.jimple.InvokeExpr;
import soot.jimple.StaticInvokeExpr;

import java.util.List;
import java.util.Map;

import grails.soot.utils.Result;
import grails.soot.utils.Helper;
import grails.soot.utils.CallsiteNameHolder;

/**
 * User: chanwit
 */
public class Prototype extends BodyTransformer {

    private static final String DOCALL_METHOD_NAME = "doCall";
    private static final Result EMPTY_RESULT = new Result(null, null);

    @Override
    protected void internalTransform(Body b, String phaseName, Map options) {

        // check only if doCall object, for closure
        if (!(Helper.hasMethodName(b, DOCALL_METHOD_NAME)))
            return;

        Result callsiteVar = findCallSiteVar(b);
        if (callsiteVar.box == null)
            return;

        Result callSiteObject = findCallSiteObject(b, callsiteVar, "render");

        if (callSiteObject.box == null)
            return;
    }

    private Result findCallSiteObject(Body b, Result result, String methodName) {
        return findCallSiteObject(b, result.unit, result.box, methodName);
    }

    private Result findCallSiteObject(Body b, Unit start, ValueBox callsiteVar,
            String methodName) {

        String[] names = CallsiteNameHolder.getCallsiteNames(b);

        Unit unit = start;
        while (true) {
            unit = b.getUnits().getSuccOf(unit);
            if (unit == null)
                break;

            List<ValueBox> useBoxes = unit.getUseBoxes();
            if (Helper.listContainsBox(useBoxes, callsiteVar)) {
                ValueBox useBox = useBoxes.get(1);
                int currentIndex = ((IntConstant) useBox.getValue()).value;

                if (names[currentIndex].equals(methodName)) {
                    return new Result(unit, unit.getDefBoxes().get(0));
                }
            }
        }

        // if cannot find any call site, return null
        return EMPTY_RESULT;
    }

    private Result findCallSiteVar(Body b) {
        for (Unit u : b.getUnits()) {

            if (!(u instanceof AssignStmt))
                continue;

            AssignStmt assignStmt = (AssignStmt) u;
            if (!assignStmt.containsInvokeExpr())
                continue;
            InvokeExpr invokeExpr = assignStmt.getInvokeExpr();
            if (!(invokeExpr instanceof StaticInvokeExpr))
                continue;
            if (!(invokeExpr.getMethod().getName().equals("$getCallSiteArray")))
                continue;
            return new Result(u, assignStmt.getLeftOpBox());
        }

        // if cannot find any call site, return null
        return EMPTY_RESULT;
    }

}
