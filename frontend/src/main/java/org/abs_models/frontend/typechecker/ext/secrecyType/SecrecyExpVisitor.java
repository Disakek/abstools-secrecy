/**
 * Copyright (c) 2009-2011, The HATS Consortium. All rights reserved. 
 * This file is licensed under the terms of the Modified BSD License.
 * Written by @Maximilian_Paul for questions please refer to uukln@student.kit.edu
 */
package org.abs_models.frontend.typechecker.ext;

import java.util.HashMap;
import java.util.LinkedList;

import org.abs_models.frontend.ast.*;

/**
 * This class is used to extract the secrecylevels for the different expressions and enforce rules with it.
 */
public class SecrecyExpVisitor {

    /**
     * Stores mappings between ASTNode's (declarations) and the assigned secrecy values.
     */
    private HashMap<ASTNode<?>,String> _secrecy = new HashMap<>();

    /**
     * Contains the secrecy lattice either given by the user or a default. (default is: Low < High)
     */
    private SecrecyLatticeStructure secrecyLatticeStructure;

    /**
     * Visitor for statements that performs typechecking for the secrecy rules.
     */
    private SecrecyStmtVisitor stmtVisitor;  

    /**
     * List holds entries for confidentiality levels if evaluated at a point in time it is the current secrecylevel. 
     */
    private LinkedList<ProgramCountNode> programConfidentiality;

    /**
     * Constructor for the secrecy expression visitor that retrieves the secrecyvalues of different expressions.
     * @param _secrecy - the hashmap that links ASTNode's to their assigned secrecylevel.
     * @param secrecyLatticeStructure - the datastructure that holds the information for the lattice.
     * @param programConfidentiality - the list for the confidentiality at a certain point in time.
     * @param stmtVisitor - the visitor that called this so that we can visit statements with it.
     */
    public SecrecyExpVisitor(HashMap<ASTNode<?>,String> _secrecy, SecrecyLatticeStructure secrecyLatticeStructure, LinkedList<ProgramCountNode> programConfidentiality, SecrecyStmtVisitor stmtVisitor) {
        this._secrecy = _secrecy;
        this.secrecyLatticeStructure = secrecyLatticeStructure;
        this.programConfidentiality = programConfidentiality;
        this.stmtVisitor = stmtVisitor;
    }

    /**
     * Visit function for expressions tries to return an attached secrecylevel.
     * Dependinding on the kind of expression the matching implementation of visit is called.
     * @param expression - the expression for which we want to retrieve the secrecylevel.
     * @return - the join of the expressions secrecylevel and the secrecylevel of the current program point.
     */
    public String visit(Exp expression){
        return secrecyLatticeStructure.join(secrecyLatticeStructure.getMinSecrecyLevel(), secrecyLatticeStructure.evaluateListLevel(programConfidentiality));
    }

    /**
     * Visit function for additive expressions.
     * Set combine as the join of the left and right values.
     * 
     * @param addAddExp - the expression for which we want to retrieve the secrecylevel.
     * @return - the join of the combine and the secrecylevel of the current program point.
     */
    public String visit(AddAddExp addAddExp) {
        
        String leftLevel = addAddExp.getLeft().accept(this);
        String rightLevel = addAddExp.getRight().accept(this);
        String combined = secrecyLatticeStructure.join(leftLevel, rightLevel);

        return secrecyLatticeStructure.join(combined, secrecyLatticeStructure.evaluateListLevel(programConfidentiality));
    }

    /**
     * Visit function for subtractive expressions.
     * Set combine as the join of the left and right values.
     * 
     * @param subAddExp - the expression for which we want to retrieve the secrecylevel.
     * @return - the join of the combine and the secrecylevel of the current program point.
     */
    public String visit(SubAddExp subAddExp) {
        
        String leftLevel = subAddExp.getLeft().accept(this);
        String rightLevel = subAddExp.getRight().accept(this);
        String combined = secrecyLatticeStructure.join(leftLevel, rightLevel);

        return secrecyLatticeStructure.join(combined, secrecyLatticeStructure.evaluateListLevel(programConfidentiality));
    }

    /**
     * Visit function for multiplicative expressions.
     * Set combine as the join of the left and right values.
     * 
     * @param multMultExp - the expression for which we want to retrieve the secrecylevel.
     * @return - the join of the combine and the secrecylevel of the current program point.
     */
    public String visit(MultMultExp multMultExp) {
        
        String leftLevel = multMultExp.getLeft().accept(this);
        String rightLevel = multMultExp.getRight().accept(this);
        String combined = secrecyLatticeStructure.join(leftLevel, rightLevel);

        return secrecyLatticeStructure.join(combined, secrecyLatticeStructure.evaluateListLevel(programConfidentiality));
    }

    /**
     * Visit function for divisive expressions.
     * Set combine as the join of the left and right values.
     * 
     * @param divMultExp - the expression for which we want to retrieve the secrecylevel.
     * @return - the join of the combine and the secrecylevel of the current program point.
     */
    public String visit(DivMultExp divMultExp) {
        
        String leftLevel = divMultExp.getLeft().accept(this);
        String rightLevel = divMultExp.getRight().accept(this);
        String combined = secrecyLatticeStructure.join(leftLevel, rightLevel);

        return secrecyLatticeStructure.join(combined, secrecyLatticeStructure.evaluateListLevel(programConfidentiality));
    }

    /**
     * Visit function for modulative expressions.
     * Set combine as the join of the left and right values.
     * 
     * @param modMultExp - the expression for which we want to retrieve the secrecylevel.
     * @return - the join of the combine and the secrecylevel of the current program point.
     */
    public String visit(ModMultExp modMultExp) {
        
        String leftLevel = modMultExp.getLeft().accept(this);
        String rightLevel = modMultExp.getRight().accept(this);
        String combined = secrecyLatticeStructure.join(leftLevel, rightLevel);

        return secrecyLatticeStructure.join(combined, secrecyLatticeStructure.evaluateListLevel(programConfidentiality));
    }

    /**
     * Visit function for and expressions.
     * Set combine as the join of the left and right values.
     * 
     * @param andExp - the expression for which we want to retrieve the secrecylevel.
     * @return - the join of the combine and the secrecylevel of the current program point.
     */
    public String visit(AndBoolExp andExp) {
        
        String leftLevel = andExp.getLeft().accept(this);
        String rightLevel = andExp.getRight().accept(this);
        String combined = secrecyLatticeStructure.join(leftLevel, rightLevel);

        return secrecyLatticeStructure.join(combined, secrecyLatticeStructure.evaluateListLevel(programConfidentiality));
    }

    /**
     * Visit function for or expressions.
     * Set combine as the join of the left and right values.
     * 
     * @param orExp - the expression for which we want to retrieve the secrecylevel.
     * @return - the join of the combine and the secrecylevel of the current program point.
     */    
    public String visit(OrBoolExp orExp) {
        
        String leftLevel = orExp.getLeft().accept(this);
        String rightLevel = orExp.getRight().accept(this);
        String combined = secrecyLatticeStructure.join(leftLevel, rightLevel);

        return secrecyLatticeStructure.join(combined, secrecyLatticeStructure.evaluateListLevel(programConfidentiality));
    }

    /**
     * Visit function for equality expressions.
     * Set combine as the join of the left and right values.
     * 
     * @param eqExp - the expression for which we want to retrieve the secrecylevel.
     * @return - the join of the combine and the secrecylevel of the current program point.
     */
    public String visit(EqExp eqExp) {
        
        String leftLevel = eqExp.getLeft().accept(this);
        String rightLevel = eqExp.getRight().accept(this);
        String combined = secrecyLatticeStructure.join(leftLevel, rightLevel);

        return secrecyLatticeStructure.join(combined, secrecyLatticeStructure.evaluateListLevel(programConfidentiality));
    }

    /**
     * Visit function for inequality expressions.
     * Set combine as the join of the left and right values.
     * 
     * @param notEqExp - the expression for which we want to retrieve the secrecylevel.
     * @return - the join of the combine and the secrecylevel of the current program point.
     */
    public String visit(NotEqExp notEqExp) {
        
        String leftLevel = notEqExp.getLeft().accept(this);
        String rightLevel = notEqExp.getRight().accept(this);
        String combined = secrecyLatticeStructure.join(leftLevel, rightLevel);

        return secrecyLatticeStructure.join(combined, secrecyLatticeStructure.evaluateListLevel(programConfidentiality));
    }

    /**
     * Visit function for less than equals expressions.
     * Set combine as the join of the left and right values.
     * 
     * @param lessThanEqualsExp - the expression for which we want to retrieve the secrecylevel.
     * @return - the join of the combine and the secrecylevel of the current program point.
     */
    public String visit(LTEQExp lessThanEqualsExp) {

        String leftLevel = lessThanEqualsExp.getLeft().accept(this);
        String rightLevel = lessThanEqualsExp.getRight().accept(this);
        String combined = secrecyLatticeStructure.join(leftLevel, rightLevel);

        return secrecyLatticeStructure.join(combined, secrecyLatticeStructure.evaluateListLevel(programConfidentiality));
    }

    /**
     * Visit function for greater than equals expressions.
     * Set combine as the join of the left and right values.
     * 
     * @param greaterThanEqualsExp - the expression for which we want to retrieve the secrecylevel.
     * @return - the join of the combine and the secrecylevel of the current program point.
     */
    public String visit(GTEQExp greaterThanEqualsExp) {

        String leftLevel = greaterThanEqualsExp.getLeft().accept(this);
        String rightLevel = greaterThanEqualsExp.getRight().accept(this);
        String combined = secrecyLatticeStructure.join(leftLevel, rightLevel);

        return secrecyLatticeStructure.join(combined, secrecyLatticeStructure.evaluateListLevel(programConfidentiality));
    }
   
    /**
     * Visit function for less than expressions.
     * Set combine as the join of the left and right values.
     * 
     * @param lessThanExp - the expression for which we want to retrieve the secrecylevel.
     * @return - the join of the combine and the secrecylevel of the current program point.
     */
    public String visit(LTExp lessThanExp) {

        String leftLevel = lessThanExp.getLeft().accept(this);
        String rightLevel = lessThanExp.getRight().accept(this);
        String combined = secrecyLatticeStructure.join(leftLevel, rightLevel);

        return secrecyLatticeStructure.join(combined, secrecyLatticeStructure.evaluateListLevel(programConfidentiality));
    }

    /**
     * Visit function for greater than expressions.
     * Set combine as the join of the left and right values.
     * 
     * @param greaterThanExp - the expression for which we want to retrieve the secrecylevel.
     * @return - the join of the combine and the secrecylevel of the current program point.
     */
    public String visit(GTExp greaterThanExp) {

        String leftLevel = greaterThanExp.getLeft().accept(this);
        String rightLevel = greaterThanExp.getRight().accept(this);
        String combined = secrecyLatticeStructure.join(leftLevel, rightLevel);

        return secrecyLatticeStructure.join(combined, secrecyLatticeStructure.evaluateListLevel(programConfidentiality));
    }

    /**
     * Visit function for minus expressions.
     * 
     * @param minusExp - the expression for which we want to retrieve the secrecylevel.
     * @return - the join of the secrecylevel of the exp below and the secrecylevel of the current program point.
     */
    public String visit(MinusExp minusExp) {
        
        ASTNode<?> child = minusExp.getChild(0);

        if(child instanceof Exp expr) {
            return secrecyLatticeStructure.join(expr.accept(this), secrecyLatticeStructure.evaluateListLevel(programConfidentiality));
        }

        return null;
    }

    /** 
     * Visit function for negate expressions.
     * 
     * @param negExp - the expression for which we want to retrieve the secrecylevel.
     * @return - the join of the secrecylevel of the exp below and the secrecylevel of the current program point.
     */
    public String visit(NegExp negExp) {
        
        ASTNode<?> child = negExp.getChild(0);

        if(child instanceof Exp expr) {
            return secrecyLatticeStructure.join(expr.accept(this), secrecyLatticeStructure.evaluateListLevel(programConfidentiality));
        }

        return null;
    }

    /**
     * Visit function for var or field use expressions.
     * 
     * @param varOrFieldUse - the expression for which we want to retrieve the secrecylevel.
     * @return - the join of the secrecylevel of the variable or field and the secrecylevel of the current program point.
     * if there is no secrecy attached to the variable or field then use the lowest value from the lattice structure.
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
     * Visit function for get expressions.
     * When we have a get we remove the associated await change from the programConfidentiality list!
     * 
     * @param getExp - the expression for which we want to retrieve the secrecylevel.
     * @return - the lowest possible value from the lattice
     */
    public String visit(GetExp getExp) {

        ASTNode<?> target = (Exp) getExp.getChild(0);
        String targetString = target.toString();

        if(target instanceof VarOrFieldUse varUse)targetString = varUse.getName();
        
        for (ProgramCountNode node : programConfidentiality) {
            if (node.levelChangingNode.equals(targetString)) {
                programConfidentiality.remove(node);
            }
        }

        stmtVisitor.updateProgramPoint(programConfidentiality);
        return secrecyLatticeStructure.getMinSecrecyLevel();
    }

    /**
     * Visit function for async call expressions.
     * 
     * @param asyncCall - the expression for which we want to retrieve the secrecylevel.
     * @return - the join of the secrecylevel of the returnvalue of the called method and the secrecylevel of the current program point.
     */
    public String visit(AsyncCall asyncCall) {
        MethodSig calledMethod = asyncCall.getMethodSig();
        String secrecyLevel = _secrecy.get(calledMethod);
        if(secrecyLevel == null) secrecyLevel = secrecyLatticeStructure.getMinSecrecyLevel();
        return secrecyLevel;
    }

    /**
     * Visit function for sync call expressions.
     * 
     * @param syncCall - the expression for which we want to retrieve the secrecylevel.
     * @return - the join of the secrecylevel of the returnvalue of the called method and the secrecylevel of the current program point.
     */
    public String visit(SyncCall syncCall) {
        MethodSig calledMethod = syncCall.getMethodSig();
        String secrecyLevel = _secrecy.get(calledMethod);
        if(secrecyLevel == null) secrecyLevel = secrecyLatticeStructure.getMinSecrecyLevel();
        return secrecyLevel;
    }

    /**
     * Allows to update the current program secrecy list on a change.
     * @param newConfidentiality - the list but with the new changes.
     */
    public void updateProgramPoint(LinkedList<ProgramCountNode> newConfidentiality) {
        programConfidentiality = newConfidentiality;
    }
}