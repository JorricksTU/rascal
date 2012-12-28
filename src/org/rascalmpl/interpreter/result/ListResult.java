/*******************************************************************************
 * Copyright (c) 2009-2011 CWI
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:

 *   * Jurgen J. Vinju - Jurgen.Vinju@cwi.nl - CWI
 *   * Tijs van der Storm - Tijs.van.der.Storm@cwi.nl
 *   * Paul Klint - Paul.Klint@cwi.nl - CWI
 *   * Mark Hills - Mark.Hills@cwi.nl (CWI)
 *   * Arnold Lankamp - Arnold.Lankamp@cwi.nl
*******************************************************************************/
package org.rascalmpl.interpreter.result;

import static org.rascalmpl.interpreter.result.ResultFactory.bool;
import static org.rascalmpl.interpreter.result.ResultFactory.makeResult;

import org.eclipse.imp.pdb.facts.IBool;
import org.eclipse.imp.pdb.facts.IInteger;
import org.eclipse.imp.pdb.facts.IList;
import org.eclipse.imp.pdb.facts.IListWriter;
import org.eclipse.imp.pdb.facts.ITuple;
import org.eclipse.imp.pdb.facts.IValue;
import org.eclipse.imp.pdb.facts.impl.fast.ListWriter;
import org.eclipse.imp.pdb.facts.type.Type;
import org.eclipse.imp.pdb.facts.type.TypeFactory;
import org.rascalmpl.interpreter.IEvaluatorContext;
import org.rascalmpl.interpreter.staticErrors.UnexpectedTypeError;
import org.rascalmpl.interpreter.staticErrors.UnsupportedSubscriptArityError;
import org.rascalmpl.interpreter.utils.RuntimeExceptionFactory;

public class ListResult extends ListOrRelationResult<IList> {
	
	public ListResult(Type type, IList list, IEvaluatorContext ctx) {
		super(type, list, ctx);
	}
		
	@Override
	public <U extends IValue, V extends IValue> Result<U> add(Result<V> result) {
		return result.addList(this);
	}
	
	@Override 
	public <U extends IValue, V extends IValue> Result<U> subtract(Result<V> result) {
		return result.subtractList(this);
	}
	
	@Override
	public <U extends IValue, V extends IValue> Result<U> join(Result<V> that) {
		return that.joinList(this);
	}
	
	@Override
	public <U extends IValue, V extends IValue> Result<U> intersect(Result<V> result) {
		return result.intersectList(this);
	}

	@Override
	public <U extends IValue, V extends IValue> Result<U> multiply(Result<V> result) {
		return result.multiplyList(this);
	}
	
	@Override
	public <V extends IValue> Result<IBool> in(Result<V> result) {
		return result.inList(this);
	}	
	
	@Override
	public <V extends IValue> Result<IBool> notIn(Result<V> result) {
		return result.notInList(this);
	}	
	
	@Override
	public <V extends IValue> Result<IBool> equals(Result<V> that) {
		return that.equalToList(this);
	}

	@Override
	public <V extends IValue> Result<IBool> nonEquals(Result<V> that) {
		return that.nonEqualToList(this);
	}
	
	@Override
	public <V extends IValue> Result<IBool> lessThan(Result<V> that) {
		return that.lessThanList(this);
	}
	
	@Override
	public <V extends IValue> LessThanOrEqualResult lessThanOrEqual(Result<V> that) {
		return that.lessThanOrEqualList(this);
	}

	@Override
	public <V extends IValue> Result<IBool> greaterThan(Result<V> that) {
		return that.greaterThanList(this);
	}
	
	@Override
	public <V extends IValue> Result<IBool> greaterThanOrEqual(Result<V> that) {
		return that.greaterThanOrEqualList(this);
	}


	@Override
	@SuppressWarnings("unchecked")
	public <U extends IValue, V extends IValue> Result<U> subscript(Result<?>[] subscripts) {
		if (subscripts.length != 1) {
			throw new UnsupportedSubscriptArityError(getType(), subscripts.length, ctx.getCurrentAST());
		}
		Result<IValue> key = (Result<IValue>) subscripts[0];
		if (!key.getType().isIntegerType()) {
			throw new UnexpectedTypeError(TypeFactory.getInstance().integerType(), key.getType(), ctx.getCurrentAST());
		}
		if (getValue().length() == 0) {
			throw RuntimeExceptionFactory.emptyList(ctx.getCurrentAST(), ctx.getStackTrace());
		}
		IInteger index = ((IInteger)key.getValue());
		if ( (index.intValue() >= getValue().length()) || (index.intValue() < 0) ) {
			throw RuntimeExceptionFactory.indexOutOfBounds(index, ctx.getCurrentAST(), ctx.getStackTrace());
		}
		return makeResult(getType().getElementType(), getValue().get(index.intValue()), ctx);
	}
	
	@Override
	public <U extends IValue, V extends IValue> Result<U> slice(Result<?> first, Result<?> second, Result<?> end) {
		return super.slice(first, second, end, getValue().length());
	}
	
	public Result<IValue> makeSlice(int first, int second, int end){
		ListWriter w = new ListWriter(getType().getElementType());
		int increment = second - first;
		if(first == end || increment == 0){
			// nothing to be done
		} else
		if(first <= end){
			for(int i = first; i >= 0 && i < end; i += increment){
				w.append(getValue().get(i));
			}
		} else {
			for(int j = first; j >= 0 && j > end && j < getValue().length(); j += increment){
				w.append(getValue().get(j));
			}
		}
		return makeResult(TypeFactory.getInstance().listType(getType().getElementType()), w.done(), ctx);
	}

	/////
	
	@Override
	protected <U extends IValue> Result<U> addList(ListResult l) {
		// Note the reverse concat
		return makeResult(getType().lub(l.getType()), l.getValue().concat(getValue()), ctx);
	}

	@Override
	protected <U extends IValue> Result<U> subtractList(ListResult l) {
		// Note the reversal of args
		IList list = l.getValue();
		for (IValue v: getValue()) {
			if (list.contains(v)) {
				list = list.delete(v);
			}
		}
		return makeResult(l.getType(), list, ctx);
	}

	@Override
	protected <U extends IValue> Result<U> multiplyList(ListResult that) {
		Type t1 = that.type.getElementType();
		Type t2 = type.getElementType();
		// Note: reverse
		Type type = getTypeFactory().listType(getTypeFactory().tupleType(t1, t2));
		IListWriter w = type.writer(getValueFactory());
		for (IValue v1 : that.getValue()) {
			for (IValue v2 : getValue()) {
				w.append(getValueFactory().tuple(v1, v2));	
			}
		}
		return makeResult(type, w.done(), ctx);	
	}
	
	@Override
	protected <U extends IValue> Result<U> intersectList(ListResult that) {
		// Note the reversal of args
		IListWriter w = type.writer(getValueFactory());
		Type type = getType().lub(that.getType());
		for(IValue v : that.getValue())
			if(getValue().contains(v))
				w.append(v);
		return makeResult(type, w.done(), ctx);
	}
	
	
	@Override
	protected <U extends IValue, V extends IValue> Result<U> insertElement(Result<V> that) {
		Type newType = getTypeFactory().listType(that.getType().lub(getType().getElementType()));
		return makeResult(newType, value.insert(that.getValue()), ctx);
	}
	
	<U extends IValue, V extends IValue> Result<U> appendElement(ElementResult<V> that) {
		// this is called by addLists in element types.
		Type newType = getTypeFactory().listType(that.getType().lub(getType().getElementType()));
		return makeResult(newType, value.append(that.getValue()), ctx);
	}

	protected <U extends IValue, V extends IValue> Result<U> removeElement(ElementResult<V> value) {
		IList list = getValue();
		return makeResult(getType(), list.delete(value.getValue()), ctx);
	}

	protected <V extends IValue> Result<IBool> elementOf(ElementResult<V> elementResult) {
		return bool((getValue().contains(elementResult.getValue())), ctx);
	}

	protected <V extends IValue> Result<IBool> notElementOf(ElementResult<V> elementResult) {
		return bool((!getValue().contains(elementResult.getValue())), ctx);
	}
	
	@Override
	protected Result<IBool> equalToList(ListResult that) {
		return that.equalityBoolean(this);
	}
	
	@Override
	protected Result<IBool> nonEqualToList(ListResult that) {
		return that.nonEqualityBoolean(this);
	}
	
	@Override
	protected Result<IBool> nonEqualToListRelation(ListRelationResult that) {
		return that.nonEqualityBoolean(this);
	}
	
	@Override
	protected Result<IBool> greaterThanList(ListResult that) {
	  return that.lessThanList(this);
	}
	
	@Override
	protected Result<IBool> greaterThanOrEqualList(ListResult that) {
	  return that.lessThanOrEqualList(this);
	}
	
	@Override
	protected Result<IBool> lessThanList(ListResult that) {
	  IList val = that.getValue();
    
    if (val.length() > value.length()) {
      return bool(false, ctx);
    }
    
    OUTER:for (int iThat = 0, iThis = 0; iThat < val.length(); iThat++) {
      for (iThis = Math.max(iThis, iThat) ; iThis < value.length(); iThis++) {
        if (val.get(iThat).isEqual(value.get(iThis))) {
          iThis++;
          continue OUTER;
        }
      }
      return bool(false, ctx);
    }

    return bool(val.length() != value.length(), ctx);
	}
	
	@Override
	protected LessThanOrEqualResult lessThanOrEqualList(ListResult that) {
	  IList left = that.getValue();
	  IList right = getValue();
	  
	  if (left.length() == 0) {
	    return new LessThanOrEqualResult(right.length() > 0, right.length() == 0, ctx);
	  }
	  else if (left.length() > right.length()) {
	    return new LessThanOrEqualResult(false, false, ctx);
	  }
	  
		OUTER:for (int iThat = 0, iThis = 0; iThat < left.length(); iThat++) {
		  for (iThis = Math.max(iThis, iThat) ; iThis < right.length(); iThis++) {
		    if (left.get(iThat).isEqual(right.get(iThis))) {
		      continue OUTER;
		    }
		  }
		  return new LessThanOrEqualResult(true, false, ctx);
		}
	  
		return new LessThanOrEqualResult(left.length() < right.length(), left.length() == right.length(), ctx);
	}
	
	@Override
	protected <U extends IValue> Result<U> joinListRelation(ListRelationResult that) {
		// Note the reverse of arguments, we need "that join this"
		int arity1 = that.getValue().arity();
		Type eltType = getType().getElementType();
		Type tupleType = that.getType().getElementType();
		Type fieldTypes[] = new Type[arity1 + 1];
		for (int i = 0;  i < arity1; i++) {
			fieldTypes[i] = tupleType.getFieldType(i);
		}
		fieldTypes[arity1] = eltType;
		Type resultTupleType = getTypeFactory().tupleType(fieldTypes);
		IListWriter writer = getValueFactory().listWriter(resultTupleType);
		IValue fieldValues[] = new IValue[arity1 + 1];
		for (IValue relValue: that.getValue()) {
			for (IValue setValue: this.getValue()) {
				for (int i = 0; i < arity1; i++) {
					fieldValues[i] = ((ITuple)relValue).get(i);
				}
				fieldValues[arity1] = setValue;
				writer.append(getValueFactory().tuple(fieldValues));
			}
		}
		Type resultType = getTypeFactory().lrelTypeFromTuple(resultTupleType);
		return makeResult(resultType, writer.done(), ctx);
	}

	@Override
	protected <U extends IValue> Result<U> joinList(ListResult that) {
		// Note the reverse of arguments, we need "that join this"
		// join between sets degenerates to product
		Type tupleType = getTypeFactory().tupleType(that.getType().getElementType(), 
				getType().getElementType());
		return makeResult(getTypeFactory().lrelTypeFromTuple(tupleType),
				that.getValue().product(getValue()), ctx);
	}
	
}
