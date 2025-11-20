/**
 * Copyright (c) 2009-2011, The HATS Consortium. All rights reserved. 
 * This file is licensed under the terms of the Modified BSD License.
 * Written by @Maximilian_Paul for questions please refer to uukln@student.kit.edu
 */
package org.abs_models.frontend.typechecker.ext;

import java.util.HashMap;
import java.util.Set;
import java.util.LinkedList;

import org.abs_models.frontend.ast.*;
import org.abs_models.frontend.typechecker.*;
import org.abs_models.frontend.analyser.ErrorMessage;
import org.abs_models.frontend.analyser.TypeError;
import org.abs_models.frontend.analyser.SemanticCondition;
import org.abs_models.frontend.analyser.SemanticConditionList;

public class SecrecyStmtVisitor {

    private HashMap<ASTNode<?>,String> _secrecy = new HashMap<>();

    private SecrecyLatticeStructure secrecyLatticeStructure;

    private final SemanticConditionList errors;

    private SecrecyExpVisitor ExpVisitor;

    LinkedList<ProgramCountNode> programConfidentiality;

    public SecrecyStmtVisitor(HashMap<ASTNode<?>,String> _secrecy, SecrecyLatticeStructure secrecyLatticeStructure, SemanticConditionList errors,LinkedList<ProgramCountNode> programConfidentiality) {
        this._secrecy = _secrecy;
        this.secrecyLatticeStructure = secrecyLatticeStructure;
        this.errors = errors;
        this.programConfidentiality = programConfidentiality;

        ExpVisitor = new SecrecyExpVisitor(_secrecy, secrecyLatticeStructure, programConfidentiality);
    }

    //todo I need this base case but why and what should it do or is it good like this
    public void visit(Stmt stmt) {
        return;
    }

    public void visit(Block blockStmt){
        for(Stmt stmt : blockStmt.getStmtList()) {
            stmt.accept(this);
        }
    }

    public void visit(AssignStmt assignStmt){

        ASTNode<?> LHS = assignStmt.getVar().getDecl();
        Exp RhsExp = assignStmt.getValue();

        String LHSsecLevel = secrecyLatticeStructure.getMinSecrecyLevel();
        String RHSsecLevel = secrecyLatticeStructure.getMinSecrecyLevel();

        if(_secrecy.get(LHS) != null)LHSsecLevel = _secrecy.get(LHS);
        if(RhsExp.accept(ExpVisitor) != null)RHSsecLevel = RhsExp.accept(ExpVisitor);
        Set<String> LHScontainedIn = secrecyLatticeStructure.getSetForSecrecyLevel(LHSsecLevel);
        
        if(LHScontainedIn.contains(RHSsecLevel)) {
            errors.add(new TypeError(assignStmt, ErrorMessage.SECRECY_LEAKAGE_ERROR_FROM_TO, RHSsecLevel, assignStmt.getValue().toString(), LHSsecLevel, assignStmt.getVar().getName()));
        }

        /*
            1.Get the LHS and RHSExp
            2.Set the default secrecy to low
            3.Overwrite the secrecy levels if there are annotations
            4.Check if the rhs is lower or at most as high as the lhs (add an error otherwise)

            Note: If it is contained than LHS is lower than RHS which is the violation of our rule
        */
    }

    public void visit(ReturnStmt returnStmt){
        
        ASTNode<?> returnExp = returnStmt.getChild(1);
        ASTNode<?> parentNode = returnStmt.getParent();
        String returnDefinitionLevel = secrecyLatticeStructure.getMinSecrecyLevel();
        String returnActualLevel = secrecyLatticeStructure.getMinSecrecyLevel();

        while(!(parentNode instanceof MethodImpl)) {
            parentNode = parentNode.getParent();
        }

        if((parentNode instanceof MethodImpl methodImpl)) {

            MethodSig methodSig = methodImpl.getMethodSig();

            if(_secrecy.get(methodSig) != null)returnDefinitionLevel = _secrecy.get(methodSig);
        }

        if(returnExp instanceof Exp exp) {

            if(exp.accept(ExpVisitor) != null)returnActualLevel = exp.accept(ExpVisitor);
        }

        Set<String> methodReturnSet = secrecyLatticeStructure.getSetForSecrecyLevel(returnActualLevel);

        if(!(methodReturnSet.contains(returnDefinitionLevel)) && !(returnActualLevel.equals(returnDefinitionLevel))) {
            errors.add(new TypeError(returnStmt, ErrorMessage.SECRECY_LEAKAGE_ERROR_FROM_TO, returnActualLevel, "returnStmt", returnDefinitionLevel, "returnDefinition"));
        }

        /* Descripton:
            1.Get the actual returnStmt
            2.Get the parentNode(method implementation node)
            3.TRY to get secrecy returnDefinitionLevel if not null
            4.TRY to get secrecy returnActualLevel if not null
            5.Check if the returnActualLevel is lower or at most equal to the returnDefinitionLevel (add an error otherwise)
        */
    }

    public void visit(IfStmt ifStmt){

        Exp condition = ifStmt.getCondition();

        if(condition.accept(ExpVisitor) != null) {
            programConfidentiality.add(new ProgramCountNode(ifStmt, secrecyLatticeStructure.join(condition.accept(ExpVisitor), secrecyLatticeStructure.evaluateListLevel(programConfidentiality))));
        }
        
        ExpVisitor.updateProgramPoint(programConfidentiality);
        Stmt thenCase = ifStmt.getThen();
        thenCase.accept(this);
        
        if(ifStmt.hasElse()) {
            Stmt elseCase = ifStmt.getElse();
            elseCase.accept(this);
        }

        programConfidentiality.removeLast();
        ExpVisitor.updateProgramPoint(programConfidentiality);
        /* Descripton:
            1.Get the condition of the ifStmt
            2.Set pc to the join of (condition level, currentLevelByList) //mby old pc level was already higher
            3.Evaluate the then (and if existing else) branch with the new pc
            4.Reset the pc to the old value once finished by removing the last list element
        */
    }

    public void visit(WhileStmt whileStmt) {
        
        Exp condition = whileStmt.getCondition();

        if(condition.accept(ExpVisitor) != null)programConfidentiality.add(new ProgramCountNode(whileStmt, secrecyLatticeStructure.join(condition.accept(ExpVisitor), secrecyLatticeStructure.evaluateListLevel(programConfidentiality))));

        ExpVisitor.updateProgramPoint(programConfidentiality);
        Stmt body = whileStmt.getBody();
        body.accept(this);

        programConfidentiality.removeLast();
        ExpVisitor.updateProgramPoint(programConfidentiality);
        /* Descripton:
            1.Get the condition of the whileStmt
            2.Set pc to the join of (condition level, currentLevelByList) //mby old pc level was already higher
            3.Evaluate the body with the new pc
            4.Reset the pc to the old value once finished by removing the last list element
        */
    }

    public void visit(AwaitStmt awaitStmt) {

        //Get the guard of the awaitstmt -> await guard;
        Guard getGuard = awaitStmt.getGuard();

        //4 different guards | duration -> nothing | and -> handle left & right | ...-> add it's secrecy level to the list
        if (getGuard instanceof AndGuard andGuard) {

            handleSingleGuards(andGuard.getLeft());
            handleSingleGuards(andGuard.getRight());

        } else {
            handleSingleGuards(getGuard);
        }
        /* Descripton:
            1.Get the guard of the await stmt
            2.If it is an AndGuard handle left and right
            3.Extract the secrecy level for the guard (or low)
            4.Insert a node for the await into the pc list -> join(guardLevel, currentLevel)
            todo MISSING -> (X.Remove the node from the list once we have a Get for it) 
        */
    }

    private void handleSingleGuards(Guard inGuard) {

        if (inGuard instanceof ExpGuard expGuard) {
  
            Exp awaitExpr = (Exp) expGuard.getChild(0);
            String getAwaitSecrecy = awaitExpr.accept(ExpVisitor);

            programConfidentiality.add(new ProgramCountNode(awaitExpr, secrecyLatticeStructure.join(programConfidentiality.getLast().getSecrecyLevel(),getAwaitSecrecy)));
            
            System.out.println("added expguard: " + awaitExpr + " has Secrecy: " + getAwaitSecrecy);
            System.out.println(programConfidentiality.toString());
        
        } else if (inGuard instanceof ClaimGuard claimGuard) {

            VarOrFieldUse awaitClaim = (VarOrFieldUse) claimGuard.getChild(0);
            String getAwaitSecrecy = awaitClaim.accept(ExpVisitor);

            programConfidentiality.add(new ProgramCountNode(awaitClaim, secrecyLatticeStructure.join(programConfidentiality.getLast().getSecrecyLevel(),getAwaitSecrecy)));

            System.out.println("added claimguard: " + claimGuard + " getDecl is: " + getAwaitSecrecy);
            System.out.println(programConfidentiality);

        } else if (inGuard instanceof AndGuard andGuard) {

            handleSingleGuards(andGuard.getLeft());
            handleSingleGuards(andGuard.getRight());
        }
        
        System.out.println(programConfidentiality.toString());
    }
}