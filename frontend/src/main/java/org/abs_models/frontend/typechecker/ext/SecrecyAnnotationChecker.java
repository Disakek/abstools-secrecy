/**
 * Copyright (c) 2009-2011, The HATS Consortium. All rights reserved. 
 * This file is licensed under the terms of the Modified BSD License.
 * Written by @Disakek for questions please refer to uukln@student.kit.edu
 */
package org.abs_models.frontend.typechecker.ext;


import org.abs_models.frontend.analyser.AnnotationHelper;
import org.abs_models.frontend.analyser.ErrorMessage;
import org.abs_models.frontend.analyser.TypeError;
import org.abs_models.frontend.ast.ASTNode;
import org.abs_models.frontend.ast.PureExp;
import org.abs_models.frontend.ast.NewExp;
import org.abs_models.frontend.ast.AssignStmt;
import org.abs_models.frontend.ast.ExpressionStmt;
import org.abs_models.frontend.ast.Model;
import org.abs_models.frontend.ast.ReturnStmt;
import org.abs_models.frontend.ast.VarDeclStmt;
import org.abs_models.frontend.ast.DataTypeDecl;
import org.abs_models.frontend.ast.ClassDecl;
import org.abs_models.frontend.ast.InterfaceDecl;
import org.abs_models.frontend.ast.Annotation;
import org.abs_models.frontend.ast.TypedAnnotation;
import org.abs_models.frontend.ast.TypeUse;
import org.abs_models.frontend.ast.UnresolvedTypeUse;
import org.abs_models.frontend.typechecker.Type;
import org.abs_models.frontend.ast.IntLiteral;


public class SecrecyAnnotationChecker extends DefaultTypeSystemExtension {

    protected SecrecyAnnotationChecker(Model m) {
        super(m);
    }

    @Override
    public void checkInterfaceDecl(InterfaceDecl decl) {
        for (Annotation annotation : decl.getAnnotations()) {
            for (int i = 0; i < annotation.getNumChild(); i++) {
                ASTNode<?> child = annotation.getChild(i);

                if (child instanceof IntLiteral intLit) {
                    // Look at the other child to ensure it's a Secrecy annotation
                    ASTNode<?> otherChild = annotation.getChild(1 - i); // the child that is not intLit
                    if (otherChild.toString().equals("Secrecy")) {
                        int level = Integer.parseInt(intLit.getContent());
                        System.out.println("Secrecy level: " + level);

                        // Save to metadata
                        System.out.println("TODO MISSING METADATA ATTACHEMENT");
                    }
                }
            }
        }
    }


    // Checks that the input secrecy is a number and that it is neither 0 nor null
    private void checkSecrecyAnnotationCorrect(ASTNode<?> n, PureExp secrecy) {
        System.out.println("Called Secrecy Correct");
        if (secrecy == null) return;
        secrecy.typeCheck(errors);
        if (!secrecy.getType().isNumericType()) {
            errors.add(new TypeError(n, ErrorMessage.WRONG_SECRECY_ANNOTATION_TYPE,
                                     secrecy.getType().getQualifiedName()));
        }
    }
}
