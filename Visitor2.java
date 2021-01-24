import minipython.analysis.*;
import minipython.node.*;
import java.util.*;

public class Visitor2 extends DepthFirstAdapter {

	private Hashtable <String,ArrayList<Node>> symtable;	
	int errors; Node fun;
	String return_type; boolean in_function;

	Visitor2(Hashtable symtable) {
		this.symtable = symtable;
		errors = 0;
		in_function = false;
    }
	
	/** Check if a called function is defined with the right amount of argumentscall non defined function. */
	@Override
	public void caseAFunctionCall(AFunctionCall node) {
        inAFunctionCall(node);
        if(node.getId() != null) {
            node.getId().apply(this);
		} 
		fun = null; return_type = null;
        {
			//get id
			String id = node.getId().toString();
			//get # of arguments
			int params = node.getArglist().size();
			if(params == 1) {
				params+= ((AArglist)node.getArglist().get(0)).getCommaExp().size();
			}
			//get line
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
						if((default_n_params==0 && params==n_params) 
						|| (params >= n_params-default_n_params && n_params >= params)) {
							enough_args = true;
							fun = ((AFunction)nodes.get(i));
							break;
				}}}
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
			((AFunction)fun).apply(this); //to find return type
		}
        outAFunctionCall(node);
	}
	

	/** Operations typechecking for variables and function calls */ 
	//ADD
	@Override
	public void caseAAddExpression(AAddExpression node) {
		inAAddExpression(node);
		int other_line=0; String type; AAssignStatement n; ArrayList<Node> nodes; int line;
        if(node.getLpar() != null) {
			Node left = node.getLpar();
			if(left instanceof AFuncCallExpression) in_function = true;
			left.apply(this);
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
				}else if(left instanceof AIdExpression) {
					String id = ((AIdExpression)left).getId().toString();
					nodes = symtable.get(id); n = null;
					line = ((AIdExpression)left).getId().getLine();
					for(int i = 0; i < nodes.size(); i++) {
						if(nodes.get(i) instanceof AAssignStatement) {
							other_line = ((AAssignStatement)nodes.get(i)).getId().getLine();
							if(other_line > line) break;
							else n = (AAssignStatement)nodes.get(i);
					}}
					type = (String)getOut(n);
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
				}else if(left instanceof AListexpExpression) {
					//TODO
				}else if(left instanceof AListConExpression) {
					System.out.println("Line " + ": " +"Invalid Syntax");
		}}}

		if(node.getRpar() != null) {
			Node right = node.getRpar();
			if(right instanceof AFuncCallExpression) in_function = true;
			right.apply(this);
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
				}else if(right instanceof AIdExpression) {
					String id = ((AIdExpression)right).getId().toString();
					nodes = symtable.get(id); n = null;
					line = ((AIdExpression)right).getId().getLine();
					for(int i = 0; i < nodes.size(); i++) {
						if(nodes.get(i) instanceof AAssignStatement){
							other_line = ((AAssignStatement)nodes.get(i)).getId().getLine();
							if(other_line > line) break;
							else n = (AAssignStatement)nodes.get(i);
						}
					}
					type = (String)getOut(n);
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
				}else if(right instanceof AListexpExpression) {
					//TODO
				}else if(right instanceof AListConExpression) {
					System.out.println("Line " + ": " +"Invalid Syntax");
		}}}
        outAAddExpression(node);
	}


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
		}else if(expression instanceof AListexpExpression ) {
			in_function = false;
			String id = ((AListexpExpression)expression).getId().toString();
			//if it's a global variable
			ArrayList<Node> nodes = symtable.get(id); AAssignStatement n = null;
			int line = ((AListexpExpression)expression).getId().getLine(); int other_line;
			for(int i = 0; i < nodes.size(); i++) {
				if(nodes.get(i) instanceof AAssignStatement){
					other_line = ((AAssignStatement)nodes.get(i)).getId().getLine();
					if(other_line > line) break;
					else n = (AAssignStatement)nodes.get(i);
			}}
			return_type = (String)getOut(n);
			//TODO if it's an argument in a function call ..
		}
		else if(expression instanceof AIdExpression) {
			in_function = false;
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
			return_type = (String)getOut(n);
			//TODO if it's an argument in a function call ..
		}
	}


	/** if a function doesn't have a return statement while looking for return type, the return type must be null*/
	@Override
	public void caseAPrintStatement(APrintStatement node) {
		inAPrintStatement(node);
		if(!in_function) {
			if(node.getExpression() != null) {
				node.getExpression().apply(this);
			}
			{
				Object temp[] = node.getCommaExp().toArray();
				for(int i = 0; i < temp.length; i++) {
					((PCommaExp) temp[i]).apply(this);
				}
			}
		}else {
			in_function = false;
			//return_type = null;
		}
        outAPrintStatement(node);
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