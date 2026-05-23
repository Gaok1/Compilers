// -*- mode: java -*- 
//
// file: cool-tree.m4
//
// This file defines the AST
//
//////////////////////////////////////////////////////////

import java.util.Enumeration;
import java.io.PrintStream;
import java.util.Vector;
import java.util.HashMap;
import java.util.HashSet;


/** Defines simple phylum Program */
abstract class Program extends TreeNode {
    protected Program(int lineNumber) {
        super(lineNumber);
    }
    public abstract void dump_with_types(PrintStream out, int n);
    public abstract void semant();

}


/** Defines simple phylum Class_ */
abstract class Class_ extends TreeNode {
    protected Class_(int lineNumber) {
        super(lineNumber);
    }
    public abstract void dump_with_types(PrintStream out, int n);

}


/** Defines list phylum Classes
    <p>
    See <a href="ListNode.html">ListNode</a> for full documentation. */
class Classes extends ListNode {
    public final static Class elementClass = Class_.class;
    /** Returns class of this lists's elements */
    public Class getElementClass() {
        return elementClass;
    }
    protected Classes(int lineNumber, Vector elements) {
        super(lineNumber, elements);
    }
    /** Creates an empty "Classes" list */
    public Classes(int lineNumber) {
        super(lineNumber);
    }
    /** Appends "Class_" element to this list */
    public Classes appendElement(TreeNode elem) {
        addElement(elem);
        return this;
    }
    public TreeNode copy() {
        return new Classes(lineNumber, copyElements());
    }
}


/** Defines simple phylum Feature */
abstract class Feature extends TreeNode {
    protected Feature(int lineNumber) {
        super(lineNumber);
    }
    public abstract void dump_with_types(PrintStream out, int n);

}


/** Defines list phylum Features
    <p>
    See <a href="ListNode.html">ListNode</a> for full documentation. */
class Features extends ListNode {
    public final static Class elementClass = Feature.class;
    /** Returns class of this lists's elements */
    public Class getElementClass() {
        return elementClass;
    }
    protected Features(int lineNumber, Vector elements) {
        super(lineNumber, elements);
    }
    /** Creates an empty "Features" list */
    public Features(int lineNumber) {
        super(lineNumber);
    }
    /** Appends "Feature" element to this list */
    public Features appendElement(TreeNode elem) {
        addElement(elem);
        return this;
    }
    public TreeNode copy() {
        return new Features(lineNumber, copyElements());
    }
}


/** Defines simple phylum Formal */
abstract class Formal extends TreeNode {
    protected Formal(int lineNumber) {
        super(lineNumber);
    }
    public abstract void dump_with_types(PrintStream out, int n);

}


/** Defines list phylum Formals
    <p>
    See <a href="ListNode.html">ListNode</a> for full documentation. */
class Formals extends ListNode {
    public final static Class elementClass = Formal.class;
    /** Returns class of this lists's elements */
    public Class getElementClass() {
        return elementClass;
    }
    protected Formals(int lineNumber, Vector elements) {
        super(lineNumber, elements);
    }
    /** Creates an empty "Formals" list */
    public Formals(int lineNumber) {
        super(lineNumber);
    }
    /** Appends "Formal" element to this list */
    public Formals appendElement(TreeNode elem) {
        addElement(elem);
        return this;
    }
    public TreeNode copy() {
        return new Formals(lineNumber, copyElements());
    }
}


/** Defines simple phylum Expression */
abstract class Expression extends TreeNode {
    protected Expression(int lineNumber) {
        super(lineNumber);
    }
    private AbstractSymbol type = null;                                 
    public AbstractSymbol get_type() { return type; }           
    public Expression set_type(AbstractSymbol s) { type = s; return this; } 
    public abstract void dump_with_types(PrintStream out, int n);
    public void dump_type(PrintStream out, int n) {
        if (type != null)
            { out.println(Utilities.pad(n) + ": " + type.getString()); }
        else
            { out.println(Utilities.pad(n) + ": _no_type"); }
    }

    /** Type-checks this expression and returns the inferred type symbol.
     *  Also sets the type field via set_type(). */
    public abstract AbstractSymbol typecheck(ClassTable ct, class_ currentClass,
                                             SymbolTable objEnv);
}


/** Defines list phylum Expressions
    <p>
    See <a href="ListNode.html">ListNode</a> for full documentation. */
class Expressions extends ListNode {
    public final static Class elementClass = Expression.class;
    /** Returns class of this lists's elements */
    public Class getElementClass() {
        return elementClass;
    }
    protected Expressions(int lineNumber, Vector elements) {
        super(lineNumber, elements);
    }
    /** Creates an empty "Expressions" list */
    public Expressions(int lineNumber) {
        super(lineNumber);
    }
    /** Appends "Expression" element to this list */
    public Expressions appendElement(TreeNode elem) {
        addElement(elem);
        return this;
    }
    public TreeNode copy() {
        return new Expressions(lineNumber, copyElements());
    }
}


/** Defines simple phylum Case */
abstract class Case extends TreeNode {
    protected Case(int lineNumber) {
        super(lineNumber);
    }
    public abstract void dump_with_types(PrintStream out, int n);

}


/** Defines list phylum Cases
    <p>
    See <a href="ListNode.html">ListNode</a> for full documentation. */
class Cases extends ListNode {
    public final static Class elementClass = Case.class;
    /** Returns class of this lists's elements */
    public Class getElementClass() {
        return elementClass;
    }
    protected Cases(int lineNumber, Vector elements) {
        super(lineNumber, elements);
    }
    /** Creates an empty "Cases" list */
    public Cases(int lineNumber) {
        super(lineNumber);
    }
    /** Appends "Case" element to this list */
    public Cases appendElement(TreeNode elem) {
        addElement(elem);
        return this;
    }
    public TreeNode copy() {
        return new Cases(lineNumber, copyElements());
    }
}


/** Defines AST constructor 'program'.
    <p>
    See <a href="TreeNode.html">TreeNode</a> for full documentation. */
class program extends Program {
    protected Classes classes;
    /** Creates "program" AST node. 
      *
      * @param lineNumber the line in the source file from which this node came.
      * @param a0 initial value for classes
      */
    public program(int lineNumber, Classes a1) {
        super(lineNumber);
        classes = a1;
    }
    public TreeNode copy() {
        return new program(lineNumber, (Classes)classes.copy());
    }
    public void dump(PrintStream out, int n) {
        out.print(Utilities.pad(n) + "program\n");
        classes.dump(out, n+2);
    }

    
    public void dump_with_types(PrintStream out, int n) {
        dump_line(out, n);
        out.println(Utilities.pad(n) + "_program");
        for (Enumeration e = classes.getElements(); e.hasMoreElements(); ) {
            // sm: changed 'n + 1' to 'n + 2' to match changes elsewhere
	    ((Class_)e.nextElement()).dump_with_types(out, n + 2);
        }
    }
    /** This method is the entry point to the semantic checker.  You will
        need to complete it in programming assignment 4.
	<p>
        Your checker should do the following two things:
	<ol>
	<li>Check that the program is semantically correct
	<li>Decorate the abstract syntax tree with type information
        by setting the type field in each Expression node.
        (see tree.h)
	</ol>
	<p>
	You are free to first do (1) and make sure you catch all semantic
    	errors. Part (2) can be done in a second stage when you want
	to test the complete compiler.
    */
    public void semant() {
        ClassTable classTable = new ClassTable(classes);

        if (classTable.errors()) {
            System.err.println("Compilation halted due to static semantic errors.");
            System.exit(1);
        }

        // Pass 2: type-check each user-defined class
        for (Enumeration e = classes.getElements(); e.hasMoreElements(); ) {
            ((class_) e.nextElement()).semant(classTable);
        }

        if (classTable.errors()) {
            System.err.println("Compilation halted due to static semantic errors.");
            System.exit(1);
        }
    }

}


/** Defines AST constructor 'class_'.
    <p>
    See <a href="TreeNode.html">TreeNode</a> for full documentation. */
class class_ extends Class_ {
    protected AbstractSymbol name;
    protected AbstractSymbol parent;
    protected Features features;
    protected AbstractSymbol filename;
    /** Creates "class_" AST node. 
      *
      * @param lineNumber the line in the source file from which this node came.
      * @param a0 initial value for name
      * @param a1 initial value for parent
      * @param a2 initial value for features
      * @param a3 initial value for filename
      */
    public class_(int lineNumber, AbstractSymbol a1, AbstractSymbol a2, Features a3, AbstractSymbol a4) {
        super(lineNumber);
        name = a1;
        parent = a2;
        features = a3;
        filename = a4;
    }
    public TreeNode copy() {
        return new class_(lineNumber, copy_AbstractSymbol(name), copy_AbstractSymbol(parent), (Features)features.copy(), copy_AbstractSymbol(filename));
    }
    public void dump(PrintStream out, int n) {
        out.print(Utilities.pad(n) + "class_\n");
        dump_AbstractSymbol(out, n+2, name);
        dump_AbstractSymbol(out, n+2, parent);
        features.dump(out, n+2);
        dump_AbstractSymbol(out, n+2, filename);
    }

    
    // sm: why were these three not in here already?
    // they are present in the PA3 cool-tree.java skeleton..
    public AbstractSymbol getName()     { return name; }
    public AbstractSymbol getParent()   { return parent; }
    public AbstractSymbol getFilename() { return filename; }

    public void dump_with_types(PrintStream out, int n) {
        dump_line(out, n);
        out.println(Utilities.pad(n) + "_class");
        dump_AbstractSymbol(out, n + 2, name);
        dump_AbstractSymbol(out, n + 2, parent);
        out.print(Utilities.pad(n + 2) + "\"");
        Utilities.printEscapedString(out, filename.getString());
        out.println("\"\n" + Utilities.pad(n + 2) + "(");
        for (Enumeration e = features.getElements(); e.hasMoreElements();) {
	    ((Feature)e.nextElement()).dump_with_types(out, n + 2);
        }
        out.println(Utilities.pad(n + 2) + ")");
    }

    public void semant(ClassTable ct) {
        SymbolTable objEnv = new SymbolTable();
        objEnv.enterScope();

        // 'self' is always in scope with type SELF_TYPE
        objEnv.addId(TreeConstants.self, TreeConstants.SELF_TYPE);

        // Collect inherited attributes (from parent chain)
        HashMap<AbstractSymbol, AbstractSymbol> inheritedAttrs = ct.getAttrEnv(parent);
        for (AbstractSymbol attrName : inheritedAttrs.keySet()) {
            objEnv.addId(attrName, inheritedAttrs.get(attrName));
        }

        // Register own attributes, checking for conflicts
        HashSet<AbstractSymbol> ownAttrs = new HashSet<AbstractSymbol>();
        for (Enumeration e = features.getElements(); e.hasMoreElements(); ) {
            Feature f = (Feature) e.nextElement();
            if (!(f instanceof attr)) continue;
            attr a = (attr) f;
            if (a.name.equals(TreeConstants.self)) {
                ct.semantError(this).println("'self' cannot be the name of an attribute.");
            } else if (inheritedAttrs.containsKey(a.name)) {
                ct.semantError(this).println("Attribute " + a.name +
                    " is an attribute of an inherited class.");
            } else if (ownAttrs.contains(a.name)) {
                ct.semantError(this).println("Attribute " + a.name +
                    " is multiply defined in class.");
            } else {
                ownAttrs.add(a.name);
                objEnv.addId(a.name, a.type_decl);
            }
        }

        // Type-check each feature
        for (Enumeration e = features.getElements(); e.hasMoreElements(); ) {
            Feature f = (Feature) e.nextElement();
            if (f instanceof attr) {
                ((attr) f).semant(ct, this, objEnv);
            } else if (f instanceof method) {
                ((method) f).semant(ct, this, objEnv);
            }
        }

        objEnv.exitScope();
    }

}


/** Defines AST constructor 'method'.
    <p>
    See <a href="TreeNode.html">TreeNode</a> for full documentation. */
class method extends Feature {
    protected AbstractSymbol name;
    protected Formals formals;
    protected AbstractSymbol return_type;
    protected Expression expr;
    /** Creates "method" AST node. 
      *
      * @param lineNumber the line in the source file from which this node came.
      * @param a0 initial value for name
      * @param a1 initial value for formals
      * @param a2 initial value for return_type
      * @param a3 initial value for expr
      */
    public method(int lineNumber, AbstractSymbol a1, Formals a2, AbstractSymbol a3, Expression a4) {
        super(lineNumber);
        name = a1;
        formals = a2;
        return_type = a3;
        expr = a4;
    }
    public TreeNode copy() {
        return new method(lineNumber, copy_AbstractSymbol(name), (Formals)formals.copy(), copy_AbstractSymbol(return_type), (Expression)expr.copy());
    }
    public void dump(PrintStream out, int n) {
        out.print(Utilities.pad(n) + "method\n");
        dump_AbstractSymbol(out, n+2, name);
        formals.dump(out, n+2);
        dump_AbstractSymbol(out, n+2, return_type);
        expr.dump(out, n+2);
    }

    
    public void dump_with_types(PrintStream out, int n) {
        dump_line(out, n);
        out.println(Utilities.pad(n) + "_method");
        dump_AbstractSymbol(out, n + 2, name);
        for (Enumeration e = formals.getElements(); e.hasMoreElements();) {
	    ((Formal)e.nextElement()).dump_with_types(out, n + 2);
        }
        dump_AbstractSymbol(out, n + 2, return_type);
	expr.dump_with_types(out, n + 2);
    }

    public void semant(ClassTable ct, class_ currentClass, SymbolTable objEnv) {
        // Validate return type
        if (!ct.isValidType(return_type)) {
            ct.semantError(currentClass.getFilename(), this).println(
                "Undefined return type " + return_type + " in method " + name + ".");
        }

        // Check override compatibility with parent
        AbstractSymbol parentName = currentClass.getParent();
        if (!parentName.equals(TreeConstants.No_class)) {
            method parentMethod = ct.getMethodEnv(parentName).get(name);
            if (parentMethod != null) {
                if (formals.getLength() != parentMethod.formals.getLength()) {
                    ct.semantError(currentClass.getFilename(), this).println(
                        "Incompatible number of formal parameters in redefined method " + name + ".");
                } else {
                    Enumeration myF = formals.getElements();
                    Enumeration pF  = parentMethod.formals.getElements();
                    while (myF.hasMoreElements()) {
                        formal mf = (formal) myF.nextElement();
                        formal pf = (formal) pF.nextElement();
                        if (!mf.type_decl.equals(pf.type_decl)) {
                            ct.semantError(currentClass.getFilename(), this).println(
                                "In redefined method " + name + ", parameter type " +
                                mf.type_decl + " is different from original type " + pf.type_decl + ".");
                        }
                    }
                }
                if (!return_type.equals(parentMethod.return_type)) {
                    ct.semantError(currentClass.getFilename(), this).println(
                        "In redefined method " + name + ", return type " + return_type +
                        " is different from original return type " + parentMethod.return_type + ".");
                }
            }
        }

        // Type-check body in a new scope with formals
        objEnv.enterScope();
        HashSet<AbstractSymbol> seen = new HashSet<AbstractSymbol>();
        for (Enumeration e = formals.getElements(); e.hasMoreElements(); ) {
            formal f = (formal) e.nextElement();
            if (f.name.equals(TreeConstants.self)) {
                ct.semantError(currentClass.getFilename(), f).println(
                    "'self' cannot be the name of a formal parameter.");
            } else if (!ct.isValidType(f.type_decl) || f.type_decl.equals(TreeConstants.SELF_TYPE)) {
                ct.semantError(currentClass.getFilename(), f).println(
                    "Class " + f.type_decl + " of formal parameter " + f.name + " is undefined.");
            } else if (seen.contains(f.name)) {
                ct.semantError(currentClass.getFilename(), f).println(
                    "Formal parameter " + f.name + " is multiply defined.");
            } else {
                seen.add(f.name);
                objEnv.addId(f.name, f.type_decl);
            }
        }

        AbstractSymbol bodyType = expr.typecheck(ct, currentClass, objEnv);
        objEnv.exitScope();

        // Verify body type conforms to declared return type
        if (ct.isValidType(return_type)) {
            if (!ct.isSubtype(bodyType, return_type, currentClass.getName())) {
                ct.semantError(currentClass.getFilename(), this).println(
                    "Inferred return type " + bodyType + " of method " + name +
                    " does not conform to declared return type " + return_type + ".");
            }
        }
    }

}


/** Defines AST constructor 'attr'.
    <p>
    See <a href="TreeNode.html">TreeNode</a> for full documentation. */
class attr extends Feature {
    protected AbstractSymbol name;
    protected AbstractSymbol type_decl;
    protected Expression init;
    /** Creates "attr" AST node. 
      *
      * @param lineNumber the line in the source file from which this node came.
      * @param a0 initial value for name
      * @param a1 initial value for type_decl
      * @param a2 initial value for init
      */
    public attr(int lineNumber, AbstractSymbol a1, AbstractSymbol a2, Expression a3) {
        super(lineNumber);
        name = a1;
        type_decl = a2;
        init = a3;
    }
    public TreeNode copy() {
        return new attr(lineNumber, copy_AbstractSymbol(name), copy_AbstractSymbol(type_decl), (Expression)init.copy());
    }
    public void dump(PrintStream out, int n) {
        out.print(Utilities.pad(n) + "attr\n");
        dump_AbstractSymbol(out, n+2, name);
        dump_AbstractSymbol(out, n+2, type_decl);
        init.dump(out, n+2);
    }

    
    public void dump_with_types(PrintStream out, int n) {
        dump_line(out, n);
        out.println(Utilities.pad(n) + "_attr");
        dump_AbstractSymbol(out, n + 2, name);
        dump_AbstractSymbol(out, n + 2, type_decl);
	init.dump_with_types(out, n + 2);
    }

    public void semant(ClassTable ct, class_ currentClass, SymbolTable objEnv) {
        if (!ct.isValidType(type_decl)) {
            ct.semantError(currentClass.getFilename(), this).println(
                "Class " + type_decl + " of attribute " + name + " is undefined.");
        }
        if (!(init instanceof no_expr)) {
            AbstractSymbol initType = init.typecheck(ct, currentClass, objEnv);
            if (ct.isValidType(type_decl) &&
                !ct.isSubtype(initType, type_decl, currentClass.getName())) {
                ct.semantError(currentClass.getFilename(), this).println(
                    "Inferred type " + initType + " of initialization of attribute " +
                    name + " does not conform to declared type " + type_decl + ".");
            }
        }
    }

}


/** Defines AST constructor 'formal'.
    <p>
    See <a href="TreeNode.html">TreeNode</a> for full documentation. */
class formal extends Formal {
    protected AbstractSymbol name;
    protected AbstractSymbol type_decl;
    /** Creates "formal" AST node. 
      *
      * @param lineNumber the line in the source file from which this node came.
      * @param a0 initial value for name
      * @param a1 initial value for type_decl
      */
    public formal(int lineNumber, AbstractSymbol a1, AbstractSymbol a2) {
        super(lineNumber);
        name = a1;
        type_decl = a2;
    }
    public TreeNode copy() {
        return new formal(lineNumber, copy_AbstractSymbol(name), copy_AbstractSymbol(type_decl));
    }
    public void dump(PrintStream out, int n) {
        out.print(Utilities.pad(n) + "formal\n");
        dump_AbstractSymbol(out, n+2, name);
        dump_AbstractSymbol(out, n+2, type_decl);
    }

    
    public void dump_with_types(PrintStream out, int n) {
        dump_line(out, n);
        out.println(Utilities.pad(n) + "_formal");
        dump_AbstractSymbol(out, n + 2, name);
        dump_AbstractSymbol(out, n + 2, type_decl);
    }

}


/** Defines AST constructor 'branch'.
    <p>
    See <a href="TreeNode.html">TreeNode</a> for full documentation. */
class branch extends Case {
    protected AbstractSymbol name;
    protected AbstractSymbol type_decl;
    protected Expression expr;
    /** Creates "branch" AST node. 
      *
      * @param lineNumber the line in the source file from which this node came.
      * @param a0 initial value for name
      * @param a1 initial value for type_decl
      * @param a2 initial value for expr
      */
    public branch(int lineNumber, AbstractSymbol a1, AbstractSymbol a2, Expression a3) {
        super(lineNumber);
        name = a1;
        type_decl = a2;
        expr = a3;
    }
    public TreeNode copy() {
        return new branch(lineNumber, copy_AbstractSymbol(name), copy_AbstractSymbol(type_decl), (Expression)expr.copy());
    }
    public void dump(PrintStream out, int n) {
        out.print(Utilities.pad(n) + "branch\n");
        dump_AbstractSymbol(out, n+2, name);
        dump_AbstractSymbol(out, n+2, type_decl);
        expr.dump(out, n+2);
    }

    
    public void dump_with_types(PrintStream out, int n) {
        dump_line(out, n);
        out.println(Utilities.pad(n) + "_branch");
        dump_AbstractSymbol(out, n + 2, name);
        dump_AbstractSymbol(out, n + 2, type_decl);
	expr.dump_with_types(out, n + 2);
    }

}


/** Defines AST constructor 'assign'.
    <p>
    See <a href="TreeNode.html">TreeNode</a> for full documentation. */
class assign extends Expression {
    protected AbstractSymbol name;
    protected Expression expr;
    /** Creates "assign" AST node. 
      *
      * @param lineNumber the line in the source file from which this node came.
      * @param a0 initial value for name
      * @param a1 initial value for expr
      */
    public assign(int lineNumber, AbstractSymbol a1, Expression a2) {
        super(lineNumber);
        name = a1;
        expr = a2;
    }
    public TreeNode copy() {
        return new assign(lineNumber, copy_AbstractSymbol(name), (Expression)expr.copy());
    }
    public void dump(PrintStream out, int n) {
        out.print(Utilities.pad(n) + "assign\n");
        dump_AbstractSymbol(out, n+2, name);
        expr.dump(out, n+2);
    }

    
    public void dump_with_types(PrintStream out, int n) {
        dump_line(out, n);
        out.println(Utilities.pad(n) + "_assign");
        dump_AbstractSymbol(out, n + 2, name);
	expr.dump_with_types(out, n + 2);
	dump_type(out, n);
    }

    public AbstractSymbol typecheck(ClassTable ct, class_ currentClass, SymbolTable objEnv) {
        if (name.equals(TreeConstants.self)) {
            ct.semantError(currentClass.getFilename(), this).println(
                "Cannot assign to 'self'.");
            return set_type(TreeConstants.Object_).get_type();
        }
        AbstractSymbol declaredType = (AbstractSymbol) objEnv.lookup(name);
        if (declaredType == null) {
            ct.semantError(currentClass.getFilename(), this).println(
                "Assignment to undeclared variable " + name + ".");
            expr.typecheck(ct, currentClass, objEnv);
            return set_type(TreeConstants.Object_).get_type();
        }
        AbstractSymbol exprType = expr.typecheck(ct, currentClass, objEnv);
        if (!ct.isSubtype(exprType, declaredType, currentClass.getName())) {
            ct.semantError(currentClass.getFilename(), this).println(
                "Type " + exprType + " of assigned expression does not conform to " +
                "declared type " + declaredType + " of identifier " + name + ".");
        }
        return set_type(exprType).get_type();
    }

}


/** Defines AST constructor 'static_dispatch'.
    <p>
    See <a href="TreeNode.html">TreeNode</a> for full documentation. */
class static_dispatch extends Expression {
    protected Expression expr;
    protected AbstractSymbol type_name;
    protected AbstractSymbol name;
    protected Expressions actual;
    /** Creates "static_dispatch" AST node. 
      *
      * @param lineNumber the line in the source file from which this node came.
      * @param a0 initial value for expr
      * @param a1 initial value for type_name
      * @param a2 initial value for name
      * @param a3 initial value for actual
      */
    public static_dispatch(int lineNumber, Expression a1, AbstractSymbol a2, AbstractSymbol a3, Expressions a4) {
        super(lineNumber);
        expr = a1;
        type_name = a2;
        name = a3;
        actual = a4;
    }
    public TreeNode copy() {
        return new static_dispatch(lineNumber, (Expression)expr.copy(), copy_AbstractSymbol(type_name), copy_AbstractSymbol(name), (Expressions)actual.copy());
    }
    public void dump(PrintStream out, int n) {
        out.print(Utilities.pad(n) + "static_dispatch\n");
        expr.dump(out, n+2);
        dump_AbstractSymbol(out, n+2, type_name);
        dump_AbstractSymbol(out, n+2, name);
        actual.dump(out, n+2);
    }

    
    public void dump_with_types(PrintStream out, int n) {
        dump_line(out, n);
        out.println(Utilities.pad(n) + "_static_dispatch");
	expr.dump_with_types(out, n + 2);
        dump_AbstractSymbol(out, n + 2, type_name);
        dump_AbstractSymbol(out, n + 2, name);
        out.println(Utilities.pad(n + 2) + "(");
        for (Enumeration e = actual.getElements(); e.hasMoreElements();) {
	    ((Expression)e.nextElement()).dump_with_types(out, n + 2);
        }
        out.println(Utilities.pad(n + 2) + ")");
	dump_type(out, n);
    }

    public AbstractSymbol typecheck(ClassTable ct, class_ currentClass, SymbolTable objEnv) {
        AbstractSymbol exprType = expr.typecheck(ct, currentClass, objEnv);

        if (!ct.isValidType(type_name) || type_name.equals(TreeConstants.SELF_TYPE)) {
            ct.semantError(currentClass.getFilename(), this).println(
                "Static dispatch to undefined class " + type_name + ".");
            for (Enumeration e = actual.getElements(); e.hasMoreElements(); )
                ((Expression) e.nextElement()).typecheck(ct, currentClass, objEnv);
            return set_type(TreeConstants.Object_).get_type();
        }

        if (!ct.isSubtype(exprType, type_name, currentClass.getName())) {
            ct.semantError(currentClass.getFilename(), this).println(
                "Expression type " + exprType +
                " does not conform to declared static dispatch type " + type_name + ".");
        }

        method m = ct.getMethodEnv(type_name).get(name);
        if (m == null) {
            ct.semantError(currentClass.getFilename(), this).println(
                "Static dispatch to undefined method " + name + ".");
            for (Enumeration e = actual.getElements(); e.hasMoreElements(); )
                ((Expression) e.nextElement()).typecheck(ct, currentClass, objEnv);
            return set_type(TreeConstants.Object_).get_type();
        }

        if (actual.getLength() != m.formals.getLength()) {
            ct.semantError(currentClass.getFilename(), this).println(
                "Method " + name + " called with wrong number of arguments.");
        }
        Enumeration actuals = actual.getElements();
        Enumeration formals = m.formals.getElements();
        while (actuals.hasMoreElements()) {
            Expression arg = (Expression) actuals.nextElement();
            AbstractSymbol argType = arg.typecheck(ct, currentClass, objEnv);
            if (formals.hasMoreElements()) {
                formal f = (formal) formals.nextElement();
                if (!ct.isSubtype(argType, f.type_decl, currentClass.getName())) {
                    ct.semantError(currentClass.getFilename(), arg).println(
                        "In call of method " + name + ", type " + argType +
                        " of parameter " + f.name +
                        " does not conform to declared type " + f.type_decl + ".");
                }
            }
        }

        AbstractSymbol retType = m.return_type;
        if (retType.equals(TreeConstants.SELF_TYPE)) retType = exprType;
        return set_type(retType).get_type();
    }

}


/** Defines AST constructor 'dispatch'.
    <p>
    See <a href="TreeNode.html">TreeNode</a> for full documentation. */
class dispatch extends Expression {
    protected Expression expr;
    protected AbstractSymbol name;
    protected Expressions actual;
    /** Creates "dispatch" AST node. 
      *
      * @param lineNumber the line in the source file from which this node came.
      * @param a0 initial value for expr
      * @param a1 initial value for name
      * @param a2 initial value for actual
      */
    public dispatch(int lineNumber, Expression a1, AbstractSymbol a2, Expressions a3) {
        super(lineNumber);
        expr = a1;
        name = a2;
        actual = a3;
    }
    public TreeNode copy() {
        return new dispatch(lineNumber, (Expression)expr.copy(), copy_AbstractSymbol(name), (Expressions)actual.copy());
    }
    public void dump(PrintStream out, int n) {
        out.print(Utilities.pad(n) + "dispatch\n");
        expr.dump(out, n+2);
        dump_AbstractSymbol(out, n+2, name);
        actual.dump(out, n+2);
    }

    
    public void dump_with_types(PrintStream out, int n) {
        dump_line(out, n);
        out.println(Utilities.pad(n) + "_dispatch");
	expr.dump_with_types(out, n + 2);
        dump_AbstractSymbol(out, n + 2, name);
        out.println(Utilities.pad(n + 2) + "(");
        for (Enumeration e = actual.getElements(); e.hasMoreElements();) {
	    ((Expression)e.nextElement()).dump_with_types(out, n + 2);
        }
        out.println(Utilities.pad(n + 2) + ")");
	dump_type(out, n);
    }

    public AbstractSymbol typecheck(ClassTable ct, class_ currentClass, SymbolTable objEnv) {
        AbstractSymbol exprType = expr.typecheck(ct, currentClass, objEnv);
        AbstractSymbol dispatchClass =
            exprType.equals(TreeConstants.SELF_TYPE) ? currentClass.getName() : exprType;

        method m = ct.getMethodEnv(dispatchClass).get(name);
        if (m == null) {
            ct.semantError(currentClass.getFilename(), this).println(
                "Dispatch to undefined method " + name + ".");
            for (Enumeration e = actual.getElements(); e.hasMoreElements(); )
                ((Expression) e.nextElement()).typecheck(ct, currentClass, objEnv);
            return set_type(TreeConstants.Object_).get_type();
        }

        if (actual.getLength() != m.formals.getLength()) {
            ct.semantError(currentClass.getFilename(), this).println(
                "Method " + name + " called with wrong number of arguments.");
        }
        Enumeration actuals = actual.getElements();
        Enumeration formals = m.formals.getElements();
        while (actuals.hasMoreElements()) {
            Expression arg = (Expression) actuals.nextElement();
            AbstractSymbol argType = arg.typecheck(ct, currentClass, objEnv);
            if (formals.hasMoreElements()) {
                formal f = (formal) formals.nextElement();
                if (!ct.isSubtype(argType, f.type_decl, currentClass.getName())) {
                    ct.semantError(currentClass.getFilename(), arg).println(
                        "In call of method " + name + ", type " + argType +
                        " of parameter " + f.name +
                        " does not conform to declared type " + f.type_decl + ".");
                }
            }
        }

        AbstractSymbol retType = m.return_type;
        if (retType.equals(TreeConstants.SELF_TYPE)) retType = exprType;
        return set_type(retType).get_type();
    }

}


/** Defines AST constructor 'cond'.
    <p>
    See <a href="TreeNode.html">TreeNode</a> for full documentation. */
class cond extends Expression {
    protected Expression pred;
    protected Expression then_exp;
    protected Expression else_exp;
    /** Creates "cond" AST node. 
      *
      * @param lineNumber the line in the source file from which this node came.
      * @param a0 initial value for pred
      * @param a1 initial value for then_exp
      * @param a2 initial value for else_exp
      */
    public cond(int lineNumber, Expression a1, Expression a2, Expression a3) {
        super(lineNumber);
        pred = a1;
        then_exp = a2;
        else_exp = a3;
    }
    public TreeNode copy() {
        return new cond(lineNumber, (Expression)pred.copy(), (Expression)then_exp.copy(), (Expression)else_exp.copy());
    }
    public void dump(PrintStream out, int n) {
        out.print(Utilities.pad(n) + "cond\n");
        pred.dump(out, n+2);
        then_exp.dump(out, n+2);
        else_exp.dump(out, n+2);
    }

    
    public void dump_with_types(PrintStream out, int n) {
        dump_line(out, n);
        out.println(Utilities.pad(n) + "_cond");
	pred.dump_with_types(out, n + 2);
	then_exp.dump_with_types(out, n + 2);
	else_exp.dump_with_types(out, n + 2);
	dump_type(out, n);
    }

    public AbstractSymbol typecheck(ClassTable ct, class_ currentClass, SymbolTable objEnv) {
        AbstractSymbol predType = pred.typecheck(ct, currentClass, objEnv);
        if (!predType.equals(TreeConstants.Bool)) {
            ct.semantError(currentClass.getFilename(), this).println(
                "Predicate of 'if' does not have type Bool.");
        }
        AbstractSymbol thenType = then_exp.typecheck(ct, currentClass, objEnv);
        AbstractSymbol elseType = else_exp.typecheck(ct, currentClass, objEnv);
        return set_type(ct.lub(thenType, elseType, currentClass.getName())).get_type();
    }

}


/** Defines AST constructor 'loop'.
    <p>
    See <a href="TreeNode.html">TreeNode</a> for full documentation. */
class loop extends Expression {
    protected Expression pred;
    protected Expression body;
    /** Creates "loop" AST node. 
      *
      * @param lineNumber the line in the source file from which this node came.
      * @param a0 initial value for pred
      * @param a1 initial value for body
      */
    public loop(int lineNumber, Expression a1, Expression a2) {
        super(lineNumber);
        pred = a1;
        body = a2;
    }
    public TreeNode copy() {
        return new loop(lineNumber, (Expression)pred.copy(), (Expression)body.copy());
    }
    public void dump(PrintStream out, int n) {
        out.print(Utilities.pad(n) + "loop\n");
        pred.dump(out, n+2);
        body.dump(out, n+2);
    }

    
    public void dump_with_types(PrintStream out, int n) {
        dump_line(out, n);
        out.println(Utilities.pad(n) + "_loop");
	pred.dump_with_types(out, n + 2);
	body.dump_with_types(out, n + 2);
	dump_type(out, n);
    }

    public AbstractSymbol typecheck(ClassTable ct, class_ currentClass, SymbolTable objEnv) {
        AbstractSymbol predType = pred.typecheck(ct, currentClass, objEnv);
        if (!predType.equals(TreeConstants.Bool)) {
            ct.semantError(currentClass.getFilename(), this).println(
                "Loop condition does not have type Bool.");
        }
        body.typecheck(ct, currentClass, objEnv);
        return set_type(TreeConstants.Object_).get_type();
    }

}


/** Defines AST constructor 'typcase'.
    <p>
    See <a href="TreeNode.html">TreeNode</a> for full documentation. */
class typcase extends Expression {
    protected Expression expr;
    protected Cases cases;
    /** Creates "typcase" AST node. 
      *
      * @param lineNumber the line in the source file from which this node came.
      * @param a0 initial value for expr
      * @param a1 initial value for cases
      */
    public typcase(int lineNumber, Expression a1, Cases a2) {
        super(lineNumber);
        expr = a1;
        cases = a2;
    }
    public TreeNode copy() {
        return new typcase(lineNumber, (Expression)expr.copy(), (Cases)cases.copy());
    }
    public void dump(PrintStream out, int n) {
        out.print(Utilities.pad(n) + "typcase\n");
        expr.dump(out, n+2);
        cases.dump(out, n+2);
    }

    
    public void dump_with_types(PrintStream out, int n) {
        dump_line(out, n);
        out.println(Utilities.pad(n) + "_typcase");
	expr.dump_with_types(out, n + 2);
        for (Enumeration e = cases.getElements(); e.hasMoreElements();) {
	    ((Case)e.nextElement()).dump_with_types(out, n + 2);
        }
	dump_type(out, n);
    }

    public AbstractSymbol typecheck(ClassTable ct, class_ currentClass, SymbolTable objEnv) {
        expr.typecheck(ct, currentClass, objEnv);

        HashSet<AbstractSymbol> seenTypes = new HashSet<AbstractSymbol>();
        AbstractSymbol resultType = null;
        for (Enumeration e = cases.getElements(); e.hasMoreElements(); ) {
            branch b = (branch) e.nextElement();
            if (seenTypes.contains(b.type_decl)) {
                ct.semantError(currentClass.getFilename(), b).println(
                    "Duplicate branch " + b.type_decl + " in case statement.");
            } else {
                seenTypes.add(b.type_decl);
            }
            if (!ct.isValidType(b.type_decl) || b.type_decl.equals(TreeConstants.SELF_TYPE)) {
                ct.semantError(currentClass.getFilename(), b).println(
                    "Class " + b.type_decl + " of case branch is undefined.");
            }
            objEnv.enterScope();
            objEnv.addId(b.name, b.type_decl);
            AbstractSymbol branchType = b.expr.typecheck(ct, currentClass, objEnv);
            objEnv.exitScope();
            resultType = (resultType == null)
                ? branchType
                : ct.lub(resultType, branchType, currentClass.getName());
        }
        if (resultType == null) resultType = TreeConstants.Object_;
        return set_type(resultType).get_type();
    }

}


/** Defines AST constructor 'block'.
    <p>
    See <a href="TreeNode.html">TreeNode</a> for full documentation. */
class block extends Expression {
    protected Expressions body;
    /** Creates "block" AST node. 
      *
      * @param lineNumber the line in the source file from which this node came.
      * @param a0 initial value for body
      */
    public block(int lineNumber, Expressions a1) {
        super(lineNumber);
        body = a1;
    }
    public TreeNode copy() {
        return new block(lineNumber, (Expressions)body.copy());
    }
    public void dump(PrintStream out, int n) {
        out.print(Utilities.pad(n) + "block\n");
        body.dump(out, n+2);
    }

    
    public void dump_with_types(PrintStream out, int n) {
        dump_line(out, n);
        out.println(Utilities.pad(n) + "_block");
        for (Enumeration e = body.getElements(); e.hasMoreElements();) {
	    ((Expression)e.nextElement()).dump_with_types(out, n + 2);
        }
	dump_type(out, n);
    }

    public AbstractSymbol typecheck(ClassTable ct, class_ currentClass, SymbolTable objEnv) {
        AbstractSymbol lastType = TreeConstants.Object_;
        for (Enumeration e = body.getElements(); e.hasMoreElements(); ) {
            lastType = ((Expression) e.nextElement()).typecheck(ct, currentClass, objEnv);
        }
        return set_type(lastType).get_type();
    }

}


/** Defines AST constructor 'let'.
    <p>
    See <a href="TreeNode.html">TreeNode</a> for full documentation. */
class let extends Expression {
    protected AbstractSymbol identifier;
    protected AbstractSymbol type_decl;
    protected Expression init;
    protected Expression body;
    /** Creates "let" AST node. 
      *
      * @param lineNumber the line in the source file from which this node came.
      * @param a0 initial value for identifier
      * @param a1 initial value for type_decl
      * @param a2 initial value for init
      * @param a3 initial value for body
      */
    public let(int lineNumber, AbstractSymbol a1, AbstractSymbol a2, Expression a3, Expression a4) {
        super(lineNumber);
        identifier = a1;
        type_decl = a2;
        init = a3;
        body = a4;
    }
    public TreeNode copy() {
        return new let(lineNumber, copy_AbstractSymbol(identifier), copy_AbstractSymbol(type_decl), (Expression)init.copy(), (Expression)body.copy());
    }
    public void dump(PrintStream out, int n) {
        out.print(Utilities.pad(n) + "let\n");
        dump_AbstractSymbol(out, n+2, identifier);
        dump_AbstractSymbol(out, n+2, type_decl);
        init.dump(out, n+2);
        body.dump(out, n+2);
    }

    
    public void dump_with_types(PrintStream out, int n) {
        dump_line(out, n);
        out.println(Utilities.pad(n) + "_let");
	dump_AbstractSymbol(out, n + 2, identifier);
	dump_AbstractSymbol(out, n + 2, type_decl);
	init.dump_with_types(out, n + 2);
	body.dump_with_types(out, n + 2);
	dump_type(out, n);
    }

    public AbstractSymbol typecheck(ClassTable ct, class_ currentClass, SymbolTable objEnv) {
        if (identifier.equals(TreeConstants.self)) {
            ct.semantError(currentClass.getFilename(), this).println(
                "'self' cannot be bound in a 'let' expression.");
        }
        if (!ct.isValidType(type_decl)) {
            ct.semantError(currentClass.getFilename(), this).println(
                "Class " + type_decl + " of let-bound identifier " + identifier +
                " is undefined.");
        }
        if (!(init instanceof no_expr)) {
            AbstractSymbol initType = init.typecheck(ct, currentClass, objEnv);
            if (ct.isValidType(type_decl) &&
                !ct.isSubtype(initType, type_decl, currentClass.getName())) {
                ct.semantError(currentClass.getFilename(), this).println(
                    "Inferred type " + initType + " of initialization of " + identifier +
                    " does not conform to identifier's declared type " + type_decl + ".");
            }
        }
        objEnv.enterScope();
        objEnv.addId(identifier, type_decl);
        AbstractSymbol bodyType = body.typecheck(ct, currentClass, objEnv);
        objEnv.exitScope();
        return set_type(bodyType).get_type();
    }

}


/** Defines AST constructor 'plus'.
    <p>
    See <a href="TreeNode.html">TreeNode</a> for full documentation. */
class plus extends Expression {
    protected Expression e1;
    protected Expression e2;
    /** Creates "plus" AST node. 
      *
      * @param lineNumber the line in the source file from which this node came.
      * @param a0 initial value for e1
      * @param a1 initial value for e2
      */
    public plus(int lineNumber, Expression a1, Expression a2) {
        super(lineNumber);
        e1 = a1;
        e2 = a2;
    }
    public TreeNode copy() {
        return new plus(lineNumber, (Expression)e1.copy(), (Expression)e2.copy());
    }
    public void dump(PrintStream out, int n) {
        out.print(Utilities.pad(n) + "plus\n");
        e1.dump(out, n+2);
        e2.dump(out, n+2);
    }

    
    public void dump_with_types(PrintStream out, int n) {
        dump_line(out, n);
        out.println(Utilities.pad(n) + "_plus");
	e1.dump_with_types(out, n + 2);
	e2.dump_with_types(out, n + 2);
	dump_type(out, n);
    }

    public AbstractSymbol typecheck(ClassTable ct, class_ currentClass, SymbolTable objEnv) {
        AbstractSymbol t1 = e1.typecheck(ct, currentClass, objEnv);
        AbstractSymbol t2 = e2.typecheck(ct, currentClass, objEnv);
        if (!t1.equals(TreeConstants.Int) || !t2.equals(TreeConstants.Int)) {
            ct.semantError(currentClass.getFilename(), this).println(
                "non-Int arguments: " + t1 + " + " + t2);
        }
        return set_type(TreeConstants.Int).get_type();
    }

}


/** Defines AST constructor 'sub'.
    <p>
    See <a href="TreeNode.html">TreeNode</a> for full documentation. */
class sub extends Expression {
    protected Expression e1;
    protected Expression e2;
    /** Creates "sub" AST node. 
      *
      * @param lineNumber the line in the source file from which this node came.
      * @param a0 initial value for e1
      * @param a1 initial value for e2
      */
    public sub(int lineNumber, Expression a1, Expression a2) {
        super(lineNumber);
        e1 = a1;
        e2 = a2;
    }
    public TreeNode copy() {
        return new sub(lineNumber, (Expression)e1.copy(), (Expression)e2.copy());
    }
    public void dump(PrintStream out, int n) {
        out.print(Utilities.pad(n) + "sub\n");
        e1.dump(out, n+2);
        e2.dump(out, n+2);
    }

    
    public void dump_with_types(PrintStream out, int n) {
        dump_line(out, n);
        out.println(Utilities.pad(n) + "_sub");
	e1.dump_with_types(out, n + 2);
	e2.dump_with_types(out, n + 2);
	dump_type(out, n);
    }

    public AbstractSymbol typecheck(ClassTable ct, class_ currentClass, SymbolTable objEnv) {
        AbstractSymbol t1 = e1.typecheck(ct, currentClass, objEnv);
        AbstractSymbol t2 = e2.typecheck(ct, currentClass, objEnv);
        if (!t1.equals(TreeConstants.Int) || !t2.equals(TreeConstants.Int)) {
            ct.semantError(currentClass.getFilename(), this).println(
                "non-Int arguments: " + t1 + " - " + t2);
        }
        return set_type(TreeConstants.Int).get_type();
    }

}


/** Defines AST constructor 'mul'.
    <p>
    See <a href="TreeNode.html">TreeNode</a> for full documentation. */
class mul extends Expression {
    protected Expression e1;
    protected Expression e2;
    /** Creates "mul" AST node. 
      *
      * @param lineNumber the line in the source file from which this node came.
      * @param a0 initial value for e1
      * @param a1 initial value for e2
      */
    public mul(int lineNumber, Expression a1, Expression a2) {
        super(lineNumber);
        e1 = a1;
        e2 = a2;
    }
    public TreeNode copy() {
        return new mul(lineNumber, (Expression)e1.copy(), (Expression)e2.copy());
    }
    public void dump(PrintStream out, int n) {
        out.print(Utilities.pad(n) + "mul\n");
        e1.dump(out, n+2);
        e2.dump(out, n+2);
    }

    
    public void dump_with_types(PrintStream out, int n) {
        dump_line(out, n);
        out.println(Utilities.pad(n) + "_mul");
	e1.dump_with_types(out, n + 2);
	e2.dump_with_types(out, n + 2);
	dump_type(out, n);
    }

    public AbstractSymbol typecheck(ClassTable ct, class_ currentClass, SymbolTable objEnv) {
        AbstractSymbol t1 = e1.typecheck(ct, currentClass, objEnv);
        AbstractSymbol t2 = e2.typecheck(ct, currentClass, objEnv);
        if (!t1.equals(TreeConstants.Int) || !t2.equals(TreeConstants.Int)) {
            ct.semantError(currentClass.getFilename(), this).println(
                "non-Int arguments: " + t1 + " * " + t2);
        }
        return set_type(TreeConstants.Int).get_type();
    }

}


/** Defines AST constructor 'divide'.
    <p>
    See <a href="TreeNode.html">TreeNode</a> for full documentation. */
class divide extends Expression {
    protected Expression e1;
    protected Expression e2;
    /** Creates "divide" AST node. 
      *
      * @param lineNumber the line in the source file from which this node came.
      * @param a0 initial value for e1
      * @param a1 initial value for e2
      */
    public divide(int lineNumber, Expression a1, Expression a2) {
        super(lineNumber);
        e1 = a1;
        e2 = a2;
    }
    public TreeNode copy() {
        return new divide(lineNumber, (Expression)e1.copy(), (Expression)e2.copy());
    }
    public void dump(PrintStream out, int n) {
        out.print(Utilities.pad(n) + "divide\n");
        e1.dump(out, n+2);
        e2.dump(out, n+2);
    }

    
    public void dump_with_types(PrintStream out, int n) {
        dump_line(out, n);
        out.println(Utilities.pad(n) + "_divide");
	e1.dump_with_types(out, n + 2);
	e2.dump_with_types(out, n + 2);
	dump_type(out, n);
    }

    public AbstractSymbol typecheck(ClassTable ct, class_ currentClass, SymbolTable objEnv) {
        AbstractSymbol t1 = e1.typecheck(ct, currentClass, objEnv);
        AbstractSymbol t2 = e2.typecheck(ct, currentClass, objEnv);
        if (!t1.equals(TreeConstants.Int) || !t2.equals(TreeConstants.Int)) {
            ct.semantError(currentClass.getFilename(), this).println(
                "non-Int arguments: " + t1 + " / " + t2);
        }
        return set_type(TreeConstants.Int).get_type();
    }

}


/** Defines AST constructor 'neg'.
    <p>
    See <a href="TreeNode.html">TreeNode</a> for full documentation. */
class neg extends Expression {
    protected Expression e1;
    /** Creates "neg" AST node. 
      *
      * @param lineNumber the line in the source file from which this node came.
      * @param a0 initial value for e1
      */
    public neg(int lineNumber, Expression a1) {
        super(lineNumber);
        e1 = a1;
    }
    public TreeNode copy() {
        return new neg(lineNumber, (Expression)e1.copy());
    }
    public void dump(PrintStream out, int n) {
        out.print(Utilities.pad(n) + "neg\n");
        e1.dump(out, n+2);
    }

    
    public void dump_with_types(PrintStream out, int n) {
        dump_line(out, n);
        out.println(Utilities.pad(n) + "_neg");
	e1.dump_with_types(out, n + 2);
	dump_type(out, n);
    }

    public AbstractSymbol typecheck(ClassTable ct, class_ currentClass, SymbolTable objEnv) {
        AbstractSymbol t = e1.typecheck(ct, currentClass, objEnv);
        if (!t.equals(TreeConstants.Int)) {
            ct.semantError(currentClass.getFilename(), this).println(
                "Argument of '~' has type " + t + " instead of Int.");
        }
        return set_type(TreeConstants.Int).get_type();
    }

}


/** Defines AST constructor 'lt'.
    <p>
    See <a href="TreeNode.html">TreeNode</a> for full documentation. */
class lt extends Expression {
    protected Expression e1;
    protected Expression e2;
    /** Creates "lt" AST node. 
      *
      * @param lineNumber the line in the source file from which this node came.
      * @param a0 initial value for e1
      * @param a1 initial value for e2
      */
    public lt(int lineNumber, Expression a1, Expression a2) {
        super(lineNumber);
        e1 = a1;
        e2 = a2;
    }
    public TreeNode copy() {
        return new lt(lineNumber, (Expression)e1.copy(), (Expression)e2.copy());
    }
    public void dump(PrintStream out, int n) {
        out.print(Utilities.pad(n) + "lt\n");
        e1.dump(out, n+2);
        e2.dump(out, n+2);
    }

    
    public void dump_with_types(PrintStream out, int n) {
        dump_line(out, n);
        out.println(Utilities.pad(n) + "_lt");
	e1.dump_with_types(out, n + 2);
	e2.dump_with_types(out, n + 2);
	dump_type(out, n);
    }

    public AbstractSymbol typecheck(ClassTable ct, class_ currentClass, SymbolTable objEnv) {
        AbstractSymbol t1 = e1.typecheck(ct, currentClass, objEnv);
        AbstractSymbol t2 = e2.typecheck(ct, currentClass, objEnv);
        if (!t1.equals(TreeConstants.Int) || !t2.equals(TreeConstants.Int)) {
            ct.semantError(currentClass.getFilename(), this).println(
                "non-Int arguments: " + t1 + " < " + t2);
        }
        return set_type(TreeConstants.Bool).get_type();
    }

}


/** Defines AST constructor 'eq'.
    <p>
    See <a href="TreeNode.html">TreeNode</a> for full documentation. */
class eq extends Expression {
    protected Expression e1;
    protected Expression e2;
    /** Creates "eq" AST node. 
      *
      * @param lineNumber the line in the source file from which this node came.
      * @param a0 initial value for e1
      * @param a1 initial value for e2
      */
    public eq(int lineNumber, Expression a1, Expression a2) {
        super(lineNumber);
        e1 = a1;
        e2 = a2;
    }
    public TreeNode copy() {
        return new eq(lineNumber, (Expression)e1.copy(), (Expression)e2.copy());
    }
    public void dump(PrintStream out, int n) {
        out.print(Utilities.pad(n) + "eq\n");
        e1.dump(out, n+2);
        e2.dump(out, n+2);
    }

    
    public void dump_with_types(PrintStream out, int n) {
        dump_line(out, n);
        out.println(Utilities.pad(n) + "_eq");
	e1.dump_with_types(out, n + 2);
	e2.dump_with_types(out, n + 2);
	dump_type(out, n);
    }

    public AbstractSymbol typecheck(ClassTable ct, class_ currentClass, SymbolTable objEnv) {
        AbstractSymbol t1 = e1.typecheck(ct, currentClass, objEnv);
        AbstractSymbol t2 = e2.typecheck(ct, currentClass, objEnv);
        boolean t1Prim = t1.equals(TreeConstants.Int) || t1.equals(TreeConstants.Bool) ||
                         t1.equals(TreeConstants.Str);
        boolean t2Prim = t2.equals(TreeConstants.Int) || t2.equals(TreeConstants.Bool) ||
                         t2.equals(TreeConstants.Str);
        if ((t1Prim || t2Prim) && !t1.equals(t2)) {
            ct.semantError(currentClass.getFilename(), this).println(
                "Illegal comparison with a basic type.");
        }
        return set_type(TreeConstants.Bool).get_type();
    }

}


/** Defines AST constructor 'leq'.
    <p>
    See <a href="TreeNode.html">TreeNode</a> for full documentation. */
class leq extends Expression {
    protected Expression e1;
    protected Expression e2;
    /** Creates "leq" AST node. 
      *
      * @param lineNumber the line in the source file from which this node came.
      * @param a0 initial value for e1
      * @param a1 initial value for e2
      */
    public leq(int lineNumber, Expression a1, Expression a2) {
        super(lineNumber);
        e1 = a1;
        e2 = a2;
    }
    public TreeNode copy() {
        return new leq(lineNumber, (Expression)e1.copy(), (Expression)e2.copy());
    }
    public void dump(PrintStream out, int n) {
        out.print(Utilities.pad(n) + "leq\n");
        e1.dump(out, n+2);
        e2.dump(out, n+2);
    }

    
    public void dump_with_types(PrintStream out, int n) {
        dump_line(out, n);
        out.println(Utilities.pad(n) + "_leq");
	e1.dump_with_types(out, n + 2);
	e2.dump_with_types(out, n + 2);
	dump_type(out, n);
    }

    public AbstractSymbol typecheck(ClassTable ct, class_ currentClass, SymbolTable objEnv) {
        AbstractSymbol t1 = e1.typecheck(ct, currentClass, objEnv);
        AbstractSymbol t2 = e2.typecheck(ct, currentClass, objEnv);
        if (!t1.equals(TreeConstants.Int) || !t2.equals(TreeConstants.Int)) {
            ct.semantError(currentClass.getFilename(), this).println(
                "non-Int arguments: " + t1 + " <= " + t2);
        }
        return set_type(TreeConstants.Bool).get_type();
    }

}


/** Defines AST constructor 'comp'.
    <p>
    See <a href="TreeNode.html">TreeNode</a> for full documentation. */
class comp extends Expression {
    protected Expression e1;
    /** Creates "comp" AST node. 
      *
      * @param lineNumber the line in the source file from which this node came.
      * @param a0 initial value for e1
      */
    public comp(int lineNumber, Expression a1) {
        super(lineNumber);
        e1 = a1;
    }
    public TreeNode copy() {
        return new comp(lineNumber, (Expression)e1.copy());
    }
    public void dump(PrintStream out, int n) {
        out.print(Utilities.pad(n) + "comp\n");
        e1.dump(out, n+2);
    }

    
    public void dump_with_types(PrintStream out, int n) {
        dump_line(out, n);
        out.println(Utilities.pad(n) + "_comp");
	e1.dump_with_types(out, n + 2);
	dump_type(out, n);
    }

    public AbstractSymbol typecheck(ClassTable ct, class_ currentClass, SymbolTable objEnv) {
        AbstractSymbol t = e1.typecheck(ct, currentClass, objEnv);
        if (!t.equals(TreeConstants.Bool)) {
            ct.semantError(currentClass.getFilename(), this).println(
                "Argument of 'not' has type " + t + " instead of Bool.");
        }
        return set_type(TreeConstants.Bool).get_type();
    }

}


/** Defines AST constructor 'int_const'.
    <p>
    See <a href="TreeNode.html">TreeNode</a> for full documentation. */
class int_const extends Expression {
    protected AbstractSymbol token;
    /** Creates "int_const" AST node. 
      *
      * @param lineNumber the line in the source file from which this node came.
      * @param a0 initial value for token
      */
    public int_const(int lineNumber, AbstractSymbol a1) {
        super(lineNumber);
        token = a1;
    }
    public TreeNode copy() {
        return new int_const(lineNumber, copy_AbstractSymbol(token));
    }
    public void dump(PrintStream out, int n) {
        out.print(Utilities.pad(n) + "int_const\n");
        dump_AbstractSymbol(out, n+2, token);
    }

    
    public void dump_with_types(PrintStream out, int n) {
        dump_line(out, n);
        out.println(Utilities.pad(n) + "_int");
	dump_AbstractSymbol(out, n + 2, token);
	dump_type(out, n);
    }

    public AbstractSymbol typecheck(ClassTable ct, class_ currentClass, SymbolTable objEnv) {
        return set_type(TreeConstants.Int).get_type();
    }

}


/** Defines AST constructor 'bool_const'.
    <p>
    See <a href="TreeNode.html">TreeNode</a> for full documentation. */
class bool_const extends Expression {
    protected Boolean val;
    /** Creates "bool_const" AST node. 
      *
      * @param lineNumber the line in the source file from which this node came.
      * @param a0 initial value for val
      */
    public bool_const(int lineNumber, Boolean a1) {
        super(lineNumber);
        val = a1;
    }
    public TreeNode copy() {
        return new bool_const(lineNumber, copy_Boolean(val));
    }
    public void dump(PrintStream out, int n) {
        out.print(Utilities.pad(n) + "bool_const\n");
        dump_Boolean(out, n+2, val);
    }

    
    public void dump_with_types(PrintStream out, int n) {
        dump_line(out, n);
        out.println(Utilities.pad(n) + "_bool");
	dump_Boolean(out, n + 2, val);
	dump_type(out, n);
    }

    public AbstractSymbol typecheck(ClassTable ct, class_ currentClass, SymbolTable objEnv) {
        return set_type(TreeConstants.Bool).get_type();
    }

}


/** Defines AST constructor 'string_const'.
    <p>
    See <a href="TreeNode.html">TreeNode</a> for full documentation. */
class string_const extends Expression {
    protected AbstractSymbol token;
    /** Creates "string_const" AST node. 
      *
      * @param lineNumber the line in the source file from which this node came.
      * @param a0 initial value for token
      */
    public string_const(int lineNumber, AbstractSymbol a1) {
        super(lineNumber);
        token = a1;
    }
    public TreeNode copy() {
        return new string_const(lineNumber, copy_AbstractSymbol(token));
    }
    public void dump(PrintStream out, int n) {
        out.print(Utilities.pad(n) + "string_const\n");
        dump_AbstractSymbol(out, n+2, token);
    }

    
    public void dump_with_types(PrintStream out, int n) {
        dump_line(out, n);
        out.println(Utilities.pad(n) + "_string");
	out.print(Utilities.pad(n + 2) + "\"");
	Utilities.printEscapedString(out, token.getString());
	out.println("\"");
	dump_type(out, n);
    }

    public AbstractSymbol typecheck(ClassTable ct, class_ currentClass, SymbolTable objEnv) {
        return set_type(TreeConstants.Str).get_type();
    }

}


/** Defines AST constructor 'new_'.
    <p>
    See <a href="TreeNode.html">TreeNode</a> for full documentation. */
class new_ extends Expression {
    protected AbstractSymbol type_name;
    /** Creates "new_" AST node. 
      *
      * @param lineNumber the line in the source file from which this node came.
      * @param a0 initial value for type_name
      */
    public new_(int lineNumber, AbstractSymbol a1) {
        super(lineNumber);
        type_name = a1;
    }
    public TreeNode copy() {
        return new new_(lineNumber, copy_AbstractSymbol(type_name));
    }
    public void dump(PrintStream out, int n) {
        out.print(Utilities.pad(n) + "new_\n");
        dump_AbstractSymbol(out, n+2, type_name);
    }

    
    public void dump_with_types(PrintStream out, int n) {
        dump_line(out, n);
        out.println(Utilities.pad(n) + "_new");
	dump_AbstractSymbol(out, n + 2, type_name);
	dump_type(out, n);
    }

    public AbstractSymbol typecheck(ClassTable ct, class_ currentClass, SymbolTable objEnv) {
        if (!type_name.equals(TreeConstants.SELF_TYPE) && ct.getClass_(type_name) == null) {
            ct.semantError(currentClass.getFilename(), this).println(
                "'new' used with undefined class " + type_name + ".");
            return set_type(TreeConstants.Object_).get_type();
        }
        return set_type(type_name).get_type();
    }

}


/** Defines AST constructor 'isvoid'.
    <p>
    See <a href="TreeNode.html">TreeNode</a> for full documentation. */
class isvoid extends Expression {
    protected Expression e1;
    /** Creates "isvoid" AST node. 
      *
      * @param lineNumber the line in the source file from which this node came.
      * @param a0 initial value for e1
      */
    public isvoid(int lineNumber, Expression a1) {
        super(lineNumber);
        e1 = a1;
    }
    public TreeNode copy() {
        return new isvoid(lineNumber, (Expression)e1.copy());
    }
    public void dump(PrintStream out, int n) {
        out.print(Utilities.pad(n) + "isvoid\n");
        e1.dump(out, n+2);
    }

    
    public void dump_with_types(PrintStream out, int n) {
        dump_line(out, n);
        out.println(Utilities.pad(n) + "_isvoid");
	e1.dump_with_types(out, n + 2);
	dump_type(out, n);
    }

    public AbstractSymbol typecheck(ClassTable ct, class_ currentClass, SymbolTable objEnv) {
        e1.typecheck(ct, currentClass, objEnv);
        return set_type(TreeConstants.Bool).get_type();
    }

}


/** Defines AST constructor 'no_expr'.
    <p>
    See <a href="TreeNode.html">TreeNode</a> for full documentation. */
class no_expr extends Expression {
    /** Creates "no_expr" AST node. 
      *
      * @param lineNumber the line in the source file from which this node came.
      */
    public no_expr(int lineNumber) {
        super(lineNumber);
    }
    public TreeNode copy() {
        return new no_expr(lineNumber);
    }
    public void dump(PrintStream out, int n) {
        out.print(Utilities.pad(n) + "no_expr\n");
    }

    
    public void dump_with_types(PrintStream out, int n) {
        dump_line(out, n);
        out.println(Utilities.pad(n) + "_no_expr");
	dump_type(out, n);
    }

    public AbstractSymbol typecheck(ClassTable ct, class_ currentClass, SymbolTable objEnv) {
        return set_type(TreeConstants.No_type).get_type();
    }

}


/** Defines AST constructor 'object'.
    <p>
    See <a href="TreeNode.html">TreeNode</a> for full documentation. */
class object extends Expression {
    protected AbstractSymbol name;
    /** Creates "object" AST node. 
      *
      * @param lineNumber the line in the source file from which this node came.
      * @param a0 initial value for name
      */
    public object(int lineNumber, AbstractSymbol a1) {
        super(lineNumber);
        name = a1;
    }
    public TreeNode copy() {
        return new object(lineNumber, copy_AbstractSymbol(name));
    }
    public void dump(PrintStream out, int n) {
        out.print(Utilities.pad(n) + "object\n");
        dump_AbstractSymbol(out, n+2, name);
    }

    
    public void dump_with_types(PrintStream out, int n) {
        dump_line(out, n);
        out.println(Utilities.pad(n) + "_object");
	dump_AbstractSymbol(out, n + 2, name);
	dump_type(out, n);
    }

    public AbstractSymbol typecheck(ClassTable ct, class_ currentClass, SymbolTable objEnv) {
        if (name.equals(TreeConstants.self)) {
            return set_type(TreeConstants.SELF_TYPE).get_type();
        }
        AbstractSymbol type = (AbstractSymbol) objEnv.lookup(name);
        if (type == null) {
            ct.semantError(currentClass.getFilename(), this).println(
                "Undeclared identifier " + name + ".");
            return set_type(TreeConstants.Object_).get_type();
        }
        return set_type(type).get_type();
    }

}


/** Constructor alias used by ASTParser for program nodes. */
class programc extends program {
    public programc(int lineNumber, Classes a1) { super(lineNumber, a1); }
}

/** Constructor alias used by ASTParser for class nodes. */
class class_c extends class_ {
    public class_c(int lineNumber, AbstractSymbol a1, AbstractSymbol a2,
                   Features a3, AbstractSymbol a4) {
        super(lineNumber, a1, a2, a3, a4);
    }
}

/** Constructor alias used by ASTParser for formal nodes. */
class formalc extends formal {
    public formalc(int lineNumber, AbstractSymbol a1, AbstractSymbol a2) {
        super(lineNumber, a1, a2);
    }
}
