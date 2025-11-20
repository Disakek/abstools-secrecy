package org.abs_models.frontend.typechecker.ext;

import org.abs_models.frontend.ast.*;

public class ProgramCountNode {

    ASTNode<?> levelChangingNode;

    String secrecyLevel;

    public ProgramCountNode(ASTNode<?> levelChangingNode, String secrecyLevel) {
        this.levelChangingNode = levelChangingNode;
        this.secrecyLevel = secrecyLevel;
    }

    public String getSecrecyLevel(){
        return secrecyLevel;
    }

}