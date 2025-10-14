/**
 * Copyright (c) 2009-2011, The HATS Consortium. All rights reserved. 
 * This file is licensed under the terms of the Modified BSD License.
 * Written by @Disakek for questions please refer to uukln@student.kit.edu
 */
package org.abs_models.frontend.typechecker.ext;

import java.util.HashMap;
import org.abs_models.frontend.analyser.AnnotationHelper;
import org.abs_models.frontend.analyser.ErrorMessage;
import org.abs_models.frontend.analyser.TypeError;
import org.abs_models.frontend.ast.*;
import org.abs_models.frontend.typechecker.Type;
import org.abs_models.frontend.typechecker.TypeAnnotation;
import org.abs_models.frontend.typechecker.ext.DefaultTypeSystemExtension;
import org.abs_models.frontend.typechecker.ext.AdaptDirection;

public class SecrecyAnnotationChecker extends DefaultTypeSystemExtension {

    private HashMap<String,Integer> _secrecy = new HashMap<>();

    protected SecrecyAnnotationChecker(Model m) {
        super(m);
    }

    //Classes of annotations
    // 1 return types of methods : return_methodName
    // 2 parameters of methods   : parameterName_methodName
    // 3 fields                  : variablename

    //TODO: MISSING IF I ASSIGN A SECRECY VALUE THERE MIGHT ALREADY BE ONE (OVERWRITING)!!!!
    @Override
    public void checkModel(Model model) {
        for (CompilationUnit cu : model.getCompilationUnits()) {
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
                            
                            //System.out.println("ClassDecl");

                            getAnnotationsForMethodSig(method.getMethodSig());               

                            //Here are the checks for the Stmts
                            Block block = method.getBlock();

                            for (Stmt stmt : block.getStmtList()) {

                                if (stmt instanceof AssignStmt assignStmt) {

                                    Exp rightSide = assignStmt.getValue();

                                    if(_secrecy.get(assignStmt.getVar().getName()) != null) {
                                        Integer LHSsecLevel = _secrecy.get(assignStmt.getVar().getName());

                                        if (rightSide instanceof VarOrFieldUse varUse) {

                                            if(_secrecy.get(varUse.getName()) != null) {
                                                Integer RHSsecLevel = _secrecy.get(varUse.getName());

                                                if(LHSsecLevel < RHSsecLevel) {
                                                    System.out.println("ERROR: LEAKAGE FOUND " + varUse.getName() + " TO " + assignStmt.getVar().getName());
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
        System.out.println(_secrecy.toString());
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

            PureExp secrecy = typedAnnotation.getValue(); // Extract the value safely

            checkSecrecyAnnotationCorrect(typeU, secrecy);
        }

        for (int i = 0; i < annotation.getNumChild(); i++) {
                ASTNode<?> child = annotation.getChild(i);

            if (child instanceof IntLiteral intLit) {
            
            // Look at the other child to ensure it's a Secrecy annotation
            
            ASTNode<?> otherChild = annotation.getChild(1 - i); // the child that is not intLit

                if (otherChild.toString().equals("Secrecy")) {

                    int level = Integer.parseInt(intLit.getContent());

                    if(_secrecy.get(variablename) != null) {
                        System.out.println("Overwrite");
                    }

                    _secrecy.put(variablename, level);

                    System.out.println("Metadata,Secrecy: " + level);
                }
            }

        }
    }

    // Checks that the input secrecy is a number and that it is not null
    // TODO: remove the option to set the secrecy to 0 (assume / set this as base value later)
    private void checkSecrecyAnnotationCorrect(ASTNode<?> n, PureExp secrecy) {
        if (secrecy == null) return;
        secrecy.typeCheck(errors);
        if (!secrecy.getType().isNumericType()) {
            errors.add(new TypeError(n, ErrorMessage.WRONG_SECRECY_ANNOTATION_TYPE,
                                     secrecy.getType().getQualifiedName()));
        }
    }
}
