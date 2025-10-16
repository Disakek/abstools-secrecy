/**
 * Copyright (c) 2009-2011, The HATS Consortium. All rights reserved. 
 * This file is licensed under the terms of the Modified BSD License.
 * Written by @Disakek for questions please refer to uukln@student.kit.edu
 */
package org.abs_models.frontend.typechecker.ext;

import java.util.HashSet;
import java.util.Set;
import java.util.HashMap;
import org.abs_models.frontend.analyser.AnnotationHelper;
import org.abs_models.frontend.analyser.ErrorMessage;
import org.abs_models.frontend.analyser.TypeError;
import org.abs_models.frontend.ast.*;
import org.abs_models.frontend.ast.DeltaDecl;
import org.abs_models.frontend.typechecker.Type;
import org.abs_models.frontend.typechecker.TypeAnnotation;
import org.abs_models.frontend.typechecker.ext.DefaultTypeSystemExtension;
import org.abs_models.frontend.typechecker.ext.AdaptDirection;

public class SecrecyAnnotationChecker extends DefaultTypeSystemExtension {

    private HashMap<String,String> _secrecy = new HashMap<>();

    // Store all known secrecy levels
    private Set<String> _secrecyLevels = new HashSet<>();

    //Store the order for the levels
    private HashMap<String, Set<String>> _latticeOrder = new HashMap<>();

    //Keeps track whether we have a custom lattice input or not
    private boolean custom_lattice = false;

    protected SecrecyAnnotationChecker(Model m) {
        super(m);

        // Define ordering
        // Each key maps to the set of levels it is *less than or equal to* (upper bounds)
        // For example: Low < High, High <= High, Low <= Low
        _latticeOrder.put("Low", Set.of("High"));
        _latticeOrder.put("High", Set.of());

    }

    //Classes of annotations
    // 1 return types of methods : return_methodName
    // 2 parameters of methods   : parameterName_methodName
    // 3 fields                  : variablename

    //TODO: MISSING IF I ASSIGN A SECRECY VALUE THERE MIGHT ALREADY BE ONE (OVERWRITING)!!!!
    //TODO: MISSING LATTICE AND IT'S ORDER NOT AS INPUT SO FAR (tried it with delta doesn't work well so far)
    @Override
    public void checkModel(Model model) {
        for (CompilationUnit cu : model.getCompilationUnits()) {

            for(DeltaDecl deltaDecl : cu.getDeltaDecls()){

                List<ModuleModifier> modifications = deltaDecl.getModuleModifierList();

                for (ModuleModifier modifier : modifications) {
                    if(modifier instanceof ModifyDataTypeModifier dataTypeModifier) {

                        DataTypeDecl dataType = dataTypeModifier.getDataTypeDecl();
                        
                        if(dataType.getName().equals("Secrecy")) {
                            for (DataConstructor cons : dataType.getDataConstructors()) {
                                _secrecyLevels.add(cons.getName()); // Extracts the user definde lattice levels from modifier
                                custom_lattice = true;
                            }
                        }
                    }
                }
            }

            if(!custom_lattice) {
                _secrecyLevels.add("Low");
                _secrecyLevels.add("High");
            }

            for (ModuleDecl moduleDecl : cu.getModuleDecls()) {

                for (Decl decl : moduleDecl.getDecls()) {

                    if (decl instanceof ClassDecl classDecl) {

                        //Extract field annotations and store them to the secrecy hashmap
                        for(FieldDecl fieldDecl : classDecl.getFields()) {

                            String name = fieldDecl.getName();
                            processTypeAnnotations(fieldDecl.getTypeUse(), name);
                            
                        }

                        //Extracts the annotation for methods of the class for their return values and their parameters
                        for (MethodImpl method : classDecl.getMethods()) {
                    
                            getAnnotationsForMethodSig(method.getMethodSig());               

                            //Here are the checks for the Stmts
                            Block block = method.getBlock();

                            for (Stmt stmt : block.getStmtList()) {

                                if (stmt instanceof AssignStmt assignStmt) {

                                    Exp rightSide = assignStmt.getValue();

                                    if(_secrecy.get(assignStmt.getVar().getName()) != null) {
                                        
                                        String  LHSsecLevel = _secrecy.get(assignStmt.getVar().getName());

                                        if (rightSide instanceof VarOrFieldUse varUse) {

                                            if(_secrecy.get(varUse.getName()) != null) {
                                                
                                                String RHSsecLevel = _secrecy.get(varUse.getName());
                                                
                                                Set<String> LHScontainedIn = _latticeOrder.get(LHSsecLevel);

                                                if(!LHScontainedIn.contains(RHSsecLevel)) {
                                                    System.out.println("ERROR: LEAKAGE FOUND " + varUse.getName() + " TO " + assignStmt.getVar().getName());
                                                } else {
                                                    System.out.println("No error"); //TODO: remove ultimatly
                                                }
                                            }
                                        }
                                    }

                                } else if (stmt instanceof VarDeclStmt varDeclStmt) {
                                    
                                    VarOrFieldDecl varDecl = varDeclStmt.getVarDecl();
                                    
                                    //Annotations for variables in methods
                                    if(varDecl instanceof TypedVarOrFieldDecl typedVar) {
                                        for(Annotation annotation : varDeclStmt.getAnnotationList()) {
                                            extractSecrecySafely(typedVar.getTypeUse(), annotation, varDecl.getName());
                                        }
                                    }
                                }
                            }
                        }

                    } else if (decl instanceof InterfaceDecl interfaceDecl) {
                        //System.out.println("InterfaceDecl");
                        for (MethodSig methodSig : interfaceDecl.getBodyList()) {
                            getAnnotationsForMethodSig(methodSig);
                        }
                    }
                }
            }
        }

        System.out.println("Print annotated Values: " + _secrecy.toString());
        System.out.println("Print all Levels: " + _secrecyLevels.toString());
        System.out.println("Print the order" + _latticeOrder.toString());
    }

    private void getAnnotationsForMethodSig(MethodSig methodSig) {

        //Extracts for the return value
        String methodName = methodSig.getName();
        String returnTypeName = "return_" + methodName;
        processTypeAnnotations(methodSig.getReturnType(), returnTypeName);

        //Extracts for the parameters
        for(ParamDecl parameter : methodSig.getParamList()) {
            for(Annotation annotation : parameter.getAnnotationList()) {
                String parameterName = parameter.getName() + "_" + methodName;
                extractSecrecySafely(parameter.getTypeUse(), annotation, parameterName);
            }
        }
    }

    private void processTypeAnnotations(TypeUse typeUse, String variablename) {
        for (Annotation annotation : typeUse.getAnnotationList()) {
            //System.out.println("ProcessAnnotation: " + annotation);
            extractSecrecySafely(typeUse, annotation, variablename);
        }
    }
    

    //Extracts the PureExp from an Annotation after running the checkSecrecyAnnotationCheck it extracts the value and stores the metadata in the _secrecy Hashmap
    private void extractSecrecySafely(TypeUse typeU, Annotation annotation, String variablename) {

        if (annotation instanceof TypedAnnotation typedAnnotation) {

            ASTNode<?> valueNode = annotation.getChild(0); // value
            ASTNode<?> nameNode  = annotation.getChild(1); // name

            if (nameNode.toString().equals("Secrecy") && valueNode instanceof DataConstructorExp dataConExp) {
           
                String levelName = dataConExp.getConstructor(); 

                if(_secrecy.get(variablename) != null)System.out.println("Overwrite");

                if (_secrecyLevels.contains(levelName)) {
                    _secrecy.put(variablename, levelName);
                    System.out.println("Metadata,Secrecy: " + levelName);
                } else {
                    System.out.println("ERROR: INVALID SECRECY LATTICE");
                    System.out.println(levelName + " NOT FOUND");
                }
            }
        }
    }

}
