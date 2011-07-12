/*******************************************************************************
 * Copyright (c) 2009-2011 CWI
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:

 *   * Jurgen J. Vinju - Jurgen.Vinju@cwi.nl - CWI
 *   * Arnold Lankamp - Arnold.Lankamp@cwi.nl
*******************************************************************************/
package org.rascalmpl.test.parser;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import org.eclipse.imp.pdb.facts.IConstructor;
import org.eclipse.imp.pdb.facts.IValue;
import org.eclipse.imp.pdb.facts.io.StandardTextReader;
import org.rascalmpl.parser.gtd.SGTDBF;
import org.rascalmpl.parser.gtd.stack.AbstractStackNode;
import org.rascalmpl.parser.gtd.stack.LiteralStackNode;
import org.rascalmpl.parser.gtd.stack.NonTerminalStackNode;
import org.rascalmpl.parser.uptr.NodeToUPTR;
import org.rascalmpl.values.ValueFactoryFactory;
import org.rascalmpl.values.uptr.Factory;

/*
S ::= AA
A ::= BB
B ::= bb | b
*/
public class Ambiguous4 extends SGTDBF implements IParserTest{
	private final static IConstructor SYMBOL_START_S = VF.constructor(Factory.Symbol_Sort, VF.string("S"));
	private final static IConstructor SYMBOL_A = VF.constructor(Factory.Symbol_Sort, VF.string("A"));
	private final static IConstructor SYMBOL_B = VF.constructor(Factory.Symbol_Sort, VF.string("B"));
	private final static IConstructor SYMBOL_b = VF.constructor(Factory.Symbol_Lit, VF.string("b"));
	private final static IConstructor SYMBOL_bb = VF.constructor(Factory.Symbol_Lit, VF.string("bb"));
	private final static IConstructor SYMBOL_char_b = VF.constructor(Factory.Symbol_CharClass, VF.list(VF.constructor(Factory.CharRange_Single, VF.integer(98))));
	
	private final static IConstructor PROD_S_AA = VF.constructor(Factory.Production_Default,  SYMBOL_START_S, VF.list(SYMBOL_A, SYMBOL_A), VF.set());
	private final static IConstructor PROD_A_BB = VF.constructor(Factory.Production_Default,  SYMBOL_A, VF.list(SYMBOL_B, SYMBOL_B), VF.set());
	private final static IConstructor PROD_B_b = VF.constructor(Factory.Production_Default,  SYMBOL_B, VF.list(SYMBOL_b), VF.set());
	private final static IConstructor PROD_B_bb = VF.constructor(Factory.Production_Default,  SYMBOL_B, VF.list(SYMBOL_bb), VF.set());
	private final static IConstructor PROD_b_b = VF.constructor(Factory.Production_Default,  SYMBOL_b, VF.list(SYMBOL_char_b), VF.set());
	private final static IConstructor PROD_bb_bb = VF.constructor(Factory.Production_Default,  SYMBOL_bb, VF.list(SYMBOL_char_b, SYMBOL_char_b), VF.set());
	
	private final static AbstractStackNode NONTERMINAL_START_S = new NonTerminalStackNode(AbstractStackNode.START_SYMBOL_ID, 0, "S");
	private final static AbstractStackNode NONTERMINAL_A0 = new NonTerminalStackNode(0, 0, "A");
	private final static AbstractStackNode NONTERMINAL_A1 = new NonTerminalStackNode(1, 1, "A");
	private final static AbstractStackNode NONTERMINAL_B2 = new NonTerminalStackNode(2, 0, "B");
	private final static AbstractStackNode NONTERMINAL_B3 = new NonTerminalStackNode(3, 1, "B");
	private final static AbstractStackNode LITERAL_b4 = new LiteralStackNode(4, 0, PROD_b_b, new char[]{'b'});
	private final static AbstractStackNode LITERAL_bb5 = new LiteralStackNode(5, 0, PROD_bb_bb, new char[]{'b','b'});
	
	public Ambiguous4(){
		super();
	}
	
	public void S(){
		expect(PROD_S_AA, NONTERMINAL_A0, NONTERMINAL_A1);
	}
	
	public void A(){
		expect(PROD_A_BB, NONTERMINAL_B2, NONTERMINAL_B3);
	}
	
	public void B(){
		expect(PROD_B_b, LITERAL_b4);
		
		expect(PROD_B_bb, LITERAL_bb5);
	}
	
	public IConstructor executeParser(){
		return parse(NONTERMINAL_START_S, null, "bbbbbb".toCharArray(), new NodeToUPTR());
	}
	
	public IValue getExpectedResult() throws IOException{
		String expectedInput = "amb({appl(prod(sort(\"S\"),[sort(\"A\"),sort(\"A\")],{}),[appl(prod(sort(\"A\"),[sort(\"B\"),sort(\"B\")],{}),[appl(prod(sort(\"B\"),[lit(\"bb\")],{}),[appl(prod(lit(\"bb\"),[\\char-class([single(98)]),\\char-class([single(98)])],{}),[char(98),char(98)])]),appl(prod(sort(\"B\"),[lit(\"bb\")],{}),[appl(prod(lit(\"bb\"),[\\char-class([single(98)]),\\char-class([single(98)])],{}),[char(98),char(98)])])]),appl(prod(sort(\"A\"),[sort(\"B\"),sort(\"B\")],{}),[appl(prod(sort(\"B\"),[lit(\"b\")],{}),[appl(prod(lit(\"b\"),[\\char-class([single(98)])],{}),[char(98)])]),appl(prod(sort(\"B\"),[lit(\"b\")],{}),[appl(prod(lit(\"b\"),[\\char-class([single(98)])],{}),[char(98)])])])]),appl(prod(sort(\"S\"),[sort(\"A\"),sort(\"A\")],{}),[amb({appl(prod(sort(\"A\"),[sort(\"B\"),sort(\"B\")],{}),[appl(prod(sort(\"B\"),[lit(\"b\")],{}),[appl(prod(lit(\"b\"),[\\char-class([single(98)])],{}),[char(98)])]),appl(prod(sort(\"B\"),[lit(\"bb\")],{}),[appl(prod(lit(\"bb\"),[\\char-class([single(98)]),\\char-class([single(98)])],{}),[char(98),char(98)])])]),appl(prod(sort(\"A\"),[sort(\"B\"),sort(\"B\")],{}),[appl(prod(sort(\"B\"),[lit(\"bb\")],{}),[appl(prod(lit(\"bb\"),[\\char-class([single(98)]),\\char-class([single(98)])],{}),[char(98),char(98)])]),appl(prod(sort(\"B\"),[lit(\"b\")],{}),[appl(prod(lit(\"b\"),[\\char-class([single(98)])],{}),[char(98)])])])}),amb({appl(prod(sort(\"A\"),[sort(\"B\"),sort(\"B\")],{}),[appl(prod(sort(\"B\"),[lit(\"b\")],{}),[appl(prod(lit(\"b\"),[\\char-class([single(98)])],{}),[char(98)])]),appl(prod(sort(\"B\"),[lit(\"bb\")],{}),[appl(prod(lit(\"bb\"),[\\char-class([single(98)]),\\char-class([single(98)])],{}),[char(98),char(98)])])]),appl(prod(sort(\"A\"),[sort(\"B\"),sort(\"B\")],{}),[appl(prod(sort(\"B\"),[lit(\"bb\")],{}),[appl(prod(lit(\"bb\"),[\\char-class([single(98)]),\\char-class([single(98)])],{}),[char(98),char(98)])]),appl(prod(sort(\"B\"),[lit(\"b\")],{}),[appl(prod(lit(\"b\"),[\\char-class([single(98)])],{}),[char(98)])])])})]),appl(prod(sort(\"S\"),[sort(\"A\"),sort(\"A\")],{}),[appl(prod(sort(\"A\"),[sort(\"B\"),sort(\"B\")],{}),[appl(prod(sort(\"B\"),[lit(\"b\")],{}),[appl(prod(lit(\"b\"),[\\char-class([single(98)])],{}),[char(98)])]),appl(prod(sort(\"B\"),[lit(\"b\")],{}),[appl(prod(lit(\"b\"),[\\char-class([single(98)])],{}),[char(98)])])]),appl(prod(sort(\"A\"),[sort(\"B\"),sort(\"B\")],{}),[appl(prod(sort(\"B\"),[lit(\"bb\")],{}),[appl(prod(lit(\"bb\"),[\\char-class([single(98)]),\\char-class([single(98)])],{}),[char(98),char(98)])]),appl(prod(sort(\"B\"),[lit(\"bb\")],{}),[appl(prod(lit(\"bb\"),[\\char-class([single(98)]),\\char-class([single(98)])],{}),[char(98),char(98)])])])])})";
		return new StandardTextReader().read(ValueFactoryFactory.getValueFactory(), Factory.uptr, Factory.Tree, new ByteArrayInputStream(expectedInput.getBytes()));
	}

	public static void main(String[] args){
		Ambiguous4 a4 = new Ambiguous4();
		IConstructor result = a4.parse(NONTERMINAL_START_S, null, "bbbbbb".toCharArray(), new NodeToUPTR());
		System.out.println(result);
		
		System.out.println("[S(A(B(bb),B(bb)),A(B(b),B(b))),S(A(B(b),B(b)),A(B(bb),B(bb))),S([A(B(b),B(bb)),A(B(bb),B(b))],[A(B(b),B(bb)),A(B(bb),B(b))])] <- good");
	}
}
