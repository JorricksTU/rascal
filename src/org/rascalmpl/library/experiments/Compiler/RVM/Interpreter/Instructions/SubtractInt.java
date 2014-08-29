package org.rascalmpl.library.experiments.Compiler.RVM.Interpreter.Instructions;

import org.rascalmpl.library.experiments.Compiler.RVM.Interpreter.CodeBlock;
import org.rascalmpl.library.experiments.Compiler.RVM.ToJVM.BytecodeGenerator;

public class SubtractInt extends Instruction {
	
	public SubtractInt(CodeBlock ins) {
		super(ins, Opcode.SUBTRACTINT);
	}
	public void generate(BytecodeGenerator codeEmittor, boolean dcode){
		codeEmittor.emitCallWithArgsSS("insnSUBTRACTINT");
		codeblock.addCode0(opcode.getOpcode());
	}
}
