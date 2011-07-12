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
S ::= Ab
A ::= aa
*/
public class Simple1 extends SGTDBF implements IParserTest{
	private final static IConstructor SYMBOL_START_S = VF.constructor(Factory.Symbol_Sort, VF.string("S"));
	private final static IConstructor SYMBOL_A = VF.constructor(Factory.Symbol_Sort, VF.string("A"));
	private final static IConstructor SYMBOL_aa = VF.constructor(Factory.Symbol_Lit, VF.string("aa"));
	private final static IConstructor SYMBOL_b = VF.constructor(Factory.Symbol_Lit, VF.string("b"));
	private final static IConstructor SYMBOL_char_a = VF.constructor(Factory.Symbol_CharClass, VF.list(VF.constructor(Factory.CharRange_Single, VF.integer(97))));
	private final static IConstructor SYMBOL_char_b = VF.constructor(Factory.Symbol_CharClass, VF.list(VF.constructor(Factory.CharRange_Single, VF.integer(98))));
	
	private final static IConstructor PROD_S_Ab = VF.constructor(Factory.Production_Default, SYMBOL_START_S, VF.list(SYMBOL_A, SYMBOL_b),  VF.set());
	private final static IConstructor PROD_A_aa = VF.constructor(Factory.Production_Default,  SYMBOL_A,  VF.list(SYMBOL_aa),VF.set());
	private final static IConstructor PROD_aa_a = VF.constructor(Factory.Production_Default,  SYMBOL_aa,VF.list(SYMBOL_char_a, SYMBOL_char_a),  VF.set());
	private final static IConstructor PROD_b_b = VF.constructor(Factory.Production_Default,  SYMBOL_b,VF.list(SYMBOL_char_b),  VF.set());
	
	private final static AbstractStackNode NONTERMINAL_START_S = new NonTerminalStackNode(AbstractStackNode.START_SYMBOL_ID, 0, "S");
	private final static AbstractStackNode NONTERMINAL_A0 = new NonTerminalStackNode(0, 0, "A");
	private final static AbstractStackNode LITERAL_aa1 = new LiteralStackNode(1, 0, PROD_aa_a, new char[]{'a','a'});
	private final static AbstractStackNode LITERAL_b2 = new LiteralStackNode(2, 1, PROD_b_b, new char[]{'b'});
	
	public Simple1(){
		super();
	}
	
	public void S(){
		expect(PROD_S_Ab, NONTERMINAL_A0, LITERAL_b2);
	}
	
	public void A(){
		expect(PROD_A_aa, LITERAL_aa1);
	}
	
	public IConstructor executeParser(){
		return parse(NONTERMINAL_START_S, null, "aab".toCharArray(), new NodeToUPTR());
	}
	
	public IValue getExpectedResult() throws IOException{
		String expectedInput = "appl(prod(sort(\"S\"),[sort(\"A\"),lit(\"b\")],{}),[appl(prod(sort(\"A\"),[lit(\"aa\")],{}),[appl(prod(lit(\"aa\"),[\\char-class([single(97)]),\\char-class([single(97)])],{}),[char(97),char(97)])]),appl(prod(lit(\"b\"),[\\char-class([single(98)])],{}),[char(98)])])";
		return new StandardTextReader().read(ValueFactoryFactory.getValueFactory(), Factory.uptr, Factory.Tree, new ByteArrayInputStream(expectedInput.getBytes()));
	}
	
	public static void main(String[] args){
		Simple1 s1 = new Simple1();
		IConstructor result = s1.parse(NONTERMINAL_START_S, null, "aab".toCharArray(), new NodeToUPTR());
		System.out.println(result);
		
		System.out.println("S(A(aa),b) <- good");
	}
}
