/**
 * Copyright (c) 2009-2011, The HATS Consortium. All rights reserved. 
 * This file is licensed under the terms of the Modified BSD License.
 * Written by @Maximilian_Paul for questions please refer to uukln@student.kit.edu
 */
package org.abs_models.frontend.typechecker.ext.secrecytype;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

import org.abs_models.frontend.ast.*;
import org.abs_models.frontend.analyser.SemanticConditionList;

public abstract class SecrecyExpVisitor {

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
     * The statement visitor needed to reupdate the program point.
     */
    protected SecrecyStmtVisitor stmtVisitor;

    /**
     * This abstract class provides a parent for secrecy expression visitors.
     * These allow for evaluation of secrecy levels for expressions by visiting the statements.
     * 
     * @param m - the model we are currently type checking.
     * @param secrecyLatticeStructure - the currently used secrecy lattice.
     * @param errors - the current type errors where we can add potential ones.
     * @param programConfidentiality - a linkedlist describing the secrecy level of each program point.
     * @param stmtVisitor - the corresponding statement visitor 
     * @param methodsCallingOthers - a current list of methods that contain a call to another method.
     */
    public SecrecyExpVisitor(Model m, SecrecyLatticeStructure secrecyLatticeStructure, SemanticConditionList errors, LinkedList<ProgramCountNode> programConfidentiality, SecrecyStmtVisitor stmtVisitor, LinkedList<CalledMethod> methodsCallingOthers) {
        this.m = m;
        this.secrecyLatticeStructure = secrecyLatticeStructure;
        this.errors = errors;
        this.programConfidentiality = programConfidentiality;
        this.stmtVisitor = stmtVisitor;
        this.methodsCallingOthers = methodsCallingOthers;
    }
    
    /**
     * Visit function for expressions tries to return an attached secrecylevel.
     * Dependinding on the kind of expression the matching implementation of visit is called.
     * @param expression - the expression for which we want to retrieve the secrecylevel.
     * @return - the join of the expression's secrecylevel and the secrecylevel of the current program point.
     */
    public String visit(Exp expression) {

        if (expression instanceof VarOrFieldUse v) { return this.visit(v); }
        else if (expression instanceof Binary b) { return this.visit(b); }
        else if (expression instanceof Unary u)  { return this.visit(u); }
        else if (expression instanceof FnApp f)  { return this.visit(f); }
        else if (expression instanceof GetExp g) { return this.visit(g); }
        else if (expression instanceof NewExp n) { return this.visit(n); }
                   
        return secrecyLatticeStructure.evaluateListLevel(programConfidentiality);
    }

    /**
     * Visit function for binary expressions tries to return an attached secrecylevel.
     * For a binary expression both left and right are checked for secrecy levels and findings are joined together.
     * @param binaryExp - the binaryExp for which we want to retrieve the secrecylevel.
     * @return - the join of the binary expressions secrecylevel and the secrecylevel of the current program point.
     */
    public String visit(Binary binaryExp) {
        
        String leftLevelAccept = binaryExp.getLeft().accept(this);
        String rightLevel = binaryExp.getRight().accept(this);
        String combined = secrecyLatticeStructure.join(leftLevelAccept, rightLevel);

        return secrecyLatticeStructure.join(combined, secrecyLatticeStructure.evaluateListLevel(programConfidentiality));
    }

    /**
     * Visit function for unary expressions tries to return an attached secrecylevel.
     * @param unaryExp - the unaryExp for which we want to retrieve the secrecylevel.
     * @return - the join of the unary expression's secrecylevel and the secrecylevel of the current program point.
     */
    public String visit(Unary unaryExp) {

        ASTNode<?> child = unaryExp.getChild(0);
        String listLevel = secrecyLatticeStructure.evaluateListLevel(programConfidentiality);

        if(child instanceof Exp expr) {
            return secrecyLatticeStructure.join(expr.accept(this), listLevel);
        }

        return listLevel;
    }

    /**
     * Visit function for the varOrFieldUse expression. Has to be implemented by a subclass.
     * Depending on the analysis type and rule implementations may differ.
     * @param varOrFieldUse - the variable of field usage expression to be visited.
     * @return - the join of the unary expression's secrecylevel and the secrecylevel of the current program point.
     */
    public abstract String visit(VarOrFieldUse varOrFieldUse);

    public abstract String visit(NewExp newExp);

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
        String varUseSecrecy = null;
        String listLevel = secrecyLatticeStructure.evaluateListLevel(programConfidentiality);

        if(target instanceof VarOrFieldUse varUse) {
            targetString = varUse.getName();
            varUseSecrecy = this.visit(varUse);
        }
        
        Iterator<ProgramCountNode> iter = programConfidentiality.iterator();
        while (iter.hasNext()) {
            ProgramCountNode node = iter.next();
            if (node.levelChangingNode.equals(targetString)) {
                iter.remove();
            }
        }

        stmtVisitor.updateProgramPoint(programConfidentiality);

        String minLevel = secrecyLatticeStructure.join(secrecyLatticeStructure.getMinSecrecyLevel(), varUseSecrecy);

        return secrecyLatticeStructure.join(minLevel, secrecyLatticeStructure.evaluateListLevel(programConfidentiality));
    }

    /**
     * Visit function for the call expression. Has to be implemented by a subclass.
     * Depending on the analysis type and rule implementations may differ.
     * @param functionCall - the call expression to be visited.
     * @return - the join of the call expression's secrecylevel and the secrecylevel of the current program point.
     */
    public abstract String visit(Call functionCall);

    /**
     * Visit function fnApp expressions.
     * 
     * @param fnApp - the expression for which we want to retrieve the secrecylevel.
     * @return - the join of the secrecylevel of the variable or field and the secrecylevel of the current program point.
     * if there is no secrecy attached to the variable or field then use the lowest value from the lattice structure.
     */
    public String visit(FnApp fnApp) {
        List<PureExp> fnAppParameters = fnApp.getParamList();
        String listLevel = secrecyLatticeStructure.evaluateListLevel(programConfidentiality);
        String secrecy = listLevel;
        
        for(PureExp fnAppParam : fnAppParameters) {

            String paramSecrecy = this.visit(fnAppParam);

            if (paramSecrecy != null) {
                secrecy = secrecyLatticeStructure.join(secrecy, paramSecrecy);
            }
        }

        return secrecy;
    }

    /**
     * A helper method that finds a class by it's classname.
     * @param m - the model we currently check and search the class in.
     * @param className - the name of the class we search for (unique).
     * @return - the classDecl of the class with the searched className or null if there is none.
     */
    public ClassDecl findClassByName (Model m, String className) {

        ClassDecl result = null;

        for (CompilationUnit cu : m.getCompilationUnits()) {
            for (ModuleDecl moduleDecl : cu.getModuleDecls()) {
                for (Decl decl : moduleDecl.getDecls()) {
                    if (decl instanceof ClassDecl classDecl) {
                        if (classDecl.getName().equals(className)) {
                            return classDecl;
                        }
                    }
                }
            }
        }

        return null;
    }

    /**
     * Helper method that given the model that is currently checked tries to find the class that contains a certain method implementation.
     * @param m - the model that is currently checked.
     * @param inMethod - the method that search the class for.
     * @return - the classDecl of the class that contains the method implementation for inMethod, null if it can't be found. 
     */
    public ClassDecl findImplementingClassHelper (Model m, MethodSig inMethod) {

        ClassDecl result = null;

        for (CompilationUnit cu : m.getCompilationUnits()) {
            for (ModuleDecl moduleDecl : cu.getModuleDecls()) {
                for (Decl decl : moduleDecl.getDecls()) {
                    if (decl instanceof ClassDecl classDecl) {
                        result = classDecl;
                        for (MethodImpl method : classDecl.getMethods()) {
                            if(inMethod == method.getMethodSig()) {
                                return result;
                            }
                        }
                    }
                }
            }
        }

        return null;
    }

    /**
     * Helper method that finds a certain method implementation of a given class.
     * @param parentClass - the class that should contain the implementation of a method
     * @param inMethod - the signature of the method we want the implementation for
     * @return - the ast node of the methods implementation for the searched method, null if it can't be found.
     */
    public MethodImpl findMethodImpl(ClassDecl parentClass, MethodSig inMethod) {
        for (MethodImpl method : parentClass.getMethods()) {
            if (method.getMethodSig() == inMethod) return method;
        }
        return null;
    }

    /**
     * Allows to update the current program secrecy list on a change.
     * @param newConfidentiality - the list describing the secrecy level of a program point but with the new changes.
     */
    public void updateProgramPoint(LinkedList<ProgramCountNode> newConfidentiality) {
        programConfidentiality = newConfidentiality;
    }
}