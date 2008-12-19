module Calls

/*
 * TODO:
 * - remove all module qualifiers
 * - Graph should extend Set and Relation
*/

import Set;
import Relation;
import Graph;

type str Proc;

public void main(){

rel[Proc, Proc] Calls = {<"a", "b">, <"b", "c">, <"b", "d">, <"d", "c">, <"d", "e">, <"f", "e">, <"f", "g">, <"g", "e">};

	int nCalls = Set::size(Calls);

	assert "nCalls":nCalls == 8;

	set[Proc] Procs = Relation::carrier(Calls);

	assert "Procs": Procs == {"a", "b", "c", "d", "e", "f", "g"};

	int nProcs = Set::size(Relation::carrier(Calls));

	assert "nProcs": nProcs == 7;

	set[str] dCalls = Relation::domain(Calls);
	
	set[str] rCalls = Relation::range(Calls);

	set[Proc] entryPoints = Graph::top(Calls);

	assert "a1": entryPoints == {"a", "f"};

	set[Proc] bottomCalls = Graph::bottom(Calls);

	assert "a2": bottomCalls == {"c", "e"};

	rel[Proc,Proc] closureCalls = Calls+;

	assert "a3": closureCalls == 
		{<"a", "b">, <"b", "c">, <"b", "d">, <"d", "c">, 
		<"d","e">, <"f", "e">, <"f", "g">, <"g", "e">, 
		<"a", "c">, <"a", "d">, <"b", "e">, <"a", "e">};

	set[Proc] calledFromA = closureCalls["a"];

	assert "a4": calledFromA == {"b", "c", "d", "e"};

	set[Proc] calledFromF = closureCalls["f"];

	assert "a5": calledFromF == {"e", "g"};

	set[Proc] commonProcs = calledFromA & calledFromF;

	assert "a6": commonProcs == {"e"};

	return;
}
