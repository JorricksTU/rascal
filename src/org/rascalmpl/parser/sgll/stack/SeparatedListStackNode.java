package org.rascalmpl.parser.sgll.stack;

import org.eclipse.imp.pdb.facts.IConstructor;
import org.rascalmpl.parser.sgll.IGLL;
import org.rascalmpl.parser.sgll.result.ContainerNode;
import org.rascalmpl.parser.sgll.result.INode;
import org.rascalmpl.parser.sgll.util.ArrayList;
import org.rascalmpl.parser.sgll.util.IntegerList;

public final class SeparatedListStackNode extends AbstractStackNode implements IListStackNode{
	private final IConstructor symbol;

	private final AbstractStackNode child;
	private final AbstractStackNode[] separators;
	private final boolean isPlusList;
	
	private ContainerNode result;
	
	public SeparatedListStackNode(int id, IConstructor symbol, AbstractStackNode child, AbstractStackNode[] separators, boolean isPlusList){
		super(id);
		
		this.symbol = symbol;
		
		this.child = child;
		this.separators = separators;
		this.isPlusList = isPlusList;
	}
	
	private SeparatedListStackNode(SeparatedListStackNode original, int newId){
		super(newId);
		
		symbol = original.symbol;

		child = original.child;
		separators = original.separators;
		isPlusList = original.isPlusList;
	}
	
	private SeparatedListStackNode(SeparatedListStackNode original){
		super(original);
		
		symbol = original.symbol;

		child = original.child;
		separators = original.separators;
		isPlusList = original.isPlusList;
	}
	
	private SeparatedListStackNode(SeparatedListStackNode original, ArrayList<INode[]> prefixes, IntegerList prefixStartLocations){
		super(original, prefixes, prefixStartLocations);
		
		symbol = original.symbol;

		child = original.child;
		separators = original.separators;
		isPlusList = original.isPlusList;
	}
	
	public String getMethodName(){
		throw new UnsupportedOperationException();
	}
	
	public boolean reduce(char[] input){
		throw new UnsupportedOperationException();
	}
	
	public boolean isClean(){
		return (result == null);
	}
	
	public AbstractStackNode getCleanCopy(){
		return new SeparatedListStackNode(this);
	}
	
	public AbstractStackNode getCleanCopyWithPrefix(){
		return new SeparatedListStackNode(this, prefixes, prefixStartLocations);
	}
	
	public void initializeResultStore(){
		result = new ContainerNode();
	}
	
	public int getLength(){
		throw new UnsupportedOperationException();
	}
	
	public Object[] getChildren(){
		AbstractStackNode psn = child.getCleanCopy();
		SeparatedListStackNode slpsn = new SeparatedListStackNode(this, id | IGLL.LIST_LIST_FLAG);
		
		AbstractStackNode from = psn;
		for(int i = 0; i < separators.length; i++){
			AbstractStackNode to = separators[i].getCleanCopy();
			from.addNext(to);
			from = to;
		}
		from.addNext(slpsn);
		
		psn.addEdge(this, symbol);
		slpsn.addEdge(this, symbol);
		
		psn.setStartLocation(startLocation);
		
		if(isPlusList){
			return new Object[]{symbol, psn};
		}
		
		EpsilonStackNode epsn = new EpsilonStackNode(DEFAULT_LIST_EPSILON_ID);
		epsn.addEdge(this, symbol);
		epsn.setStartLocation(startLocation);
		
		return new Object[]{symbol, psn, epsn};
	}
	
	public void addResult(IConstructor production, INode[] children){
		result.addAlternative(production, children);
	}
	
	public INode getResult(){
		return result;
	}

	public String toString(){
		StringBuilder sb = new StringBuilder();
		sb.append(symbol);
		sb.append(getId());
		sb.append('(');
		sb.append(startLocation);
		sb.append(',');
		sb.append('?');
		sb.append(')');
		
		return sb.toString();
	}
}
