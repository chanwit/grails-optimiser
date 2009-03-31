package grails.soot.transformer;

import soot.BodyTransformer;
import soot.Body;
import soot.Local;
import soot.PatchingChain;
import soot.RefType;
import soot.Scene;
import soot.SootClass;
import soot.SootFieldRef;
import soot.SootMethodRef;
import soot.Type;
import soot.Unit;
import soot.Value;
import soot.ValueBox;
import soot.jimple.InstanceFieldRef;
import soot.jimple.IntConstant;
import soot.jimple.AssignStmt;
import soot.jimple.InvokeExpr;
import soot.jimple.InvokeStmt;
import soot.jimple.Jimple;
import soot.jimple.StaticInvokeExpr;
import soot.jimple.StringConstant;
import soot.jimple.VirtualInvokeExpr;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import grails.soot.utils.Location;
import grails.soot.utils.Helper;
import grails.soot.utils.CallsiteNameHolder;

/**
 * @author chanwit
 *
 *         This is a prototype of a transformer to convert dynamic call
 *         <b>render</b> to something else.
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

        SootClass closureClass = Scene.v().getSootClass("groovy.lang.Closure");

        PatchingChain<Unit> units = b.getUnits();

        Location callsiteVar = findCallSiteVar(b);
        if (callsiteVar.box == null)
            return;

        Location callSiteObject = findCallSiteObject(b, callsiteVar, "render");

        if (callSiteObject.box == null)
            return;

        Unit invokeStmt = units.getSuccOf(callSiteObject.unit);

        Value helloWorld = ((AssignStmt)invokeStmt).getInvokeExpr().getArg(1);

        SootClass sc = b.getMethod().getDeclaringClass();
        String outerClassName = sc.getName().split("\\$")[0];
        SootClass controllerClass = Scene.v().getSootClass(outerClassName);
        SootFieldRef fieldRef = controllerClass.getFieldByName("__render")
                .makeRef();

        Jimple j = Jimple.v();

        /**
         *
         * $__delegate = invokespecial this.getDelegate()
         *
         * $__render = $__delegate.__render
         *
         * $__args = newarray object[1] invoke
         *
         * $__render.invoke($__delegate, "render", $__args)
         *
         **/
        Local delegate = j.newLocal("$__delegate", controllerClass.getType());
        SootMethodRef methodRef = closureClass.getMethodByName("getDelegate")
                .makeRef();

        InvokeExpr rvalue = j.newVirtualInvokeExpr(b.getThisLocal(), methodRef);
        AssignStmt getDelegate = j.newAssignStmt(delegate, rvalue);

        AssignStmt castDelegate = j.newAssignStmt(delegate, j.newCastExpr(delegate, controllerClass.getType()));

        Local render = j.newLocal("$__render", fieldRef.type());
        InstanceFieldRef renderFieldOfDelegate = j.newInstanceFieldRef(
                delegate, fieldRef);
        AssignStmt getRender = j.newAssignStmt(render, renderFieldOfDelegate);

        RefType obj = Scene.v().getSootClass("java.lang.Object").getType();
        Local args = j.newLocal("$__args", obj.getArrayType());
        AssignStmt newArray = j.newAssignStmt(args, j.newNewArrayExpr(obj, IntConstant
                .v(1)));

        AssignStmt assignValueToArray = j.newAssignStmt(j.newArrayRef(args, IntConstant.v(0)), helloWorld);

        SootClass sc1 = Scene.v().getSootClass(
                "org.codehaus.groovy.grails.web.metaclass.RenderDynamicMethod");
        SootMethodRef invokeMethod = sc1.getMethod("java.lang.Object invoke(java.lang.Object,java.lang.String,java.lang.Object[])").makeRef();
        // System.out.println(invokeMethod);

        b.getLocals().add(delegate);
        b.getLocals().add(args);
        b.getLocals().add(render);

        b.getUnits().insertBefore(
                Arrays.asList(new Unit[]{
                    getDelegate,
                    castDelegate,
                    getRender,
                    newArray,
                    assignValueToArray
                }),
        invokeStmt);

        ((AssignStmt)invokeStmt).setRightOp(
                j.newVirtualInvokeExpr(render, invokeMethod, Arrays
                        .asList(new Value[] { delegate, StringConstant.v("render"),
                                args }))
        );

        System.out.println(b);
    }

    private Unit makeInvokeStmt() {
        // 1. get delegate()
        // 2. get field "render"
        // 3. invoke render
        // Jimple.v().newstat
        return null;
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
