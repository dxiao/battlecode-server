package battlecode.engine.instrumenter;

import battlecode.engine.ErrorReporter;
import org.objectweb.asm.*;

import java.io.IOException;
import java.util.HashSet;

import static org.objectweb.asm.ClassReader.SKIP_DEBUG;

/**
 * This class transitively reads all interfaces and superclasses implemented or extended by a given class.  After visiting a class, one can call getInterfaces()
 * to get all interfaces/classes transitively implemented/extended by the visited class.  A single instance of InterfaceReader can be used more than once
 * in this fashion.
 *
 * @author adamd
 */
class InterfaceReader implements ClassVisitor {

    // this will store the final result of which interfaces are transitively implemented
    private String[] interfaces = null;

    public InterfaceReader() {
        super();
    }

    public InterfaceReader(String className) {
        ClassReader cr;
        try {
            cr = new ClassReader(className);
        } catch (IOException ioe) {
            ErrorReporter.report("Can't find the class \"" + className + "\", and this wasn't caught until the MethodData stage.", true);
            throw new InstrumentationException();
        }
        InterfaceReader ir = new InterfaceReader();
        cr.accept(ir, SKIP_DEBUG);

    }

    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        // first, put all interfaces/classes directly implemented/extended by the given class into result
        HashSet<String> result = new HashSet<String>();
        for (String i : interfaces) {
            result.add(i);
        }
        if (superName != null)
            result.add(superName);

        // now, for each element of result, use an InterfaceReader on it, so we recursively get all interfaces/classes transitively implemented/extended
        // by the given class.  The results will be stored in result2.
        HashSet<String> result2 = new HashSet<String>();
        for (String i : result) {
            ClassReader cr;
            try {
                cr = new ClassReader(i);
            } catch (IOException ioe) {
                ErrorReporter.report("Can't find the class \"" + i + "\", and this wasn't caught until the InterfaceReader stage.", true);
                continue;
            }
            InterfaceReader ir = new InterfaceReader();
            cr.accept(ir, SKIP_DEBUG);
            String[] ret = ir.getInterfaces();
            for (String j : ret)
                result2.add(j);
        }
        result2.addAll(result);

        this.interfaces = result2.toArray(new String[]{});
    }

    public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
        return null;
    }

    public void visitAttribute(Attribute attr) {
    }

    public void visitEnd() {
    }

    public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
        return null;
    }

    public void visitInnerClass(String name, String outerName, String innerName, int access) {
    }

    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        return null;
    }

    public void visitOuterClass(String owner, String name, String desc) {
    }

    public void visitSource(String source, String debug) {
    }


    /**
     * Returns all interfaces/classes transitively implemented/extended by the most recently visited class
     */
    public String[] getInterfaces() {
        return interfaces;
    }

}
