/**
 * Copyright (c) 2009-2011, The HATS Consortium. All rights reserved. 
 * This file is licensed under the terms of the Modified BSD License.
 * Written by @Maximilian_Paul for questions please refer to uukln@student.kit.edu
 */
package org.abs_models.frontend.typechecker.ext.secrecytype;

import java.util.Objects;

import org.abs_models.frontend.ast.*;

/**
 * This class is used to keep an overview of each method of a class for the secrecytype.
 * For each method of a class of a model to be checked one node is created.
 * It is used to store information on being secure and being checked.
 * Specifically it allows to generate an error if a method is calling another method which is insecure. 
 */
public class SecrecyMethod {

    /**
     * The class declaration of the class of which the method is a part of.
     */
    private ClassDecl parentClassOfMethod;

    /**
     * The method's implementation.
     */
    private MethodImpl methodNode;

    /**
     * Status flag signifying if the method in question already got typechecked with the secrecytype system.
     */
    private boolean isChecked = false;

    /**
     * Status flag signifying if the method in question was secure.
     */
    private boolean isSecure = false;

    /**
     * Constructor for adding a new method to be checked at some point.
     * @param parentClassOfMethod - the class in which the methid is implemented in.
     * @param methodNode - the implementation of the method that should be checked.
     */
    public SecrecyMethod (ClassDecl parentClassOfMethod, MethodImpl methodNode) {
        this.parentClassOfMethod = parentClassOfMethod;
        this.methodNode = methodNode;
        this.isChecked = false;
        this.isSecure = true;
    }

    /**
     * Getter for the method's implementation.
     * @return - the method's implementation.
     */
    public MethodImpl getMethodNode() {
        return methodNode;
    }

    /**
     * Getter for whether the method is checked or not.
     * @return - the isChecked flag.
     */
    public boolean getIsChecked() {
        return isChecked;
    }

    /**
     * Setter for the isChecked flag.
     * @param isChecked - the value to which it should be set
     */
    public void setIsChecked(boolean isChecked) {
        this.isChecked = isChecked;
    }

    /**
     * Getter for whether the method is secure or not.
     * @return - the isSecure flag.
     */
    public boolean getIsSecure() {
        return isSecure;
    }

    /**
     * Setter for the isSecure flag.
     * @param isSecure - the value to which it should be set
     */
    public void setIsSecure(boolean isSecure) {
        this.isSecure = isSecure;
    }

    /**
     * Custom implementation of the toString() method
     * @return - returns the string in the format "ClassName.MethodName"
     */
    public String toString() {
        return parentClassOfMethod.getName() + "." + methodNode.getMethodSig().getName();
    }

    /**
     * Custom implementation to compare two objects of the SecrecyMethod class.
     * @param obj - the object to compare it to.
     * @return - true if the two objects are equal, false otherwise.
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        SecrecyMethod other = (SecrecyMethod) obj;
        return this.parentClassOfMethod == other.parentClassOfMethod && this.methodNode == other.methodNode;
    }

    /**
     * Custom hash implementation to allow to compare two objects.
     * @return - the hash integer describing the object.
     */
    @Override
    public int hashCode() {
        return Objects.hash(parentClassOfMethod, methodNode);
    }
}