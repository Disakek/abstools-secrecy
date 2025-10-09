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

    /*
    for (MethodSig sig : decl.getMethodList()) {
            // Check return type annotations
            for (Annotation ann : sig.getTypeUse().getAnnotationList()) {
                handleAnnotation(ann, sig);
            }

            // Check parameter annotations
            for (ParamDecl param : sig.getParamList()) {
                for (Annotation ann : param.getAnnotationList()) {
                    handleAnnotation(ann, param);
                }
            }
        }
    */

    @Override
    public void checkInterfaceDecl(InterfaceDecl decl) {

        //if (decl.getModuleDecl().getName().equals("Test")) { //Helper stmt to ignore inherited classes from .Object class
            for (MethodSig sig : decl.getBodyList()) {
                
                //System.out.println(sig.toString());  //Prints the MethodSig

                TypeUse returnType = sig.getReturnType();
                
                for (Annotation annotation : returnType.getAnnotationList()) {
                    //System.out.println(annotation); //Prints the Annotations for the ReturnValues

                    extractAnnotationSafely(returnType, annotation); //Helper Function to extract and then check the SecrecyValue
                }

                for (ParamDecl parameter : sig.getParamList()) {
                
                    //System.out.println("  Param: " + parameter.getName() + " : " + parameter.getTypeUse().getName()); //Prints the Parameter Name and Type

                    for(Annotation annotation : parameter.getAnnotationList()){
                        //System.out.println(annotation); //Prints the Annotations for the ParameterValues

                        extractAnnotationSafely(parameter.getTypeUse(), annotation); //Helper Function to extract and then check the SecrecyValue
                    }
                }
            }
        //}
    }

    //Extracts the PureExp from an Annotation after running the checkSecrecyAnnotationCheck it extracts the value and adds it as metadata to the node
    private void extractAnnotationSafely(TypeUse typeU, Annotation annotation) {

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
                    System.out.println(typeU.getType().getMetaData("Secrecy")); //Check if it got attached right
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
