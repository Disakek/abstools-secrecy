/**
 * Copyright (c) 2009-2011, The HATS Consortium. All rights reserved. 
 * This file is licensed under the terms of the Modified BSD License.
 * Written by @Maximilian_Paul for questions please refer to uukln@student.kit.edu
 */
package org.abs_models.frontend.typechecker.ext;

import java.util.HashMap;
import org.abs_models.frontend.ast.*;
import org.abs_models.frontend.typechecker.*;

public class SecrecyExpVisitor {

    private HashMap<ASTNode<?>,String> _secrecy = new HashMap<>();

    private SecrecyLatticeStructure secrecyLatticeStructure;

    public SecrecyExpVisitor(HashMap<ASTNode<?>,String> _secrecy, SecrecyLatticeStructure secrecyLatticeStructure) {
        this._secrecy = _secrecy;
        this.secrecyLatticeStructure = secrecyLatticeStructure;
    }

    //No matter what Expression is to be visited returning the least upper bound of secrecy values or null if there is none
    //TODO check if the implements are needed/used or remove them

    public String visit(Exp expression){
        return null;
    }

    public String visit(AddAddExp addAddExp) {
        // Visit subexpressions first
        String leftLevel = addAddExp.getLeft().accept(this);
        String rightLevel = addAddExp.getRight().accept(this);
        //System.out.println(addAddExp);

        if(leftLevel == null && rightLevel != null) return rightLevel;
        if(rightLevel == null && leftLevel != null) return leftLevel;
        if(rightLevel == null && leftLevel == null) return null;

        // Combine levels — the stricter secrecy wins
        String combined = secrecyLatticeStructure.join(leftLevel, rightLevel);

        System.out.println("AddAddExp: (" + leftLevel + " + " + rightLevel + ") -> " + combined);

        return combined;
    }
    
    public String visit(VarOrFieldUse varOrFieldUse) {

        ASTNode<?> variable = varOrFieldUse.getDecl();
        String secrecy = _secrecy.get(variable);
        if (secrecy != null)System.out.println("VarOrFieldExp: "  + " -> " + secrecy);
        return secrecy;
    }

}