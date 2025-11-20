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

    String confidentialityOfProgramPoint; 

    public SecrecyStmtVisitor(HashMap<ASTNode<?>,String> _secrecy, SecrecyLatticeStructure secrecyLatticeStructure, SemanticConditionList errors, String confidentialityOfProgramPoint) {
        this._secrecy = _secrecy;
        this.secrecyLatticeStructure = secrecyLatticeStructure;
        this.errors = errors;
        this.confidentialityOfProgramPoint = confidentialityOfProgramPoint;

        ExpVisitor = new SecrecyExpVisitor(_secrecy, secrecyLatticeStructure, confidentialityOfProgramPoint);
    }

    //todo do I need this base case and if yes what shall it do
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
            2.Get the parentNode(method implementation node) //todo:consider special cases like interface with default implementation can have return
            3.TRY to get secrecy returnDefinitionLevel if not null
            4.TRY to get secrecy returnActualLevel if not null
            5.Check if the returnActualLevel is lower or at most equal to the returnDefinitionLevel (add an error otherwise)
        */
    }

    public void visit(IfStmt ifStmt){
        
        String oldProgramPoint = confidentialityOfProgramPoint;
        Exp condition = ifStmt.getCondition();

        if(condition.accept(ExpVisitor) != null)confidentialityOfProgramPoint = secrecyLatticeStructure.join(condition.accept(ExpVisitor), confidentialityOfProgramPoint);
        
        ExpVisitor.updateProgramPoint(confidentialityOfProgramPoint);
        Stmt thenCase = ifStmt.getThen();
        thenCase.accept(this);
        
        if(ifStmt.hasElse()) {
            Stmt elseCase = ifStmt.getElse();
            elseCase.accept(this);
        }

        if(confidentialityOfProgramPoint != oldProgramPoint) {
            System.out.println("Changed pc to: " + confidentialityOfProgramPoint);
            confidentialityOfProgramPoint = oldProgramPoint;
            System.out.println("Changed pc back to: " + confidentialityOfProgramPoint);
            ExpVisitor.updateProgramPoint(confidentialityOfProgramPoint);
        }
        /* Descripton:
            1.Store the pc level which we had so far
            2.Get the condition of the ifStmt
            3.Set pc to the join of (condition level, old pc level) //mby old pc level was already higher
            4.Evaluate the then (and if existing else) branch with the new pc
            5.Reset the pc to the old value once finished
        */
    }

    public void visit(WhileStmt whileStmt) {
        
        String oldProgramPoint = confidentialityOfProgramPoint;
        Exp condition = whileStmt.getCondition();

        if(condition.accept(ExpVisitor) != null)confidentialityOfProgramPoint = secrecyLatticeStructure.join(condition.accept(ExpVisitor), confidentialityOfProgramPoint);
        
        ExpVisitor.updateProgramPoint(confidentialityOfProgramPoint);
        Stmt body = whileStmt.getBody();
        body.accept(this);

        if(confidentialityOfProgramPoint != oldProgramPoint) {
            System.out.println("Changed pc to: " + confidentialityOfProgramPoint);
            confidentialityOfProgramPoint = oldProgramPoint;
            System.out.println("Changed pc back to: " + confidentialityOfProgramPoint);
            ExpVisitor.updateProgramPoint(confidentialityOfProgramPoint);
        }
        /* Descripton:
            1.Store the pc level which we had so far
            2.Get the condition of the whileStmt
            3.Set pc to the join of (condition level, old pc level) //mby old pc level was already higher
            4.Evaluate the body with the new pc
            5.Reset the pc to the old value once finished
        */
    }

    public void visit(AwaitStmt awaitStmt) {

        //I believe we need a list of secrecy contexts we have (but global so replace confidentialityOfCurrentProgramPoint)
        //Add the current secrecy to the list to be able to leave all awaits again
        LinkedList<String> awaitSecrecyLevels = new LinkedList<String>();
        awaitSecrecyLevels.add(confidentialityOfProgramPoint);

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
            TODO MISSING -> (X.Remove the node from the list once we have a Get for it) 
        */
    }

    private void handleSingleGuards(Guard inGuard) {

        //I believe we need a list of secrecy contexts we have
        LinkedList<String> awaitSecrecyLevels = new LinkedList<String>();

        //Add the current secrecy to the list to be able to leave all awaits again
        awaitSecrecyLevels.add(confidentialityOfProgramPoint);

        if (inGuard instanceof ExpGuard expGuard) {
  
            Exp awaitExpr = (Exp) expGuard.getChild(0);
            String getAwaitSecrecy = awaitExpr.accept(ExpVisitor);

            //TODO missing add to list -> Add the join of (the old with the new) to the list
            awaitSecrecyLevels.add(secrecyLatticeStructure.join(awaitSecrecyLevels.getLast(),getAwaitSecrecy));
            
            System.out.println("added expguard: " + awaitExpr + " has Secrecy: " + getAwaitSecrecy);

        
        } else if (inGuard instanceof ClaimGuard claimGuard) {

            VarOrFieldUse awaitClaim = (VarOrFieldUse) claimGuard.getChild(0);
            String secrecyLevel = awaitClaim.accept(ExpVisitor);

            //TODO missing add to list
            awaitSecrecyLevels.add(secrecyLatticeStructure.join(awaitSecrecyLevels.getLast(),getAwaitSecrecy));

            System.out.println("added claimguard: " + claimGuard + " getDecl is: " + secrecyLevel);
       
        } else if (inGuard instanceof AndGuard andGuard) {

            handleSingleGuards(andGuard.getLeft());
            handleSingleGuards(andGuard.getRight());
        }
        
        System.out.println(awaitSecrecyLevels);
    }
}