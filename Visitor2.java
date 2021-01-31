import minipython.analysis.*;
import minipython.node.*;
import java.util.*;

public class Visitor2 extends DepthFirstAdapter {

	private Hashtable <String,ArrayList<Node>> symtable;	
	public static int errors; Node fun;
	String return_type; boolean in_function; boolean in_function_call; boolean in_func_declaration;
	ArrayList<String> real_arguments = null; 
	ArrayList<String> passed_arguments_types = null;

	//Constructor
	Visitor2(Hashtable symtable) {
		this.symtable = symtable;
		errors = 0;
		in_function = false;
		in_function_call = false;
	}

	/** Global boolean variable to know when we are inside a function declaration (to skip typechecking in variables) */
	@Override
	public void inAFunction(AFunction node) {
		in_func_declaration = true;
	}
	@Override
	public void outAFunction(AFunction node) {
		in_func_declaration = false;
	}
	
	/** Check if a called function is defined with the right amount of arguments.
	 *  Store parameters in global lists to do typechecking in function's statement */
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
				for(int i = 0; i < params-1; i++) {
					passed.add(((ACommaExp)((AArglist)node.getArglist().get(0)).getCommaExp().get(i)).getExpression());
				}
			}
			
			//get line
			int line = ((TId) node.getId()).getLine();

			//check if function is defined
			if(!symtable.containsKey(id)) {
				System.out.println("Line " + getLineNum(line)+ ": " +"Function " + id +" is not defined.");
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
				if(!flag) { 
					System.out.println("Line " + getLineNum(line)+ ": Function " + id +" is not defined.");
					errors++;
				}
				if(!enough_args) {
					System.out.println("Line " +getLineNum(line) + ": Function " + id +" with "+params+" arguments is not defined.");
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
	

	/** Operations (logical and arithmetic) typechecking (for variables, function calls, or values) */ 
	//ADD
	@Override
	public void caseAAddExpression(AAddExpression node) {
		inAAddExpression(node);
		if(node.getLpar() != null) {
			Node left = node.getLpar();
			arithmetic((PExpression)left,"Add");
		}
		if(node.getRpar() != null) {
			Node right = node.getRpar();
			arithmetic((PExpression)right,"Add");
		}
        outAAddExpression(node);
	}
	//MINUS
	@Override
	public void caseAMinusExpression(AMinusExpression node) {
		inAMinusExpression(node);
		if(node.getLpar() != null) {
			Node left = node.getLpar();
			arithmetic((PExpression)left,"Minus");
		}
		if(node.getRpar() != null) {
			Node right = node.getRpar();
			arithmetic((PExpression)right,"Minus");
		}
        outAMinusExpression(node);
	}
	//DIV
	@Override
	public void caseADivExpression(ADivExpression node) {
		inADivExpression(node);
		if(node.getLpar() != null) {
			Node left = node.getLpar();
			arithmetic((PExpression)left,"Div");
		}
		if(node.getRpar() != null) {
			Node right = node.getRpar();
			arithmetic((PExpression)right,"Div");
		}
        outADivExpression(node);
	}
	//MOD
	@Override
	public void caseAModExpression(AModExpression node) {
		inAModExpression(node);
		if(node.getLpar() != null) {
			Node left = node.getLpar();
			arithmetic((PExpression)left,"Mod");
		}
		if(node.getRpar() != null) {
			Node right = node.getRpar();
			arithmetic((PExpression)right,"Mod");
		}
        outAModExpression(node);
	}
	//MULT
	@Override
	public void caseAMultExpression(AMultExpression node) {
		inAMultExpression(node);
		if(node.getLpar() != null) {
			Node left = node.getLpar();
			arithmetic((PExpression)left,"Mult");
		}
		if(node.getRpar() != null) {
			Node right = node.getRpar();
			arithmetic((PExpression)right,"Mult");
		}
        outAMultExpression(node);
	}
	//MULTMULT
	@Override
	public void caseAMultmultExpression(AMultmultExpression node) {
		inAMultmultExpression(node);
		if(node.getLpar() != null) {
			Node left = node.getLpar();
			arithmetic((PExpression)left,"Power");
		}
		if(node.getRpar() != null) {
			Node right = node.getRpar();
			arithmetic((PExpression)right,"Power");
		}
        outAMultmultExpression(node);
	}
	//LESS
	@Override
	public void caseALessComparisonOr(ALessComparisonOr node) {
        inALessComparisonOr(node);
        if(node.getLpar() != null) {
			Node left = node.getLpar();
            arithmetic((PExpression)left,"Less");
        }
        if(node.getRpar() != null) {
            Node right = node.getRpar();
			arithmetic((PExpression)right,"Less");
        }
        outALessComparisonOr(node);
	}
	//GREAT
	@Override
	public void caseAGreatComparisonOr(AGreatComparisonOr node) {
        inAGreatComparisonOr(node);
        if(node.getLpar() != null) {
			Node left = node.getLpar();
            arithmetic((PExpression)left,"Greater");
        }
        if(node.getRpar() != null) {
            Node right = node.getRpar();
			arithmetic((PExpression)right,"Greater");
        }
        outAGreatComparisonOr(node);
	}
	//GREATER-EQUALS
	@Override
	public void caseAGeqComparisonOr(AGeqComparisonOr node) {
        inAGeqComparisonOr(node);
        if(node.getLpar() != null) {
			Node left = node.getLpar();
            arithmetic((PExpression)left,"Greater-equals");
        }
        if(node.getRpar() != null) {
            Node right = node.getRpar();
			arithmetic((PExpression)right,"Greater-equals");
        }
        outAGeqComparisonOr(node);
	}
	//LESS-EQUALS
	@Override
	public void caseALeqComparisonOr(ALeqComparisonOr node) {
        inALeqComparisonOr(node);
        if(node.getLpar() != null) {
			Node left = node.getLpar();
            arithmetic((PExpression)left,"Less-equals");
        }
        if(node.getRpar() != null) {
            Node right = node.getRpar();
			arithmetic((PExpression)right,"Less-equals");
        }
        outALeqComparisonOr(node);
	}
	//NOT EQUALS
	@Override
	public void caseANeqComparisonOr(ANeqComparisonOr node) {
        inANeqComparisonOr(node);
        if(node.getLpar() != null) {
			Node left = node.getLpar();
            arithmetic((PExpression)left,"Not-equals");
        }
        if(node.getRpar() != null) {
            Node right = node.getRpar();
			arithmetic((PExpression)right,"Not-equals");
        }
        outANeqComparisonOr(node);
	}
	//EQUALS
	@Override
	public void caseAEqComparisonOr(AEqComparisonOr node) {
        inAEqComparisonOr(node);
        if(node.getLpar() != null) {
			Node left = node.getLpar();
            arithmetic((PExpression)left,"Equals");
        }
        if(node.getRpar() != null) {
            Node right = node.getRpar();
			arithmetic((PExpression)right,"Equals");
        }
        outAEqComparisonOr(node);
	}
	//ASSIGN MIN
	/** Also, If the expression is a function call, check if it returns something */
	public void caseAAssignminStatement(AAssignminStatement node) {
        inAAssignminStatement(node);
        if(node.getId() != null) {
			node.getId().apply(this);
			//check type
			String id = node.getId().toString();
			int line = node.getId().getLine(); String type = null;
			ArrayList<Node> nodes = symtable.get(id); AAssignStatement n = null; int other_line;
			if(in_function_call) {
				if(real_arguments.contains(id)){
					int index = real_arguments.indexOf(id);
					type = passed_arguments_types.get(index);
			}}
			if (type==null){
				for(int i = 0; i < nodes.size(); i++) {
					if(nodes.get(i) instanceof AAssignStatement){
						other_line = ((AAssignStatement)nodes.get(i)).getId().getLine();
						if(other_line > line) break;
						else n = (AAssignStatement)nodes.get(i);
						type = (String)getOut(n);
			}}}
			if(in_func_declaration && real_arguments == null) type = "NUMBER";
			if(!type.equals("NUMBER")){
				System.out.println("Line " + getLineNum(line) + ": Assign Minus operation must be on numbers only.");
				errors++;
			}
        }
        if(node.getExpression() != null) {
			Node exp = node.getExpression();
            arithmetic((PExpression)exp,"Assign Minus");
        }
        outAAssignminStatement(node);
    }
	//ASSIGN DIV
	/** Also, If the expression is a function call, check if it returns something */
	public void caseAAssigndivStatement(AAssigndivStatement node) {
        inAAssigndivStatement(node);
        if(node.getId() != null) {
			node.getId().apply(this);
			//check type
			String id = node.getId().toString();
			int line = node.getId().getLine(); String type = null;
			ArrayList<Node> nodes = symtable.get(id); AAssignStatement n = null; int other_line;
			if(in_function_call) {
				if(real_arguments.contains(id)){
					int index = real_arguments.indexOf(id);
					type = passed_arguments_types.get(index);
			}}
			if (type==null){
				for(int i = 0; i < nodes.size(); i++) {
					if(nodes.get(i) instanceof AAssignStatement){
						other_line = ((AAssignStatement)nodes.get(i)).getId().getLine();
						if(other_line > line) break;
						else n = (AAssignStatement)nodes.get(i);
						type = (String)getOut(n);
			}}}
			if(in_func_declaration && real_arguments == null) type = "NUMBER";
			if(!type.equals("NUMBER")){
				System.out.println("Line " + getLineNum(line) + ": Assign Minus operation must be on numbers only.");
				errors++;
			}
        }
        if(node.getExpression() != null) {
            Node exp = node.getExpression();
            arithmetic((PExpression)exp,"Assign Div");
        }
        outAAssigndivStatement(node);
	}
	//List Index (List Expression)
	/** Also, If the expression is a function call, check if it returns something */
	@Override
	public void caseAListexpExpression(AListexpExpression node) {
        inAListexpExpression(node);
        if(node.getId() != null) {
            node.getId().apply(this);
        }
        if(node.getExpression() != null) {
            Node exp = node.getExpression();
            arithmetic((PExpression)exp,"List index");
        }
        outAListexpExpression(node);
    }
	//List Assign
	/** lists can only be assigned string or numeric values */
	@Override
	public void caseAListStatement(AListStatement node) {
        inAListStatement(node);
        if(node.getId() != null) {
            node.getId().apply(this);
        }
        if(node.getLbr() != null) {
			Node exp = node.getLbr();
            arithmetic((PExpression)exp,"List index");
        }
        if(node.getRbr() != null) {
			node.getRbr().apply(this);
        }
        outAListStatement(node);
    }

	
	/** Store the type of an id in the OUT hashtable, based on the expression assigned to it.
	 * If the expression is a function call, check if it returns something
	 * and add the return type to the hashtable */
	public void caseAAssignStatement(AAssignStatement node) {
        inAAssignStatement(node);
        if(node.getId() != null) {
            node.getId().apply(this);
        }
        if(node.getExpression() != null) {
            node.getExpression().apply(this);
		}
		
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
			if(list.getExpression() instanceof AValueExpression) {
				PValue val = ((AValueExpression)list.getExpression()).getValue();
				if(val instanceof ANumValue) type = "NUMBER";
				else if(val instanceof AStringValue) type = "STRING";
			}
			if(type == null) {
				int line = list.getLBr().getLine();
				System.out.println("Line "+getLineNum(line)+": Lists should be initialized with numbers or strings.");
				type = "ERROR";
				errors++;
			}
		}else if(exp instanceof AFuncCallExpression) {
			type = return_type;
		} else if(exp instanceof AAddExpression || exp instanceof AMinusExpression || exp instanceof ADivExpression 
				|| exp instanceof AModExpression || exp instanceof AMultExpression 
				|| exp instanceof AMultmultExpression || exp instanceof AParExpression){
					type = "NUMBER";
		}
		setOut(node,type);
        outAAssignStatement(node);
    }

	/** Check the return statement of a function to use it as a return type for function calls in expressions */
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
			AListConExpression list = (AListConExpression) expression;
			int line = list.getLBr().getLine();
			if(list.getExpression() instanceof AValueExpression && ((AValueExpression)list.getExpression()).getValue() instanceof ANumValue) return_type = "NUMBER";
			else if(list.getExpression() instanceof AValueExpression && ((AValueExpression)list.getExpression()).getValue() instanceof AStringValue) return_type = "STRING";
			else System.out.println("Line "+getLineNum(line)+": Lists should be initialized with numbers or strings.");
			in_function = false;
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
		}}}}
	}

	/**Check if a function called in a print statement returns something */
	@Override
	public void caseAPrintStatement(APrintStatement node) {
		inAPrintStatement(node);
		int line;
		if(!in_function) {
			if(node.getExpression() != null) {
				node.getExpression().apply(this);
				if(node.getExpression() instanceof AFuncCallExpression) {
					if(return_type == null) {
						line = ((AFunctionCall)((AFuncCallExpression)node.getExpression()).getFunctionCall()).getId().getLine();
						System.out.println("Line "+getLineNum(line)+ ": Function "+((AFunctionCall)((AFuncCallExpression)node.getExpression()).getFunctionCall()).getId().toString() +" doesn't return something.");
						errors++;
			}}}
			{
				Object temp[] = node.getCommaExp().toArray();
				for(int i = 0; i < temp.length; i++) {
					((PCommaExp) temp[i]).apply(this);
					if(((ACommaExp)((PCommaExp) temp[i])).getExpression() instanceof AFuncCallExpression) { 
						if(return_type == null) {
							line = ((AFunctionCall)((AFuncCallExpression)((ACommaExp)((PCommaExp) temp[i])).getExpression()).getFunctionCall()).getId().getLine();
							System.out.println("Line "+getLineNum(line)+": Function "+((AFunctionCall)((AFuncCallExpression)((ACommaExp)((PCommaExp) temp[i])).getExpression()).getFunctionCall()).getId().toString() +" doesn't return something.");
							errors++;
				}}}
			}
		}else {
			in_function = false;
		}
		outAPrintStatement(node);
    }

	/** Print error if the expression doesn't have numeric type - used in arithmetic and logical operations
	 * and in assign min/ assign div operators */
	private void arithmetic(PExpression expression, String operation){
		int other_line=0; String type = null; AAssignStatement n; ArrayList<Node> nodes; int line;
		ArrayList<String> real = null; ArrayList<String> passed = null;
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
			|| expression instanceof AMultmultExpression || expression instanceof AModExpression || expression instanceof ADivExpression)){
			if(expression instanceof AParExpression) {
				PExpression par_exp = ((AParExpression)expression).getExpression();
				if(!(par_exp instanceof AAddExpression || par_exp instanceof AMinExpression || par_exp instanceof AMultExpression 
				|| par_exp instanceof AMultmultExpression || par_exp instanceof AModExpression || par_exp instanceof ADivExpression)) {
					expression = par_exp;
				}
			}
			if(expression instanceof ATypeExpression) {
				line = ((ATypeExpression)expression).getId().getLine();
				System.out.println("Line " + getLineNum(line) + ": "+operation+" operation cannot be done on Type.");
				errors++;
			}else if(expression instanceof AOpenExpression) {
				line = ((AOpenExpression)expression).getOpen().getLine();
				System.out.println("Line " + getLineNum(line) +": "+operation+"  operation cannot be done on Open.");
				errors++;
			}else if(expression instanceof AValueExpression) {
				PValue val = ((AValueExpression)expression).getValue();
				if(val instanceof ANoneValue) {
					line = ((ANoneValue)val).getNone().getLine();
					System.out.println("Line " +getLineNum(line) +": "+operation+" operation cannot be done on None.");
					errors++;
				} else if(!(val instanceof ANumValue)) {
					if(val instanceof AStringValue) {
						line = ((AStringValue)val).getString().getLine();
					}else {
						line = ((AMethodValue)val).getId().getLine();
					}
					System.out.println("Line " + getLineNum(line) + ": "+operation+" operation must be on numbers only.");
					errors++;
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
						int index = real.indexOf(id);
						type = passed.get(index);
					}
				}
				if (type==null){
					for(int i = 0; i < nodes.size(); i++) {
						if(nodes.get(i) instanceof AAssignStatement){
							other_line = ((AAssignStatement)nodes.get(i)).getId().getLine();
							if(other_line > line) break;
							else n = (AAssignStatement)nodes.get(i);
							type = (String)getOut(n);
						}
					}
				}
				if(in_func_declaration && real_arguments == null) type = "NUMBER";
				
				if(type.equals("NONE")){
					System.out.println("Line " +getLineNum(line)+ ": "+operation+" operation cannot be done on None.");
					errors++;
				}else if(type.equals("TYPE")){
					System.out.println("Line " +getLineNum(line)+ ": "+operation+" operation cannot be done on Type.");
					errors++;
				}else if(type.equals("OPEN")){
					System.out.println("Line " +getLineNum(line)+ ": "+operation+" operation cannot be done on Open.");
					errors++;
				}else if(!type.equals("NUMBER")) {
					System.out.println("Line " +getLineNum(line)+ ": "+operation+" operation must be on numbers only.");
					errors++;
				}
			}else if(expression instanceof AFuncCallExpression) {
				//get the function and if it has a return type, write it in the global "return_type" variable (caseAFunctionCall)
				line = ((AFunctionCall)((AFuncCallExpression)expression).getFunctionCall()).getId().getLine();
				type = return_type;
				if(fun!=null) {
					if(type==null){
						System.out.println("Line " +getLineNum(line)+ ": " +"Function "+ ((AFunction)fun).getId().toString()+" doesn't return anything.");
						errors++;
					}
					else if(type.equals("NONE")) {
						System.out.println("Line " +getLineNum(line)+ ": "+operation+" operation cannot be done on None.");
						errors++;
					}else if(type.equals("OPEN")) { 
						System.out.println("Line " +getLineNum(line)+ ": "+operation+" operation cannot be done on Open.");
						errors++;
					}else if(type.equals("TYPE")) { 
						System.out.println("Line " +getLineNum(line)+ ": "+operation+" operation cannot be done on Type.");
						errors++;
					}else if(!type.equals("NUMBER")) {
						System.out.println("Line " +getLineNum(line)+ ": "+operation+" operation must be on numbers only.");
						errors++;
					}	
				}
			}else if(expression instanceof AListConExpression) {
				line = ((AListConExpression)expression).getLBr().getLine();
				System.out.println("Line " +getLineNum(line)+": Invalid Syntax.");
				errors++;
		}}
	}

	/** Find the type of an expression 
	 * Used to give types to parameters in function calls */
	private String getExpressionType(PExpression expression) {
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
			int line = ((AListConExpression)expression).getLBr().getLine();
			System.out.println("Line " +getLineNum(line)+ ": Invalid Syntax.");
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

	/** print correct line numbers */
	private int getLineNum(int line) {
		return (int)Math.ceil(line/2)+1;
	}
}