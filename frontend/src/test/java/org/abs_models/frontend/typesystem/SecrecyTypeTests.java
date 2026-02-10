/** 
 * Copyright (c) 2009-2011, The HATS Consortium. All rights reserved. 
 * This file is licensed under the terms of the Modified BSD License.
 */
package org.abs_models.frontend.common;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Collection;

import org.abs_models.ABSTest;
import org.abs_models.frontend.FrontendTest;
import org.abs_models.frontend.analyser.ErrorMessage;
import org.abs_models.frontend.analyser.SemanticConditionList;
import org.abs_models.frontend.ast.Block;
import org.abs_models.frontend.ast.ClassDecl;
import org.abs_models.frontend.ast.DataTypeUse;
import org.abs_models.frontend.ast.FieldUse;
import org.abs_models.frontend.ast.InterfaceDecl;
import org.abs_models.frontend.ast.InterfaceTypeUse;
import org.abs_models.frontend.ast.MethodImpl;
import org.abs_models.frontend.ast.MethodSig;
import org.abs_models.frontend.ast.Model;
import org.abs_models.frontend.ast.ModuleDecl;
import org.abs_models.frontend.ast.ParamDecl;
import org.abs_models.frontend.ast.ParametricDataTypeUse;
import org.abs_models.frontend.ast.ReturnStmt;
import org.abs_models.frontend.ast.TypeUse;
import org.abs_models.frontend.ast.VarDeclStmt;
import org.abs_models.frontend.ast.VarOrFieldUse;
import org.abs_models.frontend.ast.VarUse;
import org.abs_models.frontend.typechecker.InterfaceType;
import org.abs_models.frontend.typechecker.KindedName;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

public class SecrecyTypeTests extends FrontendTest {

    //./gradlew test --tests org.abs_models.frontend.common.SecrecyTypeTests

    
    
    @Test
    public void amtOftBanerjee1() throws Exception {
        String fileName = "abssamples/SecrecyTypeTests/AmtoftBanerjeeAnnotated1.abs";
        assertTypeCheckFileOk(fileName);
    }
    
    @Test
    public void objectOrientation() throws Exception {
        String fileName = "abssamples/SecrecyTypeTests/ObjectorientationAnnotated.abs";
        Model m = assertParseFileOk(fileName);
        assertTrue("Has expected secrecy leakage errors", m.hasTypeErrors());
    }

    /*
    
    assertTypeErrors
    assertE

    @Test
    public void tch_npe() throws Exception {
        assertTypeErrors("module Test; import ABS.StdLib.Bar; { ABS.StdLib.Bar x; }", Config.EXPECT_TYPE_ERROR);
    }


    @Test
    public void ticket414_futNeedsDataType1() {
        Model m = assertParse("module M; interface I {} { Fut<I> fi; }");
        assertFalse(m.hasErrors());
        Block b = m.getMainBlock();
        assertNotNull(b);
        VarDeclStmt s = (VarDeclStmt) b.getStmt(0);
        ParametricDataTypeUse u = (ParametricDataTypeUse) s.getVarDecl().getTypeUse();
        // Have:
        TypeUse tu = u.getParam(0);
        assertEquals("I",tu.getName());
        assertThat(tu, instanceOf(InterfaceTypeUse.class));
        assertThat(tu.getType(), instanceOf(InterfaceType.class));
        assertThat(tu.getDecl(), instanceOf(InterfaceDecl.class));
    }
    */

}
