package org.abs_models.frontend.typechecker.ext;

import java.util.HashMap;
import java.util.LinkedList;

import org.abs_models.frontend.analyser.SemanticConditionList;
import org.abs_models.frontend.ast.*;

public abstract class SecrecyExpVisitor {

    protected Model m;
    protected SecrecyLatticeStructure secrecyLatticeStructure;
    protected SecrecyStmtVisitor stmtVisitor;
    protected LinkedList<ProgramCountNode> programConfidentiality;
    protected final SemanticConditionList errors;
    protected LinkedList<CalledMethod> methodsCallingOthers;

    public SecrecyExpVisitor(Model m, SecrecyLatticeStructure secrecyLatticeStructure, SemanticConditionList errors, LinkedList<ProgramCountNode> programConfidentiality, SecrecyStmtVisitor stmtVisitor, LinkedList<CalledMethod> methodsCallingOthers) {
        this.m = m;
        this.secrecyLatticeStructure = secrecyLatticeStructure;
        this.errors = errors;
        this.programConfidentiality = programConfidentiality;
        this.stmtVisitor = stmtVisitor;
        this.methodsCallingOthers = methodsCallingOthers;
    }

    public abstract String visit(Exp expression);
    public abstract String visit(Binary binaryExp);
    public abstract String visit(Unary unaryExp);
    public abstract String visit(VarOrFieldUse varOrFieldUse);
    public abstract String visit(GetExp getExp);
    public abstract String visit(Call functionCall);
    public abstract String visit(FnApp fnApp);
    public abstract void updateProgramPoint(LinkedList<ProgramCountNode> newConfidentiality);
}