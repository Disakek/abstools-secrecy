/**
 * Copyright (c) 2009-2011, The HATS Consortium. All rights reserved. 
 * This file is licensed under the terms of the Modified BSD License.
 * Written by @Maximilian_Paul for questions please refer to uukln@student.kit.edu
 */
package org.abs_models.frontend.typechecker.ext;

import java.util.HashSet;
import java.util.Set;
import java.util.HashMap;
import java.util.LinkedList;

import org.abs_models.frontend.analyser.AnnotationHelper;
import org.abs_models.frontend.analyser.ErrorMessage;
import org.abs_models.frontend.analyser.TypeError;
import org.abs_models.frontend.ast.*;
import org.abs_models.frontend.typechecker.Type;
import org.abs_models.frontend.typechecker.TypeAnnotation;
import org.abs_models.frontend.typechecker.ext.DefaultTypeSystemExtension;
import org.abs_models.frontend.typechecker.ext.AdaptDirection;
import org.abs_models.frontend.typechecker.ext.SecrecyStmtVisitor;

public class SecrecyAnnotationChecker extends DefaultTypeSystemExtension {

    //Is the mapping from an ASTNode (the declaration) to the assigned SecrecyValue
    private HashMap<ASTNode<?>,String> _secrecy = new HashMap<>();

    //todo is an idea for a current secrecy level storage
    //private HashMap<ASTNode<?>,String> _currentSecrecy = new HashMap<>();
    
    //Contains the secrecy lattice given by the user or the default (Low < High)
    SecrecyLatticeStructure secrecyLatticeStructure;
    
    //Is the visitor for all Stmts that typechecks the implemented rules    
    SecrecyStmtVisitor visitor;               

    LinkedList<ProgramCountNode> programConfidentiality;
    
    protected SecrecyAnnotationChecker(Model m) {
        super(m);

        programConfidentiality = new LinkedList<ProgramCountNode>();

        if (m.secrecyLatticeStructure != null) {
            secrecyLatticeStructure = m.secrecyLatticeStructure;
            //Set the basic starting secrecy
            programConfidentiality.add(new ProgramCountNode("default", secrecyLatticeStructure.getMinSecrecyLevel()));
        }
    }

    @Override
    public void checkModel(Model model) {

        //First pass of all the code to extract the secrecy annotations and populate _secrecy
        firstExtractionPhasePass(model); 

        visitor = new SecrecyStmtVisitor(_secrecy, secrecyLatticeStructure, errors, programConfidentiality);

        //Second pass to enforce all the typerules
        secondTypecheckPhasePass(model); 
        
        System.out.println("Print new annotated Values: " + _secrecy.toString());
        System.out.println("Print all Levels: " + secrecyLatticeStructure.getSecrecyLevels().toString());
        System.out.println("Print the order" + secrecyLatticeStructure.getLatticeOrder().toString());
        System.out.println("Confidentiality of current program point is: " + programConfidentiality.getLast().getSecrecyLevel());
    }

    private void firstExtractionPhasePass(Model model){
        for (CompilationUnit cu : model.getCompilationUnits()) {
            for (ModuleDecl moduleDecl : cu.getModuleDecls()) {
                for (Decl decl : moduleDecl.getDecls()) {
                    if (decl instanceof ClassDecl classDecl) {

                        //set for all methods declared in an implemented interface
                        Set<MethodSig> declaredInterfaceMethods = new HashSet<MethodSig>();
                        
                        if(classDecl.hasImplementedInterfaceUse()) {
                            
                            ASTNode<?> interfaceSet = classDecl.getImplementedInterfaceUseList();

                            //System.out.println("Has interface implementation: " + interfaceSet);

                            for(InterfaceTypeUse implementedInterface : classDecl.getImplementedInterfaceUseList()) {

                                InterfaceDecl usedInterfaceDecl = (InterfaceDecl) implementedInterface.getDecl();
                                //System.out.println(usedInterfaceDecl.getBodyList());
                                for(MethodSig declaredMethod : usedInterfaceDecl.getBodyList()) {

                                    //wenn sie ein secrecy value hat
                                    if(_secrecy.get(declaredMethod) != null){
                                        //System.out.println("Method: " + declaredMethod + " has Secrecy Value: " + _secrecy.get(declaredMethod));
                                        declaredInterfaceMethods.add(declaredMethod);
                                    }                                    
                                }

                            }
                        }
        
                        //Extract field annotations and store them to the secrecy hashmap
                        for(FieldDecl fieldDecl : classDecl.getFields()) {
                            String level = extractSecrecyValue(fieldDecl);
                            if(level != null)_secrecy.put(fieldDecl, level);
                        }
                        
                        //Extracts the annotation for methods of the class for their return values and their parameters
                        for (MethodImpl method : classDecl.getMethods()) {
                            
                            MethodSig methodSigNat = method.getMethodSig();

                            String Returnlevel = extractSecrecyValue(method.getMethodSig());
                            if(Returnlevel != null)_secrecy.put(method.getMethodSig(), Returnlevel);

                            for(ParamDecl parameter : method.getMethodSig().getParamList()) {
                                String Parameterlevel = extractSecrecyValue(parameter);
                                if(Parameterlevel != null)_secrecy.put(parameter, Parameterlevel);
                            }

                            //System.out.println("DeclaredInterfaceMethods " + declaredInterfaceMethods);

                            for(MethodSig declaredCandidate : declaredInterfaceMethods) {
                                if (compareMethodSignatures(method.getMethodSig(), declaredCandidate)) {
                                    System.out.println(method.getMethodSig() + " is implementation of " + declaredCandidate);
                                    checkRespectingSecrecyLevels(method.getMethodSig(), declaredCandidate);
                                }
                            }

                            Block block = method.getBlock();
                            for (Stmt stmt : block.getStmtList()) {
                                if (stmt instanceof VarDeclStmt varDeclStmt) {
                                    VarOrFieldDecl varDecl = varDeclStmt.getVarDecl();
    
                                    String varLevel = extractSecrecyValue(varDecl);
                                    if(varLevel != null) _secrecy.put(varDecl, varLevel);
                                }
                            }
                        }

                         //Extracts the annotation for methods of interfaces for their return values and their parameters
                    } else if (decl instanceof InterfaceDecl interfaceDecl) {
                        for (MethodSig methodSig : interfaceDecl.getBodyList()) {
                            
                            String Returnlevel = extractSecrecyValue(methodSig);
                            if(Returnlevel != null)_secrecy.put(methodSig, Returnlevel);

                            for(ParamDecl parameter : methodSig.getParamList()) {
                                String Parameterlevel = extractSecrecyValue(parameter);
                                if(Parameterlevel != null)_secrecy.put(parameter, Parameterlevel);
                            }
                        }
                    }
                }
            }
        }
    }

    private String extractSecrecyValue(ASTNode<?> declNode) {

        // All nodes that can have annotations implement TypeUse or have annotation lists
        List<Annotation> annotations = null;

        if (declNode instanceof ParamDecl param) {
            annotations = param.getAnnotationList();
        } else if (declNode instanceof FieldDecl field) {
            annotations = field.getTypeUse().getAnnotationList();
        } else if (declNode instanceof MethodSig sig) {
            annotations = sig.getReturnType().getAnnotationList();
        } else if (declNode instanceof TypedVarOrFieldDecl typedVar) {
            annotations = typedVar.getTypeUse().getAnnotationList();
        } 

        if (annotations == null) return null;

        for (Annotation ann : annotations) {
            if (ann instanceof TypedAnnotation typedAnn) {

                ASTNode<?> valueNode = typedAnn.getChild(0); // value
                ASTNode<?> nameNode  = typedAnn.getChild(1); // name

                if ("Secrecy".equals(nameNode.toString()) && valueNode instanceof DataConstructorExp dataCon) {
                    String levelName = dataCon.getConstructor();

                    // Check that the level exists in the lattice
                    if (!secrecyLatticeStructure.isValidLabel(levelName)) {
                        errors.add(new TypeError(typedAnn, ErrorMessage.WRONG_SECRECY_ANNOTATION_VALUE, levelName));
                        return null;
                    }

                    //System.out.println("Levelname: " + levelName);
                    return levelName;
                }
            }
        }
        return null;
    }

    private void secondTypecheckPhasePass(Model model){
        for (CompilationUnit cu : model.getCompilationUnits()) {
            for (ModuleDecl moduleDecl : cu.getModuleDecls()) {
                for (Decl decl : moduleDecl.getDecls()) {
                    if (decl instanceof ClassDecl classDecl) {
                        for (MethodImpl method : classDecl.getMethods()) {
                            Block block = method.getBlock();
                            for (Stmt stmt : block.getStmtList()) {
                                stmt.accept(visitor);
                            }
                        }
                    }
                }
            }
        }
    }

    private boolean compareMethodSignatures(MethodSig methodA, MethodSig methodB) {

        //name the same
        if(methodA.getName().equals(methodB.getName())){

            //returnvalue same type
            if(methodA.getReturnType().toString().equals(methodB.getReturnType().toString())){

                
                List<ParamDecl> paramListA = methodA.getParamList();
                List<ParamDecl> paramListB = methodB.getParamList();

                //same number of parameters
                if (paramListA.getNumChild() != paramListB.getNumChild()) {
                    return false;
                }

                LinkedList<ParamDecl> paramAList = new LinkedList<ParamDecl>();
                for(ParamDecl paramA:methodA.getParamList()){
                    paramAList.add(paramA);
                }
                LinkedList<ParamDecl> paramBList = new LinkedList<ParamDecl>();
                for(ParamDecl paramB:methodB.getParamList()){
                    paramBList.add(paramB);
                }

                //parameters 
                // if same name check same types => if true for all => same methodsig
                for(ParamDecl paramA : paramListA) {
                    for(ParamDecl paramB : paramListB) {
                        if (paramB.getName().equals(paramA.getName())){ 
                            if(paramB.getTypeUse().toString().equals(paramA.getTypeUse().toString())){
                                paramAList.remove(paramA);
                                paramBList.remove(paramB);
                            } else {
                                return false;
                            }
                        }
                    }
                }

                //each parameter was found in the other methodsig
                if(!paramAList.isEmpty() || !paramBList.isEmpty())return false;

            } else {
                return false;
            }
        } else {
            return false;
        }
        return true;
    }

    private void checkRespectingSecrecyLevels(MethodSig implementation, MethodSig definition) {

        String definitionLevel = _secrecy.get(definition);
        
        if(definitionLevel == null) {
            definitionLevel = secrecyLatticeStructure.getMinSecrecyLevel();
        }
        
        String implementationLevel = _secrecy.get(implementation);
        
        if(implementationLevel == null) {
            implementationLevel = secrecyLatticeStructure.getMinSecrecyLevel();
        }

        Set<String> implementationSet = secrecyLatticeStructure.getSetForSecrecyLevel(implementationLevel);
        
        if(!implementationSet.contains(definitionLevel) && !implementationLevel.equals(definitionLevel)) {
            errors.add(new TypeError(implementation.getReturnType(), ErrorMessage.SECRECY_LEAKAGE_ERROR_AT_MOST, definitionLevel, implementationLevel));
        }
        
        for(ParamDecl implementationParam : implementation.getParamList()) {
            for(ParamDecl definitionParam : definition.getParamList()) {

                if(definitionParam.getName().equals(implementationParam.getName())){
                    
                    implementationLevel = _secrecy.get(implementationParam);
                    definitionLevel = _secrecy.get(definitionParam);

                    if(definitionLevel == null) {
                        definitionLevel = secrecyLatticeStructure.getMinSecrecyLevel();
                    }

                    if(implementationLevel == null) {
                        implementationLevel = secrecyLatticeStructure.getMinSecrecyLevel();
                    }

                    if(!implementationLevel.equals(definitionLevel)){
                        implementationSet = secrecyLatticeStructure.getSetForSecrecyLevel(implementationLevel);

                        if(!implementationSet.contains(definitionLevel)) {
                            errors.add(new TypeError(implementation.getReturnType(), ErrorMessage.SECRECY_LEAKAGE_ERROR_AT_MOST, definitionLevel, implementationLevel));
                        }
                    }
                }
            }
        }
    }
}

/* Notes

@ Todos
- Implement visit methods (in progress)
    - for every stmt
    - for every exp 

- Refactor the _secrecy Hashmap
    - todo redo rules written below
    - Koennen wir stattdessen fuer jede ASTNode direct checken ob es eine SecrecyAnnotation gibt mit einem Visitor oder so und wenn ja diese hinzufuegen oder muessen wir den AST traversen

- Missing Rules
    - More checks in the second phase (if needed)
    - Error for overwriting an existing secrecy value in _secrecy(not allowed I think depends on how I implement it)
        - Question 2.
        //if(_secrecy.get() != null) {errors.add(new TypeError(annotation, ErrorMessage.SECRECY_OVERWRITING_EXISTING, variablename));}


@ Questions
1. Is there a better way for the two/multi pass approach to be implemented
2. Can it be that we want a MaxSecrecyLevel for a variable AND a current secrecy level? (Basically a clone for _secrecy but we can't change the level)
*/
