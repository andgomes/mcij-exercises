package org.fsf.main;

import org.fsf.grammar.*;
import org.fsf.interpretation.IntAndTable;
import org.fsf.interpretation.Table;

public class Main {
	
	public static void main(String[] args) {
		
		// a := 5 + 3; b := (print(a, a - 1), 10 * a); print(b);
		Stm prog1 = 
				new CompoundStm(
					new AssignStm(
						"a",
						new OpExp(
							new NumExp(5),
							OpExp.PLUS,
							new NumExp(3)
						)
					),
					new CompoundStm(
						new AssignStm(
							"b",
							new EseqExp(
								new PrintStm(
									new PairExpList(
										new IdExp("a"),
										new LastExpList(
											new OpExp(
												new IdExp("a"),
												OpExp.MINUS,
												new NumExp(1)
											)
										)
									)
								),
								new OpExp(
									new NumExp(10),
									OpExp.TIMES,
									new IdExp("a")
								)
							)
						),
						new PrintStm(
							new LastExpList(new IdExp("b"))
						)
					)
				);
		
		// to test nested print statements
		// a := 5 + 3; b := (print(a, a - 1), 10 * a); print((print(a, 200, b), 100));
		Stm prog2 = 
				new CompoundStm(
					new AssignStm(
						"a",
						new OpExp(
							new NumExp(5),
							OpExp.PLUS,
							new NumExp(3)
						)
					),
					new CompoundStm(
						new AssignStm(
							"b",
							new EseqExp(
								new PrintStm(
									new PairExpList(
										new IdExp("a"),
										new LastExpList(
											new OpExp(
												new IdExp("a"),
												OpExp.MINUS,
												new NumExp(1)
											)
										)
									)
								),
								new OpExp(
									new NumExp(10),
									OpExp.TIMES,
									new IdExp("a")
								)
							)
						),
						new PrintStm(
							new LastExpList(
								new EseqExp(
									new PrintStm(
										new PairExpList(
											new IdExp("a"),
											new PairExpList(
												new NumExp(200),
												new LastExpList(new IdExp("b"))
											)
										)
									),
									new NumExp(100)
								)
							)
						)
					)
				);
		
		int result = maxargs(prog1);
		
		System.out.println("Maximum number of arguments in prog1: " +
				result);
		
		result = maxargs(prog2);
		
		System.out.println("Maximum number of arguments in prog2: " +
				result);
		
		System.out.println("Interpreting prog 1...");
		
		Table initialTable = new Table("&", -1, null);
		
		Table varTable = interpStm(prog2, initialTable);
		
		System.out.println("Symbols table:");
		
		Table tempTable = varTable;
		
		while (tempTable != null) {
			
			System.out.println(
					tempTable.id + ": " + tempTable.value
				);
			
			tempTable = tempTable.tail;
			
		}
		
	}
	
	private static Table interpStm(Stm stm, Table table) {
		
		if (stm instanceof CompoundStm) {
			
			return interpStm(
					((CompoundStm) stm).stm2,
					interpStm(((CompoundStm) stm).stm1, table)
				);
			
		} else if (stm instanceof AssignStm) {
			
			IntAndTable intAndTable = interpExp(((AssignStm) stm).exp, table);
			
			Table newTable = new Table(
					((AssignStm) stm).id,
					intAndTable.i,
					null
				);
			
			if (table != intAndTable.table) {
				
				intAndTable.table.tail = table;
				newTable.tail = intAndTable.table;
			
			} else {
				newTable.tail = table;
			}
			
			return newTable;
			
		} else { // stm is instanceof PrintStm
			
			ExpList exps = ((PrintStm) stm).exps;
			
			while (exps instanceof PairExpList) {
				
				PairExpList pairExp = (PairExpList) exps;
				
				IntAndTable intAndTable = interpExp(pairExp.head, table);
				
				System.out.print(intAndTable.i + " ");
				
				if (table != intAndTable.table) {
					
					intAndTable.table.tail = table;
					table = intAndTable.table;
					
				}
				
				exps = pairExp.tail;
			
			}
			
			IntAndTable intAndTable = interpExp(((LastExpList) exps).head, table);
			
			System.out.println(intAndTable.i);
			
			if (table != intAndTable.table) {
				
				intAndTable.table.tail = table;
				table = intAndTable.table;
			
			}
			
			return table;
			
		}
		
	}
	
	private static IntAndTable interpExp(Exp exp, Table table) {
		
		if (exp instanceof IdExp) {
			
			return new IntAndTable(
					lookup(table, ((IdExp) exp).id),
					table
				);
			
		} else if (exp instanceof NumExp) {
			
			return new IntAndTable(
					((NumExp) exp).num,
					table
				);
			
		} else if (exp instanceof OpExp) {
			
			OpExp opExp = (OpExp) exp;
			Exp lExp = opExp.left;
			Exp rExp = opExp.right;
			
			IntAndTable leftInterp = interpExp(lExp, table);
			
			if (table != leftInterp.table) {
				
				leftInterp.table.tail = table;
				table = leftInterp.table;
				
			}
			
			IntAndTable rightInterp = interpExp(rExp, table);
			
			if (table != rightInterp.table) {
				
				rightInterp.table.tail = table;
				table = rightInterp.table;
				
			}
			
			int result;
			
			if (opExp.oper == OpExp.PLUS) {
				result = leftInterp.i + rightInterp.i;
			} else if (opExp.oper == OpExp.MINUS) {
				result = leftInterp.i - rightInterp.i;
			} else if (opExp.oper == OpExp.TIMES) {
				result = leftInterp.i * rightInterp.i;		
			} else { // opExp.oper == OpExp.DIV
				result = leftInterp.i / rightInterp.i;
			}
			
			return new IntAndTable(
					result,
					table
				);
			
		} else { // exp is instanceof EseqExp
			
			EseqExp eseqExp = (EseqExp) exp;
			
			Table smtTable = interpStm(eseqExp.stm, table);
			
			if (table != smtTable) {
				
				smtTable.tail = table;
				table = smtTable;
				
			}
			
			IntAndTable intAndTable = interpExp(eseqExp.exp, table);
			
			if (table != intAndTable.table) {
				
				intAndTable.table.tail = table;
				table = intAndTable.table;
				
			}
			
			return new IntAndTable(
					intAndTable.i,
					intAndTable.table
				);
			
		}
		
	}
	
	private static int lookup(Table table, String key) {
		
		Table tempTable = table;
		
		while (tempTable != null) {
			
			if (tempTable.id.equals(key)) {
				return tempTable.value;
			}
			
			tempTable = tempTable.tail;
			
		}
		
		throw new RuntimeException("variable with identifier " + key +
				" not declared or initialized");
		
	}
	
	private static int maxargs(Stm stm) {
		
		if (stm instanceof CompoundStm) {
			
			return Math.max(
					maxargs(((CompoundStm) stm).stm1),
					maxargs(((CompoundStm) stm).stm2)
				);
			
		} else if (stm instanceof AssignStm) {
			
			return maxargs(((AssignStm) stm).exp);
			
		} else { // stm is instance of PrintStm
			
			ExpList exps = ((PrintStm) stm).exps;
			
			int countArgs = 1;
			
			while (exps instanceof PairExpList) {
				
				countArgs++;
				exps = ((PairExpList) exps).tail;
			
			}
			
			return Math.max(
					countArgs,
					maxargs(((PrintStm) stm).exps)
				);
			
		}
		
	}
	
	private static int maxargs(Exp exp) {
		
		if (exp instanceof IdExp) {
			return 0;
		} else if (exp instanceof NumExp) {
			return 0;
		} else if (exp instanceof OpExp) {
			
			return Math.max(
					maxargs(((OpExp) exp).left),
					maxargs(((OpExp) exp).right)
				);
			
		} else { // exp is instance of EseqExp
			
			return Math.max(
					maxargs(((EseqExp) exp).stm),
					maxargs(((EseqExp) exp).exp)
				);
			
		}
		
	}
	
	private static int maxargs(ExpList exps) {
		
		if (exps instanceof PairExpList) {
			
			return Math.max(
					maxargs(((PairExpList) exps).head),
					maxargs(((PairExpList) exps).tail)
				);
			
		} else { // exps is instance of LastExpList
			return maxargs(((LastExpList) exps).head);
		}
		
	}
	
}
