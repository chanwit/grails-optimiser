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

import grails.soot.utils.Location;
import grails.soot.utils.Helper;
import grails.soot.utils.CallsiteNameHolder;

/**
 * @author chanwit
 *
 * This is a prototype of a transformer
 * to convert dynamic call <b>render</b> to something else.
 *
 **/
public class Prototype extends BodyTransformer {

    private static final String DOCALL_METHOD_NAME = "doCall";
    private static final Location EMPTY_LOCATION = new Location(null, null);

    @Override
    protected void internalTransform(Body b, String phaseName, Map options) {

        // check only if doCall object, for closure
        if (!(Helper.hasMethodName(b, DOCALL_METHOD_NAME)))
            return;

        Location callsiteVar = findCallSiteVar(b);
        if (callsiteVar.box == null)
            return;

        Location callSiteObject = findCallSiteObject(b, callsiteVar, "render");

        if (callSiteObject.box == null)
            return;

    }

    private Location findCallSiteObject(Body b, Location loc, String methodName) {
        return findCallSiteObject(b, loc.unit, loc.box, methodName);
    }

    private Location findCallSiteObject(Body b, Unit start,
            ValueBox callsiteVar, String methodName) {

        String[] names = CallsiteNameHolder.getCallsiteNames(b);

        Unit unit = start;
        while (true) {
            unit = b.getUnits().getSuccOf(unit);
            if (unit == null)
                break;

            List<ValueBox> useBoxes = unit.getUseBoxes();
            if (!(Helper.listContainsBox(useBoxes, callsiteVar)))
                continue;

            ValueBox useBox = useBoxes.get(1);
            int currentIndex = ((IntConstant) useBox.getValue()).value;

            if (names[currentIndex].equals(methodName)) {
                return new Location(unit, unit.getDefBoxes().get(0));
            }
        }

        // if cannot find any call site, return null
        return EMPTY_LOCATION;
    }

    private Location findCallSiteVar(Body b) {
        for (Unit unit : b.getUnits()) {

            if (!(unit instanceof AssignStmt))
                continue;

            AssignStmt assignStmt = (AssignStmt) unit;
            if (!assignStmt.containsInvokeExpr())
                continue;

            InvokeExpr invokeExpr = assignStmt.getInvokeExpr();
            if (!(invokeExpr instanceof StaticInvokeExpr))
                continue;

            if (!(invokeExpr.getMethod().getName().equals("$getCallSiteArray")))
                continue;

            return new Location(unit, assignStmt.getLeftOpBox());
        }

        // if cannot find any call site, return null
        return EMPTY_LOCATION;
    }

}
