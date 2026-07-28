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
import org.abs_models.frontend.analyser.TypeError;

/**
 * This class is used to extract the secrecylevels for the different statements and enforce rules with it.
 */
public class FlowInsensitiveStmtVisitor extends SecrecyStmtVisitor{

    /**
     * Stores mappings between ASTNode's (declarations) and the assigned secrecy values.
     */
    private HashMap<ASTNode<?>,String> _secrecy = new HashMap<>();
    
    /**
     * Visitor for expressions that performs typechecking for the secrecy rules.
     */
    private FlowInsensitiveExpVisitor ExpVisitor;               

    /**
     * Constructor for the SecrecyStmtVisitor.
     * @param _secrecy - the hashmap that links ASTNode's to their assigned secrecylevel.
     * @param secrecyLatticeStructure - the datastructure that holds the information for the lattice. 
     * @param errors - the error list that we can add typeerrors to.
     * @param programConfidentiality - the list for the confidentiality at a certain point in time.
     */
    public FlowInsensitiveStmtVisitor(Model m, HashMap<ASTNode<?>,String> _secrecy, SecrecyLatticeStructure secrecyLatticeStructure, SemanticConditionList errors,LinkedList<ProgramCountNode> programConfidentiality, LinkedList<CalledMethod> methodsCallingOthers) {
        
        super(m, secrecyLatticeStructure, errors, programConfidentiality, methodsCallingOthers); // ← must be first line
        this._secrecy = _secrecy;
        
        ExpVisitor = new FlowInsensitiveExpVisitor(m, _secrecy, secrecyLatticeStructure, errors, programConfidentiality, this, methodsCallingOthers);
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

        String LHSsecLevel = secrecyLatticeStructure.getMinSecrecyLevel();
        String RHSsecLevel = secrecyLatticeStructure.getMinSecrecyLevel();

        if(_secrecy.get(LHS) != null)LHSsecLevel = _secrecy.get(LHS);
        String rhsVisitorReturn = RhsExp.accept(ExpVisitor);
        if(rhsVisitorReturn != null)RHSsecLevel = rhsVisitorReturn;
        Set<String> LHScontainedIn = secrecyLatticeStructure.getSetForSecrecyLevel(LHSsecLevel);
        
        if(!hasDeclassify && LHScontainedIn.contains(RHSsecLevel)) {
            errors.add(new TypeError(assignStmt, ErrorMessage.SECRECY_LEAKAGE_ERROR_FROM_TO, RHSsecLevel, assignStmt.getValue().toString(), LHSsecLevel, assignStmt.getVar().getName()));
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
            //The print below can be used when one is not sure that the if-stmt's work properly
            //if(!ifNode.getSecrecyLevel().equals("Low")) System.out.println("Created new if stmt with secrecy level: " + ifNode.getSecrecyLevel());

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

            if(_secrecy.get(methodSig) != null)returnDefinitionLevel = _secrecy.get(methodSig);
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
        
        //We need to get the level here for the check because we can't find it in the usual list
        //until after this check is performed (I assume)
        //Assume lowest possible value
        String lhsLevel = secrecyLatticeStructure.getMinSecrecyLevel();
        
        //If there is an annotation extract it if it's for our secrecy annotation
        if (varDeclStmt.getAnnotationList() != null) {
            for (Annotation ann : varDeclStmt.getAnnotationList()) {
                if (ann instanceof TypedAnnotation typedAnn) {

                    ASTNode<?> valueNode = typedAnn.getChild(0);
                    ASTNode<?> nameNode  = typedAnn.getChild(1);

                    if ("Secrecy".equals(nameNode.toString()) && valueNode instanceof DataConstructorExp dataCon) {
                        String levelName = dataCon.getConstructor();

                        if (!hasDeclassify && !secrecyLatticeStructure.isValidLabel(levelName)) {
                            errors.add(new TypeError(typedAnn, ErrorMessage.SECRECY_WRONG_ANNOTATION_VALUE, levelName));
                            return;
                        }

                        //System.out.println("HERE NOW WORKS?"+varDeclStmt);
                        //System.out.println("Levelname: " + levelName);
                        lhsLevel = levelName;
                        if(levelName != null)_secrecy.put(varDecl, levelName);
                        break;
                    }
                }
            }
        }

        if(varDecl.hasInitExp()){
            Exp initExp = varDecl.getInitExp();
            String rhsLevel = initExp.accept(ExpVisitor);
            
            Set<String> setOfLHS = secrecyLatticeStructure.getSetForSecrecyLevel(lhsLevel);
        
            if(!hasDeclassify && setOfLHS.contains(rhsLevel)) {
                errors.add(new TypeError(varDeclStmt, ErrorMessage.SECRECY_LEAKAGE_ERROR_FROM_TO, rhsLevel, initExp.toString(), lhsLevel, varDecl.getName()));
            }
        }
    }

    /**
     * Visitor method calling the actual check.
     * Since the flow-sensitive approach needs checking more than once this is a helper needed for the abstract parent class.
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
     * This method is used to write an update to the current secrecy level hashmap.
     * Since the flow-insensitive implementation doesn't have a current secrecy it simply returns.
     * @param newCurrentSecrecy - the new current secrecy hashmap including the update.
     */
    public void updateCurrentSecrecy(HashMap<ASTNode<?>, String> newCurrentSecrecy) { return; }

}

