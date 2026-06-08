package org.abs_models.frontend.typechecker.ext;

import java.util.HashMap;

import java.util.LinkedList;
import org.abs_models.frontend.ast.*;
import org.abs_models.frontend.analyser.SemanticConditionList;

public abstract class SecrecyStmtVisitor {

    protected Model m;
    protected SecrecyLatticeStructure secrecyLatticeStructure;
    protected final SemanticConditionList errors;
    protected LinkedList<ProgramCountNode> programConfidentiality;
    protected LinkedList<CalledMethod> methodsCallingOthers;

    public SecrecyStmtVisitor(Model m, SecrecyLatticeStructure secrecyLatticeStructure, SemanticConditionList errors,LinkedList<ProgramCountNode> programConfidentiality, LinkedList<CalledMethod> methodsCallingOthers) {
        this.m = m;
        this.secrecyLatticeStructure = secrecyLatticeStructure;
        this.errors = errors;
        this.programConfidentiality = programConfidentiality;
        this.methodsCallingOthers = methodsCallingOthers;
    }

    public abstract void visit(Stmt stmt);
    public abstract void visit(Block blockStmt);
    public abstract void visit(AssignStmt assignStmt);
    public abstract void visit(ReturnStmt returnStmt);
    public abstract void visit(IfStmt ifStmt);
    public abstract void visit(WhileStmt whileStmt);
    public abstract void visit(ExpressionStmt expressionStmt);
    public abstract void visit(VarDeclStmt varDeclStmt);
    public abstract void visit(AwaitStmt awaitStmt);
    public abstract void updateProgramPoint(LinkedList<ProgramCountNode> newConfidentiality);
    public void updateCurrentSecrecy(HashMap<ASTNode<?>, String> newCurrentSecrecy) {}
}
