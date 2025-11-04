/**
 * Copyright (c) 2009-2011, The HATS Consortium. All rights reserved. 
 * This file is licensed under the terms of the Modified BSD License.
 * Written by @Maximilian_Paul for questions please refer to uukln@student.kit.edu
 */
package org.abs_models.frontend.typechecker.ext;

import java.util.HashMap;
import org.abs_models.frontend.ast.*;
import org.abs_models.frontend.typechecker.*;

public class SecrecyExpVisitor {

    private HashMap<ASTNode<?>,String> _secrecy = new HashMap<>();

    private SecrecyLatticeStructure secrecyLatticeStructure;

    String confidentialityOfProgramPoint; 

    public SecrecyExpVisitor(HashMap<ASTNode<?>,String> _secrecy, SecrecyLatticeStructure secrecyLatticeStructure, String confidentialityOfProgramPoint) {
        this._secrecy = _secrecy;
        this.secrecyLatticeStructure = secrecyLatticeStructure;
        this.confidentialityOfProgramPoint = confidentialityOfProgramPoint;
    }

    //No matter what Expression is to be visited returning the least upper bound of secrecy values or null if there is none
    //TODO check if the implements are needed/used or remove them

    public String visit(Exp expression){
        return secrecyLatticeStructure.getMinSecrecyLevel();
    }

    public String visit(AddAddExp addAddExp) {
        
        String leftLevel = addAddExp.getLeft().accept(this);
        String rightLevel = addAddExp.getRight().accept(this);
        String combined = secrecyLatticeStructure.join(leftLevel, rightLevel);
        //System.out.println("AddAddExp: (" + leftLevel + " + " + rightLevel + ") -> " + combined);

        return secrecyLatticeStructure.join(combined, confidentialityOfProgramPoint);
    }

    public String visit(SubAddExp subAddExp) {
        
        String leftLevel = subAddExp.getLeft().accept(this);
        String rightLevel = subAddExp.getRight().accept(this);
        String combined = secrecyLatticeStructure.join(leftLevel, rightLevel);
        //System.out.println("SubAddExp: (" + leftLevel + " + " + rightLevel + ") -> " + combined);

        return secrecyLatticeStructure.join(combined, confidentialityOfProgramPoint);
    }

    public String visit(MultMultExp multMultExp) {
        
        String leftLevel = multMultExp.getLeft().accept(this);
        String rightLevel = multMultExp.getRight().accept(this);
        String combined = secrecyLatticeStructure.join(leftLevel, rightLevel);
        //System.out.println("MultMultExp: (" + leftLevel + " + " + rightLevel + ") -> " + combined);

        return secrecyLatticeStructure.join(combined, confidentialityOfProgramPoint);
    }

    public String visit(DivMultExp divMultExp) {
        
        String leftLevel = divMultExp.getLeft().accept(this);
        String rightLevel = divMultExp.getRight().accept(this);
        String combined = secrecyLatticeStructure.join(leftLevel, rightLevel);
        //System.out.println("DivMultExp: (" + leftLevel + " + " + rightLevel + ") -> " + combined);

        return secrecyLatticeStructure.join(combined, confidentialityOfProgramPoint);
    }

    public String visit(ModMultExp modMultExp) {
        
        String leftLevel = modMultExp.getLeft().accept(this);
        String rightLevel = modMultExp.getRight().accept(this);
        String combined = secrecyLatticeStructure.join(leftLevel, rightLevel);
        //System.out.println("ModMultExp: (" + leftLevel + " + " + rightLevel + ") -> " + combined);

        return secrecyLatticeStructure.join(combined, confidentialityOfProgramPoint);
    }

    public String visit(MinusExp minusExp) {
        
        ASTNode<?> child = minusExp.getChild(0);

        if(child instanceof Exp expr) {
            return secrecyLatticeStructure.join(expr.accept(this), confidentialityOfProgramPoint);
        }

        return null;
    }

    public String visit(NegExp negExp) {
        
        ASTNode<?> child = negExp.getChild(0);

        if(child instanceof Exp expr) {
            return secrecyLatticeStructure.join(expr.accept(this), confidentialityOfProgramPoint);
        }

        return null;
    }

    public String visit(AndBoolExp andExp) {
        
        String leftLevel = andExp.getLeft().accept(this);
        String rightLevel = andExp.getRight().accept(this);
        String combined = secrecyLatticeStructure.join(leftLevel, rightLevel);
        //System.out.println("AndExp: (" + leftLevel + " + " + rightLevel + ") -> " + combined);

        return secrecyLatticeStructure.join(combined, confidentialityOfProgramPoint);
    }

    
    public String visit(OrBoolExp orExp) {
        
        String leftLevel = orExp.getLeft().accept(this);
        String rightLevel = orExp.getRight().accept(this);
        String combined = secrecyLatticeStructure.join(leftLevel, rightLevel);
        //System.out.println("OrExp: (" + leftLevel + " + " + rightLevel + ") -> " + combined);

        return secrecyLatticeStructure.join(combined, confidentialityOfProgramPoint);
    }

    
    public String visit(EqExp eqExp) {
        
        String leftLevel = eqExp.getLeft().accept(this);
        String rightLevel = eqExp.getRight().accept(this);
        String combined = secrecyLatticeStructure.join(leftLevel, rightLevel);
        //System.out.println("OrExp: (" + leftLevel + " + " + rightLevel + ") -> " + combined);

        return secrecyLatticeStructure.join(combined, confidentialityOfProgramPoint);
    }

    public String visit(NotEqExp notEqExp) {
        
        String leftLevel = notEqExp.getLeft().accept(this);
        String rightLevel = notEqExp.getRight().accept(this);
        String combined = secrecyLatticeStructure.join(leftLevel, rightLevel);
        //System.out.println("OrExp: (" + leftLevel + " + " + rightLevel + ") -> " + combined);

        return secrecyLatticeStructure.join(combined, confidentialityOfProgramPoint);
    }

    public String visit(LTEQExp lessThanEqualsExp) {

        String leftLevel = lessThanEqualsExp.getLeft().accept(this);
        String rightLevel = lessThanEqualsExp.getRight().accept(this);
        String combined = secrecyLatticeStructure.join(leftLevel, rightLevel);
        //System.out.println("OrExp: (" + leftLevel + " + " + rightLevel + ") -> " + combined);

        return secrecyLatticeStructure.join(combined, confidentialityOfProgramPoint);
    }

    public String visit(GTEQExp greaterThanEqualsExp) {

        String leftLevel = greaterThanEqualsExp.getLeft().accept(this);
        String rightLevel = greaterThanEqualsExp.getRight().accept(this);
        String combined = secrecyLatticeStructure.join(leftLevel, rightLevel);
        //System.out.println("OrExp: (" + leftLevel + " + " + rightLevel + ") -> " + combined);

        return secrecyLatticeStructure.join(combined, confidentialityOfProgramPoint);
    }
   
    public String visit(LTExp lessThanExp) {

        String leftLevel = lessThanExp.getLeft().accept(this);
        String rightLevel = lessThanExp.getRight().accept(this);
        String combined = secrecyLatticeStructure.join(leftLevel, rightLevel);
        //System.out.println("OrExp: (" + leftLevel + " + " + rightLevel + ") -> " + combined);

        return secrecyLatticeStructure.join(combined, confidentialityOfProgramPoint);
    }
    public String visit(GTExp greaterThanExp) {

        String leftLevel = greaterThanExp.getLeft().accept(this);
        String rightLevel = greaterThanExp.getRight().accept(this);
        String combined = secrecyLatticeStructure.join(leftLevel, rightLevel);
        //System.out.println("OrExp: (" + leftLevel + " + " + rightLevel + ") -> " + combined);

        return secrecyLatticeStructure.join(combined, confidentialityOfProgramPoint);
    }

    public String visit(VarOrFieldUse varOrFieldUse) {

        ASTNode<?> variable = varOrFieldUse.getDecl();
        String secrecy = _secrecy.get(variable);

        if (secrecy != null) {
            //System.out.println("VarOrFieldExp: "  + " -> " + secrecy);
            return secrecy;
        }

        //Assume low secrecy as default
        return secrecyLatticeStructure.join(secrecyLatticeStructure.getMinSecrecyLevel(), confidentialityOfProgramPoint);
    }

    public void updateProgramPoint(String newConfidentiality) {
        confidentialityOfProgramPoint = newConfidentiality;
    }
}