import minipython.analysis.*;
import minipython.node.*;
import java.util.*;

public class Visitor extends DepthFirstAdapter {

	private Hashtable <String,ArrayList<Node>> symtable;	
	static int errors;
	boolean in_function; boolean arguments_ok; boolean in_for;
	String in_function_name; String in_for_name;
	ArrayList<String> arguments;

	Visitor(Hashtable symtable) {
		this.symtable = symtable;
		errors = 0;
		in_function = false;
		in_for = false;
	}
	
	@Override
	public void caseAFunction(AFunction node) {
		inAFunction(node);
        if(node.getId() != null) {
            node.getId().apply(this);
        }
        {
			Object temp[] = node.getArgument().toArray();
			String function_name = node.getId().toString();
			int line = ((TId) node.getId()).getLine();
			//calculate total # of arguments of function "node"
			int node_params = 0; int default_node_params = 0;
			if (((AFunction) node).getArgument().size() == 1) {
				AArgument args_node = (AArgument) ((AFunction) node).getArgument().get(0);
				//add the first argument
				node_params = 1;
				default_node_params = args_node.getAssignV().size();
				//add the rest of the arguments
				node_params += args_node.getCommaAssign().size();
				for (Object v  : args_node.getCommaAssign()) {
					default_node_params += ((ACommaAssign) v).getAssignV().size();
				}
			}
			//check if there is a function with the same name
			boolean flag = true;
			if(symtable.containsKey(function_name)) {
				ArrayList<Node> temp_nodes = symtable.get(function_name);
				Node n;
				//check if the # of arguments is different
				for(int k = 0; k < temp_nodes.size(); k++) {
					n = temp_nodes.get(k);
					if(n instanceof AFunction) {
						//calculate total # of arguments of function "n"
						int n_params = 0; int default_n_params = 0;
						if (((AFunction) n).getArgument().size() == 1) {
							AArgument args_n = (AArgument) ((AFunction) n).getArgument().get(0);
							//add the first argument
							n_params = 1;
							default_n_params = args_n.getAssignV().size();
							//add the rest of the arguments
							n_params += args_n.getCommaAssign().size();
							for (Object v  : args_n.getCommaAssign()) {
								default_n_params += ((ACommaAssign) v).getAssignV().size();
							}
						}
						if (!((n_params- default_n_params > node_params - default_node_params && default_node_params==0) || (node_params - default_node_params > n_params))) {
							System.out.println("Line " + line + ": " +" Function " + function_name +" is already defined");
							errors++;
							flag = false;
							break;
						} 
					}
				}
			}
			if (flag) {
				//add function in table
				if(!symtable.containsKey(function_name)) symtable.put(function_name,new ArrayList<Node>(Arrays.asList(node)));
				else symtable.get(function_name).add(node);
			}
			//proceed to check arguments
			for(int i = 0; i < temp.length; i++) {
				((PArgument) temp[i]).apply(this);
			}
			//proceed to check statement
			in_function = true;
			in_function_name = function_name;
			if(node.getStatement() != null) {
				node.getStatement().apply(this);
			}
			in_function = false;
		}
        outAFunction(node);
    }


	public void caseAArgument(AArgument node) {
        inAArgument(node);
        if(node.getId() != null) {
            node.getId().apply(this);
        }
        {
            Object temp[] = node.getAssignV().toArray();
            for(int i = 0; i < temp.length; i++) {
                ((PAssignV) temp[i]).apply(this);
            }
		}
		{
			//check for duplicate argument names in the same function
			String arg_name = node.getId().toString();
			arguments = new ArrayList<String>(Arrays.asList(arg_name));
			Object temp[] = node.getCommaAssign().toArray();
			boolean flag = true;
			int line = ((TId) node.getId()).getLine();
			String function_name = ((AFunction)(node.parent())).getId().toString();
			for(int i = 0; i < temp.length; i++) {
				String temp_id = ((ACommaAssign)temp[i]).getId().toString();
				int size = arguments.size();
				for(int k = 0; k < size; k++) {
					if(arguments.get(k).equals(temp_id)) {
						System.out.println("Line " + line + ": " + "Duplicate argument " + temp_id + " in function "+function_name);
						flag = false;
						errors++;
						break;
					} else arguments.add(temp_id);
				}
			}
		
			//if no duplicates, add arguments in symbol table, else print error
			//if(flag) {
				//add first argument in symbol table
				if(!symtable.containsKey(arg_name)) symtable.put(arg_name,new ArrayList<Node>(Arrays.asList(node)));
				else symtable.get(arg_name).add(node);
				//add the rest arguments in symbol table
				for(int i = 0; i < temp.length; i++) {
					((PCommaAssign) temp[i]).apply(this);
				}
			//}
		}
        outAArgument(node);
    }

	//we have checked for duplicates in the same function in caseAArgument. just add the arguments in the table
	@Override
	public void inACommaAssign(ACommaAssign node) {
		String arg_name = node.getId().toString(); //get name of the argument
		if(!symtable.containsKey(arg_name)) symtable.put(arg_name,new ArrayList<Node>(Arrays.asList(node)));
		else symtable.get(arg_name).add(node);
	}
	
	// MAYBE for identifier in identifier: statement 
	// @Override
	// public void inAForStatement(AForStatement node) {
    //     defaultIn(node);
	// }
	
	@Override
	public void inAAssignStatement(AAssignStatement node) {
		String var_name = node.getId().toString();
		if(!symtable.containsKey(var_name)) symtable.put(var_name,new ArrayList<Node>(Arrays.asList(node)));
		else symtable.get(var_name).add(node);
	}
	

	//check if used identifiers are defined
	@Override
	public void inAIdExpression(AIdExpression node) {
		int line = ((TId) node.getId()).getLine();
		String id = node.getId().toString();
		if(!nameExists(id)) {
			System.out.println("Line " + line + ": " +"Name " + id +" is not defined");
			errors++;
		}		
	}
	
	@Override
	public void inATypeExpression(ATypeExpression node) {
        int line = ((TId) node.getId()).getLine();
		String id = node.getId().toString();
		if(!nameExists(id)) {
			System.out.println("Line " + line + ": " +"Name " + id +" is not defined");
			errors++;
		}
	}
	
	@Override
	public void inAAssignminStatement(AAssignminStatement node) {
        int line = ((TId) node.getId()).getLine();
		String id = node.getId().toString();
		if(!nameExists(id)) {
			System.out.println("Line " + line + ": " +"Name " + id +" is not defined");
			errors++;
		}
	}
	
	@Override
	public void inAAssigndivStatement(AAssigndivStatement node) {
        int line = ((TId) node.getId()).getLine();
		String id = node.getId().toString();
		if(!nameExists(id)) {
			System.out.println("Line " + line + ": " +"Name " + id +" is not defined");
			errors++;
		}
	}
	
	@Override
	public void inAListStatement(AListStatement node) {
        int line = ((TId) node.getId()).getLine();
		String id = node.getId().toString();
		if(!nameExists(id)) {
			System.out.println("Line " + line + ": " +"Name " + id +" is not defined");
			errors++;
		}
	}

	@Override
	public void caseAForStatement(AForStatement node) {
        inAForStatement(node);
        if(node.getForid() != null) {
            node.getForid().apply(this);
		}
		String forId = node.getForid().toString();
		if(!symtable.containsKey(forId)) symtable.put(forId,new ArrayList<Node>(Arrays.asList(node)));
		else symtable.get(forId).add(node);
        if(node.getInid() != null) {
            node.getInid().apply(this);
		}
		if (!symtable.containsKey(node.getInid().toString())) {
			int line = ((TId) node.getInid()).getLine();
			System.out.println("Line " + line + ": " + "Name " + node.getInid().toString() +" is not defined");
			errors++;
		}
		in_for = true;
		in_for_name = forId;
        if(node.getStatement() != null) {
			node.getStatement().apply(this);
		}
		in_for = false;
		
        outAForStatement(node);
    }

	//check if a name exists in the hashtable
	private boolean nameExists(String id) {
		if(symtable.containsKey(id)) {
			ArrayList<Node> values = symtable.get(id);
			for(int i = 0; i < values.size(); i++) {
				if(values.get(i) instanceof AAssignStatement) return true;
				
			}
		}
		if(in_function) {
			if(arguments.contains(id)) return true;
			/*
			if(values.get(i) instanceof AArgument) {
				if(((AFunction)((AArgument)values.get(i)).parent()).getId().toString().equals(in_function_name)) return true;
			}else if(values.get(i) instanceof ACommaAssign) {
				if(((AFunction)((ACommaAssign)values.get(i)).parent().parent()).getId().toString().equals(in_function_name)) return true;
			}
			*/
		}
		if(in_for) {
			if(id.equals(in_for_name)) return true;
			else return false;
		}
		return false;
	}

}