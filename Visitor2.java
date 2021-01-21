import minipython.analysis.*;
import minipython.node.*;
import java.util.*;

public class Visitor2 extends DepthFirstAdapter {

	private Hashtable <String,ArrayList<Node>> symtable;	
	int errors;

	Visitor2(Hashtable symtable) {
		this.symtable = symtable;
		errors = 0;
    }
	
	@Override
	public void caseAFunctionCall(AFunctionCall node) {
        inAFunctionCall(node);
        if(node.getId() != null) {
            node.getId().apply(this);
        }
        {
			//get id
			String id = node.getId().toString();
			//get # of arguments
			int params = node.getArglist().size();
			if(params == 1) {
				params+= ((AArglist)node.getArglist().get(0)).getCommaExp().size();
			}

			int line = ((TId) node.getId()).getLine();

			//check if function is defined
			if(!symtable.containsKey(id)) {
				System.out.println("Line " + line + ": " +"Function " + id +" is not defined");
				errors++;
			} else {
				ArrayList<Node> nodes = symtable.get(id);
				boolean flag = false; boolean enough_args = false;
				for(int i =0; i< nodes.size(); i++ ) {
					if( nodes.get(i) instanceof AFunction) {
						flag = true;
						//count args (non-default and total)
						AFunction n = (AFunction)nodes.get(i);
						int n_params = 0; int default_n_params = 0;
						if (n.getArgument().size() == 1) {
							AArgument args_n = (AArgument)n.getArgument().get(0);
							//add the first argument
							n_params = 1;
							default_n_params = args_n.getAssignV().size();
							//add the rest of the arguments
							n_params += args_n.getCommaAssign().size();
							for (Object v  : args_n.getCommaAssign()) {
								default_n_params += ((ACommaAssign) v).getAssignV().size();
							}
						}
						if(params >= default_n_params && params <= n_params) enough_args = true;

					} 
				}
				
				if(!flag) { 
					System.out.println("Line " + line + ": " +"Function " + id +" is not defined");
					errors++;
				}
				if(!enough_args)
					System.out.println("Line " + line + ": " +"Function " + id +" with "+params+" arguments is not defined");
					errors++;
				}	
			}
            Object temp[] = node.getArglist().toArray();
            for(int i = 0; i < temp.length; i++) {
                ((PArglist) temp[i]).apply(this);
            }
        
        outAFunctionCall(node);
    }
    
}
