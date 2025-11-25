/**
 * Copyright (c) 2009-2011, The HATS Consortium. All rights reserved. 
 * This file is licensed under the terms of the Modified BSD License.
 * Written by @Maximilian_Paul for questions please refer to uukln@student.kit.edu
 */
package org.abs_models.frontend.typechecker.ext;

import org.abs_models.frontend.ast.*;

public class ProgramCountNode {

    String  levelChangingNode;

    String secrecyLevel;

    public ProgramCountNode(String levelChangingNode, String secrecyLevel) {
        this.levelChangingNode = levelChangingNode;
        this.secrecyLevel = secrecyLevel;
    }

    public String getSecrecyLevel(){
        return secrecyLevel;
    }

    public String toString() {

        if (levelChangingNode == null) {
            return "(default, " + secrecyLevel + ")";
        }

        //return "(" + levelChangingNode.getClass().getSimpleName() + ", " + secrecyLevel + ")";
        return "(" + levelChangingNode + ", " + secrecyLevel + ")";
    }

}
