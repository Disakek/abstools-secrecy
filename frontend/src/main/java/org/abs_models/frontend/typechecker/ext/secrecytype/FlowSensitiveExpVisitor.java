/**
 * Copyright (c) 2009-2011, The HATS Consortium. All rights reserved. 
 * This file is licensed under the terms of the Modified BSD License.
 * Written by @Maximilian_Paul for questions please refer to uukln@student.kit.edu
 */
package org.abs_models.frontend.typechecker.ext;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Iterator;
import java.util.Set;

import org.abs_models.frontend.analyser.ErrorMessage;
import org.abs_models.frontend.analyser.TypeError;
import org.abs_models.frontend.analyser.SemanticConditionList;

import org.abs_models.frontend.ast.*;

/**
 * This class is used to extract the secrecylevels for the different expressions and enforce rules with it.
 */
public class FlowSensitiveExpVisitor extends SecrecyExpVisitor {

    /**
     * Stores mappings between ASTNode's (declarations) and the assigned maximum secrecy values.
     * Meaning e.g. a variable may never hold a value higher than it's value from this _maxSecrecy.
     */
    private HashMap<ASTNode<?>,String> _maxSecrecy = new HashMap<>();

    /**
     * Stores mappings between ASTNode's (declarations) and the assigned current secrecy values.
     * Meaning e.g. a variable may hold a vlaue smaller than it's max secrecy value which would allow certain actions. 
     */
    private HashMap<ASTNode<?>,String> _currentSecrecy = new HashMap<>();

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
     * The list for errors that we can add to if a rule isn't respected.
     */
    private final SemanticConditionList errors;

    private Model m;
    private LinkedList<CalledMethod> methodsCallingOthers = new LinkedList<CalledMethod>();

    /**
     * Constructor for the secrecy expression visitor that retrieves the secrecyvalues of different expressions.
     * @param _maxSecrecy - the hashmap that links ASTNode's to their assigned secrecylevel.
     * @param secrecyLatticeStructure - the datastructure that holds the information for the lattice.
     * @param programConfidentiality - the list for the confidentiality at a certain point in time.
     * @param stmtVisitor - the visitor that called this so that we can visit statements with it.
     */
    public FlowSensitiveExpVisitor(Model m, HashMap<ASTNode<?>,String> _maxSecrecy, HashMap<ASTNode<?>,String> _currentSecrecy, SecrecyLatticeStructure secrecyLatticeStructure, SemanticConditionList errors, LinkedList<ProgramCountNode> programConfidentiality, SecrecyStmtVisitor stmtVisitor, LinkedList<CalledMethod> methodsCallingOthers) {
        
        super(m, secrecyLatticeStructure, errors, programConfidentiality, stmtVisitor, methodsCallingOthers);
        this._maxSecrecy = _maxSecrecy;
        this._currentSecrecy = _currentSecrecy;
        /*
        this.m = m;
        this._maxSecrecy = _maxSecrecy;
        this._currentSecrecy = _currentSecrecy;
        this.secrecyLatticeStructure = secrecyLatticeStructure;
        this.errors = errors;
        this.programConfidentiality = programConfidentiality;
        this.stmtVisitor = stmtVisitor;
        this.methodsCallingOthers = methodsCallingOthers;
        */
    }

    /**
     * Visit function for expressions tries to return an attached secrecylevel.
     * Dependinding on the kind of expression the matching implementation of visit is called.
     * @param expression - the expression for which we want to retrieve the secrecylevel.
     * @return - the join of the expressions secrecylevel and the secrecylevel of the current program point.
     */
    public String visit(Exp expression) {

        return secrecyLatticeStructure.evaluateListLevel(programConfidentiality);
    }


    public String visit(Binary binaryExp) {
        
        String leftLevel = binaryExp.getLeft().accept(this);
        String rightLevel = binaryExp.getRight().accept(this);
        String combined = secrecyLatticeStructure.join(leftLevel, rightLevel);

        return secrecyLatticeStructure.join(combined, secrecyLatticeStructure.evaluateListLevel(programConfidentiality));
    }

    public String visit(Unary unaryExp) {

        ASTNode<?> child = unaryExp.getChild(0);
        String listLevel = secrecyLatticeStructure.evaluateListLevel(programConfidentiality);

        if(child instanceof Exp expr) {
            return secrecyLatticeStructure.join(expr.accept(this), listLevel);
        }

        return listLevel;
    }

    /**
     * Visit function for var or field use expressions.
     * 
     * @param varOrFieldUse - the expression for which we want to retrieve the secrecylevel.
     * @return - the join of the secrecylevel of the variable or field and the secrecylevel of the current program point.
     * if there is no secrecy attached to the variable or field then use the lowest value from the lattice structure (which is guaranteed to be >= minimum secrecy level of the lattice).
     */
    public String visit(VarOrFieldUse varOrFieldUse) {

        ASTNode<?> variable = varOrFieldUse.getDecl();
        String variableSecrecy = _currentSecrecy.get(variable);
        String listLevel = secrecyLatticeStructure.evaluateListLevel(programConfidentiality);

        if (variableSecrecy != null) {
            return secrecyLatticeStructure.join(variableSecrecy, secrecyLatticeStructure.evaluateListLevel(programConfidentiality));
        }

        return listLevel;
    }

    //TODO remove the prints and write the doc
    public String visit(NewExp newExp) {

        // When we create a new exp of a class we have to ensure that for all parameters the maximum declared secrecy level is respected
        String listLevel = secrecyLatticeStructure.evaluateListLevel(programConfidentiality);
        String className = newExp.getClassName();
        ClassDecl classToCreateOf = findClassByName(m, className);
        List<PureExp> calledParams = newExp.getParamList(); //the input parameters
        List<ParamDecl> declaredParams = classToCreateOf.getParamList(); //the declared parameters

        if(calledParams.getNumChild() > 0) {

            for(int i = 0; i < calledParams.getNumChild(); i++) {

                //Retrieve the _currentSecrecy for the input parameters
                String calledSecrecy = this.visit(calledParams.getChild(i));
                //Retrieve the _maxSecrecy for the class parameters of the class we try to create an object for                
                String definedSecrecy = _maxSecrecy.get(declaredParams.getChild(i));
                
                if(definedSecrecy == null) { 
                    definedSecrecy = secrecyLatticeStructure.getMinSecrecyLevel();
                }

                Set<String> calledSecrecySet = secrecyLatticeStructure.getSetForSecrecyLevel(calledSecrecy);

                //Ensure that the _currentSecrecy smaller or equal to the _maxSecrecy otherwise add a type error
                if(!(definedSecrecy.equals(calledSecrecy) || calledSecrecySet.contains(definedSecrecy))) {
                    errors.add(new TypeError(newExp, ErrorMessage.SECRECY_PARAMETER_TO_HIGH, calledSecrecy, declaredParams.getChild(i).getName(), definedSecrecy));
                }
            }
        }        

        return listLevel;
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
     * Visit function for call expressions.
     * 
     * @param functionCall - the expression for which we want to retrieve the secrecylevel.
     * @return - the join of the secrecylevel of the returnvalue of the called method and the secrecylevel of the current program point.
     */
    public String visit(Call functionCall) {
        
        String secrecyLevel = null;
        String listLevel = secrecyLatticeStructure.evaluateListLevel(programConfidentiality);
        
        if (!(functionCall.getMethodSig() == null)) {
        
            MethodSig calledMethod = functionCall.getMethodSig();

            List<ParamDecl> parameterList = calledMethod.getParamList();
            List<PureExp> calledParams = functionCall.getParamList();
            int numberOfDefinedParameters = parameterList.getNumChild();

            //TODO check here wether the called method is secure (if the caller is in the same class - ThisExp)
            Exp caller = functionCall.getCallee();
            
            //Check if it's a ThisExp
            if(caller instanceof ThisExp callerIsThis) {
                
                //Get the declaring class
                ClassDecl implementingClass = findImplementingClassHelper(m, calledMethod);
                //
                if (implementingClass != null) {
                    MethodImpl calledMethodImpl = findMethodImpl(implementingClass, calledMethod);
                    if (calledMethodImpl != null) {
                        SecrecyAnnotationChecker.addCalledMethod(implementingClass, functionCall, calledMethodImpl, methodsCallingOthers);
                    }
                }
            }

            if(numberOfDefinedParameters > 0) {

                for(int i = 0; i < parameterList.getNumChild(); i++) {

                    String definedSecrecy = _maxSecrecy.get(parameterList.getChild(i));
                    String calledSecrecy = this.visit(calledParams.getChild(i));

                    if(definedSecrecy == null) { 
                        definedSecrecy = secrecyLatticeStructure.getMinSecrecyLevel();
                    }

                    Set<String> calledSecrecySet = secrecyLatticeStructure.getSetForSecrecyLevel(calledSecrecy);

                    if(!(definedSecrecy.equals(calledSecrecy) || calledSecrecySet.contains(definedSecrecy))) {
                        errors.add(new TypeError(functionCall, ErrorMessage.SECRECY_PARAMETER_TO_HIGH, calledSecrecy, parameterList.getChild(i).getName(), definedSecrecy));
                    }
                }
            }

            //TODO think about the _maxSecrecy/_currentSecrecy level here and what it will/would/should say
            //secrecyLevel = _maxSecrecy.get(calledMethod);
            secrecyLevel = _currentSecrecy.get(calledMethod);
            
            if (secrecyLevel != null) {
                return secrecyLatticeStructure.join(secrecyLevel, listLevel);
            }
        }

        return listLevel;
    }

    /**
     * Visit function fnApp expressions.
     * 
     * @param fnApp - the expression for which we want to retrieve the secrecylevel.
     * @return - the join of the secrecylevel of the variable or field and the secrecylevel of the current program point.
     * if there is no secrecy attached to the variable or field then use the lowest value from the lattice structure.
     */
    public String visit(FnApp fnApp) {

        List<PureExp> fnAppParameters = fnApp.getParamList();
        String secrecy = null;
        String listLevel = secrecyLatticeStructure.evaluateListLevel(programConfidentiality);

        for(PureExp fnAppParam : fnAppParameters) {

            String paramSecrecy = this.visit(fnAppParam);

            if (secrecy != null) {
                secrecy = secrecyLatticeStructure.join(secrecy, paramSecrecy);
            } else {
                secrecy = paramSecrecy;
            }
        }

        if (secrecy != null) {
            return secrecyLatticeStructure.join(secrecy, listLevel);
        }

        return listLevel;
    }

    private ClassDecl findImplementingClassHelper (Model m, MethodSig inMethod) {

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

    private MethodImpl findMethodImpl(ClassDecl parentClass, MethodSig inMethod) {
        for (MethodImpl method : parentClass.getMethods()) {
            if (method.getMethodSig() == inMethod) return method;
        }
        return null;
    }

    /**
     * Allows to update the current program secrecy list on a change.
     * @param newConfidentiality - the list but with the new changes.
     */
    public void updateProgramPoint(LinkedList<ProgramCountNode> newConfidentiality) {
        programConfidentiality = newConfidentiality;
    }

    public void updateCurrentSecrecy(HashMap<ASTNode<?>, String> newCurrentSecrecy) {
        this._currentSecrecy = newCurrentSecrecy;
    }
}
