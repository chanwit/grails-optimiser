package grails.soot.utils;

import soot.Body;
import soot.Local;
import soot.RefType;
import soot.Scene;
import soot.SootClass;
import soot.SootField;
import soot.SootFieldRef;
import soot.SootMethodRef;
import soot.Type;
import soot.Value;
import soot.jimple.AssignStmt;
import soot.jimple.CastExpr;
import soot.jimple.InstanceFieldRef;
import soot.jimple.InvokeExpr;
import soot.jimple.Jimple;

public class JimpleBuilder {

    public static Local local(String name, String typeName) {
        return Jimple.v().newLocal("$__" + name, Scene.v().getSootClass(typeName).getType());
    }

    public static Local local(String name, Type type) {
        return Jimple.v().newLocal("$__" + name, type);
    }

    public static Local local(String name, SootFieldRef fieldRef) {
        return Jimple.v().newLocal("$__" + name, fieldRef.type());
    }

    public static SootMethodRef method(String signature) {
        return Scene.v().getMethod(signature).makeRef();
    }

    public static SootFieldRef field(String signature) {
        return Scene.v().getField(signature).makeRef();
    }

    public static SootFieldRef field(String className, String fieldName) {
        return Scene.v().getSootClass(className).getFieldByName(fieldName).makeRef();
    }

    public static class ObjectBuilder {

        private Local local;

        public ObjectBuilder(Local local) {
            this.local = local;
        }

        public InstanceFieldRef field(String fieldName) {
            SootField f = ((RefType)(local.getType())).getSootClass().getFieldByName(fieldName);
            return Jimple.v().newInstanceFieldRef(local, f.makeRef());
        }

    }

    public static ObjectBuilder object(Local local) {
        return new ObjectBuilder(local);
    }

    public static class InvokeBuilder {

        private Local base;

        public InvokeBuilder(Local base) {
            this.base = base;
        }

        public InvokeExpr virtual_invoke(SootMethodRef mref) {
            return Jimple.v().newVirtualInvokeExpr(base, mref);
        }

        public InvokeExpr virtual_invoke(String signature) {
            return Jimple.v().newVirtualInvokeExpr(base, Scene.v().getMethod(signature).makeRef());
        }

    }

    public static InvokeBuilder $this(Body b) {
        return new InvokeBuilder(b.getThisLocal());
    }

    public static class AssignBuilder {

        private Value left;

        public AssignBuilder(Value left) {
            this.left = left;
        }

        public AssignStmt from(Value right) {
            return Jimple.v().newAssignStmt(left, right);
        }

    }

    public static AssignBuilder assign(Value left) {
        return new AssignBuilder(left);
    }

    public static class CastBuilder {

        private Value local;

        public CastBuilder(Value local) {
            this.local = local;
        }

        public CastExpr to(String typename) {
            return Jimple.v().newCastExpr(this.local, Scene.v().getSootClass(typename).getType());
        }

        public CastExpr to(SootClass sootClass) {
            return Jimple.v().newCastExpr(this.local, sootClass.getType());
        }

        public CastExpr to(Type type) {
            return Jimple.v().newCastExpr(this.local, type);
        }

    }

    public static CastBuilder cast(Value local) {
        return new CastBuilder(local);
    }

}
