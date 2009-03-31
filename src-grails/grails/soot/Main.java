package grails.soot;

import grails.soot.transformer.CallReplacementForClosure;
import grails.soot.transformer.CallsiteNameCollector;
import grails.soot.transformer.ClosureDetector;
import grails.soot.transformer.Prototype;
import soot.Pack;
import soot.PackManager;
import soot.Transform;

@SuppressWarnings("unused")
public class Main {

    public static void main(String[] args) {
        Pack jtp = PackManager.v().getPack("jtp");

        jtp.add(new Transform("jtp.callsite_name_collector",
                new CallsiteNameCollector()
        ));
        jtp.add(new Transform("jtp.closure_detector",
                new ClosureDetector()
        ));
        jtp.add(new Transform("jtp.prototype",
                new Prototype()
        ));

        soot.Main.main(args);
    }

}
