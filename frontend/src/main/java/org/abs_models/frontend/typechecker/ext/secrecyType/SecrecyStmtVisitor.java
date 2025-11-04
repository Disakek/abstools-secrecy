/**
 * Copyright (c) 2009-2011, The HATS Consortium. All rights reserved. 
 * This file is licensed under the terms of the Modified BSD License.
 * Written by @Maximilian_Paul for questions please refer to uukln@student.kit.edu
 */
package org.abs_models.frontend.typechecker.ext;

import java.util.HashMap;
import java.util.Set;

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

    public SecrecyStmtVisitor(HashMap<ASTNode<?>,String> _secrecy, SecrecyLatticeStructure secrecyLatticeStructure, SemanticConditionList errors) {
        this._secrecy = _secrecy;
        this.secrecyLatticeStructure = secrecyLatticeStructure;
        this.errors = errors;

        ExpVisitor = new SecrecyExpVisitor(_secrecy, secrecyLatticeStructure);
    }

    public void visit(Stmt stmt) {
        //System.out.println("Is general");
    }

    public void visit(AssignStmt assignStmt){

        ASTNode<?> LHS = assignStmt.getVar().getDecl();
        Exp RhsExp = assignStmt.getValue();

        //TODO: missing a lot of expressions that need implementation
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

        if(!returnActualLevel.contains(returnDefinitionLevel)) {
            errors.add(new TypeError(returnStmt, ErrorMessage.SECRECY_LEAKAGE_ERROR_FROM_TO, returnActualLevel, "returnStmt", returnDefinitionLevel, "returnDefinition"));
        }

        /* Descripton:
            1.Get the actual returnStmt
            2.Get the parentNode(method implementation node) //todo:consider special cases like interface with default implementation can have return
            3.TRY to get secrecy returnDefinitionLevel if not null
            4.TRY to get secrecy returnActualLevel if not null
            5.Check if the returnActualLevel is lower or at most as high as the returnDefinitionLevel (add an error otherwise)
        */
    }

    public void visit(VarDeclStmt varDeclStmt){
        System.out.println(varDeclStmt);
    }

    //TODO: add all stmt's here
}