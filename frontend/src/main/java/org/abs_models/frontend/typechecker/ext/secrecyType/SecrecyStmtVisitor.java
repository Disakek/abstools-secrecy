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

    public SecrecyStmtVisitor(HashMap<ASTNode<?>,String> _secrecy, SecrecyLatticeStructure secrecyLatticeStructure, SemanticConditionList errors) {
        this._secrecy = _secrecy;
        this.secrecyLatticeStructure = secrecyLatticeStructure;
        this.errors = errors;
    }

    public void visit(Stmt stmt) {
        //System.out.println("Is general");
    }

    public void visit(AssignStmt assignStmt){

        ASTNode<?> LHS = assignStmt.getVar().getDecl();
        Exp RhsExp = assignStmt.getValue();
        String LHSsecLevel = _secrecy.get(LHS);
        SecrecyExpVisitor visitor = new SecrecyExpVisitor(_secrecy, secrecyLatticeStructure);
        
        //TODO: missing a lot of expressions that need implementation
        String RHSsecLevel = RhsExp.accept(visitor);

        //TODOs below
        //What if there is no secrecy value for the variable on the left?
        //if(LHSsecLevel == null)return;
        //if(RHSsecLevel == null)return;

        if(LHSsecLevel != null)System.out.println("LHS: " + assignStmt.getVar() + " with " + LHSsecLevel);
 
        if(RHSsecLevel != null)System.out.println("RHS: " + RhsExp + " with " + RHSsecLevel);
        
        if(LHSsecLevel == null || RHSsecLevel == null)return; //TODO: missing this case

        Set<String> LHScontainedIn = secrecyLatticeStructure.getSetForSecrecyLevel(LHSsecLevel);
        
        if(LHScontainedIn.contains(RHSsecLevel)) {
            errors.add(new TypeError(assignStmt, ErrorMessage.SECRECY_LEAKAGE_ERROR_FROM_TO, LHSsecLevel, "RHS", RHSsecLevel, assignStmt.getVar().getName()));
        }
    }

    public void visit(ReturnStmt returnStmt){
        //System.out.println("Is return");
    }

    //TODO: add all stmt's here
}