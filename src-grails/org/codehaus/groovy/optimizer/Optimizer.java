package org.codehaus.groovy.optimizer;

import org.codehaus.groovy.optimizer.transformer.CallSiteRecorder;
import org.codehaus.groovy.optimizer.transformer.ConstantRecorder;
import org.codehaus.groovy.optimizer.transformer.PrimitiveBinOps;

import soot.Pack;
import soot.PackManager;
import soot.Transform;

public class Optimizer {

    public static void main(String[] args) {
        Pack jtp = PackManager.v().getPack("jtp");
        jtp.add(new Transform("jtp.groovy.constantRecorder", ConstantRecorder.v()));
        jtp.add(new Transform("jtp.groovy.callsiteRecorder", CallSiteRecorder.v()));

//		jtp.add(new Transform("jtp.groovy.primitiveLeafs", PrimitiveLeafs.v()));
        jtp.add(new Transform("jtp.groovy.pbinop", new PrimitiveBinOps()));
//		jtp.add(new Transform("jtp.groovy.unwrap.compare", new UnwrapCompare()));
//		jtp.add(new Transform("jtp.groovy.boxcastunbox.eliminator", new BoxCastUnboxEliminator()));
        soot.Main.main(args);
    }

}
