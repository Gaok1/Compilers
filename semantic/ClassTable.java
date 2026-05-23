import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;

/** Represents the class hierarchy and provides semantic analysis utilities. */
class ClassTable {
    private int semantErrors;
    private PrintStream errorStream;
    private HashMap<AbstractSymbol, class_> classMap;

    private void installBasicClasses() {
        AbstractSymbol filename = AbstractTable.stringtable.addString("<basic class>");

        class_ Object_class =
            new class_(0,
                TreeConstants.Object_,
                TreeConstants.No_class,
                new Features(0)
                    .appendElement(new method(0, TreeConstants.cool_abort,
                        new Formals(0), TreeConstants.Object_, new no_expr(0)))
                    .appendElement(new method(0, TreeConstants.type_name,
                        new Formals(0), TreeConstants.Str, new no_expr(0)))
                    .appendElement(new method(0, TreeConstants.copy,
                        new Formals(0), TreeConstants.SELF_TYPE, new no_expr(0))),
                filename);

        class_ IO_class =
            new class_(0,
                TreeConstants.IO,
                TreeConstants.Object_,
                new Features(0)
                    .appendElement(new method(0, TreeConstants.out_string,
                        new Formals(0).appendElement(new formal(0, TreeConstants.arg, TreeConstants.Str)),
                        TreeConstants.SELF_TYPE, new no_expr(0)))
                    .appendElement(new method(0, TreeConstants.out_int,
                        new Formals(0).appendElement(new formal(0, TreeConstants.arg, TreeConstants.Int)),
                        TreeConstants.SELF_TYPE, new no_expr(0)))
                    .appendElement(new method(0, TreeConstants.in_string,
                        new Formals(0), TreeConstants.Str, new no_expr(0)))
                    .appendElement(new method(0, TreeConstants.in_int,
                        new Formals(0), TreeConstants.Int, new no_expr(0))),
                filename);

        class_ Int_class =
            new class_(0,
                TreeConstants.Int,
                TreeConstants.Object_,
                new Features(0)
                    .appendElement(new attr(0, TreeConstants.val, TreeConstants.prim_slot, new no_expr(0))),
                filename);

        class_ Bool_class =
            new class_(0,
                TreeConstants.Bool,
                TreeConstants.Object_,
                new Features(0)
                    .appendElement(new attr(0, TreeConstants.val, TreeConstants.prim_slot, new no_expr(0))),
                filename);

        class_ Str_class =
            new class_(0,
                TreeConstants.Str,
                TreeConstants.Object_,
                new Features(0)
                    .appendElement(new attr(0, TreeConstants.val, TreeConstants.Int, new no_expr(0)))
                    .appendElement(new attr(0, TreeConstants.str_field, TreeConstants.prim_slot, new no_expr(0)))
                    .appendElement(new method(0, TreeConstants.length,
                        new Formals(0), TreeConstants.Int, new no_expr(0)))
                    .appendElement(new method(0, TreeConstants.concat,
                        new Formals(0).appendElement(new formal(0, TreeConstants.arg, TreeConstants.Str)),
                        TreeConstants.Str, new no_expr(0)))
                    .appendElement(new method(0, TreeConstants.substr,
                        new Formals(0)
                            .appendElement(new formal(0, TreeConstants.arg, TreeConstants.Int))
                            .appendElement(new formal(0, TreeConstants.arg2, TreeConstants.Int)),
                        TreeConstants.Str, new no_expr(0))),
                filename);

        classMap.put(TreeConstants.Object_, Object_class);
        classMap.put(TreeConstants.IO,      IO_class);
        classMap.put(TreeConstants.Int,     Int_class);
        classMap.put(TreeConstants.Bool,    Bool_class);
        classMap.put(TreeConstants.Str,     Str_class);
    }

    public ClassTable(Classes cls) {
        semantErrors = 0;
        errorStream  = System.err;
        classMap     = new HashMap<AbstractSymbol, class_>();

        installBasicClasses();

        // --- Pass 1a: register user-defined classes ---
        for (Enumeration e = cls.getElements(); e.hasMoreElements(); ) {
            class_ c = (class_) e.nextElement();
            AbstractSymbol name = c.getName();

            if (name.equals(TreeConstants.Object_) || name.equals(TreeConstants.IO)  ||
                name.equals(TreeConstants.Int)     || name.equals(TreeConstants.Bool) ||
                name.equals(TreeConstants.Str)) {
                semantError(c).println("Redefinition of basic class " + name + ".");
                continue;
            }
            if (classMap.containsKey(name)) {
                semantError(c).println("Class " + name + " was previously defined.");
                continue;
            }
            classMap.put(name, c);
        }

        // --- Pass 1b: check inheritance constraints ---
        for (Enumeration e = cls.getElements(); e.hasMoreElements(); ) {
            class_ c = (class_) e.nextElement();
            AbstractSymbol name   = c.getName();
            AbstractSymbol parent = c.getParent();

            if (!classMap.containsKey(name)) continue; // already reported

            if (parent.equals(TreeConstants.Int)  ||
                parent.equals(TreeConstants.Bool) ||
                parent.equals(TreeConstants.Str)) {
                semantError(c).println("Class " + name + " cannot inherit class " + parent + ".");
            } else if (!parent.equals(TreeConstants.No_class) && !classMap.containsKey(parent)) {
                semantError(c).println("Class " + name +
                    " inherits from an undefined class " + parent + ".");
            }
        }

        // --- Pass 1c: detect inheritance cycles ---
        checkCycles(cls);

        // --- Pass 1d: verify Main ---
        if (!classMap.containsKey(TreeConstants.Main)) {
            semantError().println("Class Main is not defined.");
        } else {
            class_ mainClass = classMap.get(TreeConstants.Main);
            boolean hasMain  = false;
            for (Enumeration e = mainClass.features.getElements(); e.hasMoreElements(); ) {
                Feature f = (Feature) e.nextElement();
                if (f instanceof method) {
                    method m = (method) f;
                    if (m.name.equals(TreeConstants.main_meth)) {
                        if (m.formals.getLength() != 0) {
                            semantError(mainClass).println(
                                "'main' method in class Main should have no arguments.");
                        }
                        hasMain = true;
                        break;
                    }
                }
            }
            if (!hasMain) {
                semantError(mainClass).println("No 'main' method in class Main.");
            }
        }
    }

    private void checkCycles(Classes cls) {
        for (Enumeration e = cls.getElements(); e.hasMoreElements(); ) {
            class_ c = (class_) e.nextElement();
            if (!classMap.containsKey(c.getName())) continue;

            HashSet<AbstractSymbol> visited = new HashSet<AbstractSymbol>();
            AbstractSymbol cur = c.getName();
            while (cur != null && !cur.equals(TreeConstants.No_class)) {
                if (visited.contains(cur)) {
                    semantError(c).println("Class " + c.getName() +
                        ", or an ancestor of " + c.getName() +
                        ", is involved in an inheritance cycle.");
                    break;
                }
                visited.add(cur);
                class_ curClass = classMap.get(cur);
                if (curClass == null) break;
                cur = curClass.getParent();
            }
        }
    }

    // ---------------------------------------------------------------
    // Type utilities
    // ---------------------------------------------------------------

    /** True if 'type' names a known class or is SELF_TYPE. */
    public boolean isValidType(AbstractSymbol type) {
        return type.equals(TreeConstants.SELF_TYPE) || classMap.containsKey(type);
    }

    /**
     * True if 'child' conforms to 'ancestor' in the context of 'currentClass'.
     * SELF_TYPE is resolved to currentClass before comparison.
     */
    public boolean isSubtype(AbstractSymbol child, AbstractSymbol ancestor,
                             AbstractSymbol currentClass) {
        if (child.equals(TreeConstants.SELF_TYPE) && ancestor.equals(TreeConstants.SELF_TYPE)) {
            return true;
        }
        AbstractSymbol rc = child.equals(TreeConstants.SELF_TYPE)    ? currentClass : child;
        AbstractSymbol ra = ancestor.equals(TreeConstants.SELF_TYPE) ? currentClass : ancestor;

        AbstractSymbol cur = rc;
        while (cur != null && !cur.equals(TreeConstants.No_class)) {
            if (cur.equals(ra)) return true;
            class_ c = classMap.get(cur);
            if (c == null) return false;
            cur = c.getParent();
        }
        return false;
    }

    /** Least upper bound of t1 and t2 in the context of currentClass. */
    public AbstractSymbol lub(AbstractSymbol t1, AbstractSymbol t2,
                              AbstractSymbol currentClass) {
        AbstractSymbol r1 = t1.equals(TreeConstants.SELF_TYPE) ? currentClass : t1;
        AbstractSymbol r2 = t2.equals(TreeConstants.SELF_TYPE) ? currentClass : t2;

        // Collect ancestors of r1 (from r1 to Object)
        ArrayList<AbstractSymbol> chain1 = new ArrayList<AbstractSymbol>();
        AbstractSymbol cur = r1;
        while (cur != null && !cur.equals(TreeConstants.No_class)) {
            chain1.add(cur);
            class_ c = classMap.get(cur);
            if (c == null) break;
            cur = c.getParent();
        }

        // Walk r2's chain; first match is the LUB
        cur = r2;
        while (cur != null && !cur.equals(TreeConstants.No_class)) {
            if (chain1.contains(cur)) return cur;
            class_ c = classMap.get(cur);
            if (c == null) break;
            cur = c.getParent();
        }
        return TreeConstants.Object_;
    }

    /** Returns the AST node for a class (null if not found). */
    public class_ getClass_(AbstractSymbol name) {
        return classMap.get(name);
    }

    /**
     * Returns the method environment for a class (name → method node),
     * including methods inherited from ancestor classes.
     * Child definitions override parent definitions.
     */
    public HashMap<AbstractSymbol, method> getMethodEnv(AbstractSymbol className) {
        HashMap<AbstractSymbol, method> env = new HashMap<AbstractSymbol, method>();
        if (className == null || className.equals(TreeConstants.No_class)) return env;

        class_ c = classMap.get(className);
        if (c == null) return env;

        // Inherited methods first (will be overridden by own below)
        env.putAll(getMethodEnv(c.getParent()));

        for (Enumeration e = c.features.getElements(); e.hasMoreElements(); ) {
            Feature f = (Feature) e.nextElement();
            if (f instanceof method) {
                method m = (method) f;
                env.put(m.name, m);
            }
        }
        return env;
    }

    /**
     * Returns the attribute environment for a class (name → declared type),
     * including attributes inherited from ancestor classes.
     */
    public HashMap<AbstractSymbol, AbstractSymbol> getAttrEnv(AbstractSymbol className) {
        HashMap<AbstractSymbol, AbstractSymbol> env =
            new HashMap<AbstractSymbol, AbstractSymbol>();
        if (className == null || className.equals(TreeConstants.No_class)) return env;

        class_ c = classMap.get(className);
        if (c == null) return env;

        env.putAll(getAttrEnv(c.getParent()));

        for (Enumeration e = c.features.getElements(); e.hasMoreElements(); ) {
            Feature f = (Feature) e.nextElement();
            if (f instanceof attr) {
                attr a = (attr) f;
                env.put(a.name, a.type_decl);
            }
        }
        return env;
    }

    // ---------------------------------------------------------------
    // Error reporting
    // ---------------------------------------------------------------

    public PrintStream semantError(class_ c) {
        return semantError(c.getFilename(), c);
    }

    public PrintStream semantError(AbstractSymbol filename, TreeNode t) {
        errorStream.print(filename + ":" + t.getLineNumber() + ": ");
        return semantError();
    }

    public PrintStream semantError() {
        semantErrors++;
        return errorStream;
    }

    public boolean errors() {
        return semantErrors != 0;
    }
}
