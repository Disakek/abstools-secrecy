/**
 * Copyright (c) 2009-2011, The HATS Consortium. All rights reserved. 
 * This file is licensed under the terms of the Modified BSD License.
 * Written by @Maximilian_Paul for questions please refer to uukln@student.kit.edu
 */
package org.abs_models.frontend.typechecker.ext;

import java.util.HashSet;
import java.util.Set;
import java.util.HashMap;

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

    //TODO: this probably should not safe the secrecy values as string but as objects
    private HashMap<String,String> _secrecy = new HashMap<>();

    //TODO: get rid of by implementing what I might need to the SecrecyExtention
    private Set<String> _secrecyLevels = new HashSet<>();
    private HashMap<String, Set<String>> _latticeOrder = new HashMap<>();

    SecrecyExtension secrecyLatticeStructure;

    //TODO: get rid of by using objects for the variables & interface/implementation of methods
    private final String classMethodName = "methodImp";
    private final String interfaceMethodName = "methodIfc";

    protected SecrecyAnnotationChecker(Model m) {
        super(m);

        if (m.secrecyExtension != null) {

            secrecyLatticeStructure = m.secrecyExtension;

            SecrecyExtension userInput = m.secrecyExtension;
            _secrecyLevels = new HashSet<>(userInput.getSecrecyLevels());
            _latticeOrder = new HashMap<>(userInput.getLatticeOrder());
        }
    }

    //Classes of annotations
    // 1 return types of methods : return_methodName_{methodImpl | methDecl}
    // 2 parameters of methods   : parameterName_methodName_{methodImpl | methDecl}
    // 3 fields                  : variablename
    @Override
    public void checkModel(Model model) {

        //TODO: FirstExtractionPassPhase

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
                            getAnnotationsForMethodSig(method.getMethodSig(), classMethodName + "_" +classDecl.getName());               

                            Block block = method.getBlock();

                            for (Stmt stmt : block.getStmtList()) {

                                if (stmt instanceof VarDeclStmt varDeclStmt) {
                                    
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
                        for (MethodSig methodSig : interfaceDecl.getBodyList()) {
                            getAnnotationsForMethodSig(methodSig, interfaceMethodName);
                        }
                    }
                }
            }
        }

        SecrecyStmtVisitor visitor = new SecrecyStmtVisitor(_secrecy, secrecyLatticeStructure);

        //TODO: SecondTypecheckPassPhase
        //TODO: add more checks that should be performed

        for (CompilationUnit cu : model.getCompilationUnits()) {

            for (ModuleDecl moduleDecl : cu.getModuleDecls()) {

                for (Decl decl : moduleDecl.getDecls()) {

                    if (decl instanceof ClassDecl classDecl) {

                        for (MethodImpl method : classDecl.getMethods()) {

                            Block block = method.getBlock();

                            for (Stmt stmt : block.getStmtList()) {

                                //TODO: implement the stmt visitors for all possible stmt's
                                stmt.accept(visitor); 

                            }
                        }

                    } else if (decl instanceof InterfaceDecl interfaceDecl) {
                        for (MethodSig methodSig : interfaceDecl.getBodyList()) {
                            //TODO: deleted extraction here not sure if we have something to check here
                        }
                    }
                }
            }
        }

        System.out.println("Print annotated Values: " + _secrecy.toString());
        System.out.println("Print all Levels: " + _secrecyLevels.toString());
        System.out.println("Print the order" + _latticeOrder.toString());
    }

    private void firstExtractionPhasePass(){}
    private void secondTypecheckPhasePass(){}


    private void handleAssignCheck(AssignStmt assignStmt) {

        Exp RhsExp = assignStmt.getValue();

        //TODO: Change the way I store the secrecy annotations from string to objects to make it more usable and allow different kinds
        String  LHSsecLevel = _secrecy.get(assignStmt.getVar().getName());

            //TODO: if it is a variable on the right hand side
        if (RhsExp instanceof VarOrFieldUse varUse) {

                //TODO: If the right hand side is a variable but doesnt have a secrecy value what do we do?
            if(_secrecy.get(varUse.getName()) == null)return;
                                                
            String RHSsecLevel = _secrecy.get(varUse.getName());
                                                
            Set<String> LHScontainedIn = _latticeOrder.get(LHSsecLevel);

            if(!LHScontainedIn.contains(RHSsecLevel)) {
                errors.add(new TypeError(assignStmt, ErrorMessage.SECRECY_LEAKAGE_ERROR_FROM_TO, LHSsecLevel, varUse.getName(), RHSsecLevel, assignStmt.getVar().getName()));
            } else {
                System.out.println("found no errors in values on two sides");
            }
        }
    }

    private void getAnnotationsForMethodSig(MethodSig methodSig, String name) {

        if(methodSig.getParent() != null) {
            ASTNode parentNode = methodSig.getParent();

            //Extracts for the return value
            String methodName = methodSig.getName();
            String returnTypeName = "return_" + methodName + "_" + name;
            processTypeAnnotations(methodSig.getReturnType(), returnTypeName);

            //Extracts for the parameters
            for(ParamDecl parameter : methodSig.getParamList()) {
                for(Annotation annotation : parameter.getAnnotationList()) {
                    String parameterName = parameter.getName() + "_" + methodName + "_" + name;
                    extractSecrecySafely(parameter.getTypeUse(), annotation, parameterName);
                }
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

                if(_secrecy.get(variablename) != null) {
                    errors.add(new TypeError(annotation, ErrorMessage.SECRECY_OVERWRITING_EXISTING, variablename));
                }

                if (_secrecyLevels.contains(levelName)) {
                    _secrecy.put(variablename, levelName);
                    //System.out.println("Metadata,Secrecy: " + levelName); //TODO: remove print

                    if (variablename.contains(classMethodName)){    
                        String[] parts = variablename.split("_");
                        String InterfaceName = parts[0] + "_" +parts[1] + "_" + interfaceMethodName;

                        if(_secrecy.get(InterfaceName) != null) {
                        
                            String IfcSecrecyVal = _secrecy.get(InterfaceName); //Ifc - Interface
                            String ImpSecrecyVal = _secrecy.get(variablename); //Imp - Implementation
                            Set<String> setForInterface = _latticeOrder.get(IfcSecrecyVal);

                            if(variablename.contains("return")){
                                if(!IfcSecrecyVal.equals(ImpSecrecyVal) && !(setForInterface.contains(ImpSecrecyVal))) {
                                    errors.add(new TypeError(annotation, ErrorMessage.SECRECY_LEAKAGE_ERROR_AT_LEAST, IfcSecrecyVal, ImpSecrecyVal));
                                }
                            } else {
                                if(!IfcSecrecyVal.equals(ImpSecrecyVal) && setForInterface.contains(ImpSecrecyVal)) {
                                    errors.add(new TypeError(annotation, ErrorMessage.SECRECY_LEAKAGE_ERROR_AT_MOST, IfcSecrecyVal, ImpSecrecyVal));
                                }
                            }
                        }
                    }

                } else {
                    errors.add(new TypeError(annotation, ErrorMessage.WRONG_SECRECY_ANNOTATION_VALUE, levelName));
                }
            }
        }
    }

}
