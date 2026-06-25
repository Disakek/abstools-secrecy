/**
 * Copyright (c) 2009-2011, The HATS Consortium. All rights reserved. 
 * This file is licensed under the terms of the Modified BSD License.
 * Written by @Maximilian_Paul for questions please refer to uukln@student.kit.edu
 */
package org.abs_models.frontend.typechecker.ext.secrecytype;

import java.util.HashMap;
import java.util.LinkedList;

import org.abs_models.frontend.ast.*;
import org.abs_models.frontend.analyser.SemanticConditionList;

public abstract class SecrecyStmtVisitor {

    /**
     * The model which we currently check for secrecytype errors.
     */
    protected Model m;

    /**
     * Contains the secrecy lattice either given by the user or a default. (default is: Low < High)
     */
    protected SecrecyLatticeStructure secrecyLatticeStructure;

    /**
     * The error list to which we add type errors if there are any.
     */
    protected SemanticConditionList errors;

    /**
     * The list describing the current secrecy level of a certain program point.
     * Use the evaluateListLevel method to calculate the secrecy level at any point of time.
     * Evaluation is done joining all levels together.
     */
    protected LinkedList<ProgramCountNode> programConfidentiality;

    /**
     * The list of methods that contain a call to another method of the same class.
     * Used in order to add errors if one of the calls is to an insecure method. 
     */
    protected LinkedList<CalledMethod> methodsCallingOthers;

    /**
     * The secrecy stmt visitor is an abstract class to allow for different implementations.
     * Depending on whether the analysis is flow-sensitive or flow-insensitive a subclass implements this to be used.
     * Extending classes of this implement different rules that are applied to check for type errors.
     *  
     * @param m - the model which we currently check for type errors.
     * @param secrecyLatticeStructure - the current lattice structure.
     * @param errors - the current error list to which we add ours.
     * @param programConfidentiality - the current list describing the program point. 
     * @param methodsCallingOthers - the current list of methods containing a call to another possiby insecure method.
     */
    public SecrecyStmtVisitor(Model m, SecrecyLatticeStructure secrecyLatticeStructure, SemanticConditionList errors,LinkedList<ProgramCountNode> programConfidentiality, LinkedList<CalledMethod> methodsCallingOthers) {
        this.m = m;
        this.secrecyLatticeStructure = secrecyLatticeStructure;
        this.errors = errors;
        this.programConfidentiality = programConfidentiality;
        this.methodsCallingOthers = methodsCallingOthers;
    }

    /**
     * Visit function for general statements. We check every statement in special so this one has no use.
     * It is instead overwritten by one method per different statement type.
     * @param stmt - the general stmt which we want to visit.
     */
    public void visit(Stmt stmt) { 
        
        if (stmt instanceof Block b) { this.visit(b); }

        return;
    }   

    /**
     * Visit function for block statements. We check every statement in the block with this visitor.
     * @param blockStmt - the blockstmt from which we want to visit each stmt.
     */
    public void visit(Block blockStmt) {
        for(Stmt stmt : blockStmt.getStmtList()) {
            stmt.accept(this);
        }
    }

    /**
     * The abstract class for visiting assign statements.
     * Different analysis types may implement different rules for the type and it's errors.
     * @param assignStmt - the assign statement to visit.
     */
    public abstract void visit(AssignStmt assignStmt);

    /**
     * The abstract class for visiting await statements.
     * Different analysis types may implement different rules for the type and it's errors.
     * @param awaitStmt - the await statement to visit.
     */
    public abstract void visit(AwaitStmt awaitStmt);

    /**
     * The abstract class for visiting expression statements.
     * Different analysis types may implement different rules for the type and it's errors.
     * @param expressionStmt - the expression statement to visit.
     */
    public abstract void visit(ExpressionStmt expressionStmt);

    /**
     * The abstract class for visiting if-statements.
     * Different analysis types may implement different rules for the type and it's errors.
     * @param ifStmt - the if-statement to visit.
     */
    public abstract void visit(IfStmt ifStmt);

    /**
     * The abstract class for visiting return statements.
     * Different analysis types may implement different rules for the type and it's errors.
     * @param returnStmt - the return statement to visit.
     */
    public abstract void visit(ReturnStmt returnStmt);

    /**
     * The abstract class for visiting variable declaration statements.
     * Different analysis types may implement different rules for the type and it's errors.
     * @param varDeclStmt - the variable declaration statement to visit.
     */
    public abstract void visit(VarDeclStmt varDeclStmt);

    /**
     * The abstract class for visiting while-statements.
     * This class is only used as helper to call the visitWhileHelper in different ways.
     * E.g. a flow-sensitive analysis requires a while-statement to be called multiple times.
     * @param whileStmt - the while-statement to visit.
     */
    public abstract void visit(WhileStmt whileStmt);

    /**
     * The abstract class actually implementing the check for the while-statements. 
     * Different analysis types may implement different rules for the type and it's errors.
     * @param whileStmt - the while-statement to visit.
     */
    public abstract void visitWhileHelper(WhileStmt whileStmt);

    /**
     * Allows to update the current program secrecy list on a change.
     * @param newConfidentiality - the list but with the new changes.
     */
    public void updateProgramPoint(LinkedList<ProgramCountNode> newConfidentiality) {
        programConfidentiality = newConfidentiality;
    }

    /**
     * A helper method that helps checking if an expression contains a FnApp on any level.
     * @param expression - the expression to check.
     * @return - true if there is an FnApp somewhere on the expression, false otherwise
     */
    public boolean containsFnAppHelper(Exp expression) {

        if(expression instanceof FnApp) {
            return true;
        }

        if(expression instanceof Unary unaryExp) {
            containsFnAppHelper(unaryExp);
        } else if(expression instanceof Binary binaryExp) {
            if (containsFnAppHelper(binaryExp.getLeft()) || (containsFnAppHelper(binaryExp.getRight()))) {
                return true;
            }
        }

        return false;
    }
    
    /**
     * A helper method to retrieve the FnApp from an expression assuming there is one.
     * @param expression - the expression to retrieve the FnApp from.
     * @return - the FnApp or null if there is none.
     */
    public FnApp getFnAppHelper(Exp expression) {

        if(expression instanceof FnApp fnapp) {
            return fnapp;
        }

        if(expression instanceof Unary unaryExp) {
            getFnAppHelper(unaryExp);
        } else if(expression instanceof Binary binaryExp) {
            if (containsFnAppHelper(binaryExp.getLeft())) {
                getFnAppHelper(binaryExp.getRight());
            } else if (containsFnAppHelper(binaryExp.getRight())) {
                getFnAppHelper(binaryExp.getRight());
            }
        }

        return null;
    }

    /**
     * Helper method to findout if a statement is supposed to declassify some secret information.
     * @param stmt - the statement which might be declassifying
     * @return - true if there is a declassify annotation attached to the statement, false otherwise
     */
    public Boolean isDeclassifying(Stmt stmt) {
        
        List<Annotation> annotations = stmt.getAnnotationList();

        if(annotations != null && annotations.getNumChild() > 0) {

            for (Annotation ann : annotations) {

                Exp value = ann.getValue();

                if(value instanceof DataConstructorExp dataCon) {

                    if(("Declassify").equals(dataCon.getConstructor())) {
                        //System.out.println("DataCon is Declassify");
                        return true;
                    }
                    
                }
            }
        }

        return false;
    }

    /**
     * This method is used to write an update to the current secrecy level hashmap.
     * Since the flow-insensitive implementation doesn't have a current secrecy it simply returns.
     * @param newCurrentSecrecy - the new current secrecy hashmap including the update.
     */
    public abstract void updateCurrentSecrecy(HashMap<ASTNode<?>, String> newCurrentSecrecy);
}
