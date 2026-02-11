/**
 * Copyright (c) 2009-2011, The HATS Consortium. All rights reserved. 
 * This file is licensed under the terms of the Modified BSD License.
 * Written by @Maximilian_Paul for questions please refer to uukln@student.kit.edu
 */
package org.abs_models.frontend.typechecker.ext;

/**
 * This class is used to store the changes for the programConfidentiality
 */
public class ProgramCountNode {

    /**
     * Name of the node that lead to a change. (for if/while just ifStmt/whileStmt)
     */
    String  levelChangingNode;

    /**
     * Secrecy level the node is associated with.
     */
    String secrecyLevel;

    /**
     * This is the constructor for a new ProgramCountNode.
     * @param levelChangingNode - the name of the new Object
     * @param secrecylevel - the level of the new Object
     */
    public ProgramCountNode(String levelChangingNode, String secrecyLevel) {
        this.levelChangingNode = levelChangingNode;
        this.secrecyLevel = secrecyLevel;
    }

    /**
     * Getter for the secrecylevel
     * @return - returns the secrecylevel of a ProgramCountNode.
     */
    public String getSecrecyLevel(){
        return secrecyLevel;
    }

    /**
     * Custom implementation of the toString() method
     * @return - returns the string of (name, level)
     */
    public String toString() {
        return "(" + levelChangingNode + ", " + secrecyLevel + ")";
    }

}
