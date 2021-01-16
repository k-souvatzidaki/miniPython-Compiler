import minipython.analysis.*;
import minipython.node.*;
import java.util.*;

public class Visitor2 extends DepthFirstAdapter {

	private Hashtable <String,ArrayList<Node>> symtable;	

	Visitor2(Hashtable symtable) {
		this.symtable = symtable;
    }
    
    
}
