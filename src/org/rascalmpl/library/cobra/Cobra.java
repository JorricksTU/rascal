/*******************************************************************************
 * Copyright (c) 2009-2012 CWI
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:

 *   * Wietse Venema - wietsevenema@gmail.com - CWI
 *   * Paul Klint - Paul.Klint@cwi.nl - CWI - added type parameters
 *******************************************************************************/
package org.rascalmpl.library.cobra;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.imp.pdb.facts.IBool;
import org.eclipse.imp.pdb.facts.IInteger;
import org.eclipse.imp.pdb.facts.IValue;
import org.eclipse.imp.pdb.facts.IValueFactory;
import org.eclipse.imp.pdb.facts.type.Type;
import org.rascalmpl.interpreter.IEvaluatorContext;
import org.rascalmpl.interpreter.result.AbstractFunction;
import org.rascalmpl.interpreter.result.OverloadedFunction;
import org.rascalmpl.interpreter.types.ReifiedType;
import org.rascalmpl.interpreter.utils.RuntimeExceptionFactory;
import org.rascalmpl.library.cobra.util.NullOutputStream;

public class Cobra {

	public static final String TRIES = "tries";
	public static final String MAXDEPTH = "maxDepth";

	public static Type reifyType(IValue type) {
		Type reified = type.getType();
		if (!(reified instanceof ReifiedType)) {
			throw RuntimeExceptionFactory.illegalArgument(type, null, null,
					"A reified type is required instead of " + reified);
		}
		return reified.getTypeParameters().getFieldType(0);
	}


	final IValueFactory vf;

	private final QuickCheck quickcheck;

	public Cobra(IValueFactory vf) {
		this.vf = vf;
		this.quickcheck = QuickCheck.getInstance();
	}

	public static int readIntTag(AbstractFunction test, String key, int defaultVal) {
		if (test.hasTag(key)) {
			int result = Integer.parseInt(test.getTag(key));
			if (result < 1) {
				throw new IllegalArgumentException(key + " smaller than 1");
			}
			return result;
		} else {
			return defaultVal;
		}
	}

	public IValue _quickcheck(IValue function, IBool verbose, IBool maxVerbose, IEvaluatorContext eval) {
		return _quickcheck(function, verbose, maxVerbose, null, null, eval);
	}

	public IValue _quickcheck(IValue function, IBool verbose, IBool maxVerbose, IInteger maxDepth, IInteger tries,
			IEvaluatorContext eval) {

		PrintWriter out = (verbose.getValue()) ? eval.getStdOut()
				: new PrintWriter(new NullOutputStream());

		ArrayList<AbstractFunction> functions = extractFunctions(function, eval);

		if (!isReturnTypeBool(functions)) {
			throw RuntimeExceptionFactory.illegalArgument(function,
					eval.getCurrentAST(), null, "Return type should be bool.");
		}
		
		
		try {
			boolean result = true;
			for (AbstractFunction f : functions) {
				
				int _maxDepth, _tries;

				_maxDepth = getAnnotation(eval, f, Cobra.MAXDEPTH, maxDepth, 5);
				_tries = getAnnotation(eval, f, Cobra.TRIES, tries, 100);

				result = result
 && quickcheck.quickcheck(f, _maxDepth, _tries, maxVerbose.getValue(), out);

			}
			return eval.getValueFactory().bool(result);

		} catch (IllegalArgumentException e) {
			throw RuntimeExceptionFactory.illegalArgument(maxDepth,
					eval.getCurrentAST(), null,
					e.getMessage());
		} finally {
			out.flush();
		}

	}

	private int getAnnotation(IEvaluatorContext eval, AbstractFunction f, String tag, IInteger override, int defaultVal) {
		try {
			return (override == null) ? readIntTag(f, tag, defaultVal) : override.intValue();
		} catch (IllegalArgumentException e) {
			throw RuntimeExceptionFactory.illegalArgument(vf.string(tag), eval.getCurrentAST(), null,
					"Annotation: " + e.getMessage());
		}

	}

	public IValue arbitrary(IValue type, IInteger depthLimit,
			IEvaluatorContext eval, Map<Type, Type> typeParameters) {
		try {
			IValue result = quickcheck.arbitrary(Cobra.reifyType(type),
					depthLimit.intValue(), eval.getCurrentEnvt().getRoot(),
					eval.getValueFactory(), typeParameters);
			return result;
		} catch (IllegalArgumentException e) {
			throw RuntimeExceptionFactory.illegalArgument(depthLimit,
					eval.getCurrentAST(), null,
					"No construction possible at this depth or less.");
		}
	}

	private ArrayList<AbstractFunction> extractFunctions(IValue function,
			IEvaluatorContext eval) {
		ArrayList<AbstractFunction> functions = new ArrayList<AbstractFunction>();
		if (function instanceof AbstractFunction) {
			functions.add((AbstractFunction) function);
		} else if (function instanceof OverloadedFunction) {
			for (AbstractFunction f : ((OverloadedFunction) function)
					.getFunctions()) {
				functions.add(f);
			}
		} else {
			throw RuntimeExceptionFactory.illegalArgument(function, eval.getCurrentAST(), null,
					"Argument should be function.");
		}
		return functions;
	}

	public IValue getGenerator(IValue t, IEvaluatorContext eval) {
		return quickcheck.getGenerator(t, eval);
	}

	private boolean isReturnTypeBool(List<AbstractFunction> functions) {
		for(AbstractFunction f: functions){
			if (!f.getReturnType().isBoolType()) {
				return false;
			}
		}
		return true;
	}

	public void resetGenerator(IValue type) {
		quickcheck.resetGenerator(type);
	}

	public void setGenerator(IValue f) {
		quickcheck.setGenerator(f);
	}



}