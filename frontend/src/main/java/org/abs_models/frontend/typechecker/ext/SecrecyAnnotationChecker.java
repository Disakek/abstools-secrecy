/**
 * Copyright (c) 2009-2011, The HATS Consortium. All rights reserved. 
 * This file is licensed under the terms of the Modified BSD License.
 * Written by @Disakek for questions please refer to uukln@student.kit.edu
 */
package org.abs_models.frontend.typechecker.ext;


import org.abs_models.frontend.analyser.AnnotationHelper;
import org.abs_models.frontend.analyser.ErrorMessage;
import org.abs_models.frontend.analyser.TypeError;
import org.abs_models.frontend.ast.*;
import org.abs_models.frontend.typechecker.Type;
import org.abs_models.frontend.typechecker.TypeAnnotation;
import org.abs_models.frontend.typechecker.ext.DefaultTypeSystemExtension;
import org.abs_models.frontend.typechecker.ext.AdaptDirection;

public class SecrecyAnnotationChecker extends DefaultTypeSystemExtension {

    protected SecrecyAnnotationChecker(Model m) {
        super(m);
    }

    //Classes of annotations
    // 1 return types of methods
    // 2 parameters
    // 3 fields

    @Override
    public void checkInterfaceDecl(InterfaceDecl decl) {

        for (MethodSig sig : decl.getBodyList()) {

            //Annotations for the returntype
            processTypeAnnotations(sig.getReturnType());

            //Annotations for the parameters
            for (ParamDecl parameter : sig.getParamList()) {
                for(Annotation annotation : parameter.getAnnotationList()){
                    extractSecrecySafely(parameter.getTypeUse(), annotation);
                }
            }
        }
    }

    @Override
    public void checkClassDecl(ClassDecl decl) {
        //Annotations for the fields of a class
        for(FieldDecl fieldDecl : decl.getFields()) {
            processTypeAnnotations(fieldDecl.getTypeUse());
        }
    }

    @Override
    public void checkVarDeclStmt(VarDeclStmt varDeclStmt) {

        VarOrFieldDecl varDecl = varDeclStmt.getVarDecl();

        //Annotations for methodvariables
        if(varDecl instanceof TypedVarOrFieldDecl typedVar) {
            for(Annotation annotation : varDeclStmt.getAnnotationList()) {
                extractSecrecySafely(typedVar.getTypeUse(), annotation);
            }
        }
    }

    @Override
    public void checkMethodImpl(MethodImpl method) {

        //Annotations for the returntype
        processTypeAnnotations(method.getMethodSig().getReturnType());

        //Annotations for the parameteres
        for (ParamDecl parameter : method.getMethodSig().getParamList()) {
            for(Annotation annotation : parameter.getAnnotationList()){
                extractSecrecySafely(parameter.getTypeUse(), annotation); //Helper Function to extract and then check the SecrecyValue
            }
        }
    }

    //TODO: add rules here

    private void processTypeAnnotations(TypeUse typeUse) {
        for (Annotation annotation : typeUse.getAnnotationList()) {
            //System.out.println("ProcessAnnotation: " + annotation);
            extractSecrecySafely(typeUse, annotation);
        }
    }
    

    //Extracts the PureExp from an Annotation after running the checkSecrecyAnnotationCheck it extracts the value and adds it as metadata to the node
    private void extractSecrecySafely(TypeUse typeU, Annotation annotation) {

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

                    typeU.getType().addMetaData("Secrecy", level); //Add to metadata of the node
                    System.out.println("Metadata, Secrecy:" + typeU.getType().getMetaData("Secrecy")); //Check if it got attached right
                }
            }

        }
    }

    // Checks that the input secrecy is a number and that it is not null
    // TODO: remove the option to set the secrecy to 0 (assume / set this as base value later)
    private void checkSecrecyAnnotationCorrect(ASTNode<?> n, PureExp secrecy) {
        //System.out.println("Called Secrecy Correct");
        if (secrecy == null) return;
        secrecy.typeCheck(errors);
        if (!secrecy.getType().isNumericType()) {
            errors.add(new TypeError(n, ErrorMessage.WRONG_SECRECY_ANNOTATION_TYPE,
                                     secrecy.getType().getQualifiedName()));
        }
    }
}
