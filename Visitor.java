import minipython.analysis.*;
import minipython.node.*;
import java.util.*;

public class Visitor extends DepthFirstAdapter {

	private Hashtable <String,ArrayList<Node>> symtable;	

	Visitor(Hashtable symtable) {
		this.symtable = symtable;
	}
	
	@Override
	public void caseAFunction(AFunction node) {

		System.out.println("\nInside caseAFunction");

        inAFunction(node);
        if(node.getId() != null) {
            node.getId().apply(this);
        }
        {
			Object temp[] = node.getArgument().toArray();

			String function_name = node.getId().toString();
			int line = ((TId) node.getId()).getLine();
			ArrayList<Node> temp1 = symtable.get(function_name);
			boolean flag = true;
			if (temp1 != null) {
				for(Node n : temp1) {
					if(n instanceof AFunction) {
						AArgument args_n = (AArgument) ((AFunction) n).getArgument().get(0);
						int size_n = 0; // # of all parameters (including default)
						int size_default = 0; //# of default parameters
						if (((AFunction) n).getArgument().size() == 1) {
							size_n = 1;
							size_default = args_n.getAssignV().size();
							for (Object v  : args_n.getCommaAssign()) {
								size_default += ((ACommaAssign) v).getAssignV().size();
							}
							size_n += args_n.getCommaAssign().size();
						}
						AArgument args_node = (AArgument) ((AFunction) node).getArgument().get(0);
						int size_node=0; // # of all parameters (including default)
						int default_node = 0; //# of default parameters
						if (((AFunction) node).getArgument().size() == 1) {
							size_node = 1;
							default_node = args_node.getAssignV().size();
							// minus the default
							for (Object v  : args_node.getCommaAssign()) {
								default_node += ((ACommaAssign) v).getAssignV().size();
							}
							size_node += args_node.getCommaAssign().size();
						}
						
						if (((size_n - size_default > size_node - default_node && default_node==0) || (size_node - default_node > size_n))) {
							
						} 
						else {
							System.out.println("Line " + line + ": " +" Function " + function_name +" is already defined");
							flag = false;
						}
					}
				}
			}
			if (flag){
				symtable.put(function_name,new ArrayList<Node>(Arrays.asList(node)));
				for(int i = 0; i < temp.length; i++) {
					((PArgument) temp[i]).apply(this);
				}
				if(node.getStatement() != null) {
					node.getStatement().apply(this);
				}
			}
		}
		
        outAFunction(node);
    }


	@Override
	public void inAArgument(AArgument node) {

		System.out.println("Inside inAArgument");

		String arg_name = node.getId().toString();
		//the first argument is never a duplicate
		
		if(!symtable.containsKey(arg_name)) symtable.put(arg_name,new ArrayList<Node>(Arrays.asList(node)));
		else symtable.get(arg_name).add(node);
		//the rest of the arguments are "CommaAssign"s, checked in "inACommaAssign(..)"
	}
	
	// TODO : check duplicated arguments in different functions with the same name (same name, different # of arguments (without default))
	@Override
	public void inACommaAssign(ACommaAssign node) {

		System.out.println("Inside inACommaAssign");

		String arg_name = node.getId().toString(); //get name of the argument
		String function_name = ((AFunction)(node.parent().parent())).getId().toString(); //get name of the function
		//AFunction function_name_o = (AFunction) node.parent().parent();
		int line = ((TId) node.getId()).getLine();
		//if no symbol with the same name exists
		if(!symtable.containsKey(arg_name)) symtable.put(arg_name,new ArrayList<Node>(Arrays.asList(node)));
		else {

			//get all the symbols with the same name 
			ArrayList<Node> temp = symtable.get(arg_name);
			//check if a symbol with type AArgument or ACommaAssign with the same name exists
			boolean flag = true;
			for(int i =0; i < temp.size(); i++) {
				Node n = temp.get(i);
				if(n instanceof AArgument) {
					String n_function = ((AFunction)(n.parent())).getId().toString();
					//AFunction n_function = (AFunction) n.parent();
					System.out.println("Parent of AArgument = "+ n_function);
					if(n_function.equals(function_name)) {
					//if(isEqualFunc(n_function, function_name_o)){
						System.out.println("Line " + line + ": " +"Duplicate argument " + arg_name +" in function "+function_name);
						flag = false;
					}
				} else if(n instanceof ACommaAssign) {
					String n_function = ((AFunction)(n.parent().parent())).getId().toString();
					//AFunction n_function = (AFunction) n.parent().parent();
					System.out.println("Parent of ACommaAssign = "+ n_function);
					if(n_function.equals(function_name)) {
					//if(isEqualFunc(n_function, function_name_o)){
						System.out.println("Line " + line + ": " +"Duplicate argument " + arg_name +" in function "+function_name);
						flag = false;
					}
				}
			}
			if(flag == true) symtable.get(arg_name).add(node);

		}
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

		/*
		if(!symtable.contains(var_name)) symtable.put(var_name,node);
		else if (!symtable.get(fname) instanceof ACommaAssign ) symtable.put(fname,node);  //!!!!!!!!!!!!!!!
		*/
	}
	
	/*@Override  // list[a] = 
	public void inAListStatement(AListStatement node) {
		String list_name = node.getId().toString();
		
		if(!symtable.containsKey(list_name)) symtable.put(list_name,new ArrayList<Node>(Arrays.asList(node)));
		else symtable.get(list_name).add(node);


		//if(!symtable.contains(fname)) symtable.put(fname,node);
	}*/
}