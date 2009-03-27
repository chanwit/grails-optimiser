package grails.soot;

import java.util.Iterator;

import grails.soot.transformer.CallSitePrinter;
import grails.soot.transformer.CallsiteNameCollector;
import soot.Pack;
import soot.PackManager;
import soot.Transform;

@SuppressWarnings("unused")
public class Main {

    public static void main(String[] args) {
        Pack jtp = PackManager.v().getPack("jtp");

        jtp.add(new Transform("jtp.callsite_name_collector", new CallsiteNameCollector()));
        jtp.add(new Transform("jtp.callsite_printer", new CallSitePrinter()));

        soot.Main.main(args);
    }

}
