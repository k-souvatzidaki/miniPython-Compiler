import minipython.analysis.*;
import minipython.node.*;
import java.util.*;

public class Visitor2 extends DepthFirstAdapter {

	private Hashtable <String,ArrayList<Node>> symtable;	
	int errors; Node fun;
	String return_type; boolean in_function; boolean in_function_call; boolean in_func_declaration;
	ArrayList<String> real_arguments = null; 
	ArrayList<String> passed_arguments_types = null;

	Visitor2(Hashtable symtable) {
		this.symtable = symtable;
		errors = 0;
		in_function = false;
		in_function_call = false;
	}
	
	@Override
	public void inAFunction(AFunction node) {
		in_func_declaration = true;
	}
	@Override
	public void outAFunction(AFunction node) {
		in_func_declaration = false;
	}
	
	/** Check if a called function is defined with the right amount of argumentscall non defined function. */
	@Override
	public void caseAFunctionCall(AFunctionCall node) {
        inAFunctionCall(node);
        if(node.getId() != null) {
            node.getId().apply(this);
		} 
		fun = null; return_type = null;
		ArrayList<Node> passed = new ArrayList<Node>();
		ArrayList<String> real = new ArrayList<String>();
		ArrayList<String> passed_types = new ArrayList<String>();
        {
			//get id
			String id = node.getId().toString();
			//get # of arguments
			int params = node.getArglist().size();
			//add passed arguments in arraylist
			if(params == 1) {
				passed.add(((AArglist)node.getArglist().get(0)).getExpression()); 
				params+= ((AArglist)node.getArglist().get(0)).getCommaExp().size();
				System.out.println("Params "+params +"AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");
				for(int i = 0; i < params-1; i++) {
					passed.add(((ACommaExp)((AArglist)node.getArglist().get(0)).getCommaExp().get(i)).getExpression());
				}
			}
			
			//get line
			int line = ((TId) node.getId()).getLine();

			//check if function is defined
			if(!symtable.containsKey(id)) {
				System.out.println("Line " + line + ": " +"Function " + id +" is not defined");
				errors++;
			} else {
				ArrayList<Node> nodes = symtable.get(id);
				HashMap<String,PValue> real_default;
				boolean flag = false; boolean enough_args = false;
				for(int i =0; i< nodes.size(); i++ ) {
					if( nodes.get(i) instanceof AFunction) {
						real = new ArrayList<String>();
						real_default = new HashMap<String,PValue>();
						flag = true;
						//count args (non-default and total)
						AFunction n = (AFunction)nodes.get(i);
						int n_params = 0; int default_n_params = 0;
						if (n.getArgument().size() == 1) {
							AArgument args_n = (AArgument)n.getArgument().get(0);
							//add the first argument
							n_params = 1;
							default_n_params = args_n.getAssignV().size();
							if(default_n_params==1) real_default.put(args_n.getId().toString(), (((AAssignV)args_n.getAssignV().get(0)).getValue())); 
							real.add(args_n.getId().toString());
							//add the rest of the arguments
							n_params += args_n.getCommaAssign().size();
							for (Object v  : args_n.getCommaAssign()) {
								default_n_params += ((ACommaAssign) v).getAssignV().size();
								if(((ACommaAssign) v).getAssignV().size()==1) real_default.put(((ACommaAssign)v).getId().toString(), (((AAssignV)((ACommaAssign)v).getAssignV().get(0)).getValue()));
							    real.add(((ACommaAssign)v).getId().toString());
							}
						}
						if((default_n_params==0 && params==n_params) 
						|| (params >= n_params-default_n_params && n_params >= params)) {
							enough_args = true;
							fun = ((AFunction)nodes.get(i));
							while (real.size() > passed.size()){
								passed.add(real_default.get(real.get(passed.size())));
							}
							//convert passed arguments to their types
							String type = null;
							for(int k = 0; k < passed.size(); k++) {
								if(passed.get(k) instanceof PExpression) type = getExpressionType(((PExpression)passed.get(k)));
								else {
									PValue val = ((PValue)passed.get(k));
									if(val instanceof ANumValue) type = "NUMBER";
									else if (val instanceof ANoneValue) type ="NONE";
									else if (val instanceof AStringValue) type = "STRING";
								}
								passed_types.add(type);
							}
							
							break;
				}}}
				System.out.println("passed_arguments "+passed);
				System.out.println("passed_arguments types "+passed_types);
				System.out.println("real_arguments "+real);
				if(!flag) { 
					System.out.println("Line " + line + ": " +"Function " + id +" is not defined");
					errors++;
				}
				if(!enough_args) {
					System.out.println("Line " + line + ": " +"Function " + id +" with "+params+" arguments is not defined");
					errors++;
				}
			}
            Object temp[] = node.getArglist().toArray();
            for(int i = 0; i < temp.length; i++) {
                ((PArglist) temp[i]).apply(this);
			}
		}
		if(fun!=null) {
			real_arguments = real;
			passed_arguments_types = passed_types;
			in_function_call = true;
			((AFunction)fun).apply(this); //to find return type
			in_function_call = false;
			real_arguments = null;
			passed_arguments_types = null;
		}
        outAFunctionCall(node);
	}

	//find the type of an id variable
	public String getExpressionType(PExpression expression) {
		if (expression instanceof AAddExpression || expression instanceof AMinExpression || expression instanceof AMultExpression 
		    || expression instanceof AMultmultExpression || expression instanceof AModExpression || expression instanceof ADivExpression 
			|| expression instanceof AParExpression || expression instanceof AMinExpression || expression instanceof AMaxExpression){
			return "NUMBER";
		}else if(expression instanceof AValueExpression) {
			PValue val = ((AValueExpression)expression).getValue();
			if(val instanceof ANumValue) return "NUMBER";
			else if (val instanceof ANoneValue) return "NONE";
			else if (val instanceof AStringValue) return "STRING";
		}else if(expression instanceof ATypeExpression) return  "TYPE";
		else if(expression instanceof AOpenExpression) return "OPEN";
		else if(expression instanceof AListConExpression) {
			System.out.println("Line " + ": " +"Invalid Syntax");
			errors++;
		}else if(expression instanceof AListexpExpression ) {
			String id = ((AListexpExpression)expression).getId().toString();
			//if it's a global variable
			ArrayList<Node> nodes = symtable.get(id); AAssignStatement n = null;
			int line = ((AListexpExpression)expression).getId().getLine(); int other_line;
			for(int i = 0; i < nodes.size(); i++) {
				if(nodes.get(i) instanceof AAssignStatement){
					other_line = ((AAssignStatement)nodes.get(i)).getId().getLine();
					if(other_line > line) break;
					else n = (AAssignStatement)nodes.get(i);
				}
			}
			return (String)getOut(n);
		}
		else if(expression instanceof AIdExpression) {
			String id = ((AIdExpression)expression).getId().toString();
			//if it's a global variable
			ArrayList<Node> nodes = symtable.get(id); AAssignStatement n = null;
			int line = ((AIdExpression)expression).getId().getLine(); int other_line;
			for(int i = 0; i < nodes.size(); i++) {
				if(nodes.get(i) instanceof AAssignStatement){
					other_line = ((AAssignStatement)nodes.get(i)).getId().getLine();
					if(other_line > line) break;
					else n = (AAssignStatement)nodes.get(i);
			}}
			return (String)getOut(n);
		}
		return null;
	}

	

	/** Operations typechecking for variables and function calls */ 
	//ADD
	@Override
	public void caseAAddExpression(AAddExpression node) {
		System.out.println("HERE");
		inAAddExpression(node);
		int other_line=0; String type = null; AAssignStatement n; ArrayList<Node> nodes; int line;
		ArrayList<String> real = null; ArrayList<String> passed = null;
        if(node.getLpar() != null) {
			Node left = node.getLpar();
			if(left instanceof AFuncCallExpression) {
				in_function = true;
			}
			left.apply(this);
			if(in_function_call) {
				//deep copy arguments arrays
				System.out.println("MPLOU");
				real = new ArrayList<String>();
				for(String s : real_arguments) real.add(s);
				passed = new ArrayList<String>();
				for(String no : passed_arguments_types) passed.add(no);
			}
			if(!(left instanceof AAddExpression || left instanceof AMinExpression || left instanceof AMultExpression 
				|| left instanceof AMultmultExpression || left instanceof AModExpression || left instanceof ADivExpression 
				|| left instanceof AParExpression)) {
				if(left instanceof ATypeExpression) {
					System.out.println("Line " + ": " +"Add operation cannot be done on Type ");
				}else if(left instanceof AOpenExpression) {
					System.out.println("Line " + ": " +"Add operation cannot be done on Open");
				}else if(left instanceof AValueExpression) {
					PValue val = ((AValueExpression)left).getValue();
					if(val instanceof ANoneValue) {
						System.out.println("Line " + ": " +"Add operation cannot be done on None");
					} else if(!(val instanceof ANumValue)) {
						System.out.println("Line " + ": " +"Add operation must be on numbers only");
					}
				}else if(left instanceof AIdExpression || left instanceof AListexpExpression) {
					String id;
					if (left instanceof AIdExpression) {
						id = ((AIdExpression)left).getId().toString();
						line = ((AIdExpression)left).getId().getLine();
					}else {
						id = ((AListexpExpression)left).getId().toString();
						line = ((AListexpExpression)left).getId().getLine();
					}
					//System.out.println("a");
					nodes = symtable.get(id); n = null;
					System.out.println("ou");
					if(in_function_call) {
						if(real.contains(id)){
							System.out.println("IN FUNCTION CALL");
							int index = real.indexOf(id);
							type = passed.get(index);
						}
					}
					if (type==null){
						System.out.println("TYPE IS NULL");
						for(int i = 0; i < nodes.size(); i++) {
							if(nodes.get(i) instanceof AAssignStatement){
								other_line = ((AAssignStatement)nodes.get(i)).getId().getLine();
								if(other_line > line) break;
								else n = (AAssignStatement)nodes.get(i);
								type = (String)getOut(n);
							}
						}
					}
					if(in_func_declaration && real_arguments == null) {
						type = "NUMBER";
						System.out.println("TYPE IS STILL NULL");
					}
					
					if(type.equals("NONE")){
						System.out.println("Line " + ": " +"Add operation cannot be done on None");
					}else if(type.equals("TYPE")){
						System.out.println("Line " + ": " +"Add operation cannot be done on Type");
					}else if(type.equals("OPEN")){
						System.out.println("Line " + ": " +"Add operation cannot be done on open");
					}else if(!type.equals("NUMBER")) {
						System.out.println("Line " + ": " +"Add operation must be on numbers only");
					}
				}else if(left instanceof AFuncCallExpression) {
					//get the function and if it has a return type, write it in the global "return_type" variable (caseAFunctionCall)
					if (fun!= null){
						type = return_type;
						if(type==null){
							System.out.println("Line " + ": " +"Function "+ ((AFunction)fun).getId().toString()+" doesn't return anything");
						}
						else if(type.equals("NONE")) {
							System.out.println("Line " + ": " +"Add operation cannot be done on None");
						}else if(type.equals("OPEN")) { 
							System.out.println("Line " + ": " +"Add operation cannot be done on Open");
						}else if(type.equals("TYPE")) { 
							System.out.println("Line " + ": " +"Add operation cannot be done on Type");
						}else if(!type.equals("NUMBER")) {
							System.out.println("Line " + ": " +"Add operation must be on numbers only");
						}
					}
				}else if(left instanceof AListConExpression) {
					System.out.println("Line " + ": " +"Invalid Syntax");
		}}}

		if(node.getRpar() != null) {
			Node right = node.getRpar();
			//call func arithmetic
			if(right instanceof AFuncCallExpression) {
				in_function = true;
			}
			right.apply(this);
			if(in_function_call) {
				//deep copy arguments arrays
				real = new ArrayList<String>();
				for(String s : real_arguments) real.add(s);
				passed = new ArrayList<String>();
				for(String no : passed_arguments_types) passed.add(no);
			}
			if(!(right instanceof AAddExpression || right instanceof AMinExpression || right instanceof AMultExpression 
				|| right instanceof AMultmultExpression || right instanceof AModExpression || right instanceof ADivExpression
				|| right instanceof AParExpression)) {
				if(right instanceof ATypeExpression) {
					System.out.println("Line " + ": " +"Add operation cannot be done on Type ");
				}else if(right instanceof AOpenExpression) {
					System.out.println("Line " + ": " +"Add operation cannot be done on Open");
				}else if(right instanceof AValueExpression) {
					PValue val = ((AValueExpression)right).getValue();
					if(val instanceof ANoneValue) {
						System.out.println("Line " + ": " +"Add operation cannot be done on None");
					} else if(!(val instanceof ANumValue)) {
						System.out.println("Line " + ": " +"Add operation must be on numbers only");
					}
				}else if(right instanceof AIdExpression || right instanceof AListexpExpression) {
					String id;
					if (right instanceof AIdExpression) {
						id = ((AIdExpression)right).getId().toString();
						line = ((AIdExpression)right).getId().getLine();
					}else {
						id = ((AListexpExpression)right).getId().toString();
						line = ((AListexpExpression)right).getId().getLine();
					}
					nodes = symtable.get(id); n = null;
					if(in_function_call) {
						if(real.contains(id)){
							System.out.println("IN FUNCTION CALL");
							int index = real.indexOf(id);
							type = passed.get(index);
						}
					}
					if (type==null){
						System.out.println("TYPE IS NULL");
						for(int i = 0; i < nodes.size(); i++) {
							if(nodes.get(i) instanceof AAssignStatement){
								other_line = ((AAssignStatement)nodes.get(i)).getId().getLine();
								if(other_line > line) break;
								else n = (AAssignStatement)nodes.get(i);
								type = (String)getOut(n);
							}
						}
					}
					if(in_func_declaration && real_arguments == null) {
						type = "NUMBER";
						System.out.println("TYPE IS STILL NULL");
					}
					
					if(type.equals("NONE")){
						System.out.println("Line " + ": " +"Add operation cannot be done on None");
					}else if(type.equals("TYPE")){
						System.out.println("Line " + ": " +"Add operation cannot be done on Type");
					}else if(type.equals("OPEN")){
						System.out.println("Line " + ": " +"Add operation cannot be done on Open");
					}else if(!type.equals("NUMBER")) {
						System.out.println("Line " + ": " +"Add operation must be on numbers only");
					}
				}else if(right instanceof AFuncCallExpression) {
					//get the function and if it has a return type, write it in the global "return_type" variable (caseAFunctionCall)
					if (fun!=null){
						type =return_type;
						if(type==null && fun!=null){
							System.out.println("Line " + ": " +"Function "+ ((AFunction)fun).getId().toString()+" doesn't return anything");
						}
						else if(type.equals("NONE")) {
							System.out.println("Line " + ": " +"Add operation cannot be done on None");
						}else if(type.equals("OPEN")) { 
							System.out.println("Line " + ": " +"Add operation cannot be done on Open");
						}else if(type.equals("TYPE")) { 
							System.out.println("Line " + ": " +"Add operation cannot be done on Type");
						}else if(!type.equals("NUMBER")) {
							System.out.println("Line " + ": " +"Add operation must be on numbers only");
						}	
					}
				}else if(right instanceof AListConExpression) {
					System.out.println("Line " + ": " +"Invalid Syntax");
		}}}
        outAAddExpression(node);
	}

	/*private void arithmetic(PExpression expression, String operation){
		String line;
		String error_msg = operation+ " operation must be on numbers only";
		if(expression instanceof AFuncCallExpression) {
			in_function = true;
		}
		expression.apply(this);
		if(in_function_call) {
			//deep copy arguments arrays
			real = new ArrayList<String>();
			for(String s : real_arguments) real.add(s);
			passed = new ArrayList<String>();
			for(String no : passed_arguments_types) passed.add(no);
		}
		if(!(expression instanceof AAddExpression || expression instanceof AMinExpression || expression instanceof AMultExpression 
			|| expression instanceof AMultmultExpression || expression instanceof AModExpression || expression instanceof ADivExpression
			|| expression instanceof AParExpression)) {
			if(expression instanceof ATypeExpression) {
				System.out.println("Line " + ": " +"Add operation cannot be done on Type ");
			}else if(expression instanceof AOpenExpression) {
				System.out.println("Line " + ": " +"Add operation cannot be done on Open");
			}else if(expression instanceof AValueExpression) {
				PValue val = ((AValueExpression)expression).getValue();
				if(val instanceof ANoneValue) {
					System.out.println("Line " + ": " +"Add operation cannot be done on None");
				} else if(!(val instanceof ANumValue)) {
					System.out.println("Line " + ": " +"Add operation must be on numbers only");
				}
			}else if(expression instanceof AIdExpression || expression instanceof AListexpExpression) {
				String id;
				if (expression instanceof AIdExpression) {
					id = ((AIdExpression)expression).getId().toString();
					line = ((AIdExpression)expression).getId().getLine();
				}else {
					id = ((AListexpExpression)expression).getId().toString();
					line = ((AListexpExpression)expression).getId().getLine();
				}
				nodes = symtable.get(id); n = null;
				if(in_function_call) {
					if(real.contains(id)){
						System.out.println("IN FUNCTION CALL");
						int index = real.indexOf(id);
						type = passed.get(index);
					}
				}
				if (type==null){
					System.out.println("TYPE IS NULL");
					for(int i = 0; i < nodes.size(); i++) {
						if(nodes.get(i) instanceof AAssignStatement){
							other_line = ((AAssignStatement)nodes.get(i)).getId().getLine();
							if(other_line > line) break;
							else n = (AAssignStatement)nodes.get(i);
							type = (String)getOut(n);
						}
					}
				}
				if(in_func_declaration && real_arguments == null) {
					type = "NUMBER";
					System.out.println("TYPE IS STILL NULL");
				}
				
				if(type.equals("NONE")){
					System.out.println("Line " + ": " +"Add operation cannot be done on None");
				}else if(type.equals("TYPE")){
					System.out.println("Line " + ": " +"Add operation cannot be done on Type");
				}else if(type.equals("OPEN")){
					System.out.println("Line " + ": " +"Add operation cannot be done on Open");
				}else if(!type.equals("NUMBER")) {
					System.out.println("Line " + ": " +"Add operation must be on numbers only");
				}
			}else if(right instanceof AFuncCallExpression) {
				//get the function and if it has a return type, write it in the global "return_type" variable (caseAFunctionCall)
				type =return_type;
				if(type==null){
					System.out.println("Line " + ": " +"Function "+ ((AFunction)fun).getId().toString()+" doesn't return anything");
				}
				else if(type.equals("NONE")) {
					System.out.println("Line " + ": " +"Add operation cannot be done on None");
				}else if(type.equals("OPEN")) { 
					System.out.println("Line " + ": " +"Add operation cannot be done on Open");
				}else if(type.equals("TYPE")) { 
					System.out.println("Line " + ": " +"Add operation cannot be done on Type");
				}else if(!type.equals("NUMBER")) {
					System.out.println("Line " + ": " +"Add operation must be on numbers only");
				}	
			}else if(right instanceof AListConExpression) {
				System.out.println("Line " + ": " +"Invalid Syntax");
			}}
	}*/

	/** Check the return statement of a function to use it as a type for function calls in expressions */
	@Override
	public void inAReturnStatement(AReturnStatement node) {
		PExpression expression = node.getExpression();
		if (expression instanceof AAddExpression || expression instanceof AMinExpression || expression instanceof AMultExpression 
		    || expression instanceof AMultmultExpression || expression instanceof AModExpression || expression instanceof ADivExpression 
			|| expression instanceof AParExpression || expression instanceof AMinExpression || expression instanceof AMaxExpression){
			return_type = "NUMBER";
		}else if(expression instanceof AValueExpression) {
			in_function = false;
			PValue val = ((AValueExpression)expression).getValue();
			if(val instanceof ANumValue) return_type = "NUMBER";
			else if (val instanceof ANoneValue) return_type = "NONE";
			else if (val instanceof AStringValue) return_type = "STRING";
		}else if(expression instanceof ATypeExpression) { return_type = "TYPE"; in_function = false; }
		else if(expression instanceof AOpenExpression) { return_type = "OPEN"; in_function = false; }
		else if(expression instanceof AListConExpression) {
			System.out.println("Line " + ": " +"Invalid Syntax");
			errors++; in_function = false;
		}
		else if(expression instanceof AIdExpression || expression instanceof AListexpExpression) {
			String id; int line;
			if (expression instanceof AIdExpression) {
				id = ((AIdExpression)expression).getId().toString();
				line = ((AIdExpression)expression).getId().getLine();
			}else {
				id = ((AListexpExpression)expression).getId().toString();
				line = ((AListexpExpression)expression).getId().getLine();
			}
			in_function = false;
			//if it's a global variable
			ArrayList<Node> nodes = symtable.get(id); AAssignStatement n = null; int other_line;
			if(in_function_call) {
				if(real_arguments.contains(id)){
					System.out.println("IN FUNCTION CALL");
					int index = real_arguments.indexOf(id);
					return_type = passed_arguments_types.get(index);
				}
			}
			if(return_type==null){
				for(int i = 0; i < nodes.size(); i++) {
					if(nodes.get(i) instanceof AAssignStatement){
						other_line = ((AAssignStatement)nodes.get(i)).getId().getLine();
						if(other_line > line) break;
						else n = (AAssignStatement)nodes.get(i);
						return_type = (String)getOut(n);
				}}
			}
			/*if(in_func_declaration && real_arguments==null){

			}*/
		}
	}


	/** if a function doesn't have a return statement while looking for return type, the return type must be null*/

	//TODO do the same for : assign statements, function call(statement), list assign
	@Override
	public void caseAPrintStatement(APrintStatement node) {
		inAPrintStatement(node);
		System.out.println(in_function);
		//if (node.getExpression() instanceof AFuncCallExpression) in_function=true;
		if(!in_function) {
			System.out.println("NOT IN FUNCTION");
			if(node.getExpression() != null) {
				//in_function = true;
				node.getExpression().apply(this);
				if(node.getExpression() instanceof AFuncCallExpression) {
					System.out.println("return "+return_type);
					if(return_type == null) {
						System.out.println("MPLOU");
						System.out.println("Line "+" : Function "+((AFunctionCall)((AFuncCallExpression)node.getExpression()).getFunctionCall()).getId().toString() +" doesn't return something");
					}
				}
			}
			{
				Object temp[] = node.getCommaExp().toArray();
				for(int i = 0; i < temp.length; i++) {
					((PCommaExp) temp[i]).apply(this);
					if(((ACommaExp)((PCommaExp) temp[i])).getExpression() instanceof AFuncCallExpression) { 
						if(return_type == null) {
							System.out.println("Line "+" : Function "+((AFunctionCall)((AFuncCallExpression)((ACommaExp)((PCommaExp) temp[i])).getExpression()).getFunctionCall()).getId().toString() +" doesn't return something");
						}
					}
				}
			}
		}else {
			in_function = false;
			System.out.println("Line "+" : Function mpla " +" doesn't return something");
			//return_type = null;
		}
		outAPrintStatement(node);
		
		//TODO check if a function used in print statements returns something (same for all assign statements and list assign statements)
    }


	/** Store the type of an id in the OUT hashtable */
	@Override
	public void inAAssignStatement(AAssignStatement node) {
		//get value type and store in "out" Hashtable
		PExpression exp = node.getExpression();
		String type = null; 
		if(exp instanceof AValueExpression) {
			AValueExpression val = (AValueExpression)exp;
			if ( val.getValue() instanceof ANumValue) type = "NUMBER";
			else if(val.getValue() instanceof AStringValue) type = "STRING";
			else if(val.getValue() instanceof ANoneValue) type = "NONE";
			else if(val.getValue() instanceof AMethodValue) {}
		}else if (exp instanceof AIdExpression) {
			ArrayList<Node> nodes = symtable.get(((AIdExpression)exp).getId().toString());
			AAssignStatement id = null;
			int line = ((AIdExpression)exp).getId().getLine(); int other_line;
			for(int i = 0; i < nodes.size(); i++) {
				if(nodes.get(i) instanceof AAssignStatement){
					other_line = ((AAssignStatement)nodes.get(i)).getId().getLine();
					if(other_line > line) break;
					else id = (AAssignStatement)nodes.get(i);
				}
			}
			type = (String)getOut(id);
		}else if(exp instanceof AListexpExpression) {
			ArrayList<Node> nodes = symtable.get(((AListexpExpression)exp).getId().toString());
			AAssignStatement id = null;
			for(int i = 0; i< nodes.size(); i++) {
				if(nodes.get(i) instanceof AAssignStatement) id = (AAssignStatement)nodes.get(i);
			}
			type = (String)getOut(id);
		}
		else if(exp instanceof AMaxExpression || exp instanceof AMinExpression) type = "NUMBER";
		else if(exp instanceof AOpenExpression) type = "OPEN";
		else if(exp instanceof ATypeExpression) type = "TYPE";
		else if(exp instanceof  AListConExpression) {
			AListConExpression list = (AListConExpression) exp;
			if(list.getExpression() instanceof AValueExpression && ((AValueExpression)list.getExpression()).getValue() instanceof ANumValue) type = "NUMBER";
			else type = "STRING";
		}else if(exp instanceof AFuncCallExpression) {
			//??????????
		} else if(exp instanceof AAddExpression || exp instanceof AMinusExpression || exp instanceof ADivExpression 
				|| exp instanceof AModExpression || exp instanceof AMultExpression 
				|| exp instanceof AMultmultExpression || exp instanceof AParExpression){
					//????????
		}
		setOut(node,type);
		
	}



}