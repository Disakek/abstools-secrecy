/**
 * Copyright (c) 2009-2011, The HATS Consortium. All rights reserved. 
 * This file is licensed under the terms of the Modified BSD License.
 * Written by @Maximilian_Paul for questions please refer to uukln@student.kit.edu
 */
package org.abs_models.frontend.typechecker.ext.secrecytype;

import java.util.Objects;

import org.abs_models.frontend.ast.*;

/**
 * This class is used to store information on which method contains a call to which other method.
 * It is used to calculate the errors for methods that are insecure due to calling another insecure method.
 * This is only calculated for methods where caller and callee are part of the same class.
 */
public class CalledMethod {

    /**
     * The class declaration of the class where both methods are implemented in.
     */
    public ClassDecl classContainingBoth;

    /**
     * The call from one method to the possibly insecure other method.
     */
    public Call callToMethod;

    /**
     * The method implementation of the method that was called. (If this is method is insecure we need to add a type error).
     */
    public MethodImpl methodImplOfCalledMethod;

    /**
     * The constructor used to create a new call which has to be checked once all methods are checked.
     * @param classContainingBoth - the class that contains caller and callee to check
     * @param callToMethod - the method call that is part of the caller
     * @param methodImplOfCalledMethod - the method implementation that was called
     */
    public CalledMethod (ClassDecl classContainingBoth, Call callToMethod, MethodImpl methodImplOfCalledMethod) {
        this.classContainingBoth = classContainingBoth;
        this.callToMethod = callToMethod;
        this.methodImplOfCalledMethod = methodImplOfCalledMethod;
    }

    /**
     * Getter for the class declaration that contains the methods.
     * @return - returns the class declaration that contains the caller and the callee.
     */
    public ClassDecl getCallParentClass() {
        return classContainingBoth;
    }

    /**
     * Getter for the method call of the calling method.
     * @return - returns the call of the caller.
     */
    public Call getMethodCall() {
        return callToMethod;
    }

    /**
     * Getter for the method implementation of the called method.
     * @return - returns the method implementation of the callee.
     */
    public MethodImpl getMethodImpl() {
        return methodImplOfCalledMethod;
    }

    /**
     * Overwritten to string method to make it humanreadable.
     * Format is "Call to classname.methodname".
     * @return - returns the object in the described format as a string.
     */
    @Override
    public String toString() {
        return callToMethod + " to " + classContainingBoth.getName() + "." + methodImplOfCalledMethod.getMethodSig().getName();
    } 

    /**
     * Overwritten equals method to compare two instances of calledMethod.
     * If one method calls another more than once an error is only to be checked for once.
     * @param obj - the object to compare it to.
     * @return - true if the two objects are the same, false otherwise.
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        CalledMethod other = (CalledMethod) obj;
        return this.classContainingBoth == other.classContainingBoth && this.callToMethod == other.callToMethod && this.methodImplOfCalledMethod == other.methodImplOfCalledMethod;
    }

    /**
     * Overwritten hashcode method in order to enable the equals comparison.
     * @return - a hash integer describing the object.
     */
    @Override
    public int hashCode() {
        return Objects.hash(classContainingBoth, callToMethod, methodImplOfCalledMethod);
    }

}