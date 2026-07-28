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

import static java.nio.file.Files.lines;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.abs_models.frontend.parser.Main;
import java.io.File;
import java.util.Arrays;

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
    public void ifEfficiencyExample_flow_sensitive() throws Exception {
        //name of the folders and at the end the name of the file
        String fileName = "abssamples/SecrecyTypeTests/KeY_Examples/passingtests/IfEfficiencyExampleAnnotated.abs";
        //ensuring that no type errors occur!
        assertTypeCheckFileOk(fileName);
    }

    @Test
    public void conditionalConfidential_flow_sensitive() throws Exception {
        String fileName = "abssamples/SecrecyTypeTests/KeY_Examples/passingtests/ConditionalConfidentialExampleAnnotated.abs";
        assertTypeCheckFileOk(fileName);
    }

    @Test
    public void amtOftBanerjee1_flow_sensitive() throws Exception {
        String fileName = "abssamples/SecrecyTypeTests/KeY_Examples/passingtests/AmtoftBanerjeeAnnotated1.abs";
        assertTypeCheckFileOk(fileName);
    }

    @Test
    public void amtOftBanerjee2_flow_sensitive() throws Exception {
        String fileName = "abssamples/SecrecyTypeTests/KeY_Examples/passingtests/AmtoftBanerjeeAnnotated2.abs";
        assertTypeCheckFileOk(fileName);
    }

    @Test
    public void naumann_flow_sensitive() throws Exception {
        String fileName = "abssamples/SecrecyTypeTests/KeY_Examples/passingtests/NaumannAnnotated.abs";
        assertTypeCheckFileOk(fileName);
    }

    @Test
    public void passwordFileWithDeclassify_flow_sensitive() throws Exception {
        String fileName = "abssamples/SecrecyTypeTests/KeY_Examples/passingtests/SecurePasswordFileDeclassifyingAnnotated.abs";
        assertTypeCheckFileOk(fileName);
    }

    @Test
    public void bankingExample2_flow_sensitive() throws Exception {
        String fileName = "abssamples/SecrecyTypeTests/KeY_Examples/passingtests/BankingExampleAnnotated2.abs";
        assertTypeCheckFileOk(fileName);
    }


    //Below are tests that have 1 or more (expected) secrecy type errors
    
    @Test
    public void ifBlockExample_flow_sensitive() throws Exception {
        //name of the folders and at the end the name of the file
        //the file with the expected secrecy errors has to have the same name but as a .txt file
        String fileName = "abssamples/SecrecyTypeTests/KeY_Examples/failingtests/IfBlockExampleAnnotated.abs";
        //trying to get the model of the source code file
        Model m = assertParseFileOk(fileName);
        //ensuring that the secrecy type errors are exactly those that we specify in the .txt file (same folder and same name)
        assertEquals(loadExpectedErrors(fileName.replace(".abs", ".txt")), getLinesAndErrors(m.getTypeErrors()));
    }

    @Test
    public void simpleEvoting_flow_sensitive() throws Exception {
        String fileName = "abssamples/SecrecyTypeTests/KeY_Examples/failingtests/SimpleEvotingExampleAnnotated.abs";
        Model m = assertParseFileOk(fileName);
        assertEquals(loadExpectedErrors(fileName.replace(".abs", ".txt")), getLinesAndErrors(m.getTypeErrors()));
    }
    
    @Test
    public void ifLoopExamples_flow_sensitive() throws Exception {
        String fileName = "abssamples/SecrecyTypeTests/KeY_Examples/failingtests/IfLoopExamplesAnnotated.abs";
        Model m = assertParseFileOk(fileName);
        assertEquals(loadExpectedErrors(fileName.replace(".abs", ".txt")), getLinesAndErrors(m.getTypeErrors()));
    }
    
    @Test
    public void ifMethodContract_flow_sensitive() throws Exception {
        String fileName = "abssamples/SecrecyTypeTests/KeY_Examples/failingtests/IfMethodContractAnnotated.abs";
        Model m = assertParseFileOk(fileName);
        assertEquals(loadExpectedErrors(fileName.replace(".abs", ".txt")), getLinesAndErrors(m.getTypeErrors()));
    }

    @Test
    public void aliasingExamples_flow_sensitive() throws Exception {
        String fileName = "abssamples/SecrecyTypeTests/KeY_Examples/failingtests/AliasingExamplesAnnotated.abs";
        Model m = assertParseFileOk(fileName);

        assertEquals(loadExpectedErrors(fileName.replace(".abs", ".txt")), getLinesAndErrors(m.getTypeErrors()));
    }
    
    @Test
    public void differenceSeqLocset_flow_sensitive() throws Exception {
        String fileName = "abssamples/SecrecyTypeTests/KeY_Examples/failingtests/DifferenceSeqLocsetAnnotated.abs";
        Model m = assertParseFileOk(fileName);

        assertEquals(loadExpectedErrors(fileName.replace(".abs", ".txt")), getLinesAndErrors(m.getTypeErrors()));
    }
    
    @Test
    public void miniExamples_flow_sensitive() throws Exception {
        String fileName = "abssamples/SecrecyTypeTests/KeY_Examples/failingtests/MiniExamplesAnnotated.abs";
        Model m = assertParseFileOk(fileName);

        assertEquals(loadExpectedErrors(fileName.replace(".abs", ".txt")), getLinesAndErrors(m.getTypeErrors()));
    }
    
    @Test
    public void miniExamplesLecture_flow_sensitive() throws Exception {
        String fileName = "abssamples/SecrecyTypeTests/KeY_Examples/failingtests/MiniExamplesLectureAnnotated.abs";
        Model m = assertParseFileOk(fileName);

        assertEquals(loadExpectedErrors(fileName.replace(".abs", ".txt")), getLinesAndErrors(m.getTypeErrors()));
    }

    @Test
    public void amtOftBanerjee3_flow_sensitive() throws Exception {
        String fileName = "abssamples/SecrecyTypeTests/KeY_Examples/failingtests/AmtoftBanerjeeAnnotated3.abs";
        Model m = assertParseFileOk(fileName);

        assertEquals(loadExpectedErrors(fileName.replace(".abs", ".txt")), getLinesAndErrors(m.getTypeErrors()));
    }

    @Test
    public void objectOrientation_flow_sensitive() throws Exception {
        String fileName = "abssamples/SecrecyTypeTests/KeY_Examples/failingtests/ObjectorientationAnnotated.abs";
        Model m = assertParseFileOk(fileName);

        assertEquals(loadExpectedErrors(fileName.replace(".abs", ".txt")), getLinesAndErrors(m.getTypeErrors()));
    }

    @Test
    public void passwordFile_flow_sensitive() throws Exception {
        String fileName = "abssamples/SecrecyTypeTests/KeY_Examples/failingtests/SecurePasswordFileAnnotated.abs";
        Model m = assertParseFileOk(fileName);

        assertEquals(loadExpectedErrors(fileName.replace(".abs", ".txt")), getLinesAndErrors(m.getTypeErrors()));
    }
    
    @Test
    public void sumExample_flow_sensitive() throws Exception {
        String fileName = "abssamples/SecrecyTypeTests/KeY_Examples/failingtests/SumExampleAnnotated.abs";
        Model m = assertParseFileOk(fileName);

        assertEquals(loadExpectedErrors(fileName.replace(".abs", ".txt")), getLinesAndErrors(m.getTypeErrors()));
    }

    @Test
    public void bankingExample1_flow_sensitive() throws Exception {
        String fileName = "abssamples/SecrecyTypeTests/KeY_Examples/failingtests/BankingExampleAnnotated1.abs";
        Model m = assertParseFileOk(fileName);

        assertEquals(loadExpectedErrors(fileName.replace(".abs", ".txt")), getLinesAndErrors(m.getTypeErrors()));
    }

    @Test
    public void toyVoting_flow_sensitive() throws Exception {
        String fileName = "abssamples/SecrecyTypeTests/KeY_Examples/failingtests/VoterAnnotated.abs";
        Model m = assertParseFileOk(fileName);

        assertEquals(loadExpectedErrors(fileName.replace(".abs", ".txt")), getLinesAndErrors(m.getTypeErrors()));
    }

    @Test
    public void whileInterLoopLeak_flow_sensitive() throws Exception {
        String fileName = "abssamples/SecrecyTypeTests/KeY_Examples/failingtests/WhileInterLoopLeak.abs";
        Model m = assertParseFileOk(fileName);

        assertEquals(loadExpectedErrors(fileName.replace(".abs", ".txt")), getLinesAndErrors(m.getTypeErrors()));
    }

    @Test
    public void thesis_example_01_flow_sensitive() throws Exception {
        String fileName = "abssamples/SecrecyTypeTests/thesis_examples/01_Returnvalue_respect_declared.abs";
        Model m = assertParseFileOk(fileName);

        assertEquals(loadExpectedErrors(fileName.replace(".abs", ".txt")), getLinesAndErrors(m.getTypeErrors()));
    }

    @Test
    public void thesis_example_02_flow_sensitive() throws Exception {
        String fileName = "abssamples/SecrecyTypeTests/thesis_examples/02_Parameter_respect_declared.abs";
        Model m = assertParseFileOk(fileName);

        assertEquals(loadExpectedErrors(fileName.replace(".abs", ".txt")), getLinesAndErrors(m.getTypeErrors()));
    }

    @Test
    public void thesis_example_03_flow_sensitive() throws Exception {
        String fileName = "abssamples/SecrecyTypeTests/thesis_examples/03_Returnvalue_respect_impl_sig.abs";
        Model m = assertParseFileOk(fileName);

        assertEquals(loadExpectedErrors(fileName.replace(".abs", ".txt")), getLinesAndErrors(m.getTypeErrors()));
    }

    @Test
    public void thesis_example_04_flow_sensitive() throws Exception {
        String fileName = "abssamples/SecrecyTypeTests/thesis_examples/04_Expression_implicit_leak.abs";
        Model m = assertParseFileOk(fileName);

        assertEquals(loadExpectedErrors(fileName.replace(".abs", ".txt")), getLinesAndErrors(m.getTypeErrors()));
    }

    @Test
    public void thesis_example_05_flow_sensitive() throws Exception {
        String fileName = "abssamples/SecrecyTypeTests/thesis_examples/05_Expression_only_pc_level.abs";
        Model m = assertParseFileOk(fileName);

        assertEquals(loadExpectedErrors(fileName.replace(".abs", ".txt")), getLinesAndErrors(m.getTypeErrors()));
    }

    @Test
    public void thesis_example_06_flow_sensitive() throws Exception {
        String fileName = "abssamples/SecrecyTypeTests/thesis_examples/06_New_too_high_class_param.abs";
        Model m = assertParseFileOk(fileName);

        assertEquals(loadExpectedErrors(fileName.replace(".abs", ".txt")), getLinesAndErrors(m.getTypeErrors()));
    }

    @Test
    public void thesis_example_07_flow_sensitive() throws Exception {
        String fileName = "abssamples/SecrecyTypeTests/thesis_examples/07_Parameter_of_call_too_high.abs";
        Model m = assertParseFileOk(fileName);

        assertEquals(loadExpectedErrors(fileName.replace(".abs", ".txt")), getLinesAndErrors(m.getTypeErrors()));
    }

    @Test
    public void thesis_example_08_flow_sensitive() throws Exception {
        String fileName = "abssamples/SecrecyTypeTests/thesis_examples/08_Call_only_pc_leak_miss.abs";
        Model m = assertParseFileOk(fileName);

        assertEquals(loadExpectedErrors(fileName.replace(".abs", ".txt")), getLinesAndErrors(m.getTypeErrors()));
    }

    @Test
    public void thesis_example_09_flow_sensitive() throws Exception {
        String fileName = "abssamples/SecrecyTypeTests/thesis_examples/09_Call_only_returnlevel_miss.abs";
        Model m = assertParseFileOk(fileName);

        assertEquals(loadExpectedErrors(fileName.replace(".abs", ".txt")), getLinesAndErrors(m.getTypeErrors()));
    }

    @Test
    public void thesis_example_10_flow_sensitive() throws Exception {
        String fileName = "abssamples/SecrecyTypeTests/thesis_examples/10_Basic_explicit_example.abs";
        Model m = assertParseFileOk(fileName);

        assertEquals(loadExpectedErrors(fileName.replace(".abs", ".txt")), getLinesAndErrors(m.getTypeErrors()));
    }

    @Test
    public void thesis_example_11_flow_sensitive() throws Exception {

        //Test 11 from the thesis that is made to highlight the difference between flow-sensitive and flow-insensitive analysis
        //Only contains error when checked with flow-insensitive rules (this one here is a passing test)
        String fileName = "abssamples/SecrecyTypeTests/thesis_examples/11_flow_in_sensitivity_example.abs";
        assertTypeCheckFileOk(fileName);
    }

    @Test
    public void thesis_example_12_flow_sensitive() throws Exception {
        String fileName = "abssamples/SecrecyTypeTests/thesis_examples/12_Field_exceeds_declared.abs";
        Model m = assertParseFileOk(fileName);

        assertEquals(loadExpectedErrors(fileName.replace(".abs", ".txt")), getLinesAndErrors(m.getTypeErrors()));
    }

    @Test
    public void thesis_example_13_flow_sensitive() throws Exception {
        String fileName = "abssamples/SecrecyTypeTests/thesis_examples/13_Await_continue_leak.abs";
        Model m = assertParseFileOk(fileName);

        assertEquals(loadExpectedErrors(fileName.replace(".abs", ".txt")), getLinesAndErrors(m.getTypeErrors()));
    }

    @Test
    public void thesis_example_14_flow_sensitive() throws Exception {
        String fileName = "abssamples/SecrecyTypeTests/thesis_examples/14_Future_example.abs";
        assertTypeCheckFileOk(fileName);
    }

    @Test
    public void thesis_example_15_flow_sensitive() throws Exception {
        String fileName = "abssamples/SecrecyTypeTests/thesis_examples/15_If_implicit_leak.abs";
        Model m = assertParseFileOk(fileName);

        assertEquals(loadExpectedErrors(fileName.replace(".abs", ".txt")), getLinesAndErrors(m.getTypeErrors()));
    }

    @Test
    public void thesis_example_16_flow_sensitive() throws Exception {
        String fileName = "abssamples/SecrecyTypeTests/thesis_examples/16_While_implicit_ignoring_pc.abs";
        Model m = assertParseFileOk(fileName);

        assertEquals(loadExpectedErrors(fileName.replace(".abs", ".txt")), getLinesAndErrors(m.getTypeErrors()));
    }

    @Test
    public void thesis_example_17_flow_sensitive() throws Exception {
        String fileName = "abssamples/SecrecyTypeTests/thesis_examples/17_While_missed_leak.abs";
        assertTypeCheckFileOk(fileName);
    }

    @Test
    public void thesis_example_18_flow_sensitive() throws Exception {
        String fileName = "abssamples/SecrecyTypeTests/thesis_examples/18_While_second_iteration_leak_missed.abs";
        Model m = assertParseFileOk(fileName);

        assertEquals(loadExpectedErrors(fileName.replace(".abs", ".txt")), getLinesAndErrors(m.getTypeErrors()));
    }

    @Test
    public void thesis_example_19_flow_sensitive() throws Exception {
        String fileName = "abssamples/SecrecyTypeTests/thesis_examples/19_While_second_iteration_leak_findable.abs";
        Model m = assertParseFileOk(fileName);

        assertEquals(loadExpectedErrors(fileName.replace(".abs", ".txt")), getLinesAndErrors(m.getTypeErrors()));
    }

    //Below are follow the rule examples where each rule that is used should be checked with at least one test (flow-sensitive first)

    @Test
    public void assign_rule_examples_flow_sensitive() throws Exception {
        String fileName = "abssamples/SecrecyTypeTests/Rule_examples/Assign_Examples.abs";
        Model m = assertParseFileOk(fileName);

        assertEquals(loadExpectedErrors(fileName.replace(".abs", ".txt")), getLinesAndErrors(m.getTypeErrors()));
    }

    @Test
    public void await_rule_examples_flow_sensitive() throws Exception {
        String fileName = "abssamples/SecrecyTypeTests/Rule_examples/Await_Examples.abs";
        Model m = assertParseFileOk(fileName);

        assertEquals(loadExpectedErrors(fileName.replace(".abs", ".txt")), getLinesAndErrors(m.getTypeErrors()));
    }

    @Test
    public void awaitcall_rule_examples_flow_sensitive() throws Exception {
        String fileName = "abssamples/SecrecyTypeTests/Rule_examples/Await-Call_Examples.abs";
        Model m = assertParseFileOk(fileName);

        assertEquals(loadExpectedErrors(fileName.replace(".abs", ".txt")), getLinesAndErrors(m.getTypeErrors()));
    }
    
    @Test
    public void expression_rule_examples_flow_sensitive() throws Exception {
        String fileName = "abssamples/SecrecyTypeTests/Rule_examples/Expression_Examples.abs";
        Model m = assertParseFileOk(fileName);

        assertEquals(loadExpectedErrors(fileName.replace(".abs", ".txt")), getLinesAndErrors(m.getTypeErrors()));
    }

    @Test
    public void future_rule_examples_flow_sensitive() throws Exception {
        String fileName = "abssamples/SecrecyTypeTests/Rule_examples/Future_Examples.abs";
        Model m = assertParseFileOk(fileName);

        assertEquals(loadExpectedErrors(fileName.replace(".abs", ".txt")), getLinesAndErrors(m.getTypeErrors()));
    }

    @Test
    public void if_rule_examples_flow_sensitive() throws Exception {
        String fileName = "abssamples/SecrecyTypeTests/Rule_examples/If_Examples.abs";
        Model m = assertParseFileOk(fileName);

        assertEquals(loadExpectedErrors(fileName.replace(".abs", ".txt")), getLinesAndErrors(m.getTypeErrors()));
    }

    @Test
    public void Interface_implementation_rule_examples_flow_sensitive() throws Exception {
        String fileName = "abssamples/SecrecyTypeTests/Rule_examples/Interface_Implementation_Examples.abs";
        Model m = assertParseFileOk(fileName);

        assertEquals(loadExpectedErrors(fileName.replace(".abs", ".txt")), getLinesAndErrors(m.getTypeErrors()));
    }

    @Test
    public void new_rule_examples_flow_sensitive() throws Exception {
        String fileName = "abssamples/SecrecyTypeTests/Rule_examples/New_Examples.abs";
        Model m = assertParseFileOk(fileName);

        assertEquals(loadExpectedErrors(fileName.replace(".abs", ".txt")), getLinesAndErrors(m.getTypeErrors()));
    }

    @Test
    public void return_rule_examples_flow_sensitive() throws Exception {
        String fileName = "abssamples/SecrecyTypeTests/Rule_examples/Return_Examples.abs";
        Model m = assertParseFileOk(fileName);

        assertEquals(loadExpectedErrors(fileName.replace(".abs", ".txt")), getLinesAndErrors(m.getTypeErrors()));
    }

    @Test
    public void while_rule_examples_flow_sensitive() throws Exception {
        String fileName = "abssamples/SecrecyTypeTests/Rule_examples/While_Examples.abs";
        Model m = assertParseFileOk(fileName);

        assertEquals(loadExpectedErrors(fileName.replace(".abs", ".txt")), getLinesAndErrors(m.getTypeErrors()));
    }

    @Test
    public void while_interloopleak_rule_examples_flow_sensitive() throws Exception {
        String fileName = "abssamples/SecrecyTypeTests/Rule_examples/WhileInterLoopLeak.abs";
        Model m = assertParseFileOk(fileName);

        assertEquals(loadExpectedErrors(fileName.replace(".abs", ".txt")), getLinesAndErrors(m.getTypeErrors()));
    }


    @Test
    public void thesis_example_20_flow_sensitive() throws Exception {
        String fileName = "abssamples/SecrecyTypeTests/thesis_examples/20_ServerDataRequest.abs";
        Model m = assertParseFileOk(fileName);

        assertEquals(loadExpectedErrors(fileName.replace(".abs", ".txt")), getLinesAndErrors(m.getTypeErrors()));
    }

    //Below this line I do the test for a flow-insensitive analysis
    //These are the tests that should work without returning any type errors for the secrecy type

    //Since the first ones have no type errors (passing tests) they get compared to an empty list
    //Using assertTypeCheckFileOk doesnt work for this analysis because the option needs to be true!!
    //assertTypeCheckFileOk would just read in the model new and completly ignore earlier options (--flow-insensitive = true)

    @Test
    public void ifEfficiencyExample_flow_insensitive() throws Exception {
        //name of the folders and at the end the name of the file
        String fileName = "abssamples/SecrecyTypeTests/KeY_Examples/passingtests/IfEfficiencyExampleAnnotated.abs";
        //ensuring that no type errors occur!
        
        Main main = new Main();
        main.arguments.flowInsensitive = true;
        Model m = main.parse(Arrays.asList(new File(resolveFileName(fileName))));
        m.evaluateAllProductDeclarations();

        assertEquals(new LinkedList<String>(), getLinesAndErrors(m.getTypeErrors()));
    }

    @Test
    public void conditionalConfidential_flow_insensitive() throws Exception {
        String fileName = "abssamples/SecrecyTypeTests/KeY_Examples/passingtests/ConditionalConfidentialExampleAnnotated.abs";
        
        Main main = new Main();
        main.arguments.flowInsensitive = true;
        Model m = main.parse(Arrays.asList(new File(resolveFileName(fileName))));
        m.evaluateAllProductDeclarations();

        assertEquals(new LinkedList<String>(), getLinesAndErrors(m.getTypeErrors()));
    }

    @Test
    public void amtOftBanerjee1_flow_insensitive() throws Exception {
        String fileName = "abssamples/SecrecyTypeTests/KeY_Examples/passingtests/AmtoftBanerjeeAnnotated1.abs";
        
        Main main = new Main();
        main.arguments.flowInsensitive = true;
        Model m = main.parse(Arrays.asList(new File(resolveFileName(fileName))));
        m.evaluateAllProductDeclarations();

        assertEquals(new LinkedList<String>(), getLinesAndErrors(m.getTypeErrors()));
    }

    @Test
    public void amtOftBanerjee2_flow_insensitive() throws Exception {
        String fileName = "abssamples/SecrecyTypeTests/KeY_Examples/passingtests/AmtoftBanerjeeAnnotated2.abs";
        
        Main main = new Main();
        main.arguments.flowInsensitive = true;
        Model m = main.parse(Arrays.asList(new File(resolveFileName(fileName))));
        m.evaluateAllProductDeclarations();

        assertEquals(new LinkedList<String>(), getLinesAndErrors(m.getTypeErrors()));
    }

    @Test
    public void naumann_flow_insensitive() throws Exception {
        String fileName = "abssamples/SecrecyTypeTests/KeY_Examples/passingtests/NaumannAnnotated.abs";

        Main main = new Main();
        main.arguments.flowInsensitive = true;
        Model m = main.parse(Arrays.asList(new File(resolveFileName(fileName))));
        m.evaluateAllProductDeclarations();

        assertEquals(new LinkedList<String>(), getLinesAndErrors(m.getTypeErrors()));
    }

    @Test
    public void passwordFileWithDeclassify_flow_insensitive() throws Exception {
        String fileName = "abssamples/SecrecyTypeTests/KeY_Examples/passingtests/SecurePasswordFileDeclassifyingAnnotated.abs";

        Main main = new Main();
        main.arguments.flowInsensitive = true;
        Model m = main.parse(Arrays.asList(new File(resolveFileName(fileName))));
        m.evaluateAllProductDeclarations();

        assertEquals(new LinkedList<String>(), getLinesAndErrors(m.getTypeErrors()));
    }

    @Test
    public void bankingExample2_flow_insensitive() throws Exception {
        String fileName = "abssamples/SecrecyTypeTests/KeY_Examples/passingtests/BankingExampleAnnotated2.abs";
        
        Main main = new Main();
        main.arguments.flowInsensitive = true;
        Model m = main.parse(Arrays.asList(new File(resolveFileName(fileName))));
        m.evaluateAllProductDeclarations();

        assertEquals(new LinkedList<String>(), getLinesAndErrors(m.getTypeErrors()));
    }
    
    //Below are tests that have 1 or more (expected) secrecy type errors
    
    @Test
    public void ifBlockExample_flow_insensitive() throws Exception {
        String fileName = "abssamples/SecrecyTypeTests/KeY_Examples/failingtests/IfBlockExampleAnnotated.abs";
        
        //Same errors as flow_sensitive one
        Main main = new Main();
        main.arguments.flowInsensitive = true;
        Model m = main.parse(Arrays.asList(new File(resolveFileName(fileName))));
        m.evaluateAllProductDeclarations();
        
        assertEquals(loadExpectedErrors(fileName.replace(".abs", "_flow_insensitive.txt")), getLinesAndErrors(m.getTypeErrors()));
    }
    
    @Test
    public void simpleEvoting_flow_insensitive() throws Exception {
        String fileName = "abssamples/SecrecyTypeTests/KeY_Examples/failingtests/SimpleEvotingExampleAnnotated.abs";
        
        Main main = new Main();
        main.arguments.flowInsensitive = true;
        Model m = main.parse(Arrays.asList(new File(resolveFileName(fileName))));
        m.evaluateAllProductDeclarations();
        
        assertEquals(loadExpectedErrors(fileName.replace(".abs", ".txt")), getLinesAndErrors(m.getTypeErrors()));
    }
    
    @Test
    public void ifLoopExamples_flow_insensitive() throws Exception {
        String fileName = "abssamples/SecrecyTypeTests/KeY_Examples/failingtests/IfLoopExamplesAnnotated.abs";
        
        Main main = new Main();
        main.arguments.flowInsensitive = true;
        Model m = main.parse(Arrays.asList(new File(resolveFileName(fileName))));
        m.evaluateAllProductDeclarations();
        
        assertEquals(loadExpectedErrors(fileName.replace(".abs", "_flow_insensitive.txt")), getLinesAndErrors(m.getTypeErrors()));
    }
    
    @Test
    public void ifMethodContract_flow_insensitive() throws Exception {
        String fileName = "abssamples/SecrecyTypeTests/KeY_Examples/failingtests/IfMethodContractAnnotated.abs";
        
        Main main = new Main();
        main.arguments.flowInsensitive = true;
        Model m = main.parse(Arrays.asList(new File(resolveFileName(fileName))));
        m.evaluateAllProductDeclarations();
        
        assertEquals(loadExpectedErrors(fileName.replace(".abs", ".txt")), getLinesAndErrors(m.getTypeErrors()));
    }

    @Test
    public void aliasingExamples_flow_insensitive() throws Exception {
        String fileName = "abssamples/SecrecyTypeTests/KeY_Examples/failingtests/AliasingExamplesAnnotated.abs";
        
        Main main = new Main();
        main.arguments.flowInsensitive = true;
        Model m = main.parse(Arrays.asList(new File(resolveFileName(fileName))));
        m.evaluateAllProductDeclarations();
        

        assertEquals(loadExpectedErrors(fileName.replace(".abs", ".txt")), getLinesAndErrors(m.getTypeErrors()));
    }
    
    @Test
    public void differenceSeqLocset_flow_insensitive() throws Exception {
        String fileName = "abssamples/SecrecyTypeTests/KeY_Examples/failingtests/DifferenceSeqLocsetAnnotated.abs";
        
        Main main = new Main();
        main.arguments.flowInsensitive = true;
        Model m = main.parse(Arrays.asList(new File(resolveFileName(fileName))));
        m.evaluateAllProductDeclarations();
        

        assertEquals(loadExpectedErrors(fileName.replace(".abs", ".txt")), getLinesAndErrors(m.getTypeErrors()));
    }
    
    @Test
    public void miniExamples_flow_insensitive() throws Exception {
        String fileName = "abssamples/SecrecyTypeTests/KeY_Examples/failingtests/MiniExamplesAnnotated.abs";
        
        Main main = new Main();
        main.arguments.flowInsensitive = true;
        Model m = main.parse(Arrays.asList(new File(resolveFileName(fileName))));
        m.evaluateAllProductDeclarations();
        

        assertEquals(loadExpectedErrors(fileName.replace(".abs", "_flow_insensitive.txt")), getLinesAndErrors(m.getTypeErrors()));
    }
    
    @Test
    public void miniExamplesLecture_flow_insensitive() throws Exception {
        String fileName = "abssamples/SecrecyTypeTests/KeY_Examples/failingtests/MiniExamplesLectureAnnotated.abs";
        
        Main main = new Main();
        main.arguments.flowInsensitive = true;
        Model m = main.parse(Arrays.asList(new File(resolveFileName(fileName))));
        m.evaluateAllProductDeclarations();
        

        assertEquals(loadExpectedErrors(fileName.replace(".abs", "_flow_insensitive.txt")), getLinesAndErrors(m.getTypeErrors()));
    }

    @Test
    public void amtOftBanerjee3_flow_insensitive() throws Exception {
        String fileName = "abssamples/SecrecyTypeTests/KeY_Examples/failingtests/AmtoftBanerjeeAnnotated3.abs";
        
        Main main = new Main();
        main.arguments.flowInsensitive = true;
        Model m = main.parse(Arrays.asList(new File(resolveFileName(fileName))));
        m.evaluateAllProductDeclarations();
        

        assertEquals(loadExpectedErrors(fileName.replace(".abs", ".txt")), getLinesAndErrors(m.getTypeErrors()));
    }

    @Test
    public void objectOrientation_flow_insensitive() throws Exception {
        String fileName = "abssamples/SecrecyTypeTests/KeY_Examples/failingtests/ObjectorientationAnnotated.abs";
        
        Main main = new Main();
        main.arguments.flowInsensitive = true;
        Model m = main.parse(Arrays.asList(new File(resolveFileName(fileName))));
        m.evaluateAllProductDeclarations();
        

        assertEquals(loadExpectedErrors(fileName.replace(".abs", ".txt")), getLinesAndErrors(m.getTypeErrors()));
    }

    @Test
    public void passwordFile_flow_insensitive() throws Exception {
        String fileName = "abssamples/SecrecyTypeTests/KeY_Examples/failingtests/SecurePasswordFileAnnotated.abs";
        
        Main main = new Main();
        main.arguments.flowInsensitive = true;
        Model m = main.parse(Arrays.asList(new File(resolveFileName(fileName))));
        m.evaluateAllProductDeclarations();
        

        assertEquals(loadExpectedErrors(fileName.replace(".abs", ".txt")), getLinesAndErrors(m.getTypeErrors()));
    }
    
    @Test
    public void sumExample_flow_insensitive() throws Exception {
        String fileName = "abssamples/SecrecyTypeTests/KeY_Examples/failingtests/SumExampleAnnotated.abs";
        
        Main main = new Main();
        main.arguments.flowInsensitive = true;
        Model m = main.parse(Arrays.asList(new File(resolveFileName(fileName))));
        m.evaluateAllProductDeclarations();
        

        assertEquals(loadExpectedErrors(fileName.replace(".abs", "_flow_insensitive.txt")), getLinesAndErrors(m.getTypeErrors()));
    }

    @Test
    public void bankingExample1_flow_insensitive() throws Exception {
        String fileName = "abssamples/SecrecyTypeTests/KeY_Examples/failingtests/BankingExampleAnnotated1.abs";
        
        Main main = new Main();
        main.arguments.flowInsensitive = true;
        Model m = main.parse(Arrays.asList(new File(resolveFileName(fileName))));
        m.evaluateAllProductDeclarations();
        

        assertEquals(loadExpectedErrors(fileName.replace(".abs", "_flow_insensitive.txt")), getLinesAndErrors(m.getTypeErrors()));
    }

    @Test
    public void toyVoting_flow_insensitive() throws Exception {
        String fileName = "abssamples/SecrecyTypeTests/KeY_Examples/failingtests/VoterAnnotated.abs";
        
        Main main = new Main();
        main.arguments.flowInsensitive = true;
        Model m = main.parse(Arrays.asList(new File(resolveFileName(fileName))));
        m.evaluateAllProductDeclarations();
        

        assertEquals(loadExpectedErrors(fileName.replace(".abs", "_flow_insensitive.txt")), getLinesAndErrors(m.getTypeErrors()));
    }

    @Test
    public void whileInterLoopLeak_flow_insensitive() throws Exception {
        String fileName = "abssamples/SecrecyTypeTests/KeY_Examples/failingtests/WhileInterLoopLeak.abs";
        
        Main main = new Main();
        main.arguments.flowInsensitive = true;
        Model m = main.parse(Arrays.asList(new File(resolveFileName(fileName))));
        m.evaluateAllProductDeclarations();
        

        assertEquals(loadExpectedErrors(fileName.replace(".abs", "_flow_insensitive.txt")), getLinesAndErrors(m.getTypeErrors()));
    }

    @Test
    public void thesis_example_01_flow_insensitive() throws Exception {
        String fileName = "abssamples/SecrecyTypeTests/thesis_examples/01_Returnvalue_respect_declared.abs";
        
        Main main = new Main();
        main.arguments.flowInsensitive = true;
        Model m = main.parse(Arrays.asList(new File(resolveFileName(fileName))));
        m.evaluateAllProductDeclarations();
        

        assertEquals(loadExpectedErrors(fileName.replace(".abs", ".txt")), getLinesAndErrors(m.getTypeErrors()));
    }

    @Test
    public void thesis_example_02_flow_insensitive() throws Exception {
        String fileName = "abssamples/SecrecyTypeTests/thesis_examples/02_Parameter_respect_declared.abs";
        
        Main main = new Main();
        main.arguments.flowInsensitive = true;
        Model m = main.parse(Arrays.asList(new File(resolveFileName(fileName))));
        m.evaluateAllProductDeclarations();
        

        assertEquals(loadExpectedErrors(fileName.replace(".abs", ".txt")), getLinesAndErrors(m.getTypeErrors()));
    }

    @Test
    public void thesis_example_03_flow_insensitive() throws Exception {
        String fileName = "abssamples/SecrecyTypeTests/thesis_examples/03_Returnvalue_respect_impl_sig.abs";
        
        Main main = new Main();
        main.arguments.flowInsensitive = true;
        Model m = main.parse(Arrays.asList(new File(resolveFileName(fileName))));
        m.evaluateAllProductDeclarations();
        

        assertEquals(loadExpectedErrors(fileName.replace(".abs", ".txt")), getLinesAndErrors(m.getTypeErrors()));
    }

    @Test
    public void thesis_example_04_flow_insensitive() throws Exception {
        String fileName = "abssamples/SecrecyTypeTests/thesis_examples/04_Expression_implicit_leak.abs";
        
        Main main = new Main();
        main.arguments.flowInsensitive = true;
        Model m = main.parse(Arrays.asList(new File(resolveFileName(fileName))));
        m.evaluateAllProductDeclarations();
        

        assertEquals(loadExpectedErrors(fileName.replace(".abs", ".txt")), getLinesAndErrors(m.getTypeErrors()));
    }

    @Test
    public void thesis_example_05_flow_insensitive() throws Exception {
        String fileName = "abssamples/SecrecyTypeTests/thesis_examples/05_Expression_only_pc_level.abs";
        
        Main main = new Main();
        main.arguments.flowInsensitive = true;
        Model m = main.parse(Arrays.asList(new File(resolveFileName(fileName))));
        m.evaluateAllProductDeclarations();
        

        assertEquals(loadExpectedErrors(fileName.replace(".abs", "_flow_insensitive.txt")), getLinesAndErrors(m.getTypeErrors()));
    }

    @Test
    public void thesis_example_06_flow_insensitive() throws Exception {
        String fileName = "abssamples/SecrecyTypeTests/thesis_examples/06_New_too_high_class_param.abs";
        
        Main main = new Main();
        main.arguments.flowInsensitive = true;
        Model m = main.parse(Arrays.asList(new File(resolveFileName(fileName))));
        m.evaluateAllProductDeclarations();
        

        assertEquals(loadExpectedErrors(fileName.replace(".abs", ".txt")), getLinesAndErrors(m.getTypeErrors()));
    }

    @Test
    public void thesis_example_07_flow_insensitive() throws Exception {
        String fileName = "abssamples/SecrecyTypeTests/thesis_examples/07_Parameter_of_call_too_high.abs";
        
        Main main = new Main();
        main.arguments.flowInsensitive = true;
        Model m = main.parse(Arrays.asList(new File(resolveFileName(fileName))));
        m.evaluateAllProductDeclarations();
        

        assertEquals(loadExpectedErrors(fileName.replace(".abs", ".txt")), getLinesAndErrors(m.getTypeErrors()));
    }

    @Test
    public void thesis_example_08_flow_insensitive() throws Exception {
        String fileName = "abssamples/SecrecyTypeTests/thesis_examples/08_Call_only_pc_leak_miss.abs";
        
        Main main = new Main();
        main.arguments.flowInsensitive = true;
        Model m = main.parse(Arrays.asList(new File(resolveFileName(fileName))));
        m.evaluateAllProductDeclarations();
        

        assertEquals(loadExpectedErrors(fileName.replace(".abs", ".txt")), getLinesAndErrors(m.getTypeErrors()));
    }

    @Test
    public void thesis_example_09_flow_insensitive() throws Exception {
        String fileName = "abssamples/SecrecyTypeTests/thesis_examples/09_Call_only_returnlevel_miss.abs";
        
        Main main = new Main();
        main.arguments.flowInsensitive = true;
        Model m = main.parse(Arrays.asList(new File(resolveFileName(fileName))));
        m.evaluateAllProductDeclarations();
        

        assertEquals(loadExpectedErrors(fileName.replace(".abs", ".txt")), getLinesAndErrors(m.getTypeErrors()));
    }

    @Test
    public void thesis_example_10_flow_insensitive() throws Exception {
        String fileName = "abssamples/SecrecyTypeTests/thesis_examples/10_Basic_explicit_example.abs";
        
        Main main = new Main();
        main.arguments.flowInsensitive = true;
        Model m = main.parse(Arrays.asList(new File(resolveFileName(fileName))));
        m.evaluateAllProductDeclarations();
        

        assertEquals(loadExpectedErrors(fileName.replace(".abs", ".txt")), getLinesAndErrors(m.getTypeErrors()));
    }

    @Test
    public void thesis_example_11_flow_insensitive() throws Exception {
        String fileName = "abssamples/SecrecyTypeTests/thesis_examples/11_flow_in_sensitivity_example.abs";
        
        Main main = new Main();
        main.arguments.flowInsensitive = true;
        Model m = main.parse(Arrays.asList(new File(resolveFileName(fileName))));
        m.evaluateAllProductDeclarations();

        assertEquals(loadExpectedErrors(fileName.replace(".abs", "_flow_insensitive.txt")), getLinesAndErrors(m.getTypeErrors()));
    }

    @Test
    public void thesis_example_12_flow_insensitive() throws Exception {
        String fileName = "abssamples/SecrecyTypeTests/thesis_examples/12_Field_exceeds_declared.abs";
        
        Main main = new Main();
        main.arguments.flowInsensitive = true;
        Model m = main.parse(Arrays.asList(new File(resolveFileName(fileName))));
        m.evaluateAllProductDeclarations();
        

        assertEquals(loadExpectedErrors(fileName.replace(".abs", "_flow_insensitive.txt")), getLinesAndErrors(m.getTypeErrors()));
    }

    @Test
    public void thesis_example_13_flow_insensitive() throws Exception {
        String fileName = "abssamples/SecrecyTypeTests/thesis_examples/13_Await_continue_leak.abs";
        
        Main main = new Main();
        main.arguments.flowInsensitive = true;
        Model m = main.parse(Arrays.asList(new File(resolveFileName(fileName))));
        m.evaluateAllProductDeclarations();
        

        assertEquals(loadExpectedErrors(fileName.replace(".abs", "_flow_insensitive.txt")), getLinesAndErrors(m.getTypeErrors()));
    }

    @Test
    public void thesis_example_14_flow_insensitive() throws Exception {
        String fileName = "abssamples/SecrecyTypeTests/thesis_examples/14_Future_example.abs";
        assertTypeCheckFileOk(fileName);
    }

    @Test
    public void thesis_example_15_flow_insensitive() throws Exception {
        String fileName = "abssamples/SecrecyTypeTests/thesis_examples/15_If_implicit_leak.abs";
        
        Main main = new Main();
        main.arguments.flowInsensitive = true;
        Model m = main.parse(Arrays.asList(new File(resolveFileName(fileName))));
        m.evaluateAllProductDeclarations();
        

        assertEquals(loadExpectedErrors(fileName.replace(".abs", "_flow_insensitive.txt")), getLinesAndErrors(m.getTypeErrors()));
    }

    @Test
    public void thesis_example_16_flow_insensitive() throws Exception {
        String fileName = "abssamples/SecrecyTypeTests/thesis_examples/16_While_implicit_ignoring_pc.abs";
        
        Main main = new Main();
        main.arguments.flowInsensitive = true;
        Model m = main.parse(Arrays.asList(new File(resolveFileName(fileName))));
        m.evaluateAllProductDeclarations();
        

        assertEquals(loadExpectedErrors(fileName.replace(".abs", ".txt")), getLinesAndErrors(m.getTypeErrors()));
    }

    @Test
    public void thesis_example_17_flow_insensitive() throws Exception {
        String fileName = "abssamples/SecrecyTypeTests/thesis_examples/17_While_missed_leak.abs";
        assertTypeCheckFileOk(fileName);
    }

    @Test
    public void thesis_example_18_flow_insensitive() throws Exception {
        String fileName = "abssamples/SecrecyTypeTests/thesis_examples/18_While_second_iteration_leak_missed.abs";
        
        Main main = new Main();
        main.arguments.flowInsensitive = true;
        Model m = main.parse(Arrays.asList(new File(resolveFileName(fileName))));
        m.evaluateAllProductDeclarations();
        

        assertEquals(loadExpectedErrors(fileName.replace(".abs", ".txt")), getLinesAndErrors(m.getTypeErrors()));
    }

    @Test
    public void thesis_example_19_flow_insensitive() throws Exception {
        String fileName = "abssamples/SecrecyTypeTests/thesis_examples/19_While_second_iteration_leak_findable.abs";
        
        Main main = new Main();
        main.arguments.flowInsensitive = true;
        Model m = main.parse(Arrays.asList(new File(resolveFileName(fileName))));
        m.evaluateAllProductDeclarations();
        

        assertEquals(loadExpectedErrors(fileName.replace(".abs", "_flow_insensitive.txt")), getLinesAndErrors(m.getTypeErrors()));
    }

    @Test
    public void thesis_example_20_flow_insensitive() throws Exception {
        String fileName = "abssamples/SecrecyTypeTests/thesis_examples/20_ServerDataRequest.abs";
        
        Main main = new Main();
        main.arguments.flowInsensitive = true;
        Model m = main.parse(Arrays.asList(new File(resolveFileName(fileName))));
        m.evaluateAllProductDeclarations();
        

        assertEquals(loadExpectedErrors(fileName.replace(".abs", "_flow_insensitive.txt")), getLinesAndErrors(m.getTypeErrors()));
    }

    //Below are the rule examples but with the flow-insensitive analysis

    @Test
    public void assign_rule_examples_flow_insensitive() throws Exception {
        String fileName = "abssamples/SecrecyTypeTests/Rule_examples/Assign_Examples.abs";
 
        Main main = new Main();
        main.arguments.flowInsensitive = true;
        Model m = main.parse(Arrays.asList(new File(resolveFileName(fileName))));
        m.evaluateAllProductDeclarations(); 

        assertEquals(loadExpectedErrors(fileName.replace(".abs", "_flow_insensitive.txt")), getLinesAndErrors(m.getTypeErrors()));
    }

    @Test
    public void await_rule_examples_flow_insensitive() throws Exception {
        String fileName = "abssamples/SecrecyTypeTests/Rule_examples/Await_Examples.abs";
        

        Main main = new Main();
        main.arguments.flowInsensitive = true;
        Model m = main.parse(Arrays.asList(new File(resolveFileName(fileName))));
        m.evaluateAllProductDeclarations(); 

        
        assertEquals(loadExpectedErrors(fileName.replace(".abs", ".txt")), getLinesAndErrors(m.getTypeErrors()));
    }

    @Test
    public void awaitcall_rule_examples_flow_insensitive() throws Exception {
        String fileName = "abssamples/SecrecyTypeTests/Rule_examples/Await-Call_Examples.abs";
        
        Main main = new Main();
        main.arguments.flowInsensitive = true;
        Model m = main.parse(Arrays.asList(new File(resolveFileName(fileName))));
        m.evaluateAllProductDeclarations(); 

        assertEquals(loadExpectedErrors(fileName.replace(".abs", "_flow_insensitive.txt")), getLinesAndErrors(m.getTypeErrors()));
    }
    
    @Test
    public void expression_rule_examples_flow_insensitive() throws Exception {
        String fileName = "abssamples/SecrecyTypeTests/Rule_examples/Expression_Examples.abs";
        
        Main main = new Main();
        main.arguments.flowInsensitive = true;
        Model m = main.parse(Arrays.asList(new File(resolveFileName(fileName))));
        m.evaluateAllProductDeclarations(); 

        assertEquals(loadExpectedErrors(fileName.replace(".abs", ".txt")), getLinesAndErrors(m.getTypeErrors()));
    }

    @Test
    public void future_rule_examples_flow_insensitive() throws Exception {
        String fileName = "abssamples/SecrecyTypeTests/Rule_examples/Future_Examples.abs";
        
        Main main = new Main();
        main.arguments.flowInsensitive = true;
        Model m = main.parse(Arrays.asList(new File(resolveFileName(fileName))));
        m.evaluateAllProductDeclarations(); 

        assertEquals(loadExpectedErrors(fileName.replace(".abs", "_flow_insensitive.txt")), getLinesAndErrors(m.getTypeErrors()));
    }

    @Test
    public void if_rule_examples_flow_insensitive() throws Exception {
        String fileName = "abssamples/SecrecyTypeTests/Rule_examples/If_Examples.abs";
        
        Main main = new Main();
        main.arguments.flowInsensitive = true;
        Model m = main.parse(Arrays.asList(new File(resolveFileName(fileName))));
        m.evaluateAllProductDeclarations(); 

        assertEquals(loadExpectedErrors(fileName.replace(".abs", "_flow_insensitive.txt")), getLinesAndErrors(m.getTypeErrors()));
    }

    @Test
    public void Interface_implementation_rule_examples_flow_insensitive() throws Exception {
        String fileName = "abssamples/SecrecyTypeTests/Rule_examples/Interface_Implementation_Examples.abs";
        
        Main main = new Main();
        main.arguments.flowInsensitive = true;
        Model m = main.parse(Arrays.asList(new File(resolveFileName(fileName))));
        m.evaluateAllProductDeclarations(); 

        assertEquals(loadExpectedErrors(fileName.replace(".abs", ".txt")), getLinesAndErrors(m.getTypeErrors()));
    }

    @Test
    public void new_rule_examples_flow_insensitive() throws Exception {
        String fileName = "abssamples/SecrecyTypeTests/Rule_examples/New_Examples.abs";
        
        Main main = new Main();
        main.arguments.flowInsensitive = true;
        Model m = main.parse(Arrays.asList(new File(resolveFileName(fileName))));
        m.evaluateAllProductDeclarations(); 

        assertEquals(loadExpectedErrors(fileName.replace(".abs", ".txt")), getLinesAndErrors(m.getTypeErrors()));
    }

    @Test
    public void return_rule_examples_flow_insensitive() throws Exception {
        String fileName = "abssamples/SecrecyTypeTests/Rule_examples/Return_Examples.abs";
        
        Main main = new Main();
        main.arguments.flowInsensitive = true;
        Model m = main.parse(Arrays.asList(new File(resolveFileName(fileName))));
        m.evaluateAllProductDeclarations(); 

        assertEquals(loadExpectedErrors(fileName.replace(".abs", ".txt")), getLinesAndErrors(m.getTypeErrors()));
    }

    @Test
    public void while_rule_examples_flow_insensitive() throws Exception {
        String fileName = "abssamples/SecrecyTypeTests/Rule_examples/While_Examples.abs";
        
        Main main = new Main();
        main.arguments.flowInsensitive = true;
        Model m = main.parse(Arrays.asList(new File(resolveFileName(fileName))));
        m.evaluateAllProductDeclarations(); 

        assertEquals(loadExpectedErrors(fileName.replace(".abs", "_flow_insensitive.txt")), getLinesAndErrors(m.getTypeErrors()));
    }

    @Test
    public void while_interloopleak_rule_examples_flow_insensitive() throws Exception {
        String fileName = "abssamples/SecrecyTypeTests/Rule_examples/WhileInterLoopLeak.abs";
        
        Main main = new Main();
        main.arguments.flowInsensitive = true;
        Model m = main.parse(Arrays.asList(new File(resolveFileName(fileName))));
        m.evaluateAllProductDeclarations(); 

        assertEquals(loadExpectedErrors(fileName.replace(".abs", "_flow_insensitive.txt")), getLinesAndErrors(m.getTypeErrors()));
    }

    //These are helper methods to verify the failing files fail on the exact erros we expect them to

    /**
     * @param errorList - the list of type errors that we received for one of the examples
     * @return - only the secrecy type errors and in this format (LineNumber: Description output)
     */
    private List<String> getLinesAndErrors(SemanticConditionList errorList) {
        List<String> actual = new LinkedList<String>();
        for (SemanticCondition cond : errorList) {
            if(cond.msg != null && isSecrecyError(cond.msg)){
                if (cond.isError() || cond.isWarning()) {  // Filter errors/warnings
                    String message = cond.getMessage().replaceAll("tmp\\d+", "tmpXXX");
                    //String key = cond.getLine() + ":" + cond.getMessage();  // Adjust getters as needed
                    String key = cond.getLine() + ":" + message;
                    actual.add(key);
                }
            }
        }
        return actual;
    }

    /**
     * Helper that checks if the output message is one of the secrecy error messages
     * @return - true if it is a secrecy message (and thus a secrecy type error), false otherwise
     */
    private boolean isSecrecyError(ErrorMessage msg) {
        return msg == ErrorMessage.SECRECY_WRONG_ANNOTATION_VALUE ||
               msg == ErrorMessage.SECRECY_LEAKAGE_ERROR_FROM_TO ||
               msg == ErrorMessage.SECRECY_LEAKAGE_ERROR_AT_MOST ||
               msg == ErrorMessage.SECRECY_LEAKAGE_ERROR_AT_LEAST || 
               msg == ErrorMessage.SECRECY_PARAMETER_TO_HIGH ||
               msg == ErrorMessage.SECRECY_FNAPP_NOT_EQUAL ||
               msg == ErrorMessage.SECRECY_AWAIT_FIELD_VIOLATION ||
               msg == ErrorMessage.SECRECY_LEVEL_NON_EXISTANT ||
               msg == ErrorMessage.SECRECY_CALLING_INSECURE_METHOD;
    }

    /**
     * Readin expected errors from a file with the expectedFilePath to later compare it.
     * 
     * @param expectedFilePath - the filepath of the .txt file to readin that contains the expected type errors for a test case
     * @return - Returns them in a list which we need to compare them to the actual gotten errors for the example
     */
    private List<String> loadExpectedErrors(String expectedFilePath) throws Exception {
        Path expectedPath = Paths.get("src/test/resources/", expectedFilePath);
        //return lines(expectedPath).map(String::trim).filter(line -> !line.isEmpty()).toList();
        return lines(expectedPath).map(String::trim).map(line -> line.replaceAll("tmp\\d+", "tmpXXX")).filter(line -> !line.isEmpty()).toList();
    }

}
