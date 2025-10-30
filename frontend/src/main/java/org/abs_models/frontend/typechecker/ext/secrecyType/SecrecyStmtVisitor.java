/**
 * Copyright (c) 2009-2011, The HATS Consortium. All rights reserved. 
 * This file is licensed under the terms of the Modified BSD License.
 * Written by @Maximilian_Paul for questions please refer to uukln@student.kit.edu
 */
package org.abs_models.frontend.typechecker.ext;

import java.util.HashMap;
import org.abs_models.frontend.ast.*;
import org.abs_models.frontend.typechecker.*;

public class SecrecyStmtVisitor {

    private HashMap<ASTNode<?>,String> _secrecy = new HashMap<>();

    private SecrecyLatticeStructure secrecyLatticeStructure;

    public SecrecyStmtVisitor(HashMap<ASTNode<?>,String> _secrecy, SecrecyLatticeStructure secrecyLatticeStructure) {
        this._secrecy = _secrecy;
        this.secrecyLatticeStructure = secrecyLatticeStructure;
    }

    public void visit(Stmt stmt) {
        //System.out.println("Is general");
    }

    public void visit(AssignStmt assignStmt){

        Exp RhsExp = assignStmt.getValue();
        String LHSsecLevel = _secrecy.get(assignStmt.getVar().getDecl());

        if(LHSsecLevel != null)System.out.println("LHS: " + assignStmt.getVar() + " with " + LHSsecLevel);

        //try to get the RHSsecLevel next

        /*
        System.out.println("Is assign");

        //Get both sides of the stmt
        VarOrFieldUse LhsVariable = assignStmt.getVar();
        Exp RhsExp = assignStmt.getValue();

        //TODO: Missing case what if there is no secrecy for the left side 
        if(_secrecy.get(LhsVariable.getName()) == null)return;

        //TODO: Implementing a VisitorClass for the different Exp kinds important first

        //TODO: Missing case what if there is no secrecy for the right side 
        //Similiar to this if(_secrecy.get(LhsVariable.getName()) == null)return;

        //TODO: everything below needs work/refactoring
            //TODO: All possible cases for right hand side need to be considered
            //Different options what RHSExp can be (Arithmetic(Mul, Add, ...), )

        
        if (RhsExp instanceof AddAddExp addExp) {
            System.out.println("addExp: " + addExp);
        }
        if (RhsExp instanceof SubAddExp subExp) {
            System.out.println("subExp: " + subExp);
        }
        //TODO: Change the way I store the secrecy annotations from string to objects to make it more usable and allow different kinds
        /*
        String  LHSsecLevel = _secrecy.get(assignStmt.getVar().getName());

            //TODO: if it is a variable on the right hand side
        if (RhsExp instanceof VarOrFieldUse varUse) {

                //TODO: If the right hand side is a variable but doesnt have a secrecy value what do we do?
            if(_secrecy.get(varUse.getName()) == null)return;
                                                
            String RHSsecLevel = _secrecy.get(varUse.getName());
                                                
            Set<String> LHScontainedIn = secrecyLatticeStructure.getSetForSecrecyLevel(LHSsecLevel);

            if(!LHScontainedIn.contains(RHSsecLevel)) {
                errors.add(new TypeError(assignStmt, ErrorMessage.SECRECY_LEAKAGE_ERROR_FROM_TO, LHSsecLevel, varUse.getName(), RHSsecLevel, assignStmt.getVar().getName()));
            } else {
                System.out.println("found no errors in values on two sides");
            }
        }
        */
    }

    public void visit(ReturnStmt returnStmt){
        //System.out.println("Is return");
    }

    //TODO: add all stmt's here
}