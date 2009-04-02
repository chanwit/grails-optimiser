package grails.soot.transformer;

import static grails.soot.utils.JimpleBuilder.*;
import grails.soot.utils.CallsiteNameHolder;
import grails.soot.utils.Helper;
import grails.soot.utils.Location;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import soot.Body;
import soot.BodyTransformer;
import soot.Local;
import soot.PatchingChain;
import soot.Scene;
import soot.SootClass;
import soot.SootMethodRef;
import soot.Unit;
import soot.Value;
import soot.ValueBox;
import soot.jimple.AssignStmt;
import soot.jimple.IntConstant;
import soot.jimple.InvokeExpr;
import soot.jimple.Jimple;
import soot.jimple.StaticInvokeExpr;
import soot.jimple.StringConstant;

/**
 * @author chanwit
 *
 *         This is a prototype of a transformer to convert dynamic call
 *         <b>render</b> to something else.
 *
 **/
public class Prototype_2 extends BodyTransformer {

    private static final String INVOKE_SIGNATURE = "<org.codehaus.groovy.grails.web.metaclass.RenderDynamicMethod: java.lang.Object invoke(java.lang.Object,java.lang.String,java.lang.Object[])>";
    private static final String GET_DELEGATE_SIGNATURE = "<groovy.lang.Closure: java.lang.Object getDelegate()>";
    private static final String DOCALL_SUB_SIGNATURE = "java.lang.Object doCall(java.lang.Object)";

    private static final Location EMPTY_LOCATION = new Location(null, null);

    @Override
    protected void internalTransform(Body b, String phaseName, Map options) {

        if(!(b.getMethod().getSubSignature().equals(DOCALL_SUB_SIGNATURE)))
            return;
        PatchingChain<Unit> units = b.getUnits();

        Location callsiteVar = findCallSiteVar(b);
        if (callsiteVar.box == null)
            return;

        Location callSiteObject = findCallSiteObject(b, callsiteVar, "render");

        if (callSiteObject.box == null)
            return;

        // invokeStmt is a target statement
        AssignStmt invokeStmt = (AssignStmt) units.getSuccOf(callSiteObject.unit);

        SootClass sc = b.getMethod().getDeclaringClass();
        String outerClassName = sc.getName().split("\\$")[0];


        Local delegate = local("delegate", outerClassName);
        AssignStmt getDelegate =
            assign(delegate)
            .equal(
                $this(b).virtual_invoke(GET_DELEGATE_SIGNATURE)
            );

        AssignStmt castDelegate = assign(delegate)
            .equal(
                cast(delegate).to(outerClassName)
            );

        Local render = local("render", field(outerClassName, "__render"));
        AssignStmt getRender = assign(render)
            .equal(
                object(delegate).field("__render")
            );

        Local args = array("args", "java.lang.Object");
        AssignStmt newArray = assign(args)
            .equal(
                newarray("java.lang.Object", 1)
            );

        AssignStmt assignValueToArray = assign(args).at(0)
            .equal(
                args(invokeStmt, 1)
            );

        SootMethodRef invokeMethod = Scene.v().getMethod(INVOKE_SIGNATURE).makeRef();

        b.getLocals().addAll(Arrays.asList(new Local[]{
                delegate,
                args,
                render
        }));

        units.insertBefore(Arrays.asList(new Unit[] {
                getDelegate,
                castDelegate,
                getRender,
                newArray,
                assignValueToArray
        }), invokeStmt);

        invokeStmt.setRightOp(
            object(render).virtual_invoke(invokeMethod,
                new Value[]{
                    delegate,
                    StringConstant.v("render"),
                    args
                }
        ));

        // System.out.println(b);
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
