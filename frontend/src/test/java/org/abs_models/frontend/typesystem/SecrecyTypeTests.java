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
import java.util.List;
import java.util.LinkedList;
import java.util.Set;
import org.abs_models.frontend.analyser.SemanticCondition;  // For iteration type


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

//Below here are my new imports
import static java.nio.file.Files.readString;
import static java.nio.file.Files.lines;
import static java.util.stream.Collectors.toSet;
import java.nio.file.Path;
import java.nio.file.Paths;


public class SecrecyTypeTests extends FrontendTest {

    //./gradlew test --tests org.abs_models.frontend.common.SecrecyTypeTests

    
    
    @Test
    public void amtOftBanerjee1() throws Exception {
        String fileName = "abssamples/SecrecyTypeTests/passingtests/AmtoftBanerjeeAnnotated1.abs";
        assertTypeCheckFileOk(fileName);
    }
    
    @Test
    public void objectOrientation() throws Exception {
        String fileName = "abssamples/SecrecyTypeTests/failingtests/ObjectorientationAnnotated.abs";
        Model m = assertParseFileOk(fileName);
        
        assertEquals(loadExpectedErrors(fileName.replace(".abs", ".txt")), getLinesAndErrors(m.getTypeErrors()));
    }

    private List<String> getLinesAndErrors(SemanticConditionList errorList) {
        List<String> actual = new LinkedList<String>();
        for (SemanticCondition cond : errorList) {
            if(cond.msg != null && isSecrecyError(cond.msg)){
                if (cond.isError() || cond.isWarning()) {  // Filter errors/warnings
                    String key = cond.getLine() + ":" + cond.getMessage();  // Adjust getters as needed
                    actual.add(key);
                }
            }
        }
        return actual;
    }

    private boolean isSecrecyError(ErrorMessage msg) {
        return msg == ErrorMessage.WRONG_SECRECY_ANNOTATION_VALUE ||
               msg == ErrorMessage.SECRECY_LEAKAGE_ERROR_FROM_TO ||
               msg == ErrorMessage.SECRECY_LEAKAGE_ERROR_AT_MOST ||
               msg == ErrorMessage.SECRECY_PARAMETER_TO_HIGH;
    }

    private List<String> loadExpectedErrors(String expectedFilePath) throws Exception {
        Path expectedPath = Paths.get("src/test/resources/", expectedFilePath);
        return lines(expectedPath).map(String::trim).filter(line -> !line.isEmpty()).toList();
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
