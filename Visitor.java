import minipython.analysis.*;
import minipython.node.*;
import java.util.*;

public class Visitor extends DepthFirstAdapter {

	private Hashtable <String,ArrayList<Node>> symtable;	
	/*x, argument, argument, def name, list */

	Visitor(Hashtable symtable) {
		this.symtable = symtable;
	}


	// @Override
	// public void inAFunCommands(AFunCommands node) {

	// 	String fName = node.getId().toString();
	// 	int line = ((TId) node.getId()).getLine();
	// 	if (symtable.containsKey(fName)) {
	// 		System.out.println("Line " + line + ": " +" Function " + fName +" is already defined");
	// 	}else {
	// 		symtable.put(fName, node);
	// 	}
	// }

	
	/*@Override
	public void inAFunction(AFunction node) {

		System.out.println("\nInside inAFunction");

        String function_name = node.getId().toString();
		int line = ((TId) node.getId()).getLine();
		System.out.println(line);

		if(!symtable.containsKey(function_name)) symtable.put(function_name,new ArrayList<Node>(Arrays.asList(node)));
		else{
			//TODO get number of arguments and print error ONLY if same name with same # of args
			ArrayList<Node> temp = symtable.get(function_name);
			for(Node n : temp) {
				if(n instanceof AFunction){
					System.out.println("Line " + line + ": " +" Function " + function_name +" is already defined");
					break;
				}
			}
		}
	 	
	}*/
	
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
			ArrayList<Node> temp1 = symtable.get(function_name);
			boolean flag = true;
			if (temp1 != null) {
				for(Node n : temp1) {
					if(n instanceof AFunction){
						int size_n=0;
						if (((AFunction) n).getArgument().size() == 1) {
							size_n = 1 + ((AArgument) ((AFunction) n).getArgument().get(0)).getCommaAssign().size();
						}
						int size_node=0;
						if (((AFunction) node).getArgument().size() == 1) {
							size_node = 1 + ((AArgument) ((AFunction) node).getArgument().get(0)).getCommaAssign().size();
						}
						if (size_n == size_node){
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

	//TODO different functions with same arguments doesn't work 
	@Override
	public void inAArgument(AArgument node) {

		System.out.println("Inside inAArgument");


		String arg_name = node.getId().toString();
		//the first argument is never a duplicate
		if(!symtable.containsKey(arg_name)) symtable.put(arg_name,new ArrayList<Node>(Arrays.asList(node)));
		else symtable.get(arg_name).add(node);
		//the rest of the arguments are "CommaAssign"s, checked in "inACommaAssign(..)"
	}
	
	@Override
	public void inACommaAssign(ACommaAssign node) {

		System.out.println("Inside inACommaAssign");


		String arg_name = node.getId().toString(); //get name of the argument
		String function_name = ((AFunction)(node.parent().parent())).getId().toString(); //get name of the function
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
					System.out.println("Parent of AArgument = "+ n_function);
					if(n_function.equals(function_name)) {
						System.out.println("Line " + line + ": " +"Duplicate argument " + arg_name +" in function "+function_name);
						flag = false;
					}
				} else if(n instanceof ACommaAssign) {
					String n_function = ((AFunction)(n.parent().parent())).getId().toString();
					System.out.println("Parent of ACommaAssign = "+ n_function);
					if(n_function.equals(function_name)) {
						System.out.println("Line " + line + ": " +"Duplicate argument " + arg_name +" in function "+function_name);
						flag = false;
					}
				}
			}
			if(flag == true) symtable.get(arg_name).add(node);


			/*
			//if symbol with the same name exists 
			ArrayList<Node> temp = symtable.get(arg_name);
			for(int i =0; i < temp.size(); i++) {
				Node n = temp.get(i);
				System.out.println("CLASS "+n.getClass());
				if (!(n instanceof ACommaAssign)) {
					if(!(n instanceof AArgument)) symtable.get(arg_name).add(node);
					else {
						String n_function = ((AFunction)(n.parent())).getId().toString();
						System.out.println("FUNCTION "+n_function);
						if(!n_function.equals(function_name)) symtable.get(arg_name).add(node);
						else {
							System.out.println("Line " + line + ": " +"Duplicate argument " + arg_name +" in function "+function_name);
							break;
						}
					}
				}else{
					String n_function = ((AFunction)(n.parent().parent())).getId().toString();
					System.out.println("COMMAASSIGN, in FUNCTION "+n_function);
					if(!n_function.equals(function_name)) symtable.get(arg_name).add(node);
					else{
						System.out.println(function_name + " "+n_function);
						System.out.println("Line " + line + ": " +"Duplicate argument " + arg_name +" in function "+function_name);
						break;
					}
				}
			}
			*/
		}
	}
	
	/* @Override
	public void inAAssignV(AAssignV node) {
        defaultIn(node);
	} 
	*/
	
	// @Override
	// public void inAIfStatement(AIfStatement node) {
    //     defaultIn(node);
	// }
	
	// @Override
	// public void inAWhileStatement(AWhileStatement node) {
    //     defaultIn(node);
	// }
	
	// @Override
	// public void inAForStatement(AForStatement node) {
    //     defaultIn(node);
	// }
	
	// @Override
	// public void inAReturnStatement(AReturnStatement node) {
    //     defaultIn(node);
	// }
	
	// @Override
	// public void inAPrintStatement(APrintStatement node) {
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
	
	/*@Override
	public void inAAssignminStatement(AAssignminStatement node) {
        defaultIn(node);
	}
	
	@Override
	public void inAAssigndivStatement(AAssigndivStatement node) {
        defaultIn(node);
	} */
	
	@Override
	public void inAListStatement(AListStatement node) {
		String list_name = node.getId().toString();
		
		if(!symtable.containsKey(list_name)) symtable.put(list_name,new ArrayList<Node>(Arrays.asList(node)));
		else symtable.get(list_name).add(node);


		//if(!symtable.contains(fname)) symtable.put(fname,node);
	}
	
	/*
	@Override
	public void inAAssertStatement(AAssertStatement node) {
        defaultIn(node);
	}
	
	@Override
	public void inAFuncStatement(AFuncStatement node) {
        defaultIn(node);
	}
	*/
	
	/*
	@Override
	public void inAExprModExpression(AExprModExpression node) {
        defaultIn(node);
	}
	
	@Override
	public void inAMinusExpression(AMinusExpression node) {
        defaultIn(node);
	}
	
	@Override
	public void inAAddExpression(AAddExpression node) {
        defaultIn(node);
	}
	
	@Override
	public void inAExprMultmultExpressionMod(AExprMultmultExpressionMod node) {
        defaultIn(node);
	}
	
	@Override
	public void inAModExpressionMod(AModExpressionMod node) {
        defaultIn(node);
	}
	
	@Override
	public void inADivExpressionMod(ADivExpressionMod node) {
        defaultIn(node);
	}
	
	@Override
	public void inAMultExpressionMod(AMultExpressionMod node) {
        defaultIn(node);
	}
	
	@Override
	public void inAExprExpressionMultmult(AExprExpressionMultmult node) {
        defaultIn(node);
	}
	
	@Override
	public void inAMultmultExpressionMultmult(AMultmultExpressionMultmult node) {
        defaultIn(node);
	}
	
	
	@Override
	public void inASthExpr(ASthExpr node) {
        defaultIn(node);
	}
	
	@Override
	public void inAListexpExpr(AListexpExpr node) {
        defaultIn(node);
	}
	
	@Override
	public void inAOpenExpr(AOpenExpr node) {
        defaultIn(node);
	}
	
	@Override
	public void inAListConExpr(AListConExpr node) {
        defaultIn(node);
	}
	
	@Override
	public void inAFuncCallSomething(AFuncCallSomething node) {
        defaultIn(node);
	}
	
	@Override
	public void inAValueSomething(AValueSomething node) {
        defaultIn(node);
	}
	
	@Override
	public void inAIdSomething(AIdSomething node) {
        defaultIn(node);
	}
	
	@Override
	public void inATypeSomething(ATypeSomething node) {
        defaultIn(node);
	}
	
	@Override
	public void inAMaxSomething(AMaxSomething node) {
        defaultIn(node);
	}
	
	@Override
	public void inAMinSomething(AMinSomething node) {
        defaultIn(node);
	}
	
	@Override
	public void inAParSomething(AParSomething node) {
        defaultIn(node);
	}
	
	@Override
	public void inACommaV(ACommaV node) {
        defaultIn(node);
	}
	*/

	/*
	@Override
	public void inACompComparisonOr(ACompComparisonOr node) {
        defaultIn(node);
	}
	
	@Override
	public void inAOrComparisonOr(AOrComparisonOr node) {
        defaultIn(node);
	}
	
	@Override
	public void inANotCompComparisonAnd(ANotCompComparisonAnd node) {
        defaultIn(node);
	}
	
	@Override
	public void inAAndComparisonAnd(AAndComparisonAnd node) {
        defaultIn(node);
	}
	
	@Override
	public void inACompNotComp(ACompNotComp node) {
        defaultIn(node);
	}
	
	@Override
	public void inANotNotComp(ANotNotComp node) {
        defaultIn(node);
	}
	

	@Override
	public void inATrueComp(ATrueComp node) {
        defaultIn(node);
	}
	
	@Override
	public void inAFalseComp(AFalseComp node) {
        defaultIn(node);
	}
	
	@Override
	public void inALessComp(ALessComp node) {
        defaultIn(node);
	}
	
	@Override
	public void inAGreatComp(AGreatComp node) {
        defaultIn(node);
	}
	
	@Override
	public void inAGeqComp(AGeqComp node) {
        defaultIn(node);
	}
	
	@Override
	public void inALeqComp(ALeqComp node) {
        defaultIn(node);
	}
	
	@Override
	public void inANeqComp(ANeqComp node) {
        defaultIn(node);
	}
	
	@Override
	public void inAEqComp(AEqComp node) {
        defaultIn(node);
	}
	*/

	/*
	@Override
	public void inAFunctionCall(AFunctionCall node) {
        defaultIn(node);
	}
	
	@Override
	public void inAArglist(AArglist node) {
        defaultIn(node);
	}
	
	@Override
	public void inACommaExp(ACommaExp node) {
        defaultIn(node);
	}
	
	@Override
	public void inAMethodValue(AMethodValue node) {
        defaultIn(node);
	}
	
	@Override
	public void inANumValue(ANumValue node) {
        defaultIn(node);
	}
	
	@Override
	public void inAStringValue(AStringValue node) {
        defaultIn(node);
	}

	@Override
	public void inANoneValue(ANoneValue node) {
        defaultIn(node);
	}
	
	@Override
	public void inANum(ANum node) {
        defaultIn(node);
	}
	
	@Override
	public void inAIdent(AIdent node) {
        defaultIn(node);
	}
	*/

}