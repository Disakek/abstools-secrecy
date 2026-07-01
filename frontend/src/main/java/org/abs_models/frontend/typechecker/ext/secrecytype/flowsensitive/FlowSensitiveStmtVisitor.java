/**
 * Copyright (c) 2009-2011, The HATS Consortium. All rights reserved. 
 * This file is licensed under the terms of the Modified BSD License.
 * Written by @Maximilian_Paul for questions please refer to uukln@student.kit.edu
 */
package org.abs_models.frontend.typechecker.ext.secrecytype;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Set;

import org.abs_models.frontend.ast.*;
import org.abs_models.frontend.analyser.ErrorMessage;
import org.abs_models.frontend.analyser.SemanticConditionList;
import org.abs_models.frontend.analyser.SemanticCondition;
import org.abs_models.frontend.analyser.TypeError;

/**
 * This class is used to extract the secrecylevels for the different statements and enforce rules with it.
 */
public class FlowSensitiveStmtVisitor extends SecrecyStmtVisitor{

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
     * Visitor for expressions that performs typechecking for the secrecy rules.
     */
    private FlowSensitiveExpVisitor ExpVisitor;               

    /**
     * Constructor for the FlowSensitiveStmtVisitor.
     * @param _maxSecrecy - the hashmap that links ASTNode's to their assigned secrecylevel.
     * @param secrecyLatticeStructure - the datastructure that holds the information for the lattice. 
     * @param errors - the error list that we can add typeerrors to.
     * @param programConfidentiality - the list for the confidentiality at a certain point in time.
     */
    public FlowSensitiveStmtVisitor(Model m, HashMap<ASTNode<?>,String> _maxSecrecy, HashMap<ASTNode<?>,String> _currentSecrecy, SecrecyLatticeStructure secrecyLatticeStructure, SemanticConditionList errors,LinkedList<ProgramCountNode> programConfidentiality, LinkedList<CalledMethod> methodsCallingOthers) {
        
        super(m, secrecyLatticeStructure, errors, programConfidentiality, methodsCallingOthers); // ← must be first line
        this._maxSecrecy = _maxSecrecy;
        this._currentSecrecy = _currentSecrecy;
        
        ExpVisitor = new FlowSensitiveExpVisitor(m, _maxSecrecy, _currentSecrecy, secrecyLatticeStructure, errors, programConfidentiality, this, methodsCallingOthers);
    }

    /**
     * Visit function for assign statements. 
     * We check that for a:High and b:Low we never assign b = a however a = b, b = b or a = a is fine.
     * Secrecylevel of LHS has to be higher or equal to RHS. (default: Low)
     * @param assignStmt - the assign stmt that has to respect the assignment rule.
     */
    public void visit(AssignStmt assignStmt){

        Boolean hasDeclassify = isDeclassifying(assignStmt);
        ASTNode<?> LHS = assignStmt.getVar().getDecl();
        Exp RhsExp = assignStmt.getValue();

        String minSecLevel = secrecyLatticeStructure.getMinSecrecyLevel();
        String LHSsecLevel = minSecLevel;
        String RHSsecLevel = minSecLevel;

        String possibleLHSLevel = _maxSecrecy.get(LHS);
        String possibleRHSLevel = RhsExp.accept(ExpVisitor);

        if(possibleLHSLevel != null) {
            
            LHSsecLevel = possibleLHSLevel;
            
        } else {
            //When the lhs is not in the max hashmap then it has lowest possible value and thus it should also be written as that into the current!! 
            //We only do this however once it is used as otherwise it has a lot of overhead
            _currentSecrecy.put(LHS, minSecLevel);
        }

        if(possibleRHSLevel != null)RHSsecLevel = possibleRHSLevel;
        
        Set<String> LHScontainedIn = secrecyLatticeStructure.getSetForSecrecyLevel(LHSsecLevel);
        
        if(!hasDeclassify && LHScontainedIn.contains(RHSsecLevel)) {
            errors.add(new TypeError(assignStmt, ErrorMessage.SECRECY_LEAKAGE_ERROR_FROM_TO, RHSsecLevel, assignStmt.getValue().toString(), LHSsecLevel, assignStmt.getVar().getName()));
        }

        if(hasDeclassify) {
            String listLevel = secrecyLatticeStructure.evaluateListLevel(programConfidentiality);
            _currentSecrecy.put(LHS, secrecyLatticeStructure.join(LHSsecLevel, listLevel));
        } else {
            _currentSecrecy.put(LHS, RHSsecLevel); 
        }
        
    }

    /**
     * Visit function for await statements. 
     * When we check an await we need to add it to the programConfidentiality.
     * Once the await finishes we have a get so between await and get everything gets the higher program context.
     * The level of the "higher context" is defined by the level of the await's value.
     * @param awaitStmt - the await stmt that has to be handled similar to the if-stmt.
     * Handling performed by with the helper function handleGuards().
     */
    public void visit(AwaitStmt awaitStmt) {

        checkFieldsRespectMax(awaitStmt);

        Guard getGuard = awaitStmt.getGuard();
        handleGuards(getGuard);
    
    }

    /**
     * Visit function for expression statements. 
     * For an expression statement we want the expression below to be handled by the expression visitor.
     * @param expressionStmt - the expression stmt that should be visited by the expression visitor.
     */
    public void visit(ExpressionStmt expressionStmt) {
        Exp expStmtChild = expressionStmt.getExp();
        expStmtChild.accept(ExpVisitor);
        
        if(!containsFnAppHelper(expStmtChild)) {
            return;
        }

        FnApp possibleSecrecyFnApp = getFnAppHelper(expStmtChild);
        
        if(possibleSecrecyFnApp.getName().equals("secrecy")) {
            
            List<PureExp> fnAppParameters = possibleSecrecyFnApp.getParamList();
            
            if(fnAppParameters.getNumChild() == 2 && fnAppParameters.getChild(0) instanceof PureExp secPureExp) {
                String searchedVariable = secPureExp.toString();
                String secrecyFnAppValue = secPureExp.accept(ExpVisitor);
                    
                if (secrecyFnAppValue == null) {
                    secrecyFnAppValue = secrecyLatticeStructure.getMinSecrecyLevel();
                }

                String expectedLevelOfVariable = fnAppParameters.getChild(1).toString();
                expectedLevelOfVariable = expectedLevelOfVariable.replace("StringLiteral","").replace("(","").replace(")","");

                if(!secrecyLatticeStructure.isValidLabel(expectedLevelOfVariable)) {
                    //If the level is not valid then return that
                    errors.add(new TypeError(expressionStmt, ErrorMessage.SECRECY_LEVEL_NON_EXISTANT, expectedLevelOfVariable));
                } else {
                    if(!expectedLevelOfVariable.equals(secrecyFnAppValue)) {
                    //If the level is valid but it's not the same as the level of the exp return an error not equal!
                    errors.add(new TypeError(expressionStmt, ErrorMessage.SECRECY_FNAPP_NOT_EQUAL, searchedVariable.toString(), expectedLevelOfVariable, secrecyFnAppValue));
                    }
                }
            }
        }
    }

    /**
     * Visit function for if-statements. 
     * When we check the then (or else) block we might have a higher program point context.
     * The program point is defined by the one we had joined with the secrecylevel of the condition. (default: Low)
     * For this we add the secrecylevel of the condition to the programConfidentiality list and remove it once checked. 
     * @param ifStmt - the if-stmt that has to respect the if-rule.
     */
    public void visit(IfStmt ifStmt){

        Exp condition = ifStmt.getCondition();

        if(condition.accept(ExpVisitor) != null) {

            ProgramCountNode ifNode = new ProgramCountNode("ifStmt", condition.accept(ExpVisitor));

            programConfidentiality.add(ifNode);
            ExpVisitor.updateProgramPoint(programConfidentiality);

            Stmt thenCase = ifStmt.getThen();
            thenCase.accept(this);

            if(ifStmt.hasElse()) {
                Stmt elseCase = ifStmt.getElse();
                elseCase.accept(this);
            }

            programConfidentiality.remove(ifNode);
            ExpVisitor.updateProgramPoint(programConfidentiality);
        }
    }

    /**
     * Visit function for return statements. 
     * We check that for methoda:High and b:Low we never return b.
     * Secrecylevel of return has to be lower or equal the return secrecylevel of the method. (default: Low)
     * @param returnStmt - the return stmt that has to respect the returnstmt rule.
     */
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
            String possibleMethodSigSecrecy = _maxSecrecy.get(methodSig);

            if(possibleMethodSigSecrecy != null)returnDefinitionLevel = possibleMethodSigSecrecy;
        }

        if(returnExp instanceof Exp exp) {

            if(exp.accept(ExpVisitor) != null)returnActualLevel = exp.accept(ExpVisitor);

        }

        Set<String> methodReturnSet = secrecyLatticeStructure.getSetForSecrecyLevel(returnActualLevel);

        if(!(methodReturnSet.contains(returnDefinitionLevel)) && !(returnActualLevel.equals(returnDefinitionLevel))) {
            errors.add(new TypeError(returnStmt, ErrorMessage.SECRECY_LEAKAGE_ERROR_FROM_TO, returnActualLevel, "returnStmt", returnDefinitionLevel, "returnDefinition"));
        }
    }

    /**
     * Visit function for varDeclStmt statements.
     * We want to ensure that if a declaration has an initialization (exp) that we visit the init with the expression visitor.
     * @param varDeclStmt - the variable declaration statement that has to respect the rule.
     */
    public void visit(VarDeclStmt varDeclStmt) {

        Boolean hasDeclassify = isDeclassifying(varDeclStmt);
        
        VarDecl varDecl = varDeclStmt.getVarDecl();
        List<Annotation> annotations = varDeclStmt.getAnnotationList();
        for (Annotation ann : annotations) {
            if (ann instanceof TypedAnnotation typedAnn) {

                ASTNode<?> valueNode = typedAnn.getChild(0);
                ASTNode<?> nameNode  = typedAnn.getChild(1);

                if ("Secrecy".equals(nameNode.toString()) && valueNode instanceof DataConstructorExp dataCon) {
                    String levelName = dataCon.getConstructor();

                    if (!secrecyLatticeStructure.isValidLabel(levelName)) {
                        errors.add(new TypeError(typedAnn, ErrorMessage.SECRECY_WRONG_ANNOTATION_VALUE, levelName));
                        return;
                    }
                    _maxSecrecy.put(varDecl, levelName);
                }
            }
        }

        //We need to get the level here for the check because we can't find it in the usual list
        //until after this check is performed (I assume)
        //Assume lowest possible value
        String lhsLevel = _maxSecrecy.get(varDecl);
        if (lhsLevel == null) {
            lhsLevel = secrecyLatticeStructure.getMinSecrecyLevel();
        }

        if(varDecl.hasInitExp()){
            Exp initExp = varDecl.getInitExp();
            String rhsLevel = initExp.accept(ExpVisitor);
            
            Set<String> rhsLevelSet = secrecyLatticeStructure.getSetForSecrecyLevel(rhsLevel);
            
            if(!hasDeclassify && !(lhsLevel.equals(rhsLevel) || rhsLevelSet.contains(lhsLevel))) {
                
                errors.add(new TypeError(varDeclStmt, ErrorMessage.SECRECY_LEAKAGE_ERROR_FROM_TO, rhsLevel, initExp.toString(), lhsLevel, varDecl.getName()));
            
            }
        
            if(hasDeclassify) {
                String listLevel = secrecyLatticeStructure.evaluateListLevel(programConfidentiality);
                _currentSecrecy.put(varDecl, secrecyLatticeStructure.join(lhsLevel, listLevel));
            } else {
                _currentSecrecy.put(varDecl, lhsLevel);
            }
        
        }
    }

    /**
     * Visitor method calling the actual check.
     * This class requires the while stmt's to be checked more than once to find interloop leakages.
     * We don't know how often to unroll a loop but assume errors mostly appear in the first two iterations.
     * Thus to ensure some level of precision while keeping performance, we unroll it three times
     * @param whileStmt - the while stmt that has to respect the while rule.
     * It is very similar to the if-stmt (without an else).
     */
    public void visit(WhileStmt whileStmt) {

        //1.Iteration of the loop
        visitWhileHelper(whileStmt);
        //2.Iteration of the loop
        visitWhileHelper(whileStmt);
        //3.Iteration of the loop
        visitWhileHelper(whileStmt);

    }    

    /**
     * Visit function for while-statements. 
     * When we check the while block we might have a higher program point context.
     * The program point is defined by the one we had joined with the secrecylevel of the condition. (default: Low)
     * For this we add the secrecylevel of the condition to the programConfidentiality list and remove it once checked. 
     * @param whileStmt - the while stmt that has to respect the while rule.
     * It is very similar to the if-stmt (without an else).
     */
    public void visitWhileHelper(WhileStmt whileStmt) {

        Exp condition = whileStmt.getCondition();

        if(condition.accept(ExpVisitor) != null){
            ProgramCountNode whileNode = new ProgramCountNode("whileStmt", condition.accept(ExpVisitor));
            programConfidentiality.add(whileNode);

            ExpVisitor.updateProgramPoint(programConfidentiality);
            Stmt body = whileStmt.getBody();
            body.accept(this);

            programConfidentiality.remove(whileNode);
            ExpVisitor.updateProgramPoint(programConfidentiality);
        }
    }

    /**
     * Helper for the handling of the different guard kinds.
     * If the guard is an And call it recursive for the two sub guards. 
     * If it is an ExpGuard or ClaimGuard we want to add it to the programConfidentiality. (Remove only on the get)
     * @param inGuard - the gurad we want to handle.
     */
    private void handleGuards(Guard inGuard) {

        String inGuardChild = inGuard.getChild(0).toString();
        
        if (inGuard instanceof ExpGuard expGuard) {
  
            Exp awaitExpr = (Exp) expGuard.getChild(0);
            String getAwaitSecrecy = awaitExpr.accept(ExpVisitor);
            programConfidentiality.add(new ProgramCountNode(inGuardChild, getAwaitSecrecy));

        } else if (inGuard instanceof ClaimGuard claimGuard) {

            VarOrFieldUse awaitClaim = (VarOrFieldUse) claimGuard.getChild(0);
            String getAwaitSecrecy = awaitClaim.accept(ExpVisitor);

            programConfidentiality.add(new ProgramCountNode(inGuardChild, getAwaitSecrecy));

        } else if (inGuard instanceof AndGuard andGuard) {

            handleGuards(andGuard.getLeft());
            handleGuards(andGuard.getRight());
        }
        
        ExpVisitor.updateProgramPoint(programConfidentiality);
    }

    /**
     * Helper method to find the class containing a method (or the constructor) that contains a certain statement.
     * @param m - the model that is currently checked.
     * @param stmt - the statement of which we need to find the Class it is part of.
     * @return - null if we can't find a class that contains the method, otherwise the classDecl of that class.
     */
    private ClassDecl findContainingClass(Model m, Stmt stmt) {
        ASTNode<?> current = stmt;
        while (current != null) {
            if (current instanceof ClassDecl) {
                return (ClassDecl) current;
            }
            current = current.getParent();
        }
        
        return null;
    }

    /**
     * This method ensures that the fields of a class don't contain something with a higher secrecy level than allowed,
     * when reaching an await statement. This could otherwise lead to a leak by supsension and access through another process.
     * @param awaitStmt - the awaitStmt at which point we need to ensure the fields don't exceed their maximum allowed secrecy level.
     */
    private void checkFieldsRespectMax(AwaitStmt awaitStmt) {

        //In this class ensure that each field only contains something where the current secrecy is smaller or equal to the max secrecy  
        ClassDecl classContainingAwait = findContainingClass(m, awaitStmt);
        String minimumSecLevel = secrecyLatticeStructure.getMinSecrecyLevel();

        //In the case we were unable to find the class which contains the await I belive it has to be in the main block
        //In that case there are no fields and thus the check is not to be performed!
        if(classContainingAwait == null) return;

        for(FieldDecl fieldDecl : classContainingAwait.getFields()) {
            
            String currentSecOfField = _currentSecrecy.get(fieldDecl);
            String maxSecOfField = _maxSecrecy.get(fieldDecl);

            if(maxSecOfField == null) maxSecOfField = minimumSecLevel;
            if(currentSecOfField == null) currentSecOfField = minimumSecLevel;

            //Current has to be smaller or equal to max thus the set of current has to contain max 
            Set<String> setOfCurrent = secrecyLatticeStructure.getSetForSecrecyLevel(currentSecOfField); 
            
            if(!(setOfCurrent.contains(maxSecOfField)) && !(maxSecOfField.equals(currentSecOfField))) {
                errors.add(new TypeError(awaitStmt, ErrorMessage.SECRECY_AWAIT_FIELD_VIOLATION, fieldDecl.getName(), currentSecOfField, maxSecOfField));
            }
        }
    }

    /**
     * This method is used to write an update to the current secrecy level hashmap.
     * The update also has to be written to the instance of the expression visitor.
     * @param newCurrentSecrecy - the new current secrecy hashmap including the update.
     */
    public void updateCurrentSecrecy(HashMap<ASTNode<?>, String> newCurrentSecrecy) {
        this._currentSecrecy = newCurrentSecrecy;
        ExpVisitor.updateCurrentSecrecy(_currentSecrecy);
    }
}
