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
	
	//call non defined function. call function with wrong args
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
	


	//operations typechecking for variables
	@Override
	public void caseAAddExpression(AAddExpression node) {
		inAAddExpression(node);

        if(node.getLpar() != null) {
			node.getLpar().apply(this);
			Node left = node.getLpar();
			System.out.println("Printing left: "+ left);
			if(!(left instanceof AAddExpression || left instanceof AMinExpression || left instanceof AMultExpression 
			    || left instanceof AMultmultExpression || left instanceof AModExpression || left instanceof ADivExpression)) {

				System.out.println(left);
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
					System.out.println("HERE");
					String id = ((AIdExpression)left).getId().toString();
					ArrayList<Node> nodes = symtable.get(id);
					AAssignStatement n = null;
					System.out.println(nodes);
					for(int i = 0; i < nodes.size(); i++) {
						if(nodes.get(i) instanceof AAssignStatement){ n = (AAssignStatement)nodes.get(i); System.out.println("IF");}
					}
					System.out.println(n);
					String type = (String)getOut(n);
					System.out.println(type);
					if(!type.equals("NUMBER")) {
						System.out.println("Line " + ": " +"Add operation must be on numbers only");
					}
				
				}else if(left instanceof AListConExpression) {
					System.out.println("Line " + ": " +"Invalid Syntax");
				}	
			}
		}

		if(node.getRpar() != null) {
			node.getRpar().apply(this);
			Node right = node.getRpar();
			if(!(right instanceof AAddExpression || right instanceof AMinExpression || right instanceof AMultExpression 
			    || right instanceof AMultmultExpression || right instanceof AModExpression || right instanceof ADivExpression)) {

				System.out.println(right);
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
					System.out.println("HERE");
					String id = ((AIdExpression)right).getId().toString();
					ArrayList<Node> nodes = symtable.get(id);
					AAssignStatement  n = null;
					for(int i = 0; i < nodes.size(); i++) {
						if(nodes.get(i) instanceof AAssignStatement) n = (AAssignStatement)nodes.get(i);
					}
					String type = (String)getOut(n);
					if(!type.equals("NUMBER")) {
						System.out.println("Line " + ": " +"Add operation must be on numbers only");
					}
					System.out.println(type);
				
				}else if(right instanceof AListConExpression) {
					System.out.println("Line " + ": " +"Invalid Syntax");
				}	
			}
		}

		/*
        if(node.getRpar() != null) {
			node.getRpar().apply(this);
			Node right = node.getRpar();
			if(!(right instanceof AAddExpression || right instanceof AMinExpression || right instanceof AMultExpression 
			    || right instanceof AMultmultExpression || right instanceof AModExpression || right instanceof ADivExpression)) {
				System.out.println(right);
				if(right instanceof AValueExpression) {
					if(!(((AValueExpression)right).getValue() instanceof ANumValue)) {
						System.out.println("Line " + ": " +"Add operation must be on numbers only");
					}
				}else if (right instanceof AIdExpression) {
					String id = ((AIdExpression)right).getId().toString();
					ArrayList<Node> temp = symtable.get(id);
					for(int i = 0; i < temp.size(); i++ ) {
						if(temp.get(i) instanceof AAssignStatement) {
							//get type of variable
							PExpression var_type = ((AAssignStatement)temp.get(i)).getExpression();
							if(var_type instanceof )
							if(var)
							if(!(((AAssignStatement)temp.get(i)).getExpression()) )
						}
					}
				}
			}
        } */
        outAAddExpression(node);
	}
	


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
			System.out.println("here            aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
			ArrayList<Node> nodes = symtable.get(((AIdExpression)exp).getId().toString());
			System.out.println(nodes);
			AAssignStatement id = null;
			for(int i = 0; i< nodes.size(); i++) {
				if(nodes.get(i) instanceof AAssignStatement) id = (AAssignStatement)nodes.get(i);
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
		}else if(exp instanceof AFuncCallExpression) {}
		else if(exp instanceof AAddExpression || exp instanceof AMinusExpression || exp instanceof ADivExpression 
		        || exp instanceof AModExpression || exp instanceof AMultExpression || exp instanceof AMultmultExpression || exp instanceof AParExpression){}
		System.out.println(type);
		setOut(node,type);
		System.out.println((String)getOut(node));
	}
    
}
