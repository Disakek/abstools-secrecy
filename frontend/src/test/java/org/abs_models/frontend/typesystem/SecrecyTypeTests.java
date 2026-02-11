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
//TODO Test if all of those are used/needed
import static java.nio.file.Files.readString;
import static java.nio.file.Files.lines;
import static java.util.stream.Collectors.toSet;
import java.nio.file.Path;
import java.nio.file.Paths;

public class SecrecyTypeTests extends FrontendTest {

    /*
    To only run the tests from this file run this command:
        ./gradlew test --tests org.abs_models.frontend.common.SecrecyTypeTests

    The tests for the secrecy type can be split into two categories. 
        1. Tests that are completly working and where no leak is expected/found.
           These are named passingTests and there are 9 files for these.
        
        2. Tests that are partially or completly leaking and thus are expected to have certain type errors.
           These are named failingTests and there are 11. (once the 4 examples from MiniExamples are added!!)
           These consist of two files per Test 1 the source code (.abs) and the other the expected errors (.txt) 
    */

    //These are the tests that should work without returning any type errors for the secrecy type

    @Test
    public void ifEfficiencyExample() throws Exception {
        //name of the folders and at the end the name of the file
        String fileName = "abssamples/SecrecyTypeTests/passingtests/IfEfficiencyExampleAnnotated.abs";
        //ensuring that no type errors occur!
        assertTypeCheckFileOk(fileName);
    }

    @Test
    public void conditionalConfidential() throws Exception {
        String fileName = "abssamples/SecrecyTypeTests/passingtests/ConditionalConfidentialExampleAnnotated.abs";
        assertTypeCheckFileOk(fileName);
    }

    @Test
    public void amtOftBanerjee1() throws Exception {
        String fileName = "abssamples/SecrecyTypeTests/passingtests/AmtoftBanerjeeAnnotated1.abs";
        assertTypeCheckFileOk(fileName);
    }

    @Test
    public void amtOftBanerjee2() throws Exception {
        String fileName = "abssamples/SecrecyTypeTests/passingtests/AmtoftBanerjeeAnnotated2.abs";
        assertTypeCheckFileOk(fileName);
    }

    @Test
    public void naumann() throws Exception {
        String fileName = "abssamples/SecrecyTypeTests/passingtests/NaumannAnnotated.abs";
        assertTypeCheckFileOk(fileName);
    }

    @Test
    public void passwordFile() throws Exception {
        String fileName = "abssamples/SecrecyTypeTests/passingtests/SecurePasswordFileAnnotated.abs";
        assertTypeCheckFileOk(fileName);
    }

    @Test
    public void simpleEvoting() throws Exception {
        String fileName = "abssamples/SecrecyTypeTests/passingtests/SimpleEvotingExampleAnnotated.abs";
        assertTypeCheckFileOk(fileName);
    }

    @Test
    public void sumExample() throws Exception {
        String fileName = "abssamples/SecrecyTypeTests/passingtests/SumExampleAnnotated.abs";
        assertTypeCheckFileOk(fileName);
    }

    @Test
    public void bankingExample2() throws Exception {
        String fileName = "abssamples/SecrecyTypeTests/passingtests/BankingExampleAnnotated2.abs";
        assertTypeCheckFileOk(fileName);
    }


    //Below are tests that have 1 or more (expected) secrecy type errors
    
    @Test
    public void ifBlockExample() throws Exception {
        //name of the folders and at the end the name of the file
        String fileName = "abssamples/SecrecyTypeTests/failingtests/IfBlockExampleAnnotated.abs";
        //trying to get the model of the source code file
        Model m = assertParseFileOk(fileName);
        //ensuring that the secrecy type errors are exactly those that we specify in the .txt file (same folder and same name)
        assertEquals(loadExpectedErrors(fileName.replace(".abs", ".txt")), getLinesAndErrors(m.getTypeErrors()));
    }
    
    @Test
    public void ifLoopExamples() throws Exception {
        String fileName = "abssamples/SecrecyTypeTests/failingtests/IfLoopExamplesAnnotated.abs";
        Model m = assertParseFileOk(fileName);

        assertEquals(loadExpectedErrors(fileName.replace(".abs", ".txt")), getLinesAndErrors(m.getTypeErrors()));
    }
    
    @Test
    public void ifMethodContract() throws Exception {
        String fileName = "abssamples/SecrecyTypeTests/failingtests/IfMethodContractAnnotated.abs";
        Model m = assertParseFileOk(fileName);

        assertEquals(loadExpectedErrors(fileName.replace(".abs", ".txt")), getLinesAndErrors(m.getTypeErrors()));
    }
    
    //TODO Insert the mini examples here!

    @Test
    public void amtOftBanerjee3() throws Exception {
        String fileName = "abssamples/SecrecyTypeTests/failingtests/AmtoftBanerjeeAnnotated3.abs";
        Model m = assertParseFileOk(fileName);

        assertEquals(loadExpectedErrors(fileName.replace(".abs", ".txt")), getLinesAndErrors(m.getTypeErrors()));
    }

    @Test
    public void objectOrientation() throws Exception {
        String fileName = "abssamples/SecrecyTypeTests/failingtests/ObjectorientationAnnotated.abs";
        Model m = assertParseFileOk(fileName);

        assertEquals(loadExpectedErrors(fileName.replace(".abs", ".txt")), getLinesAndErrors(m.getTypeErrors()));
    }

    @Test
    public void bankingExample1() throws Exception {
        String fileName = "abssamples/SecrecyTypeTests/failingtests/BankingExampleAnnotated1.abs";
        Model m = assertParseFileOk(fileName);

        assertEquals(loadExpectedErrors(fileName.replace(".abs", ".txt")), getLinesAndErrors(m.getTypeErrors()));
    }

    @Test
    public void toyVoting() throws Exception {
        String fileName = "abssamples/SecrecyTypeTests/failingtests/VoterAnnotated.abs";
        Model m = assertParseFileOk(fileName);

        assertEquals(loadExpectedErrors(fileName.replace(".abs", ".txt")), getLinesAndErrors(m.getTypeErrors()));
    }

    //These are helping methods to verify the failing files fail on the exact erros we expect them to

    //Returns a list of the secrecy type errors for a test (it's SemanticConditionList) 
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

    //Returns true if the ErrorMessage is one of the secrecy type error messages (and thus it is a secrecy type error!)
    private boolean isSecrecyError(ErrorMessage msg) {
        return msg == ErrorMessage.WRONG_SECRECY_ANNOTATION_VALUE ||
               msg == ErrorMessage.SECRECY_LEAKAGE_ERROR_FROM_TO ||
               msg == ErrorMessage.SECRECY_LEAKAGE_ERROR_AT_MOST ||
               msg == ErrorMessage.SECRECY_PARAMETER_TO_HIGH;
    }

    //Reads the expected errors from the specified (.txt) file
    private List<String> loadExpectedErrors(String expectedFilePath) throws Exception {
        Path expectedPath = Paths.get("src/test/resources/", expectedFilePath);
        return lines(expectedPath).map(String::trim).filter(line -> !line.isEmpty()).toList();
    }

}
