/*
Copyright (c) 2000 The Regents of the University of California.
All rights reserved.

Permission to use, copy, modify, and distribute this software for any
purpose, without fee, and without written agreement is hereby granted,
provided that the above copyright notice and the following two
paragraphs appear in all copies of this software.

IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY FOR
DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES ARISING OUT
OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF THE UNIVERSITY OF
CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY WARRANTIES,
INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY
AND FITNESS FOR A PARTICULAR PURPOSE.  THE SOFTWARE PROVIDED HEREUNDER IS
ON AN "AS IS" BASIS, AND THE UNIVERSITY OF CALIFORNIA HAS NO OBLIGATION TO
PROVIDE MAINTENANCE, SUPPORT, UPDATES, ENHANCEMENTS, OR MODIFICATIONS.
*/

// This is a project skeleton file

import java.io.PrintStream;
import java.util.Vector;
import java.util.Enumeration;

/** This class is used for representing the inheritance tree during code
    generation. You will need to fill in some of its methods and
    potentially extend it in other useful ways. */
class CgenClassTable extends SymbolTable {

    /** All classes in the programc, represented as CgenNode */
    private Vector nds;

    /** This is the stream to which assembly instructions are output */
    private PrintStream str;

    private int stringclasstag;
    private int intclasstag;
    private int boolclasstag;


    // The following methods emit code for constants and global
    // declarations.

    /** Emits code to start the .data segment and to
     * declare the global names.
     * */
    private void codeGlobalData() {
	// The following global names must be defined first.

	str.print("\t.data\n" + CgenSupport.ALIGN);
	str.println(CgenSupport.GLOBAL + CgenSupport.CLASSNAMETAB);
	str.print(CgenSupport.GLOBAL); 
	CgenSupport.emitProtObjRef(TreeConstants.Main, str);
	str.println("");
	str.print(CgenSupport.GLOBAL); 
	CgenSupport.emitProtObjRef(TreeConstants.Int, str);
	str.println("");
	str.print(CgenSupport.GLOBAL); 
	CgenSupport.emitProtObjRef(TreeConstants.Str, str);
	str.println("");
	str.print(CgenSupport.GLOBAL); 
	BoolConst.falsebool.codeRef(str);
	str.println("");
	str.print(CgenSupport.GLOBAL); 
	BoolConst.truebool.codeRef(str);
	str.println("");
	str.println(CgenSupport.GLOBAL + CgenSupport.INTTAG);
	str.println(CgenSupport.GLOBAL + CgenSupport.BOOLTAG);
	str.println(CgenSupport.GLOBAL + CgenSupport.STRINGTAG);

	// We also need to know the tag of the Int, String, and Bool classes
	// during code generation.

	str.println(CgenSupport.INTTAG + CgenSupport.LABEL 
		    + CgenSupport.WORD + intclasstag);
	str.println(CgenSupport.BOOLTAG + CgenSupport.LABEL 
		    + CgenSupport.WORD + boolclasstag);
	str.println(CgenSupport.STRINGTAG + CgenSupport.LABEL 
		    + CgenSupport.WORD + stringclasstag);

    }

    /** Emits code to start the .text segment and to
     * declare the global names.
     * */
    private void codeGlobalText() {
	str.println(CgenSupport.GLOBAL + CgenSupport.HEAP_START);
	str.print(CgenSupport.HEAP_START + CgenSupport.LABEL);
	str.println(CgenSupport.WORD + 0);
	str.println("\t.text");
	str.print(CgenSupport.GLOBAL);
	CgenSupport.emitInitRef(TreeConstants.Main, str);
	str.println("");
	str.print(CgenSupport.GLOBAL);
	CgenSupport.emitInitRef(TreeConstants.Int, str);
	str.println("");
	str.print(CgenSupport.GLOBAL);
	CgenSupport.emitInitRef(TreeConstants.Str, str);
	str.println("");
	str.print(CgenSupport.GLOBAL);
	CgenSupport.emitInitRef(TreeConstants.Bool, str);
	str.println("");
	str.print(CgenSupport.GLOBAL);
	CgenSupport.emitMethodRef(TreeConstants.Main, TreeConstants.main_meth, str);
	str.println("");
    }

    /** Emits code definitions for boolean constants. */
    private void codeBools(int classtag) {
	BoolConst.falsebool.codeDef(classtag, str);
	BoolConst.truebool.codeDef(classtag, str);
    }

    /** Generates GC choice constants (pointers to GC functions) */
    private void codeSelectGc() {
	str.println(CgenSupport.GLOBAL + "_MemMgr_INITIALIZER");
	str.println("_MemMgr_INITIALIZER:");
	str.println(CgenSupport.WORD 
		    + CgenSupport.gcInitNames[Flags.cgen_Memmgr]);

	str.println(CgenSupport.GLOBAL + "_MemMgr_COLLECTOR");
	str.println("_MemMgr_COLLECTOR:");
	str.println(CgenSupport.WORD 
		    + CgenSupport.gcCollectNames[Flags.cgen_Memmgr]);

	str.println(CgenSupport.GLOBAL + "_MemMgr_TEST");
	str.println("_MemMgr_TEST:");
	str.println(CgenSupport.WORD 
		    + ((Flags.cgen_Memmgr_Test == Flags.GC_TEST) ? "1" : "0"));
    }

    /** Emits code to reserve space for and initialize all of the
     * constants.  Class names should have been added to the string
     * table (in the supplied code, is is done during the construction
     * of the inheritance graph), and code for emitting string constants
     * as a side effect adds the string's length to the integer table.
     * The constants are emmitted by running through the stringtable and
     * inttable and producing code for each entry. */
    private void codeConstants() {
	// Add constants that are required by the code generator.
	AbstractTable.stringtable.addString("");
	AbstractTable.inttable.addString("0");

	AbstractTable.stringtable.codeStringTable(stringclasstag, str);
	AbstractTable.inttable.codeStringTable(intclasstag, str);
	codeBools(boolclasstag);
    }


    /** Creates data structures representing basic Cool classes (Object,
     * IO, Int, Bool, String).  Please note: as is this method does not
     * do anything useful; you will need to edit it to make if do what
     * you want.
     * */
    private void installBasicClasses() {
	AbstractSymbol filename 
	    = AbstractTable.stringtable.addString("<basic class>");
	
	// A few special class names are installed in the lookup table
	// but not the class list.  Thus, these classes exist, but are
	// not part of the inheritance hierarchy.  No_class serves as
	// the parent of Object and the other special classes.
	// SELF_TYPE is the self class; it cannot be redefined or
	// inherited.  prim_slot is a class known to the code generator.

	addId(TreeConstants.No_class,
	      new CgenNode(new class_c(0,
				      TreeConstants.No_class,
				      TreeConstants.No_class,
				      new Features(0),
				      filename),
			   CgenNode.Basic, this));

	addId(TreeConstants.SELF_TYPE,
	      new CgenNode(new class_c(0,
				      TreeConstants.SELF_TYPE,
				      TreeConstants.No_class,
				      new Features(0),
				      filename),
			   CgenNode.Basic, this));
	
	addId(TreeConstants.prim_slot,
	      new CgenNode(new class_c(0,
				      TreeConstants.prim_slot,
				      TreeConstants.No_class,
				      new Features(0),
				      filename),
			   CgenNode.Basic, this));

	// The Object class has no parent class. Its methods are
	//        cool_abort() : Object    aborts the programc
	//        type_name() : Str        returns a string representation 
	//                                 of class name
	//        copy() : SELF_TYPE       returns a copy of the object

	class_c Object_class = 
	    new class_c(0, 
		       TreeConstants.Object_, 
		       TreeConstants.No_class,
		       new Features(0)
			   .appendElement(new method(0, 
					      TreeConstants.cool_abort, 
					      new Formals(0), 
					      TreeConstants.Object_, 
					      new no_expr(0)))
			   .appendElement(new method(0,
					      TreeConstants.type_name,
					      new Formals(0),
					      TreeConstants.Str,
					      new no_expr(0)))
			   .appendElement(new method(0,
					      TreeConstants.copy,
					      new Formals(0),
					      TreeConstants.SELF_TYPE,
					      new no_expr(0))),
		       filename);

	installClass(new CgenNode(Object_class, CgenNode.Basic, this));
	
	// The IO class inherits from Object. Its methods are
	//        out_string(Str) : SELF_TYPE  writes a string to the output
	//        out_int(Int) : SELF_TYPE      "    an int    "  "     "
	//        in_string() : Str            reads a string from the input
	//        in_int() : Int                "   an int     "  "     "

	class_c IO_class = 
	    new class_c(0,
		       TreeConstants.IO,
		       TreeConstants.Object_,
		       new Features(0)
			   .appendElement(new method(0,
					      TreeConstants.out_string,
					      new Formals(0)
						  .appendElement(new formalc(0,
								     TreeConstants.arg,
								     TreeConstants.Str)),
					      TreeConstants.SELF_TYPE,
					      new no_expr(0)))
			   .appendElement(new method(0,
					      TreeConstants.out_int,
					      new Formals(0)
						  .appendElement(new formalc(0,
								     TreeConstants.arg,
								     TreeConstants.Int)),
					      TreeConstants.SELF_TYPE,
					      new no_expr(0)))
			   .appendElement(new method(0,
					      TreeConstants.in_string,
					      new Formals(0),
					      TreeConstants.Str,
					      new no_expr(0)))
			   .appendElement(new method(0,
					      TreeConstants.in_int,
					      new Formals(0),
					      TreeConstants.Int,
					      new no_expr(0))),
		       filename);

	installClass(new CgenNode(IO_class, CgenNode.Basic, this));

	// The Int class has no methods and only a single attribute, the
	// "val" for the integer.

	class_c Int_class = 
	    new class_c(0,
		       TreeConstants.Int,
		       TreeConstants.Object_,
		       new Features(0)
			   .appendElement(new attr(0,
					    TreeConstants.val,
					    TreeConstants.prim_slot,
					    new no_expr(0))),
		       filename);

	installClass(new CgenNode(Int_class, CgenNode.Basic, this));

	// Bool also has only the "val" slot.
	class_c Bool_class = 
	    new class_c(0,
		       TreeConstants.Bool,
		       TreeConstants.Object_,
		       new Features(0)
			   .appendElement(new attr(0,
					    TreeConstants.val,
					    TreeConstants.prim_slot,
					    new no_expr(0))),
		       filename);

	installClass(new CgenNode(Bool_class, CgenNode.Basic, this));

	// The class Str has a number of slots and operations:
	//       val                              the length of the string
	//       str_field                        the string itself
	//       length() : Int                   returns length of the string
	//       concat(arg: Str) : Str           performs string concatenation
	//       substr(arg: Int, arg2: Int): Str substring selection

	class_c Str_class =
	    new class_c(0,
		       TreeConstants.Str,
		       TreeConstants.Object_,
		       new Features(0)
			   .appendElement(new attr(0,
					    TreeConstants.val,
					    TreeConstants.Int,
					    new no_expr(0)))
			   .appendElement(new attr(0,
					    TreeConstants.str_field,
					    TreeConstants.prim_slot,
					    new no_expr(0)))
			   .appendElement(new method(0,
					      TreeConstants.length,
					      new Formals(0),
					      TreeConstants.Int,
					      new no_expr(0)))
			   .appendElement(new method(0,
					      TreeConstants.concat,
					      new Formals(0)
						  .appendElement(new formalc(0,
								     TreeConstants.arg, 
								     TreeConstants.Str)),
					      TreeConstants.Str,
					      new no_expr(0)))
			   .appendElement(new method(0,
					      TreeConstants.substr,
					      new Formals(0)
						  .appendElement(new formalc(0,
								     TreeConstants.arg,
								     TreeConstants.Int))
						  .appendElement(new formalc(0,
								     TreeConstants.arg2,
								     TreeConstants.Int)),
					      TreeConstants.Str,
					      new no_expr(0))),
		       filename);

	installClass(new CgenNode(Str_class, CgenNode.Basic, this));
    }
	
    // The following creates an inheritance graph from
    // a list of classes.  The graph is implemented as
    // a tree of `CgenNode', and class names are placed
    // in the base class symbol table.
    
    private void installClass(CgenNode nd) {
	AbstractSymbol name = nd.getName();
	if (probe(name) != null) return;
	nds.addElement(nd);
	addId(name, nd);
    }

    private void installClasses(Classes cs) {
        for (Enumeration e = cs.getElements(); e.hasMoreElements(); ) {
	    installClass(new CgenNode((Class_)e.nextElement(), 
				       CgenNode.NotBasic, this));
        }
    }

    private void buildInheritanceTree() {
	for (Enumeration e = nds.elements(); e.hasMoreElements(); ) {
	    setRelations((CgenNode)e.nextElement());
	}
    }

    private void setRelations(CgenNode nd) {
	CgenNode parent = (CgenNode)probe(nd.getParent());
	nd.setParentNd(parent);
	parent.addChild(nd);
    }

    // ----------------------------------------------------------------------
    //  Layout information shared by the whole code generator
    // ----------------------------------------------------------------------

    /** All classes in tag order (index == class tag). */
    private Vector classesByTag = new Vector();

    /** The single live class table, so the code() routines of the AST nodes
     *  (which only receive a PrintStream) can reach the layout information,
     *  the variable environment and the helper emitters. */
    static CgenClassTable instance;

    /** The class whose method/initializer is currently being generated. */
    private CgenNode currentClass;

    /** Maps a variable name to where it lives at run time (an attribute slot
     *  inside self, a formalc parameter or a let/case temporary). */
    private SymbolTable env = new SymbolTable();

    /** Word offset (relative to $fp) where the next pushed temporary lands.
     *  After the prologue $sp == $fp - 4, so the first temporary is at -1. */
    private int nextTemp;

    /** Source of unique numeric labels for generated branch targets. */
    private int labelCount = 0;

    /** Describes the run-time location of a variable. */
    static class VarLoc {
	final String reg;     // SELF for attributes, FP for parameters/temps
	final int offset;     // word offset from that register
	VarLoc(String reg, int offset) { this.reg = reg; this.offset = offset; }
    }

    int newLabel() { return labelCount++; }
    CgenNode currentClass() { return currentClass; }
    VarLoc lookupVar(AbstractSymbol name) { return (VarLoc)env.lookup(name); }

    /** Looks up a class node by name. */
    CgenNode getClass(AbstractSymbol name) { return (CgenNode)probe(name); }

    /** A pushed temporary occupies the current nextTemp slot; returns its
     *  $fp word offset and advances the allocator. */
    int allocTemp() { return nextTemp--; }
    void freeTemp() { nextTemp++; }

    // ----------------------------------------------------------------------
    //  Building the object/dispatch layout
    // ----------------------------------------------------------------------

    /** Assigns class tags by a pre-order walk so each subtree owns a
     *  contiguous tag range, registers class-name and filename strings, and
     *  builds the attribute and dispatch layout of every class. */
    private void setupLayout() {
	assignTags(root());

	for (Enumeration e = classesByTag.elements(); e.hasMoreElements(); ) {
	    CgenNode nd = (CgenNode)e.nextElement();
	    // Class name and source file name become string constants.
	    AbstractTable.stringtable.addString(nd.getName().getString());
	    AbstractTable.stringtable.addString(nd.getFilename().getString());
	    buildAttrTable(nd);
	    buildDispatchTable(nd);
	}

	stringclasstag = getClass(TreeConstants.Str).classTag;
	intclasstag    = getClass(TreeConstants.Int).classTag;
	boolclasstag   = getClass(TreeConstants.Bool).classTag;
    }

    /** Recursively assigns a pre-order tag to nd and records the maximum tag
     *  in its subtree (used for the case range test). */
    private int assignTags(CgenNode nd) {
	nd.classTag = classesByTag.size();
	classesByTag.addElement(nd);
	int max = nd.classTag;
	for (Enumeration e = nd.getChildren(); e.hasMoreElements(); ) {
	    max = Math.max(max, assignTags((CgenNode)e.nextElement()));
	}
	nd.maxChildTag = max;
	return max;
    }

    /** Fills nd.attrTable with inherited attributes (in inheritance order)
     *  followed by the attributes declared in nd itself. */
    private void buildAttrTable(CgenNode nd) {
	if (!nd.getName().equals(TreeConstants.Object_)) {
	    nd.attrTable.addAll(nd.getParentNd().attrTable);
	}
	for (Enumeration e = nd.getFeatures().getElements(); e.hasMoreElements(); ) {
	    Object f = e.nextElement();
	    if (f instanceof attr) nd.attrTable.addElement(f);
	}
    }

    /** Fills nd's dispatch layout: parent slots first (with overrides
     *  redirected to nd), then the methods newly introduced by nd. */
    private void buildDispatchTable(CgenNode nd) {
	if (!nd.getName().equals(TreeConstants.Object_)) {
	    CgenNode p = nd.getParentNd();
	    nd.methodNames.addAll(p.methodNames);
	    nd.methodDefiners.addAll(p.methodDefiners);
	}
	for (Enumeration e = nd.getFeatures().getElements(); e.hasMoreElements(); ) {
	    Object f = e.nextElement();
	    if (!(f instanceof method)) continue;
	    AbstractSymbol mname = ((method)f).name;
	    int slot = nd.methodOffset(mname);
	    if (slot >= 0) {
		nd.methodDefiners.setElementAt(nd.getName(), slot);   // override
	    } else {
		nd.methodNames.addElement(mname);                     // new method
		nd.methodDefiners.addElement(nd.getName());
	    }
	}
    }

    // ----------------------------------------------------------------------
    //  Emission of the global tables and prototype objects
    // ----------------------------------------------------------------------

    /** class_nameTab: for each class (in tag order) a pointer to the String
     *  object holding its name. */
    private void codeClassNameTab() {
	str.print(CgenSupport.CLASSNAMETAB + CgenSupport.LABEL);
	for (Enumeration e = classesByTag.elements(); e.hasMoreElements(); ) {
	    CgenNode nd = (CgenNode)e.nextElement();
	    StringSymbol s =
		(StringSymbol)AbstractTable.stringtable.lookup(nd.getName().getString());
	    str.print(CgenSupport.WORD); s.codeRef(str); str.println("");
	}
    }

    /** class_objTab: for each class (in tag order) the pair
     *  (prototype object, initializer), used by "new SELF_TYPE". */
    private void codeClassObjTab() {
	str.print(CgenSupport.CLASSOBJTAB + CgenSupport.LABEL);
	for (Enumeration e = classesByTag.elements(); e.hasMoreElements(); ) {
	    CgenNode nd = (CgenNode)e.nextElement();
	    str.print(CgenSupport.WORD);
	    CgenSupport.emitProtObjRef(nd.getName(), str); str.println("");
	    str.print(CgenSupport.WORD);
	    CgenSupport.emitInitRef(nd.getName(), str); str.println("");
	}
    }

    /** Emits one dispatch table per class. */
    private void codeDispatchTables() {
	for (Enumeration e = classesByTag.elements(); e.hasMoreElements(); ) {
	    CgenNode nd = (CgenNode)e.nextElement();
	    CgenSupport.emitDispTableRef(nd.getName(), str);
	    str.print(CgenSupport.LABEL);
	    for (int i = 0; i < nd.methodNames.size(); i++) {
		str.print(CgenSupport.WORD);
		CgenSupport.emitMethodRef((AbstractSymbol)nd.methodDefiners.elementAt(i),
					  (AbstractSymbol)nd.methodNames.elementAt(i), str);
		str.println("");
	    }
	}
    }

    /** Emits a prototype object for every class.  All attributes get the
     *  default value of their declared type; the real initializers run later
     *  in the class' _init method. */
    private void codeProtObjs() {
	for (Enumeration e = classesByTag.elements(); e.hasMoreElements(); ) {
	    CgenNode nd = (CgenNode)e.nextElement();
	    str.println(CgenSupport.WORD + "-1");                 // GC eye catcher
	    CgenSupport.emitProtObjRef(nd.getName(), str);
	    str.print(CgenSupport.LABEL);
	    str.println(CgenSupport.WORD + nd.classTag);          // class tag
	    str.println(CgenSupport.WORD +
			(CgenSupport.DEFAULT_OBJFIELDS + nd.attrTable.size())); // size
	    str.print(CgenSupport.WORD);
	    CgenSupport.emitDispTableRef(nd.getName(), str); str.println("");
	    for (Enumeration a = nd.attrTable.elements(); a.hasMoreElements(); ) {
		attr at = (attr)a.nextElement();
		emitDefaultValueWord(at.type_decl);
	    }
	}
    }

    /** Emits a single ".word" giving the default value for an attribute of
     *  the given declared type: the boxed default for Int/Bool/String, the
     *  raw 0 for the primitive slots of the basic boxes, and void (0)
     *  otherwise. */
    private void emitDefaultValueWord(AbstractSymbol type) {
	if (type.equals(TreeConstants.Int)) {
	    str.print(CgenSupport.WORD);
	    ((IntSymbol)AbstractTable.inttable.addString("0")).codeRef(str);
	    str.println("");
	} else if (type.equals(TreeConstants.Bool)) {
	    str.print(CgenSupport.WORD);
	    BoolConst.falsebool.codeRef(str);
	    str.println("");
	} else if (type.equals(TreeConstants.Str)) {
	    str.print(CgenSupport.WORD);
	    ((StringSymbol)AbstractTable.stringtable.addString("")).codeRef(str);
	    str.println("");
	} else {
	    str.println(CgenSupport.WORD + 0);   // void or a raw prim_slot
	}
    }

    // ----------------------------------------------------------------------
    //  Method prologue / epilogue and the abort helpers
    // ----------------------------------------------------------------------

    /** Standard activation-record prologue.  Saves the old frame pointer,
     *  self and the return address, then installs the new frame and self. */
    void emitMethodEntry() {
	CgenSupport.emitAddiu(CgenSupport.SP, CgenSupport.SP, -12, str);
	CgenSupport.emitStore(CgenSupport.FP, 3, CgenSupport.SP, str);
	CgenSupport.emitStore(CgenSupport.SELF, 2, CgenSupport.SP, str);
	CgenSupport.emitStore(CgenSupport.RA, 1, CgenSupport.SP, str);
	CgenSupport.emitAddiu(CgenSupport.FP, CgenSupport.SP, 4, str);
	CgenSupport.emitMove(CgenSupport.SELF, CgenSupport.ACC, str);
    }

    /** Restores the caller's frame, pops the frame and the nargs arguments
     *  pushed by the caller, and returns. */
    void emitMethodExit(int nargs) {
	CgenSupport.emitLoad(CgenSupport.FP, 3, CgenSupport.SP, str);
	CgenSupport.emitLoad(CgenSupport.SELF, 2, CgenSupport.SP, str);
	CgenSupport.emitLoad(CgenSupport.RA, 1, CgenSupport.SP, str);
	CgenSupport.emitAddiu(CgenSupport.SP, CgenSupport.SP, 12 + 4 * nargs, str);
	CgenSupport.emitReturn(str);
    }

    /** Loads a String constant for the current source file into $a0. */
    void emitLoadFilename() {
	StringSymbol fn = (StringSymbol)
	    AbstractTable.stringtable.lookup(currentClass.getFilename().getString());
	CgenSupport.emitLoadString(CgenSupport.ACC, fn, str);
    }

    // ----------------------------------------------------------------------
    //  Object initializers and user method bodies
    // ----------------------------------------------------------------------

    /** Emits the _init method of every class. */
    private void codeInitializers() {
	for (Enumeration e = classesByTag.elements(); e.hasMoreElements(); ) {
	    codeInit((CgenNode)e.nextElement());
	}
    }

    private void codeInit(CgenNode nd) {
	currentClass = nd;
	CgenSupport.emitInitRef(nd.getName(), str);
	str.print(CgenSupport.LABEL);
	emitMethodEntry();

	// Bind every attribute slot so initializers can refer to attributes.
	env.enterScope();
	for (int i = 0; i < nd.attrTable.size(); i++) {
	    attr at = (attr)nd.attrTable.elementAt(i);
	    env.addId(at.name, new VarLoc(CgenSupport.SELF,
					  CgenSupport.DEFAULT_OBJFIELDS + i));
	}
	nextTemp = -1;

	// Initialize the parent's part of the object first.
	if (!nd.getName().equals(TreeConstants.Object_)) {
	    str.print(CgenSupport.JAL);
	    CgenSupport.emitInitRef(nd.getParentNd().getName(), str);
	    str.println("");
	}

	// Run the initializers declared in this class.
	int base = CgenSupport.DEFAULT_OBJFIELDS;
	for (int i = 0; i < nd.attrTable.size(); i++) {
	    attr at = (attr)nd.attrTable.elementAt(i);
	    boolean declaredHere = isDeclaredHere(nd, at.name);
	    if (declaredHere && !(at.init instanceof no_expr)) {
		at.init.code(str);
		CgenSupport.emitStore(CgenSupport.ACC, base + i, CgenSupport.SELF, str);
		if (Flags.cgen_Memmgr != Flags.GC_NOGC) {
		    CgenSupport.emitAddiu(CgenSupport.A1, CgenSupport.SELF,
					  (base + i) * CgenSupport.WORD_SIZE, str);
		    CgenSupport.emitGCAssign(str);
		}
	    }
	}

	CgenSupport.emitMove(CgenSupport.ACC, CgenSupport.SELF, str);  // return self
	env.exitScope();
	emitMethodExit(0);
    }

    /** True if the attribute is declared directly in nd (not inherited). */
    private boolean isDeclaredHere(CgenNode nd, AbstractSymbol attrName) {
	for (Enumeration e = nd.getFeatures().getElements(); e.hasMoreElements(); ) {
	    Object f = e.nextElement();
	    if (f instanceof attr && ((attr)f).name.equals(attrName)) return true;
	}
	return false;
    }

    /** Emits the method bodies of every non-basic class. */
    private void codeClassMethods() {
	for (Enumeration e = classesByTag.elements(); e.hasMoreElements(); ) {
	    CgenNode nd = (CgenNode)e.nextElement();
	    if (nd.basic()) continue;          // basic methods live in the runtime
	    currentClass = nd;
	    for (Enumeration f = nd.getFeatures().getElements(); f.hasMoreElements(); ) {
		Object feat = f.nextElement();
		if (feat instanceof method) codeMethod(nd, (method)feat);
	    }
	}
    }

    private void codeMethod(CgenNode nd, method m) {
	CgenSupport.emitMethodRef(nd.getName(), m.name, str);
	str.print(CgenSupport.LABEL);
	emitMethodEntry();

	env.enterScope();
	// Attributes are reachable through self.
	for (int i = 0; i < nd.attrTable.size(); i++) {
	    attr at = (attr)nd.attrTable.elementAt(i);
	    env.addId(at.name, new VarLoc(CgenSupport.SELF,
					  CgenSupport.DEFAULT_OBJFIELDS + i));
	}
	// Formals sit above the frame; the first formalc is the deepest push,
	// so formalc k (0-based) is at $fp word offset nargs - k + 2.
	int nargs = m.formals.getLength();
	for (int k = 0; k < nargs; k++) {
	    formalc fm = (formalc)m.formals.getNth(k);
	    env.addId(fm.name, new VarLoc(CgenSupport.FP, nargs - k + 2));
	}
	nextTemp = -1;

	m.expr.code(str);

	env.exitScope();
	emitMethodExit(nargs);
    }

    /** Binds a name to a fresh temporary stack slot and records the binding;
     *  the value to store must already be in $a0. */
    void pushVar(AbstractSymbol name) {
	int off = allocTemp();
	CgenSupport.emitStore(CgenSupport.ACC, 0, CgenSupport.SP, str);
	CgenSupport.emitAddiu(CgenSupport.SP, CgenSupport.SP, -4, str);
	env.enterScope();
	env.addId(name, new VarLoc(CgenSupport.FP, off));
    }

    /** Undoes a previous pushVar. */
    void popVar() {
	env.exitScope();
	CgenSupport.emitAddiu(CgenSupport.SP, CgenSupport.SP, 4, str);
	freeTemp();
    }

    /** Pushes the value currently in $a0 onto the stack as an anonymous
     *  temporary and returns its $fp word offset. */
    int pushTemp() {
	int off = allocTemp();
	CgenSupport.emitStore(CgenSupport.ACC, 0, CgenSupport.SP, str);
	CgenSupport.emitAddiu(CgenSupport.SP, CgenSupport.SP, -4, str);
	return off;
    }

    /** Frees the temporary allocated by the most recent pushTemp. */
    void popTemp() {
	CgenSupport.emitAddiu(CgenSupport.SP, CgenSupport.SP, 4, str);
	freeTemp();
    }

    /** Opens a scope binding name to an already-allocated frame slot. */
    void bindAt(AbstractSymbol name, int offset) {
	env.enterScope();
	env.addId(name, new VarLoc(CgenSupport.FP, offset));
    }

    /** Closes the scope opened by bindAt. */
    void unbind() { env.exitScope(); }

    /** Resolves a (possibly SELF_TYPE) static type to its class node. */
    CgenNode staticTypeClass(AbstractSymbol type) {
	if (type.equals(TreeConstants.SELF_TYPE)) return currentClass;
	return getClass(type);
    }

    /** Constructs a new class table and invokes the code generator */
    public CgenClassTable(Classes cls, PrintStream str) {
	nds = new Vector();

	this.str = str;

	stringclasstag = 0;
	intclasstag =    0;
	boolclasstag =   0;

	enterScope();
	if (Flags.cgen_debug) System.out.println("Building CgenClassTable");

	installBasicClasses();
	installClasses(cls);
	buildInheritanceTree();

	setupLayout();

	code();

	exitScope();
    }

    /** This method is the meat of the code generator. */
    public void code() {
	CgenClassTable.instance = this;

	if (Flags.cgen_debug) System.out.println("coding global data");
	codeGlobalData();

	if (Flags.cgen_debug) System.out.println("choosing gc");
	codeSelectGc();

	if (Flags.cgen_debug) System.out.println("coding constants");
	codeConstants();

	// Global constants and tables (still in the .data segment).
	codeClassNameTab();
	codeClassObjTab();
	codeDispatchTables();
	codeProtObjs();

	if (Flags.cgen_debug) System.out.println("coding global text");
	codeGlobalText();

	// Object initializers and the user-defined method bodies (.text).
	if (Flags.cgen_debug) System.out.println("coding initializers");
	codeInitializers();

	if (Flags.cgen_debug) System.out.println("coding methods");
	codeClassMethods();
    }

    /** Gets the root of the inheritance tree */
    public CgenNode root() {
	return (CgenNode)probe(TreeConstants.Object_);
    }
}
			  
    
