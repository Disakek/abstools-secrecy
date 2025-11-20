/**
 * Copyright (c) 2009-2011, The HATS Consortium. All rights reserved. 
 * This file is licensed under the terms of the Modified BSD License.
 * Written by @Maximilian_Paul for questions please refer to uukln@student.kit.edu
 */
package org.abs_models.frontend.typechecker.ext;

import java.util.HashMap;
import java.util.LinkedList;
import org.abs_models.frontend.ast.*;
import org.abs_models.frontend.typechecker.*;

/**
 * This class is used to extract the secrecy levels for the different expressions.
 */
public class SecrecyExpVisitor {

    private HashMap<ASTNode<?>,String> _secrecy = new HashMap<>();

    private SecrecyLatticeStructure secrecyLatticeStructure;

    LinkedList<ProgramCountNode> programConfidentiality;

    public SecrecyExpVisitor(HashMap<ASTNode<?>,String> _secrecy, SecrecyLatticeStructure secrecyLatticeStructure, LinkedList<ProgramCountNode> programConfidentiality) {
        this._secrecy = _secrecy;
        this.secrecyLatticeStructure = secrecyLatticeStructure;
        this.programConfidentiality = programConfidentiality;
    }

    /**
     * Visit function for expressions tries to return an attached secrecy level.
     * Calls the different implementations to handle it for all kinds of expressions.
     */
    public String visit(Exp expression){
        return secrecyLatticeStructure.getMinSecrecyLevel();
    }

    public String visit(AddAddExp addAddExp) {
        
        String leftLevel = addAddExp.getLeft().accept(this);
        String rightLevel = addAddExp.getRight().accept(this);
        String combined = secrecyLatticeStructure.join(leftLevel, rightLevel);

        return secrecyLatticeStructure.join(combined, secrecyLatticeStructure.evaluateListLevel(programConfidentiality));
    }

    public String visit(SubAddExp subAddExp) {
        
        String leftLevel = subAddExp.getLeft().accept(this);
        String rightLevel = subAddExp.getRight().accept(this);
        String combined = secrecyLatticeStructure.join(leftLevel, rightLevel);

        return secrecyLatticeStructure.join(combined, secrecyLatticeStructure.evaluateListLevel(programConfidentiality));
    }

    public String visit(MultMultExp multMultExp) {
        
        String leftLevel = multMultExp.getLeft().accept(this);
        String rightLevel = multMultExp.getRight().accept(this);
        String combined = secrecyLatticeStructure.join(leftLevel, rightLevel);

        return secrecyLatticeStructure.join(combined, secrecyLatticeStructure.evaluateListLevel(programConfidentiality));
    }

    public String visit(DivMultExp divMultExp) {
        
        String leftLevel = divMultExp.getLeft().accept(this);
        String rightLevel = divMultExp.getRight().accept(this);
        String combined = secrecyLatticeStructure.join(leftLevel, rightLevel);

        return secrecyLatticeStructure.join(combined, secrecyLatticeStructure.evaluateListLevel(programConfidentiality));
    }

    public String visit(ModMultExp modMultExp) {
        
        String leftLevel = modMultExp.getLeft().accept(this);
        String rightLevel = modMultExp.getRight().accept(this);
        String combined = secrecyLatticeStructure.join(leftLevel, rightLevel);

        return secrecyLatticeStructure.join(combined, secrecyLatticeStructure.evaluateListLevel(programConfidentiality));
    }

    public String visit(MinusExp minusExp) {
        
        ASTNode<?> child = minusExp.getChild(0);

        if(child instanceof Exp expr) {
            return secrecyLatticeStructure.join(expr.accept(this), secrecyLatticeStructure.evaluateListLevel(programConfidentiality));
        }

        return null;
    }

    public String visit(NegExp negExp) {
        
        ASTNode<?> child = negExp.getChild(0);

        if(child instanceof Exp expr) {
            return secrecyLatticeStructure.join(expr.accept(this), secrecyLatticeStructure.evaluateListLevel(programConfidentiality));
        }

        return null;
    }

    public String visit(AndBoolExp andExp) {
        
        String leftLevel = andExp.getLeft().accept(this);
        String rightLevel = andExp.getRight().accept(this);
        String combined = secrecyLatticeStructure.join(leftLevel, rightLevel);

        return secrecyLatticeStructure.join(combined, secrecyLatticeStructure.evaluateListLevel(programConfidentiality));
    }

    
    public String visit(OrBoolExp orExp) {
        
        String leftLevel = orExp.getLeft().accept(this);
        String rightLevel = orExp.getRight().accept(this);
        String combined = secrecyLatticeStructure.join(leftLevel, rightLevel);

        return secrecyLatticeStructure.join(combined, secrecyLatticeStructure.evaluateListLevel(programConfidentiality));
    }

    
    public String visit(EqExp eqExp) {
        
        String leftLevel = eqExp.getLeft().accept(this);
        String rightLevel = eqExp.getRight().accept(this);
        String combined = secrecyLatticeStructure.join(leftLevel, rightLevel);

        return secrecyLatticeStructure.join(combined, secrecyLatticeStructure.evaluateListLevel(programConfidentiality));
    }

    public String visit(NotEqExp notEqExp) {
        
        String leftLevel = notEqExp.getLeft().accept(this);
        String rightLevel = notEqExp.getRight().accept(this);
        String combined = secrecyLatticeStructure.join(leftLevel, rightLevel);

        return secrecyLatticeStructure.join(combined, secrecyLatticeStructure.evaluateListLevel(programConfidentiality));
    }

    public String visit(LTEQExp lessThanEqualsExp) {

        String leftLevel = lessThanEqualsExp.getLeft().accept(this);
        String rightLevel = lessThanEqualsExp.getRight().accept(this);
        String combined = secrecyLatticeStructure.join(leftLevel, rightLevel);

        return secrecyLatticeStructure.join(combined, secrecyLatticeStructure.evaluateListLevel(programConfidentiality));
    }

    public String visit(GTEQExp greaterThanEqualsExp) {

        String leftLevel = greaterThanEqualsExp.getLeft().accept(this);
        String rightLevel = greaterThanEqualsExp.getRight().accept(this);
        String combined = secrecyLatticeStructure.join(leftLevel, rightLevel);

        return secrecyLatticeStructure.join(combined, secrecyLatticeStructure.evaluateListLevel(programConfidentiality));
    }
   
    public String visit(LTExp lessThanExp) {

        String leftLevel = lessThanExp.getLeft().accept(this);
        String rightLevel = lessThanExp.getRight().accept(this);
        String combined = secrecyLatticeStructure.join(leftLevel, rightLevel);

        return secrecyLatticeStructure.join(combined, secrecyLatticeStructure.evaluateListLevel(programConfidentiality));
    }
    public String visit(GTExp greaterThanExp) {

        String leftLevel = greaterThanExp.getLeft().accept(this);
        String rightLevel = greaterThanExp.getRight().accept(this);
        String combined = secrecyLatticeStructure.join(leftLevel, rightLevel);

        return secrecyLatticeStructure.join(combined, secrecyLatticeStructure.evaluateListLevel(programConfidentiality));
    }

    /**
     * Visit function for variables or fields that tries to return an attached secrecy level.
     * It joins the variable level, the current program secrecy and the default value.
     * As a default value we use the lowest value from our lattice.
     */
    public String visit(VarOrFieldUse varOrFieldUse) {

        ASTNode<?> variable = varOrFieldUse.getDecl();
        String secrecy = _secrecy.get(variable);

        if (secrecy != null) {
            return secrecy;
        }

        return secrecyLatticeStructure.join(secrecyLatticeStructure.getMinSecrecyLevel(), secrecyLatticeStructure.evaluateListLevel(programConfidentiality));
    }

    /**
     * Allows to update the current program secrecy list on a change.
     */
    public void updateProgramPoint(LinkedList<ProgramCountNode> newConfidentiality) {
        programConfidentiality = newConfidentiality;
    }
}