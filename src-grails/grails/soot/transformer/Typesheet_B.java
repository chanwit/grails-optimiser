package grails.soot.transformer;

import groovy.lang.MetaMethod;

import java.util.HashMap;
import java.util.Map;

import org.aspectj.weaver.tools.TypePatternMatcher;
import org.aspectj.weaver.tools.PointcutParser;
import soot.*;


public class Typesheet_B extends BodyTransformer {

    private String matchedClosureClassName=null;
    private HashMap bindings = new HashMap();
    //private MethodNode doCallMethodNode;
    //private ShadowMatchInfo[] shadowMatchInfo;
    private int callsiteArrayVar;

    private int curMaxLocals = 0;

    //private HashMap<Integer, Pair> analysisResults = new HashMap<Integer, Pair>();
    private HashMap<String, Integer> symtabs = new HashMap<String, Integer>();
    private Class<?>[] advisedTypes;

    private int[] matchedCallSites;
    private String[] callsiteNames;
    private PointcutParser parser =
            PointcutParser.getPointcutParserSupportingAllPrimitivesAndUsingSpecifiedClassloaderForResolution(Scene.class.getClassLoader());

    public boolean match(Body b) {
        if(clazz(b, "*Controller")) {
            //
            // because "index" is a closure
            // then, index is an inner class
            // then clazz matches above will not be done correctly
            //
            if(closure(b, "index") && call(b, "render", new String[]{"s"})) {
                typeAdvice("s", String.class);

                return true;
            }
        }

        return false;
    }

    private void performWeaving(Body b) {

    }

    private void typeAdvice(String binding, Class<?> cls) {
        advisedTypes[symtabs.get(binding)] = cls;
    }

    // this predicate must also match
    // - the class itself
    // - outter class for closure semantic
    //
    private boolean clazz(Body b, String classPat) {
        SootClass declaringClass = b.getMethod().getDeclaringClass();
        TypePatternMatcher tpm = parser.parseTypePattern(classPat);
        try {
            boolean resultForMatchingClass = tpm.matches(
                    Class.forName(declaringClass.getName())
            );
            if(resultForMatchingClass) return true; // shortcut

            boolean resultForMatchingOutterClass = false;
            SootClass thisClass = declaringClass;
            while(thisClass.hasOuterClass()) {
                thisClass = thisClass.getOuterClass();
                boolean partialResult = tpm.matches(
                    Class.forName(thisClass.getName())
                );
                if(partialResult) return true; // shortcut

                resultForMatchingOutterClass = resultForMatchingOutterClass || partialResult;
            }
            return resultForMatchingClass || resultForMatchingOutterClass;

        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    private boolean closure(Body closureName, String b) {
        //ClosureScanner cs = new ClosureScanner(target, closureName);
        //boolean result = cs.isMatch();
        //if(result) {
        //    matchedClosureClassName = cs.getClosureClassName();
        //}
        return true;
    }

    // match call inside the above closure
    // with arg callPattern
    private boolean call(Body b, String callPattern, String[] bindings) {

        return true;

//        CallMatcherForClosure cmfc = new CallMatcherForClosure(matchedClosureClassName);
//        // input is a name
//        // return callsite index
//        matchedCallSites = cmfc.queryCallSiteIndice(callPattern);
//        callsiteNames = cmfc.getCallSiteNames();
//
//        boolean result = matchedCallSites.length > 0;
//        if(result) {
//            doCallMethodNode = cmfc.getDoCallMethodNode();
//            curMaxLocals = doCallMethodNode.maxLocals;
//            shadowMatchInfo = cmfc.getShadowMatchInfos();
//            analyseArgumentForCalls(matchedCallSites);
//            symtabs = buildSymbolTable(bindings);
//            initAdvisedTypes();
//        }
//        return result;
    }

    /**
     *
     * Initialise each element in advised type array to java.lang.Object
     *
     * **/
    private void initAdvisedTypes() {
        advisedTypes = new Class<?>[symtabs.size()];
        for (int i = 0; i < advisedTypes.length; i++) {
            advisedTypes[i] = Object.class;
        }
    }

    /*
    private void performWeaving(Body b) {
        InsnList units = doCallMethodNode.instructions;

        for(Map.Entry<Integer, Pair> e: analysisResults.entrySet()) {

            //
            // the key of each analysis result is call site index
            //
            int callsiteIndex = e.getKey();

            // 0. resolve conversion, later
            MethodInsnNode m = (MethodInsnNode)e.getValue().key;
            if(m.name.equals("callCurrent")) {
                Type[] argTypes = Type.getArgumentTypes(m.desc);
                int varsInStack = argTypes.length + 1; // include 0, which is the callsite
                int lastLocalVar = curMaxLocals;

                //
                // locals needs only varsInStack - 1
                // because we discard the callsite object itself
                //
                curMaxLocals += varsInStack-1;

                int[] args = new int[varsInStack];
                // algo:
                // POP
                // ASTORE ${lastLocalVar}
                // POP
                // ASTORE ${lastLocalVar + 1}
                // if varsInStack = 3, then
                // 2, 1, 0
                for(int i=varsInStack-1; i>=0; i--) {
                    units.insertBefore(m, new InsnNode(POP));
                    //
                    // TODO put conversion here before storing it
                    //
                    if (i != 0) {
                        args[i] = lastLocalVar + i;
                        units.insertBefore(m, new VarInsnNode(ASTORE, args[i]));
                    }
                }

                // LOAD all arguments

                // some rule need to check here?
                MetaMethod metaMethod = resolveMethodFromMetaClass(callsiteNames[callsiteIndex], advisedTypes);
                String[] absoluteSignature = resolveSpecialiser(metaMethod, advisedTypes);


                // [0] is the callsite
                // [1] is the "this"
                // [2..] is param0
                // (Lgroovy/lang/GroovyObject;Ljava/lang/Object;)Ljava/lang/Object;

            } else {
                throw new RuntimeException("not implemented yet");
            }
            // 1. locate e.getValue().key
            // 2. build new instruction
            // 3. replace
        }
    }
    */

    /*
    // @return [0] owner, [1] method name, [2] descriptor
    private String[] resolveSpecialiser(MetaMethod metaMethod, Class<?>[] types) {

        // TODO add some resolving rules
        return new String[] {
            "org/codehaus/groovy/grails/web/metaclass/RenderDynamicMethod",
            "invoke_render_java_lang_String",
            "(Ljava/lang/Object;Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/Object;"
        };

        if(metaMethod instanceof CachedMethod) {
            CachedMethod c = (CachedMethod)metaMethod;
            Method m = c.getCachedMethod();

            String[] result = new String[3];
            result[0] = Type.getInternalName(m.getDeclaringClass());
            result[1] = m.getName();
            result[2] = Type.getMethodDescriptor(m);
            return result;
        }
        return null;
    }
    */

    private HashMap<String, Integer> buildSymbolTable(String[] bindings) {
        HashMap<String, Integer> result = new HashMap<String, Integer>();
        for (int i = 0; i < bindings.length; i++) {
            result.put(bindings[i], i);
        }
        return result;
    }

    @Override
    protected void internalTransform(Body b, String phaseName, Map options) {
        if(match(b)) {
            performWeaving(b);
        }
    }

}
