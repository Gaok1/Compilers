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

class CgenNode extends class_c {
    /** The parent of this node in the inheritance tree */
    private CgenNode parent;

    /** The children of this node in the inheritance tree */
    private Vector children;

    /** Indicates a basic class */
    final static int Basic = 0;

    /** Indicates a class that came from a Cool programc */
    final static int NotBasic = 1;
    
    /** Does this node correspond to a basic class? */
    private int basic_status;

    // ---- Code-generation layout information (filled in by CgenClassTable) ----

    /** Numeric class tag assigned by a pre-order walk of the inheritance
     *  tree.  The whole subtree rooted at this node owns the contiguous
     *  range of tags [classTag, maxChildTag], which makes the runtime test
     *  for a "case" branch a simple range comparison. */
    int classTag;

    /** Largest tag among this node and all of its descendants. */
    int maxChildTag;

    /** Attribute layout: every attribute visible in this class, inherited
     *  ones first (in inheritance order) followed by the ones declared here.
     *  The i-th attribute lives at object word offset DEFAULT_OBJFIELDS + i. */
    Vector attrTable = new Vector();

    /** Method dispatch layout.  methodNames.get(i) is the name of the method
     *  occupying slot i of this class' dispatch table, and methodDefiners.get(i)
     *  is the name of the class whose implementation is used (i.e. the label
     *  <definer>.<name>).  Slots are inherited from the parent and overrides
     *  keep their original slot, so a method's offset is stable across every
     *  subclass. */
    Vector methodNames = new Vector();
    Vector methodDefiners = new Vector();

    /** Returns the dispatch-table offset (in words) of the named method, or
     *  -1 if this class does not understand it. */
    int methodOffset(AbstractSymbol methodName) {
	for (int i = 0; i < methodNames.size(); i++) {
	    if (methodNames.elementAt(i).equals(methodName)) return i;
	}
	return -1;
    }

    /** Constructs a new CgenNode to represent class "c".
     * @param c the class
     * @param basic_status is this class basic or not
     * @param table the class table
     * */
    CgenNode(Class_ c, int basic_status, CgenClassTable table) {
	super(0, c.getName(), c.getParent(), c.getFeatures(), c.getFilename());
	this.parent = null;
	this.children = new Vector();
	this.basic_status = basic_status;
	AbstractTable.stringtable.addString(name.getString());
    }

    void addChild(CgenNode child) {
	children.addElement(child);
    }

    /** Gets the children of this class
     * @return the children
     * */
    Enumeration getChildren() {
	return children.elements(); 
    }

    /** Sets the parent of this class.
     * @param parent the parent
     * */
    void setParentNd(CgenNode parent) {
	if (this.parent != null) {
	    Utilities.fatalError("parent already set in CgenNode.setParent()");
	}
	if (parent == null) {
	    Utilities.fatalError("null parent in CgenNode.setParent()");
	}
	this.parent = parent;
    }    
	

    /** Gets the parent of this class
     * @return the parent
     * */
    CgenNode getParentNd() {
	return parent; 
    }

    /** Returns true is this is a basic class.
     * @return true or false
     * */
    boolean basic() { 
	return basic_status == Basic; 
    }
}
    

    
