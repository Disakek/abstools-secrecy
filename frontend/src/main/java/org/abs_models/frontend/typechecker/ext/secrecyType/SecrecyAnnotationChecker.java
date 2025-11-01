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

    //Is the mapping from an ASTNode (the declaration) to the assigned SecrecyValue
    private HashMap<ASTNode<?>,String> _secrecy = new HashMap<>();
    
    //Contains the secrecy lattice given by the user or the default (Low < High)
    SecrecyLatticeStructure secrecyLatticeStructure;
    
    //Is the visitor for all Stmts that typechecks the implemented rules    
    SecrecyStmtVisitor visitor;                         
    
    protected SecrecyAnnotationChecker(Model m) {
        super(m);

        if (m.secrecyLatticeStructure != null) {
            secrecyLatticeStructure = m.secrecyLatticeStructure;
        }
    }

    @Override
    public void checkModel(Model model) {

        //First pass of all the code to extract the secrecy annotations and populate _secrecy
        firstExtractionPhasePass(model); 

        visitor = new SecrecyStmtVisitor(_secrecy, secrecyLatticeStructure, errors);

        //Second pass to enforce all the typerules
        secondTypecheckPhasePass(model); 
        
        System.out.println("Print new annotated Values: " + _secrecy.toString());
        System.out.println("Print all Levels: " + secrecyLatticeStructure.getSecrecyLevels().toString());
        System.out.println("Print the order" + secrecyLatticeStructure.getLatticeOrder().toString());
    }

    private void firstExtractionPhasePass(Model model){
        for (CompilationUnit cu : model.getCompilationUnits()) {
            for (ModuleDecl moduleDecl : cu.getModuleDecls()) {
                for (Decl decl : moduleDecl.getDecls()) {
                    if (decl instanceof ClassDecl classDecl) {

                        //Extract field annotations and store them to the secrecy hashmap
                        for(FieldDecl fieldDecl : classDecl.getFields()) {
                            String level = extractSecrecyValue(fieldDecl);
                            if(level != null)_secrecy.put(fieldDecl, level);
                        }
                        
                        //Extracts the annotation for methods of the class for their return values and their parameters
                        for (MethodImpl method : classDecl.getMethods()) {
                            
                            String Returnlevel = extractSecrecyValue(method.getMethodSig());
                            if(Returnlevel != null)_secrecy.put(method.getMethodSig(), Returnlevel);

                            for(ParamDecl parameter : method.getMethodSig().getParamList()) {
                                String Parameterlevel = extractSecrecyValue(parameter);
                                if(Parameterlevel != null)_secrecy.put(parameter, Parameterlevel);
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

                    System.out.println("Levelname: " + levelName);
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
                                //TODO: implement the stmt visitors for all possible stmt's
                                stmt.accept(visitor);
                            }
                        }
                    }
                }
            }
        }
    }
}

/* Notes

@ Todos
- Implement visit methods
    - for every stmt
    - for every exp

- Refactor the _secrecy Hashmap (not to be string based)
    - DONE switched to storing the ASTNode of the decl
    - todo redo rules written below
    - Koennen wir stattdessen fuer jede ASTNode direct checken ob es eine SecrecyAnnotation gibt mit einem Visitor oder so und wenn ja diese hinzufuegen oder muessen wir den AST traversen

- Missing Rules
    - More checks in the second phase
    - Interface and method implementation dependence -> implementation has to satisfy the interface rules
    - Error for overwriting an existing secrecy value in _secrecy(not allowed I think)
        - Question 2.
        //if(_secrecy.get() != null) {errors.add(new TypeError(annotation, ErrorMessage.SECRECY_OVERWRITING_EXISTING, variablename));}

@ Questions
1. Is there a better way for the two/multi pass approach to be implemented
2. Can it be that we want a MaxSecrecyLevel for a variable AND a current secrecy level? (Basically a clone for _secrecy but we can't change the level)
*/
