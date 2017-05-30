package visitor;


import ast.And;
import ast.ArrayAssign;
import ast.ArrayLength;
import ast.ArrayLookup;
import ast.Assign;
import ast.Block;
import ast.BooleanType;
import ast.Call;
import ast.ClassDeclExtends;
import ast.ClassDeclSimple;
import ast.False;
import ast.Formal;
import ast.Identifier;
import ast.IdentifierExp;
import ast.IdentifierType;
import ast.If;
import ast.IntArrayType;
import ast.IntegerLiteral;
import ast.IntegerType;
import ast.LessThan;
import ast.MainClass;
import ast.MethodDecl;
import ast.Minus;
import ast.NewArray;
import ast.NewObject;
import ast.Not;
import ast.Plus;
import ast.Print;
import ast.Program;
import ast.This;
import ast.Times;
import ast.True;
import ast.Type;
import ast.VarDecl;
import ast.While;
import symboltable.*;
import symboltable.Class;

public class TypeCheckVisitor implements TypeVisitor {
	
private SymbolTable symbolTable;
	
	private Class currClass;
	private Method currMethod;

	public TypeCheckVisitor(SymbolTable st) {
		symbolTable = st;
	}

	// MainClass m;
	// ClassDeclList cl;
	public Type visit(Program n) {
		n.m.accept(this);
		for (int i = 0; i < n.cl.size(); i++) {
			n.cl.elementAt(i).accept(this);
		}
		return null;
	}

	// Identifier i1,i2;
	// Statement s;
	public Type visit(MainClass n) {
		currClass = symbolTable.getClass(n.i1.s);
		
		n.i1.accept(this);
		n.i2.accept(this);
		n.s.accept(this);
		
		currClass = null;
		
		return null;
	}

	// Identifier i;
	// VarDeclList vl;
	// MethodDeclList ml;
	public Type visit(ClassDeclSimple n) {
		currClass = symbolTable.getClass(n.i.s);
		
		n.i.accept(this);
		for (int i = 0; i < n.vl.size(); i++) {
			n.vl.elementAt(i).accept(this);
		}
		for (int i = 0; i < n.ml.size(); i++) {
			n.ml.elementAt(i).accept(this);
		}
		
		currClass = null;
		
		return null;
	}

	// Identifier i;
	// Identifier j;
	// VarDeclList vl;
	// MethodDeclList ml;
	public Type visit(ClassDeclExtends n) {
		currClass = symbolTable.getClass(n.i.s);
		
		n.i.accept(this);
		n.j.accept(this);
		for (int i = 0; i < n.vl.size(); i++) {
			n.vl.elementAt(i).accept(this);
		}
		for (int i = 0; i < n.ml.size(); i++) {
			n.ml.elementAt(i).accept(this);
		}
		
		currClass = null;
		
		return null;
	}

	// Type t;
	// Identifier i;
	public Type visit(VarDecl n) {
		n.t.accept(this);
		n.i.accept(this);
		return n.t;
	}

	// Type t;
	// Identifier i;
	// FormalList fl;
	// VarDeclList vl;
	// StatementList sl;
	// Exp e;
	public Type visit(MethodDecl n) {
		currMethod = symbolTable.getMethod(n.i.s, currClass.getId());
		Type t = symbolTable.getMethodType(n.i.s, currClass.getId());
		
		Type tType = n.t.accept(this);
		//Type iType = n.i.accept(this);
		Type[] formalListType = new Type[n.fl.size()];
		for (int i = 0; i < n.fl.size(); i++) {
			formalListType[i] = n.fl.elementAt(i).accept(this);
		}
		Type[] varDeclListType = new Type[n.vl.size()];
		for (int i = 0; i < n.vl.size(); i++) {
			varDeclListType[i] = n.vl.elementAt(i).accept(this);
		}
		Type[] statementListType = new Type[n.sl.size()];
		for (int i = 0; i < n.sl.size(); i++) {
			statementListType[i] = n.sl.elementAt(i).accept(this);
		}
		Type expType = n.e.accept(this);
		if ((symbolTable.compareTypes(tType, expType)) == false ) {
			System.out.println("Em MethodDecl: " + n.i.s + " possui um retorno incompatível");
			n.accept(new PrettyPrintVisitor());
			System.exit(0);
	
		}
		currMethod = null;
		
		return t;
	}

	// Type t;
	// Identifier i;
	public Type visit(Formal n) {
		n.t.accept(this);
		n.i.accept(this);
		return null;
	}

	public Type visit(IntArrayType n) {
		return new IntArrayType();
	}

	public Type visit(BooleanType n) {
		return new BooleanType();
	}

	public Type visit(IntegerType n) {
		return new IntegerType();
	}

	// String s;
	public Type visit(IdentifierType n) {
		return n;
	}

	// StatementList sl;
	public Type visit(Block n) {
		for (int i = 0; i < n.sl.size(); i++) {
			n.sl.elementAt(i).accept(this);
		}
		return null;
	}

	// Exp e;
	// Statement s1,s2;
	public Type visit(If n) {
		Type expType = n.e.accept(this);
		
		if (expType == null) {
			return null;
		}
		if (!(expType instanceof BooleanType)) {
			System.out.println("Em If: " + n.e.toString() + " nao eh do tipo Boolean");
			n.accept(new PrettyPrintVisitor());
			System.exit(0);
		}
		
		n.s1.accept(this);
		n.s2.accept(this);
		return null;
	}

	// Exp e;
	// Statement s;
	public Type visit(While n) {
		Type expType = n.e.accept(this);

		if (expType == null) {
			return null;
		}
		if (!(expType instanceof BooleanType)) {
			System.out.println("Em While: " + n.e.toString() + " nao eh do tipo Boolean");
			n.accept(new PrettyPrintVisitor());
			System.exit(0);
		}
		
		n.s.accept(this);
		
		return null;
	}

	// Exp e;
	public Type visit(Print n) {
		Type expType = n.e.accept(this);
		if (expType == null) {
			return null;
		}
		if (!(expType instanceof Type)) {
			System.out.println("Em Print: " + n.e.toString() + " nao eh de um tipo");
			n.accept(new PrettyPrintVisitor());
			System.exit(0);
		}
		return null;
	}

	// Identifier i;
	// Exp e;
	public Type visit(Assign n) {
		Type idType = n.i.accept(this);
		Type expType = n.e.accept(this);
		if (idType == null) {
			return null;
		}
		if (expType == null) {
			return null;
		}
		
		if (!symbolTable.compareTypes(idType, expType)) {
			System.out.println("Em Assign: " + n.i.toString() + " e " + n.e.toString() + " nao sao do mesmo tipo");
			n.accept(new PrettyPrintVisitor());
			System.exit(0);
		}
		return null;
	}

	// Identifier i;
	// Exp e1,e2;
	public Type visit(ArrayAssign n) {
		Type idType = n.i.accept(this);
		Type exp1Type = n.e1.accept(this);
		Type exp2Type = n.e2.accept(this);
		if (idType == null) {
			return null;
		}
		if (exp1Type == null) {
			return null;
		}
		if (exp2Type == null) {
			return null;
		}
		if (!(exp1Type instanceof IntegerType)) {
			System.out.println("Em ArrayAssign: " + n.e1.toString() + " nao eh do tipo Int");
			n.accept(new PrettyPrintVisitor());
			System.exit(0);
		}
		return null;
	}

	// Exp e1,e2;
	public Type visit(And n) {
		Type exp1Type = n.e1.accept(this);
		Type exp2Type = n.e2.accept(this);
		if (exp1Type == null) {
			return null;
		}
		if (exp2Type == null) {
			return null;
		}
		if (!(exp1Type instanceof BooleanType)) {
			System.out.println("Em And: " + exp1Type + " nao eh do tipo Boolean");
			n.accept(new PrettyPrintVisitor());
			System.exit(0);
		}
		if (!(exp2Type instanceof BooleanType)) {
			System.out.println("Em And: " + exp2Type + " nao eh do tipo Boolean");
			n.accept(new PrettyPrintVisitor());
			System.exit(0);
		}
		return new BooleanType();
	}

	// Exp e1,e2;
	public Type visit(LessThan n) {	
		Type exp1Type = n.e1.accept(this);
		Type exp2Type = n.e2.accept(this);
		if (exp1Type == null) {
			return null;
		}
		if (exp2Type == null) {
			return null;
		}
		if (!(exp1Type instanceof IntegerType)) {
			System.out.println("Em LessThan: " + exp1Type + " nao eh do tipo Int");
			n.accept(new PrettyPrintVisitor());
			System.exit(0);
		}
		if (!(exp2Type instanceof IntegerType)) {
			System.out.println("Em LessThan: " + exp2Type + " nao eh do tipo Int");
			n.accept(new PrettyPrintVisitor());
			System.exit(0);
		}
		return new BooleanType();
	}

	// Exp e1,e2;
	public Type visit(Plus n) {
		Type exp1Type = n.e1.accept(this);
		Type exp2Type = n.e2.accept(this);
		if (exp1Type == null) {
			return null;
		}
		if (exp2Type == null) {
			return null;
		}
		if (!(exp1Type instanceof IntegerType)) {
			System.out.println("Em Plus: " + exp1Type + " nao eh do tipo Int");
			n.accept(new PrettyPrintVisitor());
			System.exit(0);
		}
		if (!(exp2Type instanceof IntegerType)) {
			System.out.println("Em Plus: " + exp2Type + " nao eh do tipo Int");
			n.accept(new PrettyPrintVisitor());
			System.exit(0);
		}
		return new IntegerType();
	}

	// Exp e1,e2;
	public Type visit(Minus n) {
		Type exp1Type = n.e1.accept(this);
		Type exp2Type = n.e2.accept(this);
		if (exp1Type == null) {
			return null;
		}
		if (exp2Type == null) {
			return null;
		}
		if (!(exp1Type instanceof IntegerType)) {
			System.out.println("Em Minus: " + exp1Type + " nao eh do tipo Int");
			n.accept(new PrettyPrintVisitor());
			System.exit(0);
		}
		if (!(exp2Type instanceof IntegerType)) {
			System.out.println("Em Minus: " + exp2Type + " nao eh do tipo Int");
			n.accept(new PrettyPrintVisitor());
			System.exit(0);
		}
		return new IntegerType();
	}

	// Exp e1,e2;
	public Type visit(Times n) {
		Type exp1Type = n.e1.accept(this);
		Type exp2Type = n.e2.accept(this);
		if (exp1Type == null) {
			return null;
		}
		if (exp2Type == null) {
			return null;
		}
		if (!(exp1Type instanceof IntegerType)) {
			System.out.println("Em Times: " + exp1Type + " nao eh do tipo Int");
			n.accept(new PrettyPrintVisitor());
			System.exit(0);
		}
		if (!(exp2Type instanceof IntegerType)) {
			System.out.println("Em Times: " + exp2Type + " nao eh do tipo Int");
			n.accept(new PrettyPrintVisitor());
			System.exit(0);
		}
		return new IntegerType();
	}

	// Exp e1,e2;
	public Type visit(ArrayLookup n) {
		Type exp1Type = n.e1.accept(this);
		Type exp2Type = n.e2.accept(this);
		if (exp1Type == null) {
			return null;
		}
		if (exp2Type == null) {
			return null;
		}
		if (!(exp1Type instanceof IntArrayType)) {
			System.out.println("Em ArrayLookup: " + exp1Type + " nao eh do tipo Int []");
			n.accept(new PrettyPrintVisitor());
			System.exit(0);
		}
		if (!(exp2Type instanceof IntegerType)) {
			System.out.println("Em ArrayLookup: " + exp2Type + " nao eh do tipo Int");
			n.accept(new PrettyPrintVisitor());
			System.exit(0);
		}
		return new IntegerType();
	}

	// Exp e;
	public Type visit(ArrayLength n) {
		Type expType = n.e.accept(this);
		if (expType == null) {
			return null;
		}
		if (!(expType instanceof IntArrayType)) {
			System.out.println("Em ArrayLength: " + expType + " nao eh do tipo Int []");
			n.accept(new PrettyPrintVisitor());
			System.exit(0);
		}
		return new IntegerType();
	}

	// Exp e;
	// Identifier i;
	// ExpList el;
	public Type visit(Call n) {
		Class classAux = currClass;
		Method methodAux = currMethod;
		n.e.accept(this);
		Type methodType = symbolTable.getMethodType(n.i.s, currClass.getId());
		Type expType = null;
		for (int i = 0; i < n.el.size(); i++) {
			if(n.el.elementAt(i) != null){ 
				expType = n.el.elementAt(i).accept(this);
			}
		}
		currClass= classAux;
		currMethod = methodAux;
		if(methodType != null) return methodType;
		return expType;
	}

	// int i;
	public Type visit(IntegerLiteral n) {
		return new IntegerType();
	}

	public Type visit(True n) {
		return new BooleanType();
	}

	public Type visit(False n) {
		return new BooleanType();
	}

	// String s;
	public Type visit(IdentifierExp n) {
		return symbolTable.getVarType(currMethod, currClass, n.s);
	}

	public Type visit(This n) {
		return currClass.type();
	}

	// Exp e;
	public Type visit(NewArray n) {
		Type expType = n.e.accept(this);
		if (expType == null) {
			return null;
		}
		if (!(expType instanceof IntegerType)) {
			System.out.println("Em NewArray: " + expType + " nao eh do tipo Int");
			n.accept(new PrettyPrintVisitor());
			System.exit(0);
		}
		return new IntArrayType();
	}

	// Identifier i;
	public Type visit(NewObject n) {
		Type idType = n.i.accept(this);
		if (idType == null) {
			return null;
		}
		if (!(symbolTable.containsClass(n.i.toString()))) {
			System.out.println("Em NewObject: " + n.i.toString() + " nao existe");
			n.accept(new PrettyPrintVisitor());
			System.exit(0);
		}
		return new IdentifierType(n.i.toString());
	}

	// Exp e;
	public Type visit(Not n) {
		Type expType = n.e.accept(this);
		if (expType == null) {
			return null;
		}
		if (!(expType instanceof BooleanType)) {
			System.out.println("Em Not: " + expType + " nao eh do tipo Boolean");
			n.accept(new PrettyPrintVisitor());
			System.exit(0);
		}
		return new BooleanType();
	}

	// String s;
	public Type visit(Identifier n) {
		return symbolTable.getVarType(currMethod, currClass, n.s);
	}
	
}