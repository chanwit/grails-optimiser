package grails.soot;

import grails.soot.transformer.CallReplacementForClosure;
import grails.soot.transformer.CallsiteNameCollector;
import grails.soot.transformer.ClosureDetector;
import grails.soot.transformer.Prototype;
import grails.soot.transformer.RenderIntroduction;
import soot.Pack;
import soot.PackManager;
import soot.Scene;
import soot.SootClass;
import soot.Transform;

@SuppressWarnings("unused")
public class Main {

    public static void main(String[] args) {
        Scene.v().setPhantomRefs(true);
        Pack jtp = PackManager.v().getPack("jtp");

        jtp.add(new Transform("jtp.callsite_name_collector",
                new CallsiteNameCollector()
        ));
        jtp.add(new Transform("jtp.render_declaration",
                new RenderIntroduction()
        ));
        jtp.add(new Transform("jtp.closure_detector",
                new ClosureDetector()
        ));
        jtp.add(new Transform("jtp.prototype",
                new Prototype()
        ));

        initClasses();
        soot.Main.main(args);
    }

    private static void initClasses() {
        String classes[] = {
            "org.codehaus.groovy.grails.web.metaclass.RenderDynamicMethod"
        };

        for (int i = 0; i < classes.length; i++) {
            Scene.v().addBasicClass(classes[i], SootClass.SIGNATURES);
        }
    }

}
