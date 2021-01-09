import minipython.analysis.*;
import minipython.node.*;
import java.util.*;

public class Visitor extends DepthFirstAdapter {

	private Hashtable symtable;	

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

	
	@Override
	public void inAFunction(AFunction node) {

        String fName = node.getId().toString();
	 	int line = ((TId) node.getId()).getLine();
	 	if (symtable.containsKey(fName)) {
	 		System.out.println("Line " + line + ": " +" Function " + fName +" is already defined");
	 	}else {
	 		symtable.put(fName, node);
		}
	}
	
	@Override
	public void inAArgument(AArgument node) {
		String fName = node.getId().toString();
		if(!symtable.contains(fname)) symtable.put(fname,node);
	}
	
	@Override
	public void inACommaAssign(ACommaAssign node) {
		String fName = node.getId().toString();
		if(!symtable.contains(fname)) symtable.put(fname,node);
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
		String fName = node.getId().toString();
		// 2 symbol tables???
		if(!symtable.contains(fname)) symtable.put(fname,node);
		else if (!symtable.get(fname) instanceof ACommaAssign ) symtable.put(fname,node);  //!!!!!!!!!!!!!!!
	}
	
	@Override
	public void inAAssignminStatement(AAssignminStatement node) {
        defaultIn(node);
	}
	
	@Override
	public void inAAssigndivStatement(AAssigndivStatement node) {
        defaultIn(node);
	}
	
	@Override
	public void inAListStatement(AListStatement node) {
        defaultIn(node);
	}
	
	@Override
	public void inAAssertStatement(AAssertStatement node) {
        defaultIn(node);
	}
	
	@Override
	public void inAFuncStatement(AFuncStatement node) {
        defaultIn(node);
	}
	
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

}
