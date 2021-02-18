package org.fsf.grammar;

public class OpExp extends Exp {

	public Exp left;
	public Exp right;
	public int oper;
	
	final public static int PLUS = 1;
	final public static int MINUS = 2;
	final public static int TIMES = 3;
	final public static int DIV = 4;
	
	public OpExp(Exp left, int oper, Exp right) {
		
		this.left = left;
		this.right = right;
		this.oper = oper;
		
	}
	
}
